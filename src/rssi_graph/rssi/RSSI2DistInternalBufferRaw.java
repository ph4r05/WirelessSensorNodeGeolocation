/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

/**
 *
 * @author ph4r05
 */
public class RSSI2DistInternalBufferRaw implements Cloneable, Comparable<Object> {
    public Integer rssi;

    /**
     * Modified clone() override
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        RSSI2DistInternalBufferRaw clone=(RSSI2DistInternalBufferRaw)super.clone();

        // make the shallow copy of the object of type Department
        clone.rssi = new Integer((Integer)rssi);
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
        final RSSI2DistInternalBufferRaw other = (RSSI2DistInternalBufferRaw) obj;
        if (this.rssi != other.rssi && (this.rssi == null || !this.rssi.equals(other.rssi))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.rssi != null ? this.rssi.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        //this optimization is usually worthwhile, and can
        //always be added
        if ( this == o ) return EQUAL;

        // test
        if (o == null) return BEFORE;
        if (!(o instanceof RSSI2DistInternalBufferRaw)){
            throw new IllegalArgumentException("Cannot compare with another objects");
        }
        
        final RSSI2DistInternalBufferRaw aThat = (RSSI2DistInternalBufferRaw) o;
        if (this.rssi==null && aThat.rssi==null) return EQUAL;
        if (this.rssi==null) return AFTER;

        int comparison = this.rssi.compareTo(aThat.rssi);
        if ( comparison != EQUAL ) return comparison;

        return EQUAL;
    }

    /**
     * =========================================================================
     *
     * CONSTRUCTORS
     *
     * =========================================================================
     */

    public RSSI2DistInternalBufferRaw() {
    }

    public RSSI2DistInternalBufferRaw(Integer rssi) {
        this.rssi = rssi;
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */
    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }
}
