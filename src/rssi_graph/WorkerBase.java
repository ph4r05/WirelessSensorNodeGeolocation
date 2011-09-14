/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.utils.AePlayWave;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import rssi_graph.motecom.MessageSender;
import rssi_graph.nodeRegister.NodeRegister;

/**
 * Base worker
 *
 * @author ph4r05
 */
public class WorkerBase implements MessageListener, WorkerInterface, ActionListener, ItemListener, rssi_graph.motecom.MessageSentListener {

    /**
     * Mote interface
     * used to register message listener, send packets and so on...
     */
    protected MoteIF moteIF = null;
    protected NodeRegister nodeRegister=null;

    public MoteIF getMoteIF() {
        return moteIF;
    }

    public void setMoteIF(MoteIF moteIF) {
        this.moteIF = moteIF;
    }

    public void itemStateChanged(ItemEvent e) {
        System.err.println("");
        System.err.println("itemStateChanged to unrecognized object");
        System.err.println("itemToStrong: " + e.getItem().toString());
        System.err.println("itemClassToStrong: " + e.getItem().getClass().toString());
        System.err.println("itemSelectableToStrong: " + e.getItemSelectable().toString());
        System.err.println("itemSelectable.getSelectedObjects==null: " + (e.getItemSelectable().getSelectedObjects()==null ?
            -1 : e.getItemSelectable().getSelectedObjects().length));
        System.err.println("paramString: " + e.paramString());
        System.err.println("getSource().toString(): " + e.getSource().toString());
        System.err.println("getSource().getClass().toString(): " + e.getSource().getClass().toString());
        System.err.println("JToggle to string: " + javax.swing.JToggleButton.class.toString());
    }

    public void messageReceived(int i, Message msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void messageSent(String listenerKey, net.tinyos.message.Message msg, int destination) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void turnOff(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void turnOn(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



    /**
     * Returns msg sender thread
     * @return
     */
    public MessageSender getMsgSender(){
        return RSSI_graphApp.getApplication().getMsgSender();
    }

    /**
     * Helper method that logs to textarea
     * @param s
     */
    public void logToTextarea(String s){
        this.logToTextarea(s, 0, "workerBase", JPannelLoggerLogElement.SEVERITY_DEBUG);
        return;
    }

    /**
     * Helper method that logs to textarea with specified severity
     * @param s
     */
    public void logToTextarea(String s, int severity){
        this.logToTextarea(s, 0, "workerBase", severity);
        return;
    }

    /**
     * Basic logging method
     * By default, log to standard log window
     *
     * This method could be overloaded to log to different window if needed
     * 
     * @param s
     * @param type
     * @param typeString
     * @param severity
     */
    public void logToTextarea(String s,int type, String typeString, int severity){
        RSSI_graphApp.getApplication().getGraphViewFrame().getjPanelLogger1().addLogEntry(s, type, typeString, severity);
    }

    /**
     * Helper method to get main frame with controls
     * We need to read/write to that controls
     *
     * @return
     */
    public RSSI_graphView getWindow(){
        return RSSI_graphApp.getApplication().getGraphViewFrame();
    }

    /**
     * Request user attention
     * Used when cycle is completed or on error
     *
     * - brings main window do front
     * - plays sound (only if sound device is free, current implementation cannot
     *      handle channel mixing)
     */
    public void notifyUser(){
        try {
            getWindow().toFront();

            // play sound
            playSoundFinished();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorkerBase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WorkerBase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(WorkerBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Plays finished sound
     */
    public void playSoundFinished() throws FileNotFoundException, IOException{
        //** add this into your application code as appropriate
        // Open an input stream  to the audio file.
        //InputStream in = new FileInputStream("./soundFinished.wav");
        // Create an AudioStream object from the input stream.
        //AudioStream as = new AudioStream(in);

        // Use the static class member "player" from class AudioPlayer to play clip.
        //AudioPlayer.player.start(as);

        new AePlayWave("./soundFinished.wav").start();

//        for (int i=0; i<5;i++){
//            try {
//                new AePlayWave("./soundFinished.wav").start();
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(WorkerBase.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    public NodeRegister getNodeRegister() {
        return nodeRegister;
    }

    public void setNodeRegister(NodeRegister nodeRegister) {
        this.nodeRegister = nodeRegister;
    }
}
