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

import nl.knaw.dans.lib.dataverse.model.dataset.Embargo
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

object SetEmbargo extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats

  val doi = args(0)
  val dateAvailable = args(1)
  val reason = args(2)
  val fileIds = args(3).split(",").map(_.toInt)
  val embargo = Embargo(
    dateAvailable = dateAvailable,
    reason = reason,
    fileIds = fileIds.toList
  )

  private val result = for {
    response <- server.dataset(doi).setEmbargo(embargo)
    _ = logger.info(s"Raw response message: ${ response.string }")
    _ = logger.info(s"JSON AST: ${ response.json }")
  } yield ()
  logger.info(s"result = $result")
}
