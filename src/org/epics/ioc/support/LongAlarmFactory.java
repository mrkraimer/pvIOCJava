/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support;

import org.epics.ioc.create.Enumerated;
import org.epics.ioc.db.*;
import org.epics.ioc.process.*;
import org.epics.ioc.pv.*;
import org.epics.ioc.util.*;

/**
 * Support for an longAlarm link.
 * @author mrk
 *
 */
public class LongAlarmFactory {
    /**
     * Create support for an longAlarm link.
     * @param dbStructure The structure.
     * @return An interface to the support or null if the supportName was not "longArray".
     */
    public static Support create(DBStructure dbStructure) {
        PVStructure pvStructure = dbStructure.getPVStructure();
        String supportName = pvStructure.getSupportName();
        if(supportName==null || !supportName.equals(supportName)) {
            pvStructure.message("does not have support " + supportName,MessageType.error);
            return null;
        }
        return new LongAlarmImpl(dbStructure);
    }
    
    private static String supportName = "longAlarm";
    
    private static class LongAlarmImpl extends AbstractSupport
    {
        private DBStructure dbStructure;
        private PVStructure pvStructure;
        
        private boolean noop;
        private AlarmSupport alarmSupport;
        
        private PVStructureArray intervalPVArray;
        private PVInt pvOutOfRange;
        private PVBoolean pvActive;
        private PVLong pvHystersis;
        
        private DBNonScalarArray dbAlarmIntervalArray = null;
        private PVLong[] pvAlarmIntervalValue = null;
        private PVInt[] pvAlarmIntervalSeverity = null;
        
        private PVLong pvValue;
        private long lastAlarmIntervalValue;
        private int lastAlarmSeverityIndex;
       
        private LongAlarmImpl(DBStructure dbStructure) {
            super(supportName,dbStructure);
            this.dbStructure = dbStructure;
            pvStructure = dbStructure.getPVStructure();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#initialize()
         */
        public void initialize() {
            if(!super.checkSupportState(SupportState.readyForInitialize,supportName)) return;
            SupportState supportState = SupportState.readyForStart;
            DBField dbParent = dbStructure.getParent();
            PVField pvParent = dbParent.getPVField();
            PVField pvField = pvParent.findProperty("value");
            if(pvField==null) {
                pvStructure.message("value field not found", MessageType.error);
                return;
            }
            DBField valueDBField = dbStructure.getDBRecord().findDBField(pvField);
            pvField = valueDBField.getPVField();
            if(pvField.getField().getType()!=Type.pvLong) {
                super.message("field type is not long", MessageType.error);
                return;
            }
            pvValue = (PVLong)pvField;
            noop = false;
            alarmSupport = AlarmFactory.findAlarmSupport(dbStructure);
            if(alarmSupport==null) {
                super.message("no alarmSupport", MessageType.error);
                noop = true;
                setSupportState(supportState);
                return;
            }
            DBField[] dbFields = dbStructure.getFieldDBFields();
            if(dbFields.length==0) {
                noop = true;
                setSupportState(supportState);
                return;
            }
            Structure structure = dbStructure.getPVStructure().getStructure();
            pvActive = pvStructure.getBooleanField("active");
            if(pvActive==null) return;
            
            intervalPVArray = (PVStructureArray)pvStructure.getArrayField(
                "interval", Type.pvStructure);
            if(intervalPVArray==null) return;
            int index = structure.getFieldIndex("interval");
            dbAlarmIntervalArray = (DBNonScalarArray)dbFields[index];
            index = structure.getFieldIndex("outOfRange");
            if(index<0) {
                super.message("outOfRange does not exist", MessageType.error);
                return;
            }
            Enumerated enumerated = AlarmSeverity.getAlarmSeverity(dbFields[index]);
            if(enumerated==null) return;
            pvOutOfRange = enumerated.getIndexField();
            if(pvOutOfRange==null) return;
            pvHystersis = pvStructure.getLongField("hystersis");
            if(pvHystersis==null) return;
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
            DBField[] dbFields = dbAlarmIntervalArray.getElementDBFields();
            pvAlarmIntervalValue = new PVLong[size];
            pvAlarmIntervalSeverity = new PVInt[size];
            
            for(int i=0; i<size; i++) {
                DBStructure dbStructure = (DBStructure)dbFields[i];
                PVStructure pvStructure = dbStructure.getPVStructure();
                Structure structure = pvStructure.getStructure();
                PVField[] pvFields = pvStructure.getFieldPVFields();
                Field[] fields = structure.getFields();
                int index = structure.getFieldIndex("value");
                if(index<0) {
                    super.message("invalid interval no value field", MessageType.error);
                    return;
                }
                Field field = fields[index];
                if(field.getType()!=Type.pvLong) {
                    super.message("invalid interval value field is not long", MessageType.error);
                    return;
                }
                pvAlarmIntervalValue[i] = (PVLong)pvFields[index];
                index = structure.getFieldIndex("severity");
                if(index<0) {
                    super.message("invalid interval no severity field", MessageType.error);
                    return;
                }
                Enumerated enumerated = AlarmSeverity.getAlarmSeverity(
                        dbStructure.getFieldDBFields()[index]);
                if(enumerated==null) {
                    super.message("invalid interval severity field is not alarmSeverity", MessageType.error);
                    return;
                }
                pvAlarmIntervalSeverity[i] = enumerated.getIndexField();
            }
            lastAlarmSeverityIndex = 0;
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
            pvOutOfRange = null;
            intervalPVArray = null;
            pvHystersis = null;
            setSupportState(SupportState.readyForInitialize);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#process(org.epics.ioc.process.RecordProcessRequester)
         */
        public void process(SupportProcessRequester supportProcessRequester) {
            if(!noop && pvActive.get()) checkAlarm();
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }                

        private void checkAlarm() {
            boolean active = pvActive.get();
            if(!active) return;
            long  val = pvValue.get();
            int len = pvAlarmIntervalValue.length;
            long intervalValue = 0;
            for(int i=0; i<len; i++) {
                intervalValue = pvAlarmIntervalValue[i].get();
                if(val<=intervalValue) {
                    int sevIndex = pvAlarmIntervalSeverity[i].get();
                    raiseAlarm(intervalValue,val,sevIndex);
                    return;
                }
            }
            int outOfRange = pvOutOfRange.get();
            // intervalValue is pvAlarmIntervalValue[len-1].get();
            raiseAlarm(intervalValue,val,outOfRange);
        }
        
        private void raiseAlarm(long intervalValue,long val,int severityIndex) {
            AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(severityIndex);
            if(severityIndex<lastAlarmSeverityIndex) {
                long diff = lastAlarmIntervalValue - val;
                if(diff<0.0) diff = -diff;
                if(diff<pvHystersis.get()) {
                    alarmSeverity = AlarmSeverity.getSeverity(lastAlarmSeverityIndex);
                    intervalValue = lastAlarmIntervalValue;
                }
            }
            if(alarmSeverity==AlarmSeverity.none) {
                lastAlarmSeverityIndex = severityIndex;
                return;
            }
            String message = pvStructure.getFullFieldName() + " " + alarmSeverity.toString();
            alarmSupport.setAlarm(message, alarmSeverity);
            lastAlarmIntervalValue = intervalValue;
            lastAlarmSeverityIndex = severityIndex;
        }
    }
}
