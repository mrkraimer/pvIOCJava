/*
 * Copyright (c) 2007 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.ioc.caV3;

import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.cas.ProcessVariable;
import gov.aps.jca.cas.ProcessVariableAttachCallback;
import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.cas.ProcessVariableExistanceCallback;
import gov.aps.jca.cas.ProcessVariableExistanceCompletion;
import gov.aps.jca.cas.ProcessVariableReadCallback;
import gov.aps.jca.cas.ProcessVariableWriteCallback;
import gov.aps.jca.cas.Server;
import gov.aps.jca.cas.ServerContext;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.CTRL;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.ENUM;
import gov.aps.jca.dbr.FLOAT;
import gov.aps.jca.dbr.GR;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.LABELS;
import gov.aps.jca.dbr.PRECISION;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.TIME;

import java.net.InetSocketAddress;
import java.util.List;

import org.epics.ioc.ca.CD;
import org.epics.ioc.ca.CDFactory;
import org.epics.ioc.ca.CDField;
import org.epics.ioc.ca.CDMonitor;
import org.epics.ioc.ca.CDMonitorFactory;
import org.epics.ioc.ca.CDMonitorRequester;
import org.epics.ioc.ca.Channel;
import org.epics.ioc.ca.ChannelAccess;
import org.epics.ioc.ca.ChannelAccessFactory;
import org.epics.ioc.ca.ChannelField;
import org.epics.ioc.ca.ChannelFieldGroup;
import org.epics.ioc.ca.ChannelFieldGroupListener;
import org.epics.ioc.ca.ChannelGet;
import org.epics.ioc.ca.ChannelGetRequester;
import org.epics.ioc.ca.ChannelListener;
import org.epics.ioc.ca.ChannelPut;
import org.epics.ioc.ca.ChannelPutRequester;
import org.epics.ioc.util.RequestResult;
import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.misc.EnumeratedFactory;
import org.epics.pvData.misc.Executor;
import org.epics.pvData.misc.ExecutorFactory;
import org.epics.pvData.misc.RunnableReady;
import org.epics.pvData.misc.ThreadCreate;
import org.epics.pvData.misc.ThreadCreateFactory;
import org.epics.pvData.misc.ThreadPriority;
import org.epics.pvData.misc.ThreadReady;
import org.epics.pvData.property.AlarmSeverity;
import org.epics.pvData.property.TimeStamp;
import org.epics.pvData.property.TimeStampFactory;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.BooleanArrayData;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Type;

import com.cosylab.epics.caj.cas.handlers.AbstractCASResponseHandler;

public class ServerFactory {
    /**
     * This starts the Channel Access Server.
     */
    public static void start() {
        new ThreadInstance();
    }
    
    private static Executor executor
        = ExecutorFactory.create("caV3Monitor", ThreadPriority.low);
    private static final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    private static final Convert convert = ConvertFactory.getConvert();
    private static final ChannelAccess channelAccess = ChannelAccessFactory.getChannelAccess();
    
    private static class ThreadInstance implements RunnableReady {

        private ThreadInstance() {
            threadCreate.create("caV3Server", 3, this);
        }
        
        /**
         * JCA server context.
         */
        private ServerContext context = null;
        
        /**
         * Initialize JCA context.
         * @throws CAException  throws on any failure.
         */
        private void initialize() throws CAException {
            
            // Get the JCALibrary instance.
            JCALibrary jca = JCALibrary.getInstance();

            // Create server implmentation
            CAServerImpl server = new CAServerImpl();
            
            // Create a context with default configuration values.
            context = jca.createServerContext(JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, server);

            // Display basic information about the context.
            System.out.println(context.getVersion().getVersionString());
            context.printInfo(); System.out.println();
        }

        /**
         * Destroy JCA server  context.
         */
        private void destroy() {
            
            try {

                // Destroy the context, check if never initialized.
                if (context != null)
                    context.destroy();
                
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }               
        /* (non-Javadoc)
         * @see org.epics.ioc.util.RunnableReady#run(org.epics.ioc.util.ThreadReady)
         */
        public void run(ThreadReady threadReady) {
            try {
                // initialize context
                initialize();
                threadReady.ready();
                System.out.println("Running server...");
                // run server 
                context.run(0);
                System.out.println("Done.");
            } catch (Throwable th) {
                th.printStackTrace();
            }
            finally {
                // always finalize
                destroy();
            }
        }
    }
    
    private static class CAServerImpl implements Server {

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.Server#processVariableAttach(java.lang.String, gov.aps.jca.cas.ProcessVariableEventCallback, gov.aps.jca.cas.ProcessVariableAttachCallback)
         */
        public ProcessVariable processVariableAttach(String aliasName,
                ProcessVariableEventCallback eventCallback,
                ProcessVariableAttachCallback asyncCompletionCallback)
                throws CAStatusException, IllegalArgumentException,
                IllegalStateException {
            return new ChannelProcessVariable(aliasName, eventCallback);
        }

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.Server#processVariableExistanceTest(java.lang.String, java.net.InetSocketAddress, gov.aps.jca.cas.ProcessVariableExistanceCallback)
         */
        public ProcessVariableExistanceCompletion processVariableExistanceTest(
                String aliasName, InetSocketAddress clientAddress,
                ProcessVariableExistanceCallback asyncCompletionCallback)
        throws CAException, IllegalArgumentException, IllegalStateException {
            boolean exists = channelAccess.isChannelProvider(aliasName, "local");
            return exists ? ProcessVariableExistanceCompletion.EXISTS_HERE : ProcessVariableExistanceCompletion.DOES_NOT_EXIST_HERE;
        }
    }
    
    /**
     * Channel process variable implementation. 
     */
    private static class ChannelProcessVariable extends ProcessVariable implements ChannelListener
    {
        private static final String[] desiredPropertys = new String[] {
            "timeStamp","alarm","display","control"
        };
        private DBRType dbrType;
        private Type type;
        private ScalarType scalarType = null;
        private final Channel channel;
        private ChannelField valueChannelField = null;
        private PVField valuePVField = null;
        private PVArray valuePVArray = null;
        private Enumerated enumerated = null;
        
        private int elementCount;
        private GetRequest getRequest = null;
        private PutRequest putRequest = null;
        private MonitorRequest monitorRequest = null;;
        private final CharacteristicsGetRequest characteristicsGetRequest;
        
        private String[] enumLabels = null;
        
        /**
         * Channel PV constructor.
         * @param pvName channelName.
         * @param eventCallback event callback, can be <code>null</code>.
         */
        public ChannelProcessVariable(String pvName, ProcessVariableEventCallback eventCallback)
            throws CAStatusException, IllegalArgumentException, IllegalStateException
        {
            super(pvName, eventCallback);

            channel = channelAccess.createChannel(pvName,desiredPropertys, "local", this);
            if (channel == null)
                throw new CAStatusException(CAStatus.DEFUNCT);
            channel.connect();
            initializeChannelDBRType();
            this.eventCallback = eventCallback;

            // cache characteristics
            characteristicsGetRequest = new CharacteristicsGetRequest();
            characteristicsGetRequest.get(null);
        }
        
        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#destroy()
         */
        @Override
        public void destroy() {
            super.destroy();
            channel.destroy();
        }
        
        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#getType()
         */
        @Override
        public DBRType getType() {
            return dbrType;
        }
        
        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#getEnumLabels()
         */
        @Override
        public String[] getEnumLabels() {
            return enumLabels;
        }

        /**
         * Extract value field type and return DBR type equvivalent.
         * @return DBR type.
         * @throws CAStatusException
         */
        private void initializeChannelDBRType() throws CAStatusException {
            // find value field
            String primaryFieldName = channel.getPrimaryFieldName();
            valueChannelField = channel.createChannelField(primaryFieldName); 
            if (valueChannelField == null)
                throw new CAStatusException(CAStatus.DEFUNCT, "Failed to find field " + primaryFieldName);

            valuePVField = valueChannelField.getPVField();
            Field field = valueChannelField.getField();
            type = field.getType();
            if(type==Type.scalar) {
                Scalar scalar = (Scalar)field;
                scalarType = scalar.getScalarType();
                dbrType = getChannelDBRType(scalar.getScalarType());
                elementCount = 1;
                return;
            } else if(type==Type.scalarArray) {
                valuePVArray = (PVArray)valuePVField;
                elementCount = valuePVArray.getCapacity();
                scalarType = valuePVArray.getArray().getElementType();
                dbrType = getChannelDBRType(scalarType);
                return;
            } else if(type==Type.structure) {
                enumerated = EnumeratedFactory.getEnumerated(valuePVField);
                if (enumerated!=null)
                {
                    // this is done only once..
                    PVStringArray pvChoices = enumerated.getChoices();
                    int count = pvChoices.getLength();
                    StringArrayData data = new StringArrayData();
                    enumLabels = new String[count];
                    int num = pvChoices.get(0, count, data);
                    System.arraycopy(data.data, 0, enumLabels, 0, num);
                    dbrType = DBRType.ENUM;
                    return;
                }
            }
            throw new RuntimeException("unsupported type");
        }
        
        private static final String[] YES_NO_LABELS = new String[] { "false", "true" };

        /**
         * Convert DB type to DBR type.
         * @return DBR type.
         * @throws CAStatusException
         */
        private final DBRType getChannelDBRType(ScalarType type) {
            switch (type) {
                case pvBoolean:
                    enumLabels = YES_NO_LABELS;
                    return DBRType.ENUM;
                case pvByte:
                    return DBRType.BYTE;
                case pvShort:
                    return DBRType.SHORT;
                case pvInt:
                case pvLong:
                    return DBRType.INT;
                case pvFloat:
                    return DBRType.FLOAT;
                case pvDouble:
                    return DBRType.DOUBLE;
                case pvString:
                    return DBRType.STRING;
                default:
                    throw new RuntimeException("unsupported type");
            }
        }

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#getDimensionSize(int)
         */
        @Override
        public int getDimensionSize(int dimension) {
            return elementCount;
        }

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#getMaxDimension()
         */
        @Override
        public int getMaxDimension() {
            return elementCount > 1 ? 1 : 0;
        }
        
        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#read(gov.aps.jca.dbr.DBR, gov.aps.jca.cas.ProcessVariableReadCallback)
         */
        public CAStatus read(DBR value, ProcessVariableReadCallback asyncReadCallback) throws CAException {
            // not syned, but now it does not harm
            if (getRequest == null) getRequest = new GetRequest();
            
            characteristicsGetRequest.fill(value);
            return getRequest.get(value);
        }
        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#write(gov.aps.jca.dbr.DBR, gov.aps.jca.cas.ProcessVariableWriteCallback)
         */
        public CAStatus write(DBR value, ProcessVariableWriteCallback asyncWriteCallback) throws CAException {
            // not syned, but now it does not harm
            if (putRequest == null) putRequest = new PutRequest();

            return putRequest.put(value);
        }

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#interestDelete()
         */
        @Override
        public void interestDelete() {
            super.interestDelete();
            // stop monitoring
            monitorRequest.stop();
        }

        /* (non-Javadoc)
         * @see gov.aps.jca.cas.ProcessVariable#interestRegister()
         */
        @Override
        public void interestRegister() {
            if(monitorRequest==null) {
                monitorRequest = new MonitorRequest();
                monitorRequest.lookForChange();
            }
            super.interestRegister();
            // start monitoring
            monitorRequest.start();
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.Requester#getRequesterName()
         */
        public String getRequesterName() {
            return name;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
         */
        public void message(String message, MessageType messageType) {
            System.err.println("Message received [" + messageType + "] : " + message);
            //Thread.dumpStack();
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.ca.ChannelListener#channelStateChange(org.epics.ioc.ca.Channel, boolean)
         */
        public void channelStateChange(Channel c, boolean isConnected) {
            // TODO
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.ca.ChannelListener#disconnect(org.epics.ioc.ca.Channel)
         */
        public void destroy(Channel c) {
            // TODO
        }
        
        
        private String getOption(String option) {
            String options = channel.getOptions();
            if(options==null) return null;
            int start = options.indexOf(option);
            if(start<0) return null;
            String rest = options.substring(start + option.length());
            if(rest==null || rest.length()<1 || rest.charAt(0)!='=') {
                message("getOption bad option " + rest,MessageType.error);
                return null;
            }
            rest = rest.substring(1);
            return rest;
        }
        
        private void getValueField(DBR dbr, PVField pvField) {
            if (elementCount == 1) {
                if (dbrType == DBRType.DOUBLE) {
                    ((DOUBLE) dbr).getDoubleValue()[0] = convert.toDouble((PVScalar)pvField);
                } else if (dbrType == DBRType.INT) {
                    ((INT) dbr).getIntValue()[0] = convert.toInt((PVScalar)pvField);
                } else if (dbrType == DBRType.SHORT) {
                    ((SHORT) dbr).getShortValue()[0] = convert.toShort((PVScalar)pvField);
                } else if (dbrType == DBRType.FLOAT) {
                    ((FLOAT) dbr).getFloatValue()[0] = convert.toFloat((PVScalar)pvField);
                } else if (dbrType == DBRType.STRING) {
                    ((STRING) dbr).getStringValue()[0] = convert.getString((PVScalar)pvField);
                } else if (dbrType == DBRType.ENUM) {
                    short[] value = ((ENUM) dbr).getEnumValue();
                    if(type==Type.scalar) {
                        if(scalarType==ScalarType.pvBoolean) {
                            PVBoolean pvBoolean = (PVBoolean)pvField;
                            value[0] = (short)((pvBoolean.get()) ? 1 : 0);
                        } else {
                            valuePVField.message("illegal enum", MessageType.error);
                        }
                    } else {
                        if (enumerated != null) {
                            value[0] = (short) enumerated.getIndex().get();
                        } else {
                            valuePVField.message("illegal enum", MessageType.error);
                        }
                    }
                } else if (dbrType == DBRType.BYTE) {
                    ((BYTE) dbr).getByteValue()[0] = convert.toByte((PVScalar)pvField);
                }
            } else {
                int dbrCount = dbr.getCount();
                if (dbrType == DBRType.DOUBLE) {
                    double[] value = ((DOUBLE) dbr).getDoubleValue();
                    convert.toDoubleArray((PVArray)pvField, 0, dbrCount, value, 0);
                } else if (dbrType == DBRType.INT) {
                    int[] value = ((INT) dbr).getIntValue();
                    convert.toIntArray((PVArray)pvField, 0, dbrCount, value, 0);
                } else if (dbrType == DBRType.SHORT) {
                    short[] value = ((SHORT) dbr).getShortValue();
                    convert.toShortArray((PVArray)pvField, 0, dbrCount, value, 0);
                } else if (dbrType == DBRType.FLOAT) {
                    float[] value = ((FLOAT) dbr).getFloatValue();
                    convert.toFloatArray((PVArray)pvField, 0, dbrCount, value, 0);
                } else if (dbrType == DBRType.STRING) {
                    String[] value = ((STRING) dbr).getStringValue();
                    convert.toStringArray((PVArray) pvField, 0, dbrCount,
                            value, 0);
                } else if (dbrType == DBRType.ENUM) {
                    short[] value = ((ENUM) dbr).getEnumValue();
                    Array array = (Array)pvField.getField();
                    if(array.getElementType()==ScalarType.pvBoolean) {
                        PVBooleanArray pvBooleanArray = (PVBooleanArray)pvField;
                        BooleanArrayData data = new BooleanArrayData();
                        int count = pvBooleanArray.get(0, dbrCount, data);
                        boolean[] bools = data.data;
                        System.arraycopy(bools, 0, value, 0, count);
                    } else {
                        valuePVField.message("illegal enum", MessageType.error);
                    }
                } else if (dbrType == DBRType.BYTE) {
                    byte[] value = ((BYTE) dbr).getByteValue();
                    convert.toByteArray((PVArray)pvField, 0, dbr.getCount(), value, 0);
                }
            }
        }

        private void getTimeStampField(DBR dbr,PVStructure field) {
            TimeStamp timeStamp = TimeStampFactory.getTimeStamp(field);
           
            final long TS_EPOCH_SEC_PAST_1970=7305*86400;
            ((TIME)dbr).setTimeStamp(new gov.aps.jca.dbr.TimeStamp(timeStamp.getSecondsPastEpoch()-TS_EPOCH_SEC_PAST_1970, timeStamp.getNanoSeconds()));
        }
        
        private void getSeverityField(DBR dbr,PVField field) {
            STS sts = (STS)dbr; 
            PVInt severityField = (PVInt)field;
            AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(severityField.get());
            switch (alarmSeverity)
            {
            case none:
                sts.setSeverity(Severity.NO_ALARM);
                sts.setStatus(Status.NO_ALARM);
                break;
            case minor:
                sts.setSeverity(Severity.MINOR_ALARM);
                // for now only SOFT_ALARM
                sts.setStatus(Status.SOFT_ALARM);
                break;
            case major:
                sts.setSeverity(Severity.MAJOR_ALARM);
                // for now only SOFT_ALARM
                sts.setStatus(Status.SOFT_ALARM);
                break;
            default:
                sts.setSeverity(Severity.INVALID_ALARM);
                sts.setStatus(Status.UDF_ALARM);
            }
        }

        private class GetRequest implements ChannelGetRequester, ChannelFieldGroupListener {        
            private final ChannelFieldGroup channelFieldGroup;
            private ChannelField severityField = null;
            private ChannelField timeStampField = null;
            private final ChannelGet channelGet;
            private RequestResult result;
            private DBR dbr;

            private GetRequest() {
                channelFieldGroup = channel.createFieldGroup(this);
                channelFieldGroup.addChannelField(valueChannelField);
                severityField = valueChannelField.findProperty("alarm.severity.index");
                if (severityField != null)
                    channelFieldGroup.addChannelField(severityField);
                timeStampField = valueChannelField.findProperty("timeStamp");
                if (timeStampField != null)
                    channelFieldGroup.addChannelField(timeStampField);
                String processValue = getOption("getProcess");
                boolean process = false;
                if(processValue!=null && processValue.equals("true")) process = true;
                channelGet = channel.createChannelGet(channelFieldGroup, this, process);
            }
            
            private synchronized CAStatus get(DBR dbr) {
                result = null;
                this.dbr = dbr;
                if(severityField==null) {
                    STS sts = (STS)dbr;
                    sts.setSeverity(Severity.NO_ALARM);
                    sts.setStatus(Status.NO_ALARM);
                }
                channelGet.get();
                // if not completed wait
                if (result == null)
                {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        return CAStatus.GETFAIL;
                    }
                }           
                return result == RequestResult.success ? CAStatus.NORMAL : CAStatus.GETFAIL;
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#getDone(org.epics.ioc.util.RequestResult)
             */
            public synchronized void getDone(RequestResult requestResult) {
                result = requestResult;
                notify();
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#nextDelayedGetField(org.epics.ioc.pv.PVField)
             */
            public boolean nextDelayedGetField(PVField pvField) {
                // nothing to do
                return false;
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#nextGetField(org.epics.ioc.ca.ChannelField, org.epics.ioc.pv.PVField)
             */
            public boolean nextGetField(ChannelField channelField, PVField pvField) {
                if(channelField==valueChannelField) {
                    getValueField(dbr,pvField);
                } else if(channelField==severityField) {
                    getSeverityField(dbr,pvField);
                } else if(channelField==timeStampField) {
                    getTimeStampField(dbr,(PVStructure)pvField);
                }
                return false;
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelFieldGroupListener#accessRightsChange(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField)
             */
            public void accessRightsChange(Channel channel, ChannelField channelField) {
                // noop            
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            public String getRequesterName() {
                return name + "-" + getClass().getName();
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            public void message(String message, MessageType messageType) {
                // delegate to parent
                ChannelProcessVariable.this.message(message,messageType);
            }
            
        }

        private class PutRequest implements ChannelPutRequester,ChannelFieldGroupListener
        {
            private final ChannelFieldGroup channelFieldGroup;
            private final ChannelPut channelPut;
            private RequestResult result;       
            private DBR value2Put;
            
            private PutRequest() {
                channelFieldGroup = channel.createFieldGroup(this);
                channelFieldGroup.addChannelField(valueChannelField);
                String processValue = getOption("putProcess");
                boolean process = false;
                if(processValue!=null && processValue.equals("true")) process = true;
                channelPut = channel.createChannelPut(channelFieldGroup, this, process);
            }

            // note that this method is synced
            private synchronized CAStatus put(DBR dbr) {
                result = null;
                value2Put = dbr;
                channelPut.put();
                
                // if not completed wait
                if (result == null)
                {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        return CAStatus.PUTFAIL;
                    }
                }

                return result == RequestResult.success ? CAStatus.NORMAL : CAStatus.PUTFAIL;
            } 

            /*
             * (non-Javadoc)
             * 
             * @see org.epics.ioc.ca.ChannelPutRequester#putDone(org.epics.ioc.util.RequestResult)
             */
            public  synchronized void putDone(RequestResult requestResult) {
                result = requestResult;
                // TODO this always returns null (javaIOC bug?)
                if (result == null)
                    result = RequestResult.success;
                notify();
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelPutRequester#nextDelayedPutField(org.epics.ioc.pv.PVField)
             */
            public boolean nextDelayedPutField(PVField field) {
                return false;
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            public String getRequesterName() {
                return name + "-" + getClass().getName();
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            public void message(String message, MessageType messageType) {
                // delegate to parent
                ChannelProcessVariable.this.message(message,messageType);
            }
              
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelPutRequester#nextPutField(org.epics.ioc.ca.ChannelField, org.epics.ioc.pv.PVField)
             */
            public boolean nextPutField(ChannelField channelField, PVField pvField) {
                if (channelField == valueChannelField) {
                    final DBR dbr = value2Put;
                    if (elementCount == 1) {
                        if (dbrType == DBRType.DOUBLE) {
                            double[] value = ((DOUBLE) dbr).getDoubleValue();
                            convert.fromDouble((PVScalar)pvField, value[0]);
                        } else if (dbrType == DBRType.INT) {
                            int[] value = ((INT) dbr).getIntValue();
                            convert.fromInt((PVScalar)pvField, value[0]);
                        } else if (dbrType == DBRType.SHORT) {
                            short[] value = ((SHORT) dbr).getShortValue();
                            convert.fromShort((PVScalar)pvField, value[0]);
                        } else if (dbrType == DBRType.FLOAT) {
                            float[] value = ((FLOAT) dbr).getFloatValue();
                            convert.fromFloat((PVScalar)pvField, value[0]);
                        } else if (dbrType == DBRType.STRING) {
                            String[] value = ((STRING) dbr).getStringValue();
                            convert.fromString((PVScalar)pvField, value[0]);
                        } else if (dbrType == DBRType.ENUM) {
                            short[] value = ((ENUM) dbr).getEnumValue();
                            if(pvField.getField().getType()==Type.scalar) {
                                PVScalar pvScalar = (PVScalar)pvField;
                                if(pvScalar.getScalar().getScalarType()==ScalarType.pvBoolean) {
                            
                                PVBoolean pvBoolean = (PVBoolean)pvField;
                                pvBoolean.put((value[0]==0) ? false : true);
                                } else {
                                    valuePVField.message("illegal enum", MessageType.error);
                                }
                            } else {                               
                               
                                if (enumerated != null)  {
                                    PVInt pvInt = enumerated.getIndex();
                                    pvInt.put(value[0]);
                                } else {
                                    valuePVField.message("illegal enum",MessageType.error);
                                }
                            }
                        } else if (dbrType == DBRType.BYTE) {
                            byte[] value = ((BYTE) dbr).getByteValue();
                            convert.fromInt((PVScalar)pvField, value[0]);
                        }
                    } else {
                        int dbrCount = dbr.getCount();
                        if (dbrType == DBRType.DOUBLE) {
                            double[] value = ((DOUBLE) dbr).getDoubleValue();
                            convert.fromDoubleArray((PVArray)pvField, 0, dbrCount,
                                    value, 0);
                        } else if (dbrType == DBRType.INT) {
                            int[] value = ((INT) dbr).getIntValue();
                            convert.fromIntArray((PVArray)pvField, 0, dbrCount, value,
                                            0);
                        } else if (dbrType == DBRType.SHORT) {
                            short[] value = ((SHORT) dbr).getShortValue();
                            convert.fromShortArray((PVArray)pvField, 0, dbrCount, value,
                                    0);
                        } else if (dbrType == DBRType.FLOAT) {
                            float[] value = ((FLOAT) dbr).getFloatValue();
                            convert.fromFloatArray((PVArray)pvField, 0, dbrCount, value,
                                    0);
                        } else if (dbrType == DBRType.STRING) {
                            String[] values = ((STRING) dbr).getStringValue();
                            convert.fromStringArray((PVArray) pvField, 0, dbr
                                    .getCount(), values, 0);
                        } else if (dbrType == DBRType.ENUM) {
                            short[] value = ((ENUM) dbr).getEnumValue();
                            Array array = (Array)pvField.getField();
                            if(array.getElementType()==ScalarType.pvBoolean) {
                                PVBooleanArray pvBooleanArray = (PVBooleanArray)pvField;
                                boolean[] bools = new boolean[dbrCount];
                                for(int i=0; i<dbrCount; i++) {
                                    bools[i] = (value[i]==0) ? false : true;
                                }
                                pvBooleanArray.put(0, dbrCount, bools, 0);
                            } else {
                                valuePVField.message("illegal enum", MessageType.error);
                            }
                        } else if (dbrType == DBRType.BYTE) {
                            byte[] value = ((BYTE) dbr).getByteValue();
                            convert.fromByteArray((PVArray)pvField, 0, dbrCount, value,
                                    0);
                        }
                    }
                }
                return false;
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelFieldGroupListener#accessRightsChange(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField)
             */
            public void accessRightsChange(Channel channel, ChannelField channelField) {
                // noop
            }
        }

        private class MonitorRequest implements CDMonitorRequester, ChannelFieldGroupListener {
            private ChannelField severityField;
            private ChannelField timeStampField;
            private final CDMonitor cdMonitor;
            
            public MonitorRequest() {
                severityField = valueChannelField.findProperty("alarm.severity.index");           
                timeStampField = valueChannelField.findProperty("timeStamp");
                cdMonitor = CDMonitorFactory.create(channel, this);
            }
            
            private void lookForChange() {
                if(scalarType!=null && scalarType.isNumeric()) {
                    cdMonitor.lookForChange(valueChannelField, true);
                } else {
                    cdMonitor.lookForPut(valueChannelField, true);
                }
                if (severityField != null) cdMonitor.lookForPut(severityField, true);
                if (timeStampField != null) cdMonitor.lookForPut(timeStampField, false);
            }
            
            private void start() {
                cdMonitor.start(3,executor);
            }
            private void stop() {
                cdMonitor.stop();
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelMonitorRequester#dataOverrun(int)
             */
            public void dataOverrun(int number) {
                // noop
                
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelMonitorRequester#monitorCD(org.epics.ioc.ca.CD)
             */
            public void monitorCD(CD cd) {
                // TODO appropriate ChannelMonitorRequester mask (VALUE, LOG, ALARM)
                DBR dbr = AbstractCASResponseHandler.createDBRforReading(ChannelProcessVariable.this);
                if(severityField==null) {
                    STS sts = (STS)dbr;
                    sts.setSeverity(Severity.NO_ALARM);
                    sts.setStatus(Status.NO_ALARM);
                }
                final CDField[] fields = cd.getCDRecord().getCDStructure().getCDFields();
                ChannelFieldGroup channelFieldGroup = cd.getChannelFieldGroup();
                final List<ChannelField> channelFieldList = channelFieldGroup.getList();
                for (int i = 0; i < fields.length; i++)
                {
                    final ChannelField channelField = channelFieldList.get(i);
                    final PVField field = fields[i].getPVField(); 
                    if (channelField == valueChannelField) {
                        getValueField(dbr,field);
                    } else if (channelField == timeStampField){
                        getTimeStampField(dbr,(PVStructure)field);
                    } else if (channelField == severityField){
                        getSeverityField(dbr,field);
                    }
                }
                characteristicsGetRequest.fill(dbr);
                eventCallback.postEvent(Monitor.VALUE|Monitor.LOG, dbr);
            }
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            public String getRequesterName() {
                return name + "-" + getClass().getName();
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            public void message(String message, MessageType messageType) {
                ChannelProcessVariable.this.message(message,messageType);
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelFieldGroupListener#accessRightsChange(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField)
             */
            public void accessRightsChange(Channel channel, ChannelField channelField) {
                // TODO noop
                
            }

        }
           
        private class CharacteristicsGetRequest implements ChannelGetRequester,ChannelFieldGroupListener
        {
            private final ChannelGet channelGet;
            private final CharacteristicsData characteristicsData;
            private RequestResult result;

            private CharacteristicsGetRequest() {
                characteristicsData = new CharacteristicsData();
                // TODO revise process flags?!
                channelGet = channel.createChannelGet(characteristicsData.getChannelFieldGroup(), this, false);

            }

            private synchronized CAStatus get(DBR dbr) {
                // reset
                result = null;
                characteristicsData.clear();
                channelGet.get();

                // if not completed wait
                if (result == null)
                {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        return CAStatus.GETFAIL;
                    }
                }

                characteristicsData.fill(dbr);

                return result == RequestResult.success ? CAStatus.NORMAL : CAStatus.GETFAIL;
            }

            public synchronized void fill(DBR dbr)
            {
                characteristicsData.fill(dbr);
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#nextDelayedGetData(org.epics.ioc.pvAccess.PVData)
             */
            public boolean nextDelayedGetField(PVField pvField) {
                return false;
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#getRequesterName()
             */
            public String getRequesterName() {
                return name + "-" + getClass().getName();
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
             */
            public void message(String message, MessageType messageType) {
                // delegate to parent
                ChannelProcessVariable.this.message(message,messageType);
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#nextGetData(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField, org.epics.ioc.pvAccess.PVData)
             */
            public boolean nextGetField(ChannelField channelField, PVField pvField) {
                characteristicsData.nextGetField(channel, channelField, pvField);
                return false;
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelGetRequester#getDone(org.epics.ioc.util.RequestResult)
             */
            public synchronized void getDone(RequestResult requestResult) {
                result = requestResult;
                notify();
            }
            
            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelFieldGroupListener#accessRightsChange(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField)
             */
            public void accessRightsChange(Channel channel, ChannelField channelField) {
                // noop
                
            }
        }

        private class CharacteristicsData implements ChannelFieldGroupListener {
            private CD channelData;
            private ChannelFieldGroup channelFieldGroup;
            private ChannelField displayField;
            private ChannelField controlLimitField;

            private CharacteristicsData() {
                init();
            }

            private ChannelFieldGroup getChannelFieldGroup()
            {
                return channelFieldGroup;
            }

            /* (non-Javadoc)
             * @see org.epics.ioc.ca.ChannelFieldGroupListener#accessRightsChange(org.epics.ioc.ca.Channel, org.epics.ioc.ca.ChannelField)
             */
            public void accessRightsChange(Channel channel, ChannelField channelField) {
                // noop
            }

            private void init() {
                channelFieldGroup = channel.createFieldGroup(this);

                // add display structure field
                displayField = valueChannelField.findProperty("display");
                if (displayField != null)
                    channelFieldGroup.addChannelField(displayField);

                // add control limit structure field
                controlLimitField = valueChannelField.findProperty("control.limit");
                if (controlLimitField != null)
                    channelFieldGroup.addChannelField(controlLimitField);

                // create CD
                channelData = CDFactory.createCD(channel, channelFieldGroup);
                if (channelData == null)
                    throw new RuntimeException("CDFactory.createData failed");
            }
            
            private void clear() {
                channelData.clearNumPuts();
            }
            private boolean nextGetField(Channel channel, ChannelField channelField, PVField pvField) {
                channelData.put(channelField,pvField);
                return false;
            }

            public void fill(DBR dbr)
            {
                fill(dbr, channelData);
            }

            private void fill(DBR dbr, CD channelData)
            {
                // labels
                if (dbr instanceof LABELS)
                    ((LABELS)dbr).setLabels(enumLabels);

                // optimisation
                if (!(dbr instanceof GR))
                    return;

                final CDField[] fields = channelData.getCDRecord().getCDStructure().getCDFields();
                final List<ChannelField> channelFieldList = channelFieldGroup.getList();
                for (int i = 0; i < fields.length; i++)
                {
                    final ChannelField channelField = channelFieldList.get(i);
                    final PVField field = fields[i].getPVField(); 
                    if (channelField == displayField && dbr instanceof GR)
                    {
                        final GR gr = (GR)dbr;
                        PVStructure displayStructure = (PVStructure)field;

                        PVString unitsField = displayStructure.getStringField("units");
                        gr.setUnits(unitsField.get());

                        if (dbr instanceof PRECISION)
                        {
                            // default;
                            short precision = (short)6;
                            PVInt pvInt = displayStructure.getIntField("resolution");
                            if(pvInt!=null) {
                                precision = (short)pvInt.get();
                            }   
                            // set precision
                            ((PRECISION)dbr).setPrecision(precision);
                        }

                        // all done via super-set double
                        PVDouble lowField = displayStructure.getDoubleField("limit.low");
                        gr.setLowerDispLimit(lowField.get());

                        PVDouble highField = displayStructure.getDoubleField("limit.high");
                        gr.setUpperDispLimit(highField.get());

                        // TODO alarm limits (there is not way to get it)
                    }
                    else if (channelField == controlLimitField && dbr instanceof CTRL)
                    {
                        final CTRL ctrl = (CTRL)dbr;
                        PVStructure controlLimitStructure = (PVStructure)field;

                        // all done via double as super-set type
                        PVDouble lowField = controlLimitStructure.getDoubleField("low");
                        ctrl.setLowerCtrlLimit(lowField.get());

                        PVDouble highField = controlLimitStructure.getDoubleField("high");
                        ctrl.setUpperCtrlLimit(highField.get());
                    }
                }
            }
        }
    }
}
