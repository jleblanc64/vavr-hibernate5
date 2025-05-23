/*
 * Copyright 2024 - Charles Dabadie
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
package io.github.jleblanc64.hibernate5.impl;

import io.github.jleblanc64.hibernate5.meta.BagProvider;
import io.github.jleblanc64.hibernate5.meta.MetaSet;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.Collection;

public class MetaSetImpl implements MetaSet<Set> {
    @Override
    public Class<Set> monadClass() {
        return Set.class;
    }

    @Override
    public Set fromJava(Collection l) {
        return HashSet.ofAll(l);
    }

    @Override
    public java.util.Set toJava(Set l) {
        return l.toJavaSet();
    }

    @Override
    public BagProvider<? extends Set> bag() {
        return (BagProvider<PersistentSetImpl>) PersistentSetImpl::new;
    }
}
