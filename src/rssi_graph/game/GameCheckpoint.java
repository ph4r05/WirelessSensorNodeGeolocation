/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import rssi_graph.localization.CoordinateRecord;

/**
 * Game checkpoint.
 * Represents static positions on ring whose player must pass to complete one round. 
 * 
 * Checkpoint is activated/passed iff RSSI received is over defined threshold
 * @author ph4r05
 */
public class GameCheckpoint {    
    /**
     * Node of checkpoint
     */
    protected int checkpointId;
    
    /**
     * Node id of checkpoint
     */
    protected int nodeId;
    
    /**
     * RSSI of input signal to activate this checkpoint
     */
    protected double rssi_threshold;

    /**
     * Position of checkpoint
     */
    protected CoordinateRecord position = null;
    
    /**
     * Constructor 
     * @param nodeId
     * @param checkpointId
     * @param rssi_threshold 
     */
    public GameCheckpoint(int checkpointId, int nodeId, double rssi_threshold) {
        this.nodeId = nodeId;
        this.checkpointId = checkpointId;
        this.rssi_threshold = rssi_threshold;
    }

    /**
     * Constructor 
     * @param nodeId
     * @param checkpointId
     */
    public GameCheckpoint(int checkpointId, int nodeId) {
        this.nodeId = nodeId;
        this.checkpointId = checkpointId;
    }

    /**
     * Constructor 
     * @param checkpointId
     */
    public GameCheckpoint(int checkpointId) {
        this.checkpointId = checkpointId;
    }

    /**
     * Constructor 
     */
    public GameCheckpoint() {
    }
    
    /**
     * True if checkpoint is active now
     * 
     * @param curRssi
     * @return 
     */
    public boolean isActive(double curRssi){
        return this.getRssi_threshold() < curRssi;
    }
    
    /**
     * Score in RSSI values. Higher the score is, the closer is node to checkpoint.
     * Can be used to compare multiple active checkpoints.
     * 
     * @param curRssi
     * @return 
     */
    public double getActiveScore(double curRssi){
        return curRssi - this.getRssi_threshold();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GameCheckpoint other = (GameCheckpoint) obj;
        if (this.nodeId != other.nodeId) {
            return false;
        }
        if (this.checkpointId != other.checkpointId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.nodeId;
        hash = 97 * hash + this.checkpointId;
        return hash;
    }

    public int getCheckpointId() {
        return checkpointId;
    }

    public void setCheckpointId(int checkpointId) {
        this.checkpointId = checkpointId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public double getRssi_threshold() {
        return rssi_threshold;
    }

    public void setRssi_threshold(double rssi_threshold) {
        this.rssi_threshold = rssi_threshold;
    }

    public CoordinateRecord getPosition() {
        return position;
    }

    public void setPosition(CoordinateRecord position) {
        this.position = position;
    }
}
