/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_localization;

import net.tinyos.message.Message;

/**
 * Message listener for modules interface
 * Since JAVA does not support callback methods we need to define interface,
 * then implement it and pass instance of it as event handler.
 * Dispatcher uses event handler interface.
 *
 * Module could register its own message listeners in message dispatchers
 *
 * @author ph4r05
 */
public interface MsgListenerInterface {
    public void messageReceived(int i, int MessageType, Message msg);
}
