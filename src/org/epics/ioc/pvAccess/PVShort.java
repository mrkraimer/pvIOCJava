/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pvAccess;

/**
 * get/put short data.
 * @author mrk
 *
 */
public interface PVShort extends PVData{
    /**
     * get the <i>short</i> value stored in the field.
     * @return short value of field.
     */
    short get();
    /**
     * put the <i>short</i> value into the field.
     * @param value new short value for field.
     * @throws IllegalStateException if the field is not mutable.
     */
    void put(short value);
}
