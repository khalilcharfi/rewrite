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
import org.openrewrite.java.fields

interface LiteralTest {

    @Test
    fun literalField(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                int n = 0;
            }
        """)

        val literal = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals(0, literal.value)
        assertEquals(JavaType.Primitive.Int, literal.type)
        assertEquals("0", literal.printTrimmed())
    }

    @Test
    fun literalCharacter(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                char c = 'a';
            }
        """)

        val literal = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals('a', literal.value)
        assertEquals(JavaType.Primitive.Char, literal.type)
        assertEquals("'a'", literal.printTrimmed())
    }

    @Test
    fun literalNumerics(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                double d1 = 1.0d;
                double d2 = 1.0;
                long l1 = 1L;
                long l2 = 1;
            }
        """)

        val (d1, d2, l1, l2) = a.fields(0..3).map { it.vars[0].initializer as J.Literal }
        assertEquals("1.0d", d1.printTrimmed())
        assertEquals("1.0", d2.printTrimmed())
        assertEquals("1L", l1.printTrimmed())
        assertEquals("1", l2.printTrimmed())
    }

    @Test
    fun literalOctal(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                long l = 01L;
                byte b = 01;
                short s = 01;
                int i = 01;
                double d = 01;
                float f = 01;
            }
        """)

        a.fields(0..5).map { it.vars[0].initializer as J.Literal }.forEach {
            assertEquals("01", it.printTrimmed().trimEnd('L'), "expected octal notation for ${it.type}")
        }
    }

    @Test
    fun literalBinary(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                long l = 0b10L;
                byte b = 0b10;
                short s = 0b10;
                int i = 0b10;
            }
        """)

        a.fields(0..3).map { it.vars[0].initializer as J.Literal }.forEach {
            assertEquals("0b10", it.printTrimmed().trimEnd('L'), "expected binary notation for ${it.type}")
        }
    }

    @Test
    fun literalHex(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                long l = 0xA0L;
                byte b = 0xA0;
                short s = 0xA0;
                int i = 0xA0;
            }
        """)

        a.fields(0..3).map { it.vars[0].initializer as J.Literal }.forEach {
            assertEquals("0xA0", it.printTrimmed().trimEnd('L'), "expected hex notation for ${it.type}")
        }
    }

    @Test
    fun transformString(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                String s = "foo ''";
            }
        """)

        val literal = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals("\"foo\"", literal.transformValue<String> { it.substringBefore(' ') })
    }

    @Test
    fun nullLiteral(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                String s = null;
            }
        """)

        assertEquals("null", a.classes[0].fields[0].vars[0].initializer?.printTrimmed())
    }

    @Test
    fun transformLong(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                Long l = 2L;
            }
        """)

        val literal = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals("4L", literal.transformValue<Long> { it * 2 })
    }

    @Test
    fun variationInSuffixCasing(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                Long l = 0l;
                Long m = 0L;
            }
        """)

        val (lower, upper) = a.fields(0..1).map { it.vars[0].initializer as J.Literal }

        assertEquals("0L", upper.printTrimmed())
        assertEquals("0l", lower.printTrimmed())
    }

    @Test
    fun escapedString(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                String s = "\"";
            }
        """)

        val s = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals("\"\\\"\"", s.printTrimmed())
    }

    @Test
    fun escapedCharacter(jp: JavaParser) {
        val a = jp.parse("""
            public class A {
                char c = '\'';
            }
        """)

        val s = a.classes[0].fields[0].vars[0].initializer as J.Literal
        assertEquals("'\\''", s.printTrimmed())
    }
}