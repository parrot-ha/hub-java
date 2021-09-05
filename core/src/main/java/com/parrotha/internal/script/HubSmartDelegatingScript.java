/**
 * Copyright (c) 2021 by the respective copyright holders.
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
package com.parrotha.internal.script;

import groovy.lang.MetaMethod;
import groovy.lang.MissingPropertyException;
import groovy.util.DelegatingScript;

import java.util.Map;

public abstract class ParrotHubDelegatingScript extends DelegatingScript {
    @Override
    public Object getProperty(String property) {
        try {
            return super.getProperty(property);
        } catch (MissingPropertyException e) {
            try {
                return super.getProperty(property);
            } catch (MissingPropertyException mpe) {
                // look for property in entity
                if (getDelegate() instanceof EntityScriptDelegate) {
                    EntityScriptDelegate entityScriptDelegate = (EntityScriptDelegate) getDelegate();
                    Map settings = entityScriptDelegate.getSettings();
                    if (settings != null) {
                        Object propertyObj = settings.get(property);
                        if (propertyObj != null)
                            return propertyObj;
                    }
                }
                // look for method in the entity (this is for automation app subscribe)
                for (MetaMethod method : getMetaClass().getMethods()) {
                    if (method.getName().equals(property) &&
                            (method.getDeclaringClass().getName().startsWith("AA_") ||
                                    method.getDeclaringClass().getName().startsWith("DH_"))) {
                        return method;
                    }
                }

                //it appears that ST will return null if property is not found, so do not throw mpe
                return null;
            }
        }
    }

}
