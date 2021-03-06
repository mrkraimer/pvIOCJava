/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvioc.support.rpc;

import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvioc.database.PVDatabase;
import org.epics.pvioc.database.PVDatabaseFactory;
import org.epics.pvioc.database.PVRecordStructure;
import org.epics.pvioc.support.AbstractSupport;
import org.epics.pvioc.support.Support;
import org.epics.pvioc.support.SupportProcessRequester;
import org.epics.pvioc.util.RequestResult;

/**
 * Support for remotely get a list of records.
 * @author mrk
 *
 */
public class StructureListFactory {
    /**
     * Create support for getting the list of structures..
     * @param pvRecordStructure The field supported.
     * @return An interface to the support or null if the supportName was not "linkArray".
     */
    public static Support create(PVRecordStructure pvRecordStructure) {
        return new StructureListImpl(pvRecordStructure);
    }
    
    private static final String supportName = "org.epics.pvioc.rpc.structureList";
    
    
    private static class StructureListImpl extends AbstractSupport
    {
       
        private final PVRecordStructure pvRecordStructure;
        private PVString pvDatabaseName = null;
        private PVString pvRegularExpression = null;
        private PVString pvStatus = null;
        private PVStringArray pvNames = null;
        
        private StructureListImpl(PVRecordStructure pvRecordStructure) {
            super(StructureListFactory.supportName,pvRecordStructure); 
            this.pvRecordStructure = pvRecordStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.support.AbstractSupport#initialize()
         */
        @Override
        public void initialize() {
            PVStructure pvStructure = pvRecordStructure.getPVStructure();
            pvDatabaseName = pvStructure.getStringField("argument.database");
            if(pvDatabaseName ==null) return;
            pvRegularExpression = pvStructure.getStringField("argument.regularExpression");
            if(pvRegularExpression ==null) return;
            pvStatus = pvStructure.getStringField("result.status");
            if(pvStatus==null) return;
            PVArray pvArray = pvStructure.getScalarArrayField("result.names",ScalarType.pvString);
            if(pvArray==null) return;
            pvNames = (PVStringArray)pvArray;
            super.initialize();
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.support.AbstractSupport#process(org.epics.pvioc.process.SupportProcessRequester)
         */
        @Override
        public void process(SupportProcessRequester supportProcessRequester) {
            String databaseName = pvDatabaseName.get();
            if(databaseName==null) databaseName = "master";
            PVDatabase pvDatabase = null;
            if(databaseName.equals("master")) {
                pvDatabase = PVDatabaseFactory.getMaster();
            } else if(databaseName.equals("beingInstalled")) {
                pvDatabase = PVDatabaseFactory.getBeingInstalled();
            }
            if(pvDatabase==null) {
                pvStatus.put("database not found");
                pvNames.setLength(0);
            } else {
                String[] names = pvDatabase.structureList(pvRegularExpression.get());
                pvNames.setLength(names.length);
                pvNames.put(0, names.length, names, 0);
                pvStatus.put("success");
            }
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }
    }
}
