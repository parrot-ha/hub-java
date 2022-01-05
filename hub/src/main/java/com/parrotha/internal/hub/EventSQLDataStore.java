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
package com.parrotha.internal.hub;

import com.parrotha.device.Event;
import com.parrotha.internal.database.DatasourceFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventSQLDataStore implements EventDataStore {
    private static final Logger logger = LoggerFactory.getLogger(EventSQLDataStore.class);

    private Jdbi jdbi;

    public EventSQLDataStore() {
        jdbi = Jdbi.create(DatasourceFactory.getDataSource());

        jdbi.registerRowMapper(Event.class, (rs, ctx) -> new Event(rs.getString("ID"), rs.getString("NAME"), rs.getString("VALUE"),
                rs.getString("DESCRIPTION_TEXT"), rs.getBoolean("DISPLAYED"), rs.getString("DISPLAY_NAME"),
                rs.getBoolean("IS_STATE_CHANGE"), rs.getString("UNIT"), rs.getString("DATA"),
                Date.from(rs.getTimestamp("DATE").toInstant()), rs.getString("SOURCE"), rs.getString("SOURCE_ID"),
                rs.getBoolean("IS_DIGITAL")));
    }

    @Override
    public void saveEvent(Event event) {
        jdbi.useHandle(handle -> {
            handle.execute("insert into EVENT_HISTORY (ID, NAME, VALUE, DESCRIPTION_TEXT, DISPLAYED, DISPLAY_NAME," +
                            "IS_STATE_CHANGE, UNIT, DATA, DATE, SOURCE, SOURCE_ID, IS_DIGITAL) " +
                            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID().toString(),
                    event.getName(),
                    event.getValue(),
                    event.getDescriptionText(),
                    event.isDisplayed(),
                    event.getDisplayName(),
                    event.isStateChange(),
                    event.getUnit(),
                    event.getData(),
                    event.getDate(),
                    event.getSource(),
                    event.getSourceId(),
                    event.isDigital());
        });
    }

    @Override
    public List<Event> eventsSince(String source, String sourceId, Date date, int maxEvents) {
        List<Event> events = jdbi.withHandle(handle -> {
                    Query query = handle.createQuery("select ID, NAME, VALUE, DESCRIPTION_TEXT, DISPLAYED, DISPLAY_NAME, " +
                                    "IS_STATE_CHANGE, UNIT, DATA, DATE, SOURCE, SOURCE_ID, IS_DIGITAL FROM EVENT_HISTORY " +
                                    "WHERE SOURCE = :source AND SOURCE_ID = :sourceId " +
                                    "AND DATE > :startDate;")
                            .bind("source", source)
                            .bind("sourceId", sourceId)
                            .bind("startDate", date);
                    if (maxEvents > -1) {
                        query.setMaxRows(maxEvents);
                    }
                    return query.mapTo(Event.class).list();
                }
        );
        return events;
    }

    @Override
    public List<Event> eventsBetween(String source, String sourceId, Date startDate, Date endDate, int maxEvents) {
        List<Event> events = jdbi.withHandle(handle ->
                handle.createQuery("select ID, NAME, VALUE, DESCRIPTION_TEXT, DISPLAYED, DISPLAY_NAME, " +
                                "IS_STATE_CHANGE, UNIT, DATA, DATE, SOURCE, SOURCE_ID, IS_DIGITAL FROM EVENT_HISTORY " +
                                "WHERE DATE > :startDate AND DATE < :endDate;")
                        .bind("startDate", startDate)
                        .bind("endDate", endDate)
                        .setMaxRows(maxEvents)
                        .mapTo(Event.class).list()
        );
        return events;
    }
}
