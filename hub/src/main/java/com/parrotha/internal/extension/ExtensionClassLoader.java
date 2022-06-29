/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
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
package com.parrotha.internal.extension;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Objects;

/**
 * A custom classloader that will allow for looking up resources without checking parent classloader.
 */
public class ExtensionClassLoader extends URLClassLoader {

    public ExtensionClassLoader(URL[] urls) {
        super(urls);
    }

    public Enumeration getResources(String name, boolean includeParent) throws IOException {
        Objects.requireNonNull(name);

        if (includeParent) {
            return super.getResources(name);
        } else {
            return this.findResources(name);
        }
    }

}
