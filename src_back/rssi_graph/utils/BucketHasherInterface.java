/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

/**
 * BucketHasher interface
 * @author ph4r05
 */
public interface BucketHasherInterface<T> {
    public String getStringHashFor(T a);
    public int getHashCodeFor(T a);
}
