/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.caV3;


import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Enum;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_CTRL_String;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_GR_Byte;
import gov.aps.jca.dbr.DBR_GR_Double;
import gov.aps.jca.dbr.DBR_GR_Float;
import gov.aps.jca.dbr.DBR_GR_Int;
import gov.aps.jca.dbr.DBR_GR_Short;
import gov.aps.jca.dbr.DBR_GR_String;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_STS_Byte;
import gov.aps.jca.dbr.DBR_STS_Double;
import gov.aps.jca.dbr.DBR_STS_Enum;
import gov.aps.jca.dbr.DBR_STS_Float;
import gov.aps.jca.dbr.DBR_STS_Int;
import gov.aps.jca.dbr.DBR_STS_Short;
import gov.aps.jca.dbr.DBR_STS_String;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.DBR_TIME_Byte;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.DBR_TIME_Enum;
import gov.aps.jca.dbr.DBR_TIME_Float;
import gov.aps.jca.dbr.DBR_TIME_Int;
import gov.aps.jca.dbr.DBR_TIME_Short;
import gov.aps.jca.dbr.DBR_TIME_String;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.misc.EnumeratedFactory;
import org.epics.pvData.property.AlarmSeverity;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Type;

/**
 * @author mrk
 *
 */
public class BaseV3ChannelStructure implements V3ChannelStructure {
    private static final Convert convert = ConvertFactory.getConvert();
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final PVDatabase masterPVDatabase = PVDatabaseFactory.getMaster();
    private static enum DBRProperty {none,status,time,graphic,control};
    private static final Pattern commaPattern = Pattern.compile("[,]");
    
    private V3Channel v3Channel;
    
    private DBRType nativeDBRType = null;
    private DBRType requestDBRType = null;
    private PVStructure pvStructure = null;
    private BitSet bitSet = null;
    private PVStructure pvAlarm = null;
    private PVString pvAlarmMessage = null;
    private PVInt pvAlarmIndex = null;
    private String[] alarmChoices = null;
    private PVStructure pvTimeStamp = null;
    private PVLong pvSeconds = null;
    private PVInt pvNanoSeconds = null;
    private PVScalar pvScalarValue = null;
    private PVArray pvArrayValue = null;
    // Following not null if nativeDBRType.isENUM(
    private Enumerated pvEnumerated = null;
    
    private boolean firstGetPVStructure = true;
    
    /**
     * The Constructor
     * @param v3Channel The v3Channel.
     */
    public BaseV3ChannelStructure(V3Channel v3Channel) {
        this.v3Channel = v3Channel;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#createPVStructure(org.epics.ioc.caV3.V3ChannelStructureRequester, org.epics.pvData.pv.PVStructure, boolean)
     */
    @Override
    public PVStructure createPVStructure(PVStructure pvRequest,boolean propertiesAllowed)
    {
        List<String> propertyList = new ArrayList<String>();
        gov.aps.jca.Channel jcaChannel = v3Channel.getJCAChannel();
        int elementCount = jcaChannel.getElementCount();
        nativeDBRType = jcaChannel.getFieldType();
        PVField[] pvFields = null;
        PVField pvf = pvRequest.getSubField("field");
        if(pvf!=null && pvf.getField().getType()==Type.structure) {
            PVStructure pvStruct = pvRequest.getStructureField("field");
            pvFields = pvStruct.getPVFields();
        } else {
        	pvFields = pvRequest.getPVFields();
        } 
        boolean valueIsIndex = false;
        boolean valueIsChoice = false;
        if(pvFields.length==0 && propertiesAllowed) {
            propertyList.add("timeStamp");
            propertyList.add("alarm");
            if(nativeDBRType!=DBRType.ENUM) {
                propertyList.add("display");
                propertyList.add("control");
            }
        }
        for(int indField = 0; indField<pvFields.length; indField++) {
            PVField pvField = pvFields[indField];
            Field field = pvField.getField();
            if(field.getType()!=Type.scalar) return null;
            Scalar scalar = (Scalar)field;
            if(scalar.getScalarType()!=ScalarType.pvString) return null;
            PVString pvString = (PVString)pvField;
            String fieldName = field.getFieldName();
            if(fieldName.equals("fieldList")) {
                String fieldList = pvString.get();
                String[] fields = commaPattern.split(fieldList);
                for(int i=0; i<fields.length; i++) {
                	String val = fields[i];
                	if(val.equals("value.index")) {
                        valueIsIndex = true;
                    } else if(val.equals("value.choice")) {
                        valueIsChoice = true;
                    } else if(!fields[i].equals("value")) {
                        propertyList.add(fields[i]);
                    }
                }
            } else if(fieldName.equals("value")) {
                String val = pvString.get();
                if(val.equals("value.index")) {
                    valueIsIndex = true;
                } else if(val.equals("value.choice")) {
                    valueIsChoice = true;
                }
            } else if(propertiesAllowed) {
                String val = pvString.get();
                if(val.equals(fieldName)) {
                    if(val.equals("timeStamp")) {
                        propertyList.add(val);
                    } else if(val.equals("alarm")) {
                        propertyList.add(val);
                    } else if(nativeDBRType==DBRType.ENUM) {
                        // do not add
                    } else if(val.equals("display")) {
                        propertyList.add(val);
                    } else if(val.equals("control")) {
                        propertyList.add(val);
                    }
                }
            }
        }
        if(nativeDBRType.isENUM()) {
            if(valueIsIndex) {
                nativeDBRType = DBRType.INT;
            } else if(valueIsChoice) {
                nativeDBRType = DBRType.STRING;
            }
        }
        String[] propertyNames = new String[propertyList.size()];
        for(int i=0; i<propertyNames.length; i++) propertyNames[i] = propertyList.get(i);
        propertyList = null;
        DBRProperty dbrProperty = DBRProperty.none;
        if(propertyNames.length>0) {
            for(String propertyName : propertyNames) {
                if(propertyName.equals("alarm")&& (dbrProperty.compareTo(DBRProperty.status)<0)) {
                    dbrProperty = DBRProperty.status;
                    continue;
                }
                if(propertyName.equals("timeStamp")&& (dbrProperty.compareTo(DBRProperty.time)<0)) {
                    dbrProperty = DBRProperty.time;
                    continue;
                }
                if(propertyName.equals("display")&& (dbrProperty.compareTo(DBRProperty.graphic)<0)) {
                    dbrProperty = DBRProperty.graphic;
                    continue;
                }
                if(propertyName.equals("control")&& (dbrProperty.compareTo(DBRProperty.control)<0)) {
                    dbrProperty = DBRProperty.control;
                    continue;
                }
            }
        }
        Type type = Type.scalar;
        ScalarType scalarType = null;
        if(nativeDBRType.isBYTE()) {
            scalarType = ScalarType.pvByte;
        } else if(nativeDBRType.isSHORT()) {
            scalarType= ScalarType.pvShort;
        } else if(nativeDBRType.isINT()) {
            scalarType = ScalarType.pvInt;
        } else if(nativeDBRType.isFLOAT()) {
            scalarType = ScalarType.pvFloat;
        } else if(nativeDBRType.isDOUBLE()) {
            scalarType = ScalarType.pvDouble;
        } else if(nativeDBRType.isSTRING()) {
            scalarType = ScalarType.pvString;
        } else if(nativeDBRType.isENUM()) {
            type = Type.structure;
        }
        Field valueField = null;
        if(type==Type.structure) {
            PVStructure pvEnumerated = masterPVDatabase.findStructure("org.epics.pvData.enumerated");
            if(pvEnumerated==null) {
                v3Channel.message("could not find structure enumerated ", MessageType.error);
                return null;
            }
            Field[] valueFields = pvEnumerated.getStructure().getFields();
            valueField = fieldCreate.createStructure("value", valueFields);
        } else if(elementCount<2) {
            valueField = fieldCreate.createScalar("value", scalarType);
        } else {
            valueField = fieldCreate.createArray("value", scalarType);
        }
        Field[] fields = new Field[propertyNames.length + 1];
        fields[0] = valueField;
        int index = 1;
        for(String propertyName : propertyNames) {
            PVStructure pvStructure = masterPVDatabase.findStructure(propertyName);
            if(pvStructure==null) pvStructure = masterPVDatabase.findStructure("org.epics.ioc." + propertyName);
            if(pvStructure==null) pvStructure = masterPVDatabase.findStructure("org.epics.pvData." + propertyName);
            if(pvStructure==null) {
                v3Channel.message("could not find structure for " + propertyName, MessageType.error);
                return null;
            }
            Field[] propertyFields = pvStructure.getStructure().getFields();
            fields[index++] = fieldCreate.createStructure(propertyName, propertyFields);
        }
        pvStructure = pvDataCreate.createPVStructure(null,"", fields);
        if(nativeDBRType.isENUM()) {
            PVStructure pvStruct = (PVStructure)pvStructure.getPVFields()[0];
            EnumeratedFactory.replacePVField(pvStruct);
            pvStruct = (PVStructure)pvStructure.getPVFields()[0];
            pvEnumerated = EnumeratedFactory.getEnumerated(pvStruct);
        }
        for(PVField pvField : pvStructure.getPVFields()) {
            if(pvField.getField().getFieldName().equals("alarm")) {
                PVStructure pvTemp = masterPVDatabase.findStructure("org.epics.pvData.alarm");
                PVStructure pvReplace = pvDataCreate.createPVStructure(pvField.getParent(), pvField.getField().getFieldName(),pvTemp);
                pvField.replacePVField(pvReplace);
                pvAlarm = pvReplace;
                PVField[] pvAlarmFields = pvAlarm.getPVFields();
                for(PVField alarmField : pvAlarmFields) {
                    String name = alarmField.getField().getFieldName();
                    if(name.equals("severity" ) || name.equals("message")) continue;
                    pvAlarm.removePVField(name);
                }
                PVField pvFromChoices = pvTemp.getSubField("severity.choices");
                PVField pvToChoices = pvAlarm.getSubField("severity.choices");
                convert.copy(pvFromChoices, pvToChoices);
                pvAlarmMessage = pvAlarm.getStringField("message");
                PVStructure pvSeverity = pvAlarm.getStructureField("severity");
                pvAlarmIndex = pvSeverity.getIntField("index");
                PVStringArray pvAlarmChoices = (PVStringArray)pvSeverity.getArrayField("choices", ScalarType.pvString);
                StringArrayData stringArrayData = new StringArrayData();
                pvAlarmChoices.get(0, pvAlarmChoices.getLength(), stringArrayData);
                alarmChoices = stringArrayData.data;
            }
            if(pvField.getField().getFieldName().equals("timeStamp")) {
                pvTimeStamp = (PVStructure)pvField;
                pvSeconds = pvTimeStamp.getLongField("secondsPastEpoch");
                pvNanoSeconds = pvTimeStamp.getIntField("nanoSeconds");
                if(pvSeconds==null || pvNanoSeconds==null) {
                    throw new RuntimeException("bad timeStamp structure");
                }
            }
        }
        bitSet = new BitSet(pvStructure.getNumberFields());
        PVField pvValue = pvStructure.getPVFields()[0];
        if(nativeDBRType.isENUM()) {
            requestDBRType = DBRType.CTRL_ENUM;
            return pvStructure;
        }
        if(elementCount<2) {
            pvScalarValue = (PVScalar)pvValue;
        } else {
            pvArrayValue = (PVArray)pvValue;
        }
        requestDBRType = null;
        switch(dbrProperty) {
        case none:
            requestDBRType = nativeDBRType;
            break;
        case status:
            if(nativeDBRType==DBRType.BYTE) {
                requestDBRType = DBRType.STS_BYTE;
            } else if(nativeDBRType==DBRType.SHORT) {
                requestDBRType = DBRType.STS_SHORT;
            } else if(nativeDBRType==DBRType.INT) {
                requestDBRType = DBRType.STS_INT;
            } else if(nativeDBRType==DBRType.FLOAT) {
                requestDBRType = DBRType.STS_FLOAT;
            } else if(nativeDBRType==DBRType.DOUBLE) {
                requestDBRType = DBRType.STS_DOUBLE;
            } else if(nativeDBRType==DBRType.STRING) {
                requestDBRType = DBRType.STS_STRING;
            } else if(nativeDBRType==DBRType.ENUM) {
                requestDBRType = DBRType.STS_ENUM;
            }
            break;
        case time:
            if(nativeDBRType==DBRType.BYTE) {
                requestDBRType = DBRType.TIME_BYTE;
            } else if(nativeDBRType==DBRType.SHORT) {
                requestDBRType = DBRType.TIME_SHORT;
            } else if(nativeDBRType==DBRType.INT) {
                requestDBRType = DBRType.TIME_INT;
            } else if(nativeDBRType==DBRType.FLOAT) {
                requestDBRType = DBRType.TIME_FLOAT;
            } else if(nativeDBRType==DBRType.DOUBLE) {
                requestDBRType = DBRType.TIME_DOUBLE;
            } else if(nativeDBRType==DBRType.STRING) {
                requestDBRType = DBRType.TIME_STRING;
            } else if(nativeDBRType==DBRType.ENUM) {
                requestDBRType = DBRType.TIME_ENUM;
            }
            break;
        case graphic:
            if(nativeDBRType==DBRType.BYTE) {
                requestDBRType = DBRType.GR_BYTE;
            } else if(nativeDBRType==DBRType.SHORT) {
                requestDBRType = DBRType.GR_SHORT;
            } else if(nativeDBRType==DBRType.INT) {
                requestDBRType = DBRType.GR_INT;
            } else if(nativeDBRType==DBRType.FLOAT) {
                requestDBRType = DBRType.GR_FLOAT;
            } else if(nativeDBRType==DBRType.DOUBLE) {
                requestDBRType = DBRType.GR_DOUBLE;
            } else if(nativeDBRType==DBRType.STRING) {
                requestDBRType = DBRType.GR_STRING;
            } else if(nativeDBRType==DBRType.ENUM) {
                requestDBRType = DBRType.CTRL_ENUM;
            }
            break;
        case control:
            if(nativeDBRType==DBRType.BYTE) {
                requestDBRType = DBRType.CTRL_BYTE;
            } else if(nativeDBRType==DBRType.SHORT) {
                requestDBRType = DBRType.CTRL_SHORT;
            } else if(nativeDBRType==DBRType.INT) {
                requestDBRType = DBRType.CTRL_INT;
            } else if(nativeDBRType==DBRType.FLOAT) {
                requestDBRType = DBRType.CTRL_FLOAT;
            } else if(nativeDBRType==DBRType.DOUBLE) {
                requestDBRType = DBRType.CTRL_DOUBLE;
            } else if(nativeDBRType==DBRType.STRING) {
                requestDBRType = DBRType.CTRL_STRING;
            } else if(nativeDBRType==DBRType.ENUM) {
                requestDBRType = DBRType.CTRL_ENUM;
            }
            break;
        }
        return pvStructure;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getPVStructure()
     */
    @Override
    public PVStructure getPVStructure() {
        return pvStructure;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getBitSet()
     */
    @Override
    public BitSet getBitSet() {
        return bitSet;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelRecord#getValueDBRType()
     */
    public DBRType getNativeDBRType() {
        return nativeDBRType;
    }
    
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#getRequestDBRType()
     */
    @Override
    public DBRType getRequestDBRType() {
        return requestDBRType;
    }
    private void setAlarm(AlarmSeverity alarmSeverity,String message) {
        int index = alarmSeverity.ordinal();
        if(pvAlarm!=null && index<alarmChoices.length) {
            String oldMessage = pvAlarmMessage.get();
            if(oldMessage!=message || (message!=null &&(!message.equals(oldMessage)))) {
                pvAlarmMessage.put(message);
            }
            if(pvAlarmIndex.get()!=index) {
                pvAlarmIndex.put(index);
            }
        } else {
            System.err.println(
                    pvStructure.getField().getFieldName()
                    + " v3Ca error " + message
                    + " severity " + alarmSeverity.toString());
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3ChannelStructure#toStructure(gov.aps.jca.dbr.DBR)
     */
    @Override
    public void toStructure(DBR fromDBR) {
        if(fromDBR==null) {
            setAlarm(AlarmSeverity.invalid,"fromDBR is null");
            return;
        }
        gov.aps.jca.dbr.Status status = null;
        gov.aps.jca.dbr.TimeStamp timeStamp = null;
        gov.aps.jca.dbr.Severity severity = null;
        gov.aps.jca.Channel jcaChannel = v3Channel.getJCAChannel();
        int elementCount = jcaChannel.getElementCount();
        
        double displayLow = 0.0;
        double displayHigh = 0.0;
        double controlLow = 0.0;
        double controlHigh = 0.0;
        String units = null;
        int precision = -1;
        bitSet.clear();
        DBRType requestDBRType = fromDBR.getType();
        if(nativeDBRType.isENUM()) {
            int index = pvEnumerated.getIndex().get();
            if(requestDBRType==DBRType.ENUM) {
                DBR_Enum dbr = (DBR_Enum)fromDBR;
                index = dbr.getEnumValue()[0];
            } else if(requestDBRType==DBRType.TIME_ENUM) {
                DBR_TIME_Enum dbr = (DBR_TIME_Enum)fromDBR;
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                timeStamp = dbr.getTimeStamp();
            } else if(requestDBRType==DBRType.STS_ENUM) {
                DBR_STS_Enum dbr = (DBR_STS_Enum)fromDBR;
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
            } else if(requestDBRType==DBRType.CTRL_ENUM) {
                DBR_CTRL_Enum dbr = (DBR_CTRL_Enum)fromDBR;
                String[] labels = dbr.getLabels();
                pvEnumerated.getChoices().put(0, labels.length, labels, 0);
                index = dbr.getEnumValue()[0];
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_ENUM;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_ENUM;
                } else {
                    this.requestDBRType = DBRType.ENUM;
                }
            } else {
                setAlarm(AlarmSeverity.invalid,
                        " unsupported DBRType " + requestDBRType.getName());
                return;
            }
            if(index!=pvEnumerated.getIndex().get()) {
                PVInt pvInt = pvEnumerated.getIndex();
                pvInt.put(index);
            }
            
        } else {
            if(requestDBRType==DBRType.DOUBLE) {
                DBR_Double dbr = (DBR_Double)fromDBR;
                if(elementCount==1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_DOUBLE) {
                DBR_STS_Double dbr = (DBR_STS_Double)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_DOUBLE) {
                DBR_TIME_Double dbr = (DBR_TIME_Double)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
            } else if(requestDBRType==DBRType.SHORT) {
                DBR_Short dbr = (DBR_Short)fromDBR;
                if(elementCount==1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_SHORT) {
                DBR_STS_Short dbr = (DBR_STS_Short)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_SHORT) {
                DBR_TIME_Short dbr = (DBR_TIME_Short)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
            } else if(requestDBRType==DBRType.INT) {
                DBR_Int dbr = (DBR_Int)fromDBR;
                if(elementCount==1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_INT) {
                DBR_STS_Int dbr = (DBR_STS_Int)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_INT) {
                DBR_TIME_Int dbr = (DBR_TIME_Int)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
            } else if(requestDBRType==DBRType.BYTE) {
                DBR_Byte dbr = (DBR_Byte)fromDBR;
                if(elementCount==1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_BYTE) {
                DBR_STS_Byte dbr = (DBR_STS_Byte)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_BYTE) {
                DBR_TIME_Byte dbr = (DBR_TIME_Byte)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
            } else if(requestDBRType==DBRType.FLOAT) {
                DBR_Float dbr = (DBR_Float)fromDBR;
                if(elementCount==1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_FLOAT) {
                DBR_STS_Float dbr = (DBR_STS_Float)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_FLOAT) {
                DBR_TIME_Float dbr = (DBR_TIME_Float)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
            } else if(requestDBRType==DBRType.STRING) {
                DBR_String dbr = (DBR_String)fromDBR;
                if(elementCount==1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if(requestDBRType==DBRType.STS_STRING) {
                DBR_STS_String dbr = (DBR_STS_String)fromDBR;
                status = dbr.getStatus();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if(requestDBRType==DBRType.TIME_STRING) {
                DBR_TIME_String dbr = (DBR_TIME_String)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
            } else if(requestDBRType==DBRType.GR_BYTE) {
                DBR_GR_Byte dbr = (DBR_GR_Byte)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_BYTE;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_BYTE;
                } else {
                    this.requestDBRType = DBRType.BYTE;
                }
            } else if(requestDBRType==DBRType.CTRL_BYTE) {
                DBR_CTRL_Byte dbr = (DBR_CTRL_Byte)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromByte(pvScalarValue, dbr.getByteValue()[0]);
                } else {
                    convert.fromByteArray(pvArrayValue, 0, dbr.getCount(), dbr.getByteValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_BYTE;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_BYTE;
                } else {
                    this.requestDBRType = DBRType.BYTE;
                }
            } else if(requestDBRType==DBRType.GR_SHORT) {
                DBR_GR_Short dbr = (DBR_GR_Short)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_SHORT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_SHORT;
                } else {
                    this.requestDBRType = DBRType.SHORT;
                }
            } else if(requestDBRType==DBRType.CTRL_SHORT) {
                DBR_CTRL_Short dbr = (DBR_CTRL_Short)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromShort(pvScalarValue, dbr.getShortValue()[0]);
                } else {
                    convert.fromShortArray(pvArrayValue, 0, dbr.getCount(), dbr.getShortValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_SHORT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_SHORT;
                } else {
                    this.requestDBRType = DBRType.SHORT;
                }
            } else if(requestDBRType==DBRType.GR_INT) {
                DBR_GR_Int dbr = (DBR_GR_Int)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_INT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_INT;
                } else {
                    this.requestDBRType = DBRType.INT;
                }
            } else if(requestDBRType==DBRType.CTRL_INT) {
                DBR_CTRL_Int dbr = (DBR_CTRL_Int)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromInt(pvScalarValue, dbr.getIntValue()[0]);
                } else {
                    convert.fromIntArray(pvArrayValue, 0, dbr.getCount(), dbr.getIntValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_INT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_INT;
                } else {
                    this.requestDBRType = DBRType.INT;
                }
            } else if(requestDBRType==DBRType.GR_FLOAT) {
                DBR_GR_Float dbr = (DBR_GR_Float)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_FLOAT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_FLOAT;
                } else {
                    this.requestDBRType = DBRType.FLOAT;
                }
            } else if(requestDBRType==DBRType.CTRL_FLOAT) {
                DBR_CTRL_Float dbr = (DBR_CTRL_Float)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromFloat(pvScalarValue, dbr.getFloatValue()[0]);
                } else {
                    convert.fromFloatArray(pvArrayValue, 0, dbr.getCount(), dbr.getFloatValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_FLOAT;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_FLOAT;
                } else {
                    this.requestDBRType = DBRType.FLOAT;
                }
            } else if(requestDBRType==DBRType.GR_DOUBLE) {
                DBR_GR_Double dbr = (DBR_GR_Double)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                precision = dbr.getPrecision();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_DOUBLE;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_DOUBLE;
                } else {
                    this.requestDBRType = DBRType.DOUBLE;
                }
            } else if(requestDBRType==DBRType.CTRL_DOUBLE) {
                DBR_CTRL_Double dbr = (DBR_CTRL_Double)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                units = dbr.getUnits();
                precision = dbr.getPrecision();
                displayLow = dbr.getLowerDispLimit().doubleValue();
                displayHigh = dbr.getUpperDispLimit().doubleValue();
                controlLow = dbr.getLowerCtrlLimit().doubleValue();
                controlHigh = dbr.getUpperCtrlLimit().doubleValue();
                if(elementCount==1) {
                    convert.fromDouble(pvScalarValue, dbr.getDoubleValue()[0]);
                } else {
                    convert.fromDoubleArray(pvArrayValue, 0, dbr.getCount(), dbr.getDoubleValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_DOUBLE;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_DOUBLE;
                } else {
                    this.requestDBRType = DBRType.DOUBLE;
                }
            } else if(requestDBRType==DBRType.GR_STRING) {
                DBR_GR_String dbr = (DBR_GR_String)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_STRING;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_STRING;
                } else {
                    this.requestDBRType = DBRType.STRING;
                }
            } else if(requestDBRType==DBRType.CTRL_STRING) {
                DBR_CTRL_String dbr = (DBR_CTRL_String)fromDBR;
                status = dbr.getStatus();
                timeStamp = dbr.getTimeStamp();
                severity = dbr.getSeverity();
                if(elementCount==1) {
                    convert.fromString(pvScalarValue, dbr.getStringValue()[0]);
                } else {
                    convert.fromStringArray(pvArrayValue, 0, dbr.getCount(), dbr.getStringValue(), 0);
                }
                if(pvTimeStamp!=null) {
                    this.requestDBRType = DBRType.TIME_STRING;
                } else if(pvAlarmIndex!=null) {
                    this.requestDBRType = DBRType.STS_STRING;
                } else {
                    this.requestDBRType = DBRType.STRING;
                }
            } else {
                setAlarm(AlarmSeverity.invalid,
                        " unsupported DBRType " + requestDBRType.getName());
                return;
            }
        }
        PVStructure pvStructure = null;
        if(timeStamp!=null&&pvTimeStamp!=null) {
            long seconds = timeStamp.secPastEpoch();
            seconds += 7305*86400;
            pvSeconds.put(seconds);
            pvNanoSeconds.put((int)timeStamp.nsec());
        }
        if(severity!=null && pvAlarm!=null) {
            int index = severity.getValue();
            AlarmSeverity alarmSeverity = AlarmSeverity.getSeverity(index);
            String message = status.getName();
            setAlarm(alarmSeverity,message);
        }
        if(displayLow<displayHigh) {
            pvStructure = this.pvStructure.getStructureField("display");
            if(pvStructure!=null) {
                if(units!=null) {
                    PVString pvUnits = pvStructure.getStringField("units");
                    if(pvUnits!=null) {
                        pvUnits.put(units.toString());
                    }
                }
                if(precision>=0) {
                    PVInt pvResolution = pvStructure.getIntField("resolution");
                    if(pvResolution!=null) {
                        pvResolution.put(precision);
                    }
                }
                PVStructure pvLimits = pvStructure.getStructureField("limit");
                if(pvLimits!=null) {
                    PVDouble pvLow = pvLimits.getDoubleField("low");
                    PVDouble pvHigh = pvLimits.getDoubleField("high");
                    if(pvLow!=null && pvHigh!=null) {
                        pvLow.put(displayLow);
                        pvHigh.put(displayHigh);
                    }
                }
            }
        }
        if(controlLow<controlHigh) {
            pvStructure = this.pvStructure.getStructureField("control");
            if(pvStructure!=null) {
                pvStructure = pvStructure.getStructureField("limit");
                if(pvStructure!=null) {
                    PVDouble pvLow = pvStructure.getDoubleField("low");
                    PVDouble pvHigh = pvStructure.getDoubleField("high");
                    if(pvLow!=null && pvHigh!=null) {
                        pvLow.put(controlLow);
                        pvHigh.put(controlHigh);
                    }
                }
            }
        }
        if(firstGetPVStructure) {
            bitSet.clear();
            bitSet.set(0);
            firstGetPVStructure = false;
        } else {
            if(pvAlarmMessage!=null) bitSet.set(pvAlarmMessage.getFieldOffset());
            if(pvAlarmIndex!=null) bitSet.set(pvAlarmIndex.getFieldOffset());
            if(pvTimeStamp!=null) bitSet.set(pvTimeStamp.getFieldOffset());
            if(pvScalarValue!=null) bitSet.set(pvScalarValue.getFieldOffset());
            if(pvArrayValue!=null) bitSet.set(pvArrayValue.getFieldOffset());
            if(pvEnumerated!=null) bitSet.set(pvEnumerated.getIndex().getFieldOffset());
        }
    }
}
