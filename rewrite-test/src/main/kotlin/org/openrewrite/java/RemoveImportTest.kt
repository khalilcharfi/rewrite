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

interface RemoveImportTest {

    @Test
    fun removeNamedImport(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.List;
            class A {}
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.List") }).fix().fixed

        assertRefactored(fixed, "class A {}")
    }

    @Test
    fun leaveImportIfRemovedTypeIsStillReferredTo(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.List;
            class A {
               List list;
            }
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.List") }).fix().fixed

        assertRefactored(fixed, """
            import java.util.List;
            class A {
               List list;
            }
        """)
    }

    @Test
    fun removeStarImportIfNoTypesReferredTo(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.*;
            class A {}
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.List") }).fix().fixed

        assertRefactored(fixed, "class A {}")
    }

    @Test
    fun replaceStarImportWithNamedImportIfOnlyOneReferencedTypeRemains(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.*;
            class A {
               Collection c;
            }
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.List") }).fix().fixed

        assertRefactored(fixed, """
            import java.util.Collection;
            class A {
               Collection c;
            }
        """)
    }

    @Test
    fun leaveStarImportInPlaceIfMoreThanTwoTypesStillReferredTo(jp: JavaParser) {
        val a = jp.parse("""
            import java.util.*;
            class A {
               Collection c;
               Set s;
            }
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.List") }).fix().fixed

        assertRefactored(fixed, """
            import java.util.*;
            class A {
               Collection c;
               Set s;
            }
        """)
    }

    @Test
    fun removeStarStaticImport(jp: JavaParser) {
        val a = jp.parse("""
            import static java.util.Collections.*;
            class A {}
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.Collections") }).fix().fixed

        assertRefactored(fixed, "class A {}")
    }

    @Test
    fun leaveStarStaticImportIfReferenceStillExists(jp: JavaParser) {
        val a = jp.parse("""
            import static java.util.Collections.*;
            class A {
               Object o = emptyList();
            }
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.Collections") }).fix().fixed

        assertRefactored(fixed, """
            import static java.util.Collections.*;
            class A {
               Object o = emptyList();
            }
        """)
    }

    @Test
    fun leaveNamedStaticImportIfReferenceStillExists(jp: JavaParser) {
        val a = jp.parse("""
            import static java.util.Collections.emptyList;
            import static java.util.Collections.emptySet;
            class A {
               Object o = emptyList();
            }
        """.trimIndent())

        val fixed = a.refactor().visit(RemoveImport().apply { setType("java.util.Collections") }).fix().fixed

        assertRefactored(fixed, """
            import static java.util.Collections.emptyList;
            class A {
               Object o = emptyList();
            }
        """)
    }

    @Test
    fun leaveNamedStaticImportOnFieldIfReferenceStillExists(jp: JavaParser) {
        val bSource = """
            package foo;
            public class B {
                public static final String STRING = "string";
                public static final String STRING2 = "string2";
            }
        """.trimIndent()

        val cSource = """
            package foo;
            public class C {
                public static final String ANOTHER = "string";
            }
        """.trimIndent()

        val a = jp.parse("""
            import static foo.B.STRING;
            import static foo.B.STRING2;
            import static foo.C.*;
            public class A {
                String a = STRING;
            }
        """.trimIndent(), bSource, cSource)

        val fixed = a.refactor()
                .visit(RemoveImport().apply { setType("foo.B") })
                .visit(RemoveImport().apply { setType("foo.C") })
                .fix().fixed

        assertRefactored(fixed, """
            import static foo.B.STRING;
            public class A {
                String a = STRING;
            }
        """)
    }

    @Test
    fun removeImportForChangedMethodArgument(jp: JavaParser) {
        val b = """
            package b;
            public interface B {
                void doSomething();
            }
        """.trimIndent()

        val c = """
            package c;
            public interface C {
                void doSomething();
            }
        """.trimIndent()

        val a = jp.parse("""
            import b.B;
            
            class A {
                void foo(B arg) {
                    arg.doSomething();
                }
            }
        """.trimIndent(), b, c)

        val fixed = a.refactor().visit(ChangeType().apply { setType("b.B"); setTargetType("c.C") })
                .fix().fixed

        assertRefactored(fixed, """
            import c.C;
            
            class A {
                void foo(C arg) {
                    arg.doSomething();
                }
            }
        """)
    }
}
