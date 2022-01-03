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
package com.parrotha.internal.device;

import com.parrotha.app.DeviceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class DeviceLogger {
    private Logger log;

    public DeviceLogger(DeviceWrapper device) {
        log = LoggerFactory.getLogger("parrothub.live.dev." + device.getId());
    }

    public void trace(Object obj) {
        if (obj != null) {
            log.trace(obj.toString());
        } else {
            log.trace("null");
        }
    }

    public void debug(Object obj) {
        if (obj != null) {
            log.debug(obj.toString());
        } else {
            log.debug("null");
        }
    }

    public void info(Object obj) {
        if (obj != null) {
            log.info(obj.toString());
        } else {
            log.info("null");
        }
    }

    public void warn(Object obj) {
        if (obj != null) {
            log.warn(obj.toString());
        } else {
            log.warn("null");
        }
    }

    public void error(Object obj) {
        if (obj != null) {
            log.error(obj.toString());
        } else {
            log.error("null");
        }
    }

}
