///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package rssi_graph_deprecated;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.*;
//import java.io.IOException;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.Timer;
//import net.tinyos.message.*;
//import rssi_graph.messages.MultiPingMsg;
//import rssi_graph.RSSIMessageQueueElement;
//import rssi_graph.RSSI_graphView;
///**
// *
// * @author ph4r05
// */
//public class RSSI_controller implements MessageListener, ActionListener {
//  private MoteIF moteIF;
//  private double myRssi=0;
//  private double hisRssi=0;
//  private double sumRssi=0;
//  public static final double lc2=0.11;
//
//  public static final int[] signalLevel = {31,27,23,19,15,11,7,3};
//  public static final int[] channels = {11,12,13,14,15,16,17,18,19,20,21,23,24,25,26};
//
//  protected File csvFile = null;
//  protected RSSI_graphView gw = null;
//  protected int numMeassurement = 10;
//  protected Timer timer;
//  private int counter=0;
//  private BufferedWriter output=null;
//
//  // to gou trough
//  protected int[] channelToGo = {11,18,26};
//  protected int[] txToGo = {31,27,23,19,15,11,7,3};
//
//  protected int outerBlockMax = 5;
//  protected int innerBlockMax = 20;
//  protected int channelMax = 2;
//  protected int txMax = 7;
//
//  // current status
//  private int mPhase=0;
//  protected boolean finished=true;
//  protected boolean waiting=false;
//
//  protected int channelCur = 0;
//  protected int txCurr = 0;
//  protected int innerBlockCur = 0;
//  protected int outerBlockCur = 0;
//
//  protected testManager tm;
//  public LinkedList<RSSIMessageQueueElement> rssiMessageQueue;
//
//  /**
//   * Constructor
//   * @param moteIF mote interface
//   */
//  public RSSI_controller(MoteIF moteIF) {
//    this.moteIF = moteIF;
//    //
//    // @deprecated, handle messages in dispatcher module
//    //this.moteIF.registerListener(new MultiPingResponseMsg(), this);
//    this.tm = new testManager();
//
//    // init maximums
//    this.txMax = this.txToGo.length - 1 ;
//    this.channelMax = this.channelToGo.length -1 ;
//    this.finished = true;
//    this.waiting = false;
//  }
//
//  @Override
//  protected void finalize() throws Throwable {
//        super.finalize();
//  }
//
//  public void setCsvFile(File fl) throws IOException{
//      this.csvFile = fl;
//      output = new BufferedWriter(new FileWriter(fl));
//  }
//
//  // @TODO: only collect data here, because this is triggered only when packet comes
//  // when is packet lost no data are written - information loss
//  // when is timer finished write lines to CSV file
//  public void messageReceived(int to, Message message) {
////    MultiPingResponseReportMsg msg = (MultiPingResponseMsg) message;
////    int source = message.getSerialPacket().get_header_src();
////
////    // calculate softMyRssi value
//////    double rssiFromMobile = true ? msg.get_rssi() : 5;
////    double tmphissRssi = hisRssi +  (lc2) * (rssiFromMobile - hisRssi);
////
////    DecimalFormat df = new DecimalFormat("00.000000");
////    System.err.println("Something received");
//////
//////    String fileStr = mPhase+";"+msg.get_counter()+";"+msg.get_rssi()+";"+msg.get_rssiFromBase();
//////
//////    // get last received packet from queue
//////    // if there is space between counters
//////    // fill it with empty packets
//////    // only on non-first packet && non empty queue
//////    // lost packet count
//////    int lostPacket=0;
//////    if (msg.get_counter() > 0 && this.rssiMessageQueue.size() > 0){
//////        // last element from queue
//////        RSSIMessageQueueElement qelemTmp = this.rssiMessageQueue.getLast();
//////        // fill spaces
//////        for (int i = qelemTmp.getMsgBody().get_counter(); i < msg.get_counter(); i++, lostPacket++) {
//////            String fileStrTmp = mPhase+";"+i+";-200;-200";
//////            try {
//////                output.write(fileStrTmp);
//////                output.newLine();
//////            } catch (IOException ex) {
//////                Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//////            }
//////        }
//////    }
////
////    // build message data
////    RSSIMessageQueueElement qelem = new RSSIMessageQueueElement();
////    qelem.setMsgBody(msg);
////    qelem.setHisRssiSmooth(tmphissRssi);
////
////    qelem.setBarrier(false);
////    qelem.setChannel(this.channelToGo[this.channelCur]);
////    qelem.setTxpower(this.txToGo[this.txCurr]);
////    qelem.setMeasurementNum(mPhase);
////
////    System.err.println(qelem.getStructuredOut());
////
////    try {
////        //tm.messageReceived(qelem);
////    } catch (Exception ex) {
////        Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
////    }
////
////
//////    // if we received packet
//////    try {
//////        output.write(fileStr);
//////        output.newLine();
//////    } catch (IOException ex) {
//////        Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//////    }
//////
//////    if (lostPacket > 0){
//////        gw.getTextArea().append("Packet lost: " + lostPacket + System.getProperty("line.separator"));
//////        System.err.println("Packet lost: " + lostPacket);
//////    }
////
////    // write to err ass well
////    if ((msg.get_counter() % 10) == 0){
////        String outStr = qelem.getFormatedOut();
////        System.err.println(outStr);
//////        System.err.println(this.tm.getStatusMessage());
////
////        // write out to textarea
////        gw.getTextArea().append(outStr + System.getProperty("line.separator"));
//////        gw.getTextArea().append(this.tm.getStatusMessage() + System.getProperty("line.separator"));
////    }
////
////    //System.out.println("softMyRSSI: " + tmpmyRssi + "; delta=" + (rssiFromBase - myRssi) + "; prev=" + myRssi);
////    hisRssi = tmphissRssi;
//  }
//
//    //Handle timer event. Update the loopslot (frame number) and the
//    //offset.  If it's the last frame, restart the timer to get a long
//    //pause between loops.
//  public void actionPerformed(ActionEvent e) {
//        MultiPingMsg payload = new MultiPingMsg();
//
////        // all tests done ?
////        if (this.tm.isDone()){
////            timer.stop();
////            this.finished=true;
////            this.waiting=false;
////
////            // info
////            gw.getProgressLabel().setText("Test finished");
////
////            try {
////                tm.deinitTest();
////            } catch (IOException ex) {
////                Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
////            }
////            return;
////        }
//
//        // somebody should be notified that cycle is ended
//        // waiting?
//        if (isWaiting()){
//            // stop timer for now
//            timer.stop();
//            return;
//        }
//
//        if (isFinished()){
//            // finished?
//            timer.stop();
//            return;
//        }
//
////
////        // waiting needed ?
////        if (this.tm.waitingNeeded()){
////            // show waiting message
////            String waitMsg = this.tm.getWaitingMessage();
////
////            // info
////            gw.getProgressLabel().setText("Waiting");
////            gw.getCommandLabel().setText(waitMsg);
////
////            // stop timer and wait
////            this.setWaiting(true);
////            timer.stop();
////            return;
////        }
////        // are we done ?
////        if (counter >= this.numMeassurement) {
////            timer.stop();
////
////            return;
////        }
//
//        // increment send counter
//        counter+=1;
//
//        // do one cycle
//        try {
//            // every fifth packet
//            //if ((counter % 10) == 0){
//            System.err.print("Sending packet " + counter + ": ");
//            //}
//
////            payload = this.tm.sendNext();
////            if (payload==null){
////                timer.stop();
////            }
//
//            // setup packet
//            payload.set_txpower((short) this.txToGo[this.txCurr]);
//            //payload.set_channel((short) this.channelToGo[this.channelCur]);
//            payload.set_delay(gw.getTimeout());
//            payload.set_packets(gw.getSamplesNumber());
//            payload.set_counter(counter);
//
//
//            System.err.print("delay: "+gw.getTimeout());
//            System.err.print("; count: "+gw.getSamplesNumber());
//            System.err.print("; tx: "+this.txToGo[this.txCurr]);
//            System.err.print("; channel: "+this.channelToGo[this.channelCur]);
//            System.err.print("; outBlock: "+this.outerBlockCur);
//            System.err.println("");
//
////             increment base counter
//            this.incTx();
//
//            // send packed
//            //MoteIF.TOS_BCAST_ADDR
//            moteIF.send(5, payload);
////            this.tm.messageSent(payload);
//        } catch (IOException exception) {
//            System.err.println("Exception thrown when sending packets. Exiting.");
//            System.err.println(exception);
//        } catch (Exception ex) {
//            Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        // move caret of textarea
//        //gw.getTextArea().setCaretPosition(gw.getTextArea().getText().length());
//    }
//
//  public void stopAction() throws IOException{
//      timer.stop();
//      this.finished=true;
//      this.waiting=false;
//      tm.deinitTest();
//      this.deinitTest();
//
////      gw.getTextArea().append("!All Tests stopped!" + System.getProperty("line.separator"));
//  }
//
//  /**
//   * Reset button from main controller form
//   * will end all tests and flush buffers to file
//   */
//  public void resetButton() {
//      timer.stop();
//      this.finished=true;
//      this.waiting=false;
//      this.deinitTest();
//        try {
//            tm.deinitTest();
//        } catch (IOException ex) {
//            Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
////      gw.getTextArea().append("!All Tests stopped!" + System.getProperty("line.separator"));
//  }
//
//  /**
//   * Done button was pressed
//   */
//  public void doneButton(){
//    timer.stop();
//    this.finished=true;
//    this.waiting=false;
//
//    this.deinitTest();
////    // set finished to true
////    tm.setFinished(true);
////
////
////    try {
////        tm.deinitTest();
////    } catch (IOException ex) {
////        Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
////    }
//
//
//    // ToDo: de-init test here and write out results HERE
//
////    gw.getTextArea().append("!Test done!" + System.getProperty("line.separator"));
//  }
//
//  public void initTest(){
//      this.getRssiMessageQueue().clear();
//  }
//
//  /**
//     * Computes output from testing, flushes queues to structured output
//     *
//     * @return String output to file
//     */
//    public String getOutput() {
//        LinkedList<RSSIMessageQueueElement> queue = this.getRssiMessageQueue();
//
//        // sum string from meassurements
//        StringBuilder mainsb = new StringBuilder();
//
//        // write header
//        mainsb.append("#Test: mixed").append("\n");;
//        mainsb.append(RSSIMessageQueueElement.getStructuredOutHeader()).append("\n");
//
//        // bucketing depending on characteristics
//        // create buckets
//        Map<Integer, ArrayList<String>> stringMap = new HashMap<Integer, ArrayList<String>>();
//
//        // init buckets
//        for (int i=0, j=this.txToGo.length; i<j; i++) {
//             ArrayList<String> tmp = new ArrayList<String>();
//             stringMap.put(new Integer(i), tmp);
//        }
//
//        for (RSSIMessageQueueElement elem : queue) {
//        //while(queue.isEmpty()==false){
//                // get element from queue
//                //RSSIMessageQueueElement elem = queue.pollFirst();
//
////                // get arrayList of strings
////                ArrayList<String> al = stringMap.get(getBucket(elem.getTxpower()));
////                if (al==null) continue;
//
//                // write element data to list
//                StringBuilder sb = new StringBuilder();
//                sb.append(elem.getStructuredOut());
//                sb.append("\n");
//
//                mainsb.append(elem.getStructuredOut());
//                mainsb.append("\n");
//
////                al.add(sb.toString());
//        }
//
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
//
//        return mainsb.toString();
//    }
//
//  public void deinitTest(){
//       java.util.Date today = new java.util.Date();
//
//        BufferedWriter output;
//        try {
//            File outFile = new File("meassurement-" + (new java.sql.Timestamp(today.getTime())) + "_mixed.csv");
//            output = new BufferedWriter(new FileWriter(outFile));
//
//            String genOut = getOutput() ;
//            output.write(genOut);
//            output.flush();
//            output.close();
//
//            System.err.print(genOut);
//
//        } catch (IOException ex) {
//            Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        initTest();
//        return;
//  }
//
//  /**
//   * Next measurement button was pressed
//   */
//  public void actionButton(){
//      try {
//          if (isFinished()){
//              // set timer
//              timer = new Timer(gw.getRTimeout(), this);
//              timer.setInitialDelay(250);
//              timer.start();
//
//              // init cycle
//              this.counter=0;
//              this.mPhase=0;
//              this.finished = false;
//              this.waiting = false;
//              this.txCurr = 0;
//              this.channelCur = 0;
//              this.innerBlockCur = 0;
//              this.outerBlockCur = 0;
//
////              // deprecated, now tests control sending process
////              // this module only sits between tests and hardware
////              //this.setNumMeassurement(gw.getSamplesNumber());
////              this.tm.initTest();
////              this.tm.setSampleCount(gw.getSamplesNumber());
////              this.tm.setDelay(gw.getTimeout());
//
////              gw.getTextArea().append(System.getProperty("line.separator") + "====================================================================="
////                        + System.getProperty("line.separator") + "New meassurement" + System.getProperty("line.separator"));
//
//              gw.getProgressLabel().setText("Working; 0%");
//              return;
//          }
//
//          if (isWaiting()){
//            this.waiting=false;
//            timer.start();
////            DEPRECATED
////            this.tm.waitEvent();
//
//            // info
//            gw.getProgressLabel().setText("Working; pass: " + this.mPhase);
////            gw.getTextArea().append("Next phase" + System.getProperty("line.separator"));
//
////            //            DEPRECATED
////            if (this.tm.waitingNeeded()){
////                // show waiting message
////                String waitMsg = this.tm.getWaitingMessage();
////
////                // info
////                gw.getProgressLabel().setText("Waiting");
////                gw.getCommandLabel().setText(waitMsg);
////
////                // stop timer and wait
////                this.setWaiting(true);
////                //timer.stop();
////                return;
////            }
////
////            // do one cycle
////            try {
////                counter +=1;
////                System.err.println("Sending packet " + counter);
////
////                MultiPingMsg payload = new MultiPingMsg();
////                payload = this.tm.sendNext();
////                if (payload==null){
////                    //timer.stop();
////                    gw.getTextArea().append("Dstal som prazdny payload. co teraz??? " + System.getProperty("line.separator"));
////                }
////
////                moteIF.send(MoteIF.TOS_BCAST_ADDR, payload);
////
////                // let test manager know that message was sent sucessfully
////                this.tm.messageSent(payload);
////
////            } catch (IOException exception) {
////                // exception ?
////                System.err.println("Exception thrown when sending packets. Exiting.");
////                System.err.println(exception);
////            } catch (Exception ex) {
////                // i dont know such exception, handle in general way
////                Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
////            }
//
//            // move caret of textarea to the last printed character = autoscroll
////            gw.getTextArea().setCaretPosition(gw.getTextArea().getText().length());
//
//            return;
//          }
//
//
//
//          gw.getProgressLabel().setText("Working");
//          gw.getCommandLabel().setText("");
//
////          gw.getTextArea().append("Waiting right now" + System.getProperty("line.separator"));
////          gw.getTextArea().setCaretPosition(gw.getTextArea().getText().length());
//
//       } catch (Exception ex) {
//            Logger.getLogger(RSSI_controller.class.getName()).log(Level.SEVERE, null, ex);
//        }
//  }
//
//    public void setGw(RSSI_graphView gw) {
//        this.gw = gw;
//    }
//
//    public int getNumMeassurement() {
//        return numMeassurement;
//    }
//
//    public void setNumMeassurement(int numMeassurement) {
//        this.numMeassurement = numMeassurement;
//    }
//
//    public void incTx() {
//        // am I at the end?
//        if (this.txCurr == this.txMax){
//            // reset and increment coutner "above" me
//            this.txCurr = 0;
//            this.incChannel();
//            return;
//        }
//
//        this.txCurr+=1;
//    }
//
//    public void incChannel(){
//        // am I at the end?
//        if (this.channelCur == this.channelMax){
//            // reset and increment coutner "above" me
//            this.channelCur = 0;
//            this.incOuterBlock();
//            return;
//        }
//
//        this.channelCur+=1;
//    }
//
//    public void incInnerBlock(){
//        // am I at the end?
//        if (this.innerBlockCur == this.innerBlockMax){
//            // reset and increment coutner "above" me
//            this.innerBlockCur = 0;
//            this.incOuterBlock();
//            return;
//        }
//
//        this.innerBlockCur+=1;
//    }
//
//    public void incOuterBlock(){
//        // am I at the end?
//        if (this.outerBlockCur == this.outerBlockMax){
//            // reset and increment coutner "above" me
//            this.outerBlockCur = 0;
//
//            // waiting now
//            setWaiting(true);
//
//            // increment meassurement number
//            this.mPhase+=1;
//
//            return;
//        }
//
//        this.outerBlockCur+=1;
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
//    public int getChannelCur() {
//        return channelCur;
//    }
//
//    public void setChannelCur(int channelCur) {
//        this.channelCur = channelCur;
//    }
//
//    public int getInnerBlockCur() {
//        return innerBlockCur;
//    }
//
//    public void setInnerBlockCur(int innerBlockCur) {
//        this.innerBlockCur = innerBlockCur;
//    }
//
//    public int getInnerBlockMax() {
//        return innerBlockMax;
//    }
//
//    public void setInnerBlockMax(int innerBlockMax) {
//        this.innerBlockMax = innerBlockMax;
//    }
//
//    public int getOuterBlockCur() {
//        return outerBlockCur;
//    }
//
//    public void setOuterBlockCur(int outerBlockCur) {
//        this.outerBlockCur = outerBlockCur;
//    }
//
//    public int getOuterBlockMax() {
//        return outerBlockMax;
//    }
//
//    public void setOuterBlockMax(int outerBlockMax) {
//        this.outerBlockMax = outerBlockMax;
//    }
//
//    public int getTxCurr() {
//        return txCurr;
//    }
//
//    public void setTxCurr(int txCurr) {
//        this.txCurr = txCurr;
//    }
//
//    private LinkedList<RSSIMessageQueueElement> getRssiMessageQueue() {
//        return this.rssiMessageQueue;
//    }
//}
