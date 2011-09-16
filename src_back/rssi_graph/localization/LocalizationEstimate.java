/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_localization.localization;

import java.util.Map;
import rssi_graph.utils.TextHistogramData;

/**
 * Store info about localization estimate.
 * 
 * @author ph4r05
 */
public class LocalizationEstimate implements TextHistogramData{
    private int mobileNodeId;
    private CoordinateRecord realPosition;
    private CoordinateRecord estimatedPosition;
    private Map<Integer, Double> distancesFromAnchors;
    private Map<Integer, Double> anchorWeights;
    private double rms;    
    private long time;
    
    
    
    public double getDError(){
        return LocalizationEstimate.getDError(realPosition, estimatedPosition);
    }
    
    public double getXError(){
        return LocalizationEstimate.getXError(realPosition, estimatedPosition);
    }
    
    public double getYError(){
        return LocalizationEstimate.getYError(realPosition, estimatedPosition);
    }

    public Map<Integer, Double> getAnchorWeights() {
        return anchorWeights;
    }

    public void setAnchorWeights(Map<Integer, Double> anchorWeights) {
        this.anchorWeights = anchorWeights;
    }

    public Map<Integer, Double> getDistancesFromAnchors() {
        return distancesFromAnchors;
    }

    public void setDistancesFromAnchors(Map<Integer, Double> distancesFromAnchors) {
        this.distancesFromAnchors = distancesFromAnchors;
    }

    public CoordinateRecord getEstimatedPosition() {
        return estimatedPosition;
    }

    public void setEstimatedPosition(CoordinateRecord estimatedPosition) {
        this.estimatedPosition = estimatedPosition;
    }

    public int getMobileNodeId() {
        return mobileNodeId;
    }

    public void setMobileNodeId(int mobileNodeId) {
        this.mobileNodeId = mobileNodeId;
    }

    public CoordinateRecord getRealPosition() {
        return realPosition;
    }

    public void setRealPosition(CoordinateRecord realPosition) {
        this.realPosition = realPosition;
    }

    public double getRms() {
        return rms;
    }

    public void setRms(double rms) {
        this.rms = rms;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public static double getDError(CoordinateRecord realPosition, CoordinateRecord estimatedPosition){
        if (realPosition==null || estimatedPosition==null) return 0.0;
        double xerror = LocalizationEstimate.getXError(realPosition, estimatedPosition);
        double yerror = LocalizationEstimate.getYError(realPosition, estimatedPosition);
        return Math.sqrt(xerror*xerror+yerror*yerror);
    }
    
    public static double getXError(CoordinateRecord realPosition, CoordinateRecord estimatedPosition){
        if (realPosition==null || estimatedPosition==null) return 0.0;
        return Math.abs(realPosition.getX() - estimatedPosition.getX());
    }
    
    public static double getYError(CoordinateRecord realPosition, CoordinateRecord estimatedPosition){
        if (realPosition==null || estimatedPosition==null) return 0.0;
        return Math.abs(realPosition.getY() - estimatedPosition.getY());
    }

    public double getHistogramData(String identification) {
        return this.getDError();
    }

    public String toStringForHistogram() {
        if (this.getRealPosition()==null) return null;
        return "["+this.getRealPosition().getX()+";"+this.getRealPosition().getY()+"]";
    }
}
