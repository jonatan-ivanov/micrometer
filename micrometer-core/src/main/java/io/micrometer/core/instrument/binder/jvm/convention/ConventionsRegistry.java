/*
 * Copyright 2026 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.jvm.convention;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConventionsRegistry {

    private final Map<String, Object> instances = new HashMap<>();

    public void register(Class<?> clazz, Variant variant, Object instance) {
        if (!clazz.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot register instance, it's not instance of " + clazz.getName());
        }
        instances.put(keyFor(clazz, variant), instance);
    }

    public <T> @Nullable T get(Class<T> clazz, Variant variant) {
        Object instance = instances.get(keyFor(clazz, variant));
        return clazz.isInstance(instance) ? clazz.cast(instance) : null;
    }

    private String keyFor(Class<?> clazz, Variant variant) {
        return clazz.getName() + "#" + variant.getName();
    }

    public interface Variant {

        String getName();

    }

}
