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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Command {
    private String name;
    private List<CommandArgument> arguments;

    public Command() {
    }

    public Command(String name) {
        this.name = name;
        this.arguments = null;
    }

    public Command(String name, List<Object> arguments) {
        this.name = name;
        this.setArguments(arguments);
    }

    public Command(String name, CommandArgument... args) {
        this.name = name;
        this.arguments = Arrays.asList(args);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CommandArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        if (arguments != null) {
            List<CommandArgument> commandArgumentsList = new ArrayList<>();
            for (Object argument : arguments) {
                if (argument instanceof CommandArgument) {
                    commandArgumentsList.add((CommandArgument) argument);
                } else if (argument instanceof String) {
                    commandArgumentsList.add(new CommandArgument((String) argument, "STRING"));
                }
            }
            this.arguments = commandArgumentsList;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Command command = (Command) o;
        return Objects.equals(name, command.name) && Objects.equals(arguments, command.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arguments);
    }

    public static boolean listsAreEqual(List<Command> commandListLeft, List<Command> commandListRight) {
        if (commandListLeft != null) {
            if (commandListRight == null) {
                return false;
            } else {
                return new HashSet<>(commandListLeft).containsAll(commandListRight) && new HashSet<>(commandListRight).containsAll(commandListLeft);
            }
        } else {
            return commandListRight == null;
        }
    }
}
