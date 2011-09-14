/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import java.util.HashMap;
import java.util.Map;
import rssi_graph.localization.CoordinateRecord;
import rssi_graph.rssi.RSSI2DistFunctionInterface;
import rssi_graph.rssi.RSSI2DistLogNormalShadowing;

/**
 * Node register holds information about every node in network. Central storage for
 * general purpose information. 
 * @author ph4r05
 */
public class SimpleGenericNode implements GenericNode {
    
    // is this anchor node?
    private boolean anchor;

    // position if known, NULL otherwise
    private CoordinateRecord position=null;

    // RSSI2distance function
    // only for maximal TX power
    // since its data are very similar
    private RSSI2DistFunctionInterface distanceFunction=null;

    // signal properties
    private double rx_attenuation=0;
    private double tx_attenuation=0;

    // optionally flags

    // is reporting node?
    private boolean reporting=true;

    // reporting protocol(tiny, medium, mass)
    private int reportProtocol=0;

    // times
    private long firstSeen=0;
    private long lastSeen=0;

    // node identification
    private int nodeId;

    // parent in network topology
    private int parentId;

    // textual info
    private String description;
    private String location;

    // counters&synchronization
    private int lastMyCounter=0;
    private int lastItsCounter=0;
    private double temperature = Integer.MIN_VALUE;

    /**
     * Node platform
     */
    protected NodePlatform platform;

    /**
     * Extension for mobile nodes
     */
    protected MobileNode mobileExtension=null;

    /**
     * Extensions
     */
    protected Map<String, Object> extensions;
    
    /**
     * Normalizing rssi constant to level of calibrated system
     */
    protected double rssiNormalizingConstant=0;

    public SimpleGenericNode() {
        initParameters();
    }

    public SimpleGenericNode(int nodeId) {
        this.nodeId = nodeId;
        initParameters();
    }

    public SimpleGenericNode(boolean anchor, int nodeId) {
        this.anchor = anchor;
        this.nodeId = nodeId;
        initParameters();
    }

    public SimpleGenericNode(boolean anchor, int nodeId, int parentId) {
        this.anchor = anchor;
        this.nodeId = nodeId;
        this.parentId = parentId;
        initParameters();
    }

    public SimpleGenericNode(boolean anchor, int nodeId, int parentId, String description) {
        this.anchor = anchor;
        this.nodeId = nodeId;
        this.parentId = parentId;
        this.description = description;
        initParameters();
    }

    public SimpleGenericNode(boolean anchor, int nodeId, int parentId, String description, String location) {
        this.anchor = anchor;
        this.nodeId = nodeId;
        this.parentId = parentId;
        this.description = description;
        this.location = location;
        initParameters();
    }

    protected final void initParameters(){
        this.distanceFunction = new RSSI2DistLogNormalShadowing();
        this.extensions = new HashMap<String, Object>();
        this.reporting=true;
        this.platform = new NodePlatformGeneric();
    }

    public boolean hasExtension(String ext){
        return false;
    }

    @Override
    public String toString() {
        return "SimpleGenericNode{" + "anchor=" + anchor + "reporting=" + reporting + "nodeId=" + nodeId + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleGenericNode other = (SimpleGenericNode) obj;
        if (this.nodeId != other.nodeId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.nodeId;
        return hash;
    }
    
    /**
     * prepare object for serialization
     */
    public void sleep(){
        // sleep
        
        // sleep mobile extension
        if (this.mobileExtension!=null)
            this.mobileExtension.sleep();
    }
    
    /**
     * prepare object recovered from serialization
     */
    public void wakeup(){
        // wakeup
        
        // wakeup extensions
        if (this.mobileExtension!=null)
            this.mobileExtension.wakeup();
    }

    public boolean isAnchor() {
        return anchor;
    }

    public void setAnchor(boolean anchor) {
        this.anchor = anchor;
    }

    public RSSI2DistFunctionInterface getDistanceFunction() {
        return distanceFunction;
    }

    public void setDistanceFunction(RSSI2DistFunctionInterface distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public CoordinateRecord getPosition() {
        return position;
    }

    public void setPosition(CoordinateRecord position) {
        this.position = position;
    }

    public double getRx_attenuation() {
        return rx_attenuation;
    }

    public void setRx_attenuation(double rx_attenuation) {
        this.rx_attenuation = rx_attenuation;
    }

    public double getTx_attenuation() {
        return tx_attenuation;
    }

    public void setTx_attenuation(double tx_attenuation) {
        this.tx_attenuation = tx_attenuation;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(long firstSeen) {
        this.firstSeen = firstSeen;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getReportProtocol() {
        return reportProtocol;
    }

    public void setReportProtocol(int reportProtocol) {
        this.reportProtocol = reportProtocol;
    }

    public boolean isReporting() {
        return reporting;
    }

    public void setReporting(boolean reporting) {
        this.reporting = reporting;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getLastItsCounter() {
        return lastItsCounter;
    }

    public void setLastItsCounter(int lastItsCounter) {
        this.lastItsCounter = lastItsCounter;
    }

    public int getLastMyCounter() {
        return lastMyCounter;
    }

    public void setLastMyCounter(int lastMyCounter) {
        this.lastMyCounter = lastMyCounter;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    public MobileNode getMobileExtension() {
        return mobileExtension;
    }

    public void setMobileExtension(MobileNode mobileExtension) {
        this.mobileExtension = mobileExtension;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double  temperature) {
        this.temperature = temperature;
    }

    public NodePlatform getPlatform() {
        return platform;
    }

    public void setPlatform(NodePlatform platform) {
        this.platform = platform;
    }

    public double getRssiNormalizingConstant() {
        return rssiNormalizingConstant;
    }

    public void setRssiNormalizingConstant(double rssiNormalizingConstant) {
        this.rssiNormalizingConstant = rssiNormalizingConstant;
    }

    /**
     * Return normalized RSSI according to current system calibration
     * @param rssi
     * @return 
     */
    public double getNormalizedRssi(double rssi, int txlevel) {
        return rssi + this.getRssiNormalizingConstant();
    }
    
    

    
}
