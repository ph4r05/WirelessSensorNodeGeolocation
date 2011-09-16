//package rssi_graph_deprecated;
//
/////*
//// * To change this template, choose Tools | Templates
//// * and open the template in the editor.
//// */
////
////package rssi_graph;
////import java.util.ArrayList;
////import java.util.Arrays;
////import java.util.HashMap;
////import java.util.LinkedList;
////import java.util.Map;
////import java.util.logging.Level;
////import java.util.logging.Logger;
////import net.tinyos.message.MoteIF;
////
/////**
//// *
//// * @author ph4r05
//// */
////public class testSignalStrength extends testBase implements testInterface{
////    public static final int[] signalLevel = {31,27,23,19,15,11,7,3};
////    public static final int[] channels = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
////    protected int currSignal = -1;
////    protected int currChannel = -1;
////
////    public testSignalStrength() {
////        initTest();
////    }
////
////    @Override
////    public boolean deinitTest() {
////        state = testSignalStrength.STATE_NOSCAN;
////        return true;
////    }
////
////    @Override
////    public void finalizeTest(){
////
////    }
////
////    public String getIdent() {
////        return "power+signal Test";
////    }
////
////    @Override
////    public boolean initTest() {
////        super.initTest();
////
////        currSignal = testSignalStrength.signalLevel[0];
////        currChannel = testSignalStrength.channels[0];
////
////        return true;
////    }
////
////    public void moveNextPhaseIfNeeded() {
////        try {
////            // move to next phase if wanted number of samples is collected/sent
////            if (isPhaseCompleted() && !isDone()){
////                nextPhase();
////            }
////         } catch (Exception ex) {
////            Logger.getLogger(testSignalStrength.class.getName()).log(Level.SEVERE, null, ex);
////        }
////    }
////
////    @Override
////    public void messageReceived(RSSIMessageQueueElement msgElem) {
////        super.messageReceived(msgElem);
////        msgElem.setTxpower(currSignal);
////        msgElem.setChannel(currChannel);
////
////        // move to next phase if wanted number of samples is collected/sent
////        moveNextPhaseIfNeeded();
////
////        if (phasenum >= (testSignalStrength.signalLevel.length)){
////            this.state = testSignalStrength.STATE_SCANDONE;
////        }
////    }
////
////    @Override
////    public void messageSent(MultiPingMsg msg) {
////        super.messageSent(msg);
////
////        // phase completed ?
////        if (isPhaseCompleted()){
////            // is this last phase ?
////            // are we done ?
////            if (phasenum >= (testSignalStrength.signalLevel.length)){
////                this.state = testSignalStrength.STATE_SCANDONE;
////            }
////        }
////    }
////
////    @Override
////    public void nextPhase() throws Exception {
////
////        // are we scanning ?
////        if (this.state!=testSignalStrength.STATE_INSCAN){
////            throw new Exception("Cannot continue to next phase since we are not in scanning mode");
////        }
////
////        // are we done ?
////        if (phasenum >= (testSignalStrength.signalLevel.length-1)){
////            this.state = testSignalStrength.STATE_SCANDONE;
////
////            // finalize messages and queue here
////            return;
////        }
////
////        // move to next phase
////        phasenum+=1;
////        cRecv=0;
////        cSent=0;
////        currSignal = testSignalStrength.signalLevel[phasenum];
////    }
////
////    @Override
////    public void resetPhase() throws Exception {
////        super.resetPhase();
////        currSignal = testSignalStrength.signalLevel[phasenum];
////    }
////
////    public String getStatusMessage() {
////        return this.getIdent()+": curSignal: "+this.currSignal+"; phasenum: "+this.phasenum+"; sent: "+this.cSent+"; recv: "+this.cRecv;
////    }
////
////    /**
////     * Prepare message to send
////     * Fill in appropriate fields
////     *
////     * @param message
////     * @param moteIF
////     */
////    public void prepareMessage(MultiPingMsg message, MoteIF moteIF) {
////        message.set_txpower((short) this.currSignal);
////        message.set_counter(this.getCounter());
////    }
////
////    protected Integer getBucket(int txpower){
////        for (int i=0, j=testSignalStrength.signalLevel.length; i<j; i++) {
////            if (txpower == testSignalStrength.signalLevel[i]){
////                return new Integer(i);
////            }
////        }
////
////        return null;
////    }
////
////    /**
////     * Computes output from testing, flushes queues to structured output
////     *
////     * @return String output to file
////     */
////    public String getOutput() {
////        LinkedList<RSSIMessageQueueElement> queue = this.getRssiMessageQueue();
////
////        // sum string from meassurements
////        StringBuilder mainsb = new StringBuilder();
////
////        // write header
////        mainsb.append("#Test: ").append(getIdent()).append("\n");;
////        mainsb.append(RSSIMessageQueueElement.getStructuredOutHeader()).append("\n");
////
////        // bucketing depending on characteristics
////        // create buckets
////        Map<Integer, ArrayList<String>> stringMap = new HashMap<Integer, ArrayList<String>>();
////
////        // init buckets
////        for (int i=0, j=testSignalStrength.signalLevel.length; i<j; i++) {
////             ArrayList<String> tmp = new ArrayList<String>();
////             stringMap.put(new Integer(i), tmp);
////        }
////
////        for (RSSIMessageQueueElement elem : queue) {
////        //while(queue.isEmpty()==false){
////                // get element from queue
////                //RSSIMessageQueueElement elem = queue.pollFirst();
////
////                // get arrayList of strings
////                ArrayList<String> al = stringMap.get(getBucket(elem.getTxpower()));
////                if (al==null) continue;
////
////                // write element data to list
////                StringBuilder sb = new StringBuilder();
////                sb.append(elem.getStructuredOut());
////                sb.append("\n");
////
////                mainsb.append(elem.getStructuredOut());
////                mainsb.append("\n");
////
////                al.add(sb.toString());
////        }
////
////        // for each bucket
////        StringBuilder sb = new StringBuilder();
////        for (int i=0, j=testSignalStrength.signalLevel.length; i<j; i++) {
////             ArrayList<String> al = stringMap.get(new Integer(i));
////             sb.append("#TXPOWER: " + testSignalStrength.signalLevel[i]+"\n");
////
////            for (String string1 : al) {
////                sb.append(string1);
////            }
////
////            sb.append("\n");
////        }
////
////        return mainsb.toString();
////    }
////}
