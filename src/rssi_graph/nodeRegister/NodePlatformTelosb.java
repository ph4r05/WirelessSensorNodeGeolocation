/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

/**
 *
 * @author ph4r05
 */
public class NodePlatformTelosb extends NodePlatformGeneric{

   /**
    * tx output power level
    */
    public static final int[] signalLevel = {31,27,23,19,15,11,7,3};

   /**
    * Corresponding power levels to signalLevel;
    * Power level at TX power 31 = powerLevel[0], 31 = signalLevel[0];
    */
    public static final double[] powerLevel = {0., -1., -3., -5., -7., -10., -15., -25.};

   /**
    * Tunable tx-rx channel
    */
    public static final int[] channels = {11,12,13,14,15,16,17,18,19,20,21,23,24,25,26};

    @Override
    public String getPlatform() {
        return "TelosB";
    }

    /**
     * Platform numeric ID.
     * Must correspond to platform ID defined in tinyOS program in reporting motes.
     * @return
     */
    @Override
    public int getPlatformId() {
        return 1;
    }

    @Override
    public int[] getTxLevels() {
        return NodePlatformTelosb.signalLevel;
    }

    @Override
    public double[] getTxOutputPower() {
        return NodePlatformTelosb.powerLevel;
    }
}
