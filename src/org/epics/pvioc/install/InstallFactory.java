/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvioc.install;

import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvioc.database.PVDatabase;
import org.epics.pvioc.database.PVDatabaseFactory;
import org.epics.pvioc.database.PVRecord;
import org.epics.pvioc.database.PVReplaceFactory;
import org.epics.pvioc.pvAccess.ChannelServerFactory;
import org.epics.pvioc.xml.XMLToPVDatabaseFactory;

/**
 * Factory that implements Install
 * @author mrk
 *
 */
public class InstallFactory {
    /**
     * Get the single instance of Install.
     * @return The instance.
     */
    public static Install get() {
        return InstallImpl.getInstall();
    }
   
    private static class InstallImpl implements Install,Requester {

        private static final PVDatabase master = PVDatabaseFactory.getMaster();
        private static InstallImpl singleImplementation = null;
        private static Requester realRequester = null;
        private static MessageType maxError;
        private static AtomicBoolean isInUse = new AtomicBoolean(false);
        private static synchronized InstallImpl getInstall() {
        	 if (singleImplementation==null) {
                 singleImplementation = new InstallImpl();
                 // Make ChannelServer register itself.
                 ChannelServerFactory.getChannelServer();
             }
             return singleImplementation;
        }
        @Override
        public String getRequesterName() {
            if(realRequester!=null) return realRequester.getRequesterName();
            return "InstallImpl";
        }
        @Override
        public void message(String message, MessageType messageType) {
            if(messageType.compareTo(maxError)>0) maxError = messageType;
            if(realRequester!=null) realRequester.message(message, messageType);
            
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installRecords(org.epics.pvdata.pv.PVDatabase, org.epics.pvdata.pv.Requester)
         */
        public boolean installRecords(PVDatabase pvDatabase, Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                return records(pvDatabase);
            } finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installRecords(java.lang.String, org.epics.pvdata.pv.Requester)
         */
        public boolean installRecords(String xmlFile, Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                return records(xmlFile);
            }
            finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installRecortd(org.epics.pvdata.pv.PVRecord, org.epics.pvdata.pv.Requester)
         */
        public boolean installRecord(PVRecord pvRecord, Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                PVDatabase pvDatabaseAdd = PVDatabaseFactory.create("beingInstalled");
                pvDatabaseAdd.addRecord(pvRecord);
                return records(pvDatabaseAdd);
            } finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installStructure(org.epics.pvdata.pv.PVStructure, java.lang.String, org.epics.pvdata.pv.Requester)
         */
        @Override
        public boolean installStructure(PVStructure pvStructure,String structureName,Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                if(master.findStructure(pvStructure.getFieldName())!=null) {
                    requester.message("structure already in master",
                            MessageType.fatalError);
                    return false;
                }
                master.addStructure(pvStructure,structureName);
                return true;
            } finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installStructures(org.epics.pvdata.pv.PVDatabase, org.epics.pvdata.pv.Requester)
         */
        public boolean installStructures(PVDatabase pvDatabase,Requester requester) {
            realRequester = requester;
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                return structures(pvDatabase);
            } finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#installStructures(java.lang.String, org.epics.pvdata.pv.Requester)
         */
        public boolean installStructures(String xmlFile, Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                return structures(xmlFile);
            } finally {
                realRequester = null;
                isInUse.set(false);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.install.Install#cleanMaster(org.epics.pvdata.pv.Requester)
         */
        public boolean cleanMaster(Requester requester) {
            boolean gotIt = isInUse.compareAndSet(false,true);
            if(!gotIt) {
                requester.message("InstallFactory is already active",
                        MessageType.fatalError);
                return false;
            }
            try {
                realRequester = requester;
                maxError = MessageType.info;
                return master.cleanMaster();
            } finally {
                realRequester =  null;
                isInUse.set(false);
            }
        }

       

        private boolean structures(String file) {
            PVDatabase pvDatabaseAdd = PVDatabaseFactory.create("beingInstalled");
            XMLToPVDatabaseFactory.convert(pvDatabaseAdd,file,this,false,null,null,null);
            if(maxError!=MessageType.info) {
                message("installStructures failed because of xml errors.",
                        MessageType.fatalError);
                return false;
            }
            return structures(pvDatabaseAdd);
        }
        
        private boolean structures(PVDatabase pvDatabase) {
            PVRecord[] pvRecords = pvDatabase.getRecords();
            if(pvRecords.length!=0) {
                message("installStructures failed because new database contained record definitions",
                        MessageType.fatalError);
                return false;
            }
            String[] beingAdded = pvDatabase.getStructureNames();
            String[] master = PVDatabaseFactory.getMaster().getStructureNames();
            for(String add : beingAdded) {
                for(String fromMaster : master) {
                    if(add.equals(fromMaster)) return false; 
                }
            }
            pvDatabase.mergeIntoMaster();
            return true;
        }

        private boolean records(String file) {
            PVDatabase pvDatabaseAdd = PVDatabaseFactory.create("beingInstalled");
            XMLToPVDatabaseFactory.convert(pvDatabaseAdd,file,this);
            if(maxError!=MessageType.info) {
                pvDatabaseAdd.abandon();
                return false;
            }
            boolean result = records(pvDatabaseAdd);
            if(!result) pvDatabaseAdd.abandon();
            return result;
        }
        
        private boolean records(PVDatabase pvDatabaseAdd) {
            PVStructure[] pvStructures = pvDatabaseAdd.getStructures();
            if(pvStructures.length!=0) return false;
            PVReplaceFactory.replace(pvDatabaseAdd);
            SupportCreation supportCreation = SupportCreationFactory.create(pvDatabaseAdd, this);
            boolean gotSupport = supportCreation.createSupport();
            if(!gotSupport) {
                message("Did not find all support.",MessageType.fatalError);
                return false;
            }
            boolean readyForStart = supportCreation.initializeSupport();
            if(!readyForStart) {
                message("initializeSupport failed",MessageType.fatalError);
                return false;
            }
            AfterStart afterStart = AfterStartFactory.create();
            boolean ready = supportCreation.startSupport(afterStart);
            if(!ready) {
                message("startSupport failed",MessageType.fatalError);
                return false;
            }
            afterStart.callRequesters(false);
            pvDatabaseAdd.mergeIntoMaster();
            afterStart.callRequesters(true);
            afterStart = null;
            supportCreation = null;
            pvDatabaseAdd = null;
            return true;
        }
    }
}
