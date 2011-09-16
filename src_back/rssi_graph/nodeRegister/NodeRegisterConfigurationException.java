/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.nodeRegister;

/**
 * Node configuration exception for configuration handling (save, restore, read, write)
 * @author ph4r05
 */
public class NodeRegisterConfigurationException extends Exception {

    public NodeRegisterConfigurationException(Throwable cause) {
        super(cause);
    }

    public NodeRegisterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeRegisterConfigurationException(String message) {
        super(message);
    }

    public NodeRegisterConfigurationException() {
    }
    
}
