/*
 * Copyright 2021 VMware, Inc.
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
package io.micrometer.api.instrument;

/**
 * A provider of tags.
 *
 * @author Marcin Grzejszczak
 * @since 2.0.0
 */
public interface TagsProvider {

    /**
     * Default low cardinality tags.
     *
     * @return tags
     */
    default Tags getLowCardinalityTags() {
        return Tags.empty();
    }

    /**
     * Default high cardinality tags.
     *
     * @return tags
     */
    default Tags getHighCardinalityTags() {
        return Tags.empty();
    }

    /**
     * Default low cardinality tags.
     *
     * @return tags
     */
    default Tags getAllTags() {
        return Tags.concat(getLowCardinalityTags(), getHighCardinalityTags()).and(getAdditionalLowCardinalityTags()).and(getAdditionalHighCardinalityTags());
    }

    /**
     * Additional to the default low cardinality tags. Can be set at runtime.
     *
     * @return tags
     */
    default Tags getAdditionalLowCardinalityTags() {
        return Tags.empty();
    }

    /**
     * Additional to the default high cardinality tags. Can be set at runtime.
     *
     * @return tags
     */
    default Tags getAdditionalHighCardinalityTags() {
        return Tags.empty();
    }
}
