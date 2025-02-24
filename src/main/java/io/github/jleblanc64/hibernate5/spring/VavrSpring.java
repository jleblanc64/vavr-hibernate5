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
package io.github.jleblanc64.libcustom.custom.spring;

import io.github.jleblanc64.libcustom.LibCustom;
import io.github.jleblanc64.libcustom.custom.hibernate.Utils;
import io.github.jleblanc64.libcustom.custom.hibernate.duplicate.ParameterizedTypeImpl;
import io.github.jleblanc64.libcustom.meta.MetaList;
import io.github.jleblanc64.libcustom.meta.MetaOption;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor;
import org.springframework.data.repository.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.jleblanc64.libcustom.custom.hibernate.Utils.getRefl;
import static io.github.jleblanc64.libcustom.functional.ListF.f;

public class VavrSpring {
    @SneakyThrows
    public static void override(MetaList metaList) {
        LibCustom.override(GenericConversionService.class, "convert", args -> {
            if (args == null || args.length != 3)
                return LibCustom.ORIGINAL;

            if (!(args[2] instanceof TypeDescriptor))
                return LibCustom.ORIGINAL;

            var targetType = (TypeDescriptor) args[2];
            if (!metaList.isSuperClassOf(targetType.getObjectType()))
                return LibCustom.ORIGINAL;

            var source = args[0];
            if (!(source instanceof Collection))
                return LibCustom.ORIGINAL;

            return metaList.fromJava(new ArrayList<>((Collection) source));
        });
    }

    @SneakyThrows
    public static void override(MetaOption metaOption) {
        var clazz = Class.forName("org.springframework.data.util.TypeDiscoverer");
        LibCustom.modifyArg(clazz, "createInfo", 0, args -> {
            var type = args[0].toString();
            if (type.startsWith(metaOption.monadClass().getName() + "<")) {
                var paramClass = Utils.paramClass(type);
                var isEntity = Utils.isEntity(paramClass.getDeclaredAnnotations());
                if (isEntity)
                    return ParameterizedTypeImpl.of(Optional.class, paramClass, null);
            }

            return LibCustom.ORIGINAL;
        });

        LibCustom.modifyReturn(MethodParameter.class, "getGenericParameterType", argsR -> {
            var returned = argsR.returned.toString();
            if (returned.startsWith(metaOption.monadClass().getName() + "<")) {
                var paramClass = Utils.paramClass(returned);
                var isEntity = Utils.isEntity(paramClass.getDeclaredAnnotations());
                if (isEntity)
                    return ParameterizedTypeImpl.of(Optional.class, paramClass, null);
            }

            return LibCustom.ORIGINAL;
        });

        LibCustom.modifyReturn(DefaultMethodInvokingMethodInterceptor.class, "invoke", argsR -> {
            var returned = argsR.returned;
            var invocation = argsR.args[0];

            if (invocation.toString().contains(metaOption.monadClass().getName())) {
                if (metaOption.isSuperClassOf(returned))
                    return returned;

                var o = (Optional<?>) returned;
                var v = o.isEmpty() ? null : o.get();
                return metaOption.fromValue(v);
            }

            return returned;
        });

        clazz = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        LibCustom.modifyArgWithSelf(clazz, "invoke", 2, argsS -> {
            var args = argsS.args;
            var self = argsS.self;

            var proxiedInterfaces = f((Class[]) getRefl(self, "proxiedInterfaces"));
            if (!proxiedInterfaces.contains(Repository.class))
                return args[2];

            if (args[2] == null)
                return args[2];

            var argsL = f((Object[]) args[2]);
            return argsL.map(arg -> {
                if (!metaOption.isSuperClassOf(arg) || arg instanceof List)
                    return arg;

                return metaOption.getOrNull(arg);
            }).toArray();
        });

        // request params
        var methodParameterClass = Class.forName("org.springframework.core.MethodParameter");
        var argResolverClass = Class.forName("org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver");

        LibCustom.modifyReturn(argResolverClass, "resolveArgument", argsRet -> {
            var args = argsRet.args;
            var returned = argsRet.returned;
            var type = methodParameterClass.getMethod("getParameterType").invoke(args[0]);

            if (metaOption.isSuperClassOf(type) && !metaOption.isSuperClassOf(returned))
                return metaOption.fromValue(returned);

            return returned;
        });

        LibCustom.modifyArg(Class.forName("org.springframework.beans.TypeConverterDelegate"), "doConvertValue", 1, args -> {
            var newValue = args[1];
            var requiredType = (Class<?>) args[2];

            if (metaOption.isSuperClassOf(requiredType))
                return metaOption.fromValue(newValue);

            return LibCustom.ORIGINAL;
        });
    }
}
