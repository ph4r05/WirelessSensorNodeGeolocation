/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

/**
 *
 * @author ph4r05
 */
public class NodePlatformGeneric implements NodePlatform {
    public String getPlatform() {
        return "Unknown";
    }

    public int getPlatformId() {
        return 0;
    }

    public int[] getTxLevels() {
        return new int[0];
    }

    public double[] getTxOutputPower() {
        return new double[0];
    }

    public NodePlatformGeneric() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if (!(obj instanceof NodePlatform)) return false;
        
        final NodePlatform platform = (NodePlatform) obj;
        return this.getPlatformId() == platform.getPlatformId();
    }

    @Override
    public int hashCode() {
        return this.getPlatformId();
    }
}
