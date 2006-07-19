/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ioc.channelAccess;

import org.epics.ioc.dbProcess.*;
import org.epics.ioc.util.*;

/**
 * @author mrk
 *
 */
public interface ChannelDataProcessListener {
    void processDone(ProcessReturn result,AlarmSeverity alarmSeverity,String status);
    void failure(String reason);
}
