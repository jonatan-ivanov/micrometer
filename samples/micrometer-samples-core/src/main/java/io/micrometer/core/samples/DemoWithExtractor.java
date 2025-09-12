/*
 * Copyright 2025 VMware, Inc.
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
package io.micrometer.core.samples;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValueProvider;
import io.micrometer.common.docs.KeyName;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public class DemoWithExtractor {

    private static final KeyValueProvider<MongoContext> COMMAND = MongoKeys.COMMAND.withExtractor(MongoContext::getCommand);
//    private static final KeyValueProvider<MongoContext> COMMAND = MongoKeys.COMMAND.withExtractor(MongoContext::getCommand, "unknown");

    public static void main(String[] args) {
        Arrays.asList(new MongoContext("demo"), new MongoContext(null), null).forEach(context -> {
            KeyValue keyValue = COMMAND.withValue(context);
            System.out.println(keyValue);
        });
    }

    enum MongoKeys implements KeyName {

        COMMAND {
            @Override
            public String asString() {
                return "command";
            }
        }

    }

    static class MongoContext {

        private final @Nullable String command;

        MongoContext(@Nullable String command) {
            this.command = command;
        }

        public @Nullable String getCommand() {
            return command;
        }

    }

}
