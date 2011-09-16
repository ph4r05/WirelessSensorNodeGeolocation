/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import rssi_graph.rssi.RSSI2DistInternalBuffer;

/**
 * General purpose utility object to categorize objects according to some
 * hasher given to categories/buckets.
 * 
 * Hasher returns String for every given object, which server as key for map.
 *
 * makeBuckets works like this:
 * Map contains String->ArrayList. All objects in ArrayList has common hashString (determined by
 * hasher), what is in String, as map key.
 *
 * buckets are lists of objects with common properties (hashString, determined by hasher)
 *
 * @author ph4r05
 */
public class BucketHasher<T> {
    private BucketHasherInterface<T> hasher = null;

    /**
     * No hasher given - use default
     */
    public BucketHasher() {
        this.hasher = (BucketHasherInterface<T>) new DefaultBucketHasherImpl();
    }

    public BucketHasher(BucketHasherInterface<T> hasher) {
        this.hasher = hasher;
    }

    /**
     * Bucketing with duplicity check
     * @param a
     * @param resultMap
     * @return
     */
    public Map<String, ArrayList<T>> makeBuckets(Collection<T> a, Map<String, ArrayList<T>> resultMap){
        return this.makeBuckets(a, resultMap, false);
    }

    /**
     * Sort to buckets with hasher
     * @param a
     * @return
     */
    public Map<String, ArrayList<T>> makeBuckets(Collection<T> a, Map<String, ArrayList<T>> resultMap, boolean doNotAddDuplicates){
        // arguments checking
        if (a==null){
            throw new IllegalArgumentException("Null collection passed");
        }

        if (this.hasher==null){
            throw new IllegalStateException("Hasher object is null");
        }

        // null check for map
        if (resultMap == null){
            throw new IllegalArgumentException("Passed map is null");
        }

        Iterator<T> it = a.iterator();
        while(it.hasNext()){
            T tmp = it.next();
            String hashString = this.hasher.getStringHashFor(tmp);

            // is on given position something or not?
            // if true, retreive it and append, if false, create new empty and append;
            ArrayList<T> tmpList = resultMap.containsKey(hashString) ?
                                    resultMap.get(hashString)
                                    : new ArrayList<T>();

            // scan for duplicities
            // bad complexity :/
            boolean canAddElement = true;
            if (doNotAddDuplicates && tmpList.size()>0){
                // scan tmpList for equal object
                Iterator<T> tmpIt = tmpList.iterator();
                while(tmpIt.hasNext()){
                    T tmpObject = tmpIt.next();
                    if (tmp.equals(tmpObject)){
                        canAddElement=false;
                        break;
                    }
                }
            }

            // duplicity check fail?
            if (canAddElement==false) continue;

            tmpList.add(tmp);
            resultMap.put(hashString, tmpList);
        }

        return resultMap;
    }

    /**
     * Sort to buckets with hasher, no map given, default empty is created
     * @param a
     * @return
     */
    public Map<String, ArrayList<T>> makeBuckets(Collection<T> a){
        if (a==null){
            throw new IllegalArgumentException("Null collection passed");
        }

        if (this.hasher==null){
            throw new IllegalStateException("Hasher object is null");
        }

        Map<String, ArrayList<T>> resultMap = new HashMap<String, ArrayList<T>>(a.size()/10);
        return this.makeBuckets(a, resultMap, false);
    }
    
    public BucketHasherInterface getHasher() {
        return hasher;
    }

    public void setHasher(BucketHasherInterface hasher) {
        this.hasher = hasher;
    }
}
