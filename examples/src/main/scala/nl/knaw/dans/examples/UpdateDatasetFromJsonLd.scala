package nl.knaw.dans.examples

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.json4s.{ DefaultFormats, Formats }

object UpdateDatasetFromJsonLd extends App with DebugEnhancedLogging with BaseApp {
  private implicit val jsonFormats: Formats = DefaultFormats

  val persistentId = args(0)

  private val result = for {
    response <- server.dataset(persistentId).updateMetadataFromJsonLd(
      """{
        |  "http://purl.org/dc/terms/title": "Darwin's Finches UPDATED 2",
        |  "http://purl.org/dc/terms/subject": "Medicine, Health and Life Sciences",
        |  "http://purl.org/dc/terms/creator": {
        |      "https://dataverse.org/schema/citation/author#Name": "Finch, Fiona  UPDATED",
        |      "https://dataverse.org/schema/citation/author#Affiliation": "Birds Inc  UPDATED."
        |  },
        |  "https://dataverse.org/schema/citation/Contact": {
        |    "https://dataverse.org/schema/citation/datasetContact#E-mail": "finch@mailinator.com",
        |    "https://dataverse.org/schema/citation/datasetContact#Name": "Finch, Fiona"
        |  },
        |  "https://dataverse.org/schema/citation/Description": {
        |    "https://dataverse.org/schema/citation/dsDescription#Text": "Darwin's finches (also known as the Galápagos finches) are a group of about fifteen species of passerine birds."
        |  }
        |}
        |""".stripMargin, replace = true)
    _ = logger.info(s"Raw response message: ${ response.string }")
    _ = logger.info(s"JSON AST: ${ response.json }")
  } yield ()
  logger.info(s"result = $result")
}


/*
{
 "http://schema.org/license": "http://creativecommons.org/licenses/by/4.0"
}
 */