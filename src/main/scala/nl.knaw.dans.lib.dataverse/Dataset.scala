/**
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
package nl.knaw.dans.lib.dataverse

import java.net.URI

import better.files.File
import nl.knaw.dans.lib.dataverse.model.DataMessage
import nl.knaw.dans.lib.dataverse.model.dataset.{ DatasetVersion, DataverseFile, FieldList, MetadataBlock, MetadataBlocks }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import scalaj.http.HttpResponse

import scala.util.{ Failure, Try }

/**
 * Functions that operate on a single dataset. See [[https://guides.dataverse.org/en/latest/api/native-api.html#datasets]].
 *
 */
class Dataset private[dataverse](id: String, isPersistentId: Boolean, configuration: DataverseInstanceConfig) extends HttpSupport with DebugEnhancedLogging {
  private implicit val jsonFormats: DefaultFormats = DefaultFormats

  protected val connectionTimeout: Int = configuration.connectionTimeout
  protected val readTimeout: Int = configuration.readTimeout
  protected val baseUrl: URI = configuration.baseUrl
  protected val apiToken: String = configuration.apiToken
  protected val apiVersion: String = configuration.apiVersion

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset]]
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-version-of-a-dataset]]
   * @param version version to view (optional)
   * @return
   */
  def view(version: Version = Version.UNSPECIFIED): Try[DataverseResponse[model.dataset.DatasetVersion]] = {
    trace(version)
    getVersioned[model.dataset.DatasetVersion]("", version)
  }

  /**
   * Almost the same as [[Dataset#view]] except that `viewLatestVersion` returns a JSON object that starts at the dataset
   * level instead of the dataset version level. The dataset level contains some fields, most of which are replicated at the dataset version level, however.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#get-json-representation-of-a-dataset]]
   * @return
   */
  def viewLatestVersion(): Try[DataverseResponse[model.dataset.DatasetLatestVersion]] = {
    trace(())
    getUnversioned[model.dataset.DatasetLatestVersion]("")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-versions-of-a-dataset]]
   * @return
   */
  def viewAllVersions(): Try[DataverseResponse[List[DatasetVersion]]] = {
    trace(())
    getUnversioned[List[DatasetVersion]]("versions")
  }

  /**
   * Since the export format is generally not JSON you cannot use the [[DataverseResponse#json]] and [[DataverseResponse#data]]
   * on the result. You should instead use [[DataverseResponse#string]].
   *
   * Note that this API does not support specifying a version.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#export-metadata-of-a-dataset-in-various-formats]]
   * @param format the export format
   * @return
   */
  def exportMetadata(format: String): Try[DataverseResponse[Any]] = {
    trace(())
    if (!isPersistentId) Failure(new IllegalArgumentException("exportMetadata only works with PIDs"))
    // Cannot use helper function because this API does not support the :persistentId constant
    get2[Any](s"datasets/export/?exporter=$format&persistentId=$id")
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-files-in-a-dataset]]
   * @param version the version of the dataset
   * @return
   */
  def listFiles(version: Version = Version.UNSPECIFIED): Try[DataverseResponse[List[DataverseFile]]] = {
    trace(version)
    getVersioned[List[DataverseFile]]("files", version)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-all-metadata-blocks-for-a-dataset]]
   * @param version the version of the dataset
   * @return a map of metadata block identifier to metadata block
   */
  def listMetadataBlocks(version: Version = Version.UNSPECIFIED): Try[DataverseResponse[Map[String, MetadataBlock]]] = {
    trace((version))
    getVersioned[Map[String, MetadataBlock]]("metadata", version)
  }

  /**
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#list-single-metadata-block-for-a-dataset]]
   * @param name    the metadata block identifier
   * @param version the version of the dataset
   * @return
   */
  def getMetadataBlock(name: String, version: Version = Version.UNSPECIFIED): Try[DataverseResponse[MetadataBlock]] = {
    trace(name, version)
    getVersioned[MetadataBlock](s"metadata/$name", version)
  }

  /**
   * Creates or overwrites the current draft's metadata completely.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#update-metadata-for-a-dataset]]
   * @param metadataBlocks map from metadata block id to [[MetadataBlock]]
   * @return
   */
  def updateMetadata(metadataBlocks: MetadataBlocks): Try[DataverseResponse[DatasetVersion]] = {
    trace(metadataBlocks)
    putVersioned[DatasetVersion]("", Serialization.write(Map("metadataBlocks" -> metadataBlocks)), Version.DRAFT)
  }

  /**
   * Edits the current draft's metadata, adding the fields that do not exist yet. If `replace` is set to `false`, all specified
   * fields must be either currently empty or allow multiple values.
   *
   * @see [[https://guides.dataverse.org/en/latest/api/native-api.html#edit-dataset-metadata]]
   * @param fields  list of fields to edit
   * @param replace wether to replace existing values
   * @return
   */
  def editMetadata(fields: FieldList, replace: Boolean = true): Try[DataverseResponse[DatasetVersion]] = {
    trace(fields)
    putVersioned("editMetadata", Serialization.write(fields), Version.UNSPECIFIED, if (replace) Map("replace" -> "true")
                                                                                   else Map.empty) // Sic! any value for replace is interpreted by Dataverse as "true"
  }

  def delete(): Try[DataverseResponse[DataMessage]] = {
    trace(())
    if (isPersistentId) deletePath2[DataMessage](s"datasets/:persistentId/?persistentId=$id")
    else deletePath2[DataMessage](s"datasets/$id")
  }

  //  def editMetadata(json: File, replace: Boolean): Try[HttpResponse[Array[Byte]]] = {
  //    tryReadFileToString(json).flatMap(s => editMetadata(s, replace))
  //  }
  //
  //  def editMetadata(json: String, replace: Boolean = false): Try[HttpResponse[Array[Byte]]] = {
  //    trace(json, replace)
  //    val path = if (isPersistentId) s"datasets/:persistentId/editMetadata/?persistentId=$id${
  //      if (replace) "&replace=$replace"
  //      else ""
  //    }"
  //               else s"datasets/$id/editMetadata/${
  //                 if (replace) "?replace=$replace"
  //                 else ""
  //               }"
  //    put(path)(json)
  //  }

  def deleteMetadata(json: File): Try[HttpResponse[Array[Byte]]] = {
    trace(json)
    val path = if (isPersistentId) s"datasets/:persistentId/deleteMetadata/?persistentId=$id"
               else s"datasets/$id/deleteMetadata"
    tryReadFileToString(json).flatMap(put(path))
  }

  def publish(updateType: String): Try[HttpResponse[Array[Byte]]] = {
    trace(updateType)
    val path = if (isPersistentId) s"datasets/:persistentId/actions/:publish/?persistentId=$id&type=$updateType"
               else s"datasets/$id/actions/:publish?type=$updateType"
    postJson(path)(null)
  }

  def deleteDraft(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/versions/:draft/?persistentId=$id"
               else s"datasets/$id/versions/:draft/"
    deletePath(path)
  }

  def setCitationDateField(fieldName: String): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/citationdate?persistentId=$id"
               else s"datasets/$id/citationdate"
    put(path)(s"$fieldName")
  }

  def revertCitationDateField(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/citationdate?persistentId=$id"
               else s"datasets/$id/citationdate"
    deletePath(path)
  }

  def listRoleAssignments(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/assignments?persistentId=$id"
               else s"datasets/$id/assignments"
    get(path)
  }

  def createPrivateUrl(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/privateUrl?persistentId=$id"
               else s"datasets/$id/privateUrl"
    postJson(path)(null)
  }

  def getPrivateUrl: Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/privateUrl?persistentId=$id"
               else s"datasets/$id/privateUrl"
    get(path)
  }

  def deletePrivateUrl(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/privateUrl?persistentId=$id"
               else s"datasets/$id/privateUrl"
    deletePath(path)
  }

  def addFile(dataFile: File, jsonMetadata: Option[File], jsonString: Option[String]): Try[HttpResponse[Array[Byte]]] = {
    trace(dataFile, jsonMetadata, jsonString)
    val path = if (isPersistentId) s"datasets/:persistentId/add?persistentId=$id"
               else s"datasets/$id/add"
    jsonMetadata.map {
      f =>
        tryReadFileToString(f).flatMap {
          s => postFile(path, dataFile, Some(s))(200, formatResponseAsJson = true)
        }
    }.getOrElse {
      postFile(path, dataFile, jsonString)(200, formatResponseAsJson = true)
    }
  }

  def submitForReview(): Try[HttpResponse[Array[Byte]]] = {
    trace(())
    val path = if (isPersistentId) s"datasets/:persistentId/submitForReview?persistentId=$id"
               else s"datasets/$id/submitForReview"
    postJson(path)(null)
  }

  def returnToAuthor(reason: String): Try[HttpResponse[Array[Byte]]] = {
    trace(reason)
    val path = if (isPersistentId) s"datasets/:persistentId/returnToAuthor?persistentId=$id"
               else s"datasets/$id/returnToAuthor"
    postJson(path)(s"""{"reasonForReturn": "$reason"}""")
  }

  def link(dataverseAlias: String): Try[HttpResponse[Array[Byte]]] = {
    trace(dataverseAlias)
    val path = if (isPersistentId) s"datasets/:persistentId/link/$dataverseAlias?persistentId=$id"
               else s"datasets/$id/link/$dataverseAlias"
    put(path)()
  }

  def getLocks(lockType: Option[String] = None): Try[HttpResponse[Array[Byte]]] = {
    trace(lockType)
    val path = if (isPersistentId) s"datasets/:persistentId/locks?persistentId=$id${ lockType.map(t => "&type=$t").getOrElse("") }"
               else s"datasets/$id/locks${ lockType.map(t => "?type=$t").getOrElse("") }"
    get(path)
  }

  /*
   * Helper functions.
   */

  private def getVersioned[D: Manifest](endPoint: String, version: Version = Version.UNSPECIFIED): Try[DataverseResponse[D]] = {
    trace(endPoint, version)
    if (isPersistentId) super.get2[D](s"datasets/:persistentId/versions/${
      if (version == Version.UNSPECIFIED) ""
      else version
    }/${ endPoint }?persistentId=$id")
    else super.get2[D](s"datasets/$id/versions/${
      if (version == Version.UNSPECIFIED) ""
      else version
    }/${ endPoint }")
  }

  private def getUnversioned[D: Manifest](endPoint: String, queryParams: Map[String, String] = Map.empty): Try[DataverseResponse[D]] = {
    trace(endPoint)
    if (isPersistentId) super.get2[D](s"datasets/:persistentId/${ endPoint }/?persistentId=$id")
    else super.get2[D](s"datasets/$id/${ endPoint }")
  }

  private def putVersioned[D: Manifest](endPoint: String, body: String, version: Version = Version.UNSPECIFIED, queryParams: Map[String, String] = Map.empty): Try[DataverseResponse[D]] = {
    val queryString = queryParams.map { case (k, v) => s"$k=$v" }.mkString("&")
    trace(endPoint, version)
    if (isPersistentId) super.put2[D](s"datasets/:persistentId/${
      if (version == Version.UNSPECIFIED) ""
      else s"versions/$version"
    }/${ endPoint }?persistentId=$id${
      if (queryString.nonEmpty) "&" + queryString
      else ""
    }")(body)
    else super.put2[D](s"datasets/$id/${
      if (version == Version.UNSPECIFIED) ""
      else s"versions/$version"
    }/${ endPoint }$queryString")(body)
  }
}
