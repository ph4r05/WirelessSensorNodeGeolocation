/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.rssi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rssi_graph.localization.CoordinateRecord;

/**
 * Holder for offline localization settings
 * @author ph4r05
 */
public class OfflineLocalizationSettings {
    public static final int RSSI_SOURCE_MEAN=1;
    public static final int RSSI_SOURCE_MEDIAN=2;
    
    /**
     * Mobile node ID
     */
    public int mobileNodeId;
    
    /**
     * Mid->real position map
     */
    public Map<Integer,CoordinateRecord> realPositions;
    
    /**
     * Rssi data for localization
     */
    public Map<String, ArrayList<RSSI2DistInternalBuffer>> rssiData;
    
    /**
     * Rssi source for localization
     */
    public int rssiSource;
    
    /**
     * Set of nodes to localize
     */
    public Set<Integer> midToLocalize;
    
    /**
     * If true then plot everything to network map
     */
    public boolean showInNetworkMap=true;
    
    /**
     * Step for generating histogram
     */
    public double histogramStep;

    public OfflineLocalizationSettings() {
        ;
    }

    public OfflineLocalizationSettings(int mobileNodeId, Map<Integer, CoordinateRecord> realPositions, Map<String, ArrayList<RSSI2DistInternalBuffer>> rssiData, int rssiSource) {
        this.mobileNodeId = mobileNodeId;
        this.realPositions = realPositions;
        this.rssiData = rssiData;
        this.rssiSource = rssiSource;
    }

    public int getMobileNodeId() {
        return mobileNodeId;
    }

    public void setMobileNodeId(int mobileNodeId) {
        this.mobileNodeId = mobileNodeId;
    }

    public Map<Integer, CoordinateRecord> getRealPositions() {
        return realPositions;
    }

    public void setRealPositions(Map<Integer, CoordinateRecord> realPositions) {
        this.realPositions = realPositions;
    }

    public Map<String, ArrayList<RSSI2DistInternalBuffer>> getRssiData() {
        return rssiData;
    }

    public void setRssiData(Map<String, ArrayList<RSSI2DistInternalBuffer>> rssiData) {
        this.rssiData = rssiData;
    }
    
    public int getRssiSource() {
        return rssiSource;
    }

    public void setRssiSource(int rssiSource) {
        this.rssiSource = rssiSource;
    }
    
    public Set<Integer> getMidToLocalize(){
	return this.midToLocalize;
    }
    
    public void setMidToLocalize(Set<Integer> set){
	this.midToLocalize=set;
    }

    public boolean isShowInNetworkMap() {
        return showInNetworkMap;
    }

    public void setShowInNetworkMap(boolean showInNetworkMap) {
        this.showInNetworkMap = showInNetworkMap;
    }

    public double getHistogramStep() {
        return histogramStep;
    }

    public void setHistogramStep(double histogramStep) {
        this.histogramStep = histogramStep;
    }
}
