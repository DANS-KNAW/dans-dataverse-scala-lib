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
package nl.knaw.dans.examples

import nl.knaw.dans.lib.scaladv.{ DataverseInstance, DataverseInstanceConfig }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.configuration.PropertiesConfiguration

import java.net.URI

trait BaseApp extends DebugEnhancedLogging {
  logger.info(s"Starting ${ getClass.getName }")
  private val props = new PropertiesConfiguration("examples/dataverse.properties")

  val server = new DataverseInstance(
    DataverseInstanceConfig(
      baseUrl = new URI(props.getString("baseUrl")),
      apiToken = props.getString("apiKey"),
      unblockKey = Option(props.getString("unblockKey"))
    )
  )
}
