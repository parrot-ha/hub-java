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
package com.parrotha.internal.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceDelegateHelper {
    static void command(Map definitionSection, String name, List<Object> arguments) {
        if (definitionSection != null) {
            List<Command> commandList;
            if (definitionSection.get("commandList") == null) {
                commandList = new ArrayList<>();
                definitionSection.put("commandList", commandList);
            } else {
                commandList = (List<Command>) definitionSection.get("commandList");
            }

            if (arguments != null) {
                List<Object> commandArgumentsList = new ArrayList<>();
                int argCount = 1;
                for (Object argument : arguments) {
                    if (argument instanceof List) {
                        List argumentAsList = (List) argument;
                        if (((List<?>) argument).size() > 2) {
                            commandArgumentsList.add(new CommandArgument((String) argumentAsList.get(0), (String) argumentAsList.get(1),
                                    (Boolean) argumentAsList.get(2)));
                        } else if (((List<?>) argument).size() > 1) {
                            commandArgumentsList.add(new CommandArgument((String) argumentAsList.get(0), (String) argumentAsList.get(1)));
                        } else if (((List<?>) argument).size() > 0) {
                            commandArgumentsList.add(new CommandArgument((String) argumentAsList.get(0)));
                        }
                    } else if (argument instanceof CommandArgument) {
                        commandArgumentsList.add(argument);
                    } else if (argument instanceof String) {
                        commandArgumentsList.add(new CommandArgument("arg" + argCount++, (String) argument));
                    }
                }
                commandList.add(new Command(name, commandArgumentsList));
            } else {
                commandList.add(new Command(name));
            }
        }
    }
}
