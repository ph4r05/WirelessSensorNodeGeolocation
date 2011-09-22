/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.messages.CommandMsg;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.NodePlatform;

/**
 *
 * @author ph4r05
 */
public class WorkerCommands extends WorkerBase implements MessageListener, WorkerInterface, ActionListener, NodeSelectionChangedListener  {
    /**
     * If test is in progress then TRUE
     */
    private boolean doingTestNow=false;
    
    /**
     * mobile node
     */
    private int destNode=0;

    /**
     * PacketID counter
     */
    private int counter;

    public WorkerCommands(MoteIF mi) {
        // init mote interface
        moteIF = mi;
        
    }
    
    /**
     * Logger override
     * @param s
     */
    @Override
    public void logToTextarea(String s) {
        super.logToTextarea(s, 9, "Command", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Logger override
     *
     * @param s
     * @param severity
     */
    @Override
    public void logToTextarea(String s, int severity) {
        super.logToTextarea(s, 9, "Command", severity);
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
        }

        // register listeners for response, report
        if (moteIF != null){
            moteIF.registerListener(new CommandMsg(), this);
        }

        // make sure that toggle button is pressed now
        this.getWindow().getjPanel_comm_NodeSelector().initThis();
        this.getWindow().getjPanel_comm_NodeSelector().addNodeSelectionChangedListener(this);

        // update status
        this.logToTextarea("Command module turned on", JPannelLoggerLogElement.SEVERITY_INFO);
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
            moteIF.deregisterListener(new CommandMsg(), this);
        }
        
        // remove me as node selector change listener
        this.getWindow().getjPanel_comm_NodeSelector().removeNodeSelectionChangedListener(this);

        // status
        this.logToTextarea("Command module turned off", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    @Override
    public String toString() {
        return "ControlCentre module";
    }

    /**
     * Start this test
     * Assumption: user does not trigger this until some test runs
     */
    public void actionStart(){
        
    }

    /**
     * Send selected defined packet to selected nodes.
     * Another methods may build custom command packet, it is then passed to this method
     * which sends it to all selected nodes
     * 
     * @param CommandMsg payload    data packet to send. Is CommandMessage
     */
    public void sendMyCommandToSelectedNodes(CommandMsg payload){
        try {
            int selectedNodes[] = getWindow().getjPanel_comm_NodeSelector().getSelectedNodes();
            if (selectedNodes==null || selectedNodes.length<=0) return;
                        
            if (this.getMsgSender().canAdd()==false){
                logToTextarea("Cannot add commands to send queue.", JPannelLoggerLogElement.SEVERITY_ERROR);
            }

            for(int i=0, cnI=selectedNodes.length; i<cnI; i++) {
                this.sendMyCommandToNode(payload, selectedNodes[i]);
            }

        }  catch (Exception ex) {
            logToTextarea("NullPointer exception: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
            Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Send selected defined packet to node.
     * Another methods may build custom command packet, it is then passed to this method
     * which sends it to all selected nodes
     * 
     * @param CommandMsg payload    data packet to send. Is CommandMessage
     */
    public void sendMyCommandToNode(CommandMsg payload, int nodeId){
        try {                        
            if (this.getMsgSender().canAdd()==false){
                logToTextarea("Cannot add commands to send queue.", JPannelLoggerLogElement.SEVERITY_ERROR);
            }
            
            // increment send counter
            counter+=1;
            payload.set_command_id(counter);

            // send packet
            logToTextarea("Sending command msg=" + payload.toString());
            this.getMsgSender().add(nodeId, payload, "CommandMsg for="+nodeId+"; Command_code="+payload.get_command_code());
        }  catch (Exception ex) {
            logToTextarea("NullPointer exception: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
            Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Sends simple commands to selected nodes.
     * Simple command consist of command code and single command data.
     * 
     * @param commandCode   command code.
     * @param commandData   command data to send
     * @param auxData       auxiliary data to send
     */
    public void sendSimpleCommand(int commandCode, int commandData, int[] auxData){
        CommandMsg payload = new CommandMsg();
        payload.set_command_version((short) 0);
        payload.set_command_code((short) commandCode);
        payload.set_command_data(commandData);
        payload.set_command_data_next(auxData != null ? auxData : new int[] {0,0,0,0});
        this.sendMyCommandToSelectedNodes(payload);
    }
    
    /**
     * Sends simple commands to destination nodes.
     * Simple command consist of command code and single command data.
     * 
     * @param commandCode   command code.
     * @param commandData   command data to send
     * @param auxData       auxiliary data to send
     * @param destination   node id to send command
     */
    public void sendSimpleCommand(int commandCode, int commandData, int[] auxData, int destination){
        CommandMsg payload = new CommandMsg();
        payload.set_command_version((short) 0);
        payload.set_command_code((short) commandCode);
        payload.set_command_data(commandData);
        payload.set_command_data_next(auxData != null ? auxData : new int[] {0,0,0,0});
        this.sendMyCommandToNode(payload, destination);
    }
    
    /**
     * Sets General Input Output pin
     * 
     * @param pinnum
     * @param status 
     */
    public void sendSetPin(int pinnum, boolean status){
        this.sendSimpleCommand(MessageTypes.COMMAND_SETPIN, status ? 1:0, new int[] {pinnum,0,0,0});
    }
    
    /**
     * Sets node parameter doSensorSampling. If true, remote sensor nodes will 
     * sample sensor reading packets received for RSSI data.
     * 
     * @param doSensorSampling 
     */
    public void sendDoSensorSampling(boolean doSensorSampling){
        this.sendSimpleCommand(MessageTypes.COMMAND_SETSAMPLESENSORREADING, doSensorSampling ? 1:0, null);
    }
    
    /**
     * Send request for sensor reading
     * 
     * @param sensorType    ID of sensor to read (for example temperature, humidity, light, accelerometer, smoke sensor, ...)
     * @param periodic      TRUE means remote node should sample sensor data periodically
     * @param period        period of sensor sampling (in ms)
     */
    public void sendSensorReadingRequest(int sensorType, boolean periodic, int period){
        this.sendSensorReadingRequest(sensorType, periodic, period, false);
    }
    
    /**
     * Send request for sensor reading
     * 
     * @param sensorType    ID of sensor to read (for example temperature, humidity, light, accelerometer, smoke sensor, ...)
     * @param periodic      TRUE means remote node should sample sensor data periodically
     * @param period        period of sensor sampling (in ms)
     * @param isBroadcast   TRUE means remote node will send answer on sensor reading to broadcast.
     *      sometimes can be used to sample sensor reading for RSSI data to perform 2-in-1 tasks.
     */
    public void sendSensorReadingRequest(int sensorType, boolean periodic, int period, boolean isBroadcast){
        this.sendSimpleCommand(MessageTypes.COMMAND_GETSENSORREADING, sensorType, new int[] {periodic ? 1 : 0, period, isBroadcast ? 1 : 0, 0});
    }

    /**
     * Main sending method
     * do nothing if we are on the end (all tx power sent)
     *
     * @return
     */
    public boolean doSendCommand(int command_code){
        // create multiPingRequest packet and send it
        try {
            int selectedNodes[] = getWindow().getjPanel_comm_NodeSelector().getSelectedNodes();
            if (selectedNodes==null || selectedNodes.length<=0) return false;
//
//            // get dest node localy
//            destNode = Integer.parseInt(getWindow().getjText_comm_destNode().getText());

            // if not defined another way, use 0
            CommandMsg payload = new CommandMsg();
            payload.set_command_version((short) 0);
            
            // which command?
            switch(command_code){
                case MessageTypes.COMMAND_SETDORANDOMIZEDTHRESHOLDING:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( getWindow().getjCheck_com_randomizedThresholding().isSelected() ? 1 : 0 );

                    break;
                case MessageTypes.COMMAND_SETQUEUEFLUSHTHRESHOLD:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( Integer.parseInt(getWindow().getjText_comm_queueFlushThreshold().getText()) );

                    break;

                case MessageTypes.COMMAND_SETREPORTINGSTATUS:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( getWindow().getjCheck_comm_reportingStatus().isSelected() ? 1 : 0 );

                    break;

                case MessageTypes.COMMAND_SETREPORTPROTOCOL:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( getWindow().getCommandReportProtocol() );

                    break;

               case MessageTypes.COMMAND_RESET:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( (short) 0 );

                    break;

               case MessageTypes.COMMAND_ABORT:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( (short) 0 );

                    break;

                case MessageTypes.COMMAND_SETTX:
                    payload.set_command_code((short) command_code);
                    
                    // get specific tx power for this nodes
                    // get platform of node, shoud be same
                    Set<NodePlatform> platforms=new HashSet<NodePlatform>();
                    for(int i=0, cnI=selectedNodes.length; i<cnI; i++){
                        if (nodeRegister.existsNode(selectedNodes[i])==false) continue;
                        GenericNode node=nodeRegister.getNode(selectedNodes[i]);

                        if (node.getPlatform()==null) continue;
                        NodePlatform nf = node.getPlatform();
                        platforms.add(nf);

                        // multiple platforms selected, cannot set tx power
                        if (platforms.size()>1) {
                            JOptionPane.showMessageDialog(null,
                                "Different nodes platforms detected, cannot set TX power since different interpretation",
                                "Cannot set TX power",
                                JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                    }

                    if (platforms.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                            "No node platform detected",
                            "Cannot set TX power",
                            JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    this.getWindow().getjSlider_comm_txpower().setEnabled(true);

                    Object[] array = platforms.toArray();
                    NodePlatform nf = (NodePlatform) array[0];
                    int[] txLevels = nf.getTxLevels();
                    
                    payload.set_command_data( txLevels[getWindow().getjSlider_comm_txpower().getValue() - 1] );
                    break;

                case MessageTypes.COMMAND_IDENTIFY:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( 1 );

                    break;

                case MessageTypes.COMMAND_SETREPORTGAP:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data( Integer.valueOf(getWindow().getjText_comm_reportGap().getText()) );

                    break;

                case MessageTypes.COMMAND_GETSENSORREADING:
                    payload.set_command_code((short) command_code);
                    payload.set_command_data(1);

                    break;
                    
               case MessageTypes.COMMAND_SETPIN:
                    int pinnum=0;
                    boolean enabled=false;
                    
                    
                   
                    payload.set_command_code((short) command_code);
                    payload.set_command_data(1);

                    break;
                    

                default:
                    throw new UnsupportedOperationException("Command not implemented yet");
            }

            if (this.getMsgSender().canAdd()==false){
                logToTextarea("Cannot add commands to send queue.", JPannelLoggerLogElement.SEVERITY_ERROR);
            }

            for(int i=0, cnI=selectedNodes.length; i<cnI; i++)
            {
                // increment send counter
                counter+=1;
                payload.set_command_id(counter);

                // send packet
                logToTextarea("Sending command msg=" + payload.toString());
                this.getMsgSender().add(selectedNodes[i], payload, "CommandMsg for="+selectedNodes[i]+"; Command_code="+command_code);
                //moteIF.send(destNode, payload);
            }
            
            return true;
        }  catch (Exception ex) {
            logToTextarea("NullPointer exception: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
            Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Message listener
     *
     * @param to
     * @param msg
     */
    @Override
    public void messageReceived(int to, Message msg) {
        System.err.println("Comm: msgReceived");
        
        // get message source here
        // (in multihop setting this will be probably problem)
        int source = msg.getSerialPacket().get_header_src();
        
        // get node for sender 
        GenericNode node = null;
        if (this.nodeRegister != null && this.nodeRegister.existsNode(source)){
            node = this.nodeRegister.getNode(source);
            
            // automatically set lastSeen indicator
            node.setLastSeen(System.currentTimeMillis());
        }
        
        // if commmand message => it will be probably ACK or command answer, process it here
        if (msg instanceof CommandMsg){
            final CommandMsg Message = (CommandMsg) msg;
            // is sensor reading?
            // if so update record in node register and trigger update
            if (Message.get_command_code() == (short) MessageTypes.COMMAND_SENSORREADING
                    && node!=null){
                
                if (Message.get_command_data_next() != null
                        && Message.get_command_data_next()[0] == 1){                    
                    
                    // set sensor
                    node.setTemperature(Message.get_command_data()/10.0);

                    Map<Integer, String> changeMap = new HashMap<Integer, String>();
                    changeMap.put(Integer.valueOf(source), "temperature");
                    this.nodeRegister.changeNotify(changeMap);
                }
            }
            
            //logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src() + "; body=" + msg.toString());
            logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src()
                    + "; code=" + Message.get_command_code()
                    + "; reply=" + Message.get_reply_on_command()
                    + "; data=" + Message.get_command_data()
                    + "; seq=" + Message.get_command_id()
                    );
        }
        else {
            System.err.println("Command, unexpected message received");
            return;
        }

        // write to error log
        System.err.println("Command packet received: " + msg.toString());
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
            if ("AutoNodeDiscovery".equals(jcheck.getActionCommand())){
                // set node discovery thread attribute
                RSSI_graphApp.sGetNodeDiscovery().setDoDiscovery((e.getStateChange() == ItemEvent.SELECTED));

                if(e.getStateChange() == ItemEvent.SELECTED) {
                    this.logToTextarea("Auto node discovery turned ON", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
                else {
                    this.logToTextarea("Auto node discovery turned OFF", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
            }
            else {
                System.err.println("Checkbox unknown to me was triggered");
            }
        }
        // toggle button
        else if(e.getItem().equals(getWindow().getjToggle_comm_on()))
        {
            System.err.print("Command action button pressed; ");

            if (e.getStateChange() == ItemEvent.SELECTED){
                System.err.println("Selected, registering and starting");
                turnOn();
            }
            else {
                System.err.println("DESelected, deregistering and closing db");
                turnOff();
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

        System.err.println();
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
        if (e.getActionCommand().equals("SendRT")) {
           System.err.println("Command; Send RT");
           doSendCommand(MessageTypes.COMMAND_SETDORANDOMIZEDTHRESHOLDING);
        }
        else if (e.getActionCommand().equals("SendRTValue") ){
            System.err.println("Command; SendRTValue");
            doSendCommand(MessageTypes.COMMAND_SETQUEUEFLUSHTHRESHOLD);
        }
        else if (e.getActionCommand().equals("SendTinyReports") ){
            System.err.println("Command; SendTinyReports -> SendReportProtocol");
            doSendCommand(MessageTypes.COMMAND_SETREPORTPROTOCOL);
        }
        else if (e.getActionCommand().equals("SendReportingStatus") ){
            System.err.println("Command; SendReportingStatus");
            doSendCommand(MessageTypes.COMMAND_SETREPORTINGSTATUS);
        }
        else if (e.getActionCommand().equals("SendTXPower") ){
            System.err.println("Command; SendTXPower");
            doSendCommand(MessageTypes.COMMAND_SETTX);
        }
        else if (e.getActionCommand().equals("SendReset") ){
            System.err.println("Command; SendReset");
            doSendCommand(MessageTypes.COMMAND_RESET);
        }
        else if (e.getActionCommand().equals("SendAbort") ){
            System.err.println("Command; SendAbort");
            doSendCommand(MessageTypes.COMMAND_ABORT);
        }
        else if (e.getActionCommand().equals("Discover")) {
            System.err.println("Command; Discover");
            doSendCommand(MessageTypes.COMMAND_IDENTIFY);
        }
        else if (e.getActionCommand().equals("sensorReading")) {
            System.err.println("Command; sensor reading");
            doSendCommand(MessageTypes.COMMAND_GETSENSORREADING);
        }
        else if (e.getActionCommand().equals("SetReportGap")) {
            System.err.println("Command; SetReportGap");
            doSendCommand(MessageTypes.COMMAND_SETREPORTGAP);
        }
        else if (e.getActionCommand().equals("SetPin")) {
            System.err.println("Command; SetPin");
            doSendCommand(MessageTypes.COMMAND_SETPIN);
        }
//        else if (e.getActionCommand().equals("moveNextTimer")){
//            System.err.println("R2D; Move next timer fired");
//            doSendPing();
//        }
        else {
            throw new UnsupportedOperationException("I don't know such event!");
        }
    }

    /**
     * Helper method to get main frame with controls
     * We need to read/write to that controls
     *
     * @return
     */
    public RSSI_graphView getWindow(){
        return RSSI_graphApp.getApplication().getGraphViewFrame();
    }

    public boolean isDoingTestNow() {
        return doingTestNow;
    }

    public void setDoingTestNow(boolean doingTestNow) {
        this.doingTestNow = doingTestNow;
    }

    /**
     * Listener method for node selector change event
     * @param evt 
     */
    public void nodeChanged(NodeSelectedEvent evt) {
        if (evt.selectedNodes == null || evt.selectedNodes.length==0){
            this.getWindow().getjSlider_comm_txpower().setEnabled(false);
            return;
        }
        
        // get platform of node, shoud be same
        Set<NodePlatform> platforms=new HashSet<NodePlatform>();
        for(int i=0, cnI=evt.selectedNodes.length; i<cnI; i++){
            if (nodeRegister.existsNode(evt.selectedNodes[i])==false) continue;
            GenericNode node=nodeRegister.getNode(evt.selectedNodes[i]);
            
            if (node.getPlatform()==null) continue;
            NodePlatform nf = node.getPlatform();
            platforms.add(nf);
            
            // multiple platforms selected, cannot set tx power
            if (platforms.size()>1) {
                this.getWindow().getjSlider_comm_txpower().setEnabled(false);
                return;
            }
        }
        
        if (platforms.isEmpty()) return;
        this.getWindow().getjSlider_comm_txpower().setEnabled(true);
        
        Object[] array = platforms.toArray();
        NodePlatform nf = (NodePlatform) array[0];
        int[] txLevels = nf.getTxLevels();
        double[] txOutput = nf.getTxOutputPower();
        this.getWindow().getjSlider_comm_txpower().setMaximum(txLevels.length);
    }

}
