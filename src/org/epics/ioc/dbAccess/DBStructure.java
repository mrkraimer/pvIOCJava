/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.dbAccess;

import org.epics.ioc.dbProcess.RecordSupport;
import org.epics.ioc.pvAccess.*;

/**
 * Data Interface for field that is a structure.
 * @author mrk
 *
 */
public interface DBStructure extends DBData, PVStructure {
    /**
     * get the reflection interfaces for the fields.
     * @return an array of <i>DBField</i> that describes each of the fields in the structure.
     */
    DBData[] getFieldDBDatas();
    /**
     * Get the index of the DBData for the specified field.
     * @param fieldName the name of the field.
     * @return the index or -1 if the field does not exist.
     */
    int getFieldDBDataIndex(String fieldName);
    /**
     * get the structure support for this structure instance.
     * @return the RecordSupport or null if no support has been set.
     */
    RecordSupport getStructureSupport();
    /**
     * set the structure support.
     * @param support the support.
     * @return true if the support was set and false if the support already was set.
     */
    boolean setStructureSupport(RecordSupport support);
}
