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

import nl.knaw.dans.lib.scaladv.DataverseResponse
import nl.knaw.dans.lib.scaladv.model.search.{ DatasetResultItem, SearchResult }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

import scala.collection.mutable.ListBuffer
import scala.util.Try

object SearchPaginated extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats

  val allDois = ListBuffer[String]()
  val perPage = 1000
  var start = 0
  var go = true

  while (go) {
    val maybeDois = getDois(server.search().find("publicationStatus:\"Published\"", start = start, perPage = perPage))
      .map {
        dois =>
          allDois.appendAll(dois)
          start += perPage
          dois.nonEmpty
      }
    go = maybeDois.getOrElse(false)
  }

  println(s"Size of allDois = ${ allDois.size }")

  private def getDois(maybeSearchResponse: Try[DataverseResponse[SearchResult]]): Try[List[String]] = {
    for {
      r <- maybeSearchResponse
      searchResult <- r.data
      dois = searchResult.items.map(_.asInstanceOf[DatasetResultItem]).map(_.globalId)
    } yield dois
  }
}
