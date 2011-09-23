/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import java.io.Serializable;
import rssi_graph.messages.MultiPingResponseMsg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import rssi_graph.RSSIInputQueue;
import rssi_graph.RSSIResponseRecord;
import rssi_graph.localization.CoordinateRecord;
import rssi_graph.utils.DataSmoother;

/**
 * Each mobile node has own instance of this
 *
 * @author ph4r05
 */
public class MobileNode implements Serializable, Cloneable {
    /**
     * mobile node address/unique identifier
     */
    private int mobile_nodeID;

    /**
     * RSSI input queues for incoming data
     * for moving mean calculations, and another windowed algorithm is needed to
     * hold some window of k elements in memory directly
     *
     * it is easy to hold it here, all data corresponding to this mobile node in single
     * mobileNode instance, indexed by reporting node in following map.
     * 
     * Do not serialize
     */
    @XStreamOmitField
    private Map<Integer, RSSIInputQueue> squeue=null;

    /**
     * Easier mean calculation algorithm (smoothing)
     * We need to remember only last value from corresponding anchor
     *
     * future implementation: encapsulate localization to module. pass new data via
     * generalized interface, allow keying by time to support history and so on
     */
    private Map<Integer, Double> floatingMeanRssi=null;


    /**
     * Computed distances form anchor nodes.
     * Here should be localization model to encapsulate localization implementation
     * to provide more generic interface and to allow easy substitution of localization
     * models. Each such model should have own displayer drawing custom data to 
     * network map via generic interface(probability map for example)
     * 
     * Later allow to trace trajectory of node by remembering distances with respect to the time for
     * last k times. Localization model could do it easily.
     *
     * Now, use simple KISS implementation of wanted functionality.
     */
    private Map<Integer, Double> distancesFromAnchors=null;
    
    /**
     * Last computed position
     * 
     * Future implementation: encapsulate to localization model to hide implementation 
     * details and allow further extension such as history
     */
    private CoordinateRecord computedPosition=null;

    /**
     * Freshness indicator
     * The greater number the fresher is this mobile node.
     * Fresh = is active = we receive some packets concerning this mobile node
     * in recent history.
     */
    private int reportFreshness;
    public static final int MAX_FRESHNESS=1000;

    /**
     * Maximal number of counter inserted to queues
     */
    private int maxCounter=0;

    /**
     * Max number of delay between maxCounter and last counter element in queue
     * to consider queue inactive
     */
    public static final int maxCounterActivityTimeout=500;

    
    private int lastLocalizationCounter=0;

    /**
     * RSSI input queue
     * do not serialize
     */
    @XStreamOmitField
    private RSSIInputQueue rssiInputQueue = null;

    /**
     * RMS after position estimation
     */
    private double positionEstimationRMS=0;
    
    /**
     * Error in position estimation (distance)
     */
    private double positionEstimationError=0;
    
    /**
     * Error in position estimation (x coord)
     */
    private double positionEstimationErrorX=0;
    
    /**
     * Error in position estimation (y coord)
     */
    private double positionEstimationErrorY=0;
    
    /**
     * Reference to created generic node
     */
    protected GenericNode genericNode=null;

    public MobileNode(int mobile_nodeID) {
        this.mobile_nodeID = mobile_nodeID;

        // init main input queue
        rssiInputQueue = new RSSIInputQueue();

        // init map of squeues for messages from static nodes
        squeue = new HashMap<Integer, RSSIInputQueue>(16);

        // init floating mean database
        floatingMeanRssi = new HashMap<Integer, Double>(4);

        // init distance map
        this.distancesFromAnchors = new HashMap<Integer, Double>();
    }











    public Map<Integer, Double> getFloatingMean(){
        return this.floatingMeanRssi;
    }


    public double getFloatingMean(int source){
        if (!floatingMeanRssi.containsKey(Integer.valueOf(source))) return -100;
        return floatingMeanRssi.get(Integer.valueOf(source));
    }

    public double addToFloatingMean(int source, int rssi, double alpha){
        Integer iSource = Integer.valueOf(source);
        Double curRssi = floatingMeanRssi.containsKey(iSource) ?
                                floatingMeanRssi.get(iSource) :
                                Double.valueOf((double)rssi);

        curRssi = DataSmoother.getSmoothed(curRssi, rssi, alpha);
        floatingMeanRssi.put(iSource, curRssi);

        return curRssi;
    }

    public double addToFloatingMean(int source, int rssi){
        return addToFloatingMean(source, rssi, 0.05);
    }

    public int getReportFreshness(){
        return reportFreshness;
    }

    public void decReportFreshness(int q){
        this.reportFreshness -= q;
        if (this.reportFreshness < 0) this.reportFreshness = 0;
    }

    public void decReportFreshness(){
        this.decReportFreshness(1);
    }

    public void incReportFreshness(int q){
        this.reportFreshness += q;
        if (this.reportFreshness > MobileNode.MAX_FRESHNESS) this.reportFreshness = MobileNode.MAX_FRESHNESS;
    }

    public void incReportFreshness(){
        this.incReportFreshness(1);
    }








    
    public int getNodeId() {
        return this.getMobile_nodeID();
    }

    



















    public int getMobile_nodeID() {
        return mobile_nodeID;
    }

    public RSSIInputQueue getRssiInputQueue() {
        return rssiInputQueue;
    }

    public boolean inputQueueCanInsert(){
        return rssiInputQueue.canInsert();
    }

    /**
     * creates squeue if does not exists
     * @param staticNodeId
     */
    public RSSIInputQueue getSqueue(int staticNodeId, boolean create){
        if (squeue.containsKey(new Integer(staticNodeId))){
            return squeue.get(new Integer(staticNodeId));
        }
        else if (create==true) {
            RSSIInputQueue riq = new RSSIInputQueue();
            squeue.put(new Integer(staticNodeId), riq);
            return riq;
        }
        else throw new RuntimeException("Cannot find given squeue");
    }

    public boolean inputQueueAdd(RSSIResponseRecord r){
        // insert new element to queue
        rssiInputQueue.add(r);
        rssiInputQueue.setActive(true);

        // manage max counter attribute to determine active queues
        maxCounter = r.getCounter();

        // add record to wanted queue
        RSSIInputQueue riq = getSqueue(r.getNodeID_report_from(), true);
        riq.add(r);

        // set activity to true
        riq.setActive(true);

        // get capacity of queue
        return true;
    }

    /**
     * Helper method to determine whether counter timeouted from maxCounter by
     * maxCounterActivityTimeout samples
     * @param counter
     * @return
     */
    public boolean isCounterTimeouted(int counter){
        // normal way of timeouting. 
        if (counter + maxCounterActivityTimeout > maxCounter) return false;

        // modulo timeout in case counter has been overflown
        // 1 << MultiPingResponseMsg.sizeBits_counter() = 2^MultiPingResponseMsg.sizeBits_counter()
        if (((counter + maxCounterActivityTimeout) % (1 << MultiPingResponseMsg.sizeBits_counter())) > maxCounter) return false;

        return true;
    }

    /*
     * Return distance from counters under 
     * Assumption:
     * counterRef < counterNew
     *
     * @param counterRef
     * @param counterNew
     * @return
     */
    public int getCounterDistance(int counterRef, int counterNew){
        return (counterNew-counterRef) % (1 << MultiPingResponseMsg.sizeBits_counter());
    }

    /**
     * gets active input queues
     * @return
     */
    public ArrayList<RSSIInputQueue> getActiveQueues(){
        // perform activity search
        ArrayList<RSSIInputQueue> inputQueues = new ArrayList<RSSIInputQueue>(4);

        // key iterator
        Iterator<Integer> it = squeue.keySet().iterator();

        // iterate over my queues
        while (it.hasNext()){
            Integer riq_id = it.next();
            RSSIInputQueue riq = squeue.get(riq_id);

            // activity test on true nodes
            // if active, try timeout test on it.
            // Count active queues to allocate array
            if (riq.isActive()){
                int tmp_counter = riq.getLastCounter();
                if (isCounterTimeouted(tmp_counter)){
                    riq.setActive(false);
                }
                else {
                    inputQueues.add(riq);
                }
            }
        }

        return inputQueues;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MobileNode other = (MobileNode) obj;
        if (this.mobile_nodeID != other.mobile_nodeID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.mobile_nodeID;
        return hash;
    }

    public int getLastLocalizationCounter() {
        return lastLocalizationCounter;
    }

    public void setLastLocalizationCounter(int lastLocalizationCounter) {
        this.lastLocalizationCounter = lastLocalizationCounter;
    }

    public int getMaxCounter() {
        return maxCounter;
    }

    public void setMaxCounter(int maxCounter) {
        this.maxCounter = maxCounter;
    }

    public Map<Integer, Double> getDistancesFromAnchors() {
        return distancesFromAnchors;
    }

    public void setDistancesFromAnchors(Map<Integer, Double> distancesFromAnchors) {
        this.distancesFromAnchors = distancesFromAnchors;
    }

    public Map<Integer, Double> getFloatingMeanRssi() {
        return floatingMeanRssi;
    }

    public void setFloatingMeanRssi(Map<Integer, Double> floatingMeanRssi) {
        this.floatingMeanRssi = floatingMeanRssi;
    }

    /**
     * Set particular RSSI value for particular node
     *
     * @param node
     * @param rssi
     */
    public void setFloatingMeanRssi(int node, double rssi){
        if (this.floatingMeanRssi==null){
            this.floatingMeanRssi = new HashMap<Integer, Double>();
        }

        this.floatingMeanRssi.put(Integer.valueOf(node), Double.valueOf(rssi));
    }

    /**
     * Object cloning. Full copy
     * 
     * @return
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            // first make shallow copy
            Object obj = super.clone();
            if (obj==null || !(obj instanceof MobileNode)){
                    throw new CloneNotSupportedException("Shallowcopy create failed");
            }
            
            MobileNode mn = (MobileNode) obj;
            
            // deep copy
            mn.computedPosition = new CoordinateRecord(
                    this.getComputedPosition().getX(),
                    this.getComputedPosition().getY(), 
                    this.getComputedPosition().getZ());
            
            // distance from anchors            
            mn.distancesFromAnchors=new HashMap<Integer, Double>();
            Iterator<Integer> iterator = this.distancesFromAnchors.keySet().iterator();
            while(iterator.hasNext()){
                Integer key = iterator.next();
                Double value = this.distancesFromAnchors.get(key);
                mn.distancesFromAnchors.put(key, value);
            }
            
            // floating mean
            mn.floatingMeanRssi=new HashMap<Integer, Double>();
            iterator = this.floatingMeanRssi.keySet().iterator();
            while(iterator.hasNext()){
                Integer key = iterator.next();
                Double value = this.floatingMeanRssi.get(key);
                mn.floatingMeanRssi.put(key, value);
            }
            
            //mn.squeue;
            
            return mn;
        } catch(Exception e){
            throw new CloneNotSupportedException(e.getMessage());
        }
    }
    
    /**
     * prepare object for serialization.
     * Should be used only on cloned
     */
    public void sleep(){
        this.rssiInputQueue = null;
        this.squeue = null;
    }
    
    /**
     * prepare object recovered from serialization
     */
    public void wakeup(){
        this.rssiInputQueue = new RSSIInputQueue();
        this.squeue = new HashMap<Integer, RSSIInputQueue>();
    }
    
    public Map<Integer, RSSIInputQueue> getSqueue() {
        return squeue;
    }

    public void setSqueue(Map<Integer, RSSIInputQueue> squeue) {
        this.squeue = squeue;
    }

    public CoordinateRecord getComputedPosition() {
        return computedPosition;
    }

    public void setComputedPosition(CoordinateRecord computedPosition) {
        this.computedPosition = computedPosition;
    }

    public GenericNode getGenericNode() {
        return genericNode;
    }

    public void setGenericNode(GenericNode genericNode) {
        this.genericNode = genericNode;
    }

    public double getPositionEstimationError() {
        return positionEstimationError;
    }

    public void setPositionEstimationError(double positionEstimationError) {
        this.positionEstimationError = positionEstimationError;
    }

    public double getPositionEstimationErrorX() {
        return positionEstimationErrorX;
    }

    public void setPositionEstimationErrorX(double positionEstimationErrorX) {
        this.positionEstimationErrorX = positionEstimationErrorX;
    }

    public double getPositionEstimationErrorY() {
        return positionEstimationErrorY;
    }

    public void setPositionEstimationErrorY(double positionEstimationErrorY) {
        this.positionEstimationErrorY = positionEstimationErrorY;
    }

    public double getPositionEstimationRMS() {
        return positionEstimationRMS;
    }

    public void setPositionEstimationRMS(double positionEstimationRMS) {
        this.positionEstimationRMS = positionEstimationRMS;
    }
    
    
}
