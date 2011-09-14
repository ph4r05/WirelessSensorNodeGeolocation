/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.rssi;

import rssi_graph.localization.CoordinateRecord;

/**
 * Simple data object for passing data between data loader and 
 * LoadData dialog.
 * 
 * @author ph4r05
 */
public class DistanceFromPositionData {
    public int mid;
    public int mobileNodeId;
    public int staticNodeId;
    public String annotation;
    
    // computation results
    public double distance;
    public CoordinateRecord mobilePosition;
}
