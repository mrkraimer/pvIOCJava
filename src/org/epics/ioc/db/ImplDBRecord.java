/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.ioc.dbd.DBD;
import org.epics.ioc.process.RecordProcess;
import org.epics.ioc.pv.Array;
import org.epics.ioc.pv.ArrayArrayData;
import org.epics.ioc.pv.PVArray;
import org.epics.ioc.pv.PVArrayArray;
import org.epics.ioc.pv.PVDataCreate;
import org.epics.ioc.pv.PVDataFactory;
import org.epics.ioc.pv.PVField;
import org.epics.ioc.pv.PVRecord;
import org.epics.ioc.pv.PVStructure;
import org.epics.ioc.pv.PVStructureArray;
import org.epics.ioc.pv.StructureArrayData;
import org.epics.ioc.pv.Type;

/**
 * Implementation of DBRecord
 * @author mrk
 *
 */
class ImplDBRecord implements DBRecord{
    PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static int numberRecords = 0;
    private int id = numberRecords++;
    private PVRecord pvRecord;
    private ImplDBStructure dbStructure;
    private IOCDB iocdb = null;
    private ReentrantLock lock = new ReentrantLock();
    private RecordProcess recordProcess = null;
    private LinkedList<RecordListenerPvt> recordListenerPvtList = new LinkedList<RecordListenerPvt>();
    private LinkedList<DBField> listenerSourceList = new LinkedList<DBField>();
    private DBD dbd = null;

    ImplDBRecord(PVRecord pvRecord,IOCDB iocdb,DBD dbd){
        this.iocdb = iocdb;
        this.dbd = dbd;
        this.pvRecord = pvRecord;
    }
    
    void init() {
        dbStructure = new ImplDBStructure(this,pvRecord);
        dbStructure.replaceCreate();
    }
    
    DBD getDBD() {
        return dbd;
    }
    
    void setDBD(DBD dbd) {
        this.dbd = dbd;
    }
   
    IOCDB getIOCDB() {
        return iocdb;
    }
    
    void setIOCDB(IOCDB iocdb) {
        this.iocdb = iocdb;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#findDBField(org.epics.ioc.pv.PVField)
     */
    public DBField findDBField(PVField pvField) {
        if(pvField==pvRecord) {
            return dbStructure;
        }
        int nlevels = 0;
        PVField parent = pvField.getParent();
        while(parent!=null) {
            nlevels++;
            parent = parent.getParent();
        }
        PVField[] pvFieldFind = new PVField[nlevels];
        pvFieldFind[nlevels-1] = pvField;
        parent = pvField.getParent();
        int index = nlevels-2;
        while(parent!=pvRecord) {
            pvFieldFind[index--] = parent;
            parent = parent.getParent();
        }
        DBField[] dbFields = dbStructure.getDBFields();
        PVField[] pvFields = pvRecord.getPVFields();
        return findDBField(pvField,pvFieldFind,0,dbFields,pvFields);
    }
    
    private DBField findDBField(
        PVField pvField,PVField[] pvFieldFind,int index,DBField[] dbFields,PVField[] pvFields)
    {
        PVField pvNow = pvFieldFind[index];
        for(int j=0; j<dbFields.length; j++) {
            if(pvFields[j]==pvNow) {
                if(pvNow==pvField) return dbFields[j];
                Type typeNow = pvNow.getField().getType();
                if(typeNow==Type.pvArray) {
                    return findArrayField(pvField,pvFieldFind,index+1,dbFields[j],(PVArray)pvFields[j]);
                }
                if(pvNow.getField().getType()!=Type.pvStructure) return dbFields[j];
                DBStructure dbStructure = (DBStructure)dbFields[j];
                dbFields = dbStructure.getDBFields();
                PVStructure pvStructure = (PVStructure)pvFields[j];
                pvFields = pvStructure.getPVFields();
                index++;
                return findDBField(pvField,pvFieldFind,index,dbFields,pvFields);
            }
        }
        throw new IllegalStateException("Logic error");
    }
    
    private DBField findArrayField(
            PVField pvField,PVField[]pvFieldFind,int index,DBField dbField,PVArray pvArray)
    {
        Array array = (Array)pvArray.getField();
        Type arrayElementType = array.getElementType();
        if(arrayElementType.isScalar()) {
            if(dbField.getPVField()==pvField) return dbField;
            throw new IllegalStateException("Logic error");
        }
        int size = pvArray.getLength();           
        switch(arrayElementType) {
        case pvStructure:
            DBStructureArray dbStructureArray = (DBStructureArray)dbField;
            PVStructureArray pvStructureArray = dbStructureArray.getPVStructureArray();
            DBStructure[] dbStructures = dbStructureArray.getElementDBStructures();
            StructureArrayData structureArrayData = new StructureArrayData();
            pvStructureArray.get(0, size, structureArrayData);
            PVStructure[] pvStructures = structureArrayData.data;
            for(int i=0; i<size; i++) {
                PVStructure pvStructure = pvStructures[i];
                if(pvStructure==null) continue;
                if(pvStructure==pvFieldFind[index]) {
                    DBStructure dbStructure = dbStructures[i];
                    if(pvStructure==pvField) {
                        if(dbStructure.getPVField()==pvStructure) return dbStructure;
                        throw new IllegalStateException("Logic error");
                    }
                    return findDBField(pvField,pvFieldFind,index+1,
                        dbStructure.getDBFields(),pvStructure.getPVFields());
                }
            }
            throw new IllegalStateException("Logic error");
        case pvArray:
            DBArrayArray dbArrayArray = (DBArrayArray)dbField;
            PVArrayArray pvArrayArray = dbArrayArray.getPVArrayArray();
            DBArray[] dbArrays = dbArrayArray.getElementDBArrays();
            ArrayArrayData arrayArrayData = new ArrayArrayData();
            pvArrayArray.get(0, size, arrayArrayData);
            PVArray[] pvArrays = arrayArrayData.data;
            for(int i=0; i<size; i++) {
                PVArray elementArray = pvArrays[i];
                if(elementArray==null) continue;
                if(elementArray==pvFieldFind[index]) {
                    Type elementType = ((Array)elementArray.getField()).getElementType();
                    if(elementType.isScalar()) {
                        if(elementArray==pvField) return dbArrays[i];
                        throw new IllegalStateException("Logic error");
                    }
                    return findArrayField(pvField,pvFieldFind,index+1,dbArrays[i],elementArray);
                }
            }
            throw new IllegalStateException("Logic error");
        }
        throw new IllegalStateException("Logic error");
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#getDBStructure()
     */
    public DBStructure getDBStructure() {
        return dbStructure;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#getPVRecord()
     */
    public PVRecord getPVRecord() {
        return pvRecord;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#lock()
     */
    public void lock() {
        lock.lock();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#unlock()
     */
    public void unlock() {
        lock.unlock();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#lockOtherRecord(org.epics.ioc.db.DBRecord)
     */
    public void lockOtherRecord(DBRecord otherRecord) {
        ImplDBRecord impl = (ImplDBRecord)otherRecord;
        int otherId = impl.getRecordID();
        if(id<=otherId) {
            otherRecord.lock();
            return;
        }
        int count = lock.getHoldCount();
        for(int i=0; i<count; i++) lock.unlock();
        otherRecord.lock();
        for(int i=0; i<count; i++) lock.lock();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#getRecordProcess()
     */
    public RecordProcess getRecordProcess() {
        return recordProcess;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#setRecordProcess(org.epics.ioc.process.RecordProcess)
     */
    public boolean setRecordProcess(RecordProcess process) {
        if(recordProcess!=null) return false;
        recordProcess = process;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#getRecordID()
     */
    public int getRecordID() {
        return id;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#beginProcess()
     */
    public void beginProcess() {
        Iterator<RecordListenerPvt> iter = recordListenerPvtList.iterator();
        while(iter.hasNext()) {
            RecordListenerPvt listener = iter.next();
            listener.getDBListener().beginProcess();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#endProcess()
     */
    public void endProcess() {
        Iterator<RecordListenerPvt> iter = recordListenerPvtList.iterator();
        while(iter.hasNext()) {
            RecordListenerPvt listener = iter.next();
            listener.getDBListener().endProcess();
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#createRecordListener(org.epics.ioc.db.DBListener)
     */
    public RecordListener createRecordListener(DBListener listener) {
        RecordListenerPvt recordListenerPvt = new RecordListenerPvt(listener);
        recordListenerPvtList.add(recordListenerPvt);
        return recordListenerPvt;
    }   
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#removeRecordListener(org.epics.ioc.db.RecordListener)
     */
    public void removeRecordListener(RecordListener listener) {
        Iterator<DBField> iter = listenerSourceList.iterator();
        while(iter.hasNext()) {
            DBField dbField = iter.next();
            dbField.removeListener(listener);
        }
        recordListenerPvtList.remove(listener);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#removeRecordListeners()
     */
    public void removeRecordListeners() {
        
        while(!recordListenerPvtList.isEmpty()) {
            RecordListenerPvt listener = recordListenerPvtList.remove();
            listener.getDBListener().unlisten(listener);
            Iterator<DBField> iter = listenerSourceList.iterator();
            while(iter.hasNext()) {
                DBField dbField = iter.next();
                dbField.removeListener(listener);
            }
        }
        listenerSourceList.clear();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#addListenerSource(org.epics.ioc.db.DBField)
     */
    public boolean addListenerSource(DBField dbField) {
        if(listenerSourceList.contains(dbField)) return false;
        listenerSourceList.add(dbField);
        return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return pvRecord.toString(0);
    }       
    /* (non-Javadoc)
     * @see org.epics.ioc.db.DBRecord#toString(int)
     */
    public String toString(int indentLevel) {
        return pvRecord.toString(indentLevel);
    }

    private static class RecordListenerPvt implements RecordListener {
        private DBListener dbListener;

        RecordListenerPvt(DBListener listener) {
            dbListener = listener;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.epics.ioc.db.RecordListener#getDBListener()
         */
        public DBListener getDBListener() {
            return dbListener;
        }
    }
}