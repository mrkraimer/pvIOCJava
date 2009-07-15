/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.swtshell;

/**
 * Create a request PVStructure for channel access.
 * @author mrk
 *
 */
public interface CreateRequest {
    /**
     * Create the request
     */
    void create();
}