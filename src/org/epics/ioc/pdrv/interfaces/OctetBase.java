/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pdrv.interfaces;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.ioc.pdrv.*;



/**
 * Base interface for Octet.
 * It provides the following features:
 * <ul>
 *    <li>registers the interface</li>
 *    <li>Provides a default implementation of all methods.<br />
 *        In particular it calls interrupt listeners.
 *    </li>Provides method interruptOccured, which derived classes should call
 *     whenever data is written. It calls the interrupt listeners via a separate thread
 *     and with no locks held.
 *     </li>
 *</ul>
 *
 * @author mrk
 *
 */
public abstract class OctetBase implements Octet {
    private Trace asynTrace;
    private String interfaceName;
    private Port port;
    private String portName;
    
    private  ReentrantLock lock = new ReentrantLock();
    private List<OctetInterruptListener> interruptlistenerList =
        new LinkedList<OctetInterruptListener>();
    private List<OctetInterruptListener> interruptlistenerListNew = null;
    private boolean interruptActive = false;
    private boolean interruptListenerListModified = false;
    private Interrupt interrupt = new Interrupt();
    private byte[] interruptData = null;
    private int interruptNumchars;
    
    /**
     * Constructor.
     * This registers the interface with the device.
     * @param device The device
     * @param interfaceName The interface.
     */
    protected OctetBase(Device device,String interfaceName) {
        asynTrace = device.getTrace();
        port = device.getPort();
        portName = port.getPortName();
        device.registerInterface(this);
        this.interfaceName = interfaceName;
    }
    /**
     * Announce an interrupt.
     * @param data The new data.
     * @param nbytes The number of bytes.
     */
    protected void interruptOccured(byte[] data,int nbytes) {
        if(interruptActive) {
            asynTrace.print(Trace.FLOW ,
                    "%s new interrupt while interruptActive",
                    portName);
            return;
        }
        interruptData = data;
        interruptNumchars = nbytes;
        interrupt.interrupt();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.Interface#getInterfaceName()
     */
    public String getInterfaceName() {
        return interfaceName;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#write(org.epics.ioc.pdrv.User, byte[], int)
     */
    public Status write(User user,byte[] data,int nbytes) {
        return writeRaw(user,data,nbytes);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#writeRaw(org.epics.ioc.pdrv.User, byte[], int)
     */
    public abstract Status writeRaw(User user,byte[] data,int nbytes);
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#read(org.epics.ioc.pdrv.User, byte[], int)
     */
    public Status read(User user,byte[] data,int nbytes) {
        return readRaw(user,data,nbytes);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#readRaw(org.epics.ioc.pdrv.User, byte[], int)
     */
    public abstract Status readRaw(User user,byte[] data,int nbytes);
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#flush(org.epics.ioc.pdrv.User)
     */
    public abstract Status flush(User user) ;
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#setInputEos(org.epics.ioc.pdrv.User, byte[], int)
     */
    public Status setInputEos(User user,byte[] eos,int eosLen) {
        user.setMessage("inputEos not supported");
        return Status.error;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#getInputEos(org.epics.ioc.pdrv.User, byte[])
     */
    public Status getInputEos(User user,byte[] eos) {
        user.setAuxStatus(0);
        return Status.success;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#setOutputEos(org.epics.ioc.pdrv.User, byte[], int)
     */
    public Status setOutputEos(User user,byte[] eos,int eosLen) {
        user.setMessage("outputEos not supported");
        return Status.error;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#getOutputEos(org.epics.ioc.pdrv.User, byte[])
     */
    public Status getOutputEos(User user,byte[] eos) {
        user.setAuxStatus(0);
        return Status.success;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#addInterruptUser(org.epics.ioc.pdrv.User, org.epics.ioc.pdrv.interfaces.OctetInterruptListener)
     */
    public Status addInterruptUser(User user,
            OctetInterruptListener octetInterruptListener)
    {
        lock.lock();
        try {
            if(interruptActive) {
                interruptListenerListModified = true;
                if(interruptlistenerListNew==null) {
                    interruptlistenerListNew = 
                        new LinkedList<OctetInterruptListener>(interruptlistenerList);
                }
                if(interruptlistenerListNew.add(octetInterruptListener)) {
                    asynTrace.print(Trace.FLOW ,
                            "%s echoDriver.addInterruptUser while interruptActive",
                            portName);
                    return Status.success;
                }
            } else if(interruptlistenerList.add(octetInterruptListener)) {
                asynTrace.print(Trace.FLOW ,"addInterruptUser");
                return Status.success;
            }
            asynTrace.print(Trace.ERROR,"addInterruptUser but already registered");
            user.setMessage("add failed");
            return Status.error;
        } finally {
            lock.unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.pdrv.interfaces.Octet#removeInterruptUser(org.epics.ioc.pdrv.User, org.epics.ioc.pdrv.interfaces.OctetInterruptListener)
     */
    public Status removeInterruptUser(User user, OctetInterruptListener octetInterruptListener) {
        lock.lock();
        try {
            if(interruptActive) {
                interruptListenerListModified = true;
                if(interruptlistenerListNew==null) {
                    interruptlistenerListNew = 
                        new LinkedList<OctetInterruptListener>(interruptlistenerList);
                }
                if(interruptlistenerListNew.remove(octetInterruptListener)) {
                    asynTrace.print(Trace.FLOW ,"removeInterruptUser while interruptActive");
                    return Status.success;
                }
            } else if(interruptlistenerList.remove(octetInterruptListener)) {
                asynTrace.print(Trace.FLOW ,"removeInterruptUser");
                return Status.success;
            }
            asynTrace.print(Trace.ERROR,"removeInterruptUser but not registered");
            user.setMessage("remove failed");
            return Status.error;
        } finally {
            lock.unlock();
        }
    }
    
    private void callback() {
        lock.lock();
        try {
            interruptActive = true;
            asynTrace.print(Trace.FLOW ,
                "%s begin calling interruptListeners",portName);
        } finally {
            lock.unlock();
        }
        ListIterator<OctetInterruptListener> iter = interruptlistenerList.listIterator();
        while(iter.hasNext()) {
            OctetInterruptListener listener = iter.next();
            listener.interrupt(interruptData, interruptNumchars);
        }
        lock.lock();
        try {
            if(interruptListenerListModified) {
                interruptlistenerList = interruptlistenerListNew;
                interruptlistenerListNew = null;
                interruptListenerListModified = false;
            }
            interruptActive = false;
            asynTrace.print(Trace.FLOW ,"%s end calling interruptListeners",portName);
        } finally {
            lock.unlock();
        }
    } 
    
    private class Interrupt implements Runnable {
        private Thread thread = new Thread(this);
        private ReentrantLock lock = new ReentrantLock();
        private Condition moreWork = lock.newCondition();
        
        private Interrupt() {
            thread.start();
        }
        
        private void interrupt() {
            lock.lock();
            try {
                moreWork.signal();
            } finally {
                lock.unlock();
            }
        }
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                while(true) {
                    lock.lock();
                    try {
                        moreWork.await();
                        callback();
                    }finally {
                        lock.unlock();
                    }
                }
            } catch(InterruptedException e) {
                
            }
        }
    }
}
