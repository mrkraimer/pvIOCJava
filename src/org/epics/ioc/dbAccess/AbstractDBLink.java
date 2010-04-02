/**
 * 
 */
package org.epics.ioc.dbAccess;

import org.epics.ioc.dbDefinition.*;
import org.epics.ioc.pvAccess.*;

/**
 * Abstract base class for DBLink
 * @author mrk
 *
 */
public abstract class AbstractDBLink extends AbstractDBStructure implements DBLink
{
    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#getConfigDBStructure()
     */
    public DBStructure getConfigDBStructure() {
        return configDBStructure;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#putConfigDBStructure(org.epics.ioc.dbAccess.DBStructure)
     */
    public void putConfigDBStructure(DBStructure dbStructure) {
        configDBStructure = dbStructure;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#getConfigStructureFieldName()
     */
    public String getConfigStructureName() {
        return pvConfigStructName.get();
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#putConfigStructureFieldName(java.lang.String)
     */
    public void putConfigStructureName(String name) {
        pvConfigStructName.put(name);
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#getLinkSupportName()
     */
    public String getLinkSupportName() {
        return pvLinkSupportName.get();
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.dbAccess.DBLink#putLinkSupportName(java.lang.String)
     */
    public void putLinkSupportName(String name) {
        pvLinkSupportName.put(name);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { return getString(0);}

    /* (non-Javadoc)
     * @see org.epics.ioc.pvAccess.PVData#toString(int)
     */
    public String toString(int indentLevel) {
        return getString(indentLevel);
    }

    private String getString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString(indentLevel));
        if(configDBStructure!=null) {
            builder.append(configDBStructure.toString(indentLevel));
        }
        return builder.toString();
    }

    /**
     * Constructor
     * @param dbdLinkField
     */
    AbstractDBLink(DBDField dbdField)
    {
        super(dbdField);
        assert(super.pvData.length==2);
        PVData linkSupport = super.pvData[0];
        Field field = linkSupport.getField();
        assert(field.getType()==Type.pvString);
        assert(field.getName().equals("linkSupportName"));
        pvLinkSupportName = (PVString)linkSupport;
        PVData config = super.pvData[1];
        field = config.getField();
        assert(field.getType()==Type.pvString);
        assert(field.getName().equals("configStructureName"));
        pvConfigStructName = (PVString)config;
    }

    private PVString pvConfigStructName;
    private PVString pvLinkSupportName;
    private DBStructure configDBStructure = null;
}