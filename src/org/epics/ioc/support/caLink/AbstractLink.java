/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support.caLink;

import org.epics.ioc.channelAccess.*;
import org.epics.pvData.channelAccess.*;

import org.epics.ioc.install.*;
import org.epics.ioc.support.AbstractSupport;
import org.epics.ioc.support.RecordProcess;
import org.epics.ioc.support.SupportState;
import org.epics.ioc.support.alarm.AlarmSupport;
import org.epics.ioc.support.alarm.AlarmSupportFactory;
import org.epics.pvData.pv.*;
import org.epics.pvData.factory.*;
import org.epics.pvData.misc.*;

/**
 * Abstract Support for Channel Access Link.
 * This is not public since it is for use by this package.
 * @author mrk
 *
 */
abstract class AbstractLink extends AbstractSupport implements AfterStartRequester,ChannelFindRequester,ChannelRequester {
    /**
     * pvDataCreate is for creating PV data.
     */
    protected static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    /**
     * The pvStructure that this link supports.
     */
    protected PVStructure pvStructure;
    /**
     * The pvRecord that this link supports.
     */
    protected PVRecord pvRecord;
    /**
     * The channelRequesterName.
     */
    protected String channelRequesterName;
    /**
     * The recordProcess for this record.
     */
    protected RecordProcess recordProcess = null;
    /**
     * The alarmSupport.
     */
    protected AlarmSupport alarmSupport = null;
    /**
     * The name of the channel provider.
     */
    protected PVString providerPV = null;
    /**
     * The interface for getting the pvName.
     */
    protected PVString pvnamePV = null;
    
    /**
     * The channel to which this link is connected.
     */
    protected Channel channel = null;
    protected static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    protected ChannelProvider channelProvider = null;
    
    private AfterStartNode afterStartNode = AfterStartFactory.allocNode(this);
    private AfterStart afterStart = null;
    
    
    /**
     * Constructor.
     * @param supportName The name of the support.
     * @param pvField The field which is supported.
     */
    protected AbstractLink(
        String supportName,PVField pvField)
    {
        super(supportName,pvField);
        this.pvStructure = pvField.getParent();
        pvRecord = pvStructure.getPVRecord();
        channelRequesterName = pvField.getFullName();
    }
    /**
     * Must be implemented by derived class and is called by this class.
     * @param isConnected is connected?
     */
    abstract void connectionChange(boolean isConnected);
    /* (non-Javadoc)
     * @see org.epics.ioc.support.AbstractSupport#initialize(org.epics.ioc.support.RecordSupport)
     */
    public void initialize(LocateSupport recordSupport) {
        if(!super.checkSupportState(SupportState.readyForInitialize,null)) return;
        recordProcess = recordSupport.getRecordProcess();
        PVField pvAlarm = pvStructure.getSubField("alarm");
        if(pvAlarm==null) {
            pvStructure.message("alarm not found", MessageType.error);
            return;
        }
        alarmSupport = AlarmSupportFactory.getAlarmSupport(pvAlarm,recordSupport);
        if(alarmSupport==null) {
            pvStructure.message("alarm does not have alarmSupport", MessageType.error);
            return;
        }
        providerPV = pvStructure.getStringField("providerName");
        if(providerPV==null) return;
        pvnamePV = pvStructure.getStringField("pvname");
        if(pvnamePV==null) return;
        super.initialize(recordSupport);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.support.AbstractSupport#start()
     */
    public void start(AfterStart afterStart) {
        if(!super.checkSupportState(SupportState.readyForStart,null)) return;
        String providerName = providerPV.get();
        ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
        channelProvider = channelAccess.getProvider(providerName);
        if(channelProvider==null) {
            message("providerName " + providerName +  " not found",MessageType.error);
            return;
        }
        super.start(afterStart);
        this.afterStart = afterStart;
        afterStart.requestCallback(afterStartNode, true, ThreadPriority.low);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.install.AfterStartRequester#callback(org.epics.ioc.install.AfterStartNode)
     */
    @Override
    public void callback(AfterStartNode node) {
        channelProvider.channelFind(pvnamePV.get(), this);
        afterStart.done(afterStartNode);
        afterStart = null;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.channelAccess.ChannelFindRequester#channelFindResult(org.epics.ioc.channelAccess.ChannelFind, boolean)
     */
    @Override
    public void channelFindResult(ChannelFind channelFind, boolean wasFound) {
        if(!wasFound) {
            message("pvname " + pvnamePV.get() +  " not found",MessageType.error);
            return;
        }
        channelProvider.createChannel(pvnamePV.get(), this);
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.channelAccess.ChannelRequester#channelCreated(org.epics.pvData.channelAccess.Channel)
     */
    @Override
    public void channelCreated(Channel channel) {
        this.channel = channel;
        channel.connect();
    }

    /* (non-Javadoc)
     * @see org.epics.pvData.channelAccess.ChannelRequester#channelNotCreated()
     */
    @Override
    public void channelNotCreated() {
        message("pvname " + pvnamePV.get() +  " not created",MessageType.error);
        return;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.support.AbstractSupport#stop()
     */
    public void stop() {
        if(channel!=null) channel.destroy();
        channel = null;
        super.stop();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.support.AbstractSupport#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    public void message(String message,MessageType messageType) {
        pvRecord.lock();
        try {
            pvStructure.message(message, messageType);
        } finally {
            pvRecord.unlock();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.channelAccess.ChannelRequester#destroy(org.epics.pvData.channelAccess.Channel)
     */
    public void destroy(Channel c) {
        pvRecord.lock();
        try {
            if(super.getSupportState()!=SupportState.ready) return;
        } finally {
            pvRecord.unlock();
        }
        recordProcess.stop();
        recordProcess.start(null);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.channelAccess.ChannelRequester#channelStateChange(org.epics.pvData.channelAccess.Channel, boolean)
     */
    public void channelStateChange(Channel c, boolean isConnected) {
        connectionChange(isConnected);
    }
}