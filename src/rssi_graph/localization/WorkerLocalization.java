/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.localization;

import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.MobileNode;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import java.awt.event.ItemEvent;
import rssi_graph.messages.MultiPingResponseReportMsg;
import rssi_graph.messages.MultiPingResponseMsg;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.RSSI_graphApp;
import rssi_graph.WorkerBase;
import rssi_graph.WorkerInterface;
import rssi_graph.messages.CommandMsg;
import rssi_graph.messages.MultiPingResponseTinyReportMsg;
import rssi_graph.rssi.OfflineLocalizationSettings;
import rssi_graph.rssi.RSSI2DistInternalBuffer;
import rssi_graph.rssi.RSSI2DistLogNormalShadowing;
import rssi_graph.utils.TextHistogram;

/**
 * Main executable module, performing localization
 * Everything common with localization sits in this class
 *
 * @author ph4r05
 */
public class WorkerLocalization extends WorkerBase implements MessageListener, WorkerInterface, ActionListener {
  /**
   * Manages all mobile nodes data
   */
  private MobileNodeManager mobileNodeManager = null;

  private JPanelLocalizationMain mainPanel = null;

  boolean useSampleTimer=false;
  private javax.swing.Timer sampleDataTimer = null;

  private RSSI2DistLogNormalShadowing distanceFunction = null;

  private Set<Integer> anchors = null;

  private Map<Integer, CoordinateRecord> coords = null;

  // to be integrated in mote register

  private LocalizationNllsFunction locFunction = null;

  private LocalizationNllsLM locFunctionOpt = null;

  private double smoothingAlpha = 0.05;
  
  /**
   * Id of heuristic used for localization
   */
  private int heuristicId=0;
  
  /**
   * Heuristic exponent, weighted localization. 1/(Distance^G)
   */
  private double heuristic_exponent=0;
  
  /**
   * Simple history. Holds last localizationEstimate object for node.
   */
  private Map<Integer, LocalizationEstimate> localizationHistory;

    public WorkerLocalization(MoteIF mi) {
        // init mote interface
        moteIF = mi;

        // get mobile node manager from node register
        
        mobileNodeManager = RSSI_graphApp.sGetNodeRegister() == null ? null : RSSI_graphApp.sGetNodeRegister().getMnm();
        
        // init localizaton history
        localizationHistory = new HashMap<Integer, LocalizationEstimate> ();

//        // register listeners for response, report
//        mi.registerListener(new MultiPingResponseMsg(), this);
//        mi.registerListener(new MultiPingResponseReportMsg(), this);

        /**
         * @todo: register listener for command
         */
    }

    /**
     * Logger override
     * @param s
     */
    @Override
    public void logToTextarea(String s) {
        super.logToTextarea(s, 9, "Localization", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Logger override
     *
     * @param s
     * @param severity
     */
    @Override
    public void logToTextarea(String s, int severity) {
        super.logToTextarea(s, 9, "Localization", severity);
    }

    /**
     * reload settings to internal data structures
     */
    public boolean reFetchSettings(){

        // TIMER
        int timerDelay = this.mainPanel.getjPanelLocalizationCommand1().lookupSampleTimerDelay();
        if (this.sampleDataTimer == null){
            this.sampleDataTimer = new Timer(timerDelay, this);
        }

        this.sampleDataTimer.setActionCommand("sampleDataTimer");
        this.sampleDataTimer.setRepeats(false);
        this.sampleDataTimer.setDelay(timerDelay);
        this.sampleDataTimer.setInitialDelay(timerDelay*5);
        this.sampleDataTimer.stop();

        // RSSI->Distance function
        this.distanceFunction = new RSSI2DistLogNormalShadowing();
        Map<String, Double> functionParameters = this.mainPanel.getjPanelLocalizationSettings1().getModelParameters();
        this.distanceFunction.setParameters(functionParameters);

        this.smoothingAlpha = this.mainPanel.getjPanelLocalizationSettings1().lookupSmoothingAlpha();

        // POSITION & ANCHORS SETTINGS
        this.coords = new HashMap<Integer, CoordinateRecord>();
        this.anchors = this.mainPanel.getjPanelLocalizationSettings1().getAnchorNodes();
        if (this.anchors==null || this.anchors.isEmpty()){
            this.logToTextarea("No anchor nodes!", JPannelLoggerLogElement.SEVERITY_ERROR);
            return false;
        }

        Iterator<Integer> it = this.anchors.iterator();
        while(it.hasNext()){
            Integer curNodeId = it.next();
            CoordinateRecord coordsFor = this.mainPanel.getjPanelLocalizationSettings1().getCoordsFor(curNodeId);
            if (coordsFor==null){
                this.logToTextarea("Coord point is null!", JPannelLoggerLogElement.SEVERITY_ERROR);
                return false;
            }

            this.coords.put(curNodeId, coordsFor);
        }

        this.locFunctionOpt = new LocalizationNllsLM();

        // everything went fine, end
        return true;
    }

     /**
     * Turn module on
     * - register message listeners
     * - start necessary connections (sqlite)
     */
    @Override
    public void turnOn(){
        // register listeners for response, report
        if (moteIF != null){
            moteIF.registerListener(new CommandMsg(), this);
            moteIF.registerListener(new MultiPingResponseMsg(), this);
            moteIF.registerListener(new MultiPingResponseReportMsg(), this);
            moteIF.registerListener(new MultiPingResponseTinyReportMsg(), this);
        }

        // node register
        this.nodeRegister = RSSI_graphApp.sGetNodeRegister();

        // instantiate new mobile manager
        this.mobileNodeManager = RSSI_graphApp.sGetNodeRegister().getMnm();
        
        // init localizaton history
        this.localizationHistory = new HashMap<Integer, LocalizationEstimate> ();

        // need main panel for settings lookup & displaying results
        if (mainPanel == null){
            this.mainPanel = RSSI_graphApp.getApplication().getGraphViewFrame().getjPanelLocalizationMain1();
            this.mainPanel.getjPanelRSSIInTime1().initThis();
            this.mainPanel.getjPanelDistanceInTime1().initThis();

            this.mainPanel.getjPanelLocalizationSettings1().initThis();
            this.mainPanel.getjPanelLocalizationCommand1().initThis();

            this.mainPanel.getjPanelNetworkMap1().setNodeRegister(RSSI_graphApp.sGetNodeRegister());
            this.mainPanel.getjPanelNetworkMap1().setMnm(mobileNodeManager);
            this.mainPanel.getjPanelNetworkMap1().setWorker(this);
            this.mainPanel.getjPanelNetworkMap1().initThis();
        }

        // fetch settings data from settings panel
        boolean success = this.reFetchSettings();
        if (success==false) return;

        // node register test
        Set<Integer> nodeSet = RSSI_graphApp.sGetNodeRegister().getNodesSet();
        for(Integer curNode: nodeSet){
            System.err.println("Found in nodeRegister: " + curNode);
        }

        // log
        this.logToTextarea("Module RSSI->Distance registered and turned on. Listening to messages.", JPannelLoggerLogElement.SEVERITY_INFO);
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
            // moteIF.deregisterListener(new CommandMsg(), this);
            moteIF.deregisterListener(new MultiPingResponseMsg(), this);
            moteIF.deregisterListener(new MultiPingResponseReportMsg(), this);
            moteIF.deregisterListener(new MultiPingResponseTinyReportMsg(), this);
        }

        this.sampleDataTimer.stop();

        // log
        this.logToTextarea("Module RSSI->Distance registered and turned off", JPannelLoggerLogElement.SEVERITY_INFO);
    }

    @Override
    public String toString() {
        return "Localization module";
    }

    /**
     * Central event handler used for all messages needed
     * According to AMtype decide what to do next
     *
     * According to worker's internal state this function routes messages to appropriate
     * methods for further processing.
     *
     * There can be several different states such as:
     *  - localization
     *  - network configuration
     * Under different conditions we handle messages by different way
     * For localization use MobileNode class
     *
     *
     * @implements MessageListener
     * @param i
     * @param msg
     */
    @Override
    public void messageReceived(int to, Message message) {
        try{
            
            long curMilis = System.currentTimeMillis();
            
            // determine source
            int source = message.getSerialPacket().get_header_src();
            MobileNode mn = null;
        
            // get corresponding node from node register
            // set last seen counter for this node (sender of message)
            GenericNode curNode = null;
            if (this.nodeRegister.existsNode(source)){
                curNode = this.nodeRegister.getNode(source);
                curNode.setLastSeen(curMilis);
            }
            
            // need to be changed, categorizing with amType does not work
            // sometime throws exception - cannot cast class
            if (message instanceof CommandMsg){
                // nothing to do for now                
                return;
            }

            else if(message instanceof MultiPingResponseMsg){
                    MultiPingResponseMsg responseMsg = (MultiPingResponseMsg) message;
                    // simple ping response received
                    // can happen only when message comes from Base Station
                    int sourceNodeId = responseMsg.getSerialPacket().get_header_src();

                    // mobile manager for handling mobile nodes
                    mn = mobileNodeManager.getMobileNode(sourceNodeId, true);

                    // normalize tx power for mobile nodes
                    int normalizedRssi = this.nodeRegister.existsNode(sourceNodeId) ?
                               (int) this.nodeRegister.getNode(sourceNodeId).getNormalizedRssi(responseMsg.get_rssi(), 0) :
                               responseMsg.get_rssi();
                    
                    // add to floating mean
                    // only base station can hear direct response from node, so
                    // source of this meassurement is base station with ID 1
                    mn.addToFloatingMean(1, normalizedRssi , this.smoothingAlpha);
                    mobileNodeManager.incReportFreshnessFor(mn.getMobile_nodeID());

                    // tick for freshness mechanism (aging all mobile nodes to
                    // disable localization for not working mobile nodes)
                    mobileNodeManager.tickReportFreshness();
            }

            else if (message instanceof MultiPingResponseReportMsg){
                    MultiPingResponseReportMsg reportMsg = (MultiPingResponseReportMsg) message;

                    // message router here!
                    // by default for time reasons use simple router now
                    // directly to localization

                    // from beginning assume only reports
                    try {
    ////////                    HashSet<MobileNode> mobileNodes2localization = new HashSet<MobileNode>(4);
                        for(int i = 0; i<reportMsg.get_datanum(); i++){
                            // get mobile node from mobile manager
                            // now it is important do decide wether source node is mobile or
                            // we are in network testing phase when static nodes are pinged.
                            // for now its mobile node, since test phase is not implemented yet.
                            mn = mobileNodeManager.getMobileNode(reportMsg.get_nodeid()[i], true);

                            // normalize tx power for mobile nodes
                            int normalizedRssi = this.nodeRegister.existsNode(mn.getMobile_nodeID()) ?
                               (int) this.nodeRegister.getNode(mn.getMobile_nodeID()).getNormalizedRssi(reportMsg.get_rssi()[i], 0) :
                               reportMsg.get_rssi()[i];
                            
                            // add observed rssi value to floating/moving mean
                            mn.addToFloatingMean(source, normalizedRssi , this.smoothingAlpha);
                            mobileNodeManager.incReportFreshnessFor(mn.getMobile_nodeID());
                            
                            // send freshness to reported mobile nodes (they are alive probably too)
                            mn.getGenericNode().setLastSeen(curMilis);

                            // old code used windowed mean
    //////                        // if is able to insert new element, do it
    //////                        if (mn.inputQueueCanInsert()){
    //////                            // create new RSSI response record to be created
    //////                            // now we need some reference to packet which requested this action
    //////                            // is needed to handle requester to this.
    //////                            RSSIResponseRecord rrr = new RSSIResponseRecord();
    //////                            rrr.setCounter(reportMsg.get_nodecounter()[i]);
    //////                            rrr.setNodeID_report_from(source);
    //////                            rrr.setRssi(reportMsg.get_rssi()[i]);
    //////
    //////                            mn.inputQueueAdd(rrr);
    //////
    //////                            mobileNodes2localization.add(mn);
    //////                        }
                        }
    //////
    //////                    // main worker, perform localization on every mobile node occurred
    //////                    Iterator<MobileNode> it = mobileNodes2localization.iterator();
    //////                    while (it.hasNext()){
    //////                        localize(it.next());
    //////                    }


                    } catch (Exception ex) {
                        Logger.getLogger(RSSI_graphApp.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // tick for freshness mechanism (aging all mobile nodes to
                    // disable localization for not working mobile nodes)
                    mobileNodeManager.tickReportFreshness();
            }
        } catch(ClassCastException e){
            this.logToTextarea("Exception thrown: cannot cast class; e: " + e.getMessage() + "; stack: " + e.getStackTrace(), JPannelLoggerLogElement.SEVERITY_ERROR);
        } catch(Exception e){
            this.logToTextarea("Exception thrown: unknown exception; e: " + e.getMessage() + "; stack: " + e.getStackTrace(), JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }

    //public Map<Integer,Double> getAnchorWeights(int mobileid, )
    
    /**
     * To support mass offline localization of node
     * 
     * @throws FunctionEvaluationException
     * @throws OptimizationException 
     */
    public void localize(OfflineLocalizationSettings locSettings) throws FunctionEvaluationException, OptimizationException {
        // localize data from loc settings
        if (locSettings==null || locSettings.getRssiData()==null || locSettings.getRealPositions()==null){
            return;
        }
        // now we have data, localize now
        Map<String, ArrayList<RSSI2DistInternalBuffer>> rssiData = locSettings.getRssiData();
        Map<Integer, CoordinateRecord> realPositions = locSettings.getRealPositions();
        Iterator<String> iterator = rssiData.keySet().iterator();
	Set<Integer> toLocalize = locSettings.getMidToLocalize();
        
        // is network map available?
        boolean networkMapAvailable = this.mainPanel!=null && this.mainPanel.getjPanelNetworkMap1()!=null;
        if (locSettings.isShowInNetworkMap() && networkMapAvailable){
            // clear history to get clean results
            this.mainPanel.getjPanelNetworkMap1().flushEstimateHistory(null);
        }
        
        // update freshness
        this.mobileNodeManager.incReportFreshnessFor(locSettings.getMobileNodeId());
        MobileNode mobileNode = this.mobileNodeManager.getMobileNode(locSettings.getMobileNodeId(), true);
        
        // fetch settings
        this.reFetchSettings();
        
        // for error counting
        double errorDist=0;
        double errorX=0;
        double errorY=0;
        double rms=0;
        int cnLocalized=0;
        
        // histograms for localization error evaluation
        TextHistogram tHistogram4 = new TextHistogram();
        tHistogram4.setStep(0.25);
        
        TextHistogram tHistogram2 = new TextHistogram();
        tHistogram2.setStep(0.5);
        
        TextHistogram tHistogram1 = new TextHistogram();
        tHistogram1.setStep(1.0);
        
        // one iteration = one localization of single mobile node
        while(iterator.hasNext()){
            String t = iterator.next();
            
            // null test
            if (t==null || t.isEmpty()) continue;
            
            ArrayList<RSSI2DistInternalBuffer> curBuffer = rssiData.get(t);
            if (curBuffer==null || curBuffer.isEmpty()) continue;
            
            int curMid = curBuffer.get(0).getMid();
            CoordinateRecord curRealPos=null;
	    
	    // to localize?
            // adds ability to specify which nodes needs to be localized from while set.
            // inclusion mechanism. If null, localize all nodes.
	    if (toLocalize!=null && !toLocalize.contains(Integer.valueOf(curMid))){
		continue;
	    }
            
            // get current mid
            if (realPositions.containsKey(curMid)){
                curRealPos = realPositions.get(curMid);
            }
            
            // set current data (floating means)
            Iterator<RSSI2DistInternalBuffer> iterator1 = curBuffer.iterator();
            
            // reset floating means
            mobileNode.setFloatingMeanRssi(new HashMap<Integer, Double>());
            
            // set correct RSSI values from saved buffer
            while(iterator1.hasNext()){
                RSSI2DistInternalBuffer nextSubBuff = iterator1.next();
                if (nextSubBuff.getStats()==null) continue;
                
                Double source=nextSubBuff.getStats().getMean();
                if (locSettings.getRssiSource() == OfflineLocalizationSettings.RSSI_SOURCE_MEDIAN){
                    source=nextSubBuff.getStats().getMedian();
                }
                
                // source fix
                // if mobile node id == X and source = X => signal meassured on BS
                int reportingMote = nextSubBuff.getReportingMote();
                if (reportingMote==mobileNode.getNodeId()){
                    reportingMote=1;
                }
                
                mobileNode.setFloatingMeanRssi(reportingMote, source);
            }
            
            // set real position
            GenericNode node = this.nodeRegister.getNode(mobileNode.getNodeId());
            if (node==null) continue;
            
            node.setPosition(curRealPos);
            try {
                this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Localizing MID: " + curMid
                                    + "; mobile=" + mobileNode.getNodeId() + "; posX="+curRealPos.getX()+"; posY="+curRealPos.getY());
                
                // localize method call
                // will localize given nodes. It is important not to have any localization running
                // at this moment so as not to get mixed results.
                this.localize();
            } catch (FunctionEvaluationException ex) {
                Logger.getLogger(WorkerLocalization.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OptimizationException ex) {
                Logger.getLogger(WorkerLocalization.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // evaluate results
            // compute error
            if (curRealPos!=null){
                ++cnLocalized;
                errorDist+=mobileNode.getPositionEstimationError();
                errorX+=mobileNode.getPositionEstimationErrorX();
                errorY+=mobileNode.getPositionEstimationErrorY();
                rms+=mobileNode.getPositionEstimationRMS();
            }
            
            // localization method stores data about last performed localization cycle
            // for each mobile node to history. Now is history very usefull to get
            // detailed info about localization (positions, errors, weight, ...)
            LocalizationEstimate locEstimate = null;
            if (this.localizationHistory.containsKey(node.getNodeId())){
                locEstimate = this.localizationHistory.get(node.getNodeId());
                
                tHistogram1.add(locEstimate);
                tHistogram2.add(locEstimate);
                tHistogram4.add(locEstimate);
                
                // add to network graph to plot
                if (locSettings.isShowInNetworkMap() 
                        && networkMapAvailable){
                    
                    this.mainPanel.getjPanelNetworkMap1().addToEstimateHistory(locEstimate);
                }
            }
        }
        
        this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Hist 1.00: " + tHistogram1.export());
        this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Hist 0.05: " + tHistogram2.export());
        this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Hist 0.25: " + tHistogram4.export());

        this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Localization finished; meanErrorDist=" + (errorDist/cnLocalized) 
                + "; meanErrorX=" + (errorX/cnLocalized)
                + "; meanErrorY=" + (errorY/cnLocalized)
                + "; meanRMS=" + (rms/cnLocalized)
                + "; cn=" + (cnLocalized)
                );
        
        // refresh positions
        this.nodeRegister.changeNotify(null);
    }
    
    /**
     * Determine weight of anchor nodes for localization according to selected heuristic to use
     * 
     * @param mobileNodeId
     * @param distancesFromAnchors
     * @return 
     */
    public Map<Integer, Double> getAnchorWeights(int mobileNodeId, Map<Integer, Double> distancesFromAnchors){
        if (distancesFromAnchors==null) return null;
        
        Map<Integer, Double> weights = new HashMap<Integer, Double>();
        Iterator<Integer> iterator=null;
        switch(this.heuristicId){
            case 0:
                // standard weights = 1 for all
                iterator = distancesFromAnchors.keySet().iterator();
                while(iterator.hasNext()){
                    Integer nodeId = iterator.next();
                    Double distance = distancesFromAnchors.get(nodeId);
                    
                    // constant weight
                    weights.put(nodeId, Double.valueOf(1.0));
                }
                
                break;
                
            // 3 minimal
            case 1:
                // standard weights = 1 for all
                List<DistanceCompare> distances = new ArrayList<DistanceCompare>(distancesFromAnchors.keySet().size());
                iterator = distancesFromAnchors.keySet().iterator();
                while(iterator.hasNext()){
                    Integer nodeId = iterator.next();
                    Double distance = distancesFromAnchors.get(nodeId);
                    DistanceCompare dc = new DistanceCompare(nodeId, distance);
                    distances.add(dc);
                }
                
                // sort
                Collections.sort(distances);
                
                // set weights
                for(int i=0, cnI=distances.size(); i<cnI; i++){
                    DistanceCompare dc = distances.get(i);
                    weights.put(new Integer(dc.nodeid), new Double(i < 3 ? 1.0 : 0.0));
                }
                
                break;
                
            // inverted g    
            case 2:
                // standard weights = 1 for all
                iterator = distancesFromAnchors.keySet().iterator();
                while(iterator.hasNext()){
                    Integer nodeId = iterator.next();
                    Double distance = distancesFromAnchors.get(nodeId);
                    weights.put(nodeId, Math.abs( 1/Math.pow( Math.max(1, distance), heuristic_exponent)));
                }  
                
                break;
                
             // inverted g , constant from begining 
            case 3:
                // standard weights = 1 for all
                iterator = distancesFromAnchors.keySet().iterator();
                while(iterator.hasNext()){
                    Integer nodeId = iterator.next();
                    Double distance = distancesFromAnchors.get(nodeId);
                    if (distance <= 3) {
                        weights.put(nodeId, Double.valueOf(1.0));
                    } else {
                        weights.put(nodeId, Math.abs( 1/Math.pow( Math.max(1, distance-2), heuristic_exponent)));
                    }
                }  
                
                break;
                
        }
        
        return weights;
    }

    // for base heuristic for localization
    // optimal design: heuristics should be decorators. User could specify
    // which heuristic to use
    // define comparable for heuristic
    class DistanceCompare implements Comparable<DistanceCompare>{
        public int nodeid;
        public double distance;
        public DistanceCompare(int nodeid, double distance){
            this.nodeid=nodeid;
            this.distance=distance;
        }
        public int compareTo(DistanceCompare o) {
            if (o==null) return -1;
            if (distance == o.distance) return 0;

            return distance < o.distance ? -1 : 1;
        }
    }
            
    /**
     * Localize fresh nodes using floating average RSSI data
     *
     * @throws FunctionEvaluationException
     * @throws OptimizationException
     */
    public void localize() throws FunctionEvaluationException, OptimizationException {
        // get fresh nodes here
        Set<MobileNode> fresh = this.mobileNodeManager.getFreshNodes();
        Iterator<MobileNode> it = fresh.iterator();
        
        // fetch data from heuristics panel
        JPanelLocalizationHeuristics jPanelLocalizationHeuristics1 = this.mainPanel.getjPanelLocalizationHeuristics1();
        this.setHeuristicId(jPanelLocalizationHeuristics1.getHeuristicId());
        this.setHeuristic_exponent(jPanelLocalizationHeuristics1.getInvertedExponent());
        
        for(int i=0; it.hasNext(); i++){
            // get mobile node
            MobileNode mn = it.next();
            Map<Integer, Double> floatingMean = mn.getFloatingMean();

            // get generic node counterpart
            GenericNode curMobileGenericNode = this.nodeRegister.getNode(mn.getNodeId());

            // no data for distance estimation.
            // Better design: pass whole object to distance model/localization model
            // and return answer if this data can be used for localization.
            if (floatingMean==null){
                continue;
            }

            // test if there is enough data
            // for trilateration at leas 3 independent samples are needed.
            // Better design for this: localization model should decide what is
            // enough for localization purposes.
            if (floatingMean.size() < 2){
                continue;
            }

            // store nodeID to index(raw data for localization) association to be able
            // to reference data and to allow for heuristics to manage this data
            Map<Integer,Integer> nodeToIndexAssociation = new HashMap<Integer, Integer>();
            Map<Integer,Double> anchorWeights = null;

            boolean error = false;
            boolean[] applicableDistances = new boolean[floatingMean.size()];
            double[] distances = new double[floatingMean.size()];
            double[][] positions = new double[floatingMean.size()][3];
            Iterator<Integer> itAnchor = floatingMean.keySet().iterator();

            // computed distances from anchors
            Map<Integer,Double> distancesFromAnchors = new HashMap<Integer, Double>();

            for(int j=0; itAnchor.hasNext(); j++){
                Integer curAnchor = itAnchor.next();
                GenericNode curAnchorNode = this.nodeRegister.getNode(curAnchor);
                nodeToIndexAssociation.put(Integer.valueOf(curAnchorNode.getNodeId()), Integer.valueOf(j));

                // set this value as applicable
                applicableDistances[j]=true;

                // compute distance from RSSI reading using appropriate distance function
                // @todo: tx mobile node calibration (each node can transmit with 
                // another real output power values at maximal tx power) should
                // be considered here, so we would need to pass mobile node ID
                // to lookup reference RSSI at 1meter.
                distances[j] = curAnchorNode.getDistanceFunction().getDistanceFromRSSI(floatingMean.get(curAnchor));

                // check if this anchor has defined its coordinates in node register
                // if this node selected as anchor?
                if (curAnchorNode.getPosition()==null
                        || curAnchorNode.isAnchor()==false ){
                    // we don't have anchor's position. this computed distance is not applicable
                    applicableDistances[j]=false;
                    continue;
                }

                // set distance from anchor to node register
                distancesFromAnchors.put(curAnchor, distances[j]);

                CoordinateRecord coordPoint = curAnchorNode.getPosition();
                positions[j][0] = coordPoint.getX();
                positions[j][1] = coordPoint.getY();
                positions[j][2] = coordPoint.getZ();

                this.mainPanel.getjPanelLocalizationLog1().addLogEntry("MeanRSSI for: " + mn.getMobile_nodeID()
                                    + "anchor=" + curAnchor + "; rssi="+floatingMean.get(curAnchor));
                this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Distance for: " + mn.getMobile_nodeID()
                                    + "anchor=" + curAnchor + "; distance="+distances[j]);
            }
            
            // get anchor weights according to selected heuristic
            anchorWeights = getAnchorWeights(mn.getMobile_nodeID(), distancesFromAnchors);

            // check if we have at least 3 applicable distances
            int numOfApplicableDistances = 0;
            for(int j=0; j<floatingMean.size(); j++){
                numOfApplicableDistances += applicableDistances[j] ? 1 : 0;
            }

            if (numOfApplicableDistances<2 || error){
                continue;
            }
            
            // application of anchor weights
            double[] anchorWeightsValues = new double[anchorWeights.keySet().size()];
            // target of optimization = 0, we want residuals to get to 0 after optimization
            double[] target = new double[anchorWeights.keySet().size()];
            Iterator<Integer> wIt = anchorWeights.keySet().iterator();
            for(int tmpI=0; wIt.hasNext(); tmpI++){
                Integer curAnchorId = wIt.next();
                Double weight = anchorWeights.get(curAnchorId);
                int indexOfNodeData = nodeToIndexAssociation.get(curAnchorId);
                anchorWeightsValues[tmpI] = weight;
                target[tmpI]=0.0;
                
                if (weight<=0.05){
                    // unset from distances map. Not to draw circles. We do not use this
                    // data so we remove them.
                    //distancesFromAnchors.remove(Integer.valueOf(curAnchorId));
                }
            }

            // set distances from anchors
            mn.setDistancesFromAnchors(distancesFromAnchors);

            locFunctionOpt.getFunction().setDistances(distances);
            locFunctionOpt.getFunction().setPositions(positions);
            locFunctionOpt.getFunction().setApplicableDistances(applicableDistances);

            // select last localized point as initial
            double[] startPoint = new double[] { 0.0, 0.0 };
//            if (this.localizationHistory!=null 
//                    && this.localizationHistory.containsKey(mn.getMobile_nodeID())){
//                LocalizationEstimate est = this.localizationHistory.get(mn.getMobile_nodeID());
//                CoordinateRecord estimatedPosition = est.getEstimatedPosition();
////                if (estimatedPosition!=null) {
////                    startPoint[0] = estimatedPosition.getX();
////                    startPoint[1] = estimatedPosition.getY();
////                }
//            }
            
            // localize
            try {
                // optimize NLS to get position
                double[] position = locFunctionOpt.getPosition(target, anchorWeightsValues, startPoint);
                if (position==null || position.length<2){
                    continue;
                }

                CoordinateRecord resultCoord = new CoordinateRecord(position[0], position[1]);
                this.addLocalizedPointToLog(mn.getMobile_nodeID(), resultCoord);
                
                // new estimate object, store to history
                LocalizationEstimate localizationEstimate = new LocalizationEstimate();
                localizationEstimate.setMobileNodeId(mn.getNodeId());
                localizationEstimate.setEstimatedPosition(resultCoord);
                localizationEstimate.setRms(locFunctionOpt.getLmOptimizer().getRMS());
                localizationEstimate.setTime(System.currentTimeMillis());
                
                // model dependent, maybe probability map for positions...
                // better: abstract object, can be(distance, probability map, ...)
                localizationEstimate.setDistancesFromAnchors(mn.getDistancesFromAnchors());
                
                // localization error caluclation
                // if position of mobile node is defined
                if (curMobileGenericNode!=null && curMobileGenericNode.getPosition()!=null){
                    CoordinateRecord positionReal = curMobileGenericNode.getPosition();
                    double errorX = Math.round(Math.abs(positionReal.getX() - resultCoord.getX()) * 1e2) / 1e2;
                    double errorY = Math.round(Math.abs(positionReal.getY() - resultCoord.getY()) * 1e2) / 1e2;
                    double errorDist = Math.sqrt(
                            (positionReal.getX() - resultCoord.getX())*(positionReal.getX() - resultCoord.getX())
                            +
                            (positionReal.getY() - resultCoord.getY())*(positionReal.getY() - resultCoord.getY())
                            );
                    errorDist = Math.round(Math.abs(errorDist) * 1e2) / 1e2;
                    
                    mn.setPositionEstimationError(errorDist);
                    mn.setPositionEstimationErrorX(errorX);
                    mn.setPositionEstimationErrorY(errorY);
                    
                    // real position for estimate object
                    localizationEstimate.setRealPosition(positionReal);
                    
                    this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Localization error for: " + mn.getMobile_nodeID()
                                    + "; errorX=" + errorX + "; errorY=" + errorY + "; errorDist=" + errorDist);
                } else {
                    mn.setPositionEstimationError(0);
                    mn.setPositionEstimationErrorX(0);
                    mn.setPositionEstimationErrorY(0);
                }

                this.mainPanel.getjPanelLocalizationLog1().addLogEntry("");

                // add localized point to register
                mn.setComputedPosition(resultCoord);
                mn.setPositionEstimationRMS(locFunctionOpt.getLmOptimizer().getRMS());
                
                // store to history
                this.localizationHistory.put(Integer.valueOf(mn.getNodeId()), localizationEstimate);
            }
            catch(Exception e){
                this.logToTextarea("Something bad happened during localization: " + e.getMessage(), JPannelLoggerLogElement.SEVERITY_WARNING);
            }

            // redraw network map
            this.getMainPanel().getjPanelNetworkMap1().redraw();

            // start timer again
            if (useSampleTimer){
                this.sampleDataTimer.start();
            }
        }
    }

    public void addLocalizedPointToLog(int mobileNode, CoordinateRecord coordPoint){
        this.mainPanel.getjPanelLocalizationLog1().addLogEntry("Position for: " + mobileNode
                + "; X="+coordPoint.getX()
                + "; Y="+coordPoint.getY());
    }

    public Set<MobileNode> getActiveMobileNodes(){
        // get fresh nodes here
        return this.mobileNodeManager.getFreshNodes();
    }

    /**
     * Main localization worker
     * Now working with bucket filling
     * 
     * @param mobile_node
     * @deprecated for now, used bucket filing localization
     */
//    public void localize(MobileNode mobile_node){
//        // perform localization only if there was some timeout since last lozalization
//        // not to waste resources and this approach need to fill queues with fresh new data
//        // distance between lastLocalizationCounter and maxCounter has to be greater than timeout required
//        int packetDistance = mobile_node.getCounterDistance(mobile_node.getLastLocalizationCounter(), mobile_node.getMaxCounter());
//        if (packetDistance < 100){
//            System.err.println("Err: Not timeouted yet, distance="+packetDistance+"; maxCounter="+mobile_node.getMaxCounter()+
//                    "; lastCounter=" + mobile_node.getLastLocalizationCounter());
//            return;
//        }
//        
//        // get queues
//        ArrayList<RSSIInputQueue> riqs = mobile_node.getActiveQueues();
//
//        // if there is less than 3 active records cannot perform trilateration/multilateration
//        if (riqs.size() < 3) {
//            System.err.println("Err: Cannot perform localization since there is not enough active records");
//            return;
//        }
//
//        System.err.println("OK, enought active queues, cn: " + riqs.size());
//
//        // at least 3 queues has to have more than 3 samples
//        RSSIInputQueue[] usableQueues = new RSSIInputQueue[riqs.size()];
//        int usableQueuesNumber=0;
//        int minSize=10000;
//
//        Iterator<RSSIInputQueue> it = riqs.iterator();
//        while(it.hasNext()){
//            RSSIInputQueue riq = it.next();
//
//            int qsize = riq.getInputQueue().size();
//            if (qsize > 100){
//                usableQueues[usableQueuesNumber] = riq;
//                usableQueuesNumber += 1;
//
//                minSize = qsize < minSize ? qsize : minSize;
//            }
//        }
//
//        // if there are less than 3 usable queues we dont have enough data for localization
//        // we have to wait some time
//        if (usableQueuesNumber < 3){
//            System.err.println("Err: Cannot perform localization. Not enought data, queues: " + usableQueuesNumber + "; ");
//            return;
//        }
//
//        // take last 100 samples from every queue
//        System.err.println("Performing localization with " + minSize + " samples; max counter="+mobile_node.getMaxCounter());
//        mobile_node.setLastLocalizationCounter(mobile_node.getMaxCounter());
//    }

    /**
     * Event handler for buttons and timers
     * 
     * @param ActionEvent e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("sampleDataTimer".equals(e.getActionCommand())
                || "LocalizeNow".equalsIgnoreCase(e.getActionCommand())) {
            try {
                localize();
            } catch (FunctionEvaluationException ex) {
                Logger.getLogger(WorkerLocalization.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OptimizationException ex) {
                Logger.getLogger(WorkerLocalization.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if ("MoveStep".equals(e.getActionCommand())){
            System.err.println("Localizaion; Move step");
        }
        else if ("ApplySettings".equals(e.getActionCommand())){
            this.smoothingAlpha = this.mainPanel.getjPanelLocalizationSettings1().lookupSmoothingAlpha();

            // register change for change notification
            HashMap<Integer, String> dbChange = new HashMap<Integer, String>();

            // save settings from panel to node register
            // iterate over node register and fetch position data to database
            Set<Integer> nodes = this.nodeRegister.getNodesSet();
            Iterator<Integer> nodeIt = nodes.iterator();

            // fetch info about node anchor status
            Set<Integer> anchorNodes = this.mainPanel.getjPanelLocalizationSettings1().getAnchorNodes();
            if (anchorNodes==null) anchorNodes = new HashSet<Integer>();

            while(nodeIt.hasNext()){
                Integer curNode = nodeIt.next();
                try {
                    CoordinateRecord coordsFor = this.mainPanel.getjPanelLocalizationSettings1().getCoordsFor(curNode);
                    GenericNode node = this.nodeRegister.getNode(curNode);
                    
                    node.setPosition(coordsFor);

                    // set anchor status
                    this.nodeRegister.getNode(curNode).setAnchor(anchorNodes.contains(curNode));
                } catch(Exception ex){
                }
            }

            // update rssi->distance function parameters
            int nodeSelected = this.mainPanel.getjPanelLocalizationSettings1().getCurrentNodeId();
            this.nodeRegister.getNode(nodeSelected).getDistanceFunction().setParameters(this.mainPanel.getjPanelLocalizationSettings1().getModelParameters());

            // force node register to notify about data change its listeners
            this.nodeRegister.changeNotify(null);
        }
        else {
            throw new UnsupportedOperationException("I don't know such event!");
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        // all other toggle buttons
        if (javax.swing.JToggleButton.class.equals(e.getItem().getClass())) {
            final JToggleButton jtog = (JToggleButton) e.getItem();
            if ("LocalizationSampling".equals(jtog.getActionCommand())){
                if (e.getStateChange() == ItemEvent.SELECTED){
                    // re-fetch settings to get actual data from form
                    this.reFetchSettings();
                    
                    // start localization timer
                    this.useSampleTimer=true;
                    this.sampleDataTimer.start();
                    this.logToTextarea("Position sampling ON", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
                else {
                    this.useSampleTimer=false;
                    this.sampleDataTimer.stop();
                    this.logToTextarea("Position sampling OFF", JPannelLoggerLogElement.SEVERITY_DEBUG);
                }
            }
            else if ("ModuleON".equals(jtog.getActionCommand())){
                if (e.getStateChange() == ItemEvent.SELECTED){
                    this.turnOn();
                }
                else {
                    this.turnOff();
                }
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

    public JPanelLocalizationMain getMainPanel() {
        return mainPanel;
    }

    public void setMainPanel(JPanelLocalizationMain mainPanel) {
        this.mainPanel = mainPanel;
    }

    public double getSmoothingAlpha() {
        return smoothingAlpha;
    }

    public void setSmoothingAlpha(double smoothingAlpha) {
        this.smoothingAlpha = smoothingAlpha;
    }

    public int getHeuristicId() {
        return heuristicId;
    }

    public void setHeuristicId(int heuristicId) {
        this.heuristicId = heuristicId;
    }

    public double getHeuristic_exponent() {
        return heuristic_exponent;
    }

    public void setHeuristic_exponent(double heuristic_exponent) {
        this.heuristic_exponent = heuristic_exponent;
    }
    
    
    
    
}
