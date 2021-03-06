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
package org.openrewrite.properties.internal;

import org.openrewrite.Tree;
import org.openrewrite.properties.PropertiesSourceVisitor;
import org.openrewrite.properties.tree.Properties;

public class PrintProperties extends PropertiesSourceVisitor<String> {
    @Override
    public String defaultTo(Tree t) {
        return "";
    }

    @Override
    public String visitFile(Properties.File file) {
        return file.getFormatting().getPrefix() + visit(file.getContent()) + file.getFormatting().getSuffix();
    }

    @Override
    public String visitEntry(Properties.Entry entry) {
        return entry.getFormatting().getPrefix() + entry.getKey() +
                entry.getEqualsFormatting().getPrefix() + "=" + entry.getEqualsFormatting().getSuffix() +
                entry.getValue() +
                entry.getFormatting().getSuffix();
    }
}
