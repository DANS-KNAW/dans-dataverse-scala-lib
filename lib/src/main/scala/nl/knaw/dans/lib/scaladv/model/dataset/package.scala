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
package nl.knaw.dans.lib.scaladv.model

import nl.knaw.dans.lib.scaladv.model.dataset.CompoundField.CompoundFieldValue
import org.json4s.{ CustomSerializer, DefaultFormats, Extraction, Formats, JArray, JNull, JObject, JString, JValue }

package object dataset {
  type MetadataBlocks = Map[String, MetadataBlock]

  object UpdateType extends Enumeration {
    type UpdateType = Value
    val major, minor = Value
  }

  val TYPE_CLASS_PRIMITIVE = "primitive"
  val TYPE_CLASS_CONTROLLED_VOCABULARY = "controlledVocabulary"
  val TYPE_CLASS_COMPOUND = "compound"

  val EXPORT_FORMAT_DDI = "ddi"
  val EXPORT_FORMAT_OAI_DDI = "oai_ddi"
  val EXPORT_FORMAT_DCTERMS = "dcterms"
  val EXPORT_FORMAT_OAI_DC = "oai_dc"
  val EXPORT_FORMAT_SCHEMA_ORG = "schema.org"
  val EXPORT_FORMAT_OAI_ORE = "OAI_ORE"
  val EXPORT_FORMAT_DATACITE = "Datacite"
  val EXPORT_FORMAT_OAI_DATACITE = "oai_datacite"
  val EXPORT_FORMAT_DATAVERSE_JSON = "dataverse_json"

  /**
   * Utility function that converts a list of metadata fields
   *
   * @param subFields
   * @return
   */
  def toFieldMap(subFields: MetadataField*): CompoundFieldValue = {
    subFields.map(f => (f.typeName, f)).toMap
  }

  implicit val jsonFormats: Formats = DefaultFormats + MetadataFieldSerializer

  private def isExternalCvocValue(v: JValue): Boolean = v match {
    case JArray(list) => list.headOption.exists(v => !v.isInstanceOf[JString])
    case JString(_) => false
    case _ => true
  }

  object MetadataFieldSerializer extends CustomSerializer[MetadataField](_ => ( {
    case jsonObj: JObject =>
      val multiple = (jsonObj \ "multiple").extract[Boolean]
      val typeClass = (jsonObj \ "typeClass").extract[String]
      val isExteralCvoc = isExternalCvocValue(jsonObj \ "value")

      typeClass match {
        case TYPE_CLASS_PRIMITIVE if multiple =>
          if (isExteralCvoc) Extraction.extract[PrimitiveMultipleValueFieldExtCvoc](jsonObj)
          else Extraction.extract[PrimitiveMultipleValueField](jsonObj)
        case TYPE_CLASS_PRIMITIVE =>
          if (isExteralCvoc) Extraction.extract[PrimitiveSingleValueFieldExtCvoc](jsonObj)
          else Extraction.extract[PrimitiveSingleValueField](jsonObj)
        case TYPE_CLASS_CONTROLLED_VOCABULARY if multiple => Extraction.extract[ControlledMultipleValueField](jsonObj)
        case TYPE_CLASS_CONTROLLED_VOCABULARY => Extraction.extract[ControlledSingleValueField](jsonObj)
        case TYPE_CLASS_COMPOUND => Extraction.extract[CompoundField](jsonObj)
          // TODO: Implement isExteralCvoc ?
      }
  }, {
    case null => JNull
  }
  ))
}
