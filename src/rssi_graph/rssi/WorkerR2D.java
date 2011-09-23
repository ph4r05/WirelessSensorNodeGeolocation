/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import rssi_graph.motecom.MessageDeliveryEvent;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.messages.CommandMsg;
import rssi_graph.messages.MultiPingResponseReportMsg;
import rssi_graph.messages.MultiPingMsg;
import rssi_graph.messages.MultiPingResponseMsg;
import rssi_graph.messages.MultiPingResponseTinyReportMsg;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import javax.swing.Timer;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.MessageTypes;
import rssi_graph.NodeSelectedEvent;
import rssi_graph.RSSI_graphApp;
import rssi_graph.WorkerBase;
import rssi_graph.WorkerInterface;
import rssi_graph.WorkerMessageQueueElement;
import rssi_graph.motecom.DefaultMessageDeliveryGuarantorWatcher;
import rssi_graph.motecom.MessageDeliveryEventListener;
import rssi_graph.motecom.MessageSentListener;
import rssi_graph.motecom.MessageToSend;
import rssi_graph.nodeRegister.NodeRegisterEventListener;
import rssi_graph.utils.BucketHasher;
import rssi_graph.utils.BucketHasherInterface;
import rssi_graph.utils.DefaultBucketHasherImpl;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 * RSSI-to-distance module worker
 *
 * @author ph4r05
 */
public class WorkerR2D extends WorkerBase implements MessageListener, 
        WorkerInterface, ActionListener, MessageSentListener, MessageDeliveryEventListener,
        NodeRegisterEventListener {
    
    /**
     * Manages all mobile nodes data
     */
    private MobileNodeManager mobileNodeManager = null;

    /**
     * SQLite connection
     */
    private Connection conn = null;

    /**
     * If test is in progress then TRUE
     */
    private boolean doingTestNow=false;

    /**
     * Current test ID
     */
    private int curTestId=0;

    /**
     * Current round ID
     */
    private int curRoundId=0;

    /**
     * Move next timer counts timeout windows from last received packet
     * When timer fires, move to next TX power
     * (when wanted number of packet is received, move as well and stop this timer)
     */
    private Timer moveNextTimer = null;

    /**
     * if user wants more tx power options we need to store state about it
     */
    private int curTxPower=0;

    /**
     * Array of wanted power levels to test
     */
    private int[] wantedTxPower = null;

    /**
     * counter
     */
    private int counter=0;

    /**
     * internal packet received counter
     */
    private int packetsReceived=0;

    /**
     * Node from which I can accept messages
     */
    private int sourceNode=0;

    /**
     * mobile node
     */
    private int mobileNode=0;
    
    /**
     * node register mobile node counterpart
     * usefull when determining normalized rssi in message reception
     */
    private GenericNode mobileNodeGeneric=null;

    /**
     * Count responses per round
     */
    private int responsesPerRound=0;

    /**
     * Messages2store queue
     * Samples are stored here and at the end of the round are flushed to DB
     * It is much faster and comfortable way (can save at the end/drop)
     * Batch saving is a lot quicker
     */
    private Queue<WorkerMessageQueueElement> samples2storeQueue = null;

    /**
     * Receive from list
     * Nodes in this list are listened, packets from other nodes are dropped
     */
    private Set<Integer> receiveFromList = null;

    /**
     * Packet counter for each node
     */
    private Map<Integer, Integer> packetCounters = null;

    /**
     * When was gui updated for the last time?
     * Is compared to packet counters. Updating gui on every packet received is
     * expensive, so do it only every K message received, and on timer fired
     */
    private int lastGuiUpdate=0;

    /**
     * Update gui every <> message received
     */
    public final static int GUIUPDATE_TIMEOUT=10;

    /**
     * How many times we should try to ask talking node to talk
     */
    protected int retryCountToResend=0;

    /**
     * Data loader panel
     */
    protected jPannelRSSI2DistanceDataLoader dataLoaderPanel = null;

    /**
     * Finish this round by force?
     */
    protected boolean finishThisRound=false;

    /**
     * Send ABORT on broadcast before new round start?
     */
    protected boolean doResetBeforeRound=false;

    public WorkerR2D(MoteIF mi) {
        // init mote interface
        moteIF = mi;

        // create mobile node manager
        mobileNodeManager = RSSI_graphApp.sGetNodeRegister().getMnm();

        // init queue
        samples2storeQueue = new ArrayBlockingQueue<WorkerMessageQueueElement>(20000);

        // receive from
        receiveFromList = new HashSet<Integer>(20);

        // packet counter init
        packetCounters = new HashMap<Integer, Integer>(20);
    }

    /**
     * Logger override
     * @param s
     */
    @Override
    public void logToTextarea(String s) {
        super.logToTextarea(s, 5, "R2D", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Logger override
     *
     * @param s
     * @param severity
     */
    @Override
    public void logToTextarea(String s, int severity) {
        super.logToTextarea(s, 5, "R2D", severity);
    }



    /**
     * Turn module on
     * - register message listeners
     * - start necessary connections (sqlite)
     */
    @Override
    public void turnOn(){
        // node register
        if (this.nodeRegister==null){
            this.nodeRegister = RSSI_graphApp.sGetNodeRegister();
            this.nodeRegister.addChangeListener(this);
        }

        // register listeners for response, report
        if (moteIF != null){
            this.getMsgListener().registerListener(new CommandMsg(), this);
            this.getMsgListener().registerListener(new MultiPingResponseMsg(), this);
            this.getMsgListener().registerListener(new MultiPingResponseReportMsg(), this);
            this.getMsgListener().registerListener(new MultiPingResponseTinyReportMsg(), this);
        }

        // open sqlite database
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:meassurements_sqlite_2");
            conn.setAutoCommit(true);
        }
        catch (Exception e){
            System.err.println("SQLite exception: " + e.toString());
        }

        // get round id & test number
        try {
            setCurTestId(this.getLastTestNo()+1);
            setCurRoundId(this.getLastTestID()+1);
        }
        catch(Exception e){
            System.err.println("SQLite exception: " + e.toString());
        }

        // clear queue
        samples2storeQueue.clear();

        // clear receive list
        receiveFromList.clear();

        // try to acquire panels if not loaded not
        if (this.dataLoaderPanel == null){
            this.dataLoaderPanel = RSSI_graphApp.getApplication().getGraphViewFrame().getjPannelRSSI2DistanceDataLoader2();
        }

        // if data loader panel is active, add connection to it
        if (this.dataLoaderPanel instanceof jPannelRSSI2DistanceDataLoader){
            this.dataLoaderPanel.setConn(conn);
            this.dataLoaderPanel.turnedOnOff(true);
        }

        // init graph window
        RSSI_graphApp.getApplication().getGraphViewFrame().getjPannelRSSI2DistanceChart1().hideInfoLabel();
        RSSI_graphApp.getApplication().getGraphViewFrame().getjPannelRSSI2DistanceChart1().clear();

        // update integers to database jText_roundId
        RSSI_graphApp.getApplication().getGraphViewFrame().getjText_R2D_testNo().setText(new Integer(getCurTestId()).toString());
        RSSI_graphApp.getApplication().getGraphViewFrame().getjText_roundId().setText(new Integer(getCurRoundId()).toString());

        // fetch settings from form
        // do reset before next round?
        this.doResetBeforeRound = this.getWindow().getjCheck_R2D_resetBeforeRound().isSelected();
        this.updateReceiveList();
        
        updateNodeSelector();
        updateReceiveList();

        // log
        this.logToTextarea("Module RSSI->Distance registered and turned on", 0, "RSSI2Distance", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    /**
     * Turn off module
     * - deregister listeners
     * -
     */
    @Override
    public void turnOff(){
        // deregister listeners for response, report
        if (moteIF != null){
            this.getMsgListener().deregisterListener(new CommandMsg(), this);
            this.getMsgListener().deregisterListener(new MultiPingResponseMsg(), this);
            this.getMsgListener().deregisterListener(new MultiPingResponseReportMsg(), this);
            this.getMsgListener().deregisterListener(new MultiPingResponseTinyReportMsg(), this);
        }

        // close sqlite database
        try {
            conn.setAutoCommit(false);
            conn.commit();
            conn.close();
        }
        catch (Exception e){
            System.err.println("SQLite exception: " + e.toString() + "; " + e.getStackTrace());
            
            // log to window
            this.logToTextarea("DBbackend exception: " + e.toString(), 1, "RSSI2Distance", JPannelLoggerLogElement.SEVERITY_WARNING);
        }

        // if data loader panel is active, add connection to it
        if (this.dataLoaderPanel instanceof jPannelRSSI2DistanceDataLoader){
            this.dataLoaderPanel.setConn(null);
            this.dataLoaderPanel.turnedOnOff(false);
        }

        // log
        this.logToTextarea("Module RSSI->Distance registered and turned off", 0, "RSSI2Distance", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    @Override
    public String toString() {
        return "RSSI module";
    }

    /**
     * Start this test
     * Assumption: user does not trigger this until some test runs
     */
    public void actionStart(){
        try {
            //
            // delete all test records if exists
            deleteRoundData(getCurRoundId());

            // ensure record exists
            ensureRecordExists(getCurRoundId());

            // null packet counter
            RSSI_graphApp.getApplication().getGraphViewFrame().getjText_packetsReceived().setText("0");

            // init new TX timer if is selected option with multiple TX powers
            wantedTxPower = getWantedTX();
            moveNextTimer = new Timer(1000, this);
            moveNextTimer.setActionCommand("moveNextTimer");
            moveNextTimer.setRepeats(false);
            moveNextTimer.setDelay(2000);

            // set curr tx power counter to the first element
            setCurTxPower(0);

            // reset counter
            counter=0;
            packetsReceived=0;

            // set flag in test
            setDoingTestNow(true);

            // flush queue
            clearQueue();

            // set source node
            sourceNode = Integer.parseInt(getWindow().getjText_static().getText());
            mobileNode = lookupViewMobileNode();
            mobileNodeGeneric = this.nodeRegister.getNode(mobileNode); 

            // node lists
            updateNodeSelector();
            
            // manage receive list
            this.updateReceiveList();

            // set packet counters
            initPacketCounters();

            // since this attribute is related to packet counters, reset together with
            // packet counters
            lastGuiUpdate=0;

            // packet counter highlight on,started, 
            getWindow().setR2DPacketsHighlight(true);
            getWindow().setR2DStartHighlight(false);

            // start sending
            doSendPing();
        } catch (SQLException ex) {
            this.logToTextarea("SQL exception in [start]; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            this.logToTextarea("Nullpointer exception catched; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Updates node selector from node register
     */
    public void updateNodeSelector(){
        // get currently selected node
        int selectedIndex = this.getWindow().getjCombo_R2D_mobile().getSelectedIndex();
        
        Set<Integer> nodesSet = this.nodeRegister.getNodesSet();
        List<Integer> nodesList = new ArrayList<Integer>(nodesSet);
        Collections.sort(nodesList);
        
        Integer[] nodes = new Integer[nodesList.size()];
        Iterator<Integer> iterator = nodesList.iterator();
        for(int i=0; iterator.hasNext(); i++){
            nodes[i] = iterator.next();
        }
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(nodes);
        this.getWindow().getjCombo_R2D_mobile().setModel(model);
        
        // is possible to select previously selected?
        if (selectedIndex >=0 && this.getWindow().getjCombo_R2D_mobile().getItemCount() > selectedIndex){
            this.getWindow().getjCombo_R2D_mobile().setSelectedIndex(selectedIndex);
        }
        
        JTable jTableReceiveList = this.getWindow().getjTableReceiveList();
        Object[][] data = new Object[nodes.length][3];
        
        ReceiveListTableModel tblModel = new ReceiveListTableModel();
        for(int i=0, cnI=nodes.length; i<cnI; i++){
            data[i][0] = new Boolean(true);
            data[i][1] = new Integer(nodes[i]);
            data[i][2] = new Integer(0);
        }
        
        tblModel.setData(data);
        jTableReceiveList.setModel(tblModel);
    }

    /**
     * Updates receive from list according to data
     * @return 
     */
    public void updateReceiveList(){
        receiveFromList.clear();
        JTable jTableReceiveList = this.getWindow().getjTableReceiveList();
        TableModel model = jTableReceiveList.getModel();
        for(int i=0, cnI=model.getRowCount(); i<cnI; i++){
            Integer nodeid = (Integer) model.getValueAt(i, 1);
            Boolean selected = (Boolean) model.getValueAt(i, 0);
            
            if (selected){
                receiveFromList.add(nodeid);
            }
        }
    }
    
    class ReceiveListTableModel extends TableMyAbstractTableModel {
        public ReceiveListTableModel() {
            this.columnNames = new String[] {"Use", "Node", "Messages"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col==0;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==0) return Boolean.class;
            else return Integer.class;
        }
    }
    
    /**
     * returns current progress
     * @return
     */
    public double getTxProgress(){
        double progress = wantedTxPower.length > 0 ? ((double)getCurTxPower()) / (wantedTxPower.length) : 0.0;
        return progress > 1.0 ? 1.0 : progress;
    }

    public double getTxProgressTick(){
        return 1.0 / wantedTxPower.length;
    }

    public double getPacketCountProgress(){
        double progress = this.getPacketsCounterInfo("max") / Integer.parseInt(getWindow().getjText_packets().getText());
        return progress > 1.0 ? 1.0 : progress;
    }

    /**
     * Not very precise, only for progress bar
     * @param type
     * @return
     */
    public double getPacketsCounterInfo(String type){
        if (this.receiveFromList==null) return 0;

        int c=0;
        double result=0.0;
        Iterator<Integer> it = this.receiveFromList.iterator();
        while(it.hasNext()){
            Integer cur = it.next();
            if (!this.packetCounters.containsKey(cur)) continue;

            c++;
            int curCounter = this.packetCounters.get(cur);
            if ("min".equals(type)){
                result = Math.min(result, curCounter);
            } else if("max".equals(type)){
                result = Math.max(result, curCounter);
            } else if("avg".equals(type)){
                result += curCounter;
            }
        }

        if ("avg".equals(type) && c>0){
            result = result / c;
        }

        return result;
    }

    /**
     * Main sending method
     * do nothing if we are on the end (all tx power sent)
     * 
     * @return
     */
    public boolean doSendPing(){
        // create multiPingRequest packet and send it
        MultiPingMsg payload = new MultiPingMsg();

        // test finished?
        if (isDoingTestNow()==false) return false;

        // wanted packets
        int wantedPacketsNum = Integer.parseInt(getWindow().getjText_packets().getText());

        // test whether received number of packets are enough (95% of wanted)
        // if yes, continue with next round, otherwise send same request on new packets
        // is usefull when request before got lost or we are using mass reporting protocol
        // where packets comes in blocks. After each block, talking mote is stopped to transmit/talk
        // so we need to re-launch it
        //
        // if finishThisRound==true then force to end this round
        if (getPacketCounter(mobileNode) >= wantedPacketsNum*0.95 || finishThisRound==true){
            // increment
            setCurTxPower(getCurTxPower()+1);
            finishThisRound=false;
        }
        // otherwise ask for next packets to complete this round
        else {
            
        }

        // if we finished, quit
        if (wantedTxPower.length <= (getCurTxPower())){
            setDoingTestNow(false);

            // log partial packet counts to be able to monitor node functionality
            logToTextarea("Incoming packet counts; Overall=" + responsesPerRound +
                "; M"+mobileNode+"=" + getPacketCounter(mobileNode) +
                "; S20=" + getPacketCounter(20) +
                "; S21=" + getPacketCounter(21) +
                "; S22=" + getPacketCounter(22) +
                "; ");
            logToTextarea("RoundNumber: " + getCurRoundId() + " finished! (flush timer fired)" + RSSI_graphApp.getLineSeparator());
            updateGuiPacketCounters();

            // highlight
            getWindow().setR2DAddToGraph(true);
            getWindow().setR2DSaveNextHighlight(true);
            
            // notify user
            notifyUser();

            // flush packet queue to sqlite
            return false;
        }

        // else create packet and SEND it
        System.err.println("Sending ping request; counter="+(counter+1)+"; tx power=" + wantedTxPower[getCurTxPower()] + "; packets:" + packetsReceived);
        logToTextarea("Sending ping request; counter="+(counter+1)+"; tx power=" + wantedTxPower[getCurTxPower()]);

        // log partial packet counts to be able to monitor node functionality
        logToTextarea("Incoming packet counts; Overall=" + responsesPerRound +
                "; M"+mobileNode+"=" + getPacketCounter(mobileNode) +
                "; S20=" + getPacketCounter(20) +
                "; S21=" + getPacketCounter(21) +
                "; S22=" + getPacketCounter(22) +
                "; ");
        updateGuiPacketCounters();

        // increment send counter
        counter+=1;
        
        // delete counters
        if (getPacketCounter(mobileNode) >= wantedPacketsNum*0.95){
            // set responses per round to null since new round is started right now
            responsesPerRound=0;

            // reset internal packet counters
            initPacketCounters();
        }

        // do one cycle
        try {
            // prepare packet fields
            payload.set_txpower((short) wantedTxPower[getCurTxPower()]);
            payload.set_delay(Integer.parseInt(getWindow().getjText_delay().getText()));
            payload.set_packets(wantedPacketsNum);
            payload.set_counter(counter);
            logToTextarea(payload.toString());

            // if is wanted to send reset command before new round started, do it
            if (this.doResetBeforeRound){
                CommandMsg commandMessage = new CommandMsg();
                commandMessage.set_command_code((short)MessageTypes.COMMAND_ABORT);
                commandMessage.set_command_data(1);

                this.logToTextarea("Sending broadcast abort, sending request for next round");
                this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                        commandMessage,
                        "Broadcast abort");
            }
            
            // now use msg sender thread
            // need2think: cannot start timer immediately. need to start timer on message sent event
            this.logToTextarea("Sending round request");
            this.getMsgSender().add(lookupViewMobileNode(), payload, 
                    "R2D Ping request for="+lookupViewMobileNode()+";", this, "ping");

            // start timer
            //moveNextTimer.start();

            return true;
        }
//        catch (IOException exception) {
//            System.err.println("Exception thrown when sending packets. Exiting.");
//            System.err.println(exception);
//        }
        catch (Exception ex) {
            Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public void actionDrop(){
        try {
            // abort all nodes now
            CommandMsg commandMessage = new CommandMsg();
            commandMessage.set_command_code((short)MessageTypes.COMMAND_ABORT);
            commandMessage.set_command_data(1);

            this.logToTextarea("Sending broadcast abort, sending request for next round");
            this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                    commandMessage,
                    "Broadcast abort");

            // not in test anymore
            setDoingTestNow(false);
            moveNextTimer.stop();

            // clear packet counters
            initPacketCounters();
            updateGuiPacketCounters();
            
            //
            // delete all test records if exists
            deleteRoundData(getCurRoundId());

            // delete even round from DB
            deleteRound(getCurRoundId());

            // flush queue
            clearQueue();

            // null packet counter
            RSSI_graphApp.getApplication().getGraphViewFrame().getjText_packetsReceived().setText("0");

            // packet counter highlight off - reset. 0 packets
            getWindow().setR2DPacketsHighlight(false);
            getWindow().setR2DAddToGraph(false);
            getWindow().setR2DSaveNextHighlight(false);
            getWindow().setR2DStartHighlight(true);

            // inform
            logToTextarea("RoundNumber: " + getCurRoundId() + " dropped");
        } catch (SQLException ex) {
            this.logToTextarea("SQL exception in [start]; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            this.logToTextarea("Nullpointer exception catched; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * saveAndNext action
     * Assumption: record exists, we update it and reset needed counters
     *
     * Save changes to opened round id (annotation, distance,timeend)
     * And moves pointers
     */
    public void actionSaveAndNext(){
        try {
            // ensure record exists
            ensureRecordExists(getCurRoundId());
            conn.setAutoCommit(false);

            // update record in database - save annotation, distance and timeend only
            PreparedStatement prep=conn.prepareStatement("UPDATE rssi2dist_measurement SET anotation=?, distance=?, timeend=? WHERE mid=?;");
            prep.setString(1, getWindow().getjText_anotate().getText());
            prep.setString(2, getWindow().getjText_R2DDistance().getText());
            prep.setLong(3, System.currentTimeMillis()/1000);
            prep.setInt(4, getCurRoundId());
            prep.executeUpdate();
            prep.close();
            conn.commit();
            conn.setAutoCommit(true);
            
            // finaly update counters
            //setCurTestId(this.getLastTestNo());
            //setCurRoundId(this.getLastTestID() + 1);
            setCurRoundId(getCurRoundId()+1);

            // reflect to area
            getWindow().getjText_roundId().setText(Integer.toString(getCurRoundId()));

            // save queue
            writeQueue();

            this.packetsReceived=0;
            this.responsesPerRound=0;
            
            // null packet counter
            RSSI_graphApp.getApplication().getGraphViewFrame().getjText_packetsReceived().setText("0");

            // highlight parameter, user may update it
            getWindow().setR2DParameterHighlight(true);

            // packet counter highlight off - saved. 0packets
            getWindow().setR2DPacketsHighlight(false);
            getWindow().setR2DAddToGraph(false);
            getWindow().setR2DSaveNextHighlight(false);
            getWindow().setR2DStartHighlight(true);

            logToTextarea("Round no: " + (getCurRoundId()-1) + "saved; continue");
            System.err.println("Round no: " + (getCurRoundId()-1) + "saved; continue");
            
        } catch (SQLException ex) {
            this.logToTextarea("SQL exception in [start]; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            this.logToTextarea("Nullpointer exception catched; please check it: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);

            System.err.println(ex);
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void actionAbort(){
        
    }

    /**
     * Finish current round - do not try to re-send pings
     */
    public void doFinishRound(){
        finishThisRound=true;

        // fire immediately
        // time would fire this event lately
        this.doSendPing();
    }

    /**
     * MoveNext timer control
     *
     * @param turnOn
     */
    public void doSetTimer(boolean turnOn){
        if (turnOn==false && this.moveNextTimer.isRunning()){
            this.moveNextTimer.stop();
            this.logToTextarea("Timer was stopped", JPannelLoggerLogElement.SEVERITY_INFO);
        }
        else if(turnOn==true && this.moveNextTimer.isRunning()==false){
            this.moveNextTimer.start();
            this.logToTextarea("Timer was started", JPannelLoggerLogElement.SEVERITY_INFO);
        }
    }

    /**
     * Move forward (after button hit)
     */
    public void doMoveStep(){
        // fire immediately
        // time would fire this event lately
        this.doSendPing();
    }

    /**
     * Prepare network, set mass report protocol
     */
    public void doPrepareMass(){
        // prepare network by acknowledged messages
        // at first set network by broadcast flood
        
         // abort all nodes now
        CommandMsg commandMessage = new CommandMsg();
        commandMessage.set_command_code((short)MessageTypes.COMMAND_RESET);
        commandMessage.set_command_data(1);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                commandMessage,
                "Broadcast reset");

        CommandMsg protocolCommandMessage = new CommandMsg();
        protocolCommandMessage.set_command_code((short)MessageTypes.COMMAND_SETREPORTPROTOCOL);
        protocolCommandMessage.set_command_data(MessageTypes.REPORTING_MASS);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
               protocolCommandMessage,
                "Mass reporting protocol");

        CommandMsg thresholdCommandMessage = new CommandMsg();
        thresholdCommandMessage.set_command_code((short)MessageTypes.COMMAND_SETQUEUEFLUSHTHRESHOLD);
        thresholdCommandMessage.set_command_data(150);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                thresholdCommandMessage,
                "Threshold 200", this, "prepareMass");

        // send messages for each static node separately
        Set<Integer> nodesSet = this.nodeRegister.getNodesSet();
        Iterator<Integer> nodeIt = nodesSet.iterator();
        while(nodeIt.hasNext()){
            Integer destination = nodeIt.next();
            GenericNode node = this.nodeRegister.getNode(destination);
            // skip base station and mobile nodes
            if (node==null || node.isAnchor()==false || destination==1) continue;

            // init watchers at first
            // protocol and threshold
            int curCounter = this.getMsgSender().getMessageDeliveryGuarantor().getNextMessageId();
            CommandMsg commandMessage1 = new CommandMsg();
            commandMessage1.set_command_code((short)MessageTypes.COMMAND_RESET);
            commandMessage1.set_command_data(1);
            commandMessage1.set_command_id(curCounter);
            
            curCounter = this.getMsgSender().getMessageDeliveryGuarantor().getNextMessageId();
            CommandMsg protocolCommandMessage1 = new CommandMsg();
            protocolCommandMessage1.set_command_code((short)MessageTypes.COMMAND_SETREPORTPROTOCOL);
            protocolCommandMessage1.set_command_data(MessageTypes.REPORTING_MASS);
            protocolCommandMessage1.set_command_id(curCounter);
            
            curCounter = this.getMsgSender().getMessageDeliveryGuarantor().getNextMessageId();
            CommandMsg thresholdCommandMessage1 = new CommandMsg();
            thresholdCommandMessage1.set_command_code((short)MessageTypes.COMMAND_SETQUEUEFLUSHTHRESHOLD);
            thresholdCommandMessage1.set_command_data(150);
            thresholdCommandMessage1.set_command_id(curCounter);

            curCounter = this.getMsgSender().getMessageDeliveryGuarantor().getNextMessageId();
            CommandMsg gapCommandMessage1 = new CommandMsg();
            gapCommandMessage1.set_command_code((short)MessageTypes.COMMAND_SETREPORTGAP);
            gapCommandMessage1.set_command_data(15);
            gapCommandMessage1.set_command_id(curCounter);
            
            // watchers
            MessageToSend gapMsg = new MessageToSend(gapCommandMessage1, destination, "Gap for " + destination);
            DefaultMessageDeliveryGuarantorWatcher watcherGap = new DefaultMessageDeliveryGuarantorWatcher(0);
            watcherGap.setMsgToSend(gapMsg);
            watcherGap.setGuarantor(this.getMsgSender().getMessageDeliveryGuarantor());
            watcherGap.setListener(this);
            watcherGap.setListenerKey("prepareMass");

            MessageToSend thresholdMsg = new MessageToSend(thresholdCommandMessage1, destination, "Threshold for " + destination);
            DefaultMessageDeliveryGuarantorWatcher watcherThreshold = new DefaultMessageDeliveryGuarantorWatcher(0);
            watcherThreshold.setMsgToSend(thresholdMsg);
            watcherThreshold.setGuarantor(this.getMsgSender().getMessageDeliveryGuarantor());
            watcherThreshold.setListener(this);
            watcherThreshold.setListenerKey("prepareMass");
            watcherThreshold.addChild(watcherGap);

            MessageToSend protocolMsg = new MessageToSend(protocolCommandMessage1, destination, "Protocol for " + destination);
            DefaultMessageDeliveryGuarantorWatcher watcherProtocol = new DefaultMessageDeliveryGuarantorWatcher(0);
            watcherProtocol.setMsgToSend(protocolMsg);
            watcherProtocol.setGuarantor(this.getMsgSender().getMessageDeliveryGuarantor());
            watcherProtocol.setListener(this);
            watcherProtocol.setListenerKey("prepareMass");
            watcherProtocol.addChild(watcherThreshold);

            MessageToSend resetMsg = new MessageToSend(commandMessage1, destination, "Reset for " + destination);
            DefaultMessageDeliveryGuarantorWatcher watcherReset = new DefaultMessageDeliveryGuarantorWatcher(0);
            watcherReset.setMsgToSend(resetMsg);
            watcherReset.setGuarantor(this.getMsgSender().getMessageDeliveryGuarantor());
            watcherReset.setListener(this);
            watcherReset.setListenerKey("prepareMass");
            watcherReset.addChild(watcherProtocol);

            // add root
            this.getMsgSender().getMessageDeliveryGuarantor().add(watcherReset);
        }
    }

    /**
     * Prepare network, set medium report protocol as default
     */
    public void doPrepareMedium(){
        CommandMsg commandMessage = new CommandMsg();
        commandMessage.set_command_code((short)MessageTypes.COMMAND_RESET);
        commandMessage.set_command_data(1);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                commandMessage,
                "Broadcast reset");

        commandMessage = new CommandMsg();
        commandMessage.set_command_code((short)MessageTypes.COMMAND_SETREPORTPROTOCOL);
        commandMessage.set_command_data(MessageTypes.REPORTING_MEDIUM);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                commandMessage,
                "Mass reporting protocol");

        commandMessage = new CommandMsg();
        commandMessage.set_command_code((short)MessageTypes.COMMAND_SETQUEUEFLUSHTHRESHOLD);
        commandMessage.set_command_data(4);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                commandMessage,
                "Threshold 4");

        commandMessage = new CommandMsg();
        commandMessage.set_command_code((short)MessageTypes.COMMAND_SETDORANDOMIZEDTHRESHOLDING);
        commandMessage.set_command_data(1);
        this.getMsgSender().add(MoteIF.TOS_BCAST_ADDR,
                commandMessage,
                "Randomized thresholding", this, "prepareMedium");
    }

    /**
     * Adds current data to graph
     */
    public void addToGraph(){
        // do we have data to put to the graph?
        
        //samples2storeQueue
        //WorkerMessageQueueElement
        //RSSI2DistInternalBuffer
        int curTestNo = lookupViewTestNo();
        int curMobileNode = lookupViewMobileNode();
        int curDistance = lookupViewDistance();
        int curCurRoundId = lookupViewRoundId();

        // convert my samples2storeQueue to RSSI2DistInternalBuffer
        // RSSI2DistInternalBuffer holds all RSSI readings for specified distance,txpower,nodes,testno,...
        // WorkerMessageQueueElement is simple single record (rssi, source, txpower, counter,round_id)
        // thus we need to sort it to buckets. 1 bucket = (source,txpower,round_id)
        // thus we define own hasher for this buckets.
        //
        // This could be in separated method (WorkerMessageQueueElement -> RSSI2DistInternalBuffer)
        BucketHasherInterface<WorkerMessageQueueElement> hasher = new DefaultBucketHasherImpl<WorkerMessageQueueElement>(){
            @Override
            public int getHashCodeFor(WorkerMessageQueueElement a) {
                int hash = 3;
                hash = 67 * hash + a.getRound_id();
                hash = 67 * hash + a.getSource();
                hash = 67 * hash + a.getTxpower();
                return hash;
            }

            @Override
            public String getStringHashFor(WorkerMessageQueueElement a) {
                return a.getRound_id()+";"+a.getSource()+";"+a.getTxpower();
            }

        };
        BucketHasher<WorkerMessageQueueElement> bhasher = new BucketHasher<WorkerMessageQueueElement>(hasher);
        Map<String, ArrayList<WorkerMessageQueueElement>> buckets = bhasher.makeBuckets(
                new ArrayList<WorkerMessageQueueElement>(this.samples2storeQueue));

        // now create ArrayList<RSSI2DistInternalBuffer> from buckets
        ArrayList<RSSI2DistInternalBuffer> tmpBuffer = new ArrayList<RSSI2DistInternalBuffer>(this.samples2storeQueue.size());

        // iterate over buckets and create records
        Set<String> bucketTitle = buckets.keySet();
        Iterator<String> itKey = bucketTitle.iterator();
        for(int i=0; itKey.hasNext(); i++){
            String curKey = itKey.next();
            if (curKey==null || !buckets.containsKey(curKey)){
                this.logToTextarea("Something is wrong with curKey here, should throw exception", JPannelLoggerLogElement.SEVERITY_WARNING);
                continue;
            }

            ArrayList<WorkerMessageQueueElement> tmpArrListWMQE = buckets.get(curKey);
            if (tmpArrListWMQE==null || tmpArrListWMQE.isEmpty()){
                continue;
            }

            // get first element from this list to instantiate internal buffer
            WorkerMessageQueueElement firstWMQE = tmpArrListWMQE.get(0);
            if(firstWMQE == null){
                this.logToTextarea("Weird state occurred, WMQE in bucket array list is null, why?", JPannelLoggerLogElement.SEVERITY_WARNING);
                continue;
            }           

            //int testno, int mid, int txpower, int distance, int talkingMote, int reportingMote
            RSSI2DistInternalBuffer newRDBuffer = new RSSI2DistInternalBuffer(
                    curTestNo,
                    curCurRoundId,
                    firstWMQE.getTxpower(),
                    curDistance,
                    curMobileNode,
                    firstWMQE.getSource());

            // extract RSSI data from buckets
            Integer[] data = new Integer[tmpArrListWMQE.size()];
            Iterator<WorkerMessageQueueElement> itWMQE = tmpArrListWMQE.iterator();
            for(int j=0; itWMQE.hasNext(); j++){
                WorkerMessageQueueElement anotherWMQE = itWMQE.next();
                if (anotherWMQE==null){
                    this.logToTextarea("Weird state occurred, WMQE in bucket array list is null, why? (in iteration, first was ok)", JPannelLoggerLogElement.SEVERITY_WARNING);
                    continue;
                }

                data[j] = new Integer(anotherWMQE.getRssi());
            }

            newRDBuffer.loadData(data);
            tmpBuffer.add(newRDBuffer);
        }

        // add to graph now
        getWindow().getjPannelRSSI2DistanceChart1().addRSSIData(tmpBuffer);
        getWindow().getjPannelRSSI2DistanceChart1().fireDataChanged();

        // button highlight
        getWindow().setR2DAddToGraph(false);
    }

    /**
     * Message received event
     *
     * @param to
     * @param msg
     */
    @Override
    public synchronized void messageReceived(int to, Message msg) {
        try {
//        if (isDoingTestNow()==false){
//            System.err.println("R2D received unexpected packet");
//        }
        //System.err.println("R2D messageReceived");
        // accept messages only from targeted node
        Integer messageFrom = new Integer(msg.getSerialPacket().get_header_src());
        if (receiveFromList.contains(messageFrom) == false){
            return;
        }

        // message arrived, stop timer
        if (moveNextTimer==null) return;
        moveNextTimer.stop();
        boolean startTimer=true;
        
        // normalized rssi
        int normalizedRssi=0;

        // current packet counter for message source
        // since its reference, it should be automaticaly updated in map
        Integer curPacketCounter=packetCounters.get(messageFrom);

        if (msg.amType() == MessageTypes.AM_MULTIPINGRESPONSEMSG && msg instanceof MultiPingResponseMsg){
            MultiPingResponseMsg Message = (MultiPingResponseMsg) msg;
            
            if (!(this.mobileNodeGeneric instanceof GenericNode)){
                throw new IllegalArgumentException("Node is null");
            }
            
            normalizedRssi = (int) this.mobileNodeGeneric.getNormalizedRssi(Message.get_rssi(), getCurrentTX());
            storeRssiData(Message.getSerialPacket().get_header_src(), Message.get_counter(), normalizedRssi);

            // increment counter
            packetsReceived+=1;
            responsesPerRound+=1;
            curPacketCounter = curPacketCounter + 1;
            
        }
        else if (msg.amType() == MessageTypes.AM_MULTIPINGRESPONSETINYREPORTMSG && msg instanceof MultiPingResponseTinyReportMsg){
            MultiPingResponseTinyReportMsg messageTinyReport = (MultiPingResponseTinyReportMsg) msg;
            
            if (!(this.mobileNodeGeneric instanceof GenericNode)){
                throw new IllegalArgumentException("Node is null");
            }
            
            normalizedRssi = (int) this.mobileNodeGeneric.getNormalizedRssi(messageTinyReport.get_rssi(), getCurrentTX());
            storeRssiData(messageTinyReport.getSerialPacket().get_header_src(), messageTinyReport.get_nodecounter(), normalizedRssi);

            // increment counter
            packetsReceived+=1;
            responsesPerRound+=1;
            curPacketCounter = curPacketCounter + 1;
        }
        else if (msg.amType() == MessageTypes.AM_MULTIPINGRESPONSEREPORTMSG && msg instanceof MultiPingResponseReportMsg){
            MultiPingResponseReportMsg messageReport = (MultiPingResponseReportMsg) msg;
            
            // batch insert will be faster
            for(int i = 0; i<messageReport.get_datanum(); i++){
                if (messageReport.get_nodeid()[i] != mobileNode) {
                   System.err.println("Ignoring message from: " + messageReport.get_nodeid()[i]);
                   continue;
                }
                
                if (!(this.mobileNodeGeneric instanceof GenericNode)){
                    throw new IllegalArgumentException("Node is null");
                }
                
                normalizedRssi = (int) this.mobileNodeGeneric.getNormalizedRssi(messageReport.get_rssi()[i], getCurrentTX());
                storeRssiData(messageReport.getSerialPacket().get_header_src(), messageReport.get_nodecounter()[i], normalizedRssi);

                // increment counter
                packetsReceived+=1;
                responsesPerRound+=1;
                curPacketCounter = curPacketCounter + 1;
            }
        }
        else if (msg.amType() == MessageTypes.AM_COMMANDMSG && msg instanceof CommandMsg){
            CommandMsg CommandMessage = (CommandMsg) msg;

            // if it is queueFlush command, make note about it
            if (CommandMessage.get_command_code() == MessageTypes.COMMAND_FLUSHREPORTQUEUE){
                logToTextarea("Mass report is in progress");
                
            }
            else {
                //logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src() + "; body=" + msg.toString());
                logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src()
                    + "; code=" + CommandMessage.get_command_code()
                    + "; reply=" + CommandMessage.get_reply_on_command()
                    + "; data=" + CommandMessage.get_command_data()
                    + "; seq=" + CommandMessage.get_command_id()
                    );
            }

            startTimer=false;
        }
        else {
            System.err.println("R2D, unexpected message received: " + msg.toString());
            return;
        }

        // i was wrong, i've got copy, need to insert
        packetCounters.put(messageFrom, curPacketCounter);

        // update gui ?
        // timeout needed to perform faster since GUI update is expensive operation
        if (lastGuiUpdate + GUIUPDATE_TIMEOUT <= packetsReceived){
            getWindow().getjText_packetsReceived().setText(Integer.toString(packetsReceived));

            // packet counters
            updateGuiPacketCounters();
            lastGuiUpdate=packetsReceived;
        }

        if (startTimer){
            // on every received packet start one shot timer
            moveNextTimer.start();
        }
        } catch(Exception ex){
            Logger.getLogger(WorkerR2D.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("R2D; Error catched: " + ex.getMessage());
        }
    }

    /**
     * Message sent event
     * 
     * @param listenerKey
     * @param msg
     * @param destination
     */
    @Override
    public synchronized void messageSent(String listenerKey, Message msg, int destination) {
        // since we are sending only ping requests, start timer now
        if ("ping".equals(listenerKey)){
            synchronized(moveNextTimer){
                if (moveNextTimer.isRunning()) return;
                moveNextTimer.start();
            }
        }

        else if ("prepareMass".equals(listenerKey)){
            this.logToTextarea("Network prepared (mass)", JPannelLoggerLogElement.SEVERITY_INFO);
        }

        else if ("prepareMedium".equals(listenerKey)){
            this.logToTextarea("Network prepared (medium)", JPannelLoggerLogElement.SEVERITY_INFO);
        }
        else {
            this.logToTextarea("Unknown message sent!", JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }

    /**
     * Item state changed for toggle buttons and check boxes
     * @param e
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        // check box
        if (javax.swing.JCheckBox.class.equals(e.getItem().getClass())) {
            final JCheckBox jcheck = (JCheckBox) e.getItem();

            // auto node discovery from discovery thread
            if ("resetBeforeRound".equals(jcheck.getActionCommand())){
                // set local boolean
                // if true then send abort packet on broadcast before new round started
                doResetBeforeRound = e.getStateChange() == ItemEvent.SELECTED;

                if(e.getStateChange() == ItemEvent.SELECTED) {
                    this.logToTextarea("Reset before new round will be commited", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
                else {
                    this.logToTextarea("Reset before new round will NOT be commited", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
            }
            else {
                System.err.println("Checkbox unknown to me was triggered");
            }
        }
                
//        else if (javax.swing.JComboBox.class.equals(e.getItem().getClass())) {
//            final JComboBox jcombo = (JComboBox) e.getItem();
//            
//            if ("R2DSelectedNode".equals(jcombo.getActionCommand())){
//                // select appropriate tx powers
//                this.nodeSelectionChanged();
//            } else {
//                System.err.println("Checkbox unknown to me was triggered");
//            }
//        }
        
        // toggle ON button
        else if(e.getItem().equals(RSSI_graphApp.getApplication().getGraphViewFrame().getjToggle_R2D_ON())) {
            System.err.print("R2D action button pressed; ");

            if (e.getStateChange() == ItemEvent.SELECTED){
                System.err.println("Selected, registering and starting");
                turnOn();
            }
            else {
                System.err.println("DESelected, deregistering and closing db");
                turnOff();
            }
        }

        // all other toggle buttons
        else if (javax.swing.JToggleButton.class.equals(e.getItem().getClass())) {
            final JToggleButton jtog = (JToggleButton) e.getItem();
            if ("TimerOnOff".equals(jtog.getActionCommand())){

                doSetTimer(e.getStateChange() == ItemEvent.SELECTED);
                System.err.println("TimerOnOff triggered");
            }
            else {
                System.err.println("Toggle button unknown to me was triggered");
            }
        }
        else {    
            // debug output for better binding
            System.err.println("");
            System.err.println("itemStateChanged to unrecognized object");
            System.err.println("itemToStrong: " + e.getItem().toString());
            System.err.println("itemClassToStrong: " + e.getItem().getClass().toString());
            System.err.println("itemSelectableToStrong: " + e.getItemSelectable().toString());
            System.err.println("itemSelectable.getSelectedObjects==null: " + (e.getItemSelectable().getSelectedObjects()==null ?
                -1 : e.getItemSelectable().getSelectedObjects().length));
            System.err.println("paramString: " + e.paramString());
            System.err.println("getSource().toString(): " + e.getSource().toString());
            System.err.println("getSource().getClass().toString(): " + e.getSource().getClass().toString());
            System.err.println("JToggle to string: " + javax.swing.JToggleButton.class.toString());
        }
    }

    /**
     * Button action handlers
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // if DROP is pressed
        // old way of determining action
        //if (e.getSource().equals( RSSI_graphApp.getApplication().getGraphViewFrame().getjButton_drop() )){
        
        if ("Drop".equals(e.getActionCommand())) {
           System.err.println("R2D; Drop  test");
           actionDrop();
        }
        else if ("SaveAndNext".equals(e.getActionCommand()) ){
            System.err.println("R2D; Saving test and moving on");
            actionSaveAndNext();
        }
        else if ("Start".equals(e.getActionCommand()) ){
            System.err.println("R2D; Starting with test");
            actionStart();
        }
        else if ("moveNextTimer".equals(e.getActionCommand())){
            System.err.println("R2D; Move next timer fired");
            doSendPing();
        }
        else if ("FinishRound".equals(e.getActionCommand())){
            System.err.println("R2D; Finish round pressed");
            doFinishRound();
        }
        else if ("AddToGraph".equals(e.getActionCommand())){
            System.err.println("R2D; Add to graph");
            addToGraph();
        }
        else if ("MoveStep".equals(e.getActionCommand())){
            System.err.println("R2D; Move step");
            doMoveStep();
        }
        else if ("PrepareMedium".equals(e.getActionCommand())){
            System.err.println("R2D; PrepareMedium");
            doPrepareMedium();
        }
        else if ("PrepareMass".equals(e.getActionCommand())){
            System.err.println("R2D; PrepareMass");
            doPrepareMass();
        }
        else if ("R2DSelectedNode".equals(e.getActionCommand())){
            //System.err.println("R2D; R2DSelectedNode");
            this.nodeSelectionChanged();
        }        
        else {
            throw new UnsupportedOperationException("I don't know such event!");
        }
    }
    
    /**
     * React on node selection change by setting correct values to tx power selector
     */
    protected void nodeSelectionChanged(){
        // select appropriate tx powers
        NodeSelectedEvent evt = new NodeSelectedEvent();
        int nodeid = this.getWindow().getjCombo_R2D_mobile().getItemCount()>0 ? Integer.valueOf((Integer) this.getWindow().getjCombo_R2D_mobile().getSelectedItem()) : -1;
        evt.selectedNodes = new int[1];
        evt.selectedNodes[0] = nodeid;
        this.getWindow().getjPanelTXpowerSelector1().nodeChanged(evt);
    }

    /**
     * returns ID of last test in db
     * @return
     */
    public int getLastTestID() throws SQLException{
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select `mid` FROM `rssi2dist_measurement` ORDER BY `mid` DESC LIMIT 1;");

        int lastId = rs.next() ? rs.getInt("mid") : 0;
        rs.close();

        return lastId;
    }

    /**
     * returns last test Number
     * @return
     */
    public int getLastTestNo() throws SQLException{
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT `testno` FROM `rssi2dist_measurement` ORDER BY `testno` DESC LIMIT 1;");

        int lastId = rs.next() ? rs.getInt("testno") : 0;
        rs.close();

        return lastId;
    }

    /**
     * delete all round data
     * @param r
     */
    public void deleteRoundData(int r) throws SQLException{
        PreparedStatement prep = conn.prepareStatement("DELETE FROM rssi2dist_measurement_data WHERE mid=?;");
        prep.setInt(1, r);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * delete round/test data
     * @param r
     */
    public void deleteRound(int r) throws SQLException{
        PreparedStatement prep = conn.prepareStatement("DELETE FROM rssi2dist_measurement WHERE mid=?;");
        prep.setInt(1, r);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * Ensures that record with id=r exists in database
     * If not, creates one
     * 
     * @param r
     * @throws SQLException
     */
    public void ensureRecordExists(int r) throws SQLException{
        PreparedStatement prep = conn.prepareStatement("SELECT mid FROM rssi2dist_measurement WHERE mid=?;");
        prep.setInt(1, r);
        ResultSet res = prep.executeQuery();
        if (res.next()) {
            res.close();
            return;
        }

        // test id re-read
        setCurTestId(lookupViewTestNo());

        // record does not exists, create new
        prep=conn.prepareStatement("INSERT INTO rssi2dist_measurement VALUES(?,?,?,?,?,?,'empty',?,?,0);");
        prep.setInt(1, r);
        prep.setInt(2, lookupViewMobileNode());
        prep.setInt(3, new Integer(RSSI_graphApp.getApplication().getGraphViewFrame().getjText_static().getText()).intValue());
        prep.setLong(4, System.currentTimeMillis()/1000);
        prep.setInt(5, new Integer(RSSI_graphApp.getApplication().getGraphViewFrame().getjText_packets().getText()).intValue());
        prep.setInt(6, new Integer(RSSI_graphApp.getApplication().getGraphViewFrame().getjText_delay().getText()).intValue());
        prep.setInt(7, new Integer(RSSI_graphApp.getApplication().getGraphViewFrame().getjText_R2DDistance().getText()).intValue());
        prep.setInt(8, getCurTestId());
        prep.executeUpdate();
        prep.close();
    }

    /**
     * store measured data to queue
     * 
     * @param counter
     * @param rssi
     */
    public void storeRssiData(int source, int counter, int rssi){
        //TODO!!!
        if ((wantedTxPower.length-1) < getCurTxPower()) return;
        WorkerMessageQueueElement welem = new WorkerMessageQueueElement(getCurRoundId(), source, getCurrentTX(), counter, rssi);
        samples2storeQueue.add(welem);
    }

    /**
     * clear sample queue
     */
    public void clearQueue(){
        samples2storeQueue.clear();
    }

    /**
     * flush message queue to db
     */
    public void writeQueue() throws SQLException{
        PreparedStatement prep;
        prep = conn.prepareStatement("INSERT INTO rssi2dist_measurement_data VALUES(NULL,?,?,?,?,?);");

        // batch insert will be faster
        Iterator<WorkerMessageQueueElement> it = samples2storeQueue.iterator();

        // element
        WorkerMessageQueueElement welem=null;

        while(it.hasNext()){
            welem = it.next();

            prep.setInt(1, welem.getRound_id());
            prep.setInt(2, welem.getSource());
            prep.setInt(3, welem.getTxpower());
            prep.setInt(4, welem.getCounter());
            prep.setInt(5, welem.getRssi());
            prep.addBatch();
        }

        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
        prep.close();

        // clear queue
        clearQueue();

        logToTextarea("Queue flushed");
    }

    /**
     * Initialize packet counters from receiveFromList to zero
     */
    public void initPacketCounters(){
        packetCounters.clear();

        // iterate over hashSet and add it to packet counters
        Iterator<Integer> it = receiveFromList.iterator();
        while(it.hasNext()){
            packetCounters.put(it.next(), new Integer(0));
        }
    }

    /**
     * Returns packet counter value
     */
    public int getPacketCounter(int nodeid){
        Integer tmp = new Integer(nodeid);
        return packetCounters.containsKey(tmp) ? packetCounters.get(tmp).intValue() : 0;
    }
    
    /**
     * String version of getPacketCounter
     * 
     * @param s
     * @return
     */
    public int getPacketCounter(String s){
        return getPacketCounter(Integer.parseInt(s));
    }

    /**
     * Helper method, update gui packet counters
     */
    public void updateGuiPacketCounters(){
        // packet counters
        JTable jTableReceiveList = getWindow().getjTableReceiveList();
        TableModel model = jTableReceiveList.getModel();
        
        for (int i=0, cnI=model.getRowCount(); i<cnI; i++){
            Integer nodeId = (Integer) model.getValueAt(i, 1);
            model.setValueAt(Integer.valueOf(getPacketCounter(nodeId)), i, 2);
        }

        double txProgress = this.getTxProgress();
        double packetProgress = this.getPacketCountProgress();
        double result = txProgress + this.getTxProgressTick() * packetProgress;
        getWindow().getjProgress_R2D_progress().setValue((int)Math.round(result*100));
    }


    /**
     * Returns array of wanted TX power to try according to selected radiobutton
     * on main control panel.
     * If 1TX is selected than read value from slider
     *
     * Change: now supports multiple nodes platforms
     * @return
     */
    public int[] getWantedTX(){
        return this.getWindow().getjPanelTXpowerSelector1().getWantedTx();
    }
    
    /**
     * Returns currenlty TX power used in measurements
     * @return 
     */
    public int getCurrentTX(){
        return (wantedTxPower.length > getCurTxPower()) ? 
            wantedTxPower[getCurTxPower()] : 
            999;
    }

    /**
     * Lookup testNo field displayed on the form
     * @return
     */
    public int lookupViewTestNo(){
        return Integer.parseInt(getWindow().getjText_R2D_testNo().getText());
    }

    /**
     * Lookup node id of mobile node
     * @return
     */
    public int lookupViewMobileNode(){
        return Integer.valueOf((Integer) getWindow().getjCombo_R2D_mobile().getSelectedItem());
    }

    /**
     * Lookup distance displayed on panel
     * @return
     */
    public int lookupViewDistance(){
        return Integer.parseInt(getWindow().getjText_R2DDistance().getText());
    }

    /**
     * Lookup RoundID displayed on panel
     * @return
     */
    public int lookupViewRoundId(){
        return Integer.parseInt(getWindow().getjText_roundId().getText());

    }

    /**
     * =========================================================================
     *
     * GETTERS+SETTERS
     *
     * =========================================================================
     */

    public Connection getConn() {
        return conn;
    }

    public int getCurRoundId() {
        return curRoundId;
    }

    public void setCurRoundId(int curRoundId) {
        this.curRoundId = curRoundId;
    }

    public int getCurTestId() {
        return curTestId;
    }

    public void setCurTestId(int curTestId) {
        this.curTestId = curTestId;
    }

    public int getCurTxPower() {
        return curTxPower;
    }

    public void setCurTxPower(int curTxPower) {
        this.curTxPower = curTxPower;
    }

    public boolean isDoingTestNow() {
        return doingTestNow;
    }

    public void setDoingTestNow(boolean doingTestNow) {
        this.doingTestNow = doingTestNow;

        // button enabled?
        getWindow().getjToggle_R2D_Timer().setEnabled(doingTestNow);
    }

    public jPannelRSSI2DistanceDataLoader getDataLoaderPanel() {
        return dataLoaderPanel;
    }

    public void setDataLoaderPanel(jPannelRSSI2DistanceDataLoader dataLoaderPanel) {
        this.dataLoaderPanel = dataLoaderPanel;
        if (this.dataLoaderPanel == null) return;

        // set this worker for panel
        this.dataLoaderPanel.setWorkerR2D(this);
    }

    public void messageDeliveryEventAccepted(MessageDeliveryEvent evt) {
        if (evt==null) return;
        if ("prepareMass".equals(evt.getListenerKey())){
            if (evt.getMsgToSend()==null
                    || evt.getMsgToSend().getsMsg()==null
                    || !(evt.getMsgToSend().getsMsg() instanceof CommandMsg)
                    ) return;

            final DefaultMessageDeliveryGuarantorWatcher watcher = (DefaultMessageDeliveryGuarantorWatcher) evt.getWatcher();
            final CommandMsg commandMsg = (CommandMsg) evt.getMsgToSend().getsMsg();
            if (evt.getState() == MessageDeliveryEvent.STATE_DEAD){
                this.logToTextarea("Prepare failed for node: " + evt.getMsgToSend().getDestination() + "; code=" + commandMsg.get_command_code(), JPannelLoggerLogElement.SEVERITY_ERROR);
            }
            else if (evt.getState() == MessageDeliveryEvent.STATE_SENT_OK){
                // notify only if message has no children => end of chain
                if (watcher.getChildren() != null && !watcher.getChildren().isEmpty()){
                    return;
                }

                // has listeners => display info
                this.logToTextarea("Node " + evt.getMsgToSend().getDestination() + " prepared", JPannelLoggerLogElement.SEVERITY_INFO);
            }

        }
        else{
            this.logToTextarea("Unknown event dispatched", JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }

    @Override
    public synchronized void accept(NodeRegisterEvent evt) {
        // node register change, reload node list
         if (evt!=null && evt.getEventType() != NodeRegisterEvent.EVENT_TYPE_DATA_CHANGED) return;
        
        // no changes - do it
        if (evt.changes==null){
            updateNodeSelector();
            return;
        }
        
        // do not refresh on each data change - light intensity is not interesting for us for instance
        boolean doRefresh = false;
        Iterator<Integer> iterator = evt.changes.keySet().iterator();
        while(iterator.hasNext()){
            String curChange = evt.changes.get(iterator.next());
            if (curChange==null){
                doRefresh=true;
                break;
            }
        }
        
        if (doRefresh){
            updateNodeSelector();
        }
    }
}
