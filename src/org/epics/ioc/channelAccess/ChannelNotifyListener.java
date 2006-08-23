/**
 * Copyright - See the COPYRIGHT that is included with this disctibution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.channelAccess;


/**
 * @author mrk
 *
 */
public interface ChannelNotifyListener {
    void beginSynchronous(Channel channel);
    void endSynchronous(Channel channel);
    void reason(Channel channel,Event reason);
    void newData(Channel channel,ChannelField field);
    void failure(Channel channel,String reason);
}
