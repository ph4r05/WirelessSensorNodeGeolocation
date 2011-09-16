/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

/**
 * Smooth input stream of data according to smotthing parameter
 * @author ph4r05
 */
public class DataSmoother {

    private double alpha=0.05;

    /**
     * Return smoothed value
     *
     * @param current
     * @param nextSample
     * @return
     */
    public double getSmoothed(double current, double nextSample){
        return DataSmoother.getSmoothed(current, nextSample, alpha);
    }
    
    /**
     * Static method for smoothing
     * 
     * @param current
     * @param nextSample
     * @param alpha
     */
    public static double getSmoothed(double current, double nextSample, double alpha){
        return current + alpha * (nextSample - current);
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
}
