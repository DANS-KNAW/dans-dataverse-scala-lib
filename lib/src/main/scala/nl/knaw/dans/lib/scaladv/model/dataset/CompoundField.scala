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
package nl.knaw.dans.lib.scaladv.model.dataset

import nl.knaw.dans.lib.scaladv.model.dataset.CompoundField.CompoundFieldValue

case class CompoundField(override val typeClass: String, override val typeName: String, override val multiple: Boolean, value: List[CompoundFieldValue]) extends MetadataField(typeClass, typeName, multiple)

object CompoundField {
  type CompoundFieldValue = Map[String, MetadataField]

  def apply(typeName: String, value: CompoundFieldValue): CompoundField = {
    CompoundField(TYPE_CLASS_COMPOUND, typeName, multiple = false, List(value))
  }

  def apply(typeName: String, value: List[CompoundFieldValue]): CompoundField = {
    CompoundField(TYPE_CLASS_COMPOUND, typeName, multiple = true, value)
  }
}