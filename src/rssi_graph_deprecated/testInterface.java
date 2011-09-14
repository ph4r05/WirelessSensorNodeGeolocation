///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package rssi_graph_deprecated;
//import java.util.LinkedList;
//import net.tinyos.message.*;
//import rssi_graph.messages.MultiPingMsg;
//import rssi_graph.RSSIMessageQueueElement;
//
///**
// *
// * @author ph4r05
// */
//
//public interface testInterface {
//    // inits, deinits
//    public boolean initTest();
//    public boolean deinitTest();
//
//    public String getIdent();
//
//    // next phrase of meassurement
//    public void nextPhase() throws Exception;
//
//    public void resetPhase() throws Exception;
//
//    // for. ex. moving of receiver, rotating, adding barier
//    public boolean waitingNeeded();
//
//    // notification to user, add barier, rotate receiver,...
//    public String getWaitingMessage();
//
//    // is done ?
//    public boolean isDone();
//
//    // called when message arive,
//    public void messageReceived(RSSIMessageQueueElement msgElem);
//
//    // called after message is send
//    public void messageSent(MultiPingMsg msg);
//
//    // prepare message before sending, then sending is invoked, then messageSent is called
//    public void prepareMessage(MultiPingMsg message, MoteIF moteIF);
//
//    public int getSampleNum();
//    public void setSampleNum(int sampleNum);
//
//    public int getDelay();
//    public void setDelay(int num);
//
//    public boolean isPhaseCompleted();
//    public int getCounter();
//    public LinkedList<RSSIMessageQueueElement> getRssiMessageQueue();
//    public String getStatusMessage();
//    public void moveNextPhaseIfNeeded();
//    public void fillGaps();
//    public String getOutput();
//}
