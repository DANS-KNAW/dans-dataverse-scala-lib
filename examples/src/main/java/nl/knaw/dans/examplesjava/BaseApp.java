/*
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.examplesjava;

import nl.knaw.dans.lib.scaladv.DataverseInstance;
import nl.knaw.dans.lib.scaladv.DataverseInstanceConfig;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import scala.Some;

import java.net.URI;
import java.net.URISyntaxException;

public class BaseApp {

  protected static DataverseInstance server;

  static {
    try {

      PropertiesConfiguration props = new PropertiesConfiguration("dataverse.properties");
      server = new DataverseInstance(new DataverseInstanceConfig(new URI(props.getString("baseUrl")), props.getString("apiKey"), new Some<String>(props.getString("unblockKey")), 5000, 300000, "1", 10, 500));
    }
    catch (ConfigurationException e) {
      e.printStackTrace();
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
}
