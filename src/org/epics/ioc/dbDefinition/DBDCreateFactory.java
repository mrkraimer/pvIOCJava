/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.dbDefinition;
import org.epics.ioc.pvAccess.*;
import org.epics.ioc.pvAccess.Enum;

/**
 * Factory that creates an implementation of the various
 * Database Definition interfaces.
 * @author mrk
 *
 */
public final class  DBDCreateFactory {

    /**
     * creates a DBDMenu.
     * @param menuName name of the menu.
     * @param choices for the menu.
     * @return the menu or null if it already existed.
     */
    public static DBDMenu createMenu(String menuName, String[] choices)
    {
        return new MenuInstance(menuName,choices);
    }

    /**
     * create a DBDStructure.
     * @param name name of the structure.
     * @param dbdField an array of DBDField for the fields of the structure.
     * @param property an array of properties for the structure.
     * @return interface for the newly created structure.
     */
    public static DBDStructure createStructure(String name,
        DBDField[] dbdField,Property[] property)
    {
        return new StructureInstance(name,dbdField,property);
    }
    
    /**
     * create a DBDRecordType.
     * @param name the recordType name.
     * @param dbdField an array of DBDField for the fields of the structure.
     * @param property an array of properties for the structure.
     * @return interface for the newly created structure.
     */
    public static DBDRecordType createRecordType(String name,
        DBDField[] dbdField,Property[] property)
    {
        return new RecordTypeInstance(name,dbdField,property);
    }

    /**
     * create a DBDLinkSupport
     * @param supportName name of the link support.
     * @param configStructureName name of the configuration structure.
     * @return the DBDLinkSupport or null if it already existed.
     */
    public static DBDLinkSupport createLinkSupport(String supportName,
        String configStructureName)
    {
        return new LinkSupportInstance(supportName,configStructureName);
    }
    
    /**
     * creates a DBDField.
     * This is used for all DBTypes.
     * @param attribute the DBDAttribute interface for the field.
     * @param property an array of properties for the field.
     * @return interface for the newly created field.
     */
    public static DBDField createField(DBDAttribute attribute, Property[]property)
    {
        DBType dbType = attribute.getDBType();
        Type type = attribute.getType();
        switch(dbType) {
        case dbPvType:
            if(type==Type.pvEnum) {
                return new EnumFieldInstance(attribute,property);
            } else {
                return new FieldInstance(attribute,property);
            }
        case dbMenu:
            return new MenuFieldInstance(attribute,property);
        case dbArray:
            return new ArrayFieldInstance(attribute,property);
        case dbStructure:
            return new StructureFieldInstance(attribute,property);
        case dbLink:
            return new LinkFieldInstance(attribute,property);
        }
        throw new IllegalStateException("Illegal DBType. Logic error");
    }
    
    /**
     * Create a DBDStructure for link.
     * This is called by DBDFactory.
     * @param dbd the DBD that will have the DBDStructure for link.
     */
    public static void createLinkDBDStructure(DBD dbd) {
        DBDAttributeValues linkSupportValues = new StringValues(
            "linkSupportName");
        DBDAttribute linkSupportAttribute = DBDAttributeFactory.create(
            dbd,linkSupportValues);
        DBDField linkSupport = createField(linkSupportAttribute,null);
        DBDAttributeValues configValues = new StringValues(
            "configStructureName");
        DBDAttribute configAttribute = DBDAttributeFactory.create(
            dbd,configValues);
        DBDField config = createField(configAttribute,null);
        DBDField[] dbdField = new DBDField[]{linkSupport,config};
        DBDStructure link = createStructure("link",dbdField,null);
        dbd.addStructure(link);
    
    }

    private static class StringValues implements DBDAttributeValues {
      
        public int getLength() {
            return name.length;
        }
        public String getName(int index) {
            return name[index];
        }
        public String getValue(int index) {
            return value[index];
        }
        public String getValue(String attributeName) {
            for(int i=0; i< name.length; i++) {
                if(attributeName.equals(name[i])) return value[i];
            }
            return null;
        }
        StringValues(String fieldName) {
            name = new String[]{"name","type"};
            value = new String[]{fieldName,"string"};
        }
        String fieldName;
        String[] name = null;
        String[] value = null;
    }


   static private void newLine(StringBuilder builder, int indentLevel) {
        builder.append("\n");
        for (int i=0; i <indentLevel; i++) builder.append(indentString);
    }
    static private String indentString = "    ";

    static private class MenuInstance implements DBDMenu
    {
        public String[] getChoices() {
            return choices;
        }

        public String getName() {
            return menuName;
        }
        
        public String toString() { return getString(0);}

        public String toString(int indentLevel) {
            return getString(indentLevel);
        }

        private String getString(int indentLevel) {
            StringBuilder builder = new StringBuilder();
            newLine(builder,indentLevel);
            builder.append(String.format("menu %s { ",menuName));
            for(String value: choices) {
                builder.append(String.format("\"%s\" ",value));
            }
            builder.append("}");
            return builder.toString();
        }


        MenuInstance(String menuName, String[] choices)
        {
            this.menuName = menuName;
            this.choices = choices;
        } 

        private String menuName;
        private String[] choices;

    }


    static private class StructureInstance implements DBDStructure
    {

        public DBDAttribute getAttribute() {
            return null; // structures have no attributes
        }

        public DBType getDBType() {
            return DBType.dbStructure;
        }

        public int getDBDFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public int getFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public DBDField[] getDBDFields() {
            return dbdField;
        }

        public Field getField(String fieldName) {
            return structure.getField(fieldName);
        }

        public String[] getFieldNames() {
            return structure.getFieldNames();
        }

        public Field[] getFields() {
            return structure.getFields();
        }

        public String getStructureName() {
            return structure.getStructureName();
        }

        public String getName() {
            return structure.getName();
        }

        public Property getProperty(String propertyName) {
            return structure.getProperty(propertyName);
        }

        public Property[] getPropertys() {
            return structure.getPropertys();
        }

        public Type getType() {
            return structure.getType();
        }

        public boolean isMutable() {
            return structure.isMutable();
        }

        public void setMutable(boolean value) {
            structure.setMutable(value);
        }

        StructureInstance(String name,
            DBDField[] dbdField,Property[] property)
        {
            structure = FieldFactory.createStructureField(
                name,name,dbdField,property);
            this.dbdField = dbdField;
        }
                
        
        public String toString() { return getString(0);}

        public String toString(int indentLevel) {
            return getString(indentLevel);
        }

        private String getString(int indentLevel) {
            return structure.toString(indentLevel);
        }

        private Structure structure;
        private DBDField[] dbdField;

    }

    static private class RecordTypeInstance implements DBDRecordType
    {

        public int getDBDFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public int getFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public DBDAttribute getAttribute() {
            return null; // record types have no attributes
        }

        public DBType getDBType() {
            return DBType.dbStructure;
        }

        public DBDField[] getDBDFields() {
            return dbdField;
        }

        public Field getField(String fieldName) {
            return structure.getField(fieldName);
        }

        public String[] getFieldNames() {
            return structure.getFieldNames();
        }

        public Field[] getFields() {
            return structure.getFields();
        }

        public String getStructureName() {
            return structure.getStructureName();
        }

        public String getName() {
            return structure.getName();
        }

        public Property getProperty(String propertyName) {
            return structure.getProperty(propertyName);
        }

        public Property[] getPropertys() {
            return structure.getPropertys();
        }

        public Type getType() {
            return structure.getType();
        }

        public boolean isMutable() {
            return structure.isMutable();
        }

        public void setMutable(boolean value) {
            structure.setMutable(value);
        }

        RecordTypeInstance(String name,
            DBDField[] dbdField,Property[] property)
        {
            structure = FieldFactory.createStructureField(
                name,name,dbdField,property);
            this.dbdField = dbdField;
        }
                
        
        public String toString() { return getString(0);}

        public String toString(int indentLevel) {
            return getString(indentLevel);
        }

        private String getString(int indentLevel) {
            return structure.toString(indentLevel);
        }

        private Structure structure;
        private DBDField[] dbdField;

    }
    static private class LinkSupportInstance implements DBDLinkSupport
    {

        public String getConfigStructureName() {
            return configStructureName;
        }

        public String getLinkSupportName() {
            return linkSupportName;
        }

        LinkSupportInstance(String supportName,
            String configStructureName)
        {
            this.configStructureName = configStructureName;
            this.linkSupportName = supportName;
        }
        
        
        public String toString() { return getString(0);}

        public String toString(int indentLevel) {
            return getString(indentLevel);
        }

        private String getString(int indentLevel) {
            StringBuilder builder = new StringBuilder();
            newLine(builder,indentLevel);
            builder.append(String.format(
                    "linkSupportName %s configStructureName %s",
                    linkSupportName,configStructureName));
            return builder.toString();
        }

        private String configStructureName;
        private String linkSupportName;
    }

    static private class FieldInstance extends AbstractDBDField
    {

        FieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property); 
        }
    }
    
    static private class EnumFieldInstance extends AbstractDBDField
        implements DBDEnumField
    {
        public boolean isChoicesMutable() {
            return enumField.isChoicesMutable();
        }

        EnumFieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property);
            enumField = (Enum)field;
        }
        
        private Enum enumField;
    }
    
    static private class MenuFieldInstance extends AbstractDBDField
    implements DBDMenuField
    {
        public boolean isChoicesMutable() {
            return enumField.isChoicesMutable();
        }

        MenuFieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property);
            enumField = (Enum)field;
        }
    
        private Enum enumField;
    }
    
    static private class ArrayFieldInstance extends AbstractDBDField
        implements DBDArrayField
    {
        
        public Type getElementType() {
            return array.getElementType();
        }

        ArrayFieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property);
            array = (Array)field;
        }
        
        private Array array;
        
    }
    
    static private class StructureFieldInstance extends AbstractDBDField
    implements DBDStructureField
    {

        public DBDStructure getDBDStructure() {
            return dbdStructure;
        }

        public int getFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public Field getField(String fieldName) {
            return structure.getField(fieldName);
        }

        public String[] getFieldNames() {
            return structure.getFieldNames();
        }

        public Field[] getFields() {
            return structure.getFields();
        }

        public String getStructureName() {
            return structure.getStructureName();
        }
        
        public String toString() { return getString(0);}

        public String toString(int indentLevel) {
            return getString(indentLevel);
        }

        private String getString(int indentLevel) {
            Property[] structureProperty = dbdStructure.getPropertys();
            if(structureProperty.length<=0) return super.toString(indentLevel);
            StringBuilder builder = new StringBuilder();
            newLine(builder,indentLevel);
            builder.append(String.format("field %s is structure with property {",
                    field.getName()));
            for(Property property : structureProperty) {
                newLine(builder,indentLevel+1);
                builder.append(String.format("{name = %s field = %s}",
                    property.getName(),property.getFieldName()));
            }
            newLine(builder,indentLevel);
            builder.append("}");
            builder.append(super.toString(indentLevel));
            return builder.toString();
        }

        StructureFieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property);
            structure = (Structure)field;
            dbdStructure = attribute.getStructure();
        }
        private Structure structure;
        private DBDStructure dbdStructure;
    }
    
    
    static private class LinkFieldInstance extends AbstractDBDField
        implements DBDLinkField
    {

        public DBDStructure getDBDStructure() {
            return dbdStructure;
        }

        public int getFieldIndex(String fieldName) {
            return structure.getFieldIndex(fieldName);
        }

        public Field getField(String fieldName) {
            return structure.getField(fieldName);
        }

        public String[] getFieldNames() {
            return structure.getFieldNames();
        }

        public Field[] getFields() {
            return structure.getFields();
        }

        public String getStructureName() {
            return structure.getStructureName();
        }

        LinkFieldInstance(DBDAttribute attribute,Property[]property)
        {
            super(attribute,property);
            structure = (Structure)field;
            dbdStructure = attribute.getStructure();
        }
        
        private Structure structure;
        private DBDStructure dbdStructure;
        
    }
    
}
