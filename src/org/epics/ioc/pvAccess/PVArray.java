/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pvAccess;

/**
 * Base interface for array data.
 * Each PVType has an array interface that extends PVArray.
 * @author mrk
 *
 */
public interface PVArray extends PVData{
    /**
     * Get the current length of the array.
     * @return The current length of the array.
     */
    int getLength();
    /**
     * Set the length of the array.
     * @param len Set the length.
     * @throws IllegalStateException if the field is not mutable.
     */
    void setLength(int len);
    /**
     * Get the current capacity of the array,
     * i.e. the allocated number of elements.
     * @return The capacity.
     */
    int getCapacity();
    /**
     * Set the capacity.
     * @param len The new capacity for the array.
     * @throws IllegalStateException if the capacity can't be changed.
     */
    void setCapacity(int len);
    /**
     * Can the capacity be changed?
     * @return if it can be modified.
     */
    boolean isCapacityMutable();
    /**
     * Get the element type.
     * @return The element type.
     */
    Type getElementType();
}
