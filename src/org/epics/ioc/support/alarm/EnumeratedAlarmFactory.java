/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support.alarm;

import org.epics.ioc.database.PVRecordStructure;
import org.epics.ioc.install.AfterStart;
import org.epics.ioc.support.AbstractSupport;
import org.epics.ioc.support.Support;
import org.epics.ioc.support.SupportProcessRequester;
import org.epics.ioc.support.SupportState;
import org.epics.ioc.util.RequestResult;
import org.epics.pvData.property.AlarmSeverity;
import org.epics.pvData.property.AlarmStatus;
import org.epics.pvData.property.PVEnumerated;
import org.epics.pvData.property.PVEnumeratedFactory;
import org.epics.pvData.pv.IntArrayData;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;

/**
 * Support for alarms for an enumerated value.
 * @author mrk
 *
 */
public class EnumeratedAlarmFactory {
    /**
     * Create support for a digitalAlarm structure.
     * @param pvRecordStructure The structure.
     * @return An interface to the support.
     */
    public static Support create(PVRecordStructure pvRecordStructure) {
        return new EnumeratedAlarmImpl(pvRecordStructure);
    }
    
    private static class EnumeratedAlarmImpl extends AbstractSupport
    {
        private static final String supportName = "org.epics.ioc.enumeratedAlarm";
        private final IntArrayData stateSeverityData = new IntArrayData();
        private PVRecordStructure pvRecordStructure;
        private PVStructure pvStructure;
        private boolean noop;
        private AlarmSupport alarmSupport;
        
        private PVBoolean pvActive;
        private PVIntArray pvStateSeverity = null;
        private PVInt pvChangeStateSeverity = null;
        private PVInt pvValue;
        
        private int prevValue = 0;
       
        private EnumeratedAlarmImpl(PVRecordStructure pvRecordStructure) {
            super(supportName,pvRecordStructure);
            this.pvRecordStructure = pvRecordStructure;
            pvStructure = pvRecordStructure.getPVStructure();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.support.AbstractSupport#initialize()
         */
        @Override
        public void initialize() {
            if(!super.checkSupportState(SupportState.readyForInitialize,supportName)) return;
            PVStructure pvStruct = pvStructure.getParent().getStructureField("value");
            if(pvStruct==null) return;
            PVEnumerated enumerated = PVEnumeratedFactory.create();
            if(!enumerated.attach(pvStruct)) {
                pvStruct.message(" is not an enumerated structure", MessageType.error);
                return;
            }
            pvValue = pvStruct.getIntField("index");
            alarmSupport = AlarmSupportFactory.findAlarmSupport(pvRecordStructure);
            if(alarmSupport==null) {
                super.message("no alarmSupport", MessageType.error);
                return;
            }
            pvActive = pvStructure.getBooleanField("active");
            if(pvActive==null) return;
            PVArray pvArray = pvStructure.getScalarArrayField("stateSeverity",ScalarType.pvInt);
            if(pvArray==null) return;
            pvStateSeverity = (PVIntArray)pvArray;
            if(enumerated.getChoices().length!=pvArray.getLength()) {
            	super.message("value.length != stateSeverity.length", MessageType.error);
            	return;
            }
            pvChangeStateSeverity = pvStructure.getIntField("changeStateSeverity");
            if(pvChangeStateSeverity==null) return;
            setSupportState(SupportState.readyForStart);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#start()
         */
        @Override
        public void start(AfterStart afterStart) {
            if(!super.checkSupportState(SupportState.readyForStart,supportName)) return;
            if(noop) {
                setSupportState(SupportState.ready);
                return;
            }
            prevValue = pvValue.get();
            setSupportState(SupportState.ready);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#stop()
         */
        @Override
        public void stop() {
            if(super.getSupportState()!=SupportState.ready) return;
            setSupportState(SupportState.readyForStart);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#uninitialize()
         */
        @Override
        public void uninitialize() {
            if(super.getSupportState()!=SupportState.ready) return;
            if(noop) {
                setSupportState(SupportState.readyForInitialize);
                return;
            }
            pvActive = null;
            pvStateSeverity = null;
            pvChangeStateSeverity = null;
            pvValue = null;
            setSupportState(SupportState.readyForInitialize);
        }       
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#process(org.epics.ioc.process.RecordProcessRequester)
         */
        @Override
        public void process(SupportProcessRequester supportProcessRequester) {
            if(noop || !pvActive.get()) {
                supportProcessRequester.supportProcessDone(RequestResult.success);
                return;
            }
            int  value = pvValue.get();
            pvStateSeverity.get(0, pvStateSeverity.getLength(),stateSeverityData);
            int[] severities = stateSeverityData.data;
            int severity = severities[value];
            if(severity>0) {
            	alarmSupport.setAlarm("stateAlarm",AlarmSeverity.getSeverity(severity),AlarmStatus.RECORD);
            }
            
            if(prevValue!=value) {
                prevValue = value;
                int index = pvChangeStateSeverity.get();
                alarmSupport.setAlarm("changeStateAlarm",AlarmSeverity.getSeverity(index),AlarmStatus.RECORD);
            }
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }                
    }
}
