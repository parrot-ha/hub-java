package com.parrotha.integration.zigbee;

import com.zsmartsystems.zigbee.ZigBeeCommand;
import com.zsmartsystems.zigbee.zcl.ZclCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZigBeeMessageTransformerTest {
    @Test
    public void testManufacturerSpecificRawCommand() {
        ZigBeeCommand zigBeeCommand = ZigBeeMessageTransformer.createCommand("ph raw 0x99A2 0x01 0x01 0x0006 { 053412FF030A00 }", null);
        assertNotNull(zigBeeCommand);
        assertTrue(zigBeeCommand instanceof ZclCommand);
        ZclCommand zclCommand = (ZclCommand) zigBeeCommand;
        assertTrue(zclCommand.isManufacturerSpecific());
        assertEquals(4660, zclCommand.getManufacturerCode());
    }
}
