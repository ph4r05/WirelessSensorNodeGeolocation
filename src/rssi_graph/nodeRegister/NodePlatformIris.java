/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

/**
 *
 * @author ph4r05
 */
public class NodePlatformIris extends NodePlatformGeneric {
 /**
    * tx output power level
    */
    public static final int[] signalLevel = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};

   /**
    * Corresponding power levels to signalLevel;
    * Power level at TX power 31 = powerLevel[0], 31 = signalLevel[0];
    */
    public static final double[] powerLevel = {+3.0,+2.6,+2.1,+1.6,+1.1,+0.5,-0.2,
                                -1.2,-2.2,-3.2,-4.2,-5.2,-7.2,-9.2,-12.2,-17.2};

   /**
    * Tunable tx-rx channel
    */
    public static final int[] channels = {11,12,13,14,15,16,17,18,19,20,21,23,24,25,26};

    @Override
    public String getPlatform() {
        return "IRIS";
    }

    /**
     * Platform numeric ID.
     * Must correspond to platform ID defined in tinyOS program in reporting motes.
     * @return
     */
    @Override
    public int getPlatformId() {
        return 2;
    }

    @Override
    public int[] getTxLevels() {
        return NodePlatformIris.signalLevel;
    }

    @Override
    public double[] getTxOutputPower() {
        return NodePlatformIris.powerLevel;
    }


    
}
