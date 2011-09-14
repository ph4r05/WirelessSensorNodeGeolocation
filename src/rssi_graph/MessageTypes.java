/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

/**
 * Message types as defined in RssiDemoMessages.h
 * It is needed to keep this class data consistent with nesC header file to work
 * properly.
 * 
 * @author ph4r05
 */
public class MessageTypes {
  public static final int AM_RSSIMSG = 10;
  public static final int AM_PINGMSG = 11;
  public static final int AM_MULTIPINGMSG = 12;
  public static final int AM_MULTIPINGRESPONSEMSG = 13;
  public static final int AM_COMMANDMSG = 14;
  public static final int AM_MULTIPINGRESPONSEREPORTMSG = 16;
  public static final int AM_MULTIPINGRESPONSETINYREPORTMSG = 17;

  // abort message types
  public static final int COMMAND_NONE=0;
  public static final int COMMAND_ABORT=1;
  public static final int COMMAND_IDENTIFY=2;
  public static final int COMMAND_RESET=3;
  public static final int COMMAND_SETTX=4;
  public static final int COMMAND_SETCHANNEL=5;
  public static final int COMMAND_ACK=6;
  public static final int COMMAND_NACK=7;
  public static final int COMMAND_SETBS=8;
  public static final int COMMAND_LOCK=9;
  public static final int COMMAND_GETREPORTINGSTATUS=10;
  public static final int COMMAND_SETREPORTINGSTATUS=11;
  public static final int COMMAND_SETDORANDOMIZEDTHRESHOLDING=12;
  public static final int COMMAND_SETQUEUEFLUSHTHRESHOLD=13;
  public static final int COMMAND_SETTINYREPORTS=14;
  public static final int COMMAND_SETOPERATIONMODE=15;
  public static final int COMMAND_SETREPORTPROTOCOL=16;
  public static final int COMMAND_FLUSHREPORTQUEUE=17;
  public static final int COMMAND_SETNOISEFLOORREADING=18;
  public static final int COMMAND_SETREPORTGAP=19;
  public static final int COMMAND_GETSENSORREADING=20;
  public static final int COMMAND_SENSORREADING=21;

  // identity types
  public static final int NODE_STATIC=1;
  public static final int NODE_DYNAMIC=2;
  public static final int NODE_BS=3;
  public static final int NODE_DEAD=4;

  // report protocols
  public static final int REPORTING_MEDIUM=1;
  public static final int REPORTING_TINY=2;
  public static final int REPORTING_MASS=3;
}
