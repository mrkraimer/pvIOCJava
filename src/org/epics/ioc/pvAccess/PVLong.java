/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pvAccess;

/**
 * get/put long data.
 * @author mrk
 *
 */
public interface PVLong extends PVData{
    /**
     * get the <i>long</i> value stored in the field.
     * @return long value of field.
     */
    long get();
    /**
     * put the <i>long</i> value into the field.
     * @param value new long value for field.
     * @throws IllegalStateException if the field is not mutable.
     */
    void put(long value);
}
