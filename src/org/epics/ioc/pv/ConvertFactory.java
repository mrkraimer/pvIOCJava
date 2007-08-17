/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.pv;

/**
 * Factory to obtain the implementation of <i>Convert</i>
 * @author mrktestByteArrayCopy
 *
 */
public final class ConvertFactory {
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    /**
     * Implements <i>Convert</i>.
     * The implementation ensures that a single instance is created.
     * @return the implementation of <i>Convert</i>
     */
    public static Convert getConvert()
    {
    	return ImplementConvert.getConvert();
    }

    private static final class ImplementConvert implements Convert{
        private static ImplementConvert singleImplementation = null;
        private static synchronized ImplementConvert getConvert() {
                if (singleImplementation==null) {
                    singleImplementation = new ImplementConvert();
                }
                return singleImplementation;
        }
        // Guarantee that ImplementConvert can only be created via getConvert
        private ImplementConvert() {}
        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#getString(org.epics.ioc.pv.PVField)
         */
        public String getString(PVField pv) {
            return ConvertToString(pv,0);
        }
    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#getString(org.epics.ioc.pv.PVField, int)
         */
        public String getString(PVField pv,int indentLevel) {
            return ConvertToString(pv,indentLevel);
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromString(org.epics.ioc.pv.PVField, java.lang.String)
         */
        public void fromString(PVField pv, String from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvBoolean: {
                        PVBoolean value = (PVBoolean)pv;
                        value.put(Boolean.parseBoolean(from));
                        return;
                    }
                case pvByte : {
                        PVByte value = (PVByte)pv;
                        value.put(Byte.decode(from));
                        return;
                    }
                case pvShort : {
                        PVShort value = (PVShort)pv;
                        value.put(Short.decode(from));
                        return;
                    }
                case pvInt : {
                        PVInt value = (PVInt)pv;
                        value.put(Integer.decode(from));
                        return;
                    }
                case pvLong : {
                        PVLong value = (PVLong)pv;
                        value.put(Long.decode(from));
                        return;
                    }
                case pvFloat : {
                        PVFloat value = (PVFloat)pv;
                        value.put(Float.valueOf(from));
                        return;
                    }
                case pvDouble : {
                        PVDouble value = (PVDouble)pv;
                        value.put(Double.valueOf(from));
                        return;
                    }
                case pvString: {
                        PVString value = (PVString)pv;
                        value.put(from);
                        return;
                    }
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
            
        }       
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromStringArray(org.epics.ioc.pv.PVArray, int, int, java.lang.String[], int)
         */
        public int fromStringArray(PVArray pv, int offset, int len,
            String[] from, int fromOffset)
        {
            return ConvertFromStringArray(pv,offset,len,from,fromOffset);
        }       
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toStringArray(org.epics.ioc.pv.PVArray, int, int, java.lang.String[], int)
         */
        public int toStringArray(PVArray pv, int offset, int len, String[] to, int toOffset) {
            return ConvertToStringArray(pv,offset,len,to,toOffset);
        }        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#isCopyScalarCompatible(org.epics.ioc.pv.Field, org.epics.ioc.pv.Field)
         */
        public boolean isCopyScalarCompatible(Field fromField, Field toField) {
            Type fromType = fromField.getType();
            Type toType = toField.getType();
            if(!fromType.isScalar() || !toType.isScalar()) return false;
            if(fromType.isNumeric() && toType.isNumeric()) return true;
            if(fromType==toType) return true;
            if(fromType==Type.pvString) return true;
            if(toType==Type.pvString) return true;
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#copyScalar(org.epics.ioc.pv.PVField, org.epics.ioc.pv.PVField)
         */
        public void copyScalar(PVField from, PVField to) {
            Field field = from.getField();
            Type type = field.getType();
            switch(type) {
            case pvBoolean: {
                    Type toType = to.getField().getType();
                    if(toType!=Type.pvBoolean) {
                        if(toType!=Type.pvString) break;
                    }
                    PVBoolean data = (PVBoolean)from;
                    boolean value = data.get();
                    if(toType==Type.pvString) {
                        PVString dataTo = (PVString)to;
                        dataTo.put(((Boolean)value).toString());
                    } else {
                        PVBoolean dataTo = (PVBoolean)to;
                        dataTo.put(value);
                    }
                    return;
                }
            case pvByte : {
                    PVByte data = (PVByte)from;
                    byte value = data.get();
                    fromByte(to,value);
                    return;
                }
            case pvShort : {
                    PVShort data = (PVShort)from;
                    short value = data.get();
                    fromShort(to,value);
                    return;
                } 
            case pvInt :{
                    PVInt data = (PVInt)from;
                    int value = data.get();
                    fromInt(to,value);
                    return;
                }    
            case pvLong : {
                    PVLong data = (PVLong)from;
                    long value = data.get();
                    fromLong(to,value);
                    return;
                }  
            case pvFloat : {
                    PVFloat data = (PVFloat)from;
                    float value = data.get();
                    fromFloat(to,value);
                    return;
                }     
            case pvDouble : {
                    PVDouble data = (PVDouble)from;
                    double value = data.get();
                    fromDouble(to,value);
                    return;
                }  
            case pvString: {
                    PVString data = (PVString)from;
                    String value = data.get();
                    fromString(to,value);
                    return;
                }
            default:
            }
            throw new IllegalArgumentException(
                    "Convert.copyScalar arguments are not compatible"
                  );
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#isCopyArrayCompatible(org.epics.ioc.pv.Array, org.epics.ioc.pv.Array)
         */
        public boolean isCopyArrayCompatible(Array fromArray, Array toArray) {
            Type fromType = fromArray.getElementType();
            Type toType = toArray.getElementType();
            if(fromType==toType) return true;
            if(!fromType.isScalar() || !toType.isScalar()) return false;
            if(fromType.isNumeric() && toType.isNumeric()) return true;
            if(toType==Type.pvString) return true;
            if(fromType==Type.pvString) return true;
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#copyArray(org.epics.ioc.pv.PVArray, int, org.epics.ioc.pv.PVArray, int, int)
         */
        public int copyArray(PVArray from, int offset, PVArray to, int toOffset, int len)
        {
            Type fromElementType = ((Array)from.getField()).getElementType();
            Type toElementType = ((Array)to.getField()).getElementType();
            if(fromElementType==toElementType) {
                switch(fromElementType) {
                case pvArray: {
                        PVArrayArray fromArray = (PVArrayArray)from;
                        PVArrayArray toArray = (PVArrayArray)to;
                        ArrayArrayData arrayData = new ArrayArrayData();
                        int fromLength = fromArray.get(offset, len, arrayData);
                        PVArray[] fromDatas = arrayData.data;
                        int toLength = toArray.get(toOffset, len, arrayData);
                        PVArray[] toDatas = null;
                        if(toLength>0) {
                            toDatas = arrayData.data;
                        } else {
                            toDatas = new PVArray[fromLength];
                            for(int i=0; i<toLength; i++) toDatas[i] = null;
                        }
                        for(int i=0; i<fromLength; i++) {
                            PVArray fromData = fromDatas[i];
                            if(fromData==null) continue;
                            PVArray toData = null;
                            if(i<toLength) toData = toDatas[i];
                            if(toData==null) {
                                toData = (PVArray)pvDataCreate.createPVArray(
                                        to, fromData.getField(),fromData.getLength(), true);
                                toDatas[0] = toData;
                                toArray.put(0, 1, toDatas, 0);
                                
                            }
                            if(isCopyArrayCompatible((Array)fromData.getField(),(Array)toData.getField())) {
                                copyArray(fromData,0,toData,0,fromData.getLength());
                            }
                        }
                        return fromLength;
                    }
                case pvStructure: {
                        PVStructureArray fromArray = (PVStructureArray)from;
                        PVStructureArray toArray = (PVStructureArray)to;
                        StructureArrayData arrayData = new StructureArrayData();
                        int fromLength = fromArray.get(offset, len, arrayData);
                        PVStructure[] fromDatas = arrayData.data;
                        int toLength = toArray.get(toOffset, len, arrayData);
                        PVStructure[] toDatas = null;
                        if(toLength>0) {
                            toDatas = arrayData.data;
                        } else {
                            toDatas = new PVStructure[fromLength];
                            for(int i=0; i<toLength; i++) toDatas[i] = null;
                        }
                        for(int i=0; i<fromLength; i++) {
                            PVStructure fromData = fromDatas[i];
                            if(fromData==null) continue;
                            PVStructure toData = null;
                            if(i<toLength) toData = toDatas[i];
                            if(toData==null) {
                                toData = (PVStructure)pvDataCreate.createPVField(to, fromData.getField());
                                toDatas[0] = toData;
                                toArray.put(0, 1, toDatas, 0);
                                
                            }
                            if(isCopyStructureCompatible((Structure)fromData.getField(),(Structure)toData.getField())) {
                                copyStructure(fromData,toData);
                            }
                        }
                        return fromLength;
                    }
                default: break;
                }
                // just fall through
            }
            if(!fromElementType.isScalar() || !toElementType.isScalar()) return 0;
            if(toElementType.isNumeric() && fromElementType.isNumeric())
                return CopyNumericArray(from,offset,to,toOffset,len);
            if(toElementType==Type.pvBoolean && fromElementType==Type.pvBoolean) {
                PVBooleanArray pvfrom = (PVBooleanArray)from;
                PVBooleanArray pvto = (PVBooleanArray)to;
                BooleanArrayData data = new BooleanArrayData();
                int ncopy = 0;
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = pvto.put(toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                return ncopy;
            }
            if(toElementType==Type.pvString) {
                PVStringArray pvto = (PVStringArray)to;
                int ncopy = from.getLength();
                if(ncopy>len) ncopy = len;
                int num = ncopy;
                String[] toData = new String[1];
                while(num>0) {
                    toStringArray(from,offset,1,toData,0);
                    pvto.put(toOffset,1,toData,0);
                    num--; offset++; toOffset++;
                }
                return ncopy;
            }
            if(fromElementType==Type.pvString && toElementType.isScalar()) {
                PVStringArray pvfrom = (PVStringArray)from;
                StringArrayData data = new StringArrayData();
                int ncopy = 0;
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromStringArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                return ncopy;
            }
            return 0;
        }        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#isCopyStructureCompatible(org.epics.ioc.pv.Structure, org.epics.ioc.pv.Structure)
         */
        public boolean isCopyStructureCompatible(Structure from, Structure to) {
            Field[] fromFields = from.getFields();
            Field[] toFields = to.getFields();
            int length = fromFields.length;
            if(length!=toFields.length) return false;
            for(int i=0; i<length; i++) {
                if(fromFields[i].getType()!=toFields[i].getType()) return false;
            }
            return true;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#copyStructure(org.epics.ioc.pv.PVStructure, org.epics.ioc.pv.PVStructure)
         */
        public void copyStructure(PVStructure from, PVStructure to) {
            Structure fromStructure = (Structure)from.getField();
            Structure toStructure = (Structure)to.getField();
            if(!isCopyStructureCompatible(fromStructure,toStructure)) {
                throw new IllegalArgumentException(
                    "Convert.copyStructure from and to are not the same type of structure");
            }
            PVField[] fromDatas = from.getFieldPVFields();
            PVField[] toDatas = to.getFieldPVFields();
            for(int i=0; i < fromDatas.length; i++) {
                PVField fromField = fromDatas[i];
                PVField toField = toDatas[i];
                Type type = fromField.getField().getType();
                if(type.isScalar()) {
                    copyScalar(fromField,toField);
                } else if(type==Type.pvArray) {
                    Array fromElementArray = (Array)fromField.getField();
                    Array toElementArray = (Array)toField.getField();
                    if(isCopyArrayCompatible(fromElementArray,toElementArray)) {
                        int len = ((PVArray)fromField).getLength();
                        copyArray((PVArray)fromField,0,(PVArray)toField,0,len);
                    }
                } else if(type==Type.pvStructure) {
                    Structure fromElementStructure = (Structure)fromField.getField();
                    Structure toElementStructure = (Structure)toField.getField();
                    if(isCopyStructureCompatible(fromElementStructure,toElementStructure))
                        copyStructure((PVStructure)fromField,(PVStructure)toField);
                }
            }
        }        
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromByte(org.epics.ioc.pv.PVField, byte)
         */
        public void fromByte(PVField pv, byte from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromByteArray(org.epics.ioc.pv.PVField, int, int, byte[], int)
         */
        public int fromByteArray(PVField pv, int offset, int len,
            byte[] from, int fromOffset) {
            return ConvertByteArrayFrom(pv,offset,len,from,fromOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromDouble(org.epics.ioc.pv.PVField, double)
         */
        public void fromDouble(PVField pv, double from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromDoubleArray(org.epics.ioc.pv.PVField, int, int, double[], int)
         */
        public int fromDoubleArray(PVField pv, int offset, int len,
            double[] from, int fromOffset) {
            return ConvertDoubleArrayFrom(pv,offset,len,from,fromOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromFloat(org.epics.ioc.pv.PVField, float)
         */
        public void fromFloat(PVField pv, float from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromFloatArray(org.epics.ioc.pv.PVField, int, int, float[], int)
         */
        public int fromFloatArray(PVField pv, int offset, int len,
            float[] from, int fromOffset) {
            return ConvertFloatArrayFrom(pv,offset,len,from,fromOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromInt(org.epics.ioc.pv.PVField, int)
         */
        public void fromInt(PVField pv, int from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromIntArray(org.epics.ioc.pv.PVField, int, int, int[], int)
         */
        public int fromIntArray(PVField pv, int offset, int len,
            int[] from, int fromOffset) {
            return ConvertIntArrayFrom(pv,offset,len,from,fromOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromLong(org.epics.ioc.pv.PVField, long)
         */
        public void fromLong(PVField pv, long from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromLongArray(org.epics.ioc.pv.PVField, int, int, long[], int)
         */
        public int fromLongArray(PVField pv, int offset, int len,
            long[] from, int fromOffset) {
            return ConvertLongArrayFrom(pv,offset,len,from,fromOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromShort(org.epics.ioc.pv.PVField, short)
         */
        public void fromShort(PVField pv, short from) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; value.put((byte)from); return;}
                case pvShort :
                    {PVShort value = (PVShort)pv; value.put((short)from); return;}
                case pvInt :
                    {PVInt value = (PVInt)pv; value.put((int)from); return;}
                case pvLong :
                    {PVLong value = (PVLong)pv; value.put((long)from); return;}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; value.put((float)from); return;}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; value.put((double)from); return;}
                case pvString :
                    {PVString value = (PVString)pv; value.put(String.valueOf(from)); return;}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#fromShortArray(org.epics.ioc.pv.PVField, int, int, short[], int)
         */
        public int fromShortArray(PVField pv, int offset, int len,
            short[] from, int fromOffset) {
            return ConvertShortArrayFrom(pv,offset,len,from,fromOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toByte(org.epics.ioc.pv.PVField)
         */
        public byte toByte(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (byte)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (byte)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (byte)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (byte)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (byte)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (byte)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toByteArray(org.epics.ioc.pv.PVField, int, int, byte[], int)
         */
        public int toByteArray(PVField pv, int offset, int len,
            byte[] to, int toOffset) {
        	return ConvertByteArrayTo(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toDouble(org.epics.ioc.pv.PVField)
         */
        public double toDouble(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (double)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (double)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (double)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (double)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (double)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (double)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toDoubleArray(org.epics.ioc.pv.PVField, int, int, double[], int)
         */
        public int toDoubleArray(PVField pv, int offset, int len,
            double[] to, int toOffset) {
        	return ConvertDoubleArrayTo(pv,offset,len,to,toOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toFloat(org.epics.ioc.pv.PVField)
         */
        public float toFloat(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (float)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (float)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (float)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (float)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (float)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (float)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toFloatArray(org.epics.ioc.pv.PVField, int, int, float[], int)
         */
        public int toFloatArray(PVField pv, int offset, int len,
            float[] to, int toOffset) {
        	return ConvertFloatArrayTo(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toInt(org.epics.ioc.pv.PVField)
         */
        public int toInt(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (int)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (int)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (int)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (int)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (int)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (int)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toIntArray(org.epics.ioc.pv.PVField, int, int, int[], int)
         */
        public int toIntArray(PVField pv, int offset, int len,
            int[] to, int toOffset) {
        	return ConvertIntArrayTo(pv,offset,len,to,toOffset);
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toLong(org.epics.ioc.pv.PVField)
         */
        public long toLong(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (long)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (long)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (long)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (long)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (long)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (long)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toLongArray(org.epics.ioc.pv.PVField, int, int, long[], int)
         */
        public int toLongArray(PVField pv, int offset, int len,
            long[] to, int toOffset) {
        	return ConvertLongArrayTo(pv,offset,len,to,toOffset);
        }   
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toShort(org.epics.ioc.pv.PVField)
         */
        public short toShort(PVField pv) {
            Field field = pv.getField();
            Type type = field.getType();
            switch(type) {
                case pvByte :
                    {PVByte value = (PVByte)pv; return (short)value.get();}
                case pvShort :
                    {PVShort value = (PVShort)pv; return (short)value.get();}
                case pvInt :
                    {PVInt value = (PVInt)pv; return (short)value.get();}
                case pvLong :
                    {PVLong value = (PVLong)pv; return (short)value.get();}
                case pvFloat :
                    {PVFloat value = (PVFloat)pv; return (short)value.get();}
                case pvDouble :
                    {PVDouble value = (PVDouble)pv; return (short)value.get();}
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#toShortArray(org.epics.ioc.pv.PVField, int, int, short[], int)
         */
        public int toShortArray(PVField pv, int offset, int len,
            short[] to, int toOffset) {
        	return ConvertShortArrayTo(pv,offset,len,to,toOffset);
        }
    
    
        private int ConvertByteArrayFrom(PVField pv, int offset, int len,
        byte[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n; ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertByteArrayTo(PVField pv, int offset, int len,
        byte[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (byte)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertShortArrayFrom(PVField pv, int offset, int len,
        short[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertShortArrayTo(PVField pv, int offset, int len,
        short[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (short)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertIntArrayFrom(PVField pv, int offset, int len,
            int[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertIntArrayTo(PVField pv, int offset, int len,
            int[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (int)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertLongArrayFrom(PVField pv, int offset, int len,
        long[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertLongArrayTo(PVField pv, int offset, int len,
        long[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (long)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertFloatArrayFrom(PVField pv, int offset, int len,
        float[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[] data = new double[1];
                    while(len>0) {
                        data[0] = (double)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertFloatArrayTo(PVField pv, int offset, int len,
        float[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (float)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertDoubleArrayFrom(PVField pv, int offset, int len,
        double[]from, int fromOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = (byte)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = (short)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = (int)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = (long)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = (float)from[fromOffset];
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n;  ntransfered += n;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
    
        private int ConvertDoubleArrayTo(PVField pv, int offset, int len,
        double[]to, int toOffset)
        {
            Field field = pv.getField();
            Type type = field.getType();
            if(type!=Type.pvArray) throw new IllegalArgumentException(
                "Illegal PVType. Must be array but it is " + type.toString());
            Array array = (Array)field;
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        byte[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        short[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        int[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        long[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        float[] dataArray = data.data;
                        int dataOffset = data.offset;
                        for(int i=0; i<num; i++)
                            to[i+toOffset] = (double)dataArray[i+dataOffset];
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    while(len>0) {
                        int num = pvdata.get(offset,len,data);
                        if(num==0) break;
                        double[] dataArray = data.data;
                        int dataOffset = data.offset;
                        System.arraycopy(dataArray,dataOffset,to,toOffset,num);
                        len -= num; offset += num; toOffset += num; ntransfered += num;
                    }
                    return ntransfered;
                } 
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be numeric but it is "
                      + type.toString()
                    );
            }
        }
        
        private int ConvertFromStringArray(PVArray pv, int offset, int len,
                String[]from, int fromOffset)
        {
            Array array = (Array)pv.getField();
            Type elemType = array.getElementType();
            int ntransfered = 0;
            switch(elemType) {
                case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    boolean[] data = new boolean[1];
                    while(len>0) {
                        data[0] = Boolean.parseBoolean(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                }
                case pvByte : {
                    PVByteArray pvdata = (PVByteArray)pv;
                    byte[] data = new byte[1];
                    while(len>0) {
                        data[0] = Byte.decode(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvShort : {
                    PVShortArray pvdata = (PVShortArray)pv;
                    short[] data = new short[1];
                    while(len>0) {
                        data[0] = Short.decode(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvInt : {
                    PVIntArray pvdata = (PVIntArray)pv;
                    int[] data = new int[1];
                    while(len>0) {
                        data[0] = Integer.decode(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvLong : {
                    PVLongArray pvdata = (PVLongArray)pv;
                    long[] data = new long[1];
                    while(len>0) {
                        data[0] = Long.decode(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvFloat : {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    float[] data = new float[1];
                    while(len>0) {
                        data[0] = Float.valueOf(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvDouble : {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    double[]data = new double[1];
                    while(len>0) {
                        data[0] = Double.valueOf(from[fromOffset]);
                        if(pvdata.put(offset,1,data,0)==0) return ntransfered;
                        --len; ++ntransfered; ++offset; ++fromOffset;
                    }
                    return ntransfered;
                } 
                case pvString:
                    PVStringArray pvdata = (PVStringArray)pv;
                    while(len>0) {
                        int n = pvdata.put(offset,len,from,fromOffset);
                        if(n==0) break;
                        len -= n; offset += n; fromOffset += n; ntransfered += n;
                    }
                    return ntransfered;
                default:
                    throw new IllegalArgumentException(
                      "Illegal PVType. Must be scalar but it is "
                      + elemType.toString()
                    );
            }
        }
        
        private int ConvertToStringArray(PVArray pv, int offset, int len,
                String[]to, int toOffset)
        {
            Array array = (Array)pv.getField();
            Type elementType = array.getElementType();
            int ncopy = pv.getLength();
            if(ncopy>len) ncopy = len;
            int num = ncopy;
            switch(elementType) {
            case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    BooleanArrayData data = new BooleanArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            boolean[] dataArray = data.data;
                            Boolean value = new Boolean(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvByte: {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            byte[] dataArray = data.data;
                            Byte value = new Byte(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvShort: {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            short[] dataArray = data.data;
                            Short value = new Short(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvInt: {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            int[] dataArray = data.data;
                            Integer value = new Integer(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvLong: {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            long[] dataArray = data.data;
                            Long value = new Long(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvFloat: {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            float[] dataArray = data.data;
                            Float value = new Float(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvDouble: {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            double[] dataArray = data.data;
                            Double value = new Double(dataArray[data.offset]);
                            to[toOffset+i] = value.toString();
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvString: {
                    PVStringArray pvdata = (PVStringArray)pv;
                    StringArrayData data = new StringArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            String[] dataArray = data.data;
                            to[toOffset+i] = dataArray[data.offset];
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvStructure: {
                    PVStructureArray pvdata = (PVStructureArray)pv;
                    StructureArrayData data = new StructureArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            PVStructure[] dataArray = data.data;
                            if(dataArray==null
                            || (dataArray.length<=data.offset)
                            || dataArray[data.offset]==null) {
                                to[toOffset+i] = "null";
                            } else {
                                to[toOffset+i] = dataArray[data.offset].toString();
                            }
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            case pvArray: {
                    PVArrayArray pvdata = (PVArrayArray)pv;
                    ArrayArrayData data = new ArrayArrayData();
                    for(int i=0; i<num; i++) {
                        if(pvdata.get(offset+i,1,data)==1) {
                            PVArray[] dataArray = data.data;
                            if(dataArray==null
                            || (dataArray.length<=data.offset)
                            || dataArray[data.offset]==null) {
                                to[toOffset+i] = "null";
                            } else {
                                to[toOffset+i] = dataArray[data.offset].toString();
                            }
                        } else {
                            to[toOffset+i] = "bad pv";
                        }
                    }
                }
                break;
            default:    
                throw new IllegalArgumentException(
                        "Illegal PVType. Must be scalar but it is "
                        + elementType.toString()
                      );
            }
            return ncopy;
        }    
        /* (non-Javadoc)
         * @see org.epics.ioc.pv.Convert#newLine(java.lang.StringBuilder, int)
         */
        public void newLine(StringBuilder builder, int indentLevel) {
            builder.append(String.format("%n"));
            for (int i=0; i <indentLevel; i++) builder.append(indentString);
        }
        private static String indentString = "    ";

        private String ConvertToString(PVField pv,int indentLevel) {
            Field field = pv.getField();
            switch(field.getType()) {
            case pvBoolean: {
                    PVBoolean data = (PVBoolean)pv;
                    boolean value = data.get();
                    if(value) {
                        return "true";
                    } else {
                        return "false";
                    }
                }
            case pvByte: {
                    PVByte data = (PVByte)pv;
                    return String.format("%d",data.get());
                }
            case pvShort: {
                    PVShort data = (PVShort)pv;
                    return String.format("%d",data.get());
                }
            case pvInt: {
                    PVInt data = (PVInt)pv;
                    return String.format("%d",data.get());
                }
            case pvLong: {
                    PVLong data = (PVLong)pv;
                    return String.format("%d",data.get());
                }
            case pvFloat: {
                    PVFloat data = (PVFloat)pv;
                    return String.format("%f",data.get());
                }
            case pvDouble: {
                    PVDouble data = (PVDouble)pv;
                    return String.format("%f",data.get());
                }
            case pvString: {
                    PVString data = (PVString)pv;
                    return data.get();
                }
            case pvStructure: return convertStructure(pv,indentLevel);
            case pvArray: return convertArray(pv,indentLevel);
            default:
                return "unknown PVType";
            }
        }
    
        private String convertStructure(PVField pv,int indentLevel) {
            PVStructure data = (PVStructure)pv;
            Structure structure = (Structure)pv.getField();
            StringBuilder builder = new StringBuilder();
            newLine(builder,indentLevel);
            builder.append(String.format("structure %s{",
                structure.getStructureName()));
            PVField[] fieldsData = data.getFieldPVFields();
            if(fieldsData!=null) for(PVField fieldField : fieldsData) {
                Field fieldnow = fieldField.getField();
                newLine(builder,indentLevel+1);
                builder.append(String.format("%s = ", fieldnow.getFieldName()));
                builder.append(ConvertToString(fieldField,indentLevel+1));
            }
            newLine(builder,indentLevel);
            builder.append("}");
            return builder.toString();
        }
    
        private String convertArray(PVField pv,int indentLevel) {
            Array array = (Array)pv.getField();
            Type type = array.getElementType();
            StringBuilder builder = new StringBuilder();
            switch(type) {
            case pvBoolean: {
                    PVBooleanArray pvdata = (PVBooleanArray)pv;
                    BooleanArrayData data = new BooleanArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             boolean[] value = data.data;
                             if(value[data.offset]) {
                                 builder.append("true ");
                             } else {
                                 builder.append("false ");
                             }
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvByte: {
                    PVByteArray pvdata = (PVByteArray)pv;
                    ByteArrayData data = new ByteArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             byte[] value = data.data;
                             builder.append(String.format("%d ",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvShort: {
                    PVShortArray pvdata = (PVShortArray)pv;
                    ShortArrayData data = new ShortArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             short[] value = data.data;
                             builder.append(String.format("%d ",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvInt: {
                    PVIntArray pvdata = (PVIntArray)pv;
                    IntArrayData data = new IntArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             int[] value = data.data;
                             builder.append(String.format("%d ",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvLong: {
                    PVLongArray pvdata = (PVLongArray)pv;
                    LongArrayData data = new LongArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             long[] value = data.data;
                             builder.append(String.format("%d ",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvFloat: {
                    PVFloatArray pvdata = (PVFloatArray)pv;
                    FloatArrayData data = new FloatArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             float[] value = data.data;
                             builder.append(String.format("%f ",value[data.offset]));
                        } else {
                             builder.append(indentString + "???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvDouble: {
                    PVDoubleArray pvdata = (PVDoubleArray)pv;
                    DoubleArrayData data = new DoubleArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        if(num==1) {
                             double[] value = data.data;
                             builder.append(String.format("%f ",value[data.offset]));
                        } else {
                             builder.append("???? ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvString: {
                    PVStringArray pvdata = (PVStringArray)pv;
                    StringArrayData data = new StringArrayData();
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        String[] value = data.data;
                        if(num==1 && value[data.offset]!=null) {
                            builder.append("\"");
                            builder.append(value[data.offset]);
                            builder.append("\" ");
                        } else {
                             builder.append("null ");
                        }
                    }
                    builder.append("}");
                    break;
                }
            case pvStructure: {
                    PVStructureArray pvdata = (PVStructureArray)pv;
                    StructureArrayData data = new StructureArrayData();
                    newLine(builder,indentLevel);
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        int num = pvdata.get(i,1,data);
                        PVStructure[] value = data.data;
                        if(num==1 && value[data.offset]!=null) {
                            builder.append(convertStructure(value[data.offset],
                                indentLevel + 1));
                        } else {
                             newLine(builder,indentLevel + 1);
                             builder.append("null ");
                        }
                    }
                    newLine(builder,indentLevel);
                    builder.append("}");
                    break;
                }
            case pvArray: {
                    PVArrayArray pvdata = (PVArrayArray)pv;
                    ArrayArrayData data = new ArrayArrayData();
                    newLine(builder,indentLevel);
                    builder.append("{");
                    for(int i=0; i < pvdata.getLength(); i++) {
                        newLine(builder,indentLevel);
                        builder.append(indentString);
                        int num = pvdata.get(i,1,data);
                        PVArray[] value = data.data;
                        if(num==1 && value[data.offset]!=null) {
                            builder.append(convertArray(value[data.offset],
                                indentLevel + 1));
                        } else {
                             builder.append("{}");
                        }
                    }
                    newLine(builder,indentLevel);
                    builder.append("}");
                    break;
                }
            default:
                builder.append(" array element is unknown PVType");
            }
            return builder.toString();
        }

        private int CopyNumericArray(PVArray from, int offset, PVArray to, int toOffset, int len)
        {
            Type fromElementType = ((Array)from.getField()).getElementType();
            int ncopy = 0;
            switch(fromElementType) {
            case pvByte: {
                    PVByteArray pvfrom = (PVByteArray)from;
                    ByteArrayData data = new ByteArrayData();
                    while(len>0) {
                        int num = pvfrom.get(offset,len,data);
                        if(num<=0) break;
                        while(num>0) {
                            int n = fromByteArray(to,toOffset,num,data.data,data.offset);
                            if(n<=0) break;
                            len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                        }
                    }
                    break;
                }
            case pvShort: {
                PVShortArray pvfrom = (PVShortArray)from;
                ShortArrayData data = new ShortArrayData();
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromShortArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                break;
                
                }
            case pvInt: {
                PVIntArray pvfrom = (PVIntArray)from;
                IntArrayData data = new IntArrayData();
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromIntArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                break;
                
                }
            case pvLong: {
                PVLongArray pvfrom = (PVLongArray)from;
                LongArrayData data = new LongArrayData();
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromLongArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                break;
                
                }
            case pvFloat: {
                PVFloatArray pvfrom = (PVFloatArray)from;
                FloatArrayData data = new FloatArrayData();
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromFloatArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                break;
                
                }
            case pvDouble: {
                PVDoubleArray pvfrom = (PVDoubleArray)from;
                DoubleArrayData data = new DoubleArrayData();
                while(len>0) {
                    int num = pvfrom.get(offset,len,data);
                    if(num<=0) break;
                    while(num>0) {
                        int n = fromDoubleArray(to,toOffset,num,data.data,data.offset);
                        if(n<=0) break;
                        len -= n; num -= n; ncopy+=n; offset += n; toOffset += n; 
                    }
                }
                break;
                }
            }
            return ncopy;
        }
        
    }  
}
