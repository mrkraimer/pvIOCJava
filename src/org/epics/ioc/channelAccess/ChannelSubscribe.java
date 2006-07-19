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
public interface ChannelSubscribe {
    void destroy();
    void start(ChannelFieldGroup fieldGroup,ChannelNotifyListener listener,Event why);
    void start(ChannelFieldGroup fieldGroup,ChannelDataListener listener,Event why);
    void stop();
}
