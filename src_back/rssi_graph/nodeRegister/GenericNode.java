/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import java.io.Serializable;
import rssi_localization.localization.CoordinateRecord;
import rssi_graph.rssi.RSSI2DistFunctionInterface;

/**
 *
 * @author ph4r05
 */
public interface GenericNode extends Serializable, Cloneable{
    public int getNodeId();

    public RSSI2DistFunctionInterface getDistanceFunction();
    public void setDistanceFunction(RSSI2DistFunctionInterface distanceFunction);

    public CoordinateRecord getPosition();
    public void setPosition(CoordinateRecord position);

    public boolean isAnchor();
    public void setAnchor(boolean anchor);

    public long getFirstSeen();
    public void setFirstSeen(long firstSeen);

    public long getLastSeen();
    public void setLastSeen(long lastSeen);

    public MobileNode getMobileExtension();
    public void setMobileExtension(MobileNode mobileExtension);

    public double getTemperature();
    public void setTemperature(double  temperature);

    public NodePlatform getPlatform();
    public void setPlatform(NodePlatform platform);
    
    /**
     * Return normalized rssi value for system
     */
    public double getNormalizedRssi(double rssi, int txlevel);
    
    // serialization
    public void sleep();
    public void wakeup();
}
