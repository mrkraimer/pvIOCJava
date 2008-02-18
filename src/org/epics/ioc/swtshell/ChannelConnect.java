/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.swtshell;

import org.eclipse.swt.widgets.Composite;
import org.epics.ioc.ca.Channel;

/**
 * A set of controls for connecting to a channel.
 * @author mrk
 *
 */
public interface ChannelConnect {
    /**
     * Create all the widgets for connecting to a channel.
     * @param parentWidget The parentWidget.
     * @param field Also get field
     * @param propertys Also get properties.
     */
    void createWidgets(Composite parentWidget,boolean field,boolean propertys);
    /**
     * Get the channel
     * @return the channel of null if not connected to a channel.
     */
    Channel getChannel();
}