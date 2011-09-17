/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.MessageTypes;
import rssi_graph.RSSI_graphApp;
import rssi_graph.WorkerBase;
import rssi_graph.WorkerInterface;
import rssi_graph.localization.LocalizationEstimate;
import rssi_graph.messages.CommandMsg;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.nodeRegister.NodeRegisterEventListener;
import rssi_graph.utils.DataSmoother;


/**
 *
 * @author ph4r05
 */
public class GameWorker extends WorkerBase implements MessageListener, WorkerInterface, ActionListener,
        NodeRegisterEventListener {
    private MobileNodeManager mobileNodeManager;
    private HashMap<Integer, LocalizationEstimate> localizationHistory;
    private JPanelGame mainPanel = null;
    
    private boolean multiplayer=true;
    private rssi_graph.game.Player player1=null;
    private rssi_graph.game.Player player2=null;
    
    protected JFrameScreen screen = null;
    
    protected Timer guiSyncTimer = null;
    protected Timer energyCalcTimer = null;
    
    /**
     * Timeout between sensor reading from sensor
     */
    protected int requestTimeout=500;
    
    /**
     * Threshold of node unresponsivity to watchdog reaction (HW reset, send request)
     */
    protected int watchdogThreshold=10000;
    
    /**
     * Is watchdog enabled?
     */
    protected boolean watchdogEnabled;
    
    /**
     * Smoothing constant for light sensor averaging
     */
    protected double smoothingSensor=0.5;
    
//    protected boolean[] playerDoGuiUpdate = { true, true };

    public GameWorker(MoteIF mi) {
        // init mote interface
        moteIF = mi;

        // get mobile node manager from node registe
        mobileNodeManager = RSSI_graphApp.sGetNodeRegister() == null ? null : RSSI_graphApp.sGetNodeRegister().getMnm();
        
        // init localizaton history
        localizationHistory = new HashMap<Integer, LocalizationEstimate> ();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
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

        // log
        this.logToTextarea("Module GAME registered and turned off", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    /**
     * Turn module on
     * - register message listeners
     * - start necessary connections (sqlite)
     */
    @Override
    public void turnOn() {
         // register listeners for response, report
        if (moteIF != null){
            moteIF.registerListener(new CommandMsg(), this);
        }
        
        // node register
        if (this.nodeRegister==null){
            this.nodeRegister = RSSI_graphApp.sGetNodeRegister();
            this.nodeRegister.addChangeListener(this);
        }

        // instantiate new mobile manager
        this.mobileNodeManager = RSSI_graphApp.sGetNodeRegister().getMnm();
        
        // init localizaton history
        this.localizationHistory = new HashMap<Integer, LocalizationEstimate> ();

        // need main panel for settings lookup & displaying results
        if (mainPanel == null){
            this.mainPanel = this.getWindow().getjPanelGame1();
            this.mainPanel.setGameWorker(this);
            this.mainPanel.initThis();
            this.refreshMainWindow();
        }
        
        // screen frame initialization
        if (this.screen == null){
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            screen = new JFrameScreen();
            screen.setLocationRelativeTo(mainFrame);
            screen.setParentPanel(this.mainPanel);
            screen.setGameWorker(this);
            screen.setTitle("Závod");
            screen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            screen.initThis();
            
            // set dispose window listener
            screen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    screenDisposed();
                }
            });
        }
        
        // init timers
        this.energyCalcTimer = new Timer(250, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                energyCalcEvent(e);
            }
        });
        this.energyCalcTimer.setActionCommand("energyCalc");
        this.energyCalcTimer.setRepeats(true);
        
        this.guiSyncTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateGuiEvent(e);
            }
        });
        this.guiSyncTimer.setActionCommand("guiSync");
        this.guiSyncTimer.setRepeats(true);

        
        
        
//        // fetch settings data from settings panel
//        boolean success = this.reFetchSettings();
//        if (success==false) return;

        // node register test
        Set<Integer> nodeSet = RSSI_graphApp.sGetNodeRegister().getNodesSet();
//        for(Integer curNode: nodeSet){
//            System.err.println("Found in nodeRegister: " + curNode);
//        }

        // log
        this.logToTextarea("Module GAME registered and turned on. Listening to messages.", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    /**
     * Module identification
     * @return text id
     */
    @Override
    public String toString() {
        return "Game module";
    }

    /**
     * Refresh data from database
     */
    public void refreshMainWindow(){
         if (mainPanel != null){
            Set<Integer> mobileNodes = this.mobileNodeManager.getMobileNodes();
            
            // set mobile nodes to choose
            this.mainPanel.setMobileNodes(mobileNodes);
         }
    }
    
    /**
     * event fired when settings changed
     */
    public void settingsChanged(){        
        String player1name = this.mainPanel.GetPlayerNode(1);
        String player2name = this.mainPanel.GetPlayerNode(2);
        
        // is none?
        if ("NONE".equalsIgnoreCase(player1name)){
            this.setPlayer1(null);
            this.setMultiplayer(false);
        } else {
            this.setPlayer1(new Player());
            this.player1.setNode(Integer.valueOf(player1name));
            this.player1.setEnergyCalculator(new EnergyCalculator());
            this.player1.setLastResponseTime(System.currentTimeMillis());
        }
        
        // is none?
        if ("NONE".equalsIgnoreCase(player2name)){
            this.setPlayer2(null);
            this.setMultiplayer(false);
        } else {
            this.setPlayer2(new Player());
            this.player2.setNode(Integer.valueOf(player2name));
            this.player2.setEnergyCalculator(new EnergyCalculator());
            this.player2.setLastResponseTime(System.currentTimeMillis());
        }
        
        // player names
        if (this.player1 instanceof Player){
            this.player1.setName(this.mainPanel.GetPlayerName(1));
            
            // update screen
            this.screen.setPlayerName(1, this.player1.getName());
            this.mainPanel.enablePlayerEdit(1, true);
        } else {
            this.mainPanel.enablePlayerEdit(1, false);
        }
        
        if (this.player2 instanceof Player){
            this.player2.setName(this.mainPanel.GetPlayerName(2));            
            
            // update screen
            this.screen.setPlayerName(2, this.player2.getName());
            this.mainPanel.enablePlayerEdit(2, true);
        } else {
            this.mainPanel.enablePlayerEdit(2, false);
        }
        
        // multiplayer assert
        this.setMultiplayer(this.player1 instanceof Player && this.player2 instanceof Player);
        this.screen.setMultiplayer(this.isMultiplayer());
    }
    
    /**
     * Set sensor reading request command to node
     * @param player 
     */
    public void sendRequest(int player){
        if (player==1 && !(this.player1 instanceof Player)) return;
        if (player==2 && !(this.player2 instanceof Player)) return;
        
        int nodeId=0;
        if (player==1){
            nodeId = this.player1.getNode();
        } else if (player==2){
            nodeId = this.player2.getNode();
        } else {
            return;
        }
        
        this.sendSensorReadingRequest(nodeId, 2, true, this.requestTimeout);
    }
    
    /**
     * Send request for sensor reading
     * 
     * @param sensorType
     * @param periodic
     * @param period
     */
    public void sendSensorReadingRequest(int nodeid, int sensorType, boolean periodic, int period){
        try {
            int command_code = MessageTypes.COMMAND_GETSENSORREADING;
            CommandMsg payload = new CommandMsg();
            payload.set_command_version((short) 0);
            payload.set_command_code((short) command_code);
            payload.set_command_data(sensorType);
            
            if (periodic){
                payload.set_command_data_next(new int[] {1, period,0,0});
            } else {
                payload.set_command_data_next(new int[] {0, period,0,0});
            }
            
            if (this.getMsgSender().canAdd()==false){
                logToTextarea("Cannot add commands to send queue.", JPannelLoggerLogElement.SEVERITY_ERROR);
                return;
            }

            payload.set_command_id(1);

            // send packet
            this.getMsgSender().add(nodeid, payload, "GAME request command for="+nodeid+"; Command_code="+command_code);
        }  catch (Exception ex) {
            logToTextarea("NullPointer exception: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
            Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Send reset packet to node
     * @param player 
     */
    public void sendReset(int player){
        if (player==1 && !(this.player1 instanceof Player)) return;
        if (player==2 && !(this.player2 instanceof Player)) return;
        
        int nodeId=0;
        if (player==1){
            nodeId = this.player1.getNode();
        } else if (player==2){
            nodeId = this.player2.getNode();
        } else {
            return;
        }
        
        CommandMsg payload = new CommandMsg();
        payload.set_command_version((short) 0);
        payload.set_command_code((short) MessageTypes.COMMAND_RESET);
        payload.set_command_data( (short) 0 );
        
        if (this.getMsgSender().canAdd()==false){
            logToTextarea("Cannot add commands to send queue.", JPannelLoggerLogElement.SEVERITY_ERROR);
            return;
        }

        payload.set_command_id(1);

        // send packet
        this.getMsgSender().add(nodeId, payload, "Game, send reset; for="+nodeId);
    }
    
    /**
     * Accept node register event change
     * @param evt 
     */
    public void accept(NodeRegisterEvent evt) {
        if (evt!=null && evt.getEventType() != NodeRegisterEvent.EVENT_TYPE_DATA_CHANGED) return;
        
        // no changes - do it
        if (evt.changes==null){
            this.refreshMainWindow();
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
            this.refreshMainWindow();
        }
    }

    /**
     * Process sensor readings
     * 
     * @param i
     * @param msg 
     */
    @Override
    public void messageReceived(int i, Message msg) {
        //super.messageReceived(i, msg);
        if (msg instanceof CommandMsg){
            final CommandMsg Message = (CommandMsg) msg;
            int source = Message.getSerialPacket().get_header_src();

            // is sensor reading?
            // if so update record in node register and trigger update
            if (Message.get_command_code() == (short) MessageTypes.COMMAND_SENSORREADING
                    && this.nodeRegister != null
                    && this.nodeRegister.existsNode(source)){
                
                // get system miliseconds
                long currentTimeMillis = System.currentTimeMillis();
                
                GenericNode node = this.nodeRegister.getNode(source);
                if (Message.get_command_data_next() != null
                        && Message.get_command_data_next()[0] == 2){
                    
                    int lightSensorOutput = Message.get_command_data();
                    
                    // set light intensity
                    node.setLightIntensity(lightSensorOutput);
                    // to keep fit
                    node.setLastSeen(currentTimeMillis);

                    Map<Integer, String> changeMap = new HashMap<Integer, String>();
                    changeMap.put(Integer.valueOf(source), "light");
                    this.nodeRegister.changeNotify(changeMap);
                    
                    // reflect change to player node
                    if (this.player1 instanceof Player && this.player1.getNode() == source){
                        // set no need to do watchdog
                        double light = this.player1.getLight();
                        
                        this.player1.setLight(DataSmoother.getSmoothed(light, lightSensorOutput, this.smoothingSensor));
                        this.player1.setLastResponseTime(currentTimeMillis);
                    } else if (this.player2 instanceof Player && this.player2.getNode() == source) {
                        double light = this.player2.getLight();
                        
                        // set no need to do watchdog
                        this.player2.setLight(DataSmoother.getSmoothed(light, lightSensorOutput, this.smoothingSensor));
                        this.player2.setLastResponseTime(currentTimeMillis);
                    }
                    
                    //logToTextarea("GAME Sensor reading received from: " + msg.getSerialPacket().get_header_src());
                }
            }
            
//            logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src() + "; body=" + msg.toString());
//            logToTextarea("Command packet received from=" + msg.getSerialPacket().get_header_src()
//                    + "; code=" + Message.get_command_code()
//                    + "; reply=" + Message.get_reply_on_command()
//                    + "; data=" + Message.get_command_data()
//                    + "; seq=" + Message.get_command_id()
//                    );
        }
        else {
            System.err.println("Command, unexpected message received");
            return;
        }
    }

    @Override
    public void messageSent(String listenerKey, Message msg, int destination) {
        //super.messageSent(listenerKey, msg, destination);
    }
    
    
    
    /**
     * Recompute energy
     * 
     * @param player 
     */
    public void recomputeEnergy(int player){
        
    }
    
    
    
    /**
     * event triggered when game screen is disposed
     */
    public void screenDisposed(){
        // inform parent panel that screen was disposed
        this.mainPanel.screenDisposed();
    }
    
    /**
     * Energy calc event
     * @param e 
     */
    public void energyCalcEvent(ActionEvent e){
        // get current time in milis
        long currentTimeMillis = System.currentTimeMillis();
        
        // compute new energy here
        if (this.player1 instanceof Player){
            Double newEnergy1 = this.player1.getNewEnergy();
            if (newEnergy1!=null){
                this.player1.setEnergy(newEnergy1);
            }
            
            // now compute node latency
            this.player1.setLastResponse(currentTimeMillis - this.player1.getLastResponseTime());
            
            // watchdog event?
            if (this.player1.getLastResponse() > this.watchdogThreshold 
                    && (currentTimeMillis - this.player1.getLastWatchdogTime()) > this.watchdogThreshold ){
                // save current time - when watchdog was "released"
                this.player1.setLastWatchdogTime(currentTimeMillis);
                this.sendReset(1);
                this.sendRequest(1);
            }
        }
        
        // do the same for 2nd player
        if (this.player2 instanceof Player){
            Double newEnergy2 = this.player2.getNewEnergy();
            if (newEnergy2!=null){
                this.player2.setEnergy(newEnergy2);
            }
            
            // now compute node latency
            this.player2.setLastResponse(currentTimeMillis - this.player2.getLastResponseTime());
            
            // watchdog event?
            if (this.player2.getLastResponse() > this.watchdogThreshold 
                    && (currentTimeMillis - this.player2.getLastWatchdogTime()) > this.watchdogThreshold ){
                // save current time - when watchdog was "released"
                this.player2.setLastWatchdogTime(currentTimeMillis);
                this.sendReset(2);
                this.sendRequest(2);
            }
        }
    }
    
    /**
     * Event on update GUI
     * @bug: breaks abstraction, here should be register for change listener
     * @param e 
     */
    public void updateGuiEvent(ActionEvent e){
        // @BUG:
        this.mainPanel.updateGuiTimerFired();
        this.screen.updateGuiTimerFired();
    }
    
    /**
     * starts/stops timer for update GUI
     * 
     * @param doUpdate 
     */
    public void setUpdateGui(boolean doUpdate){
        if (doUpdate && !this.guiSyncTimer.isRunning()){
            this.guiSyncTimer.start();
            this.logToTextarea("Started GUI timer, delay: " + this.guiSyncTimer.getDelay());
        } else if (doUpdate==false && this.guiSyncTimer.isRunning()){
            this.guiSyncTimer.stop();
            this.logToTextarea("Stoped GUI timer");
        }
    }
    
    /**
     * Starts / stop GUI timer for game counters
     * 
     * @param enable 
     */
    public void setGameTimer(boolean enable){
        if (enable && !this.energyCalcTimer.isRunning()){
            this.energyCalcTimer.start();
            this.logToTextarea("Started Game timer, delay: " + this.energyCalcTimer.getDelay());
        } else if (enable==false && this.energyCalcTimer.isRunning()){
            this.energyCalcTimer.stop();
            this.logToTextarea("Stopped Game timer");
        }
    }
    
    /**
     * Controls screen visibility
     * 
     * @param visible 
     */
    public void setScreenVisible(boolean visible){
        this.screen.repaint();
        this.screen.validate();
        this.screen.setVisible(visible);
    }

    public HashMap<Integer, LocalizationEstimate> getLocalizationHistory() {
        return localizationHistory;
    }

    public void setLocalizationHistory(HashMap<Integer, LocalizationEstimate> localizationHistory) {
        this.localizationHistory = localizationHistory;
    }

    public JPanelGame getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanelGame mainPanel) {
        this.mainPanel = mainPanel;
    }

    public MobileNodeManager getMobileNodeManager() {
        return mobileNodeManager;
    }

    public void setMobileNodeManager(MobileNodeManager mobileNodeManager) {
        this.mobileNodeManager = mobileNodeManager;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    public void setMultiplayer(boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public boolean isWatchdogEnabled() {
        return watchdogEnabled;
    }

    public void setWatchdogEnabled(boolean watchdogEnabled) {
        boolean oldWatchdogState = this.watchdogEnabled;
        this.watchdogEnabled = watchdogEnabled;
        
        if (this.watchdogEnabled!=oldWatchdogState){
            // do something new here, probably run watchdog?
        }
    }

    public int getWatchdogThreshold() {
        return watchdogThreshold;
    }

    public void setWatchdogThreshold(int watchdogThreshold) {
        this.watchdogThreshold = watchdogThreshold;
    }

    public double getSmoothingSensor() {
        return smoothingSensor;
    }

    public void setSmoothingSensor(double smoothingSensor) {
        this.smoothingSensor = smoothingSensor;
    }
    
    
    
    
}
