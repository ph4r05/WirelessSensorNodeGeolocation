/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_localization;

import java.io.IOException;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.PhoenixError;

/**
 *
 * @author ph4r05
 */
public class PacketError implements PhoenixError {

    MoteIF moteif;
    public void error(IOException ioe) {
        System.err.println("Phoenix Error: " + ioe.getMessage());
        if (moteif!=null){
            moteif.getSource().setResurrection();
        }
    }

    public MoteIF getMoteif() {
        return moteif;
    }

    public void setMoteif(MoteIF moteif) {
        this.moteif = moteif;
    }
    
    
    
}
