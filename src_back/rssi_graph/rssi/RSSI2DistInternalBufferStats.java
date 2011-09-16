/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Holds statistical data about RAW data in RSSI2DistInternalBuffer
 * Useful for further processing (graph, export, curve fitting)
 * @author ph4r05
 */
public class RSSI2DistInternalBufferStats implements Cloneable {

    /**
     * Number of samples
     */
    private int n;

    /**
     * Arithmetic Mean
     */
    private double mean;

    /**
     * Median
     */
    private double median;

    /**
     * Minimum value
     */
    private double min;

    /**
     * Maximum value
     */
    private double max;

    /**
     * Standard deviation
     */
    private double stddev;

    /**
     * 1st quartile
     */
    private double q1;

    /**
     * 3rd quartile
     */
    private double q3;

    /**
     * Modified clone() override
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        RSSI2DistInternalBufferStats clone=(RSSI2DistInternalBufferStats)super.clone();

        // make the shallow copy of the object of type Department
        clone.n = this.getN();
        clone.max = this.getMax();
        clone.min = this.getMin();
        clone.mean = this.getMean();
        clone.median = this.getMedian();
        clone.stddev = this.getStddev();
        clone.q1 = this.getQ1();
        clone.q3= this.getQ3();
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
        final RSSI2DistInternalBufferStats other = (RSSI2DistInternalBufferStats) obj;
        if (this.n != other.n) {
            return false;
        }
        if (Double.doubleToLongBits(this.mean) != Double.doubleToLongBits(other.mean)) {
            return false;
        }
        if (Double.doubleToLongBits(this.median) != Double.doubleToLongBits(other.median)) {
            return false;
        }
        if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(other.min)) {
            return false;
        }
        if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(other.max)) {
            return false;
        }
        if (Double.doubleToLongBits(this.stddev) != Double.doubleToLongBits(other.stddev)) {
            return false;
        }
        if (Double.doubleToLongBits(this.q1) != Double.doubleToLongBits(other.q1)) {
            return false;
        }
        if (Double.doubleToLongBits(this.q3) != Double.doubleToLongBits(other.q3)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.n;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.mean) ^ (Double.doubleToLongBits(this.mean) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.median) ^ (Double.doubleToLongBits(this.median) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.stddev) ^ (Double.doubleToLongBits(this.stddev) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.q1) ^ (Double.doubleToLongBits(this.q1) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.q3) ^ (Double.doubleToLongBits(this.q3) >>> 32));
        return hash;
    }

    /**
     * =========================================================================
     *
     * CONSTRUCTORS
     *
     * =========================================================================
     */
    
    public RSSI2DistInternalBufferStats(int n, double mean, double median, double min, double max, double stddev, double q1, double q3) {
        this.n = n;
        this.mean = mean;
        this.median = median;
        this.min = min;
        this.max = max;
        this.stddev = stddev;
        this.q1 = q1;
        this.q3 = q3;
    }

    public RSSI2DistInternalBufferStats() {
    }

    /**
     * =========================================================================
     *
     * STATICS COMPUTATION METHODS
     *
     * =========================================================================
     */

    /**
     * Returns initialized object with computed statistical values for raw data
     * Uses Apache common library for calculation.
     * If wanted, better and abstraction clear solution is to create standalone class
     * which computes statistics from raw data.
     * 
     * @param a LinkedList
     * @return RSSI2DistInternalBufferStats
     */
     public static RSSI2DistInternalBufferStats getInstance(List<RSSI2DistInternalBufferRaw> a){
         if (a==null) {
             throw new IllegalArgumentException("Null object passed");
         }

         // init and fill apache common math library
         DescriptiveStatistics stats = new DescriptiveStatistics();

         // Add the data from the array
         Iterator<RSSI2DistInternalBufferRaw> it = a.iterator();
         for(int i = 0; it.hasNext(); i++) {
                stats.addValue(it.next().getRssi());
         }

        // Compute some statistics
        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        double median = stats.getPercentile(50);
        double q1 = stats.getPercentile(25);
        double q3 = stats.getPercentile(75);
        double min = stats.getMin();
        double max = stats.getMax();
        double stddev = stats.getStandardDeviation();
        int n = a.size();
         
         RSSI2DistInternalBufferStats result = new RSSI2DistInternalBufferStats(n, mean, median, min, max, stddev, q1, q3);
         return result;
     }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */

    public double getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getMin() {
        return min;
    }

    public int getN() {
        return n;
    }

    public double getQ1() {
        return q1;
    }

    public double getQ3() {
        return q3;
    }

    public double getStddev() {
        return stddev;
    }
}
