/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

import java.util.ArrayList;

/**
 * Use to hold fitted functions to given data set
 *
 * @author ph4r05
 */
public class RSSI2DistFittedFunctions implements Cloneable, Comparable<RSSI2DistFittedFunctions> {
    private int testno=-1;
    private int txpower=-1;
    private int talkingMote=-1;
    private int reportingMote=-1;
    private ArrayList<RSSI2DistFunctionInterface> functions=null;

    public RSSI2DistFittedFunctions() {
        
    }

    public RSSI2DistFittedFunctions(int testno, int txpower, int talkingMote, int reportingMote) {
        this.testno=testno;
        this.txpower=txpower;
        this.talkingMote = talkingMote;
        this.reportingMote = reportingMote;
    }

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
        clone.setReportingMote(this.getReportingMote());
        clone.setTalkingMote(this.getTalkingMote());
        clone.setTestno(this.getTestno());
        clone.setTxpower(this.getTxpower());

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
        final RSSI2DistFittedFunctions other = (RSSI2DistFittedFunctions) obj;
        if (this.testno != other.testno) {
            return false;
        }
        if (this.txpower != other.txpower) {
            return false;
        }
        if (this.talkingMote != other.talkingMote) {
            return false;
        }
        if (this.reportingMote != other.reportingMote) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.testno;
        hash = 83 * hash + this.txpower;
        hash = 83 * hash + this.talkingMote;
        hash = 83 * hash + this.reportingMote;
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
     * @param o
     * @return
     */
    public int compareTo(RSSI2DistFittedFunctions other) {
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
        sb.append("RSSI2DistFittedFunctions[testno=").append(this.testno)
                .append("; talkingMote=").append(this.talkingMote)
                .append("; reportingMote=").append(this.reportingMote)
                .append("; txpower=").append(this.txpower)
                .append("] ");
        return sb.toString();
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */

    public int getReportingMote() {
        return reportingMote;
    }

    public void setReportingMote(int reportingMote) {
        this.reportingMote = reportingMote;
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

    public ArrayList<RSSI2DistFunctionInterface> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<RSSI2DistFunctionInterface> functions) {
        this.functions = functions;
    }
}
