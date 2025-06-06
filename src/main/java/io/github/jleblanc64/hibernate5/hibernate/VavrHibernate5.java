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
package io.github.jleblanc64.hibernate5.hibernate;

import io.github.jleblanc64.hibernate5.hibernate.duplicate.FieldCustomType;
import io.github.jleblanc64.hibernate5.hibernate.duplicate.JavaXProperty;
import io.github.jleblanc64.hibernate5.hibernate.duplicate.MyCollectionType;
import io.github.jleblanc64.hibernate5.hibernate.duplicate.TypeImpl;
import io.github.jleblanc64.hibernate5.impl.MetaListImpl;
import io.github.jleblanc64.hibernate5.impl.MetaOptionImpl;
import io.github.jleblanc64.hibernate5.impl.MetaSetImpl;
import io.github.jleblanc64.hibernate5.jackson.VavrJackson;
import io.github.jleblanc64.hibernate5.meta.*;
import io.github.jleblanc64.hibernate5.spring.OverrideContentType;
import io.github.jleblanc64.hibernate5.spring.VavrSpring;
import io.github.jleblanc64.libcustom.LibCustom;
import lombok.SneakyThrows;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.java.JavaXMember;
import org.hibernate.cfg.AccessType;
import org.hibernate.cfg.PropertyInferredData;
import org.hibernate.cfg.annotations.BagBinder;
import org.hibernate.cfg.annotations.CollectionBinder;
import org.hibernate.cfg.annotations.SetBinder;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.internal.PluralAttributeBuilder;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.type.BagType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.SetType;

import javax.persistence.metamodel.PluralAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jleblanc64.hibernate5.hibernate.Utils.*;

public class VavrHibernate5 {
    public static void override() {
        var metaList = new MetaListImpl();
        var metaOption = new MetaOptionImpl();
        var metaSet = new MetaSetImpl();

        overrideCustom(metaList, metaOption, metaSet);
    }

    public static void overrideCustom(MetaList metaList, MetaOption metaOption, MetaSet metaSet) {
        overrideCustom(metaList);
        if (metaSet != null)
            overrideCustom(metaSet);

        VavrSpring.overrideCustom(metaList);

        var metas = newArrayList(metaList, metaOption);
        if (metaSet != null)
            metas.add(metaSet);

        VavrJackson.overrideCustom(metas.toArray(new WithClass[0]));

        overrideCustom(metaOption);
        VavrSpring.overrideCustom(metaOption);

        OverrideContentType.override();

        LibCustom.load();
    }

    @SneakyThrows
    private static void overrideCustom(MetaColl meta) {
        var bag = meta.bag();

        LibCustom.modifyArg(org.hibernate.cfg.AnnotationBinder.class, "processElementAnnotations", 2, args -> {
            var pid = (PropertyInferredData) args[2];
            var p = pid.getProperty();
            var type = (Type) getRefl(p, "type");
            var at = (AccessType) getRefl(pid, "defaultAccess");
            var rm = (ReflectionManager) getRefl(pid, "reflectionManager");
            var j = JavaXProperty.of((JavaXMember) p, type, meta);

            if (!(type instanceof ParameterizedType))
                return LibCustom.ORIGINAL;

            var rawType = ((ParameterizedType) type).getRawType();
            if (meta.isSuperClassOf(rawType)) {
                var f = (Field) j.getMember();
                var jOver = JavaXProperty.of(f, type, j, meta);
                return new PropertyInferredData(pid.getDeclaringClass(), jOver, at.getType(), rm);
            }

            return LibCustom.ORIGINAL;
        });

        LibCustom.override(org.hibernate.metamodel.internal.AttributeFactory.class, "determineCollectionType", args -> {
            var clazz = (Class) args[0];
            var type = meta.isSet() ? PluralAttribute.CollectionType.SET : PluralAttribute.CollectionType.LIST;
            if (meta.isSuperClassOf(clazz))
                return type;

            return LibCustom.ORIGINAL;
        });

        LibCustom.overrideWithSelf(org.hibernate.metamodel.model.domain.internal.PluralAttributeBuilder.class, "build", x -> {
            var self = x.self;

            var collectionClass = (Class) getRefl(self, "collectionClass");
            var className = meta.isSet() ? "org.hibernate.metamodel.model.domain.internal.SetAttributeImpl" :
                    "org.hibernate.metamodel.model.domain.internal.ListAttributeImpl";

            var listAttrClass = Class.forName(className);
            var constructor = listAttrClass.getDeclaredConstructor(PluralAttributeBuilder.class);
            constructor.setAccessible(true);

            if (meta.isSuperClassOf(collectionClass))
                return constructor.newInstance(self);

            return LibCustom.ORIGINAL;
        });

        LibCustom.modifyArg(Class.forName("org.hibernate.type.CollectionType"), "getElementsIterator", 0, args -> {
            var collection = args[0];
            if (meta.isSuperClassOf(collection))
                return meta.toJava(collection);

            return collection;
        });

        LibCustom.override(CollectionBinder.class, "getBinderFromBasicCollectionType", args -> {
            var binder = meta.isSet() ? new SetBinder(false) : new BagBinder();
            return meta.isSuperClassOf(args[0]) ? binder : LibCustom.ORIGINAL;
        });

        var typeClass = meta.isSet() ? SetType.class : BagType.class;
        LibCustom.override(typeClass, "instantiate", args -> {
            if (args.length == 1)
                return LibCustom.ORIGINAL;

            var pers = (AbstractCollectionPersister) args[1];
            if (isOfType(pers, meta))
                return checkPersistentBag(bag.of((SharedSessionContractImplementor) args[0], null));

            return LibCustom.ORIGINAL;
        });

        LibCustom.override(typeClass, "wrap", args -> {
            var arg1 = args[1];

            if (meta.isSuperClassOf(arg1)) {
                var c = meta.toJava(arg1);
                return checkPersistentBag(bag.of((SharedSessionContractImplementor) args[0], c));
            }

            return LibCustom.ORIGINAL;
        });

        LibCustom.overrideWithSelf(CollectionType.class, "replaceElements", x -> {
            var args = x.args;
            var c = (CollectionType) x.self;

            return MyCollectionType.replaceElements(args[0], args[1], args[2], (Map) args[3], (SharedSessionContractImplementor) args[4], c);
        });
    }

    @SneakyThrows
    private static void overrideCustom(MetaOption<?> metaOption) {
        var setterFieldImplClass = Class.forName("org.hibernate.property.access.spi.SetterFieldImpl");
        var getterFieldImplClass = Class.forName("org.hibernate.property.access.spi.GetterFieldImpl");

        LibCustom.modifyArgWithSelf(setterFieldImplClass, "set", 1, argsSelf -> {
            var args = argsSelf.args;
            var value = args[1];
            var self = argsSelf.self;
            var field = (Field) getRefl(self, setterFieldImplClass.getDeclaredField("field"));

            if (metaOption.isSuperClassOf(field.getType()) && !metaOption.isSuperClassOf(value))
                return metaOption.fromValue(value);

            return LibCustom.ORIGINAL;
        });

        LibCustom.modifyReturn(getterFieldImplClass, "get", x -> {
            var ret = x.returned;
            if (metaOption.isSuperClassOf(ret))
                return metaOption.getOrNull(ret);

            return ret;
        });

        LibCustom.modifyArg(Class.forName("org.hibernate.annotations.common.reflection.java.JavaXProperty"), "create", 0, args -> {
            var member = args[0];
            if (member instanceof Field) {
                var field = (Field) member;
                if (!(field.getGenericType() instanceof ParameterizedType))
                    return LibCustom.ORIGINAL;

                var type = (ParameterizedType) field.getGenericType();
                var typeRaw = type.getRawType();
                var typeParam = type.getActualTypeArguments()[0];
                var ownerType = ((ParameterizedType) field.getGenericType()).getOwnerType();

                if (metaOption.isSuperClassOf(typeRaw))
                    return FieldCustomType.create(field, new TypeImpl((Class<?>) typeParam, new Type[]{}, ownerType));
            }

            return LibCustom.ORIGINAL;
        });
    }
}
