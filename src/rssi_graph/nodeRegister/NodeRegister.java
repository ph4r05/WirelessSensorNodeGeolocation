/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rssi_graph.MessageTypes;
import rssi_graph.WorkerBase;

/**
 * Base register of all nodes.
 * Stores all generic information about each node.
 * 
 * @author ph4r05
 */
public class NodeRegister extends WorkerBase implements Serializable{
    /**
     * nodeID -> node object
     * all nodes in network
     */
    private Map<Integer, GenericNode> nodes = null;
    
    /**
     * Base station node
     */
    private GenericNode baseStation=null;

    /**
     * Listeners to data change events
     */
    private List<NodeRegisterEventListener> dataChangeListeners = null;

    /**
     * Mobile node manager
     */
    private MobileNodeManager mnm=null;

    public NodeRegister() {
        this.nodes = new HashMap<Integer, GenericNode>();
        this.dataChangeListeners = new ArrayList<NodeRegisterEventListener>();
    }

    /**
     * Retrieve node from database
     * @param nodeId
     * @return
     */
    public GenericNode getNode(int nodeId){
        GenericNode resultNode = null;
        Integer iNodeId = Integer.valueOf(nodeId);

        if (!this.nodes.containsKey(iNodeId)) return null;
        resultNode = this.nodes.get(iNodeId);

        return resultNode;
    }

    public Set<Integer> getNodesSet(){
        if (this.nodes==null) return new HashSet<Integer>();
        return this.nodes.keySet();
    }

    public boolean existsNode(int nodeId){
        Integer iNodeId = Integer.valueOf(nodeId);
        return this.nodes.containsKey(iNodeId);
    }

    /**
     * Adds given node to node register map.
     * @param a 
     */
    public void addNode(GenericNode a){
        if (this.nodes==null || a==null || !(a instanceof GenericNode)) return;
        this.nodes.put(a.getNodeId(), a);
    }

    public void addChangeListener(NodeRegisterEventListener listener){
        if (listener==null){
            throw new NullPointerException("Null change event listener not permited");
        }

        this.dataChangeListeners.add(listener);
    }

    public void removeChangeListener(NodeRegisterEventListener listener){
        if (listener==null){
            throw new NullPointerException("Null change event listener not permited");
        }

        Iterator<NodeRegisterEventListener> it = this.dataChangeListeners.iterator();
        while(it.hasNext()){
            NodeRegisterEventListener curListener = it.next();
            if (listener.equals(curListener)){
                this.dataChangeListeners.remove(curListener);
                return;
            }
        }
    }

    /**
     * perform data changed notification to all registered listeners
     * @param changes
     */
    public void changeNotify(Map<Integer, String> changes){
        if (this.dataChangeListeners.isEmpty()) return;
        
        // create new event
        NodeRegisterEvent evt = new NodeRegisterEvent();
        evt.setEventType(NodeRegisterEvent.EVENT_TYPE_DATA_CHANGED);
        evt.setChanges(changes);

        Iterator<NodeRegisterEventListener> it = this.dataChangeListeners.iterator();
        while(it.hasNext()){
            NodeRegisterEventListener cur = it.next();
            cur.accept(evt);
        }
    }

    /**
     * Disvocery pong with default platform identifivation. 
     * 
     * @param from
     * @param identification
     * @return 
     */
    public boolean discoveryPong(int from, int identification){
        NodePlatform defNode = new NodePlatformTelosb();
        return this.discoveryPong(from, identification, defNode.getPlatformId());
    }

    /**
     * Response on command IDENTIFY message of nodes.
     * If replied existing node, timers are incremented to signalize node is alive,
     * if replied new node it is added to node register and change event is fired.
     * 
     * Nodes can specify its ID and platform ID (telosb, iris, ...).
     * 
     * @param from
     * @return true if node is new
     */
    public boolean discoveryPong(int from, int identification, int platform){
        // synchronize over node list to avoid collisions
        synchronized(this.nodes){
            if (this.existsNode(from)){
                // node is not new, node exists
                // update counters only
                GenericNode curNode = this.getNode(from);
                if (curNode==null) throw new NullPointerException("Registered node is null");

                // update last seen counter
                curNode.setLastSeen(System.currentTimeMillis());

                // update platform if changed
                if (curNode.getPlatform() == null ||
                        curNode.getPlatform().getPlatformId() != platform){
                    curNode.setPlatform(NodePlatformFactory.getPlatform(platform));
                }

                return false;
            }
            else {
                // node is new, create record, notify listeners
                // determine type of node
                GenericNode newNode = new SimpleGenericNode(true, from, 1);

                // set node platform
                newNode.setPlatform(NodePlatformFactory.getPlatform(platform));

                if (identification==MessageTypes.NODE_DYNAMIC){
                    // create mobile node instance
                    MobileNode newMobileNode = new MobileNode(from);
                    newMobileNode.setGenericNode(newNode);

                    // tie mobile node and generic node together
                    newNode.setMobileExtension(newMobileNode);
                    newNode.setAnchor(false);

                    this.addNode(newNode);
                    if (mnm!=null){
                        mnm.addMobileNode(newMobileNode);
                    }
                } else {
                    newNode.setAnchor(true);
                    this.addNode(newNode);
                }

                // notify about new node
                Map<Integer, String> changes = new HashMap<Integer, String>();
                changes.put(Integer.valueOf(from), null);
                this.changeNotify(changes);
                
                return true;
            }
        }
    }

    public GenericNode getBaseStation() {
        return baseStation;
    }

    public void setBaseStation(GenericNode baseStation) {
        this.baseStation = baseStation;
    }

    public MobileNodeManager getMnm() {
        return mnm;
    }

    public void setMnm(MobileNodeManager mnm) {
        this.mnm = mnm;
    }

    /**
     * Delete specified node from node register
     *
     * @param node
     */
    public synchronized boolean removeNode(GenericNode node){
        // check existence
        if (node==null || this.existsNode(node.getNodeId())==false){
            return false;
        }
        // need to delete all specific extensions
        if (this.mnm.existsMobileNode(node.getNodeId())){
            this.mnm.removeMobileNode(node.getNodeId());
        }

        this.nodes.remove(Integer.valueOf(node.getNodeId()));

        // notify
        this.changeNotify(null);
        return true;
    }

    /**
     * Delete specified node from node register
     *
     * @param nodeId
     */
    public synchronized boolean removeNode(int nodeId){
        // check existence
       if (this.existsNode(nodeId)==false){
           return false;
       }

       return this.removeNode(this.getNode(nodeId));
    }

    /**
     * Creates basic config object representing current node register state
     * intended for further serialization, de-serialization and restoring.
     * 
     * @param comment
     * @return 
     */
    public synchronized NodeRegisterConfiguration storeConfigToObject(String comment){
        // create configuration object
        NodeRegisterConfiguration config = new NodeRegisterConfiguration();
        config.setComment(comment);
        
        // new node list for config object
        List<GenericNode> nodeList = new LinkedList<GenericNode>();
        
        Iterator<Integer> iterator = this.nodes.keySet().iterator();
        while(iterator.hasNext()){
            Integer curNodeId = iterator.next();
            GenericNode curNode = this.nodes.get(curNodeId);
            
            // do not serialize null elements
            if (curNode==null) continue;
            
            // add to node list
            // prepare nodes for serialization somehow?
            // using xstrem converters...
            nodeList.add(curNode);
        }
        
        // set nodelist
        config.setNodes(nodeList);
        
        return config;
    }
    
    /**
     * Restores state from config object.
     * 1. all nodes are removed.
     * 2. node recovering: add node, if mobile node, add to mobile manager.
     * 
     * @param newConfig
     * @return 
     */
    public synchronized int restoreConfigFromObject(NodeRegisterConfiguration newConfig) throws NodeRegisterConfigurationException{
        // verify config
        if (newConfig==null || newConfig.getNodes()==null ){
            throw new NullPointerException("Cannot restore configuration from null object");
        }
        
        // check for base station (ID=1)
        boolean hasBS=false;
        Iterator<GenericNode> iterator = newConfig.getNodes().iterator();
        while(iterator.hasNext()){
            if (iterator.next().getNodeId()==1) {
                hasBS=true;
                break;
            }
        }
        
        if (hasBS==false){
            throw new NodeRegisterConfigurationException("No BS node in configuration, cannot restore such state.");
        }
        
        // clean mobile manager
        synchronized(this.mnm){
            this.mnm.resetState();
        }
        
        // flush all nodes in node register
        this.baseStation = null;
        this.nodes = new HashMap<Integer, GenericNode>();
        
        // notify listeners
        this.changeNotify(null);
        
        // iterate again, now add nodes
        iterator = newConfig.getNodes().iterator();
        while(iterator.hasNext()){
            GenericNode curNode = iterator.next();
            if (curNode==null) continue;
            
            // wakeup
            curNode.wakeup();
            
            // discovery pong
            this.addNode(curNode);
            
            if (curNode.getMobileExtension()!=null){
                MobileNode mobileExtension = curNode.getMobileExtension();
                if (this.mnm!=null){
                    this.mnm.addMobileNode(mobileExtension);
                }
            }
            
            // base station?
            if (curNode.getNodeId()==1){
                this.baseStation = curNode;
            }
        }
        
        // notify listeners
        this.changeNotify(null);
        return 0;
    }
}
