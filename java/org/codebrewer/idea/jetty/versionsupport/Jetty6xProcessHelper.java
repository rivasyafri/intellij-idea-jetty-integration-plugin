/*
 * Copyright 2010 Mark Scott
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
package org.codebrewer.idea.jetty.versionsupport;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Scott
 * @version $Id$
 */
public class Jetty6xProcessHelper extends AbstractProcessHelper
{
  @NonNls
  private static final String START_COMMAND = "-DSTOP.PORT=0 -cp start.jar org.mortbay.start.Main";

  @NonNls
  private static final String STARTING_MESSAGE = "-DSTOP.KEY=";

  @NotNull
  @Override
  public String getStartCommand()
  {
    return START_COMMAND;
  }

  @NotNull
  @Override
  public String getStopCommandTemplate()
  {
    return STOP_COMMAND_TEMPLATE;
  }

  @Override
  public boolean isStartingMessage(@Nullable String text)
  {
    return text != null && text.startsWith(STARTING_MESSAGE);
  }
}
