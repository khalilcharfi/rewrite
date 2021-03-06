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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser
import org.openrewrite.java.firstMethodStatement

interface ContinueTest {
    
    @Test
    fun continueFromWhileLoop(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                public void test() {
                    while(true) continue;
                }
            }
        """)
        
        val whileLoop = a.firstMethodStatement() as J.WhileLoop
        assertTrue(whileLoop.body is J.Continue)
        assertNull((whileLoop.body as J.Continue).label)
    }

    @Test
    fun continueFromLabeledWhileLoop(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                public void test() {
                    labeled: while(true)
                        continue labeled;
                }
            }
        """)

        val whileLoop = (a.firstMethodStatement() as J.Label).statement as J.WhileLoop
        assertTrue(whileLoop.body is J.Continue)
        assertEquals("labeled", (whileLoop.body as J.Continue).label?.simpleName)
    }

    @Test
    fun formatContinueLabeled(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                public void test() {
                    labeled : while(true)
                        continue labeled;
                }
            }
        """)

        val whileLoop = (a.firstMethodStatement() as J.Label).statement as J.WhileLoop
        assertEquals("continue labeled", whileLoop.body.printTrimmed())
    }
}