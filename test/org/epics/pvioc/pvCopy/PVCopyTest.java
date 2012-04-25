/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvioc.pvCopy;

import junit.framework.TestCase;

import org.epics.pvaccess.client.*;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.BitSetUtil;
import org.epics.pvdata.misc.BitSetUtilFactory;
import org.epics.pvdata.pv.*;
import org.epics.pvioc.database.PVDatabase;
import org.epics.pvioc.database.PVDatabaseFactory;
import org.epics.pvioc.database.PVRecord;
import org.epics.pvioc.database.PVRecordField;
import org.epics.pvioc.database.PVRecordStructure;
import org.epics.pvioc.database.PVReplaceFactory;
import org.epics.pvioc.pvCopy.PVCopy;
import org.epics.pvioc.pvCopy.PVCopyFactory;
import org.epics.pvioc.pvCopy.PVCopyMonitor;
import org.epics.pvioc.pvCopy.PVCopyMonitorRequester;
import org.epics.pvioc.xml.XMLToPVDatabaseFactory;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVCopyTest extends TestCase {
    private final static PVDatabase master = PVDatabaseFactory.getMaster();
    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private final static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private final static BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    private final static Requester requester = new RequesterImpl();
    private final static Convert convert = ConvertFactory.getConvert();
   
    
    private static class RequesterImpl implements Requester {
		@Override
		public String getRequesterName() {
			return "pvCopyTest";
		}
		@Override
		public void message(String message, MessageType messageType) {
		    System.out.printf("message %s messageType %s%n",message,messageType.name());
			
		}
    }
    
    public static void testPVCopy() {
        // get database for testing
System.out.println("start");
        Requester iocRequester = new RequesterForTesting("accessTest");
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/xml/structures.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/org/epics/ioc/pvCopy/powerSupply.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/org/epics/ioc/pvCopy/powerSupplyArray.xml", iocRequester);
        XMLToPVDatabaseFactory.convert(master,"${JAVAIOC}/test/org/epics/ioc/pvCopy/structuresForPVCopyTest.xml", iocRequester);
        PVReplaceFactory.replace(master);
        exampleTest();
        exampleShareDataTest();
        copyMonitorTest();
        longTest();
    }
    
    public static void exampleTest() {
        System.out.printf("%n%n****Example****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = null;
        String request = "";
        PVStructure pvRequest = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvCopyStructure = null;
        BitSet bitSet = null;
        pvRecord = master.findRecord("powerSupply");
//System.out.println(pvRecord.getPVRecordStructure().getPVStructure().toString());
        request = "alarm,timeStamp,power.value";
        pvRequest = CreateRequestFactory.createRequest(request,requester);
        assertTrue(pvRequest!=null);
//System.out.println("pvRequest " + pvRequest);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"");
        pvCopyStructure = pvCopy.createPVStructure();
//System.out.println("pvCopyStructure " + pvCopyStructure);
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        request = "field(alarm,timeStamp,power{power.value,power.alarm},"
                + "current{current.value,current.alarm},voltage{voltage.value,voltage.alarm})";
System.out.println("request " + request);
        pvRequest = CreateRequestFactory.createRequest(request,requester);
System.out.println("pvRequest ");
System.out.println(pvRequest);
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
        "%npowerSupply from powerSupplyArray%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvRequest = master.findStructure("powerSupplyFromPowerSupplyArray");
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        System.out.println(pvCopyStructure.toString());
    }
    
    public static void exampleShareDataTest() {
        System.out.printf("%n%n****Example Share Data****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = null;
        PVStructure pvRequest = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvCopyStructure = null;
        
        System.out.printf(
                "%nalarm,timeStamp,power.value from powerSupply%n");
        pvRecord = master.findRecord("powerSupply");
        pvRequest = master.findStructure("powerFromPowerSupplyShared"); 
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        pvRequest = master.findStructure("powerSupplyFromPowerSupplyShared");
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        System.out.println(pvCopyStructure.toString());
        
        System.out.printf(
        "%npowerSupply from powerSupplyArray%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvRequest = master.findStructure("powerSupplyFromPowerSupplyArrayShared"); 
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        System.out.println(pvCopyStructure.toString());
    }
    
    public static void copyMonitorTest() {
        System.out.printf("%n%n****Copy Monitor****%n");
        // definitions for request structure to pass to PVCopyFactory
        PVRecord pvRecord = master.findRecord("powerSupply");
//System.out.println(pvRecord.getPVRecordStructure().getPVStructure().toString());
        PVStructure pvStructure = pvRecord.getPVRecordStructure().getPVStructure();
        PVLong pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        PVStructure pvRequest = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVStructure pvCopyStructure = null;
        BitSet changeBitSet = null;
        BitSet overrunBitSet = null;
         
        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        pvRequest = master.findStructure("powerSupplyFromPowerSupply"); 
        assertTrue(pvRequest!=null);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,"field");
        pvCopyStructure = pvCopy.createPVStructure();
        PVLong pvCopySeconds = (PVLong)pvCopyStructure.getSubField("timeStamp.secondsPastEpoch");
        PVInt pvCopyNanoSeconds = (PVInt)pvCopyStructure.getSubField("timeStamp.nanoSeconds");
        PVDouble pvCopyPowerValue = (PVDouble)pvCopyStructure.getSubField("power.value");
        changeBitSet = new BitSet(pvCopyStructure.getNumberFields());
        overrunBitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, changeBitSet, true);
        CopyMonitorRequester copyMonitorRequester = new CopyMonitorRequester(pvCopy);
        copyMonitorRequester.startMonitoring(changeBitSet, overrunBitSet);
        // must flush initial
        pvRecord.beginGroupPut();
        pvRecord.endGroupPut();
        copyMonitorRequester.setDataChangedFalse();
        changeBitSet.clear();
        overrunBitSet.clear();
        pvRecord.beginGroupPut();
        assertFalse(changeBitSet.get(pvCopySeconds.getFieldOffset()));
        assertFalse(changeBitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertFalse(changeBitSet.get(pvCopyPowerValue.getFieldOffset()));
        pvRecordSeconds.put(5000);
        pvRecordNanoSeconds.put(6000);
        pvRecordPowerValue.put(1.56);
        assertTrue(changeBitSet.get(pvCopySeconds.getFieldOffset()));
        assertTrue(changeBitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertTrue(changeBitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertFalse(overrunBitSet.get(pvCopyPowerValue.getFieldOffset()));
        pvRecordPowerValue.put(2.0);
        assertTrue(overrunBitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertFalse(copyMonitorRequester.dataChanged);
        pvRecord.endGroupPut();
        assertTrue(copyMonitorRequester.dataChanged);
        copyMonitorRequester.stopMonitoring();
    }
    
    private static class CopyMonitorRequester implements PVCopyMonitorRequester {
        private PVCopyMonitor pvCopyMonitor = null;
        private boolean dataChanged = false;
        

        private CopyMonitorRequester(PVCopy pvCopy) {
            pvCopyMonitor = pvCopy.createPVCopyMonitor(this);
        }
        
        private void startMonitoring(BitSet changeBitSet, BitSet overrunBitSet) {
            pvCopyMonitor.startMonitoring(changeBitSet, overrunBitSet);
        }
        
        private void stopMonitoring() {
            pvCopyMonitor.stopMonitoring();
        }
        
        private void setDataChangedFalse() {
            dataChanged = false;
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopyMonitorRequester#dataChanged()
         */
        @Override
        public void dataChanged() {
            dataChanged = true;
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.PVCopyMonitorRequester#unlisten()
         */
        @Override
        public void unlisten() {
            // TODO Auto-generated method stub
            
        }
        
    }
    public static void longTest() {  
        System.out.printf("%n%n****Long Test****%n");
        // definitions for request structure to pass to PVCopyFactory
        int offset = 0;
        PVStructure pvRequest = null;
        PVString pvString = null;
        // definitions for PVCopy
        PVCopy pvCopy = null;
        PVRecordStructure pvRecordStructure = null;
        PVStructure pvCopyStructure = null;
        BitSet bitSet = null;
        PVField pvInRecord = null;
        PVField pvFromRecord = null;
        PVField pvFromCopy = null;
        // fields in pvRecordStructure
        PVLong pvRecordSeconds = null;
        PVInt pvRecordNanoSeconds = null;
        PVInt pvRecordUserTag = null;
        PVDouble pvRecordPowerValue = null;
        PVDouble pvRecordCurrentValue = null;
        // fields in pvCopyStructure
        PVField pvCopyTimeStamp = null;
        PVLong pvCopySeconds = null;
        PVInt pvCopyNanoSeconds = null;
        PVInt pvCopyUserTag = null;
        PVField pvCopyPower = null;
        PVField pvCopyPowerValue = null;
        PVField pvCopyCurrentValue = null;
        
        PVRecord pvRecord = master.findRecord("powerSupply");
        PVStructure pvStructure = pvRecord.getPVRecordStructure().getPVStructure();
        System.out.printf("%nvalue, alarm, timeStamp%n");
        Field[] fields = new Field[1];
        String[] fieldNames = new String[1];
        fields[0] = fieldCreate.createScalar(ScalarType.pvString);
        fieldNames[0] = "fieldList";
        Structure structure = fieldCreate.createStructure(fieldNames, fields);
        pvRequest = pvDataCreate.createPVStructure(null,structure);
        pvString = pvRequest.getStringField(fieldNames[0]);
        pvString.put("power.value,alarm,timeStamp");
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,null);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvStructure.getSubField("power.value");
        offset = pvCopy.getCopyOffset(pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("value"));
        pvInRecord = pvStructure.getSubField("alarm");
        offset = pvCopy.getCopyOffset(pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("alarm"));
        pvRecordStructure = (PVRecordStructure)pvRecord.findPVRecordField(pvStructure.getSubField("alarm"));
        pvInRecord = pvStructure.getSubField("alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        bitSet.clear();
        pvCopyPowerValue = pvCopyStructure.getSubField("value");  
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        pvRecordUserTag = (PVInt)pvStructure.getSubField("timeStamp.userTag");
        pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        pvRecordNanoSeconds.put(1000);
        pvRecordUserTag.put(1);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvRecordSeconds.put(20000);
        pvRecordNanoSeconds.put(2000);
        pvRecordUserTag.put(2);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);

        System.out.printf(
             "%npower, current, voltage. For each value and alarm."
              + " Note that PVRecord.power does NOT have an alarm field.%n");
        String[] requestFieldNames = new String[3];
        Field[] requestFields = new Field[4];
        requestFieldNames[0] = "fieldList";
        requestFieldNames[1] = "power";
        requestFieldNames[2] = "current";
        requestFieldNames[3] = "value";
        requestFields[0] = fieldCreate.createScalar(ScalarType.pvString);
        fieldNames = new String[1];
        fields = new Field[1];
        fieldNames[0] = "fieldList";
        fields[0] = fieldCreate.createScalar(ScalarType.pvString);
        structure = fieldCreate.createStructure(fieldNames,fields);
        requestFields[1] = structure;
        requestFields[2] = structure;
        requestFields[3] = structure;
        structure = fieldCreate.createStructure(requestFieldNames, requestFields);
        pvRequest = pvDataCreate.createPVStructure(null,structure);
        pvString = pvRequest.getStringField("power.fieldList");
        pvString.put("power.value,power.alarm");
        pvString = pvRequest.getStringField("current.fieldList");
        pvString.put("current.value,current.alarm");
        pvString = pvRequest.getStringField("voltage.fieldList");
        pvString.put("voltage.value,voltage.alarm");
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,null);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvCopy.toString());
        pvInRecord = pvStructure.getSubField("current.value");
        offset = pvCopy.getCopyOffset(pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("value"));
        pvRecordStructure = (PVRecordStructure)pvRecord.findPVRecordField(pvStructure.getSubField("current.alarm"));
        pvInRecord = pvStructure.getSubField("current.alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        bitSet.clear();
        // get pvRecord fields
        pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        pvRecordUserTag = (PVInt)pvStructure.getSubField("timeStamp.userTag");
        pvRecordPowerValue = (PVDouble)pvStructure.getSubField("power.value");
        pvRecordCurrentValue = (PVDouble)pvStructure.getSubField("current.value");
        // get pvStructureForCopy fields
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvCopySeconds = (PVLong)pvCopyStructure.getSubField("timeStamp.secondsPastEpoch");
        pvCopyNanoSeconds = (PVInt)pvCopyStructure.getSubField("timeStamp.nanoSeconds");
        pvCopyUserTag = (PVInt)pvCopyStructure.getSubField("timeStamp.userTag");
        pvCopyPower = pvCopyStructure.getSubField("power");
        pvCopyPowerValue = pvCopyStructure.getSubField("power.value"); 
        pvCopyCurrentValue = pvCopyStructure.getSubField("current.value"); 
        pvRecordNanoSeconds.put(1000);
        pvRecordUserTag.put(1);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(4.0);
        pvRecordCurrentValue.put(.4);
        pvRecordSeconds.put(40000);
        pvRecordNanoSeconds.put(4000);
        pvRecordUserTag.put(4);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        assertTrue(bitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyPower.getFieldOffset()));
        assertTrue(bitSet.get(pvCopySeconds.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyUserTag.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyTimeStamp.getFieldOffset()));
        showModified("before compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        assertFalse(bitSet.get(pvCopyPowerValue.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyPower.getFieldOffset()));
        assertFalse(bitSet.get(pvCopySeconds.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyNanoSeconds.getFieldOffset()));
        assertFalse(bitSet.get(pvCopyUserTag.getFieldOffset()));
        assertTrue(bitSet.get(pvCopyTimeStamp.getFieldOffset()));
        showModified("after compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("after second compress update value, current, and timeStamp " + pvCopyPowerValue.toString() + " "
                + pvCopyCurrentValue.toString() + " "+ pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        PVStructure empty = pvDataCreate.createPVStructure(null,new String[0], new PVField[0]);
        pvCopy = PVCopyFactory.create(pvRecord, empty,null);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        compareCopyWithRecord("after init",pvCopyStructure,pvCopy);
        pvRecordPowerValue.put(6.0);
        pvRecordCurrentValue.put(.6);
        pvRecordSeconds.put(60000);
        pvRecordNanoSeconds.put(6000);
        pvRecordUserTag.put(6);
        compareCopyWithRecord("after change record ",pvCopyStructure,pvCopy);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        compareCopyWithRecord("after updateCopy",pvCopyStructure,pvCopy);
        pvCopySeconds = (PVLong)pvCopyStructure.getSubField("timeStamp.secondsPastEpoch");
        pvCopyNanoSeconds = (PVInt)pvCopyStructure.getSubField("timeStamp.nanoSeconds");
        pvCopyUserTag = (PVInt)pvCopyStructure.getSubField("timeStamp.userTag");
        PVDouble pvDouble = (PVDouble)pvCopyStructure.getSubField("power.value");
        pvDouble.put(7.0);
        pvCopySeconds.put(700);
        pvCopyNanoSeconds.put(7000);
        pvCopyUserTag.put(7);
        compareCopyWithRecord("after change copy ",pvCopyStructure,pvCopy);
        pvCopy.updateRecord(pvCopyStructure, bitSet,true);
        compareCopyWithRecord("after updateRecord",pvCopyStructure,pvCopy);
        
        System.out.printf("%npowerSupplyArray: value alarm and timeStamp."
                 + " Note where power and alarm are chosen.%n");
        pvRecord = master.findRecord("powerSupplyArray");
        pvStructure = pvRecord.getPVRecordStructure().getPVStructure();
        pvRequest = CreateRequestFactory.createRequest("supply.0.power.value,supply.0.alarm,timeStamp",requester);
        pvCopy = PVCopyFactory.create(pvRecord, pvRequest,null);
        pvCopyStructure = pvCopy.createPVStructure();
        bitSet = new BitSet(pvCopyStructure.getNumberFields());
//System.out.println(pvStructureFromCopy.toString());
        pvInRecord = pvStructure.getSubField("supply.0.power.value");
        offset = pvCopy.getCopyOffset(pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("value"));
        pvInRecord = (PVStructure)pvStructure.getSubField("supply.0.alarm");
        offset = pvCopy.getCopyOffset(pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("alarm"));
        pvRecordStructure = (PVRecordStructure)pvRecord.findPVRecordField(pvStructure.getSubField("supply.0.alarm"));
        pvInRecord = pvStructure.getSubField("supply.0.alarm.message");
        offset = pvCopy.getCopyOffset(pvRecordStructure,pvRecord.findPVRecordField(pvInRecord));
        pvFromRecord = pvCopy.getRecordPVField(offset).getPVField();
        pvFromCopy = pvCopyStructure.getSubField(offset);
        assertTrue(pvInRecord==pvFromRecord);
        assertTrue(pvFromCopy.getFieldName().equals("message"));
        pvCopy.initCopy(pvCopyStructure, bitSet, true);
        bitSet.clear();
        pvCopyPowerValue = pvCopyStructure.getSubField("value");  
        pvCopyTimeStamp = pvCopyStructure.getSubField("timeStamp");
        pvRecordSeconds = (PVLong)pvStructure.getSubField("timeStamp.secondsPastEpoch");
        pvRecordNanoSeconds = (PVInt)pvStructure.getSubField("timeStamp.nanoSeconds");
        pvRecordUserTag = (PVInt)pvStructure.getSubField("timeStamp.userTag");
        pvRecordPowerValue = (PVDouble)pvStructure.getSubField("supply.0.power.value");
        pvRecordNanoSeconds.put(1000);
        pvRecordUserTag.put(1);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet, true);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update nanoSeconds " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
        bitSet.clear();
        pvRecordPowerValue.put(2.0);
        pvRecordSeconds.put(20000);
        pvRecordNanoSeconds.put(2000);
        pvRecordUserTag.put(2);
        pvCopy.updateCopySetBitSet(pvCopyStructure, bitSet,true);
        bitSetUtil.compress(bitSet, pvCopyStructure);
        showModified("update value and timeStamp " + pvCopyPowerValue.toString() + " " + pvCopyTimeStamp.toString(),pvCopyStructure,bitSet);
    }
    
    static void showModified(String message,PVStructure pvStructure,BitSet bitSet) {
        System.out.println();
        System.out.println(message);
        System.out.printf("modifiedFields bitSet %s%n", bitSet);
        int size = bitSet.size();
        int index = -1;
        while(++index < size) {
            if(bitSet.get(index)) {
                PVField pvField = pvStructure.getSubField(index);
                StringBuilder builder = new StringBuilder();
                convert.getFullFieldName(builder,pvField);
               System.out.println("   " + builder.toString());
            }
        }
    }
    
    static void compareCopyWithRecord(String message,PVStructure pvStructure,PVCopy pvCopy) {
        System.out.println();
        System.out.println(message);
        int length = pvStructure.getNumberFields();
        for(int offset=0; offset<length; offset++) {
            PVField pvCopyField = pvStructure.getSubField(offset);
            PVRecordField pvRecordField = pvCopy.getRecordPVField(offset);
            if(!pvCopyField.equals(pvRecordField.getPVField())) {
                StringBuilder builder = new StringBuilder();
                builder.append("    ");
                convert.getFullFieldName(builder,pvCopyField);
                builder.append(" NE ");
                convert.getFullFieldName(builder,pvRecordField.getPVField());
                builder.append(" NE ");
                System.out.println(builder.toString());
            }
        }
    }
}
