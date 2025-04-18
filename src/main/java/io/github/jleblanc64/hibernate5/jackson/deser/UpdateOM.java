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
package io.github.jleblanc64.hibernate5.jackson.deser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.jleblanc64.hibernate5.hibernate.Utils;
import io.github.jleblanc64.hibernate5.impl.MetaListImpl;
import io.github.jleblanc64.hibernate5.impl.MetaOptionImpl;
import io.github.jleblanc64.hibernate5.impl.MetaSetImpl;
import io.github.jleblanc64.hibernate5.meta.MetaList;
import io.github.jleblanc64.hibernate5.meta.MetaOption;
import io.github.jleblanc64.hibernate5.meta.MetaSet;
import lombok.SneakyThrows;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

public class UpdateOM {

    public static void update(ObjectMapper om, List<HttpMessageConverter<?>> converters) {
        var metaOption = new MetaOptionImpl();
        var metaList = new MetaListImpl();
        var metaSet = new MetaSetImpl();
        updateCustom(om, converters, metaOption, metaList, metaSet);
    }

    @SneakyThrows
    public static void updateCustom(ObjectMapper om, List<HttpMessageConverter<?>> converters, MetaOption metaOption, MetaList metaList, MetaSet metaSet) {
        om.registerModule(new OptionModule(metaOption));

        var simpleModule = new SimpleModule()
                .addDeserializer(metaList.monadClass(), new CollectionDeser.Deserializer(metaList))
                .addSerializer(metaList.monadClass(), new CollectionDeser.Serializer(metaList));
        om.registerModule(simpleModule);

        if (metaSet != null) {
            simpleModule = new SimpleModule()
                    .addDeserializer(metaSet.monadClass(), new CollectionDeser.Deserializer(metaSet))
                    .addSerializer(metaSet.monadClass(), new CollectionDeser.Serializer(metaSet));
            om.registerModule(simpleModule);
        }

        var msgConverterClass = Class.forName("org.springframework.http.converter.json.MappingJackson2HttpMessageConverter");
        io.vavr.collection.List.ofAll(converters).filter(c -> msgConverterClass.isAssignableFrom(c.getClass()))
                .forEach(c -> setObjectMapper(c, om));
    }

    @SneakyThrows
    static void setObjectMapper(Object msgConverter, ObjectMapper om) {
        Utils.invoke(msgConverter, "setObjectMapper", om);
    }
}
