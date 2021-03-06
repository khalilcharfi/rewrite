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
package org.openrewrite;

public interface Profile {
    Profile ALL = () -> "__all__";

    String getName();

    default <S, T extends SourceVisitor<S>> FilterReply accept(T visitor) {
        return visitor.validate().isValid() ? FilterReply.ACCEPT : FilterReply.DENY;
    }

    default <S, T extends SourceVisitor<S>> T configure(T visitor) {
        return visitor;
    }

    enum FilterReply {
        ACCEPT, DENY, NEUTRAL;
    }
}
