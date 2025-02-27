/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.registry.maven;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Maven artifact license references
 */
public class MavenArtifactDependency extends MavenArtifactReference {

    public enum Scope {
        COMPILE,
        PROVIDED,
        RUNTIME,
        TEST,
        SYSTEM,
        IMPORT
    }

    private Scope scope;
    private boolean optional;
    private List<MavenArtifactReference> exclusions;
    private boolean broken;

    public MavenArtifactDependency(@NotNull String groupId, @NotNull String artifactId, @Nullable String classifier, @NotNull String version, Scope scope, boolean optional) {
        super(groupId, artifactId, classifier, version);
        this.scope = scope;
        this.optional = optional;
    }

    public Scope getScope() {
        return scope;
    }

    public boolean isOptional() {
        return optional;
    }

    public List<MavenArtifactReference> getExclusions() {
        return exclusions;
    }

    void addExclusion(MavenArtifactReference ref) {
        if (exclusions == null) {
            exclusions = new ArrayList<>();
        }
        exclusions.add(ref);
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
