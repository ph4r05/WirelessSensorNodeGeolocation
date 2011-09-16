/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_localization;

import rssi_graph.messages.MultiPingResponseReportMsg;
import java.text.DecimalFormat;

/**
 * Element of message queue
 * @author ph4r05
 */
public class RSSIMessageQueueElement {
    protected MultiPingResponseReportMsg msgBody;
    protected double myRssiSmooth;
    protected double hisRssiSmooth;
    protected boolean gap=false;


    protected int measurementNum=0;
    protected double distance=0;
    protected int HAngle=0;
    protected int VAngle=0;
    protected int channel=0;
    protected int txpower=0;
    protected boolean barrier=false;

    @Override
    public RSSIMessageQueueElement clone() throws CloneNotSupportedException {
        RSSIMessageQueueElement tmp = (RSSIMessageQueueElement) super.clone();
        tmp.setMsgBody((MultiPingResponseReportMsg) this.getMsgBody().clone());
        return tmp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RSSIMessageQueueElement other = (RSSIMessageQueueElement) obj;
        if (this.msgBody != other.msgBody && (this.msgBody == null || !this.msgBody.equals(other.msgBody))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.msgBody != null ? this.msgBody.hashCode() : 0);
        return hash;
    }


    public int bucketHashCode(){
        int hash = 5;
        hash = 29 * hash + (this.msgBody != null ? this.msgBody.hashCode() : 0);
        hash = 29 * hash + this.HAngle;
        hash = 29 * hash + this.VAngle;
        hash = 29 * hash + this.channel;
        hash = 29 * hash + this.txpower;
        hash = 29 * hash + (this.barrier ? 1 : 0);
        return hash;
    }

    public static String getStructuredOutHeader(){
        return "distance;"
                +"counter;"
                +"rssi;"
                +"txpower;"
                +"channel;"
                +"HAngle;"
                +"VAngle";
    }

    public String getStructuredOut() {
        return getDistance()+";"
                +getMsgBody().get_counter()+";"
                +getMsgBody().get_rssi()+";"
                +getTxpower()+";"
                +getChannel()+";"
                +getHAngle()+";"
                +getVAngle();
    }

    public String getFormatedOut() {
        DecimalFormat df = new DecimalFormat("00.000000");
        return "RssiMsg received; node1: " +
		       "hisRssi=" +  getMsgBody().get_rssi() +
                       " Counter: " + getMsgBody().get_counter() +
                       " Dist: " + getDistance() +
                       "; TXpower: " + getTxpower() +
                       ";";
    }

    public double getHisRssiSmooth() {
        return hisRssiSmooth;
    }

    public void setHisRssiSmooth(double hisRssiSmooth) {
        this.hisRssiSmooth = hisRssiSmooth;
    }

    public MultiPingResponseReportMsg getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(MultiPingResponseReportMsg msgBody) {
        this.msgBody = msgBody;
    }

    public double getMyRssiSmooth() {
        return myRssiSmooth;
    }

    public void setMyRssiSmooth(double myRssiSmooth) {
        this.myRssiSmooth = myRssiSmooth;
    }

    public boolean isGap() {
        return gap;
    }

    public void setGap(boolean gap) {
        this.gap = gap;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getHAngle() {
        return HAngle;
    }

    public void setHAngle(int HAngle) {
        this.HAngle = HAngle;
    }

    public int getVAngle() {
        return VAngle;
    }

    public void setVAngle(int VAngle) {
        this.VAngle = VAngle;
    }

    public boolean isBarrier() {
        return barrier;
    }

    public void setBarrier(boolean barrier) {
        this.barrier = barrier;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public int getMeasurementNum() {
        return measurementNum;
    }

    public void setMeasurementNum(int measurementNum) {
        this.measurementNum = measurementNum;
    }
    
}
