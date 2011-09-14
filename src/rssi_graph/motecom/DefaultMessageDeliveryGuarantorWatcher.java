/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import rssi_graph.MessageTypes;
import rssi_graph.messages.CommandMsg;

/**
 * Per message object watches signals from guarantor service, keeps track about previous
 * attempts on re-sending, ...
 *
 * @author ph4r05
 */
public class DefaultMessageDeliveryGuarantorWatcher implements MessageDeliveryGuarantorWatcher {

    /**
     * reference to parent guarantor
     */
    private MessageDeliveryGuarantor guarantor=null;

    /**
     * number of milliseconds to wait for ACK after message was sent
     */
    private int ttl=2000;

    /**
     * Maximal re-send retry count
     */
    private int retryCount=3;

    /**
     * Current retry count
     */
    private int curRetryCount=0;

    /**
     * time of message sending
     */
    private long timeSent=0;

    /**
     * TRUE if message was correctly acknowledged
     */
    private boolean acknowledged=false;

    /**
     * dead entry, retry was unsuccessful, maximal retry count reached without
     * success.
     */
    private boolean dead=false;

    /**
     * was passed to send() ?
     */
    private boolean sent=false;

    /**
     * unique acknowledged message id waiting for ack.
     */
    private int uniqueId=0;

    private int destination=65535;

    /**
     * Message to send
     * Prepared for MessageSender, should contain CommandMsg in msgToSend.sMsg
     */
    private MessageToSend msgToSend;

    /**
     * Listener key for MessageDeliveryEventListener (this.listener)
     */
    private String listenerKey;

    /**
     * Change listener
     * Events sent if message is successfully acknowledged or if message failed
     */
    private MessageDeliveryEventListener listener;

    /**
     * If message was successfully sent, this watchers will be added to
     * guarantor to support hierarchy dependency.
     */
    private List<MessageDeliveryGuarantorWatcher> children;

    public DefaultMessageDeliveryGuarantorWatcher(int id) {
        this.uniqueId=id;
        this.children=null;
    }

    /**
     * send message
     */
    public synchronized void send(){
        if (this.guarantor==null) return;
        if (this.guarantor.getMsgSender()==null) return;
        if (this.msgToSend==null) return;

        // reset sent time not to timeout
        this.timeSent=0;
        this.sent=true;
        
        // add to send queue, add myself as message sent listener
        this.msgToSend.addListener(this);
        this.getGuarantor().getMsgSender().add(this.msgToSend);
    }

    /**
     * timer fired event. Service fired this timer to check expiration
     * @param guarantor
     */
    public synchronized void timerFired(MessageDeliveryGuarantor guarantor) {
        boolean isExpired = this.isExpired();
        if (isExpired==false){
            if (this.sent==false){
                this.send();
            }
           
            // not expired, is OK; waiting for message ACK
            return;
        }

        // expired here, increment curRetryCount
        this.curRetryCount+=1;

        // retry count threshold?
        // => dead, expired
        if (this.retryCount <= this.curRetryCount){
            this.dead=true;

            // notify
            if (this.listener!=null){
                MessageDeliveryEvent evt = new MessageDeliveryEvent(MessageDeliveryEvent.STATE_DEAD);
                evt.setListenerKey(this.listenerKey);
                evt.setMsgToSend(this.msgToSend);
                evt.setUniqueId(uniqueId);
                evt.setWatcher(this);

                this.listener.messageDeliveryEventAccepted(evt);
            }

            return;
        }

        // expired now, try to re-send message
        //

        // re-send
        this.send();
    }

    /**
     * If received waiting acknowledgment
     * 
     * @param i
     * @param msg
     */
    public void messageReceived(int i, Message msg) {
        if (this.isAcknowledged()) return;
        if (this.isDead()) return;

        // accept only command messages
        if (!(msg instanceof CommandMsg)) return;
        final CommandMsg commandMsg = (CommandMsg) msg;

        // restrictions
        if (commandMsg.get_command_code() != MessageTypes.COMMAND_ACK) return;
        if (commandMsg.get_reply_on_command_id() != this.uniqueId) return;
        if (this.destination!=0 && this.destination != MoteIF.TOS_BCAST_ADDR
                && this.destination != msg.getSerialPacket().get_header_src()) return;


        // now we have received reply on our message probably, change state
        this.acknowledged = true;
        this.dead = false;

        // notify
        if (this.listener!=null){
            MessageDeliveryEvent evt = new MessageDeliveryEvent(MessageDeliveryEvent.STATE_SENT_OK);
            evt.setListenerKey(this.listenerKey);
            evt.setMsgToSend(this.msgToSend);
            evt.setUniqueId(uniqueId);
            evt.setWatcher(this);

            this.listener.messageDeliveryEventAccepted(evt);
        }

        Logger.getLogger(DefaultMessageDeliveryGuarantorWatcher.class.getName()).log(Level.SEVERE, null, "Listened: " + commandMsg.get_reply_on_command_id());

        // add children to guarantor
        if (this.children!=null && !this.children.isEmpty() && this.guarantor!=null){
            synchronized(this.guarantor){
                Iterator<MessageDeliveryGuarantorWatcher> childIt = this.children.iterator();
                while(childIt.hasNext()){
                    this.guarantor.add(childIt.next());
                }
            }
        }
    }

    public void messageSent(String listenerKey, Message msg, int destination) {
        if (this.isAcknowledged()) return;
        if (this.isDead()) return;

        // accept only command messages
        if (!(msg instanceof CommandMsg)) return;
        final CommandMsg commandMsg = (CommandMsg) msg;

        // received message sent event not for my message
        if (commandMsg.get_command_id()!=this.uniqueId) return;

        // fill in message sent time
        this.timeSent = System.currentTimeMillis();
    }

    /**
     * Return TRUE if message was expired = waited more than TTL seconds after message
     * sent event and no ACK received yet
     * @return
     */
    public boolean isExpired(){
        // if acknowledged, do nothing, cannot get expired
        if (this.isAcknowledged() || this.isDead()) return false;

        // if time sent is null then message was not sent yet => cannot be expired
        if (this.timeSent==0) return false;

        // if time sent + ttl > current time => sent time is in TTL interval and message is
        // not expired
        if ((this.timeSent + this.ttl) >= System.currentTimeMillis()) return false;
        return true;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultMessageDeliveryGuarantorWatcher other = (DefaultMessageDeliveryGuarantorWatcher) obj;
        if (this.guarantor != other.guarantor && (this.guarantor == null || !this.guarantor.equals(other.guarantor))) {
            return false;
        }
        if (this.ttl != other.ttl) {
            return false;
        }
        if (this.retryCount != other.retryCount) {
            return false;
        }
        if (this.uniqueId != other.uniqueId) {
            return false;
        }
        if (this.msgToSend != other.msgToSend && (this.msgToSend == null || !this.msgToSend.equals(other.msgToSend))) {
            return false;
        }
        if ((this.listenerKey == null) ? (other.listenerKey != null) : !this.listenerKey.equals(other.listenerKey)) {
            return false;
        }
        if (this.listener != other.listener && (this.listener == null || !this.listener.equals(other.listener))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.guarantor != null ? this.guarantor.hashCode() : 0);
        hash = 67 * hash + this.ttl;
        hash = 67 * hash + this.retryCount;
        hash = 67 * hash + this.uniqueId;
        hash = 67 * hash + (this.msgToSend != null ? this.msgToSend.hashCode() : 0);
        hash = 67 * hash + (this.listenerKey != null ? this.listenerKey.hashCode() : 0);
        hash = 67 * hash + (this.listener != null ? this.listener.hashCode() : 0);
        return hash;
    }

    /**
     * Adds child watcher to list. List will be sent if this message was successfully acknowledged.
     * @param watcher
     */
    public void addChild(MessageDeliveryGuarantorWatcher watcher){
        if (this.children==null){
            this.children=new LinkedList<MessageDeliveryGuarantorWatcher>();
        }

        this.children.add(watcher);
    }

    public void removeChild(MessageDeliveryGuarantorWatcher watcher){
        if (this.children==null){
            this.children=new LinkedList<MessageDeliveryGuarantorWatcher>();
            return;
        }

        this.children.remove(watcher);
    }

    public int getCurRetryCount() {
        return curRetryCount;
    }

    public void setCurRetryCount(int curRetryCount) {
        this.curRetryCount = curRetryCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public MessageDeliveryGuarantor getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(MessageDeliveryGuarantor guarantor) {
        this.guarantor = guarantor;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public MessageToSend getMsgToSend() {
        return msgToSend;
    }

    public String getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(String listenerKey) {
        this.listenerKey = listenerKey;
    }

    /**
     * Sets msgToSend and if message to send contains command message
     * update uniqueID attribute to match command message id.
     * @param msgToSend
     */
    public void setMsgToSend(MessageToSend msgToSend) {
        this.msgToSend = msgToSend;

        // update unique ID
        if (msgToSend!=null 
                && msgToSend.getsMsg() != null
                && msgToSend.sMsg instanceof CommandMsg){
            final CommandMsg commandMsg = (CommandMsg) msgToSend.sMsg;
            this.uniqueId = commandMsg.get_command_id();
            this.destination = msgToSend.destination;
        }
    }

    public MessageDeliveryEventListener getListener() {
        return listener;
    }

    public void setListener(MessageDeliveryEventListener listener) {
        this.listener = listener;
    }

    public List<MessageDeliveryGuarantorWatcher> getChildren() {
        return children;
    }

    public void setChildren(List<MessageDeliveryGuarantorWatcher> children) {
        this.children = children;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    

    
}
