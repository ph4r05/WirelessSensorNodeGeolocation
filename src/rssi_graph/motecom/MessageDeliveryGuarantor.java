/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tinyos.message.Message;
import rssi_graph.messages.CommandMsg;
import rssi_graph.nodeRegister.NodeRegister;

/**
 * Service responsible for the delivery of messages.
 * Message is delivered if mote acknowledges it
 *
 * Clean design would contain another level of hierachy between this service and
 * ACK objects per messages. This middle layer would be bounded to specific
 * message type, now CommandMessage ack is hard coded to delivery to meet KISS principle.
 *
 * if message has timeouted, it could be re-sent or client could be notified about 
 * this failure. tree hierarchy could be defined in this messages.
 * (nodes under root will be send iff root was successfully acknowledged, otherwise no node under root is send)
 * 
 * @author ph4r05
 */
public class MessageDeliveryGuarantor implements MessageSentListener, net.tinyos.message.MessageListener {
    private MessageSender msgSender;
    private NodeRegister nodeRegister;

    protected List<MessageDeliveryGuarantorWatcher> messagesToWatch;

    /**
     * Set of active message ids waiting for ACK, every new message to ACK should have
     * unique ID
     */
    protected Set<Integer> activeWaitingMessageIds;

    /**
     * Map of booked message ids waiting for ACK to-be-used, every new message to ACK should have
     * unique ID.
     * Mapped Long is generating time of record. To support resources releasing/cleaning old
     */
    protected Map<Integer, Long> bookedWaitingMessageIds;

    /**
     * Random number generator
     */
    private Random generator = null;

    /**
     * Mutual exclusive
     */
    private boolean mutex=false;

    private List<MessageDeliveryGuarantorWatcher> toRemove=null;
    private List<MessageDeliveryGuarantorWatcher> toAdd=null;

    public MessageDeliveryGuarantor(MessageSender msgSender) {
        this.msgSender = msgSender;
        this.nodeRegister = null;
        this.messagesToWatch = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.toRemove = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.toAdd = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.activeWaitingMessageIds = new HashSet<Integer>(16);
        this.bookedWaitingMessageIds = new HashMap<Integer, Long>();
        this.generator = new Random();

        this.registerListeners();
    }

    public MessageDeliveryGuarantor(MessageSender msgSender, NodeRegister nodeRegister) {
        this.msgSender = msgSender;
        this.nodeRegister = nodeRegister;
        this.messagesToWatch = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.toRemove = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.toAdd = new LinkedList<MessageDeliveryGuarantorWatcher>();
        this.activeWaitingMessageIds = new HashSet<Integer>(16);
        this.bookedWaitingMessageIds = new HashMap<Integer, Long>();
        this.generator = new Random();

        this.registerListeners();
    }

    public void registerListeners(){
        if (this.msgSender.getGateway()==null) return;
        this.msgSender.getGateway().registerListener(new CommandMsg(), this);
    }

    /**
     * Expire old booked records not already added as active
     * Time complexity O(n log(n))
     * @return
     */
    protected void cleanBookedMessages(){
        if (this.bookedWaitingMessageIds==null || this.bookedWaitingMessageIds.isEmpty()) return;

        // cur time
        long curMilis = System.currentTimeMillis();

        // iterate over booked set
        Iterator<Integer> bookedKeyIterator = this.bookedWaitingMessageIds.keySet().iterator();
        while(bookedKeyIterator.hasNext()){
            Integer curKey = bookedKeyIterator.next();

            // if is already added in active queue, leave alone
            if (this.activeWaitingMessageIds.contains(curKey)){
                continue;
            }

            // if TTL time is OK, do not remove
            if ((this.bookedWaitingMessageIds.get(curKey) + 30000) >= curMilis) continue;

            // remove from map
            this.bookedWaitingMessageIds.remove(curKey);
        }
    }

    /**
     * Generate new unique message ID waiting for ack
     * @return
     */
    public synchronized int getNextMessageId(){
        // generate new number, ifalready exists in actove setm try another one
        //
        long curMilis = System.currentTimeMillis();
        for(int tryCounter=0; tryCounter<5000; tryCounter++){
            int curRandom = this.generator.nextInt(32768);
            Integer curRandomInteger = Integer.valueOf(curRandom);
            if (this.bookedWaitingMessageIds.containsKey(curRandomInteger)) continue;

            // if here then we have found unused message id
            // add to booked queue not to generate two same ID
            // (could happen if 1. id is generated and not yet added to active queue
            // in time of generation 2. id. possibility of such collision is very small,
            // but could happen 1 / (32768*P_of_such_concurency)
            this.bookedWaitingMessageIds.put(curRandomInteger, Long.valueOf(curMilis));
            return curRandom;
        }

        // failed 1000times to found unique non-booked ID for message, return error
        return -1;
    }

    /**
     * Handle timer tick event from parent thread
     */
    public synchronized void timerTick(){
        // avoid
        if (this.isMutex()) return;

        // cleanup booked queue
        this.cleanBookedMessages();

        synchronized(messagesToWatch){
            // delete all from toRemove
            Iterator<MessageDeliveryGuarantorWatcher> it = this.toRemove.iterator();
            while(it.hasNext()){
                MessageDeliveryGuarantorWatcher cur = it.next();
                this.messagesToWatch.remove(cur);
            }
            this.toRemove.clear();

            // insert all to toAdd
            it = this.toAdd.iterator();
            while(it.hasNext()){
                MessageDeliveryGuarantorWatcher cur = it.next();
                this.messagesToWatch.add(cur);
            }
            this.toAdd.clear();

            // visit all registered messages
            it = this.messagesToWatch.iterator();
            while(it.hasNext()){
                MessageDeliveryGuarantorWatcher cur = it.next();
                cur.timerFired(this);

                // check if is watcher dead
                if (cur.isDead() || cur.isAcknowledged()){
                    synchronized(messagesToWatch){
                        // Can remove from queues, watcher is dead now.
                        // Assume that client object was notified about this.
                        Integer uniqueId = cur.getUniqueId();
                        this.activeWaitingMessageIds.remove(uniqueId);
                        this.bookedWaitingMessageIds.remove(uniqueId);
                        //it.remove();
                        toRemove.add(cur);

                        Logger.getLogger(MessageDeliveryGuarantor.class.getName()).log(Level.SEVERE, null, "Removed from queue (onTimerTick): " + uniqueId);
                    }
                }
            }
        }
    }

    public synchronized void messageSent(String listenerKey, Message msg, int destination) {
        // iterate over all registered watchers and pass events
        synchronized(messagesToWatch){
            this.setMutex(true);
            Iterator<MessageDeliveryGuarantorWatcher> it = this.messagesToWatch.iterator();
            while(it.hasNext()){
                MessageDeliveryGuarantorWatcher cur = it.next();
                cur.messageSent(listenerKey, msg, destination);
            }

            this.setMutex(false);
        }
    }

    public synchronized void messageReceived(int i, Message msg) {
         synchronized(messagesToWatch){
             this.setMutex(true);
             try {
                // list to delete
                LinkedList<MessageDeliveryGuarantorWatcher> toRemove=new LinkedList<MessageDeliveryGuarantorWatcher>();

                // iterate over all registered watchers and pass events
                Iterator<MessageDeliveryGuarantorWatcher> it = this.messagesToWatch.iterator();
                while(it.hasNext()){
                    MessageDeliveryGuarantorWatcher cur = it.next();
                    cur.messageReceived(i, msg);

                    // check if is this watcher acknowledged
                    if (cur.isDead() || cur.isAcknowledged()){
                            // Is not needed now, can remove from sets.
                            // Assume that client object was notified about success.
                            Integer uniqueId = cur.getUniqueId();
                            this.activeWaitingMessageIds.remove(uniqueId);
                            this.bookedWaitingMessageIds.remove(uniqueId);
                            toRemove.add(cur);
                            //it.remove();
                            
                            Logger.getLogger(MessageDeliveryGuarantor.class.getName()).log(Level.SEVERE, null, "Removed from queue: " + uniqueId);
                    }
                }
            } catch(Exception e){
                 Logger.getLogger(MessageDeliveryGuarantor.class.getName()).log(Level.SEVERE, null, e);
            }

            this.setMutex(false);
        }
    }

    /**
     * Add watcher to queue
     * via toAdd queue not to modify concurently accessed linked list
     * 
     * @param watcher
     */
    public synchronized void add(MessageDeliveryGuarantorWatcher watcher){
        this.activeWaitingMessageIds.add(watcher.getUniqueId());
        this.toAdd.add(watcher);
        this.registerListeners();
    }

    public synchronized void remove(MessageDeliveryGuarantorWatcher watcher){
        this.activeWaitingMessageIds.remove(watcher.getUniqueId());
        this.bookedWaitingMessageIds.remove(watcher.getUniqueId());
        this.toRemove.add(watcher);
    }

    public Set<Integer> getActiveWaitingMessageIds() {
        return activeWaitingMessageIds;
    }

    public void setActiveWaitingMessageIds(Set<Integer> activeWaitingMessageIds) {
        this.activeWaitingMessageIds = activeWaitingMessageIds;
    }

    public Map<Integer, Long> getBookedWaitingMessageIds() {
        return bookedWaitingMessageIds;
    }

    public void setBookedWaitingMessageIds(Map<Integer, Long> bookedWaitingMessageIds) {
        this.bookedWaitingMessageIds = bookedWaitingMessageIds;
    }

    public Random getGenerator() {
        return generator;
    }

    public void setGenerator(Random generator) {
        this.generator = generator;
    }

    public List<MessageDeliveryGuarantorWatcher> getMessagesToWatch() {
        return messagesToWatch;
    }

    public void setMessagesToWatch(List<MessageDeliveryGuarantorWatcher> messagesToWatch) {
        this.messagesToWatch = messagesToWatch;
    }

    public MessageSender getMsgSender() {
        return msgSender;
    }

    public void setMsgSender(MessageSender msgSender) {
        this.msgSender = msgSender;
    }

    public NodeRegister getNodeRegister() {
        return nodeRegister;
    }

    public void setNodeRegister(NodeRegister nodeRegister) {
        this.nodeRegister = nodeRegister;
    }

    public boolean isMutex() {
        return mutex;
    }

    public void setMutex(boolean mutex) {
        this.mutex = mutex;
    }
}
