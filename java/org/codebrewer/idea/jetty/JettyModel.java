/*
 * Copyright 2007, 2010 Mark Scott, Peter Niederwieser, Chris Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codebrewer.idea.jetty;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.appServerIntegrations.ApplicationServer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.execution.DefaultOutputProcessor;
import com.intellij.javaee.run.execution.OutputProcessor;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import org.codebrewer.idea.jetty.versionsupport.JettyVersionHelper;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mark Scott
 * @author Peter Niederwieser
 * @author Chris Miller
 * @version $Id$
 */
public class JettyModel implements ServerModel
{
  @NonNls
  private static final String EXCEPTION_TEXT_NO_APPLICATION_SERVER =
    "exception.text.application.server.not.specified";

  @NonNls
  private static final String EXCEPTION_TEXT_NO_SAVED_JETTY_CONFIGURATION_STATE =
    "exception.text.no.saved.jetty.configuration.state";

  private CommonModel commonModel;
  private String stopKey;
  private int stopPort;
  private File scratchDirectory;

  @NotNull
  private JettyPersistentData getJettyPersistentData() throws RuntimeConfigurationError
  {
    final ApplicationServer applicationServer = commonModel.getApplicationServer();

    if (applicationServer == null) {
      throw new RuntimeConfigurationError(JettyBundle.message(EXCEPTION_TEXT_NO_APPLICATION_SERVER));
    }

    final JettyPersistentData jettyPersistentData = (JettyPersistentData) applicationServer.getPersistentData();

    if (jettyPersistentData == null) {
      throw new RuntimeConfigurationError(JettyBundle.message(EXCEPTION_TEXT_NO_SAVED_JETTY_CONFIGURATION_STATE));
    }

    return jettyPersistentData;
  }

  @NotNull
  public File[] getActiveConfigFiles() throws RuntimeConfigurationError
  {
    final JettyPersistentData jettyPersistentData = getJettyPersistentData();
    final List<JettyPersistentData.JettyConfigurationFile> configFiles =
      jettyPersistentData.getJettyConfigurationFiles();
    final List<JettyPersistentData.JettyConfigurationFile> activeConfigFiles =
      new ArrayList<JettyPersistentData.JettyConfigurationFile>();

    for (final JettyPersistentData.JettyConfigurationFile configFile : configFiles) {
      if (configFile.isActive()) {
        activeConfigFiles.add(configFile);
      }
    }

    final File[] result = new File[activeConfigFiles.size()];

    for (int i = 0; i < activeConfigFiles.size(); i++) {
      result[i] = activeConfigFiles.get(i).getFile();
    }

    return result;
  }

  @NotNull
  public String[] getActiveConfigFilePaths() throws RuntimeConfigurationError
  {
    final File[] activeConfigFiles = getActiveConfigFiles();
    final String[] result = new String[activeConfigFiles.length];

    for (int i = 0; i < activeConfigFiles.length; i++) {
      result[i] = activeConfigFiles[i].getAbsolutePath();
    }

    return result;
  }

  public String getHomeDirectory() throws RuntimeConfigurationException
  {
    return getJettyPersistentData().getJettyHome().replace('/', File.separatorChar);
  }

  @Nullable
  public JettyVersionHelper getJettyVersionHelper() throws JettyException
  {
    try {
      return getJettyPersistentData().getJettyVersionHelper();
    }
    catch (RuntimeConfigurationError e) {
      throw new JettyException(e.getMessage(), e);
    }
  }

  public Project getProject()
  {
    return commonModel.getProject();
  }

  public File getScratchDirectory()
  {
    return scratchDirectory;
  }

  public String getStopKey()
  {
    return stopKey;
  }

  public int getStopPort()
  {
    return stopPort;
  }

  public boolean isLocal()
  {
    return commonModel.isLocal();
  }

  public void setScratchDirectory(File scratchDirectory)
  {
    this.scratchDirectory = scratchDirectory;
  }

  public void setStopKey(final String stopKey)
  {
    this.stopKey = stopKey;
  }

  public void setStopPort(final int stopPort)
  {
    this.stopPort = stopPort;
  }

  public void readExternal(final Element element) throws InvalidDataException
  {
    DefaultJDOMExternalizer.readExternal(this, element);
  }

  public void writeExternal(final Element element) throws WriteExternalException
  {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }

  public J2EEServerInstance createServerInstance() throws ExecutionException
  {
    try {
      final JettyVersionHelper versionHelper = getJettyVersionHelper();

      if (versionHelper == null) {
        throw new ExecutionException("The chosen application server is not configured with a supported version of Jetty");
      }

      final JettyServerInstance jettyServerInstance = new JettyServerInstance(versionHelper, commonModel);

      if (commonModel.isLocal()) {
        JettyDeploymentProvider.prepareServer(this);
      }

      return jettyServerInstance;
    }
    catch (JettyException e) {
      throw new ExecutionException(e.getMessage(), e);
    }
  }

  public DeploymentProvider getDeploymentProvider()
  {
    return commonModel.isLocal() ? JettyManager.getInstance().getDeploymentProvider() : null;
  }

  @NonNls
  public String getDefaultUrlForBrowser()
  {
    // Todo - add context?

    final StringBuilder result = new StringBuilder(JettyConstants.HTTP_SCHEME);
    result.append(commonModel.getHost());
    result.append(':');
    result.append(String.valueOf(commonModel.getPort()));
    result.append('/');

    return result.toString();
  }

  public SettingsEditor<CommonModel> getEditor()
  {
    // Only local run configurations are supported
    //
    return commonModel.isLocal() ? new JettyLocalRunConfigurationEditor() : null;
  }

  public OutputProcessor createOutputProcessor(
    final ProcessHandler j2EEOSProcessHandlerWrapper, final J2EEServerInstance serverInstance)
  {
    return new DefaultOutputProcessor(j2EEOSProcessHandlerWrapper);
  }

  public List<Pair<String, Integer>> getAddressesToCheck()
  {
    // I guess this checks that any TCP ports needed for deployment aren't
    // already in use.

    final List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
    result.add(Pair.create(commonModel.getHost(), commonModel.getPort()));

    // Todo - add the shutdown port chosen (if it's ever made user-configurable)

    return result;
  }

  public void checkConfiguration() throws RuntimeConfigurationException
  {
    final Set<String> contexts = new HashSet<String>();

    // Todo - add those contexts already 'claimed' by active Jetty configuration files

    for (final DeploymentModel deploymentModel : commonModel.getDeploymentModels()) {
      if (deploymentModel instanceof JettyModuleDeploymentModel) {
        final JettyModuleDeploymentModel model = (JettyModuleDeploymentModel) deploymentModel;
        final String contextPath = model.getContextPath();

        if (!contexts.add(contextPath)) {
          throw new RuntimeConfigurationError(
            JettyBundle.message("error.text.duplicated.context.path", contextPath));
        }

        if (!contextPath.startsWith("/")) {
          throw new RuntimeConfigurationError(JettyBundle.message("error.text.missing.leading.slash", contextPath));
        }
      }
    }
  }

  public int getDefaultPort()
  {
    return JettyConstants.DEFAULT_PORT;
  }

  @SuppressWarnings({ "ParameterNameDiffersFromOverriddenParameter" })
  public void setCommonModel(final CommonModel commonModel)
  {
    this.commonModel = commonModel;
  }

  @Override
  public JettyModel clone() throws CloneNotSupportedException
  {
    return (JettyModel) super.clone();
  }

  public int getLocalPort()
  {
    try {
      return JettyUtil.getPort(getActiveConfigFiles());
    }
    catch (RuntimeConfigurationException ignore) {
      return getDefaultPort();
    }
  }
}
