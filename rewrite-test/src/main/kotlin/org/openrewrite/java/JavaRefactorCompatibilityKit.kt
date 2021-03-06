/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(JavaParserResolver::class)
abstract class JavaRefactorCompatibilityKit {
    abstract fun javaParser(): JavaParser

    @Nested
    inner class AddAnnotationTck : AddAnnotationTest

    @Nested
    inner class AddFieldTck : AddFieldTest

    @Nested
    inner class AddImportTck : AddImportTest

    @Nested
    inner class ChangeFieldNameTck : ChangeFieldNameTest

    @Nested
    inner class ChangeFieldTypeTck : ChangeFieldTypeTest

    @Nested
    inner class ChangeLiteralTck : ChangeLiteralTest

    @Nested
    inner class ChangeMethodNameTck : ChangeMethodNameTest

    @Nested
    inner class ChangeMethodTargetToStaticTck : ChangeMethodTargetToStaticTest

    @Nested
    inner class ChangeTypeTck : ChangeTypeTest

    @Nested
    inner class DeleteMethodArgumentTck : DeleteMethodArgumentTest

    @Nested
    inner class DeleteStatementTck : DeleteStatementTest

    @Nested
    inner class GenerateConstructorUsingFieldsTck : GenerateConstructorUsingFieldsTest

    @Nested
    inner class ImplementInterfaceTck : ImplementInterfaceTest

    @Nested
    inner class InsertMethodArgumentTck : InsertMethodArgumentTest

    @Nested
    inner class MethodMatcherTck : MethodMatcherTest

    @Nested
    inner class OrderImportTck : OrderImportTest

    @Nested
    inner class RemoveImportTck : RemoveImportTest

    @Nested
    inner class RenameVariableTck : RenameVariableTest

    @Nested
    inner class ReorderMethodArgumentsTck : ReorderMethodArgumentsTest

    @Nested
    inner class UnwrapParenthesesTck : UnwrapParenthesesTest
}
