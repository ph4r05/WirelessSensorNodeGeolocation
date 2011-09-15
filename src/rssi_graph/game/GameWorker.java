/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Set;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.RSSI_graphApp;
import rssi_graph.WorkerBase;
import rssi_graph.WorkerInterface;
import rssi_graph.localization.LocalizationEstimate;
import rssi_graph.messages.CommandMsg;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.nodeRegister.NodeRegisterEventListener;


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
            this.refreshMainWindow();
        }
//
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
        }
        
        // is none?
        if ("NONE".equalsIgnoreCase(player2name)){
            this.setPlayer2(null);
            this.setMultiplayer(false);
        } else {
            this.setPlayer2(new Player());
            this.player2.setNode(Integer.valueOf(player2name));
        }
        
        // player names
        if (this.player1 instanceof Player){
            this.player1.setName(this.mainPanel.GetPlayerName(1));
        }
        
        if (this.player2 instanceof Player){
            this.player2.setName(this.mainPanel.GetPlayerName(2));
        }
        
        // multiplayer assert
        this.setMultiplayer(this.player1 instanceof Player && this.player2 instanceof Player);
    }
    
    /**
     * Accept node register event change
     * @param evt 
     */
    public void accept(NodeRegisterEvent evt) {
        this.refreshMainWindow();
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
}
