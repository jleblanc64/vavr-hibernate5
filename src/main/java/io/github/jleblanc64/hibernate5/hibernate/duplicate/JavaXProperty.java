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
package io.github.jleblanc64.hibernate5.hibernate.duplicate;


import io.github.jleblanc64.hibernate5.hibernate.Utils;
import io.github.jleblanc64.hibernate5.meta.MetaList;
import io.github.jleblanc64.libcustom.functional.ListF;
import lombok.SneakyThrows;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.annotations.common.reflection.java.JavaXMember;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

import static io.github.jleblanc64.hibernate5.hibernate.Utils.getRefl;
import static io.github.jleblanc64.hibernate5.hibernate.Utils.isEntity;
import static io.github.jleblanc64.libcustom.functional.ListF.f;

// https://github.com/hibernate/hibernate-commons-annotations/blob/5.1/src/main/java/org/hibernate/annotations/common/reflection/java/JavaXProperty.java
public class JavaXProperty extends JavaXMember implements XProperty {
    public final TypeEnvironment env;
    public final JavaReflectionManager factory;
    public final ListF<Annotation> annotations;
    private final JavaXMember javaXProperty;
    private boolean isCollection;
    private Class collectionClass;
    private XClass elementClass;

    @SneakyThrows
    public static JavaXProperty of(JavaXMember m, Type type, MetaList metaList) {
        return of(m, type, f(m.getAnnotations()), metaList);
    }

    @SneakyThrows
    public static JavaXProperty of(Field f, Type type, JavaXProperty j, MetaList metaList) {
        return new JavaXProperty(f, type, j.env, j.factory, j.annotations, metaList);
    }

    @SneakyThrows
    private static JavaXProperty of(JavaXMember m, Type type, List<Annotation> annotations, MetaList metaList) {
        var env = (TypeEnvironment) getRefl(m, "env");
        return new JavaXProperty(m.getMember(), type, env, new JavaReflectionManager(), f(annotations), metaList);
    }

    @SneakyThrows
    private JavaXProperty(Member member, Type type, TypeEnvironment env, JavaReflectionManager factory, ListF<Annotation> annotations,
                          MetaList metaList) {
        super(member, type, env, factory, factory.toXType(env, typeOf(member, env)));

        this.env = env;
        this.factory = factory;
        this.annotations = annotations;

        var clazz = Class.forName("org.hibernate.annotations.common.reflection.java.JavaXProperty");
        var clazzJavaXType = Class.forName("org.hibernate.annotations.common.reflection.java.JavaXType");

        var constructor = clazz.getDeclaredConstructor(Member.class, Type.class, TypeEnvironment.class,
                JavaReflectionManager.class, clazzJavaXType);
        constructor.setAccessible(true);
        javaXProperty = (JavaXMember) constructor.newInstance(member, type, env, factory, factory.toXType(env, typeOf(member, env)));

        // elementClass
        var typeS = getRefl(this, "type").toString();
        if (metaList != null && typeS.startsWith(metaList.monadClass().getName() + "<")) {
            var paramClass = Utils.paramClass(typeS);
            var clazzJavaXClass = Class.forName("org.hibernate.annotations.common.reflection.java.JavaXClass");
            constructor = clazzJavaXClass.getDeclaredConstructor(Class.class, TypeEnvironment.class, JavaReflectionManager.class);
            constructor.setAccessible(true);

            isCollection = isEntity(paramClass.getDeclaredAnnotations());
            collectionClass = metaList.monadClass();
            elementClass = (XClass) constructor.newInstance(paramClass, env, factory);
        }
    }

    @Override
    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public Class getCollectionClass() {
        return collectionClass;
    }

    @Override
    public XClass getElementClass() {
        return elementClass;
    }

    @Override
    public String getName() {
        return javaXProperty.getName();
    }

    @Override
    public Object invoke(Object target) {
        return javaXProperty.invoke(target);
    }

    @Override
    public Object invoke(Object target, Object... parameters) {
        return javaXProperty.invoke(target, parameters);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return (T) annotations.findSafe(a -> annotationType.isAssignableFrom(a.getClass()));
    }

    @Override
    public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType) {
        return annotations.stream().anyMatch(a -> annotationType.isAssignableFrom(a.getClass()));
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.toArray(new Annotation[0]);
    }
}
