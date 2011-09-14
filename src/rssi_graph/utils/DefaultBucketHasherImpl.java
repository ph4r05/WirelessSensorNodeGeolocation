/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

/**
 * Default hasher for BucketHasher
 * @author ph4r05
 */
public class DefaultBucketHasherImpl<T> implements BucketHasherInterface<T> {
    /**
     * Basic constructor
     */
    public DefaultBucketHasherImpl() {

    }

    /**
     * Get unique string representation
     * @param a
     * @return
     */
    public String getStringHashFor(T a){
        if (a==null){
            throw new IllegalArgumentException("Illegal argument passed");
        }
        
        return a.toString();
    }

    /**
     * HashCode of string
     *
     * @param a
     * @return
     */
    public int getHashCodeFor(T a){
        return a.hashCode();
    }
}
