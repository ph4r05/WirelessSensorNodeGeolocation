/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

import java.util.LinkedList;
import rssi_graph.utils.ExternalSoundPlayer;

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
    public static final String GameStart = "./sounds/Air Horn-SoundBible.com-964603082.wav";
    public static final String GameStartCountdown = null;
    public static final String GameFinish = "./sounds/Buzzer-SoundBible.com-188422102.wav";
    public static final String GameFinishCountdown = null;
    
    /**
     * Sounds during game
     */
    public static final String EnergyCharging = null;
    
    /**
     * Player states during play
     */
    public static final String[] PlayerEnergyFull = {"./sounds/Power_Up_Ray-Mike_Koenig-800933783.wav", "./sounds/Power_Up_Ray-Mike_Koenig-800933783.wav"};
    public static final String[] PlayerEnergyWarning = {"./soundFinished.wav","./soundFinished.wav"};
    public static final String[] PlayerEnergyCritical = {"./soundFinished.wav","./soundFinished.wav"};
    public static final String[] PlayerEnergyEmpty = {"./sounds/Buzzer-SoundBible.com-188422102.wav","./sounds/Buzzer-SoundBible.com-188422102.wav"};
    
    /**
     * Game finished
     * 0 = you (single player)
     * 1 = player 1
     * 2 = player 2
     */
    public static final String[] PlayerWon = {"./sounds/Buzzer-SoundBible.com-188422102.wav","./sounds/Buzzer-SoundBible.com-188422102.wav","./sounds/Buzzer-SoundBible.com-188422102.wav"};
    public static final String[] PlayerLoose = {"./sounds/Buzzer-SoundBible.com-188422102.wav","./sounds/Buzzer-SoundBible.com-188422102.wav","./sounds/Buzzer-SoundBible.com-188422102.wav"};
    
    /**
     * Energy notifications
     */
    public static final int ENERGY_EMPTY= 0;
    public static final int ENERGY_CRITICAL=1;
    public static final int ENERGY_WARNING=2;
    public static final int ENERGY_OK=3;
    public static final int ENERGY_FULL=4;
    
    /**
     * Last energy level notified to user
     */
    protected int[] playerLastEnergyNotif = {ENERGY_FULL,ENERGY_FULL};
    
    /**
     * Play queue
     */
    protected LinkedList<String> playQueue = null;
    
    /**
     * Time when last sound was played (not to overflow)
     */
    protected long lastSoundPlayed = 0;

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
        this.playerLastEnergyNotif[0]=ENERGY_FULL;
        this.playerLastEnergyNotif[1]=ENERGY_FULL;
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
            if (this.playerLastEnergyNotif[player-1]!=GameSounds.ENERGY_FULL){
                this.playerLastEnergyNotif[player-1]=GameSounds.ENERGY_FULL;
                this.playSound(GameSounds.PlayerEnergyFull[player-1]);
            }
        } else if (energy>=GameWorker.THRESHOLD_WARNING){
            // ok
            if (this.playerLastEnergyNotif[player-1]!=GameSounds.ENERGY_OK){
                this.playerLastEnergyNotif[player-1]=GameSounds.ENERGY_OK;
            }
            return;
        } else if (energy>=GameWorker.THRESHOLD_CRITICAL){ 
            // warning
            if (this.playerLastEnergyNotif[player-1]!=GameSounds.ENERGY_WARNING){
                this.playerLastEnergyNotif[player-1]=GameSounds.ENERGY_WARNING;
                this.playSound(GameSounds.PlayerEnergyWarning[player-1]);
            }
        } else if (energy > 0){
            // critical
            if (this.playerLastEnergyNotif[player-1]!=GameSounds.ENERGY_CRITICAL){
                this.playerLastEnergyNotif[player-1]=GameSounds.ENERGY_CRITICAL;
                this.playSound(GameSounds.PlayerEnergyCritical[player-1]);
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
                this.playSound(GameSounds.GameStart);
                break;
                
            case GameEvent.GAMEOVER:
                // fire at end
                this.playSound(GameSounds.GameFinish);
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
            this.playSound(GameSounds.PlayerWon[player]);
        } else {
            // you win / you loose
            this.playSound(GameSounds.PlayerWon[0]);
        }
    }
    
    /**
     * Play sound if exists
     * @param path 
     */
    public void playSound(String path){
        if (path!=null){
            long curMili = System.currentTimeMillis();
            
            // do not play 2 sounds closer that 1500 ms
            if ((curMili - this.lastSoundPlayed) < 1500) return;
            
            // play that sound
            new ExternalSoundPlayer(path).start();
            this.lastSoundPlayed = curMili;
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
