package com.parrotha.zwave.internal;

import groovy.beans.DefaultPropertyWriter;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class BeanUtils {
    public static void populate(final Object bean, final Map<String, ? extends Object> properties)
            throws IllegalAccessException, InvocationTargetException {
        for (Object key : properties.keySet()) {
            try {
                DefaultPropertyWriter.INSTANCE.write(bean, (String) key, properties.get(key));
            } catch (GroovyCastException gce) {
                gce.printStackTrace();
            }
        }
    }
}
