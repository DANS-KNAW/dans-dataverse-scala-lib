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

import nl.knaw.dans.lib.dataverse.model.dataset.{ CompoundField, ControlledMultipleValueField, Dataset, DatasetVersion, MetadataBlock, PrimitiveSingleValueField }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

object ImportDataset extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats

  val dataset = Dataset(
    DatasetVersion(
      metadataBlocks = Map(
        "citation" -> MetadataBlock(
          displayName = "Citation Metadata",
          fields = List(
            PrimitiveSingleValueField(
              "title",
              "Test  license import"
            ),
            CompoundField(
              typeName = "author",
              value = List(
                Map(
                  "authorName" -> PrimitiveSingleValueField("authorName", "Test Author")
                )
              )
            ),
            CompoundField(
              typeName = "datasetContact",
              value = List(
                Map(
                  "contactName" -> PrimitiveSingleValueField("contactName", "Test Author")
                )
              )
            ),
            CompoundField(
              typeName = "dsDescription",
              value = List(
                Map(
                  "dsDescriptionValue" -> PrimitiveSingleValueField("dsDescriptionValue", "Test descr")
                )
              )
            ),
            ControlledMultipleValueField(
              "subject",
              List("Law")
            )
          )
        )
      )
    )
  )

  private val result = for {
    response <- server.dataverse("root").importDataset(dataset, Option("doi:10.80270/test-zux-8us3"))
    _ = logger.info(s"Raw response message: ${ response.string }")
    _ = logger.info(s"JSON AST: ${ response.json }")
    _ = logger.info(s"JSON serialized: ${ Serialization.writePretty(response.json) }")
  } yield ()
  logger.info(s"result = $result")
}
