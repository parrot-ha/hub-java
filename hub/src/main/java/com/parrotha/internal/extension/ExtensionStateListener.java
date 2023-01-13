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
package com.parrotha.internal.extension;

import org.apache.commons.lang3.tuple.Pair;

public interface ExtensionStateListener {
    void stateUpdated(ExtensionState state);

    /**
     * Implementing class should return true if the extension is in use, in addition a message can be returned explaining the usage.
     *
     * @param extensionId The extension id to check for usage
     * @return
     */
    Pair<Boolean, String> isExtensionInUse(String extensionId);

}
