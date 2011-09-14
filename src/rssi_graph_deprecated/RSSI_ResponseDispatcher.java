///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package rssi_graph_deprecated;
//
//import rssi_graph.messages.MultiPingResponseMsg;
//import rssi_graph_deprecated.testManager;
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
//import rssi_graph.MobileNodeManager;
//import rssi_graph.RSSIMessageQueueElement;
//import rssi_graph.RSSI_graphView;
//
///**
// *
// * @author ph4r05
// */
//public class RSSI_ResponseDispatcher implements MessageListener, ActionListener {
//    /**
//   * Mote interface
//   * used to register message listener
//   */
//  private MoteIF moteIF=null;
//
//  /**
//   * MobileNodeManager used to store input data to queues
//   */
//  private MobileNodeManager mnm=null;
//
//
//  public static final double lc2=0.11;
//
//  protected RSSI_graphView gw = null;
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
//  public RSSI_ResponseDispatcher(MoteIF moteIF) {
//    this.moteIF = moteIF;
//    this.moteIF.registerListener(new MultiPingResponseMsg(), this);
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
//  // @TODO: only collect data here, because this is triggered only when packet comes
//  // when is packet lost no data are written - information loss
//  // when is timer finished write lines to CSV file
//  public void messageReceived(int to, Message message) {
//    MultiPingResponseMsg msg = (MultiPingResponseMsg) message;
//    int source = message.getSerialPacket().get_header_src();
//
//    DecimalFormat df = new DecimalFormat("00.000000");
//    System.err.println("Response received; From: " + source + "; Message: " +  msg.toString());
//
////    try {
////        // get mobile node from mobile manager
////        // now it is important do decide wether source node is mobile or
////        // we are in network testing phase when static nodes are pinged
////        // for now its mobile node
////        MobileNode mn = mnm.getMobileNode(source, true);
////
////        // if is able to insert new element, do it
////        if (mn.inputQueueCanInsert()){
////            // create new RSSI response record to be created
////            // now we need some reference to packet which requested this action
////            // is needed to handle requester to this.
////            RSSIResponseRecord rrr = new RSSIResponseRecord();
////            rrr.setCounter(msg.get_counter());
////            rrr.setNodeID_report_from(source);
////            rrr.setRssi(msg.get_rssi());
////
////            mn.inputQueueAdd(rrr);
////        }
////
////    } catch (Exception ex) {
////        Logger.getLogger(RSSI_ResponseDispatcher.class.getName()).log(Level.SEVERE, null, ex);
////    }
//  }
//
//    //Handle timer event. Update the loopslot (frame number) and the
//    //offset.  If it's the last frame, restart the timer to get a long
//    //pause between loops.
//    public void actionPerformed(ActionEvent e) {
////            System.err.print("delay: "+gw.getTimeout());
////            System.err.print("; count: "+gw.getSamplesNumber());
////            System.err.print("; tx: "+this.txToGo[this.txCurr]);
////            System.err.print("; channel: "+this.channelToGo[this.channelCur]);
////            System.err.print("; outBlock: "+this.outerBlockCur);
////            System.err.println("");
////
////            // move caret of textarea
////            gw.getTextArea().setCaretPosition(gw.getTextArea().getText().length());
//    }
//
//  public void initTest(){
//
//  }
//
//  /**
//     * Computes output from testing, flushes queues to structured output
//     *
//     * @return String output to file
//     */
//    public String getOutput() {
//
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
////        // init buckets
////        for (int i=0, j=this.txToGo.length; i<j; i++) {
////             ArrayList<String> tmp = new ArrayList<String>();
////             stringMap.put(new Integer(i), tmp);
////        }
////
////        for (RSSIMessageQueueElement elem : queue) {
////        //while(queue.isEmpty()==false){
////                // get element from queue
////                //RSSIMessageQueueElement elem = queue.pollFirst();
////
//////                // get arrayList of strings
//////                ArrayList<String> al = stringMap.get(getBucket(elem.getTxpower()));
//////                if (al==null) continue;
////
////                // write element data to list
////                StringBuilder sb = new StringBuilder();
////                sb.append(elem.getStructuredOut());
////                sb.append("\n");
////
////                mainsb.append(elem.getStructuredOut());
////                mainsb.append("\n");
////
//////                al.add(sb.toString());
////        }
//
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
//            Logger.getLogger(RSSI_ResponseDispatcher.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        initTest();
//        return;
//  }
//
//
//
//    public void setGw(RSSI_graphView gw) {
//        this.gw = gw;
//    }
//
//    public MobileNodeManager getMnm() {
//        return mnm;
//    }
//
//    public void setMnm(MobileNodeManager mnm) {
//        this.mnm = mnm;
//    }
//}
