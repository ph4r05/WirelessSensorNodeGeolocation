/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

/**
 *
 * @author ph4r05
 */
public class NodePlatformFactory {
    public static NodePlatform getPlatform(int i){
        if (i == 1){
            return new NodePlatformTelosb();
        } else if (i == 2){
            return new NodePlatformIris();
        } else {
            return new NodePlatformGeneric();
        }
    }
}
