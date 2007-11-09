/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pdrv.interfaces;

import org.epics.ioc.pdrv.*;
import org.epics.ioc.pv.*;
import org.epics.ioc.util.AlarmSeverity;

/**
 * Abstract base class for array interfaces.
 * @author mrk
 *
 */
public abstract class AbstractArrayInterface extends AbstractPVArray implements Interface {
    protected Device device;
    protected String interfaceName; 
    protected Trace trace;
    
	/**
	 * Constructor.
	 * @param device The device
	 * @param interfaceName The interfaceName.
	 */
	protected AbstractArrayInterface(
			PVField parent,Array array,int capacity,boolean capacityMutable,
            Device device,String interfaceName)
    {
		super(parent,array,capacity,capacityMutable);
		this.device = device;
		this.interfaceName = interfaceName;
		trace = device.getTrace();
		device.registerInterface(this);
	}
	/**
     * Generate a trace message.
     * @param reason One of ERROR|SUPPORT|INTERPOSE|DRIVER|FLOW.
     * @param message The message to print
     */
    protected void print(int reason,String message) {
    	if((reason&trace.getMask())==0) return;
        trace.print(reason,
        	"port " + device.getPort().getPortName()
        	+ ":" + device.getAddr() + " "+ message);
    }
    /**
     * Generate a trace message.
     * @param reason One of ERROR|SUPPORT|INTERPOSE|DRIVER|FLOW.
     * @param format A format.
     * @param args The data associated with the format.
     */
    protected void print(int reason,String format, Object... args) {
    	if((reason&trace.getMask())==0) return;
    	trace.print(reason,format,
            	"port " + device.getPort().getPortName()
            	+ ":" + device.getAddr() + " " + args);
    }
	/* (non-Javadoc)
	 * @see org.epics.ioc.pdrv.interfaces.Interface#getDevice()
	 */
	public Device getDevice() {
		return device;
	}
	/* (non-Javadoc)
	 * @see org.epics.ioc.pdrv.interfaces.Interface#getAlarmMessage()
	 */
	public String getAlarmMessage() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.epics.ioc.pdrv.interfaces.Interface#getAlarmSeverity()
	 */
	public AlarmSeverity getAlarmSeverity() {
		return AlarmSeverity.none;
	}
	/* (non-Javadoc)
	 * @see org.epics.ioc.pdrv.interfaces.Interface#getInterfaceName()
	 */
	public String getInterfaceName() {
		return interfaceName;
	}
}
