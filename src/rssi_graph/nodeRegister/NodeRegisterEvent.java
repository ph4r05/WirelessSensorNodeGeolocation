/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ph4r05
 */
public class NodeRegisterEvent {
    public static final int EVENT_TYPE_DATA_CHANGED=1;
    public int eventType=NodeRegisterEvent.EVENT_TYPE_DATA_CHANGED;

    /**
     * Changes map
     * Key = NODEID
     * Value = parameter changed
     */
    public Map<Integer, String> changes=null;

    public NodeRegisterEvent() {
        changes = new HashMap<Integer, String>();
    }

    public Map<Integer, String> getChanges() {
        return changes;
    }

    public void setChanges(Map<Integer, String> changes) {
        this.changes = changes;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
