/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support;

import org.epics.ioc.db.*;
import org.epics.ioc.process.*;
import org.epics.ioc.pv.*;
import org.epics.ioc.util.*;

/**
 * Support for an shortAlarm link.
 * @author mrk
 *
 */
public class ShortAlarmFactory {
    /**
     * Create support for an shortAlarm link.
     * @param dbLink The link.
     * @return An interface to the support or null if the supportName was not "shortArray".
     */
    public static Support create(DBLink dbLink) {
        PVLink pvLink = dbLink.getPVLink();
        String supportName = pvLink.getSupportName();
        if(supportName==null || !supportName.equals(supportName)) {
            pvLink.message("does not have support " + supportName,MessageType.error);
            return null;
        }
        return new ShortAlarmImpl(dbLink);
    }
    
    private static String supportName = "shortAlarm";
    
    private static class ShortAlarmImpl extends AbstractLinkSupport
    {
        private DBLink dbLink;
        private PVLink pvLink;
        
        private boolean noop;
        private AlarmSupport alarmSupport;
        
        private StructureArrayData structureArrayData = new StructureArrayData();
        private PVStructureArray intervalPVArray;
        private PVMenu outOfRangePVMenu;
        private PVBoolean pvActive;
        
        private PVShort[] pvAlarmIntervalValue = null;
        private PVMenu[] pvAlarmIntervalSeverity = null;
        
        private PVShort pvValue;
       
        private ShortAlarmImpl(DBLink dbLink) {
            super(supportName,dbLink);
            this.dbLink = dbLink;
            pvLink = dbLink.getPVLink();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#initialize()
         */
        public void initialize() {
            if(!super.checkSupportState(SupportState.readyForInitialize,supportName)) return;
            SupportState supportState = SupportState.readyForStart;
            noop = false;
            if(pvValue==null) {
                super.message("setField was not called with a short field", MessageType.error);
                noop = true;
                return;
            }
            PVStructure configStructure = super.getConfigStructure("shortAlarm", false);
            if(configStructure==null) {
                noop = true;
                setSupportState(supportState);
                return;
            }
            alarmSupport = AlarmFactory.findAlarmSupport(dbLink);
            if(alarmSupport==null) {
                super.message("no alarmSupport", MessageType.error);
                noop = true;
                setSupportState(supportState);
                return;
            }
            pvActive = configStructure.getBooleanField("active");
            if(pvActive==null) return;
            intervalPVArray = (PVStructureArray)configStructure.getArrayField(
                "interval", Type.pvStructure);
            if(intervalPVArray==null) return;
            outOfRangePVMenu = configStructure.getMenuField("outOfRange", "alarmSeverity");
            if(outOfRangePVMenu==null) return;
            setSupportState(supportState);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#start()
         */
        public void start() {
            if(!super.checkSupportState(SupportState.readyForStart,supportName)) return;
            SupportState supportState = SupportState.ready;
            if(noop) {
                setSupportState(supportState);
                return;
            }
            int size = intervalPVArray.getLength();
            if(size<=0) {
                super.message("invalid interval", MessageType.error);
                return;
            }
            pvAlarmIntervalValue = new PVShort[size];
            pvAlarmIntervalSeverity = new PVMenu[size];
            int num = intervalPVArray.get(0, size, structureArrayData);
            if(num!=size) {
                super.message("intervalPVArray num != size", MessageType.error);
                return;
            }
            PVStructure[] pvStructures = structureArrayData.data;
            for(int i=0; i<size; i++) {
                PVStructure pvStructure = pvStructures[i];
                if(pvStructure==null) {
                    super.message("invalid interval is null", MessageType.error);
                    return;
                }
                Structure structure = pvStructure.getStructure();
                PVField[] pvFields = pvStructure.getFieldPVFields();
                Field[] fields = structure.getFields();
                int index = structure.getFieldIndex("value");
                if(index<0) {
                    super.message("invalid interval no value field", MessageType.error);
                    return;
                }
                Field field = fields[index];
                if(field.getType()!=Type.pvShort) {
                    super.message("invalid interval value field is not short", MessageType.error);
                    return;
                }
                pvAlarmIntervalValue[i] = (PVShort)pvFields[index];
                index = structure.getFieldIndex("severity");
                if(index<0) {
                    super.message("invalid interval no severity field", MessageType.error);
                    return;
                }
                field = fields[index];
                if(field.getType()!=Type.pvMenu) {
                    super.message("invalid interval severity field is not a menu", MessageType.error);
                    return;
                }
                Menu menu = (Menu)field;
                if(!menu.getMenuName().equals("alarmSeverity")) {
                    super.message("invalid interval severity field is not an alarmSeverity menu", MessageType.error);
                    return;
                }
                pvAlarmIntervalSeverity[i] = (PVMenu)pvFields[index];
            }
            setSupportState(supportState);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#stop()
         */
        public void stop() {
            if(super.getSupportState()!=SupportState.ready) return;
            pvAlarmIntervalValue = null;
            pvAlarmIntervalSeverity = null;
            setSupportState(SupportState.readyForStart);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#uninitialize()
         */
        public void uninitialize() {
            if(super.getSupportState()!=SupportState.ready) return;
            pvActive = null;
            outOfRangePVMenu = null;
            intervalPVArray = null;
            setSupportState(SupportState.readyForInitialize);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#process(org.epics.ioc.process.RecordProcessRequester)
         */
        public void process(SupportProcessRequester supportProcessRequester) {
            if(!noop && pvActive.get()) checkAlarm();
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }                
        /* (non-Javadoc)
         * @see org.epics.ioc.process.LinkSupport#setField(org.epics.ioc.db.DBField)
         */
        public void setField(DBField dbField) {
            PVField pvField = dbField.getPVField();
            if(pvField.getField().getType()!=Type.pvShort) {
                super.message("setField: field type is not short", MessageType.error);
                return;
            }
            pvValue = (PVShort)pvField;
        }

        private void checkAlarm() {
            boolean active = pvActive.get();
            if(!active) return;
            short  val = pvValue.get();
            int len = pvAlarmIntervalValue.length;
            short intervalValue = 0;
            for(int i=0; i<len; i++) {
                intervalValue = pvAlarmIntervalValue[i].get();
                if(val<=intervalValue) {
                    int sevIndex = pvAlarmIntervalSeverity[i].getIndex();
                    raiseAlarm(sevIndex);
                    return;
                }
            }
            int outOfRange = outOfRangePVMenu.getIndex();
            // intervalValue is pvAlarmIntervalValue[len-1].get();
            raiseAlarm(outOfRange);
        }
        
        private void raiseAlarm(int severityIndex) {
            AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(severityIndex); 
            String message = pvLink.getFullFieldName() + " " + alarmSeverity.toString();
            alarmSupport.setAlarm(message, alarmSeverity);
        }
    }
}
