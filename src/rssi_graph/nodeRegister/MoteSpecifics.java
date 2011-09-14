/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

/**
 * Defines mote specific values
 * 
 * @author ph4r05
 */
public class MoteSpecifics {

      /**
       * tx output power level
       */
      public static final int[] signalLevel = {31,27,23,19,15,11,7,3};
      
      /**
       * Corresponding power levels to signalLevel;
       * Power level at TX power 31 = powerLevel[0], 31 = signalLevel[0];
       */
      public static final int[] powerLevel = {0, -1, -3, -5, -7, -10, -15, -25};

      /**
       * Tunable tx rx channel
       */
      public static final int[] channels = {11,12,13,14,15,16,17,18,19,20,21,23,24,25,26};

      /**
       * Lookup txpower and return corresponding output power in dBm
       * @param txpower
       * @return
       */
      public static int getPowerLevel(int txpower){
          int result=0;
            switch(txpower){
                case 31: result=powerLevel[0]; break;
                case 27: result=powerLevel[1]; break;
                case 23: result=powerLevel[2]; break;
                case 19: result=powerLevel[3]; break;
                case 15: result=powerLevel[4]; break;
                case 11: result=powerLevel[5]; break;
                case 7: result=powerLevel[6]; break;
                case 3: result=powerLevel[7]; break;
            }

            return result;
      }

      /**
       * Convert RSSI to output power in dBm
       * @param rssi
       * @return
       */
      public double getDbmFromRSSI(double rssi){
          return rssi;
      }

      public static int[] getChannels() {
          return channels;
      }

      public static int[] getPowerLevel() {
          return powerLevel;
      }

      public static int[] getSignalLevel() {
          return signalLevel;
      }

      public String getId(){
          return "generic";
      }
}
