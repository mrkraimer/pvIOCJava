/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.dbAccess;

import org.epics.ioc.pvAccess.*;
import org.epics.ioc.dbDefinition.*;

/**
 * Base interface for database array data.
 * @author mrk
 *
 */
public interface DBArray extends DBData, PVArray {
    /**
     * Get the element DBType.
     * @return The DBType.
     */
    DBType getElementDBType();
}
