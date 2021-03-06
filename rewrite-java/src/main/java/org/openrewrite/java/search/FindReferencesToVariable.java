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
package org.openrewrite.java.search;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaSourceVisitor;
import org.openrewrite.java.tree.J;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class FindReferencesToVariable extends JavaSourceVisitor<List<Tree>> {
    private final J.Ident variable;

    public FindReferencesToVariable(J.Ident variable) {
        this.variable = variable;
    }

    @Override
    public Iterable<Tag> getTags() {
        return Tags.of("variable", variable.getSimpleName());
    }

    @Override
    public List<Tree> defaultTo(Tree t) {
        return emptyList();
    }

    @Override
    public List<Tree> visitAssign(J.Assign assign) {
        return hasReference(assign.getVariable()) ? singletonList(assign) : super.visitAssign(assign);
    }

    @Override
    public List<Tree> visitAssignOp(J.AssignOp assignOp) {
        return hasReference(assignOp.getVariable()) ? singletonList(assignOp) : super.visitAssignOp(assignOp);
    }

    @Override
    public List<Tree> visitUnary(J.Unary unary) {
        return hasReference(unary.getExpr()) ? singletonList(unary) : super.visitUnary(unary);
    }

    private boolean hasReference(Tree t) {
        return new HasReferenceToVariableInSubtree().visit(t);
    }

    private class HasReferenceToVariableInSubtree extends JavaSourceVisitor<Boolean> {
        @Override
        public Boolean defaultTo(Tree t) {
            return false;
        }

        @Override
        public Boolean visitIdentifier(J.Ident ident) {
            return ident.getIdent() == variable.getIdent() || super.visitIdentifier(ident);
        }
    }
}
