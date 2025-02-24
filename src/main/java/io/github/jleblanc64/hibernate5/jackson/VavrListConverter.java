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
package io.github.jleblanc64.hibernate5.jackson;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.vavr.collection.List;

import java.util.Collection;

public class VavrListConverter {
    public static class FromCollec extends StdConverter<Collection<?>, List<?>> {
        @Override
        public List<?> convert(Collection value) {
            return List.ofAll(value);
        }
    }

    public static class ToCollec extends StdConverter<List<?>, Collection<?>> {
        @Override
        public Collection<?> convert(List value) {
            return value.toJavaList();
        }
    }
}
