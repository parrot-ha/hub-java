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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Attribute {
    // does anyone actually use id?
    private String id;
    private String dataType;
    private String name;
    private List<String> possibleValues;

    public Attribute() {
    }

    public Attribute(String id, String dataType, String name, List<String> possibleValues) {
        this.id = id;
        this.dataType = dataType;
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public String getId() {
        return id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attribute attribute = (Attribute) o;
        return Objects.equals(name, attribute.name) &&
                Objects.equals(dataType, attribute.dataType) && Objects.equals(possibleValues, attribute.possibleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, possibleValues);
    }

    public static boolean listsAreEqual(List<Attribute> leftList, List<Attribute> rightList) {
        if (leftList != null) {
            if (rightList == null) {
                return false;
            } else {
                return new HashSet<>(leftList).containsAll(rightList) && new HashSet<>(rightList).containsAll(leftList);
            }
        } else {
            return rightList == null;
        }
    }
}
