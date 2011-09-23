/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Register of known checkpoints
 * 
 * @author ph4r05
 */
public class GameCheckpointRegister {
    
    protected Map<Integer, GameCheckpoint> checkpointDb = null;
    
    protected Map<Integer, GameCheckpoint> checkpointDbByNodeId=null;
    
    /**
     * Arbitrary init method
     * 
     * @UGLY_CODE: this is programmed by bad way, I know it, but there is no time left
     * and I need to have it working. It may be later refactored to bether form,
     * but now it must be enought.
     */
    public void initThis(){
        this.checkpointDb = new HashMap<Integer, GameCheckpoint>(24);
        checkpointDbByNodeId = new HashMap<Integer, GameCheckpoint>(24);
        
        // manualy inserting checkpoints to source code (and thresholds)
        this.checkpointDb.put(Integer.valueOf(1), new GameCheckpoint(1, 12, -25.0));
        this.checkpointDb.put(Integer.valueOf(2), new GameCheckpoint(2, 13, -25.0));
        this.checkpointDb.put(Integer.valueOf(3), new GameCheckpoint(3, 14, -25.0));
        this.checkpointDb.put(Integer.valueOf(4), new GameCheckpoint(4, 15, -25.0));
        this.checkpointDb.put(Integer.valueOf(5), new GameCheckpoint(5, 16, -25.0));
        this.checkpointDb.put(Integer.valueOf(6), new GameCheckpoint(6, 17, -25.0));
        this.checkpointDb.put(Integer.valueOf(7), new GameCheckpoint(7, 18, -25.0));
        this.checkpointDb.put(Integer.valueOf(8), new GameCheckpoint(8, 19, -25.0));
        
        // sync maps
        this.syncMaps();
    }
    
    /**
     * Returns iterator for key set of db
     */
    public Iterator<Integer> getKeyIterator(){
        return this.checkpointDb.keySet().iterator();
    }
    
    /**
     * Returns checkpoint with given ID from database
     * 
     * @param id
     * @return 
     */
    public GameCheckpoint getCheckpoint(Integer id){
        if (id==null) return null;
        if (this.getCheckpointDb().containsKey(id)==false) return null;
        return this.getCheckpointDb().get(id);
    }
    
    /**
     * Return checkpoint by node id
     * @param nodeId
     * @return 
     */
    public GameCheckpoint getCheckpointByNodeId(Integer nodeId){
        if (this.getCheckpointDbByNodeId().containsKey(nodeId)==false) return null;
        return this.getCheckpointDbByNodeId().get(nodeId);
    }
    
    /**
     * Returns size of checkpoint database
     * 
     * @return 
     */
    public int getDbSize(){
        return this.checkpointDb.size();
    }
    
    /**
     * Synchronize maps
     */
    protected void syncMaps(){
        this.checkpointDbByNodeId.clear();
        Iterator<Integer> it = this.getKeyIterator();
        
        while(it.hasNext()){
            GameCheckpoint check = this.checkpointDb.get(it.next());
            this.checkpointDbByNodeId.put(Integer.valueOf(check.getNodeId()), check);
        }
    }
    
    /**
     * Is checkpoint database empty?
     */
    public boolean isEmpty(){
        return this.checkpointDb.isEmpty();
    }

    public Map<Integer, GameCheckpoint> getCheckpointDb() {
        return checkpointDb;
    }

    public void setCheckpointDb(Map<Integer, GameCheckpoint> checkpointDb) {
        this.checkpointDb = checkpointDb;
    }

    public Map<Integer, GameCheckpoint> getCheckpointDbByNodeId() {
        return checkpointDbByNodeId;
    }
}
