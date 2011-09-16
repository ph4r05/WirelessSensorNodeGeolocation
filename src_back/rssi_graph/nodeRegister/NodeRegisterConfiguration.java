/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.nodeRegister;

import java.util.List;

/**
 * Node register configuration object used for serialization.
 * Serializing whole node register is not good choice since it can contains
 * event handlers and so on. We need to store/restore only some parameters such as
 * nodes, its parameters and some others.
 * 
 * This is only storage class intended to be serialized directly to XML.
 * NodeRegister or some other class unserializes it and loads configuration to
 * currently initialized node register.
 * 
 * @author ph4r05
 */
public class NodeRegisterConfiguration {
    /**
     * comment for xml file
     */
    private String comment;
    
    /**
     * List of all nodes
     */
    private List<GenericNode> nodes;

    public NodeRegisterConfiguration() {
    }

    public NodeRegisterConfiguration(List<GenericNode> nodes) {
        this.nodes = nodes;
    }

    public NodeRegisterConfiguration(String comment, List<GenericNode> nodes) {
        this.comment = comment;
        this.nodes = nodes;
    }
    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<GenericNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GenericNode> nodes) {
        this.nodes = nodes;
    }
}
