/**
 * Copyright (c) 2021-2023 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageLayoutBuilder {
    private List<Map<String, Object>> sectionList = new ArrayList<>();

    public static class SectionBuilder {
        private String name;
        private String title;
        private List<Map<String, Object>> bodyList;

        public SectionBuilder(String name, String title) {
            this.name = name;
            this.title = title;
            this.bodyList = new ArrayList<>();
        }

        public SectionBuilder withButton(String name, String title, String action) {
            return withButton(name, title, action, null);
        }

        public SectionBuilder withButton(String name, String title, String action, Map options) {
            Map<String, Object> button = new HashMap<>();
            button.put("name", name);
            button.put("type", "button");
            button.put("title", title);
            button.put("action", action);
            if (options != null) {
                button.putAll(options);
            }
            bodyList.add(button);
            return this;
        }

        public SectionBuilder withDynamicParagraph(String name, String source) {
            bodyList.add(Map.of("name", name, "type", "dynamicParagraph", "source", source));
            return this;
        }

        public Map<String, Object> build() {
            return Map.of("name", name, "title", title, "body", bodyList);
        }
    }

    public PageLayoutBuilder withSection(SectionBuilder section) {
        sectionList.add(section.build());
        return this;
    }

    public List<Map<String, Object>> build() {
        return sectionList;
    }
}
