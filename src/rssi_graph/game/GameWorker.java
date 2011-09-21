/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import rssi_graph.utils.PlatformUtils;

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
    protected JFrameFinish frameFinish = null;
    
    protected Timer guiSyncTimer = null;
    protected Timer energyCalcTimer = null;
    
    
    /**
     * Static constants for energy warning
     * (for meter thresholds, sounds play)
     */
    public static final double THRESHOLD_CRITICAL=10D;
    public static final double THRESHOLD_WARNING=25D;
    
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
    protected boolean watchdogEnabled=false;
    
    /**
     * Smoothing constant for light sensor averaging
     */
    protected double smoothingSensor=0.5;
    
    /**
     * Should play sounds?
     */
    protected boolean soundEnabled=false;
    
    /**
     * Game sounds handling object
     */
    protected GameSounds gameSounds = null;
    
    /**
     * Game states constants & current state
     */
    public static final int GAME_STATE_CLEAN = 0;
    public static final int GAME_STATE_STARTED = 1;
    public static final int GAME_STATE_PAUSED = 2;
    public static final int GAME_STATE_STOPPED = 3;
    protected int gameState = GameWorker.GAME_STATE_CLEAN;
    
    /**
     * Time when game started
     */
    protected long gameTimeRemaining = 0;
    
    /**
     * Time when time counting was started
     */
    protected long gameTimeLastChange=0;
    
    /**
     * Winner of game
     */
    protected int winner=-1;
    
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
            screen.setExtendedState(JFrame.MAXIMIZED_BOTH);
            screen.initThis();
            
            // set dispose window listener
            screen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    screenDisposed();
                }
            });
        }
        
        if (this.frameFinish == null){
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            frameFinish = new JFrameFinish();
            frameFinish.setLocationRelativeTo(this.screen);
            frameFinish.setTitle("KONEC");
            frameFinish.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameFinish.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.50f));
            
            
//            frameFinish.setParentPanel(this.screen.getParentPanel());            
//            frameFinish.setExtendedState(JFrame.MAXIMIZED_BOTH);
//            frameFinish.initThis();
//            
//            // set dispose window listener
//            screen.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosing(WindowEvent e) {
//                    screenDisposed();
//                }
//            });
//            
//            // Determine what the default GraphicsDevice can support.
//            GraphicsEnvironment ge =
//                GraphicsEnvironment.getLocalGraphicsEnvironment();
//            GraphicsDevice gd = ge.getDefaultScreenDevice();
//            boolean isUniformTranslucencySupported =
//                gd.isWindowTranslucencySupported(TRANSLUCENT);
//            boolean isPerPixelTranslucencySupported =
//                gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT);
//            boolean isShapedWindowSupported =
//                gd.isWindowTranslucencySupported(PERPIXEL_TRANSPARENT);
//            AWTUtilities.setWindowOpaque(this.frameFinish, false);
            
            /**
             * Set window opacity via java reflection API (opacity was added in JDK6 10)
             * @see http://java.sun.com/developer/technicalArticles/GUI/translucent_shaped_windows/
             */
            try {
               Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
               Method mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
               mSetWindowOpacity.invoke(null, this.frameFinish, Float.valueOf(0.85f));
            } catch (NoSuchMethodException ex) {
               ex.printStackTrace();
            } catch (SecurityException ex) {
               ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
               ex.printStackTrace();
            } catch (IllegalAccessException ex) {
               ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
               ex.printStackTrace();
            } catch (InvocationTargetException ex) {
               ex.printStackTrace();
            }
        }
        
        // init game sounds
        if (this.gameSounds == null){
            this.gameSounds = new GameSounds();
            this.gameSounds.setGameWorker(this);
            this.gameSounds.initThis();
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

    public static void centerWindow(Window target, Component parent){
        target.pack();

        Dimension size = target.getSize();
        Rectangle parentBounds = parent == null || !parent.isShowing() ? 
          getUsableScreenBounds() :
          new Rectangle(parent.getLocationOnScreen(), parent.getSize());

        target.setLocation(parentBounds.x + (parentBounds.width - size.width)/2, parentBounds.y + (parentBounds.height - size.height)/2);
      }
    
    /**
   * Attempts to determine the usable screen bounds of the default screen
   * device. If the require java.awt API is not available under the JVM we're
   * running in, this will simply return the screen bounds obtained via
   * <code>Toolkit.getScreenSize()</code>.
   */
  
  public static Rectangle getUsableScreenBounds(){
    if (PlatformUtils.isJavaBetterThan("1.4")){
      try{
        Class graphicsEnvironmentClass = Class.forName("java.awt.GraphicsEnvironment");
        Class graphicsDeviceClass = Class.forName("java.awt.GraphicsDevice");
        Class graphicsConfigurationClass = Class.forName("java.awt.GraphicsConfiguration");
        
        Class [] emptyClassArr = new Class[0];
        Method getLocalGraphicsEnvironmentMethod = 
          graphicsEnvironmentClass.getMethod("getLocalGraphicsEnvironment", emptyClassArr);
        Method getDefaultScreenDeviceMethod = 
          graphicsEnvironmentClass.getMethod("getDefaultScreenDevice", emptyClassArr);
        Method getDefaultConfigurationMethod =
          graphicsDeviceClass.getMethod("getDefaultConfiguration", emptyClassArr);
        Method getBoundsMethod = 
          graphicsConfigurationClass.getMethod("getBounds", emptyClassArr);
        Method getScreenInsetsMethod = 
          Toolkit.class.getMethod("getScreenInsets", new Class[]{graphicsConfigurationClass});
        
        Object [] emptyObjArr = new Object[0];
        Object graphicsEnvironment = getLocalGraphicsEnvironmentMethod.invoke(null, emptyObjArr);
        Object defaultScreenDevice = getDefaultScreenDeviceMethod.invoke(graphicsEnvironment, emptyObjArr);
        Object defaultConfiguration = getDefaultConfigurationMethod.invoke(defaultScreenDevice, emptyObjArr);
        Rectangle bounds = (Rectangle)getBoundsMethod.invoke(defaultConfiguration, emptyObjArr);
        Insets insets = 
          (Insets)getScreenInsetsMethod.invoke(Toolkit.getDefaultToolkit(), new Object[]{defaultConfiguration});
        
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= insets.left + insets.right;
        bounds.height -= insets.top + insets.bottom;
        
        return bounds;
      } catch (ClassNotFoundException e){e.printStackTrace();}
        catch (SecurityException e){e.printStackTrace();}
        catch (NoSuchMethodException e){e.printStackTrace();}
        catch (IllegalArgumentException e){e.printStackTrace();}
        catch (IllegalAccessException e){e.printStackTrace();}
        catch (InvocationTargetException e){e.printStackTrace();}
    }

    return new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
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
    @Override
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
                                    
                        // now compute node latency, 0 = now, message received
                        this.player1.setLastResponse(0);
                        
                    } else if (this.player2 instanceof Player && this.player2.getNode() == source) {
                        double light = this.player2.getLight();
                        
                        // set no need to do watchdog
                        this.player2.setLight(DataSmoother.getSmoothed(light, lightSensorOutput, this.smoothingSensor));
                        this.player2.setLastResponseTime(currentTimeMillis);
                                    
                        // now compute node latency, 0 = now, message received
                        this.player2.setLastResponse(0);
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
        int oldState = this.gameState;
        
        // game need to be running to move counters
        if (this.gameState != GameWorker.GAME_STATE_STARTED){
            return;
        }
        
        // change time remaining for game
        if ((currentTimeMillis - this.gameTimeLastChange) >= 1000){
            // do increment here
            this.gameTimeLastChange += 1000;
            this.gameTimeRemaining-=1;
        }
        
        // is game timer == 0??
        if (this.gameTimeRemaining <= 0){
            // stop game here!
            this.gameState = GameWorker.GAME_STATE_STOPPED;
            this.setWinner(-1);
            this.eventGameStateChanged(oldState, this.gameState, "timeout");
        }
        
        // compute new energy here
        if (this.player1 instanceof Player){
            double oldEnergy1 = this.player1.getEnergy();
            Double newEnergy1 = this.player1.getNewEnergy();
            if (newEnergy1!=null){
                this.player1.setEnergy(newEnergy1);
                
                // trigger event
                this.eventEnergyChanged(1, oldEnergy1, newEnergy1);
            }
            
            // now compute node latency
            this.player1.setLastResponse(currentTimeMillis - this.player1.getLastResponseTime());
            
            // watchdog event?
            if (this.isWatchdogEnabled()
                    && this.player1.getLastResponse() > this.watchdogThreshold 
                    && (currentTimeMillis - this.player1.getLastWatchdogTime()) > this.watchdogThreshold ){
                // save current time - when watchdog was "released"
                this.player1.setLastWatchdogTime(currentTimeMillis);
                this.sendReset(1);
                this.sendRequest(1);
            }
        }
        
        // do the same for 2nd player
        if (this.player2 instanceof Player){
            double oldEnergy2 = this.player2.getEnergy();
            Double newEnergy2 = this.player2.getNewEnergy();
            if (newEnergy2!=null){
                this.player2.setEnergy(newEnergy2);
                                
                // trigger event
                this.eventEnergyChanged(1, oldEnergy2, newEnergy2);
            }
            
            // now compute node latency
            this.player2.setLastResponse(currentTimeMillis - this.player2.getLastResponseTime());
            
            // watchdog event?
            if (this.isWatchdogEnabled()
                    && this.player2.getLastResponse() > this.watchdogThreshold 
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
    
    /**
     * Event triggered when energy changed
     * 
     * @param player
     * @param oldEnergy
     * @param newEnergy 
     */
    public void eventEnergyChanged(int player, double oldEnergy, double newEnergy){
        // under limit?
        if (oldEnergy != newEnergy && newEnergy==0){
            // stop game here
            int oldState = this.gameState;
            this.gameState = GameWorker.GAME_STATE_STOPPED;
            this.setWinner(player==1 ? 2:1);
            this.eventGameStateChanged(oldState, this.gameState, "nullEnergy");
        }
        
        
        // sound handler
        this.energySound(player, newEnergy);
    }
    
    /**
     * Play sounds on energy changed event
     */
    public void energySound(int player, double newEnergy){
        // do not play sound when disabled
        if (this.isSoundEnabled()==false) return;
        
        // pass this to sound object
        // @TODO: this object should implement some general interface. There should be
        // listeners hooked to energy changed event. Game sounds module would be registered
        // as energy change listener. 
        // Current design solution is good only for a few child objects
        this.gameSounds.energyChanged(player, newEnergy);
    }
    
    
    /**
     * Event triggered on state change
     * 
     * @param oldState
     * @param newState 
     */
    public void eventGameStateChanged(int oldState, int newState){
        this.eventGameStateChanged(oldState, newState, null);
    }
    
    /**
     * Event triggered on state change
     * 
     * @param oldState
     * @param newState 
     */
    public void eventGameStateChanged(int oldState, int newState, String reason){
        // is game stopped?
        if (newState == GameWorker.GAME_STATE_STOPPED){
            // show finis dialog, disable massively
            this.frameFinish.setLocationRelativeTo(screen);
            this.frameFinish.repaint();
            this.frameFinish.validate();
            this.frameFinish.setVisible(true);
            this.frameFinish.setLocationRelativeTo(screen);
            GameWorker.centerWindow(this.frameFinish, screen);
            
            if (reason!=null && this.winner>0){
                if (!this.isMultiplayer()){
                    if (this.winner==1){
                        this.frameFinish.setText("Vyhrál jsi");
                    } else {
                        this.frameFinish.setText("Prohál jsi");
                    }
                } else {
                    if (this.winner==1){
                        this.frameFinish.setText("Vyhrál " + this.player1.getName());
                    } else if (this.winner==2){
                        this.frameFinish.setText("Vyhrál " + this.player2.getName());
                    }
                }                
            } else {
                this.frameFinish.setText("");
            }
                  
        }
        
        this.screen.setGameState(newState);
        return;
    }
    
    /**
     * public caller for game state change
     * @param stateChange
     * @return 
     */
    public boolean changeGameState(int stateChange){
        // is valid state change?
        // can be if new state is paused and current is paused as well (toggle)
        if (this.gameState == stateChange && stateChange!=GameWorker.GAME_STATE_PAUSED) return false;
        
        // keep old state for game state changed event
        int oldState = this.gameState;
        
        // if here => state is really changed
        // paused?
        if (stateChange == GameWorker.GAME_STATE_PAUSED){
            // pausing started game?
            if (this.gameState == GameWorker.GAME_STATE_STARTED){
                // pause current timer
                this.gameState = GameWorker.GAME_STATE_PAUSED;
                this.eventGameStateChanged(oldState, this.gameState);
                return true;
            } else if (this.gameState == GameWorker.GAME_STATE_PAUSED){
                // un-pausing 
                this.gameState = GameWorker.GAME_STATE_STARTED;
                this.gameTimeLastChange = System.currentTimeMillis();
                this.eventGameStateChanged(oldState, this.gameState);
                return true;
            } else {
                // otherwise another state transition is not allowed
                return false;
            }
            
        } else if (stateChange == GameWorker.GAME_STATE_STARTED){
            // starting new game
            if (this.gameState == GameWorker.GAME_STATE_CLEAN
                    || this.gameState == GameWorker.GAME_STATE_STOPPED){
                // clean game state, reset
                this.initNewGame(); 
                this.gameTimeLastChange = System.currentTimeMillis();
                
                this.gameState = GameWorker.GAME_STATE_STARTED;
                this.eventGameStateChanged(oldState, this.gameState);
                return true;
            } else {
                return false;
            }
        } else if (stateChange == GameWorker.GAME_STATE_STOPPED){
            // stopping current game, it is not allowed to continue in stopped game
            // when game is stopped, timers are not counting...
            if (this.gameState == GameWorker.GAME_STATE_STARTED){
                // allowed transition only if game is started
                this.gameState = GameWorker.GAME_STATE_STOPPED;
                this.eventGameStateChanged(oldState, this.gameState);
                return true;
            }
        }
             
        return false;
    }
    
    /**
     * Initialize new game
     * @return 
     */
    public boolean initNewGame(){
        // reset game counter
        this.gameTimeRemaining = this.mainPanel.getGameTime();
        this.updateGuiEvent(null);
        
        // show game over window
        this.frameFinish.setVisible(false);
        
        // player default energy
        if (this.player1 instanceof Player){
            this.player1.setEnergy(100.0);
        }
        
        if (this.player2 instanceof Player){
            this.player2.setEnergy(100.0);
        }
        
        return true;
    }

     /**
     * Used for enable/disable
     * 
     * @param root
     * @param enable 
     */
    static void enableTree(Container root, boolean enable) {
        Component children[] = root.getComponents();        
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Container) {
                enableTree((Container) children[i], enable);
            } else {
                children[i].setEnabled(enable);
            }
        }
        
        root.setEnabled(enable);
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

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }



    public int getGameState() {
        return gameState;
    }

    public void setGameState(int gameState) {
        this.gameState = gameState;
    }

    public long getGameTimeRemaining() {
        return gameTimeRemaining;
    }

    public void setGameTimeRemaining(long gameTimeRemaining) {
        this.gameTimeRemaining = gameTimeRemaining;
    }

    public long getGameTimeLastChange() {
        return gameTimeLastChange;
    }

    public void setGameTimeLastChange(long gameTimeLastChange) {
        this.gameTimeLastChange = gameTimeLastChange;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }   
    
}
