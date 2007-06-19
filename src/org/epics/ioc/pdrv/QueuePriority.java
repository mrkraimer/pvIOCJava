/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pdrv;

/**
 * 
 * Priorities for queueRequest if the port can block.
 * @author mrk
 *
 */
public enum QueuePriority {
    /**
     * Priority low
     */
    low,
    /**
     * Priority medium
     */
    medium,
    /**
     * Priority high
     */
    high
}
