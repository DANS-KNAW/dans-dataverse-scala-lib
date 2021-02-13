/**
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.easydv.subcommand

import nl.knaw.dans.easydv.subCommandFooter
import org.rogach.scallop.{ ScallopOption, Subcommand }

import java.nio.file.Path

class DatasetSubcommand extends AbstractSubcommand("dataset") {
  shortSubcommandsHelp(true)
  descr("Operations on a dataset. See: https://guides.dataverse.org/en/latest/api/native-api.html#datasets")
  val id: ScallopOption[String] = trailArg("id",
    descr = "dataset identifier; if it consists of only numbers, it is taken to be a database ID, otherwise as a persistent ID")
  val version: ScallopOption[String] = opt(name = "version", descr = "specific version of the dataset")
  val latest: ScallopOption[Boolean] = opt(name = "latest", descr = "latest version of the dataset (default)")
  val latestPublished: ScallopOption[Boolean] = opt(name = "latest-published", short = 'p', descr = "latest published version of the dataset")
  val draft: ScallopOption[Boolean] = opt(name = "draft", descr = "the draft version of the dataset")
  val all: ScallopOption[Boolean] = opt(name = "all", descr = "all versions versions of the dataset")
  mutuallyExclusive(version, latestPublished, latest, draft, all)

  val view = addSimpleCommand(
    name = "view",
    description = "Get JSON Representation of a Dataset. See: https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset")

  val exportMetadata = new Subcommand("export-metadata") {
    descr("Export the metadata of the current published version of a dataset in various formats. See: https://guides.dataverse.org/en/latest/api/native-api.html#export-metadata-of-a-dataset-in-various-formats")
    val format: ScallopOption[String] = trailArg(
      name = "format",
      descr = "One of ddi, oai_ddi, dcterms, oai_dc, schema.org, OAI_ORE, Datacite, oai_datacite and dataverse_json",
      required = true)
  }
  addSubcommand(exportMetadata)

  val listFiles = addSimpleCommand(
    name = "list-files",
    description = "Lists all the file metadata, for the given dataset and version. See: https://guides.dataverse.org/en/latest/api/native-api.html#list-files-in-a-dataset"
  )

  // TODO: list-metadata-blocks
  // TODO: get-metadata-block
  // TODO: update-metadata
  // TODO: edit-metadata
  // TODO: delete-metadata
  // TODO: publish
  // TODO: delete-draft
  // TODO: set-citation-date-field
  // TODO: revert-citation-date-field
  // TODO: list-role-assignments
  // TODO: assign-role
  // TODO: delete-role-assignment
  // TODO: create-private-url
  // TODO: get-private-url
  // TODO: delete-private-url

  val addFile = new Subcommand("add-file") {
    descr("Adds file data and/or metadata to a dataset. See: https://guides.dataverse.org/en/latest/api/native-api.html#add-a-file-to-a-dataset")
    val dataFile: ScallopOption[Path] = trailArg(name = "data-file", descr = "data file (please, provide metadata JSON on the STDIN)", required = false)
    val metadata: ScallopOption[Boolean] = opt(name = "metadata-from-stdin", descr = "Read metadata from STDIN")
  }
  addSubcommand(addFile)

  // TODO: storage-size
  // TODO: download-size
  // TODO: submit-for-review
  // TODO: return-to-author
  // TODO: link
  // TODO: get-locks
  // TODO: delete
  // TODO: destroy
  // TODO: await-unlock
  // TODO: await-lock

  footer(subCommandFooter)
}
