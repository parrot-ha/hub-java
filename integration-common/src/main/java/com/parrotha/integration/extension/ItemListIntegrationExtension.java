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
package com.parrotha.integration.extension;

import java.util.List;
import java.util.Map;

/**
 * Implement this interface if your integration needs to provide a simple form for listing items/devices from the integration page.
 * This interface is typically combined with ItemAddIntegration
 */
public interface ItemListIntegrationExtension {

    Map<String, Object> itemListButton(String id, String button);

    // this method provides the columns for the item list
    List<Map<String, Object>> getItemListLayout();

    List<Map<String, Object>> getItemList();
}
