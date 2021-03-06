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

import org.junit.jupiter.api.Test

interface ChangeMethodTargetToVariableTest {

    @Test
    fun refactorExplicitStaticToVariable(jp: JavaParser) {
        val a = """
            package a;
            public class A {
               public void foo() {}
            }
        """.trimIndent()

        val b = """
            package b;
            public class B {
               public static void foo() {}
            }
        """.trimIndent()

        val c = """
            import a.*;
            import b.B;
            public class C {
               A a;
               public void test() {
                   B.foo();
               }
            }
        """.trimIndent()

        val cu = jp.parse(c, a, b)
        val f = cu.classes[0].findFields("a.A")[0]

        val fixed = cu.refactor()
                .visit(ChangeMethodTargetToVariable().apply {
                    setMethod("b.B foo()")
                    setVariable(f.vars[0].simpleName)
                })
                .fix().fixed

        assertRefactored(fixed, """
            import a.A;
            public class C {
               A a;
               public void test() {
                   a.foo();
               }
            }
        """)
    }

    @Test
    fun refactorStaticImportToVariable(jp: JavaParser) {
        val a = """
            package a;
            public class A {
               public void foo() {}
            }
        """.trimIndent()

        val b = """
            package b;
            public class B {
               public static void foo() {}
            }
        """.trimIndent()

        val c = """
            import a.*;
            import static b.B.*;
            public class C {
               A a;
               public void test() {
                   foo();
               }
            }
        """.trimIndent()

        val cu = jp.parse(c, a, b)

        val f = cu.classes[0].findFields("a.A")[0]
        val fixed = cu.refactor()
                .visit(ChangeMethodTargetToVariable().apply {
                    setMethod("b.B foo()")
                    setVariable(f.vars[0].simpleName)
                })
                .fix().fixed

        assertRefactored(fixed, """
            import a.A;
            public class C {
               A a;
               public void test() {
                   a.foo();
               }
            }
        """)
    }
}
