/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.motecom;

/**
 * Used for listening for sent messages by MsgSender class
 *
 * @author ph4r05
 */
public interface MessageSentListener {
    public void messageSent(String listenerKey, net.tinyos.message.Message msg, int destination);
}
