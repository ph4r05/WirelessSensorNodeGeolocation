/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

/**
 * Event information object for message delivery guarantor.
 * Informs about specific conditions (message sent, re-sent, failed)
 * 
 * @author ph4r05
 */
public class MessageDeliveryEvent {
    public final static int STATE_SENT_OK=1;
    public final static int STATE_DEAD=2;

    public int state;
    public int uniqueId;
    public String listenerKey;
    public MessageToSend msgToSend;
    public MessageDeliveryGuarantorWatcher watcher;

    public MessageDeliveryEvent(int state) {
        this.state = state;
    }

    public String getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(String listenerKey) {
        this.listenerKey = listenerKey;
    }

    public MessageToSend getMsgToSend() {
        return msgToSend;
    }

    public void setMsgToSend(MessageToSend msgToSend) {
        this.msgToSend = msgToSend;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public MessageDeliveryGuarantorWatcher getWatcher() {
        return watcher;
    }

    public void setWatcher(MessageDeliveryGuarantorWatcher watcher) {
        this.watcher = watcher;
    }
}
