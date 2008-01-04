/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pv;

/**
 * Interface for enumerated data.
 * @author mrk
 *
 */
public interface PVEnumerated {
    /**
     * Get the index field of an enumerated structure.
     * @return The interface.
     */
    PVInt getIndexField();
    /**
     * Get the choice field of an enumerated structure.
     * @return The interface.
     */
    PVString getChoiceField();
    /**
     * * Get the choices field of an enumerated structure.
     * @return The interface.
     */
    PVStringArray getChoicesField();
    /**
     * Get the PVField interface.
     * @return The interface.
     */
    PVField getPVField();
}
