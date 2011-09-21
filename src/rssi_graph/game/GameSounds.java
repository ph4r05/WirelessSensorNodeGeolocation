/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.util.LinkedList;
import rssi_graph.utils.AePlayWave;

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
    public static final String GameStart = "./soundFinished.wav";
    public static final String GameStartCountdown = null;
    public static final String GameFinish = "./soundFinished.wav";
    public static final String GameFinishCountdown = null;
    
    /**
     * Sounds during game
     */
    public static final String EnergyCharging = null;
    
    /**
     * Player states during play
     */
    public static final String[] PlayerEnergyFull = {"./soundFinished.wav", "./soundFinished.wav"};
    public static final String[] PlayerEnergyWarning = {"./soundFinished.wav","./soundFinished.wav"};
    public static final String[] PlayerEnergyCritical = {"./soundFinished.wav","./soundFinished.wav"};
    public static final String[] PlayerEnergyEmpty = {"./soundFinished.wav","./soundFinished.wav"};
    
    /**
     * Game finished
     * 0 = you (single player)
     * 1 = player 1
     * 2 = player 2
     */
    public static final String[] PlayerWon = {"./soundFinished.wav","./soundFinished.wav","./soundFinished.wav"};
    public static final String[] PlayerLoose = {"./soundFinished.wav","./soundFinished.wav","./soundFinished.wav"};
    
    /**
     * Energy notifications
     */
    public static final int ENERGY_EMPTY=0;
    public static final int ENERGY_CRITICAL=1;
    public static final int ENERGY_WARNING=2;
    public static final int ENERGY_OK=3;
    public static final int ENERGY_FULL=4;
    
    /**
     * Last energy level notified to user
     */
    protected Integer[] playerLastEnergyNotif = {null,null};
    
    /**
     * Play queue
     */
    protected LinkedList<String> playQueue = null;

    /**
     * Initialization
     */
    public void initThis() {
        playQueue = new LinkedList<String>();
    }
    
    /**
     * reset state on game reset
     */
    public void resetInternalState(){
        this.playerLastEnergyNotif[0]=null;
        this.playerLastEnergyNotif[1]=null;
    }
    
    /**
     * Event trigered on energy change
     * @param player
     * @param energy 
     */
    public void energyChanged(int player, double energy){
        if (player!=1 && player!=2) return;
        
        // categorize this energy 
        if (energy>=100){
            // full
            //get last detection
            if (this.playerLastEnergyNotif[player]==null || this.playerLastEnergyNotif[player]!=GameSounds.ENERGY_FULL){
                this.playerLastEnergyNotif[player]=Integer.valueOf(GameSounds.ENERGY_FULL);
                GameSounds.playSound(GameSounds.PlayerEnergyFull[player-1]);
            }
        } else if (energy>=GameWorker.THRESHOLD_WARNING){
            // ok
            return;
        } else if (energy>=GameWorker.THRESHOLD_CRITICAL){ 
            // warning
            if (this.playerLastEnergyNotif[player]==null || this.playerLastEnergyNotif[player]!=GameSounds.ENERGY_WARNING){
                this.playerLastEnergyNotif[player]=Integer.valueOf(GameSounds.ENERGY_WARNING);
                GameSounds.playSound(GameSounds.PlayerEnergyWarning[player-1]);
            }
        } else if (energy > 0){
            // critical
            if (this.playerLastEnergyNotif[player]==null || this.playerLastEnergyNotif[player]!=GameSounds.ENERGY_CRITICAL){
                this.playerLastEnergyNotif[player]=Integer.valueOf(GameSounds.ENERGY_CRITICAL);
                GameSounds.playSound(GameSounds.PlayerEnergyCritical[player-1]);
            }
        }
    }
    
    /**
     * Event on game state change
     * @param newState 
     */
    public void gameStateChanged(int newState){
        switch(newState){
            case GameEvent.STARTED:
                // fire at start
                GameSounds.playSound(GameSounds.GameStart);
                break;
                
            case GameEvent.GAMEOVER:
                // fire at end
                GameSounds.playSound(GameSounds.GameFinish);
                break;
        }
    }
    
    /**
     * Player won 
     * 
     * @param player 
     */
    public void playerFinished(int player, boolean won){
        if (player!=1 && player!=2) return;
        
        // who won?
        if (this.gameWorker.isMultiplayer()){
            // who won?
            GameSounds.playSound(GameSounds.PlayerWon[player]);
        } else {
            // you win / you loose
            GameSounds.playSound(GameSounds.PlayerWon[0]);
        }
    }
    
    /**
     * Play sound if exists
     * @param path 
     */
    public static void playSound(String path){
        if (path!=null){
            // play that sound
            new AePlayWave(path).start();
        }
        
        return;
    }
    
    public GameWorker getGameWorker() {
        return gameWorker;
    }

    public void setGameWorker(GameWorker gameWorker) {
        this.gameWorker = gameWorker;
    }
}
