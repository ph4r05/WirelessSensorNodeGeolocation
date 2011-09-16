//package rssi_graph_deprecated;
//
/////*
//// * To change this template, choose Tools | Templates
//// * and open the template in the editor.
//// */
////
////package rssi_graph;
////import java.util.LinkedList;
////import net.tinyos.message.MoteIF;
////
/////**
//// *
//// * @author ph4r05
//// */
////public abstract class testBase implements testInterface{
////    public static final int STATE_NOSCAN=1;
////    public static final int STATE_INSCAN=2;
////    public static final int STATE_WAITING=3;
////    public static final int STATE_SCANDONE=4;
////    protected int state;
////    protected int phasenum = 0;
////    protected int cSent=0;
////    protected int cRecv=0;
////    protected int sampleNum=100;
////    protected int delay=50;
////
////    /**
////     * Every test need to has own counter
////     * since it is independent on other tests
////     */
////    protected int counter;
////    public LinkedList<RSSIMessageQueueElement> rssiMessageQueue;
////
////    public testBase() {
////        initTest();
////    }
////
////    public boolean deinitTest() {
////        state = testBase.STATE_NOSCAN;
////        return true;
////    }
////
////    public String getWaitingMessage() {
////        String out = new String();
////        if (!this.waitingNeeded()){
////            return out;
////        }
////
////        return out;
////    }
////
////    public void finalizeTest(){
////
////    }
////
////    public boolean initTest() {
////        state = testBase.STATE_INSCAN;
////        phasenum = 0;
////        rssiMessageQueue = new LinkedList<RSSIMessageQueueElement>();
////        rssiMessageQueue.clear();
////
////        return true;
////    }
////
////     public void fillGaps(){
////        // is empty?
////        if (getRssiMessageQueue().isEmpty()) return;
////
////        // get last message, remove from queue
////        RSSIMessageQueueElement lastMsg = getRssiMessageQueue().pollLast();
////
////        // get prelast message from queue, do not remove
////        RSSIMessageQueueElement preLastMsg = getRssiMessageQueue().peekLast();
////        if (preLastMsg==null){
////            getRssiMessageQueue().add(lastMsg);
////            return;
////        }
////
////        int i = preLastMsg.getMsgBody().get_counter()+1;
////        int t = lastMsg.getMsgBody().get_counter();
////
////        // out of order
////        if (i>t) return;
////
////        for(; i<t; i++){
////            RSSIMessageQueueElement newBogusMsg = new RSSIMessageQueueElement();
////            newBogusMsg.setGap(true);
////            newBogusMsg.setDistance(preLastMsg.getDistance());
////            newBogusMsg.setMsgBody((MultiPingResponseMsg) preLastMsg.getMsgBody().clone());
////            newBogusMsg.getMsgBody().set_counter(i);
////            getRssiMessageQueue().add(newBogusMsg);
////        }
////
////        getRssiMessageQueue().add(lastMsg);
////        return;
////    }
////
////    public boolean isDone() {
////        return this.state == testBase.STATE_SCANDONE;
////    }
////
////    public void messageReceived(RSSIMessageQueueElement msgElem) {
////        this.cRecv+=1;
////    }
////
////    public void messageSent(MultiPingMsg msg) {
////        this.cSent+=1;
////        this.counter+=1;
////    }
////
////    public boolean isPhaseCompleted() {
////        return this.getSampleNum() <= this.cSent;
////    }
////
////    public boolean waitingNeeded() {
////        return (this.state == testBase.STATE_WAITING);
////    }
////
////    public void resetPhase() throws Exception {
////        this.state=STATE_INSCAN;
////        this.phasenum=0;
////        this.cRecv=0;
////        this.cSent=0;
////    }
////
////    public void nextPhase() throws Exception {
////        this.cSent=0;
////        this.cRecv=0;
////    }
////
////    public int getSampleNum() {
////        return sampleNum;
////    }
////
////    public void setSampleNum(int sampleNum) {
////        this.sampleNum = sampleNum;
////    }
////
////    public int getDelay() {
////        return this.delay;
////    }
////
////    public void setDelay(int num) {
////        this.delay = num;
////    }
////
////    public int getCounter() {
////        return counter;
////    }
////
////    public LinkedList<RSSIMessageQueueElement> getRssiMessageQueue() {
////        return rssiMessageQueue;
////    }
////
////}
