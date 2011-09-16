/*
 * Main executable class of program.
 * (contains public static main() method)
 * 
 * @author ph4r05 (Dusan Klinec)
 * RSSI_graphApp.java
 */

package rssi_localization;
import rssi_graph.rssi.WorkerR2D;
import rssi_graph.localization.WorkerLocalization;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.nodeRegister.SimpleGenericNode;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.File;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;
import rssi_graph.motecom.MessageSender;
import rssi_graph.motecom.NodeDiscovery;

/**
 * The main class of the application.
 *
 * @TODO:
 *  - floorPlan
 */
public class RSSI_graphApp extends SingleFrameApplication {
    /**
     * Mote interface holding BaseStation
     */
    protected MoteIF moteInterface = null;

    /**
     * Global Mobile Node Manager
     */
    protected MobileNodeManager mnm = null;
    
    /**
     * Main program modules
     */
    protected WorkerBase[] workers = null;

    /**
     * Platform dependent line separator here
     */
    protected static String LineSeparator;

    /**
     * Message sender thread
     */
    protected MessageSender msgSender = null;

    /**
     * Node discovery thread
     */
    protected NodeDiscovery nodeDiscovery = null;

    /**
     * Node register instance, only single
     */
    protected NodeRegister nodeRegister = null;

    /**
     * is application started?
     */
    protected static boolean appStarted=false;

    /**
     * Register of initialized panels
     * @deprecated, not implemented yet, another design pattern chosen
     */
    protected HashMap<String, javax.swing.JPanel> panelMap = null;

    /**
     * Main window frame of application
     * to be able to reference it
     */
    protected RSSI_graphView graphViewFrame = null;

    /**
     * Splash screen instance & related attributes
     */
    protected SplashScreen splash;
    protected Graphics2D splashGraphics;
    protected Double splashProgressArea;

    /**
     * Main constructor
     */
    public RSSI_graphApp() {
        initSplash();
        this.updateSplash("Initializing ...",  5);
    }

    /**
     * Initializes splash screen
     */
    protected final void initSplash(){
        // splash screen here
        splash = SplashScreen.getSplashScreen();
        if (splash == null) {
          System.out.println("SplashScreen.getSplashScreen() returned null");
          return;
        }

        // get graphics object
        splashGraphics = splash.createGraphics();
        if (splashGraphics == null) {
          System.out.println("g is null");
          return;
        }

        // basic slash init
        Dimension ssDim = splash.getSize();
        int height = ssDim.height;
        int width = ssDim.width;

        // stake out some area for our status information
        splashProgressArea = new Rectangle2D.Double(10, 455, 530, 12);
    }

    /**
     * Displays new message and percentage to splash window
     * 
     * @param message
     * @param pct
     */
    protected final void updateSplash(String message, int pct){
        if (splash==null || !(splash instanceof SplashScreen) || !splash.isVisible()) return;
        RSSI_graphApp.splashProgress(splash, splashGraphics, splashProgressArea, message,  pct);
        splash.update();
    }


    /**
     * Initialization from arguments
     * specify -comm parameter to determine basestation correctly.
     * Otherwise /dev/sensor_bs will be probed, tmote platform is chosen by default.
     * If /dev/sensor_bs does not exists, /dev/tmote_sky_[0-9] will be probed.
     * 
     * @param args
     */
    @Override protected void initialize(String[] args) {
        PhoenixSource phoenix=null;
        String source = null;

        // update splash window
        this.updateSplash("Base station initialization",  10);

        // parse parameters to determine basestaion
        if (args.length == 2) {
          if (!args[0].equals("-comm")) {
            usage();
            System.exit(1);
          }
          source = args[1];
        }
        else if (args.length != 0) {
          usage();
          System.exit(1);
        }

        // base station autodetection
        // if was not specified by explicit, use this method
        else {
            File f = null;
            String path = null;
            boolean bs_found=false;

            // always 1loop
            do {
                // search at first for /dev/tmote_sky_bs
                // if is defined, use it as default
                path = "/dev/sensor_bs";
                f = new File(path);
                if (f.exists() && f.canRead()){
                    bs_found=true;
                    break;
                }

                // search for default tmote file, assume we use our udev script so every
                // tmote has symlink like /dev/tmote_sky_0 -> /dev/ttyUSB0
                // search for first available tmote device
                for (int i=0; i<10 && bs_found==false; i++){
                    path = "/dev/tmote_sky_" + i;
                    f = new File(path);

                    // does file exists and can I read it?
                    if (f.exists() && f.canRead()){
                        bs_found=true;
                        break;
                    }
                }
                
            } while(false);

            // if base station was found, use it
            if (bs_found){
                source = "serial@" + path + ":tmote";
                System.err.println("Base station autodetection was successfull. Using mote on: " + path);
            }
            else {
                // source is null => base station is not connected, application will work in limited mode
                System.err.print("Cannot autodetect any suitable base station. Please take a look at configuration.");
                source=null;
            }
        }

        // can connect to base station?
        // in try block
        try {
            // only if some source is defined
            if (source != null) {
                phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);

                // phoenix is not null, can create packet source and mote interface
                if (phoenix != null) {
                    this.updateSplash("Registering base station", 20);

                    // loading phoenix
                    this.moteInterface = new MoteIF(phoenix);
                } else {
                    // phoenix is null, cannot connect to base station
                    this.moteInterface = null;
                }
            }
        } catch(Exception e){
            System.err.println("Cannot create default packet source; Is basestation connected? Exception: " + e.getMessage());
            
            // reset phoenix to indicate error/non-detected base station
            phoenix=null;
            this.moteInterface = null;
        }
        
        // initialization block
        try {
            this.updateSplash("Classes initialization",  30);

            // set packet error iff moteif is not null
            if (moteInterface!=null){
                PacketError pe = new PacketError();
                pe.setMoteif(moteInterface);
            
                moteInterface.getSource().setResurrection();
                moteInterface.getSource().setPacketErrorHandler(pe);
            }
            
            // new message sender
            this.msgSender = new MessageSender(moteInterface, null);

            // node register
            this.nodeRegister = new NodeRegister();
            mnm = new MobileNodeManager(this.nodeRegister);
            this.nodeRegister.setMnm(mnm);

            // now init node register manually
            // gateway node
            GenericNode baseStation = new SimpleGenericNode(true, 1, -1, "BaseStation");
            this.nodeRegister.setBaseStation(baseStation);
            this.nodeRegister.addNode(baseStation);

            // node discovery thread
            this.nodeDiscovery = new NodeDiscovery(nodeRegister, msgSender);
        }
        catch(Exception e){
            System.err.println("Error from exception: " + e.getMessage());
        }

        this.updateSplash("Registering objects",  40);

        // app really started -> modules can register itself
        RSSI_graphApp.appStarted=true;

        // initialize panel map
        this.panelMap = new HashMap<String, JPanel>(16);

        // store line separator
        RSSI_graphApp.LineSeparator = System.getProperty("line.separator");

        // current date
        java.util.Date today = new java.util.Date();

        this.updateSplash("Instantiating modules",  45);

        // new workers
        this.workers = new WorkerBase[10];
        this.workers[0] = new WorkerLocalization(moteInterface);
        this.workers[1] = new WorkerR2D(moteInterface);
        this.workers[2] = new WorkerCommands(moteInterface);

        // start msgSender thread
        this.msgSender.start();
        this.nodeDiscovery.start();
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        this.updateSplash("Creating GUI forms",  60);

        RSSI_graphView fw = new RSSI_graphView(this);
        graphViewFrame = fw;

        // here connecto workers to the window and invoke turnOn
        this.updateSplash("Connecting and starting modules",  75);

        // iterate over every worker available
        if (this.workers!=null){
            for(int i=0, cnI=this.workers.length; i<cnI; i++){
                WorkerBase wb = this.workers[i];
                if (wb==null || !(wb instanceof WorkerBase)) continue;
                wb.turnOn();

                this.updateSplash("Turning on: " + wb.toString(), (int) (75 + (25.0 / cnI)));
            }
        }

        // as last step dispose() splashScreen and show main window
        this.updateSplash("Finishing",  100);

        // if basestation is null, show warning alert
        if (this.moteInterface == null)
            JOptionPane.showMessageDialog(null,
                "Base station cannot be detected!" + RSSI_graphApp.getLineSeparator() +
                "Application will be running in limited mode.",
                "SensorNode warning",
                JOptionPane.WARNING_MESSAGE);

        // show main form
        show(fw);

        // close splash for now
        if (splash!=null && splash.isVisible()){
            splash.close();
        }
        System.err.println("Initialization done");
    }

    /**
     * Display a (very) basic progress bar
     * @param pct how much of the progress bar to display 0-100
     */
    public static void splashProgress(SplashScreen splash, Graphics2D splashGraphics, Double splashProgressArea, String message, int pct)
    {
        if (splash != null && splash.isVisible())
        {
            String dispMessage = null;
            if (message==null){
                dispMessage = "Initializing";
            }
            else {
                dispMessage = message;
            }

            splashGraphics.setComposite(AlphaComposite.Clear);
            splashGraphics.fillRect(0, 430, 550, 34);
            splashGraphics.setPaintMode();
            splashGraphics.setColor(Color.BLACK);
            splashGraphics.drawString(dispMessage + "...", 10, 448);

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
            splashGraphics.setPaint(new Color(214,213,213));
            splashGraphics.fill(splashProgressArea);

            // draw an outline
            splashGraphics.setPaint(new Color(62, 120, 156));
            splashGraphics.draw(splashProgressArea);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea.getMinX();
            int y = (int) splashProgressArea.getMinY();
            int wid = (int) splashProgressArea.getWidth();
            int hgt = (int) splashProgressArea.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(new Color(62, 120, 156));
            splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
        
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of RSSI_graphApp
     */
    public static RSSI_graphApp getApplication() {
        return Application.getInstance(RSSI_graphApp.class);
    }

    private static void usage() {
        System.err.println("usage: RSSI_graphApp [-comm <source>]");
     }

    @Override
    protected void shutdown() {
        System.err.println("Application shutdown called");
        super.shutdown();

        // shutdown thread node discovery
        if (this.nodeDiscovery!=null && this.nodeDiscovery.isAlive()){
            this.nodeDiscovery.setDoDiscovery(false);
            this.nodeDiscovery.setShutdown(true);
            this.nodeDiscovery.interrupt();
        }

        if (this.msgSender!=null && this.msgSender.isAlive()){
            this.msgSender.setShutdown(true);
            this.msgSender.interrupt();
        }
    }

    @Override
    protected void end() {
        System.err.println("Application ending");
        super.end();

         // shutdown thread node discovery
        if (this.nodeDiscovery!=null && this.nodeDiscovery.isAlive()){
            this.nodeDiscovery.setDoDiscovery(false);
            this.nodeDiscovery.setShutdown(true);
            this.nodeDiscovery.interrupt();
        }

        if (this.msgSender!=null && this.msgSender.isAlive()){
            this.msgSender.setShutdown(true);
            this.msgSender.interrupt();
        }
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(RSSI_graphApp.class, args);
    }

    public RSSI_graphView getGraphViewFrame() {
        return graphViewFrame;
    }

    public WorkerBase getWorker(int i){
        return workers[i];
    }

    public WorkerBase[] getWorkers() {
        return workers;
    }

    public HashMap<String, JPanel> getPanelMap() {
        return panelMap;
    }

    public static boolean isAppStarted() {
        return appStarted;
    }

    public static String getLineSeparator() {
        return RSSI_graphApp.LineSeparator;
    }
    
    public MessageSender getMsgSender() {
        return msgSender;
    }

    public NodeRegister getNodeRegister() {
        return nodeRegister;
    }

    public void setNodeRegister(NodeRegister nodeRegister) {
        this.nodeRegister = nodeRegister;
    }

    public MobileNodeManager getMnm() {
        return mnm;
    }

    public void setMnm(MobileNodeManager mnm) {
        this.mnm = mnm;
    }

    public NodeDiscovery getNodeDiscovery() {
        return nodeDiscovery;
    }

    public void setNodeDiscovery(NodeDiscovery nodeDiscovery) {
        this.nodeDiscovery = nodeDiscovery;
    }

    public static NodeDiscovery sGetNodeDiscovery(){
        return RSSI_graphApp.getApplication().getNodeDiscovery();
    }

    public static NodeRegister sGetNodeRegister(){
        return RSSI_graphApp.getApplication().getNodeRegister();
    }

    public static MessageSender sGetMessageSender(){
        return RSSI_graphApp.getApplication().getMsgSender();
    }
}
