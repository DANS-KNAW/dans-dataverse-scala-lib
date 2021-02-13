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
package nl.knaw.dans.easydv.dispatcher

import nl.knaw.dans.easydv.Command.FeedBackMessage
import nl.knaw.dans.easydv.CommandLineOptions
import nl.knaw.dans.lib.dataverse.{ DatasetApi, Version }
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.lang.StringUtils
import org.json4s.native.Serialization
import org.json4s.{ DefaultFormats, Formats }

import java.io.PrintStream
import scala.language.reflectiveCalls
import scala.util.{ Failure, Success, Try }

object Dataset extends DebugEnhancedLogging {
  private implicit val jsonFormats: Formats = DefaultFormats

  def dispatch(commandLine: CommandLineOptions, d: DatasetApi, datasetSetVersion: Option[Version])(implicit resultOutput: PrintStream): Try[FeedBackMessage] = {
    trace(())
    commandLine.subcommands match {
      case (ds @ commandLine.dataset) :: commandLine.dataset.view :: Nil =>
        for {
          response <-
            if (ds.all()) d.viewAllVersions()
            else d.view(version = datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "view"
      case commandLine.dataset :: (c @ commandLine.dataset.exportMetadata) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else if (List("schema.org", "OAI_ORE", "dataverse_json").contains(c.format())) {
          for {
            response <- d.exportMetadata(c.format())
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "export-metadata"
        }
        else {
          for {
            response <- d.exportMetadata(c.format())
            s <- response.string
            _ = resultOutput.println(s)
          } yield "export-metadata"
        }
      case commandLine.dataset :: commandLine.dataset.listFiles :: Nil =>
        for {
          response <- d.listFiles(datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-files"
      case commandLine.dataset :: commandLine.dataset.listMetadataBlocks :: Nil =>
        for {
          response <- d.listMetadataBlocks(datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "list-metadata-blocks"
      case commandLine.dataset :: (c @ commandLine.dataset.getMetadataBlock) :: Nil =>
        for {
          response <- d.getMetadataBlock(c.name(), datasetSetVersion.getOrElse(Version.LATEST))
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "get-metadata-block"
      case commandLine.dataset :: (c @ commandLine.dataset.updateMetadata) :: Nil =>
        if (datasetSetVersion.isDefined) Failure(new IllegalArgumentException("Versions not supported for this subcommand"))
        else {
          for {
            s <- getStringFromStd
            response <- d.updateMetadata(s)
            json <- response.json
            _ = resultOutput.println(Serialization.writePretty(json))
          } yield "update-metadata"
        }

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

      case commandLine.dataset :: commandLine.dataset.addFile :: Nil =>
        for {
          metadata <- if (commandLine.dataset.addFile.metadata()) getStringFromStd
                      else Success("")
          _ = debug(metadata)
          optMeta = if (StringUtils.isBlank(metadata)) Option.empty
                    else Option(metadata)
          optDataFile = commandLine.dataset.addFile.dataFile.map(p => better.files.File(p)).toOption
          response <- d.addFileItem(optDataFile, optMeta)
          json <- response.json
          _ = resultOutput.println(Serialization.writePretty(json))
        } yield "add-file"

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

      case _ => Failure(new RuntimeException(s"Unkown dataverse sub-command: ${ commandLine.args.tail.mkString(" ") }"))
    }
  }
}