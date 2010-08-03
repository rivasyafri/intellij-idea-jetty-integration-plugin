/*
 * Copyright 2008, 2010 Ben Gidley
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

/**
 * <p>
 * Version of this class to support Hightide (commercially supported version of
 * Jetty).
 * </p>
 *
 * @author Ben Gidley
 * @version $Id$
 */
public class Hightide6xVersionFileChecker extends AbstractJettyVersionFileChecker
{
  @NonNls private static final String VERSION_FILE_NAME = "VERSION.txt";
  @NonNls private static final String VERSION_PATTERN = "^hightide-(.*) - .*";

  @NotNull
  public String getVersionFileName()
  {
    return VERSION_FILE_NAME;
  }

  @NotNull public String getVersionPattern()
  {
    return VERSION_PATTERN;
  }
}