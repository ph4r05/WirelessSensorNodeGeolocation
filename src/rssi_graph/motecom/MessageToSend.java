/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

import java.util.LinkedList;
import java.util.List;
import net.tinyos.message.Message;

/**
 * Queue element for each message. Uses general message interface.
 * Contains string passed to logger when message is sent.
 *
 * Optional fields are event listener which is triggered on message sent.
 * @author ph4r05
 */
public class MessageToSend {

    public net.tinyos.message.Message sMsg;
    public String string;
    public int destination;

    /**
     * Message sent listener
     */
    public List<MessageSentListener> listener=null;

    /**
     * Listener key for message listener to uniquely determine message
     */
    public String listenerKey=null;


    public MessageToSend(net.tinyos.message.Message sMsg, int destination, String string) {
        this.string = string;
        this.destination = destination;
        this.sMsg = sMsg;
    }

    public MessageToSend(net.tinyos.message.Message sMsg, int destination, String string,
            MessageSentListener listener, String listenerKey) {
        this.sMsg = sMsg;
        this.string = string;
        this.destination = destination;
        this.listener = new LinkedList<MessageSentListener>();
        this.listener.add(listener);
        this.listenerKey = listenerKey;
    }

    /**
     * Adds message sent listener
     *
     * @param listener
     */
    public boolean addListener(MessageSentListener listener){
       if (this.listener == null){
           this.listener = new LinkedList<MessageSentListener>();
       }

       // add only if does not exist
       if (this.listener.contains(listener)){
           return false;
       }

       return this.listener.add(listener);
    }

    /**
     * Removes listener
     *
     * @param listener
     * @return
     */
    public boolean removeListener(MessageSentListener listener){
       if (this.listener == null){
           this.listener = new LinkedList<MessageSentListener>();
           return false;
       }

       return this.listener.remove(listener);
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public Message getsMsg() {
        return sMsg;
    }

    public void setsMsg(Message sMsg) {
        this.sMsg = sMsg;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public List<MessageSentListener> getListener() {
        return listener;
    }

    public void setListener(List<MessageSentListener> listener) {
        this.listener = listener;
    }

    public String getListenerKey() {
        return listenerKey;
    }

    public void setListenerKey(String listenerKey) {
        this.listenerKey = listenerKey;
    }
}
