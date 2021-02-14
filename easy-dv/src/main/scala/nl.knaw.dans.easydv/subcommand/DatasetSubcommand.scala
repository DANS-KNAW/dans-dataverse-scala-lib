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
  shortSubcommandsHelp(false)
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
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset")

  val exportMetadata = new Subcommand("export-metadata") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#export-metadata-of-a-dataset-in-various-formats")
    val format: ScallopOption[String] = trailArg(
      name = "format",
      descr = "One of ddi, oai_ddi, dcterms, oai_dc, schema.org, OAI_ORE, Datacite, oai_datacite and dataverse_json",
      required = true)
  }
  addSubcommand(exportMetadata)

  val listFiles = addSimpleCommand(
    name = "list-files",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#list-files-in-a-dataset"
  )

  val listMetadataBlocks = addSimpleCommand(
    name = "list-metadata-blocks",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#list-all-metadata-blocks-for-a-dataset"
  )

  val getMetadataBlock = new Subcommand("get-metadata-block") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#list-single-metadata-block-for-a-dataset")
    val name: ScallopOption[String] = trailArg(
      name = "name",
      descr = "name of the block",
      required = true
    )
  }
  addSubcommand(getMetadataBlock)

  val updateMetadata = addSimpleCommand(
    name = "update-metadata",
    description = "Reads the input JSON from STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#update-metadata-for-a-dataset"
  )

  val editMetadata = new Subcommand("edit-metadata") {
    descr("Reads the input JSON from STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#edit-dataset-metadata")
    val replace: ScallopOption[Boolean] = opt(
      name = "replace",
      descr = "replace existing data"
    )
  }
  addSubcommand(editMetadata)

  val deleteMedata = addSimpleCommand(
    name = "delete-metadata",
    description = "Reads th input JSON from STDIN. See: https://guides.dataverse.org/en/latest/api/native-api.html#delete-dataset-metadata"
  )

  val publish = new Subcommand("publish") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#publish-a-dataset")
    val major: ScallopOption[Boolean] = opt(name = "major", descr = "publish as major version")
    val minor: ScallopOption[Boolean] = opt(name = "minor", descr = "publish as minor version")
    mutuallyExclusive(major, minor)
    requireAtLeastOne(major, minor)
  }
  addSubcommand(publish)

  val deleteDraft = addSimpleCommand(
    name = "delete-draft",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#delete-dataset-draft"
  )

  val setCitationDateField = new Subcommand("set-citation-date-field") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#set-citation-date-field-type-for-a-dataset")
    val field: ScallopOption[String] = trailArg(name = "field-name", descr = "name of the field to use")
  }
  addSubcommand(setCitationDateField)

  val revertCitationDateField = addSimpleCommand(
    name = "revert-citation-date-field",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#revert-citation-date-field-type-to-default-for-dataset")

  val listRoleAssignments = addSimpleCommand(
    name = "list-role-assignments",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#list-role-assignments-in-a-dataset")

  val assignRole = addSimpleCommand(
    name = "assign-role",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#assign-a-new-role-on-a-dataset")

  val deleteRoleAssignment = new Subcommand("delete-role-assignment") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#delete-role-assignment-from-a-dataset")
    val id: ScallopOption[Int] = trailArg(name = "role-id", descr = "the ide of the role, use list-role-assignments to retrieve", required = false)
  }
  addSubcommand(deleteRoleAssignment)

  val createPrivateUrl = addSimpleCommand(
    name = "create-private-url",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#create-a-private-url-for-a-dataset")

  val getPrivateUrl = addSimpleCommand(
    name = "get-private-url",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#get-the-private-url-for-a-dataset")

  val deletePrivateUrl = addSimpleCommand(
    name = "delete-private-url",
    description = "See: https://guides.dataverse.org/en/latest/api/native-api.html#delete-the-private-url-from-a-dataset")

  val addFile = new Subcommand("add-file") {
    descr("See: https://guides.dataverse.org/en/latest/api/native-api.html#add-a-file-to-a-dataset")
    val dataFile: ScallopOption[Path] = trailArg(name = "data-file", descr = "data file", required = false)
    val metadata: ScallopOption[Boolean] = opt(name = "metadata-from-stdin", descr = "Read metadata from STDIN")
    requireAtLeastOne(dataFile, metadata)
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
