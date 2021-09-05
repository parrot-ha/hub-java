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
package com.parrotha.internal;

import com.parrotha.internal.database.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public void createDatabase() {

        try {
            logger.trace("Creating database");
            Connection c = DriverManager.getConnection("jdbc:hsqldb:file:database/testdb", "SA", "");
            // check for existing tables
            ScriptRunner scriptRunner = new ScriptRunner(c, true, true);
            DatabaseMetaData md = c.getMetaData();

            createTableTablesIfNotExisting(md, scriptRunner, "QRTZ_%", "/org/quartz/impl/jdbcjobstore/tables_hsqldb.sql");
            createTableTablesIfNotExisting(md, scriptRunner, "EVENT_HISTORY", "/database/hsqldb/create_event_history.sql");
            createTableTablesIfNotExisting(md, scriptRunner, "DEVICE_STATE_HISTORY", "/database/hsqldb/create_device_state_history.sql");

            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableTablesIfNotExisting(DatabaseMetaData md, ScriptRunner scriptRunner, String tableNamePattern, String scriptFile) {
        try {
            ResultSet rs = md.getTables(null, null, tableNamePattern, null);
            if (!rs.next()) {
                logger.trace("Creating database tables");
                // create tables
                InputStream inputStream = getClass().getResourceAsStream(scriptFile);
                scriptRunner.runScript(new InputStreamReader(inputStream));
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
