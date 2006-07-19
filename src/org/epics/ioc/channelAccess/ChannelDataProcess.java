/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.channelAccess;

/**
 * @author mrk
 *
 */
public interface ChannelDataProcess {
    void destroy();
    void process(ChannelFieldGroup fieldGroup,ChannelDataProcessListener callback, boolean wait);
    void cancelProcess();
}
