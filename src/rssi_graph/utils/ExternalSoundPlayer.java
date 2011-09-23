/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.utils;

import java.io.File;

/**
 * External sound player for java
 * uses playsound command
 * 
 * @author ph4r05
 */
public class ExternalSoundPlayer extends Thread {

    /**
     * Executable of sound player
     */
    public static final String soundPlayer="/usr/bin/playsound";
    
    private String filename;

    private Position curPosition;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    enum Position {
        LEFT, RIGHT, NORMAL
    };

    public ExternalSoundPlayer(String wavfile) {
        filename = wavfile;
        curPosition = Position.NORMAL;
    }

    @Override
    public void run() {
        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }

        try {
            Process p = new ProcessBuilder(ExternalSoundPlayer.soundPlayer, filename).start();
        } catch(Exception e){
            e.printStackTrace(System.err);
        }
    }
}
 