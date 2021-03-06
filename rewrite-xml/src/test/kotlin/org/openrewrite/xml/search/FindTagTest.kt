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
package org.openrewrite.xml.search

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.openrewrite.xml.XmlParser

class FindTagTest : XmlParser() {
    private val x = parse("""
            <?xml version="1.0" encoding="UTF-8"?>
            <dependencies>
                <dependency>
                    <artifactId scope="compile">org.openrewrite</artifactId>
                </dependency>
            </dependency>
        """.trimIndent())

    @Test
    fun matchAbsolute() {
        assertNotNull(FindTag("/dependencies/dependency").visit(x))
        assertNotNull(FindTag("/dependencies/*").visit(x))
        assertNull(FindTag("/dependency/dne").visit(x))
    }
}