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
import io.vavr.control.Option;

public class VavrOptionConverter {
    public static class FromOption<T> extends StdConverter<Option<T>, T> {
        @Override
        public T convert(Option<T> value) {
            return value.getOrNull();
        }
    }

    public static class ToOption<T> extends StdConverter<T, Option<T>> {
        @Override
        public Option<T> convert(T value) {
            return Option.of(value);
        }
    }
}
