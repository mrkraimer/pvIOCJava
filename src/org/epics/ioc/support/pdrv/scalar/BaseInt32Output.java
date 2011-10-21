/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support.pdrv.scalar;

import org.epics.ioc.database.PVRecordStructure;
import org.epics.ioc.install.AfterStart;
import org.epics.ioc.pdrv.Status;
import org.epics.ioc.pdrv.Trace;
import org.epics.ioc.pdrv.interfaces.Int32;
import org.epics.ioc.pdrv.interfaces.Interface;
import org.epics.ioc.support.SupportState;
import org.epics.ioc.support.pdrv.AbstractPortDriverSupport;
import org.epics.pvData.property.AlarmSeverity;
import org.epics.pvData.property.AlarmStatus;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.Type;

/**
 * Implement Int32Output.
 * @author mrk
 *
 */
public class BaseInt32Output extends AbstractPortDriverSupport
{
    /**
     * The constructor.
     * @param pvRecordStructure The structure being supported.
     * @param supportName The name of the support.
     */
    public BaseInt32Output(PVRecordStructure pvRecordStructure,String supportName) {
        super(supportName,pvRecordStructure);
    }

    private Int32 int32 = null;
    private int value;
    private Status status = Status.success;
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.AbstractPortDriverSupport#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        if(!super.checkSupportState(SupportState.readyForStart,supportName)) return;
        if(valuePVField.getField().getType()==Type.scalar) return;
        super.uninitialize();
        pvStructure.message("value field is not a scalar type", MessageType.fatalError);
        return;
    }      
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.AbstractPortDriverSupport#start()
     */
    @Override
    public void start(AfterStart afterStart) {
        super.start(afterStart);
        if(!super.checkSupportState(SupportState.ready,supportName)) return;
        Interface iface = device.findInterface(user, "int32");
        if(iface==null) {
            pvStructure.message("interface int32 not supported", MessageType.fatalError);
            super.stop();
            return;
        }
        int32 = (Int32)iface;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.AbstractPortDriverSupport#stop()
     */
    @Override
    public void stop() {
        super.stop();
        int32 = null;
    }            
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.PortDriverSupport#processCallback()
     */
    @Override
    public void beginProcess() {
        value = convert.toInt((PVScalar)valuePVField);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.AbstractPortDriverSupport#queueCallback()
     */
    @Override
    public void queueCallback() {
        if((deviceTrace.getMask()&Trace.SUPPORT)!=0) {
            deviceTrace.print(Trace.SUPPORT, "pv %s value = %d", fullName,value);
        }
        status = int32.write(user, value);
        if(status!=Status.success) {
            if((deviceTrace.getMask()&Trace.SUPPORT)!=0) {
                deviceTrace.print(Trace.ERROR,
                    "pv %s support %s int32.write failed", fullName,supportName);
            }
            return;
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.support.pdrv.AbstractPortDriverSupport#endProcess()
     */
    @Override
    public void endProcess() {
        if(status!=Status.success) {
            alarmSupport.setAlarm(user.getMessage(), AlarmSeverity.INVALID,AlarmStatus.DRIVER);
        }
    }        
}
