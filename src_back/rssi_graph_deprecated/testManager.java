///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package rssi_graph_deprecated;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.LinkedList;
//import net.tinyos.message.*;
//import rssi_graph.messages.MultiPingMsg;
//import rssi_graph.RSSIMessageQueueElement;
//
///**
// *
// * @author ph4r05
// */
//public class testManager {
//    protected testInterface[] tests;
//    protected int curTest=0;
//    protected boolean finished=false;
//    protected boolean waiting=false;
//
//    protected int msmtCur = 0;
//
//    public testManager() {
//        initTest();
//    }
//
//    public boolean initTest(){
//        curTest = 0;
//        waiting=false;
//        finished=false;
//        msmtCur = 0;
////
////        tests = new testInterface[1];
////
////        // create tests here manualy
////        testInterface signalTest = new testSignalStrength();
////        signalTest.initTest();
////        tests[0] = signalTest;
//
//        return true;
//    }
//
//    // write out meassured results
//    public boolean deinitTest() throws IOException{
//        java.util.Date today = new java.util.Date();
//        for(int i=0,j=tests.length; i<j; i++){
//            File outFile = new File("meassurement-" + (new java.sql.Timestamp(today.getTime())) + "_" +tests[i].getIdent() + ".csv");
//            BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
//
//            String genOut = tests[i].getOutput();
//            output.write(genOut);
//            output.flush();
//            output.close();
//
//            System.err.print(genOut);
//
//            // end tests
//            tests[i].deinitTest();
//        }
//
//        initTest();
//        return true;
//    }
//
//    public String getStatusMessage(){
//        return "Test manager; distance: " + this.msmtCur + "; " + tests[curTest].getStatusMessage();
//    }
//
//    // called when message arive, iterate over all tests
//    public void messageReceived(RSSIMessageQueueElement msgElem) throws Exception{
//        // test may change received message
//        msgElem.setMeasurementNum(msmtCur);
//        tests[curTest].messageReceived(msgElem);
//
//        // add message to queue
//        tests[curTest].getRssiMessageQueue().add(msgElem);
//
//        // find gaps
//        tests[curTest].fillGaps();
//    }
//
//    // called after message is send
//    public void messageSent(MultiPingMsg msg) throws Exception{
//        tests[curTest].messageSent(msg);
//
////        // has to wait ?
////         if (tests[curTest].isDone()
////            && (curTest >= (this.tests.length-1))){
////
////            if ((msmtCur+msmtStep) > msmtEnd){
////                this.finished=true;
////            }
////        }
//    }
//
//    // prepare message before sending, then sending is invoked, then messageSent is called
//    public void prepareMessage(MultiPingMsg message, MoteIF moteIF){
//        tests[curTest].prepareMessage(message, moteIF);
//    }
//
//    public MultiPingMsg sendNext() throws Exception{
//        // select and change test before message is send
//        // receive handler has time for next time invoking sendNext(), it is guaranteed
//        // that curTest wont change
//
////        single test assumption now
////        // test selection and moving
////        do {
////            // completed test? = done()
////            if (tests[curTest].isDone()){
////
////                // last test ?
////                // then need to move to next stage
////                if (curTest >= (this.tests.length-1)){
////                    ;
////                }
////                else
////                {
////                    // move to next test if we are not on the last test now
////                    curTest+=1;
////                    break;
////                }
////            }
////            else break;
////        } while(true);
//
//        MultiPingMsg toSend = new MultiPingMsg();
//
//        // default settings
//        toSend.set_txpower((short) 3);
//        toSend.set_channel((short) 26);
//        toSend.set_delay(50);
//        toSend.set_packets(100);
//
//        // modify
//        tests[curTest].prepareMessage(toSend, null);
//
//        return toSend;
//    }
//
//    // for. ex. moving of receiver, rotating, adding barier
//    public boolean waitingNeeded(){
//        // move actual phase if wanted
//        tests[curTest].moveNextPhaseIfNeeded();
//
//        // compute
//        // before new send
//        if (tests[curTest].isDone()){
//            this.setWaiting(true);
//            return true;
//        }
//
//        return isWaiting();
//    }
//
//    // main wait event
//    public void waitEvent() throws Exception{
//        // un-wait event
//        this.setWaiting(false);
//
//        // move to next measurement phase
//        this.msmtCur += 1;
//
//        boolean curDone = tests[curTest].isDone();
//        if (curDone){
//            tests[curTest].resetPhase();
//        }
////
////        // move distance ?
////        if (curTest >= (tests.length-1))
////        {
////            // move distance now, last test is done
////            msmtCur += msmtStep;
////
////            // teset test
////            curTest = 0;
////
////            // on end ?
////            if (msmtCur > msmtEnd){
////                finished=true;
////            }
////        }
//    }
//
//    // notification to user, add barier, rotate receiver,...
//    public String getWaitingMessage(){
//        StringBuilder sb = new StringBuilder();
//        // message from manager itself
//        sb.append("testManager: Next phase will be: ").append(msmtCur+1).append("m;");
//
//        // messages from tests
//        for(int i=0,j=tests.length; i<j; i++){
//            sb.append(tests[i].getWaitingMessage());
//        }
//        return sb.toString();
//    }
//
//    // is done ?
//    public boolean isDone(){
////        if (tests[curTest].isDone()
////                && curTest >= (tests.length-1)
////                && (msmtCur+msmtStep) > msmtEnd
////           ){
////             finished=true;
////             return true;
////        }
//
//        return finished;
//    }
//
//    public boolean isWaiting() {
//        return waiting;
//    }
//
//    public void setWaiting(boolean waiting) {
//        this.waiting = waiting;
//    }
//
//    public boolean isFinished() {
//        return finished;
//    }
//
//    public void setFinished(boolean finished) {
//        this.finished = finished;
//    }
//
//    public void setSampleCount(int num){
//        for (testInterface interface1 : tests) {
//            interface1.setSampleNum(num);
//        }
//    }
//
//    public void setDelay(int num){
//        for (testInterface interface1 : tests) {
//            interface1.setDelay(num);
//        }
//    }
//}
