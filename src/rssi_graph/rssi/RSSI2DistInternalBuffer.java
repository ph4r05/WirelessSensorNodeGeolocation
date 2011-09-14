/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Internal library independent data structure to hold RSSI measurements
 * with relation to distance or other parameters.
 *
 * Can be used to pass data to graph, output, another process for further processing
 * @author ph4r05
 */
public class RSSI2DistInternalBuffer implements Cloneable, Comparable<RSSI2DistInternalBuffer> {

    /**
     * =========================================================================
     *
     * DETERMINANTS
     *
     * =========================================================================
     */

    private int testno=-1;
    private int mid=-1;
    private int txpower=-1;
    private int distance=-1;
    private int talkingMote=-1;
    private int reportingMote=-1;

    // xvalue for graph
    private double xvalue=-1;

    /**
     * =========================================================================
     *
     * DATA
     *
     * =========================================================================
     */

    /**
     * Raw data in linked list,should be unmodifiable
     */
    private List<RSSI2DistInternalBufferRaw> raw=null;

    /**
     * Statistical data (Descriptive Statistics)
     * Should be always consistent with raw data if not null
     * Should be read-only
     */
    private RSSI2DistInternalBufferStats stats=null;

    /**
     * Clone method, clone whole objects with data
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        RSSI2DistInternalBuffer clone=(RSSI2DistInternalBuffer)super.clone();

        // copy attributes
        clone.setDistance(this.getDistance());
        clone.setMid(this.getMid());
        clone.setReportingMote(this.getReportingMote());
        clone.setTalkingMote(this.getTalkingMote());
        clone.setTestno(this.getTestno());
        clone.setTxpower(this.getTxpower());
        clone.setXvalue(this.getXvalue());

        // clone stat data
        clone.stats = stats!=null ? (RSSI2DistInternalBufferStats) this.getStats().clone() : null;

        // clone raw data
        if (raw != null && raw instanceof List){
            clone.raw = new LinkedList<RSSI2DistInternalBufferRaw>();
            Iterator<RSSI2DistInternalBufferRaw> it = raw.iterator();
            while(it.hasNext()){
                clone.raw.add((RSSI2DistInternalBufferRaw) it.next().clone());
            }
        }
        
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RSSI2DistInternalBuffer other = (RSSI2DistInternalBuffer) obj;
        if (this.testno != other.testno) {
            return false;
        }
        if (this.mid != other.mid) {
            return false;
        }
        if (this.txpower != other.txpower) {
            return false;
        }
        if (this.distance != other.distance) {
            return false;
        }
        if (this.talkingMote != other.talkingMote) {
            return false;
        }
        if (this.reportingMote != other.reportingMote) {
            return false;
        }
        if (Double.doubleToLongBits(this.xvalue) != Double.doubleToLongBits(other.xvalue)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.testno;
        hash = 67 * hash + this.mid;
        hash = 67 * hash + this.txpower;
        hash = 67 * hash + this.distance;
        hash = 67 * hash + this.talkingMote;
        hash = 67 * hash + this.reportingMote;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.xvalue) ^ (Double.doubleToLongBits(this.xvalue) >>> 32));
        return hash;
    }

    /**
     * Basic compare method.
     * If another comparison criteria are needed, define own comparator.
     *
     * Default Compare priority:
     *  - testno
     *  - talking mote
     *  - reporting mote
     *  - txpower
     *  - distance
     *  - mid
     * @param o
     * @return
     */
    public int compareTo(RSSI2DistInternalBuffer other) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (other == null) return -1;
        if (getClass() != other.getClass()) return -1;

        //this optimization is usually worthwhile, and can
        //always be added
        if ( this == other ) return EQUAL;
        
        if (this.testno < other.testno) return BEFORE;
        if (this.testno > other.testno) return AFTER;
        
        if (this.talkingMote < other.talkingMote) return BEFORE;
        if (this.talkingMote > other.talkingMote) return AFTER;
        
        if (this.reportingMote < other.reportingMote) return BEFORE;
        if (this.reportingMote > other.reportingMote) return AFTER;

        if (this.txpower < other.txpower) return BEFORE;
        if (this.txpower > other.txpower) return AFTER;
        
        if (this.distance < other.distance) return BEFORE;
        if (this.distance > other.distance) return AFTER;

        if (this.mid < other.mid) return BEFORE;
        if (this.mid > other.mid) return AFTER;

        //all comparisons have yielded equality
        //verify that compareTo is consistent with equals (optional)
        assert this.equals(other) : "compareTo inconsistent with equals.";

        return EQUAL;
    }

    /**
     * Override to string
     * Should return in same order as compareTo defines compare relation
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RSSI2DistInternalBuffer[testno=").append(this.testno)
                .append("; talkingMote=").append(this.talkingMote)
                .append("; reportingMote=").append(this.reportingMote)
                .append("; txpower=").append(this.txpower)
                .append("; distance=").append(this.distance)
                .append("; mid=").append(this.mid)
                .append("; xvalue=").append(this.xvalue)
                .append("; rawData=").append(this.raw==null ? "null" : this.raw.size())
                .append("; stats=").append(this.stats==null ? "no" : "yes")
                .append("] ");
        return sb.toString();
    }

    /**
     * =========================================================================
     *
     * CONSTRUCTORS
     *
     * =========================================================================
     */

    public RSSI2DistInternalBuffer() {
    }

    public RSSI2DistInternalBuffer(int testno, int mid, int txpower, int distance, int talkingMote, int reportingMote) {
        this.testno = testno;
        this.mid = mid;
        this.txpower = txpower;
        this.distance = distance;
        this.talkingMote = talkingMote;
        this.reportingMote = reportingMote;
    }

    /**
     * Compute statistics for loaded raw data
     */
    public void computeStats(){
        if (this.raw==null){
            this.stats=null;
            return;
        }

        this.stats = RSSI2DistInternalBufferStats.getInstance(this.getRaw());
    }

    /**
     * delete data
     */
    public void clear(){
        this.setRaw(null);
    }

    /**
     * Load raw data from Integer array
     * Suitable for mass data load (from resultsets, measurement queues)
     * @param a
     */
    public void loadData(Integer[] a){
        if (a==null){
            throw new IllegalArgumentException("Null object passed");
        }

        this.clear();
        LinkedList<RSSI2DistInternalBufferRaw> tmpList = new LinkedList<RSSI2DistInternalBufferRaw>();
        for(int i=0, cn=a.length; i<cn; i++){
            tmpList.offer(new RSSI2DistInternalBufferRaw(a[i]));
        }
        
        this.raw = Collections.unmodifiableList(tmpList);
        this.stats = null;
    }

    /**
     * Load raw data from 2D integer array
     *  [1d] = only sequence number
     *  [2d] = only 1 element, rssi value
     *
     * Suitable when extracting raw data from resultset containing only RSSI values
     * @param a
     */
    public void loadData(Integer[][] a){
        if (a==null){
            throw new IllegalArgumentException("Null object passed");
        }

        this.clear();
        LinkedList<RSSI2DistInternalBufferRaw> tmpList = new LinkedList<RSSI2DistInternalBufferRaw>();
        for(int i=0, cn=a.length; i<cn; i++){
            if (a[i]==null || a[i].length<1 || a[i].length>1){
                throw new IllegalArgumentException("Subarray is illegal (null or does not have correct size");
            }

            tmpList.offer(new RSSI2DistInternalBufferRaw(a[i][0]));
        }

        this.raw = Collections.unmodifiableList(tmpList);
        this.stats = null;
    }

    /**
     * Load raw data from 2D integer array
     *  [1d] = only sequence number
     *  [2d] = only 1 element, rssi value
     *
     * Suitable when extracting raw data from resultset containing only RSSI values
     * @param a
     */
    public void loadData(Object[][] a){
        if (a==null){
            throw new IllegalArgumentException("Null object passed");
        }

        this.clear();
        LinkedList<RSSI2DistInternalBufferRaw> tmpList = new LinkedList<RSSI2DistInternalBufferRaw>();
        for(int i=0, cn=a.length; i<cn; i++){
            if (a[i]==null || a[i].length<1 || a[i].length>1){
                throw new IllegalArgumentException("Subarray is illegal (null or does not have correct size");
            }

            if (!(a[i][0] instanceof Integer)){
                throw new IllegalArgumentException("Illegal type, Integer excepted");
            }

            tmpList.offer(new RSSI2DistInternalBufferRaw((Integer) a[i][0]));
        }

        this.raw = Collections.unmodifiableList(tmpList);
        this.stats = null;
    }
    
    /**
     * Export data as raw integers
     * Useful for chart generation
     * 
     * @return
     */
    public List<Integer> exportData(){
        if (this.raw==null) return null;

        List<Integer> resultList = new LinkedList<Integer>();
        Iterator<RSSI2DistInternalBufferRaw> it = this.raw.iterator();
        while(it.hasNext()){
            resultList.add(it.next().getRssi());
        }

        return resultList;
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */

    /**
     * Set raw objects and cleans statistics
     * @param raw
     */
    public void setRaw(LinkedList<RSSI2DistInternalBufferRaw> raw) {
        this.raw = raw;
        this.stats = null;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    /**
     * Returns unmodifiable list
     * @return
     */
    public List<RSSI2DistInternalBufferRaw> getRaw() {
        return Collections.unmodifiableList(raw);
    }

    public int getReportingMote() {
        return reportingMote;
    }

    public void setReportingMote(int reportingMote) {
        this.reportingMote = reportingMote;
    }

    public RSSI2DistInternalBufferStats getStats() {
        return stats;
    }

    public int getTalkingMote() {
        return talkingMote;
    }

    public void setTalkingMote(int talkingMote) {
        this.talkingMote = talkingMote;
    }

    public int getTestno() {
        return testno;
    }

    public void setTestno(int testno) {
        this.testno = testno;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public double getXvalue() {
        return xvalue;
    }

    public void setXvalue(double xvalue) {
        this.xvalue = xvalue;
    }

    
}
