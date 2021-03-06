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
package org.openrewrite.config;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.openrewrite.AutoConfigure;
import org.openrewrite.SourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class AutoConfigureSourceVisitorLoader implements SourceVisitorLoader {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigureSourceVisitorLoader.class);

    private final String[] whitelistVisitorPackages;

    public AutoConfigureSourceVisitorLoader(String... whitelistVisitorPackages) {
        this.whitelistVisitorPackages = whitelistVisitorPackages;
    }

    public Collection<SourceVisitor<?>> loadVisitors() {
        try(ScanResult scanResult = new ClassGraph()
                .whitelistPackages(whitelistVisitorPackages)
                .enableMemoryMapping()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .ignoreClassVisibility()
                .ignoreMethodVisibility()
                .scan()) {
            return scanResult.getClassesWithAnnotation(AutoConfigure.class.getName()).stream()
                    .map(classInfo -> {
                        Class<?> visitorClass = classInfo.loadClass();
                        try {
                            Constructor<?> constructor = visitorClass.getConstructor();
                            constructor.setAccessible(true);
                            return (SourceVisitor<?>) constructor.newInstance();
                        } catch (Exception e) {
                            logger.warn("Unable to configure {}", visitorClass.getName(), e);
                        }
                        return null;
                    })
                    .collect(toList());
        }
    }
}
