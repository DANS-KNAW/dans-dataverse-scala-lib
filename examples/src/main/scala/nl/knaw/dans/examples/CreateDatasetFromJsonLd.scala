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

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

object CreateDatasetFromJsonLd extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats

  private val result = for {
    response <- server.dataverse("root").createDatasetFromJsonLd(
      """{
        |  "http://purl.org/dc/terms/title": "Darwin's Finches",
        |  "http://purl.org/dc/terms/subject": "Medicine, Health and Life Sciences",
        |  "http://purl.org/dc/terms/creator": {
        |      "https://dataverse.org/schema/citation/author#Name": "Finch, Fiona",
        |      "https://dataverse.org/schema/citation/author#Affiliation": "Birds Inc."
        |  },
        |  "https://dataverse.org/schema/citation/Contact": {
        |    "https://dataverse.org/schema/citation/datasetContact#E-mail": "finch@mailinator.com",
        |    "https://dataverse.org/schema/citation/datasetContact#Name": "Finch, Fiona"
        |  },
        |  "https://dataverse.org/schema/citation/Description": {
        |    "https://dataverse.org/schema/citation/dsDescription#Text": "Darwin's finches (also known as the Galápagos finches) are a group of about fifteen species of passerine birds."
        |  }
        |}
        |""".stripMargin)
    _ = logger.info(s"Raw response message: ${ response.string }")
    _ = logger.info(s"JSON AST: ${ response.json }")
  } yield ()
  logger.info(s"result = $result")
}
