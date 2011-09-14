/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.nodeRegister;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Each mobile node has own instance of mobile node
 * Manager manages its own mobile nodes.
 *
 * @author ph4r05
 */
public class MobileNodeManager implements Serializable {
    /**
     * Mobile node pool
     */
    private Map<Integer, MobileNode> pool=null;

    /**
     * node register
     */
    private NodeRegister nodeRegister = null;

    public MobileNodeManager(NodeRegister nodeRegister) {
        this.nodeRegister = nodeRegister;

        // init map
        pool = new HashMap<Integer, MobileNode>(5);
    }

    /**
     * Increments freshness indicator for mobile node.
     * Freshness indicator indicates status of node (active, idle)
     * @param nodeid 
     */
    public void incReportFreshnessFor(int nodeid){
        Integer iNodeid = Integer.valueOf(nodeid);
        if (!pool.containsKey(iNodeid)) return;

        MobileNode tmpNode = this.pool.get(iNodeid);
        tmpNode.incReportFreshness(pool.size()+2);
    }

    /**
     * Decrements freshness indicator for particular mobile node
     * @param nodeid 
     */
    public void decReportFreshnessFor(int nodeid){
        Integer iNodeid = Integer.valueOf(nodeid);
        if (!pool.containsKey(iNodeid)) return;

        MobileNode tmpNode = this.pool.get(iNodeid);
        tmpNode.decReportFreshness(1);
    }

    /**
     * Global aging all freshness indicators.
     * 
     * When some message arrives handler can increment freshness indicator for
     * destination node and decrement it for all others to indicate aging.
     */
    public void tickReportFreshness(){
        Set<Integer> keySet = pool.keySet();
        Iterator<Integer> itKey = keySet.iterator();

        while(itKey.hasNext()){
            try{
                pool.get(itKey.next()).decReportFreshness();
            } catch(Exception e){
                ;
            } 
        }
    }

    /**
     * Returns set of mobile nodes which has freshness indicator over
     * some threshold.
     * 
     * @return 
     */
    public Set<MobileNode> getFreshNodes(){
        Set<Integer> keySet = pool.keySet();
        Iterator<Integer> itKey = keySet.iterator();
        Set<MobileNode> freshNodes = new HashSet<MobileNode>();

        while(itKey.hasNext()){
            try{
                MobileNode tmpNnode = pool.get(itKey.next());
                int freshness = tmpNnode.getReportFreshness();
                if (freshness > 0){
                    freshNodes.add(tmpNnode);
                }
            } catch(Exception e){
                ;
            }
        }

        return freshNodes;
    }

    /**
     * Does pool contain specified mobile node?
     * Used to determine whether node has its own class instance
     *
     * @param i - node id
     * @return boolean
     */
    public boolean existsMobileNode(int i){
        return pool.containsKey(new Integer(i));
    }

    /**
     * Returns mobileNode from pool if exists, otherwise throws IllegalArgumentException
     *
     * @throws IllegalArgumentException
     * @param i - node id
     * @param createit - if nodeid is not found in pool create new instance and return
     * otherwise exception is thrown
     * @return MobileNode
     */
    public MobileNode getMobileNode(int i, boolean createit){
        if (this.existsMobileNode(i) == false) {
            if (createit==false)
                throw new IllegalArgumentException("MobileNode does not exists");
            else {
                return addMobileNode(i);
            }
        }
        else {
            return pool.get(new Integer(i));
        }
    }

    /**
     * Another getMobileNode implementation, by default do not create new mobile
     * nodes and throw exception
     * @param i
     * @return MobileNode
     */
    public MobileNode getMobileNode(int i){
        return getMobileNode(i, false);
    }

    /**
     * Registers new mobile node if does not exists, otherwise throws IllegalArgumentException
     *
     * @throws IllegalArgumentException
     * @param i - nodeid
     */
    public synchronized MobileNode addMobileNode(int i){
        if (this.existsMobileNode(i) == true) throw new IllegalArgumentException("MobileNode already exists");

        // create new instance
        MobileNode mb = new MobileNode(i);

        // insert to pool
        pool.put(new Integer(i), mb);

        // put to node register as well
        

        return mb;
    }

    /**
     * Adds mobile node to mobile manager
     * 
     * @param mn
     * @return
     */
    public synchronized MobileNodeManager addMobileNode(MobileNode mn){
        if (mn==null) throw new NullPointerException("Passed mobile node is null, null is not permited");
        if (this.existsMobileNode(mn.getNodeId())) throw new IllegalArgumentException("Node already in manager");

        pool.put(Integer.valueOf(mn.getNodeId()), mn);
        return this;
    }

    public synchronized boolean removeMobileNode(int nodeId){
        if (this.existsMobileNode(nodeId)==false) return false;
        
        // remove from container
        pool.remove(Integer.valueOf(nodeId));
        return true;
    }

    /**
     * Return all registered mobile nodes
     * 
     * @return
     */
    public Set<Integer> getMobileNodes(){
        return pool.keySet();
    }

    /**
     * Reset manager state = remove all mobile nodes from mapping
     */
    public synchronized void resetState(){
        this.pool = new HashMap<Integer, MobileNode>();
    }
}
