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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openrewrite.SourceVisitor;
import org.openrewrite.Tree;
import org.openrewrite.Validated;
import org.openrewrite.ValidationException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

import static org.openrewrite.Validated.required;
import static org.openrewrite.Validated.test;

public class YamlResourceLoader implements ProfileConfigurationLoader, SourceVisitorLoader {
    private static final ObjectMapper propertyConverter = new ObjectMapper()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Map<String, ProfileConfiguration> profiles = new HashMap<>();
    private final Collection<SourceVisitor<?>> visitors = new ArrayList<>();

    public YamlResourceLoader(InputStream yamlInput) throws UncheckedIOException {
        try {
            Yaml yaml = new Yaml();
            for (Object resource : yaml.loadAll(yamlInput)) {
                if (resource instanceof Map) {
                    @SuppressWarnings("unchecked") Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    String type = resourceMap.getOrDefault("type", "invalid").toString();
                    switch(type) {
                        case "beta.openrewrite.org/v1/visitor":
                            mapVisitor(resourceMap);
                            break;
                        case "beta.openrewrite.org/v1/profile":
                            mapProfile(resourceMap);
                            break;
                    }
                }
            }
        } finally {
            try {
                yamlInput.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void mapVisitor(Map<String, Object> visitorMap) {
        Validated validation = required("name", visitorMap.get("name"))
                .and(required("visitors", visitorMap.get("visitors")))
                .and(test("visitors",
                        "must be a list",
                        visitorMap.get("visitors"),
                        visitors -> visitors instanceof List));

        if (validation.isInvalid()) {
            throw new ValidationException(validation);
        }

        List<SourceVisitor<? extends Tree>> subVisitors = new ArrayList<>();

        //noinspection unchecked
        for (Object subVisitorNameAndConfig : (List<Object>) visitorMap.get("visitors")) {
            try {
                if (subVisitorNameAndConfig instanceof String) {
                    //noinspection unchecked
                    subVisitors.add((SourceVisitor<Tree>) visitorClass((String) subVisitorNameAndConfig)
                            .getDeclaredConstructor().newInstance());
                } else if (subVisitorNameAndConfig instanceof Map) {
                    //noinspection unchecked
                    for (Map.Entry<String, Object> subVisitorEntry : ((Map<String, Object>) subVisitorNameAndConfig)
                            .entrySet()) {
                        @SuppressWarnings("unchecked") SourceVisitor<Tree> subVisitor =
                                (SourceVisitor<Tree>) visitorClass(subVisitorEntry.getKey())
                                        .getDeclaredConstructor().newInstance();

                        propertyConverter.updateValue(subVisitor, subVisitorEntry.getValue());

                        subVisitors.add(subVisitor);
                    }
                }
            } catch (Exception e) {
                throw new ValidationException(
                        Validated.invalid("visitor", subVisitorNameAndConfig, "must be constructable", e)
                );
            }
        }

        this.visitors.add(new CompositeRefactorVisitor(visitorMap.get("name").toString(), subVisitors));
    }

    private Class<?> visitorClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return Class.forName("org.openrewrite." + name);
        }
    }

    private void mapProfile(Map<String, Object> profileMap) {
        ProfileConfiguration profile = new ProfileConfiguration();
        try {
            propertyConverter.updateValue(profile, profileMap);
        } catch (JsonMappingException e) {
            throw new ValidationException(Validated.invalid("profile", profileMap,
                    "must be a valid profile configuration", e));
        }

        profiles.compute(profile.getName(), (name, existing) -> profile.merge(existing));
    }

    @Override
    public Collection<ProfileConfiguration> loadProfiles() {
        return profiles.values();
    }

    @Override
    public Collection<SourceVisitor<?>> loadVisitors() {
        return visitors;
    }
}
