/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

/**
 * Handling sounds in game
 * 
 * @author ph4r05
 */
public class GameSounds {
    /**
     * Keep back reference to parent.
     */
    protected GameWorker gameWorker=null;
    
    /**
     * Player name for introduction
     */
    public static final String[] PlayerName = {null, null};
    
    /**
     * Starting/stopping game
     */
    public static final String GameStart = null;
    public static final String GameStartCountdown = null;
    public static final String GameFinish = null;
    public static final String GameFinishCountdown = null;
    
    /**
     * Sounds during game
     */
    public static final String EnergyCharging = null;
    
    /**
     * Player states during play
     */
    public static final String[] PlayerEnergyFull = {null, null};
    public static final String[] PlayerEnergyWarning = {null,null};
    public static final String[] PlayerEnergyCritical = {null,null};
    public static final String[] PlayerEnergyEmpty = {null,null};
    protected double[] playerEnergyLastNotification = {-1D,-1D};
    
    /**
     * Game finished
     */
    public static final String[] PlayerWon = {null,null};
    public static final String[] PlayerLoose = {null,null};
    

    /**
     * Initialization
     */
    void initThis() {
        ;
    }
    
    /**
     * Event trigered on energy change
     * @param player
     * @param energy 
     */
    public void energyChanged(int player, double energy){
        
    }
    
    public GameWorker getGameWorker() {
        return gameWorker;
    }

    public void setGameWorker(GameWorker gameWorker) {
        this.gameWorker = gameWorker;
    }
}
