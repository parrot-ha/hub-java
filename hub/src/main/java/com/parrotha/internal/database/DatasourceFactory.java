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
package com.parrotha.internal.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;

public class DatasourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceFactory.class);

    private static ComboPooledDataSource dataSource;

    public static ComboPooledDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new ComboPooledDataSource();
            try {
                //TODO: get these settings from the configuration.yaml file, we could be using an external database
                dataSource.setDriverClass("org.hsqldb.jdbcDriver"); //loads the jdbc driver
                dataSource.setJdbcUrl("jdbc:hsqldb:file:database/testdb");
                dataSource.setUser("SA");
                dataSource.setPassword("");
            } catch (PropertyVetoException e) {
                logger.warn("Exception", e);
            }
        }
        return dataSource;
    }

    public static void setDataSource(ComboPooledDataSource dataSource1) {
        dataSource = dataSource1;
    }

    public static void shutdown() {
        if (dataSource != null)
            dataSource.close();
        dataSource = null;
    }
}
