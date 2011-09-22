/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_detectbasestation;

import java.io.*;
import java.io.IOException;
import java.text.DecimalFormat;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;
import rssi_detectbasestation.CommandMsg;

/**
 *
 * @author ph4r05
 */
public class RSSI_detectBaseStation implements MessageListener {
    private MoteIF moteIF;
    protected String source=null;
    protected boolean isOK=false;
    protected int okScore=0;
    protected int state=0;
    protected long sentStart = 0;
    protected long sendMessageTime=0;
    
    /**
     * @param args the command line arguments
     * 
     * iterate over found files
     */
    public static void main(String[] args) {
        
        File f = null;
        String path = null;
        boolean bs_found=false;
        
        String source = null;
        if (args.length == 2) {
          if (!args[0].equals("-comm")) {
            System.exit(1);
          }
          source = args[1];
        }
        else if (args.length != 0) {
          System.exit(1);
        }
        
        // test defined
        if (source!=null){
            int result = testSource(source);
            if (result>0){
                System.out.println(source);
                System.exit(0);
            } else {
                System.err.println("Nope, score: " + result);
                System.exit(1);
            }
        }

        // search for default tmote file, assume we use our udev script so every
        // tmote has symlink like /dev/tmote_sky_0 -> /dev/ttyUSB0
        // search for first available tmote device
        for (int i=0; i<12 && bs_found==false; i++){
            path = "/dev/ttyUSB" + i;
            f = new File(path);

            // does file exists and can I read it?
            if (f.exists() && f.canRead()){
                // rights are OK, perform test on this
                source = "serial@" + path + ":micaz";
                int result = testSource(source);
                if (result==0) continue;
                
                System.out.println(source);
                System.exit(0);
            }
        }
        
        return;
    }
    
    public static int testSource(String source){
        RSSI_detectBaseStation tester = new RSSI_detectBaseStation();
        return tester.testInterface(source);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.moteIF.deregisterListener(new CommandMsg(), this);
        this.moteIF = null;
    }

    public int testInterface(String connectString){
        try {
            PhoenixSource phoenix;
            phoenix = BuildSource.makePhoenix(connectString, PrintStreamMessenger.err);
            MoteIF mif = new MoteIF(phoenix);
            
            this.moteIF = mif;
            this.moteIF.registerListener(new CommandMsg(), this);
            CommandMsg payload = new CommandMsg();

            // try to send
            try{
                    payload.set_command_id((short)1);
                    payload.set_command_code((short)2);
                    payload.set_command_data(0);
                    
                    // inform
                    System.err.println("Trying to send IDENTIFY request");
                    
                    // store time
                    this.sentStart = System.currentTimeMillis();
                    
                    // try to send
                    moteIF.send(MoteIF.TOS_BCAST_ADDR, payload);
                    
                    // wait for reply
                    long curMilis = System.currentTimeMillis();
                    if (curMilis - this.sentStart > 1000){
                        // sending failed 
                        return this.okScore;
                    }

                    // if here, it is probably OK
                    this.okScore+=1;
                    this.isOK=true;
                    System.err.println("Request sent, waiting answer");

                    // sleep for a while
                    int counter = 0;
                    while(this.okScore!=2 && counter<8){
                        Thread.sleep(250);
                        counter += 1;
                    }
                    
                    return this.okScore;
            }
            catch (Exception exception) {
              System.err.println("Exception thrown when sending packets. Exiting.");
              System.err.println(exception);
              return this.okScore;
            }
            
        } catch(Exception ex){
            return this.okScore;
        }
    }
    
    @Override
    public void messageReceived(int i, Message msg) {
        if (msg instanceof CommandMsg){
            final CommandMsg Message = (CommandMsg) msg;

            // get system miliseconds
            long currentTimeMillis = System.currentTimeMillis();
            
            // determine source of message (node id of sender)
            int sourceTmp = Message.getSerialPacket().get_header_src();

            // inform that message was received
            this.okScore+=1;
            System.err.println("MessageReceived from: " + sourceTmp + " at: " + currentTimeMillis);
            
            // set OK now
            this.setIsOK(true);
        }
        else {
            System.err.println("Command, unexpected message received");
            return;
        }
    }

    public MoteIF getMoteIF() {
        return moteIF;
    }

    public void setMoteIF(MoteIF moteIF) {
        this.moteIF = moteIF;
    }

    public long getSendMessageTime() {
        return sendMessageTime;
    }

    public void setSendMessageTime(long sendMessageTime) {
        this.sendMessageTime = sendMessageTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isIsOK() {
        return isOK;
    }

    public void setIsOK(boolean isOK) {
        this.isOK = isOK;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
