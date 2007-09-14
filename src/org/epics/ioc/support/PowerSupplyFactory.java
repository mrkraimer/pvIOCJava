/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.support;

import org.epics.ioc.pv.*;
import org.epics.ioc.db.*;
import org.epics.ioc.process.*;
import org.epics.ioc.util.*;

/**
 * Record that holds a double value, an input link, and an array of process or output links.
 * @author mrk
 *
 */
public class PowerSupplyFactory {
    /**
     * Create the support for the record or structure.
     * @param dbStructure The structure or record for which to create support.
     * @return The support instance.
     */
    public static Support create(DBStructure dbStructure) {
        String supportName = dbStructure.getSupportName();
        if(!supportName.equals(powerSupplyCurrentName)) {
            dbStructure.getPVStructure().message("no support for " + supportName, MessageType.fatalError);
            return null;
        }
        // we want the parent of the parent
        DBField parentDBField = dbStructure.getParent();
        if(parentDBField==null) {
            dbStructure.getPVStructure().message("no parent", MessageType.fatalError);
            return null;
        }
        parentDBField = parentDBField.getParent();
        if(parentDBField==null) {
            dbStructure.getPVStructure().message("no parent of the parent", MessageType.fatalError);
            return null;
        }
        PVField parentPVField = parentDBField.getPVField();
        PVField valuePVField = parentPVField.findProperty("current.value");
        if(valuePVField==null) {
            parentPVField.message("current.value does not exist", MessageType.fatalError);
            return null;
        }
        DBField currentDBField = parentDBField.getDBRecord().findDBField(valuePVField);
        valuePVField = parentPVField.findProperty("voltage.value");
        if(valuePVField==null) {
            parentPVField.message("voltage.value does not exist", MessageType.fatalError);
            return null;
        }
        DBField voltageDBField = parentDBField.getDBRecord().findDBField(valuePVField);
        valuePVField = parentPVField.findProperty("power.value");
        if(valuePVField==null) {
            parentPVField.message("power.value does not exist", MessageType.fatalError);
            return null;
        }
        DBField powerDBField = parentDBField.getDBRecord().findDBField(valuePVField);
       
        return new PowerSupplyCurrentImpl(dbStructure,currentDBField,voltageDBField,powerDBField);
    }
    
    private static final String powerSupplyCurrentName = "powerSupplyCurrent";
    
    
    static private class PowerSupplyCurrentImpl extends AbstractSupport
    {
        private DBField powerDBField = null;
        private PVDouble powerPVField = null;
        private DBField currentDBField = null;
        private PVDouble currentPVField = null;
        private DBField voltageDBField = null;
        private PVDouble voltagePVField = null;
        
        private double power;
        private double voltage;
        private double current;
        
        private PowerSupplyCurrentImpl(DBStructure dbStructure,DBField currentDBField, DBField voltageDBField, DBField powerDBField) {
            super(powerSupplyCurrentName,dbStructure);
            this.powerDBField = powerDBField;
            this.currentDBField = currentDBField;
            this.voltageDBField = voltageDBField;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#initialize()
         */
        public void initialize() {
            if(!super.checkSupportState(SupportState.readyForInitialize,powerSupplyCurrentName)) return;
            PVField pvField = powerDBField.getPVField();
            if(pvField.getField().getType()!=Type.pvDouble) {
                super.message("power.value is not type double", MessageType.error);
                return;
            }
            powerPVField = (PVDouble)pvField;
            pvField = currentDBField.getPVField();
            if(pvField.getField().getType()!=Type.pvDouble) {
                super.message("current.value is not type double", MessageType.error);
                return;
            }
            currentPVField = (PVDouble)pvField;
            pvField = voltageDBField.getPVField();
            if(pvField.getField().getType()!=Type.pvDouble) {
                super.message("voltage.value is not type double", MessageType.error);
                return;
            }
            voltagePVField = (PVDouble)pvField;
            setSupportState(SupportState.readyForStart);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#uninitialize()
         */
        public void uninitialize() {
            powerPVField = null;
            currentPVField = null;
            voltagePVField = null;
            setSupportState(SupportState.readyForInitialize);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.process.Support#process(org.epics.ioc.process.RecordProcessRequester)
         */
        public void process(SupportProcessRequester supportProcessRequester) {
            if(!super.checkSupportState(SupportState.ready,"process")) {
                supportProcessRequester.supportProcessDone(RequestResult.failure);
                return;
            }
            power = powerPVField.get();
            voltage = voltagePVField.get();
            if(voltage==0.0) {
                current = 0.0;
            } else {
                current = power/voltage;
            }
            currentPVField.put(current);
            currentDBField.postPut();
            supportProcessRequester.supportProcessDone(RequestResult.success);
        }
    }
}
