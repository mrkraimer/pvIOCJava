/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvioc.database;

import org.epics.pvdata.misc.LinkedList;
import org.epics.pvdata.misc.LinkedListCreate;
import org.epics.pvdata.misc.LinkedListNode;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PostHandler;
import org.epics.pvdata.pv.Type;
import org.epics.pvioc.support.Support;

/**
 * @author mrk
 *
 */
public class BasePVRecordField implements PVRecordField, PostHandler{
	private static LinkedListCreate<PVListener> linkedListCreate = new LinkedListCreate<PVListener>();
	private Support support = null;
	private PVField pvField = null;
	private BasePVRecord pvRecord = null;
	private PVRecordStructure parent = null;
	private boolean isStructure = false;
	private LinkedList<PVListener> pvListenerList = linkedListCreate.create();
	private String fullName = null;
	private String fullFieldName = null;

	BasePVRecordField(PVField pvField,PVRecordStructure parent,BasePVRecord pvRecord) {
		this.pvField = pvField;
		this.parent = parent;
		this.pvRecord = pvRecord;
		if(pvField.getField().getType()==Type.structure) isStructure = true;
		pvField.setPostHandler(this);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvioc.database.PVRecordField#getSupport()
	 */
	@Override
	public Support getSupport() {
		return support;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvioc.database.PVRecordField#setSupport(org.epics.pvioc.support.Support)
	 */
	@Override
	public void setSupport(Support support) {
		if(this.support!=null) {
			throw new IllegalStateException("a support has already been assigned to this field");
		}
		this.support = support;
	}
	
	@Override
    public String getRequesterName() {
        return pvRecord.getRecordName();
    }
    @Override
    public void message(String message, MessageType messageType) {
        pvRecord.message(getFullName() + " " + message, messageType);
    }
    @Override
	public PVRecordStructure getParent() {
		return parent;
	}
	@Override
	public String getFullFieldName() {
		if(fullFieldName==null) createNames();
		return fullFieldName;
	}
	@Override
	public String getFullName() {
		if(fullName==null) createNames();
		return fullName;
	}
	@Override
	public PVField getPVField() {
		return pvField;
	}

//	@Override
//	public void replacePVField(PVField newPVField) {
//	    String message = "";
//	    boolean ok = true;
//	    if(newPVField.getField().getType()==Type.structure) {
//	        ok = false;
//	        message += " illegal attempt to replace a structure field";
//	    }
//	    if(newPVField.getField()!=pvField.getField()) {
//	        ok = false;
//	        message += " attempt to replace a field but introspection interface not the same";
//	    }
//	    if(!ok) {
//	        throw new IllegalStateException(getFullName() + message);
//	    }
//	    parent.getPVStructure().replacePVField(pvField,newPVField);
//		pvField = newPVField;
//		pvField.setPostHandler(this);
//	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVField#getPVRecord()
	 */
	@Override
	public PVRecord getPVRecord() {
		return pvRecord;
	}
//	/* (non-Javadoc)
//	 * @see org.epics.pvioc.database.PVRecordField#renameField(java.lang.String)
//	 */
//	@Override
//	public void renameField(String newName) {
//		pvField.renameField(newName);
//		createNames();
//	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVField#addListener(org.epics.pvdata.pv.PVListener)
	 */
	@Override
	public boolean addListener(PVListener pvListener) {
		if(!pvRecord.isRegisteredListener(pvListener)) return false;
		if(pvListenerList.contains(pvListener)) return false;
		LinkedListNode<PVListener> listNode = linkedListCreate.createNode(pvListener);
		pvListenerList.addTail(listNode);
		return true;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.PVRecordField#removeListener(org.epics.pvdata.pv.PVListener)
	 */
     @Override
     public void removeListener(PVListener pvListener) {
         pvListenerList.remove(pvListener);
         if(isStructure) {
        	 PVRecordStructure recordStructure = (PVRecordStructure)this;
        	 PVRecordField[] pvRecordFields = recordStructure.getPVRecordFields();
        	 for(PVRecordField pvRecordField: pvRecordFields) {
        		 pvRecordField.removeListener(pvListener);
        	 }
         }
     }
     
     /* (non-Javadoc)
      * @see org.epics.pvdata.pv.PVField#postPut()
      */
     @Override
     public void postPut() {
    	 if(parent!=null) {
    		 BasePVRecordField pvf = (BasePVRecordField)parent;
    		 pvf.postParent(this);
    	 }
    	 postSubField();
     }
      
     private void postParent(PVRecordField subField) {
    	 LinkedListNode<PVListener> listNode = pvListenerList.getHead();
    	 while(listNode!=null) {
    		 PVListener pvListener = listNode.getObject();
             pvListener.dataPut((PVRecordStructure)this,subField);
             listNode = pvListenerList.getNext(listNode);
         }
    	 if(parent!=null) {
    		 BasePVRecordField pv = (BasePVRecordField)parent;
    		 pv.postParent(subField);
    	 }
     }
     
     private void postSubField() {
         callListener();
         if(isStructure) {
             PVRecordStructure recordStructure = (PVRecordStructure)this;
             PVRecordField[] pvRecordFields = recordStructure.getPVRecordFields();
             for(int i=0; i<pvRecordFields.length; i++) {
                 BasePVRecordField pv = (BasePVRecordField)pvRecordFields[i];
                 pv.postSubField();
             }
         }
     }
     
     private void callListener() {
    	 LinkedListNode<PVListener> listNode = pvListenerList.getHead();
    	 while(listNode!=null) {
    		 PVListener pvListener = listNode.getObject();
             pvListener.dataPut(this);
             listNode = pvListenerList.getNext(listNode);
         }
     }
     
     private void createNames(){
    	 StringBuilder builder = new StringBuilder();
    	 PVField pvField = getPVField();
    	 boolean isLeaf = true;
    	 while(pvField!=null) {
    	     String fieldName = pvField.getFieldName();
    	     PVStructure pvParent = pvField.getParent();
    		 if(!isLeaf && pvParent!=null) {
    		     builder.insert(0, '.');
    		 }
    		 isLeaf = false;
    		 builder.insert(0, fieldName);
    		 pvField = pvParent;
    	 }
    	 fullFieldName = builder.toString();
    	 String xxx = pvRecord.getRecordName();
    	 if(fullFieldName.length()>0) xxx += ".";
    	 builder.insert(0, xxx);
    	 fullName = builder.toString();
     }
}
