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
package org.openrewrite.java.tree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.java.assertRefactored
import java.nio.file.Path
import java.nio.file.Paths

interface CompilationUnitTest {

    @Test
    fun newClass(jp: JavaParser) {
        val a = J.CompilationUnit.buildEmptyClass(Paths.get("sourceSet"), "my.org", "MyClass")

        assertRefactored(a, """
            package my.org;
            
            public class MyClass {
            }
        """)
    }

    @Test
    fun imports(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.List;
            import java.io.*;
            public class A {}
        """)

        assertEquals(2, a.imports.size)
    }

    @Test
    fun classes(jp: JavaParser) {
        val a = jp.parse("""
            public class A {}
            class B{}
        """)

        assertEquals(2, a.classes.size)
    }
    
    @Test
    fun format(jp: JavaParser) {
        val a = """
            /* Comment */
            package a;
            import java.util.List;
            
            public class A { }
        """
        
        assertEquals(a.trimIndent(), jp.parse(a).printTrimmed())
    }
}