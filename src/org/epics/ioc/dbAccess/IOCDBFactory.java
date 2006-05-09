
package org.epics.ioc.dbAccess;

import org.epics.ioc.dbDefinition.*;
import org.epics.ioc.pvAccess.*;

import java.util.*;
import java.util.regex.*;


/**
 * factory for creating an IOCDB.
 * @author mrk
 *
 */
public class IOCDBFactory {

    /**
     * create an IOCDB.
     * @param dbd the reflection database.
     * @param name the name for the IOCDB.
     * @return the newly created IOCDB.
     */
    public static IOCDB create(DBD dbd, String name) {
        if(find(name)!=null) return null;
        IOCDB iocdb = new IOCDBInstance(dbd,name);
        iocdbList.addLast(iocdb);
        return iocdb;
    }
    
    /**
     * find an IOCDB.
     * @param name the IOCDB name.
     * @return the IOCDB.
     */
    public static IOCDB find(String name) {
        ListIterator<IOCDB> iter = iocdbList.listIterator();
        while(iter.hasNext()) {
            IOCDB iocdb = iter.next();
            if(name.equals(iocdb.getName())) return iocdb;
        }
        return null;
    }
    
    /**
     * get the complete collection of IOCDBs.
     * @return the collection.
     */
    public static Collection<IOCDB> getIOCDBList() {
        return iocdbList;
    }

    /**
     * remove an IOCDB from the collection.
     * @param iocdb
     */
    public static void remove(IOCDB iocdb) {
        iocdbList.remove(iocdb);
    }
    
    private static LinkedList<IOCDB> iocdbList;
    static {
        iocdbList = new LinkedList<IOCDB>();
    }
    
    private static class DBAccessInstance implements DBAccess {
        public DBRecord getDbRecord() {
            return dbRecord;
        }
        public DBData getField() {
            return dbDataSetField;
        }
        public boolean setField(String fieldName) {
            if(fieldName==null || fieldName.length()==0) {
                dbDataSetField = null;
                return false;
            }
            String[] names = periodPattern.split(fieldName,0);
            if(names.length<=0) return false;
            DBData currentData = dbDataSetField;
            if(currentData==null) currentData = dbRecord;
            DBData newData = null;
            int nameIndex = 0;
            while(nameIndex < names.length) {
                String name = names[nameIndex];
                int arrayIndex = -1;
                int startBracket = name.indexOf('[');
                if(startBracket>=0) {
                    String arrayIndexString = name.substring(startBracket+1);
                    name = name.substring(0,startBracket);
                    int endBracket = arrayIndexString.indexOf(']');
                    if(endBracket<0) break;
                    arrayIndexString = arrayIndexString.substring(0,endBracket);
                    arrayIndex = Integer.parseInt(arrayIndexString);
                }
                newData = findField(currentData,name);
                if(newData==null) break;
                if(arrayIndex>=0) {
                    Field field = newData.getField();
                    if(field.getType()!=Type.pvArray) break;
                    Array array = (Array)field;
                    if(array.getElementType()!=Type.pvStructure) break;
                    DBStructureArray dbStructureArray = (DBStructureArray)newData;
                    if(arrayIndex>=dbStructureArray.getLength()) break;
                    DBStructure[] structureArray = new DBStructure[1];
                    int n = dbStructureArray.get(arrayIndex,1,structureArray,0);
                    if(n<1 || structureArray[0]==null) break;
                    newData = structureArray[0];
                }
                currentData = newData;
                nameIndex++;
            }
            if(nameIndex<names.length) return false;
            dbDataSetField = currentData;
            return true;
        }
        
        public void setField(DBData dbData) {
            if(dbData.getRecord()!=dbRecord) 
                throw new IllegalArgumentException ("field is not in this record instance");
            dbDataSetField = dbData;
        }
        
        public DBData getPropertyField(Property property) {
            if(property==null) return null;
            DBData currentData = dbDataSetField;
            if(currentData==null) currentData = dbRecord;
            return findPropertyField(currentData,property);
        }

        
        DBAccessInstance(DBRecord dbRecord) {
            this.dbRecord = dbRecord;
            dbDataSetField = null;
        }
        
        private DBRecord dbRecord;
        private DBData dbDataSetField;
        static private Pattern periodPattern = Pattern.compile("[.]");
        
        static private DBData findField(DBData dbData,String name) {
            DBData newField = getField(dbData,name);
            if(newField!=null) return newField;
            Property property = getProperty(dbData,name);
            if(property!=null) newField = findPropertyField(dbData,property);
            return newField;
            
        }
        
        static private DBData  findPropertyField(DBData dbData,Property property) {
            String propertyName = property.getName();
            String propertyFieldName = property.getFieldName();
            if(propertyFieldName.equals("..")) {
                if(dbData.getField().getType()!=Type.pvStructure) {
                    dbData = dbData.getParent();
                }
                DBData dbTemp = dbData.getParent();
                if(dbTemp==null) {
                    System.err.printf("record type %s has a property with fieldName ..",
                            ((Structure)dbData.getField()).getStructureName());
                    return null;
                }
                dbData = dbTemp;
                if(property!=null) {
                    dbData = findPropertyFieldParent(dbData,propertyName);
                }
            } else {
                dbData = findPropertyFieldChild(dbData,propertyFieldName);
            }
            return dbData;            
        }
        
        static private DBData findPropertyFieldParent(DBData dbData,String propertyName) {
            if(dbData==null) return dbData;
            DBStructure structure = (DBStructure)dbData;
            DBData[] datas = structure.getFieldDBDatas();
            int ind =  structure.getFieldDBDataIndex(propertyName);
            if(ind>=0) return datas[ind];
            return findPropertyFieldParent(dbData.getParent(),propertyName);
        }
        
        static private DBData findPropertyFieldChild(DBData dbData,String propertyName) {   
            String[] names = periodPattern.split(propertyName,0);
            int length = names.length;
            if(length<1 || length>2) {
                DBRecord dbRecord = dbData.getRecord();
                System.err.printf(
                    "somewhere in recordType %s " +
                    "property has bad field %s\n",
                    ((Structure)dbRecord.getField()).getStructureName(),
                    propertyName,dbData.getField().getName());
                return null;
            }
            dbData = getField(dbData,names[0]);
            if(dbData!=null && length==2) {
                DBData newField = getField(dbData,names[1]);
                if(newField!=null) {
                    dbData = newField;
                } else {
                    Property property = getProperty(dbData,names[1]);
                    if(property==null) {
                        dbData = null;
                    } else {
                        dbData = findPropertyFieldChild(dbData,property.getFieldName());
                    }
                }
            }
            return dbData;            
        }
        
        static private DBData getField(DBData dbData, String fieldName) {
            DBData newData = null;
            if(dbData.getField().getType()==Type.pvStructure) {
                DBStructure structure = (DBStructure)dbData;
                DBData[] dbDatas = structure.getFieldDBDatas();
                int dataIndex = structure.getFieldDBDataIndex(fieldName);
                if(dataIndex>=0) {
                    newData = dbDatas[dataIndex];
                }
            }
            if(newData==null) {
                DBStructure structure = dbData.getParent();
                if(structure!=null) {
                    DBData[]dbDatas = structure.getFieldDBDatas();
                    int dataIndex = structure.getFieldDBDataIndex(fieldName);
                    if(dataIndex>=0) {
                        newData = dbDatas[dataIndex];
                    }
                }
            }
            return newData;
        }
        static private Property getProperty(DBData dbData,String name) {
            Property property = null;
            Property[]propertys = dbData.getField().getPropertys();
            for(int i = 0; i < propertys.length; i++) {
                if(propertys[i].getName().equals(name)) {
                    property = propertys[i];
                    return property;
                }
            }
            DBData parent = dbData.getParent();
            if(parent!=null) {
                propertys = parent.getField().getPropertys();
                for(int i = 0; i < propertys.length; i++) {
                    if(propertys[i].getName().equals(name)) {
                        property = propertys[i];
                            break;
                    }
                }
            }
            return property;                
        }
    }

    private static class IOCDBInstance implements IOCDB
    {
        public DBAccess createAccess(String recordName) {
            DBRecord dbRecord = findRecord(recordName);
            if(dbRecord!=null) return new DBAccessInstance(dbRecord);
            return null;
        }
        public boolean createRecord(String recordName, DBDRecordType dbdRecordType) {
            DBRecord record = FieldDataFactory.createRecord(recordName,dbdRecordType);
            recordMap.put(recordName,record);
            return true;
        }
        public DBD getDBD() {
            return dbd;
        }
        public String getName() {
            return name;
        }
        
        public DBRecord findRecord(String recordName) {
            return recordMap.get(recordName);
        }

        public Map<String,DBRecord> getRecordMap() {
            return recordMap;
        }
        IOCDBInstance(DBD dbd, String name) {
            this.dbd = dbd;
            this.name = name;
        }
        
        private DBD dbd;
        private String name;
        private static Map<String,DBRecord> recordMap;
        static {
            recordMap = new HashMap<String,DBRecord>();
        }
    }
}