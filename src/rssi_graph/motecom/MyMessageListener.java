/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.motecom;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.JPanelLogger;
import rssi_graph.JPannelLoggerLogElement;

/**
 * Global message listener intended to receive packets from MoteIF as only one entity
 * in whole system. I think that listeners handling is not performed very well in MoteIF, so 
 * I wrote my very own. 
 * 
 * All module which need to receive messages from network registers here, same as it was performed before 
 * in MoteIf.
 * 
 * Globality of this object enables packetSource restart/change at runtime. When packet source is changed, listener and sender
 * are reseted, listeners are re-registered.
 * 
 * Uses thread-safe data structures to avoid corruption. 1 thread waits for registering/deregistering requests.
 * MessageReceived is invoked by different thread (from TOS lobrary receiving packets from serial interface).
 * 
 * Messages are then pushed to ConcurentQueue.
 * In another thread runs MessageNotifier which scans ConcurentQueue for new elements. 
 * Then is performed callback to registered listeners for particular message. Every single callback
 * is in separate TRY block to separate exceptions among worker modules.
 * 
 * Works very similar like messageSender, in analogous way.
 * 
 * @since  23-20-2011
 * @author ph4r05
 */
public class MyMessageListener extends Thread implements MessageListener {
    /**
     * maximum number of notify threads in fixed size thread pool
     */
    private static final int MAX_NOTIFY_THREADS=1;

    /**
     * Sleep time after message sent
     */
    private static final int SENT_SLEEP_TIME=1000;
    
    /**
     * Maximum number of messages in queue to reset queue
     */
    public static final int MAX_QUEUE_SIZE_TO_RESET=1000;

    /**
     * Message queue
     */
    private final ConcurrentLinkedQueue<MessageReceived> queue;
    
    /**
     * Mote interface for gateway
     */
    private MoteIF gateway;

    /**
     * Logger
     */
    private JPanelLogger logger;

    /**
     * Thread pool
     */
    protected ExecutorService tasks;
    
    /**
     * Received message
     */
    protected MessageReceived msgReceived;

    /**
     * AMTYPE -> message mapping
     */
    protected ConcurrentHashMap<Integer, net.tinyos.message.Message> amType2Message = null;
    
    /**
     * Message listeners
     */
    protected ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>> messageListeners = null;

    /**
     * time of last sent message
     */
    protected long timeLastMessageSent;

    /**
     * Should I shutdown?
     */
    protected boolean shutdown=false;
    
    /**
     * Am I dropping packets currently?
     */
    protected boolean dropingPackets=true;
    
    /**
     * My worker, used to notify if something arrived to queue
     */
    private MessageNotifyWorker myWorker = null;

    /**
     *
     * @param gateway
     * @param logger
     */
    public MyMessageListener(MoteIF gateway, JPanelLogger logger) {
        // set thread title
        super("MyMessageListener");
        
        // init queues
        queue = new ConcurrentLinkedQueue<MessageReceived>();
        amType2Message = new ConcurrentHashMap<Integer, Message>(8);
        messageListeners = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>>(8);
        
        this.gateway = gateway;
        this.logger = logger;

        // instantiate thread pool
        tasks = Executors.newFixedThreadPool(MAX_NOTIFY_THREADS);
        this.myWorker = new MessageNotifyWorker();
        tasks.execute(this.myWorker);
        
//        // create notify threads
//        for(int i=0; i<MAX_NOTIFY_THREADS; i++){
//            tasks.execute(new MessageNotifyWorker());
//        }
    }

    /**
     * Pausing thread
     * @param microsecs
     */
    private void pause(int microsecs) {
        try {
            Thread.sleep(microsecs);
        } catch (InterruptedException ie) {
            return;
        }
    }
    
    /**
     * Register message listener
     */
    public synchronized void registerListener(net.tinyos.message.Message msg, net.tinyos.message.MessageListener listener){
        // null?
        if (msg==null || listener==null){
            // @TODO: throw null pointer exception here
            return;
        }
        
        try {
            // is message in translate queue?
            Integer amtype = msg.amType();

            // if does not contain mapping amtype -> message, create one
            if (this.amType2Message.containsKey(amtype)==false){
                this.amType2Message.put(amtype, msg);
                
                // register this in real
                this.gateway.registerListener(msg, this);
            }
            
            // first deregister to ensure that here is no duplicity
            this.deregisterListener(msg, listener);

            // check if linked list exists
            if (this.messageListeners.containsKey(amtype)==false 
                    || this.messageListeners.get(amtype)==null){
                // none such mapping yet created, create one
                ConcurrentLinkedQueue<net.tinyos.message.MessageListener> queueListener = new ConcurrentLinkedQueue<MessageListener>();
                this.messageListeners.put(amtype, queueListener);
            }

            // here is queueListener set, get it now
            ConcurrentLinkedQueue<MessageListener> queueListeners = this.messageListeners.get(amtype);

            // check if is already in linked queue
            if (queueListeners==null) return;
            Iterator<MessageListener> iterator = queueListeners.iterator();
            while(iterator.hasNext()){
                MessageListener curMessageListener = iterator.next();
                if (curMessageListener==null){
                    iterator.remove();
                    continue;
                }

                // equals? already set
                if (curMessageListener.equals(listener)){
                    return;
                }
            }

            // if here, listener is not in, set it
            queueListeners.add(listener);

            // update map
            this.messageListeners.put(amtype, queueListeners);
        } catch (Exception e){
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * unregister message listener
     */
    public synchronized void deregisterListener(net.tinyos.message.Message msg, net.tinyos.message.MessageListener listener){
        if (msg==null || listener==null){
            // @TODO: throw null pointer exception here
            return;
        }
        
        try {
            // is message in translate queue?
            Integer amtype = msg.amType();
            
            // if does not contain mapping amtype -> message, create one
            if (this.amType2Message.containsKey(amtype)==false){                
                // if not here, cannot be registered
                return;
            }
            
            // is amtype established?
            if (this.messageListeners.containsKey(amtype)==false){
                // no, cannot be in underlying list
                return;
            }
            
            // get list
            ConcurrentLinkedQueue<MessageListener> listenersList = this.messageListeners.get(amtype);
            if (listenersList==null || listenersList.isEmpty()){
                // list empty => cannot be in
                return;
            }
            
            Iterator<MessageListener> iterator = listenersList.iterator();
            while(iterator.hasNext()){
                MessageListener curListener = iterator.next();
                if (curListener==null){
                    iterator.remove();
                    continue;
                }
                
                if (curListener.equals(listener)){
                    iterator.remove();
                    break;
                }
            }
            
            // push back to map
            this.messageListeners.put(amtype, listenersList);
            
        } catch (Exception e){
            e.printStackTrace(System.err);
        }
    }

    /**
     * perform hard reset to this object = clears entire memory
     */
    public synchronized void reset(){
        this.queue.clear();
        this.msgReceived = null;
        
        System.err.println("MesageListener queues was flushed");
    }
    
    /**
     * After gateway change is needed to register as listener
     */
    protected void reregisterListeners(){
        if (this.amType2Message == null || this.amType2Message.isEmpty()) return;
        
        try{
            Iterator<Integer> iterator = this.amType2Message.keySet().iterator();
            while(iterator.hasNext()){
                Integer amtype = iterator.next();
                Message curMsg = this.amType2Message.get(amtype);

                // register
                this.gateway.registerListener(curMsg, this);
            }
        } catch(Exception e){
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * The thread either executes tasks or sleep.
     */
    @Override
    public void run() {
         // do in infitite loop
         while(true){
            // yield for some time
            this.pause(250);

            // shutdown
            if (this.shutdown == true){
                System.err.println("MyMessageReceiver shutting down.");
                this.tasks.shutdown();
                break;
            }

            //  nulltest
            if (queue==null) continue;
            
//            // test queue received
//            synchronized(queue){
//                if (this.queue.isEmpty()){
//                    msgReceived=null;
//                }
//                else {
//                    msgReceived=queue.remove();
//                }
//            }
//
//            // if message was null, continue to sleep
//            if (msgReceived==null) continue;
//            
//            try {
//                // add message to tonotify queue if needed
//                if (msgToSend.listener != null && msgToSend.listener.isEmpty()==false){
//                    // do it in sycnhronized block not to interfere with reading
//                    // threads
//                    synchronized(this.toNotify){
//                        this.toNotify.add(msgToSend);
//                    }
//                }
//
//                // store last sent messages
//                this.timeLastMessageSent = System.currentTimeMillis();
//
//                Thread.sleep(SENT_SLEEP_TIME);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    /**
     * Return TRUE if is possible to add new message to send, FALSE otherwise
     * (moteInterface may be NULL => cannot add message to send)
     * 
     * @return booleans
     */
    public boolean canAdd(){
        return (this.getGateway()!=null);
    }

    /**
     * Return size of message queue to send
     *
     * @return
     */
    public synchronized int getQueueLength(){
        return this.queue != null ? this.queue.size() : 0;
    }

    @Override
    public synchronized void messageReceived(int i, Message msg) {
        // blocking?
        if (this.dropingPackets) return;
        
        // if here blocking is not enabled, push to queue
        // can add to queue? If not, return false
        if (this.canAdd()==false){
            return;
        }
        
        // really add message to queue
        MessageReceived msgReceiveed = new MessageReceived(i, msg);
        this.queue.add(msgReceiveed);
    }

    /**
     * =========================================================================
     *
     * INTERNAL SUBCLASS NOTIFICATOR WORKER
     *
     * =========================================================================
     */

    /**
     * Perform notifications to listeners
     * Isolated from message sender not to block sending during event notification.
     */
    private class MessageNotifyWorker extends Thread implements Runnable {
        public MessageNotifyWorker() {
            
        }

        /**
         * Pausing thread
         * @param microsecs
         */
        private void pause(int mili) {
            try {
                Thread.sleep(mili);
            } catch (InterruptedException ie) {
                
            }
        }

        /**
         * Log if there is some logger. Uses main thread logger
         * 
         * @param s
         * @param subtype
         * @param code
         * @param severity
         */
        private void log(String s, int subtype, int code, int severity){
            if (logger==null) return;
            logger.addLogEntry(s, 57, "MessageNotifyWorker", subtype, code, severity);
        }

        /**
         * Main run method
         */
        @Override
        public void run() {
            // new message to be notified
            MessageReceived tmpMessage = null;

            synchronized(this){
                // do in infitite loop
                while(true){
                    // yield for some time iff is queue empty
                    if (queue==null || queue.isEmpty()){
                        this.pause(10);
                    }

                    //  nulltest
                    if (queue==null){ 
                        continue;
                    }

                    // select new message from queue in synchronized block
                    synchronized(queue){
                        // check queue size
                        if (queue.size() > MAX_QUEUE_SIZE_TO_RESET){
                            queue.clear();
                            
                            System.err.println("Warning! Input queue has to be flushed out!");
                            continue;
                        }
                        
                        // if is nonempty, select first element
                        if (!queue.isEmpty()){
                            tmpMessage = queue.remove();
                        }
                        else {
                            tmpMessage = null;
                        }
                    }

                    // end of synchronization block, check if we have some message
                    if (tmpMessage==null){
                        continue;
                    }
                    
                    // check listener for existence
                    if (!(tmpMessage instanceof MessageReceived)){
                        this.log("Message is not instance of messageReceived", 1, 1, JPannelLoggerLogElement.SEVERITY_ERROR);
                        continue;
                    }

                    // determine message received
                    Message msg = tmpMessage.getMsg();
                    if (msg == null){
                        // empty message, continue
                        this.log("Message is empty in envelope", 1, 1, JPannelLoggerLogElement.SEVERITY_ERROR);
                        continue;
                    }

                    Integer amtype = Integer.valueOf(msg.amType());

                    // is this message registered to anybody?
                    if (messageListeners.containsKey(amtype)==false){
                        // registered to no one, continue
                        continue;
                    }
                    
                    ConcurrentLinkedQueue<MessageListener> listenersList = messageListeners.get(amtype);
                    if (listenersList==null || listenersList.isEmpty()){
                        // list is null || empty
                        continue;
                    }
                    
                    Iterator<MessageListener> iterator = listenersList.iterator();
                    while(iterator.hasNext()){
                        MessageListener curListener = iterator.next();
                        if (curListener==null) continue;
                        
                        // notify this listener
                        try{
                            // notify here
                            curListener.messageReceived(tmpMessage.getI(), msg);
                        } catch (Exception e){
                            this.log("Exception during notifying listener", 1, 1, JPannelLoggerLogElement.SEVERITY_ERROR);
                            continue;
                        }
                    }
                    
                    // set message to null to release it from memory for garbage collector
                    tmpMessage = null;
                } //end while(true)
            } // end of synchronized
        } // end run()
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */
    public JPanelLogger getConsolePanel() {
        return logger;
    }

    public void setConsolePanel(JPanelLogger consolePanel) {
        this.logger = consolePanel;
    }

    public MoteIF getGateway() {
        return gateway;
    }

    public synchronized void setGateway(MoteIF gateway) {
        this.gateway = gateway;
        
        // reset internal queues
        this.reset();
        
        // re-register listeners here
        this.reregisterListeners();
        
        // reset queues again
        this.reset();
        System.err.println("Gateway changed for MessageListener");
    }

    public ConcurrentLinkedQueue<MessageReceived> getQueue() {
        return queue;
    }

    public ExecutorService getTasks() {
        return tasks;
    }

    public long getTimeLastMessageSent() {
        return timeLastMessageSent;
    }

    public void setTimeLastMessageSent(Long timeLastMessageSent) {
        this.timeLastMessageSent = timeLastMessageSent;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public boolean isDropingPackets() {
        return dropingPackets;
    }

    public void setDropingPackets(boolean dropingPackets) {
        this.dropingPackets = dropingPackets;
    }

    public JPanelLogger getLogger() {
        return logger;
    }

    public void setLogger(JPanelLogger logger) {
        this.logger = logger;
    }
}
