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

import io.github.jleblanc64.hibernate5.meta.MetaColl;
import io.github.jleblanc64.hibernate5.meta.MetaOption;
import io.github.jleblanc64.libcustom.LibCustom;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.Collection;

import static io.github.jleblanc64.libcustom.FieldMocked.*;

public class VavrJackson {
    public static void overrideCustom(MetaColl... metaList) {
        LibCustom.modifyReturn(AbstractJackson2HttpMessageConverter.class, "readJavaType", argsR -> {
            var returned = argsR.returned;
            if (returned == null)
                return returned;

            var collection = toCollection(returned, metaList);
            if (collection != null)
                collection.forEach(x -> fillEmpty(x, metaList));
            else
                fillEmpty(returned, metaList);

            return returned;
        });
    }

    private static Collection toCollection(Object returned, MetaColl... metaList) {
        if (returned instanceof Collection)
            return (Collection) returned;

        var matched = matchMeta(returned.getClass(), metaList);
        if (matched != null)
            return matched.toJava(returned);

        return null;
    }

    private static void fillEmpty(Object returned, MetaColl... metaList) {
        fields(returned).forEach(f -> {
            var type = f.getType();
            Object empty;
            var matched = matchMeta(type, metaList);
            if (matched != null)
                empty = matched.fromJava(new ArrayList());
            else
                return;

            var o = getRefl(returned, f);
            if (o == null)
                setRefl(returned, f, empty);
        });
    }

    private static MetaColl matchMeta(Class<?> type, MetaColl... metas) {
        for (var meta : metas)
            if (meta.isSuperClassOf(type))
                return meta;

        return null;
    }

    public static void overrideCustom(MetaOption metaOption, MetaColl... metaList) {
        LibCustom.modifyReturn(AbstractJackson2HttpMessageConverter.class, "readJavaType", argsR -> {
            var returned = argsR.returned;
            if (returned == null)
                return returned;

            var collection = toCollection(returned, metaList);
            if (collection != null)
                collection.forEach(x -> fillEmpty(x, metaOption));
            else
                fillEmpty(returned, metaOption);

            return returned;
        });
    }

    private static void fillEmpty(Object returned, MetaOption metaOption) {
        fields(returned).forEach(f -> {
            var type = f.getType();
            Object empty;
            if (metaOption.isSuperClassOf(type))
                empty = metaOption.fromValue(null);
            else
                return;

            var o = getRefl(returned, f);
            if (o == null)
                setRefl(returned, f, empty);
        });
    }
}
