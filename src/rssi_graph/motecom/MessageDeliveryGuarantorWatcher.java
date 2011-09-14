/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

import net.tinyos.message.MessageListener;

/**
 *
 * @author ph4r05
 */
public interface MessageDeliveryGuarantorWatcher extends MessageListener, MessageSentListener {
    /**
     * Check TTL on timer fire
     * Width of time intervals of this timer firing are not guaranteed
     */
    public void timerFired(MessageDeliveryGuarantor guarantor);

    /**
     * IF true then message is acknowledge and service can release resources for this
     * @return
     */
    public boolean isAcknowledged();

    /**
     * if TRUE then node is absolutely expired
     * @return
     */
    public boolean isDead();

    public int getUniqueId();
    
    public MessageDeliveryGuarantor getGuarantor();
    public void setGuarantor(MessageDeliveryGuarantor guarantor);

    public MessageToSend getMsgToSend();
    public void setMsgToSend(MessageToSend msgToSend);

    public MessageDeliveryEventListener getListener();
    public void setListener(MessageDeliveryEventListener listener);

    public String getListenerKey();
    public void setListenerKey(String listenerKey);

    public int getDestination();
}
