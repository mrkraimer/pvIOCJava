/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS ovData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvioc.support.alarm;

import org.epics.pvdata.property.AlarmSeverity;
import org.epics.pvdata.property.AlarmStatus;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvioc.database.PVRecordStructure;
import org.epics.pvioc.support.AbstractSupport;
import org.epics.pvioc.support.Support;
import org.epics.pvioc.support.SupportProcessRequester;
import org.epics.pvioc.support.SupportState;
import org.epics.pvioc.util.RequestResult;
/**
 * Support for an longAlarm link.
 * @author mrk
 *
 */
public class LongAlarmFactory {
    /**
     * Create support for an longAlarm structure.
     * @param pvRecordStructure The structure.
     * @return An interface to the support.
     */
    public static Support create(PVRecordStructure pvRecordStructure) {
        return new LongAlarmImpl(pvRecordStructure);
    }
    
    private static class LongAlarmImpl extends AbstractSupport
    {
        private static final String supportName = "org.epics.pvioc.longAlarm";
        private PVRecordStructure pvRecordStructure;
        private PVStructure pvStructure;
        private AlarmSupport alarmSupport;
        private PVLong pvValue;
        
        private PVBoolean pvActive;
        private PVLong pvHystersis;
        private PVLong pvLowAlarmLimit;
        private PVInt pvLowAlarmSeverity;
        private PVLong pvLowWarningLimit;
        private PVInt pvLowWarningSeverity;
        private PVLong pvHighWarningLimit;
        private PVInt pvHighWarningSeverity;
        private PVLong pvHighAlarmLimit;
        private PVInt pvHighAlarmSeverity;
        
        private long lastAlarmIntervalValue;
        private int lastAlarmSeverity = 0;
        private String lastAlarmMessage = null;

       
        private LongAlarmImpl(PVRecordStructure pvRecordStructure) {
            super(supportName,pvRecordStructure);
            this.pvRecordStructure = pvRecordStructure;
            pvStructure = pvRecordStructure.getPVStructure();
        }
        /* (non-Javadoc)
         * @see org.epics.pvioc.support.AbstractSupport#initialize()
         */
        @Override
        public void initialize() {
            if(!super.checkSupportState(SupportState.readyForInitialize,supportName)) return;
            SupportState supportState = SupportState.readyForStart;
            pvValue = pvStructure.getParent().getLongField("value");
            if(pvValue==null) return;
            alarmSupport = AlarmSupportFactory.findAlarmSupport(pvRecordStructure);
            if(alarmSupport==null) {
                super.message("no alarmSupport", MessageType.error);
                return;
            }
            pvActive = pvStructure.getBooleanField("active");
            if(pvActive==null) return;
            pvLowAlarmSeverity = pvStructure.getIntField("lowAlarmSeverity");
            if(pvLowAlarmSeverity==null) return;
            pvLowAlarmLimit = pvStructure.getLongField("lowAlarmLimit");
            if(pvLowAlarmLimit==null) return;
            pvLowWarningSeverity = pvStructure.getIntField("lowWarningSeverity");
            if(pvLowWarningSeverity==null) return;
            pvLowWarningLimit = pvStructure.getLongField("lowWarningLimit");
            if(pvLowWarningLimit==null) return;
            
            pvHighWarningSeverity = pvStructure.getIntField("highWarningSeverity");
            if(pvHighWarningSeverity==null) return;
            pvHighWarningLimit = pvStructure.getLongField("highWarningLimit");
            if(pvHighWarningLimit==null) return;
            pvHighAlarmSeverity = pvStructure.getIntField("highAlarmSeverity");
            if(pvHighAlarmSeverity==null) return;
            pvHighAlarmLimit = pvStructure.getLongField("highAlarmLimit");
            if(pvHighAlarmLimit==null) return;                       
            pvHystersis = pvStructure.getLongField("hysteresis");
            if(pvHystersis==null) return;
            setSupportState(supportState);
        }
                /* (non-Javadoc)
         * @see org.epics.pvioc.process.Support#process(org.epics.pvioc.process.RecordProcessRequester)
         */
        @Override
        public void process(SupportProcessRequester supportProcessRequester) {
            if(pvActive.get()) checkAlarm();
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }                

        private void checkAlarm() {
        	boolean active = pvActive.get();
            if(!active) return;
            long  val = pvValue.get();
            int severity = pvHighAlarmSeverity.get();
            long level = pvHighAlarmLimit.get();
            if(severity>0 && (val>=level)) {
            	raiseAlarm(level,val,severity,"highAlarm");
            	return;
            }
            severity = pvLowAlarmSeverity.get();
            level = pvLowAlarmLimit.get();
            if(severity>0 && (val<=level)) {
            	raiseAlarm(level,val,severity,"lowAlarm");
            	return;
            }
            severity = pvHighWarningSeverity.get();
            level = pvHighWarningLimit.get();
            if(severity>0 && (val>=level)) {
            	raiseAlarm(level,val,severity,"highWarning");
            	return;
            }
            severity = pvLowWarningSeverity.get();
            level = pvLowWarningLimit.get();
            if(severity>0 && (val<=level)) {
            	raiseAlarm(level,val,severity,"lowWarning");
            	return;
            }
            raiseAlarm(0,val,0,"");
        }
        
        private void raiseAlarm(long intervalValue,long val,int severity,String message) {
        	AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(severity);
            if(severity<lastAlarmSeverity) {
                long diff = lastAlarmIntervalValue - val;
                if(diff<0) diff = -diff;
                if(diff<pvHystersis.get()) {
                    alarmSeverity = AlarmSeverity.getSeverity(lastAlarmSeverity);
                    intervalValue = lastAlarmIntervalValue;
                    message = lastAlarmMessage;
                }
            }
            if(alarmSeverity==AlarmSeverity.NONE) {
                lastAlarmSeverity = severity;
                return;
            }
            alarmSupport.setAlarm(message, alarmSeverity,AlarmStatus.RECORD);
            lastAlarmIntervalValue = intervalValue;
            lastAlarmSeverity = severity;
        }
    }
}
