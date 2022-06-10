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
package nl.knaw.dans.lib.scaladv

import better.files.File
import org.json4s.{ DefaultFormats, Formats }
import scalaj.http.HttpResponse

import scala.util.Try

/**
 * HTTP support that targets a specific type of object, such as datasets. The target is specified with an ID.
 */
private[scaladv] trait TargetedHttpSupport extends HttpSupport {
  private implicit val jsonFormats: Formats = DefaultFormats

  protected val targetBase: String
  protected val id: String
  protected val isPersistentId: Boolean
  protected val extraHeaders: Map[String, String] = Map.empty

  /**
   * Get a specific version of something.
   *
   * @param endPoint the API endpoint
   * @param version  the version or version label
   * @tparam D the type of model object to expect in the response message
   * @return
   */
  protected def getVersionedFromTarget[D: Manifest](endPoint: String, version: Version = Version.LATEST): Try[DataverseResponse[D]] = {
    trace(endPoint, version)
    if (isPersistentId) super.get[D](
      subPath = s"${ targetBase }/:persistentId/versions/${ version }/${ endPoint }",
      params = Map("persistentId" -> id),
      headers = extraHeaders)
    else super.get[D](
      subPath = s"${ targetBase }/$id/versions/${ version }/${ endPoint }",
      headers = extraHeaders)
  }

  /**
   * Get something for which versions do not apply
   *
   * @param endPoint the API endpoint
   * @tparam D the type of model object to expect in the response message
   * @return
   */
  protected def getUnversionedFromTarget[D: Manifest](endPoint: String, queryParams: Map[String, String] = Map.empty): Try[DataverseResponse[D]] = {
    trace(endPoint)
    if (isPersistentId) super.get[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders)
    else super.get[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      headers = extraHeaders)
  }

  protected def getUnwrappedFromTarget(endPoint: String, queryParams: Map[String, String] = Map.empty): Try[HttpResponse[Array[Byte]]] = {
    trace(endPoint)
    if (isPersistentId) super.getUnwrapped(
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders)
    else super.getUnwrapped(
      subPath = s"${ targetBase }/$id/${ endPoint }",
      headers = extraHeaders)
  }

  protected def postJsonToTarget[D: Manifest](endPoint: String, body: String, queryParams: Map[String, String] = Map.empty, headers: Map[String, String] = Map.empty, isJsonLd: Boolean = false): Try[DataverseResponse[D]] = {
    if (isPersistentId) super.postJson[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      body,
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders ++ headers,
      isJsonLd = isJsonLd)
    else super.postJson[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      body,
      params = queryParams,
      headers = extraHeaders ++ headers,
      isJsonLd = isJsonLd)
  }

  protected def postFileToTarget[D: Manifest](endPoint: String, optFile: Option[File], optMetadata: Option[String], queryParams: Map[String, String] = Map.empty): Try[DataverseResponse[D]] = {
    if (isPersistentId) super.postFile[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      optFile,
      optMetadata,
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders)
    else super.postFile[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      optFile,
      optMetadata,
      params = queryParams,
      headers = extraHeaders)
  }

  protected def putToTarget[D: Manifest](endPoint: String, body: String, queryParams: Map[String, String] = Map.empty): Try[DataverseResponse[D]] = {
    if (isPersistentId) super.put[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      body,
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders)
    else super.put[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      body,
      params = queryParams,
      headers = extraHeaders)
  }

  protected def putJsonToTarget[D: Manifest](endPoint: String, body: String, queryParams: Map[String, String] = Map.empty, isJsonLd: Boolean = false): Try[DataverseResponse[D]] = {
    if (isPersistentId) super.putJson[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      body,
      params = Map("persistentId" -> id) ++ queryParams,
      headers = extraHeaders,
      isJsonLd = isJsonLd)
    else super.putJson[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      body,
      params = queryParams,
      headers = extraHeaders,
      isJsonLd = isJsonLd)
  }


  protected def deleteAtTarget[D: Manifest](endPoint: String): Try[DataverseResponse[D]] = {
    if (isPersistentId) super.deletePath[D](
      subPath = s"${ targetBase }/:persistentId/${ endPoint }",
      params = Map("persistentId" -> id),
      headers = extraHeaders)
    else super.put[D](
      subPath = s"${ targetBase }/$id/${ endPoint }",
      headers = extraHeaders)
  }
}
