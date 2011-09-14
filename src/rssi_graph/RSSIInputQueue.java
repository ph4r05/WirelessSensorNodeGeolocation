/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Producer-consumer queue handler for incoming messages
 * Each mobile mote has own RSSIInputQueue instance
 *
 * @author ph4r05
 */
public class RSSIInputQueue {
    /**
     * queue container
     */
    private BlockingQueue<RSSIResponseRecord> inputQueue=null;

    /**
     * NodeID of mote which I track
     */
    private int mobile_nodeid=0;

    /**
     * activity flag
     * manager could change this
     */
    private boolean active=true;

    public RSSIInputQueue() {
        // init queue with fixed capacity 10 000 messages
        inputQueue = new ArrayBlockingQueue(10000);
    }

    /**
     * Can insert something?
     * @return boolean
     */
    public boolean canInsert(){
        return inputQueue.remainingCapacity() > 0;
    }

    /**
     * Returns queue remaining capacity (free slots)
     * 
     * @return
     */
    public int getRemainingCapacity(){
        return inputQueue.remainingCapacity();
    }

    /**
     * Is queue empty?
     * @return boolean
     */
    public boolean isEmpty(){
        return inputQueue.size() == 0;
    }

    /**
     * add record to queue
     * uses offer method. If queue is empty nothing will happen.
     * 
     * @param r
     * @return
     */
    public boolean add(RSSIResponseRecord r){
        inputQueue.offer(r);
        return true;
    }

    /**
     * retrieves last counter number from queue
     * -1 on empty queue
     * @return
     */
    public int getLastCounter(){
        if (inputQueue.isEmpty()) return -1;

        RSSIResponseRecord r = inputQueue.peek();
        return r.getCounter();
    }

    /**
     * Return "raw" queue directly
     * @return
     */
    public Queue getInputQueue() {
        return inputQueue;
    }

    public int getMobile_nodeid() {
        return mobile_nodeid;
    }

    public void setMobile_nodeid(int mobile_nodeid) {
        this.mobile_nodeid = mobile_nodeid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
