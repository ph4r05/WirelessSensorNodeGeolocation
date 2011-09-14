/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import rssi_graph.utils.BucketHasherInterface;

/**
 * Default hasher for RSSI2DistInternalBuffer
 * May be used when needed to sort RSSI2DistInternalBuffer classes to buckets
 * according to specified conditions
 *
 * By default is this class used to categorize different data sets for graph.
 * 1 dataset for graph is relation distance->rssi => nodes,txpower,testno are constant in
 * whole dataset.
 *
 * @author ph4r05
 */
public class DefaultRSSI2DistInternalBufferHasher implements BucketHasherInterface<RSSI2DistInternalBuffer> {

    /**
     * String builder for better performance
     */
    private StringBuilder sb = null;
    
    private DecimalFormat df=null;
    
    /**
     * Values used to build identificator
     */
    private Set<String> xgroup=null;
    private boolean useTestno=false;
    private boolean useMid=false;
    private boolean useTxPower=false;
    private boolean useStaticId=false;
    private boolean useMobileId=false;

    /**
     * Basic constructor
     */
    public DefaultRSSI2DistInternalBufferHasher() {
        xgroup=new HashSet<String>();
        df=new DecimalFormat("00");
    }

    /**
     * Get unique string representation
     * @param a
     * @return
     */
    public String getStringHashFor(RSSI2DistInternalBuffer a){
        if (a==null || !(a instanceof RSSI2DistInternalBuffer)){
            throw new IllegalArgumentException("Illegal argument passed");
        }
        
        if (this.xgroup==null){
            this.xgroup.add("testno");
            this.xgroup.add("staticId");
            this.xgroup.add("txpower");
        }

        sb = new StringBuilder(32);
        
        int used=0;
        if (this.useTestno){
            sb.append("t=").append(a.getTestno());
            used++;
        }
        
        if (this.useMobileId){
            if (used>0) sb.append("; ");
            sb.append("mn=").append(a.getTalkingMote());
            used++;
        }
        
        if (this.useStaticId){
            if (used>0) sb.append("; ");
            sb.append("sn=").append(a.getReportingMote());
            used++;
        }
        
        if (this.useTxPower){
            if (used>0) sb.append("; ");
            sb.append("tx=").append(df.format(a.getTxpower()));
            used++;
        }
        
        if (this.useMid){
            if (used>0) sb.append("; ");
            sb.append("mid=").append(a.getMid());
            used++;
        }
        
//        sb.append(a.getTestno())
//                .append(";")
//                .append("node=")
//                .append(a.getReportingMote())
//                .append(";tx=")
//                .append(a.getTxpower());

        return sb.toString();
    }

    /**
     * HashCode of string
     * 
     * @param a
     * @return
     */
    public int getHashCodeFor(RSSI2DistInternalBuffer a){
        return this.getStringHashFor(a).hashCode();
    }

    public Set<String> getXgroup() {
        return xgroup;
    }

    public void setXgroup(Set<String> xgroup) {
        this.xgroup = xgroup;
        if (this.xgroup==null){
            this.xgroup.add("testno");
            this.xgroup.add("staticId");
            this.xgroup.add("txpower");
        }
        
        this.useTestno=xgroup.contains("testno");
        this.useMobileId=xgroup.contains("mobileId");
        this.useStaticId=xgroup.contains("staticId");
        this.useTxPower=xgroup.contains("txpower");
        this.useMid=xgroup.contains("mid");
    }

    public DecimalFormat getDf() {
        return df;
    }

    public void setDf(DecimalFormat df) {
        this.df = df;
    }
}
