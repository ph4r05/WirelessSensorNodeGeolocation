/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.localization;

/**
 * Coordinates for given positions
 * @author ph4r05
 */
public class CoordinateRecord {
    public double x;
    public double y;
    public double z;

    public CoordinateRecord() {
    }    

    public CoordinateRecord(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public CoordinateRecord(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
    /**
     * Returns Euclid distance a from b
     * @param a
     * @param b
     * @return 
     */
    public static double getEuclidDistance(CoordinateRecord a, CoordinateRecord b){
        if (a==null || b==null){
            throw new NullPointerException("Input argument is null");
        }
        
        double xerror = LocalizationEstimate.getXError(a, b);
        double yerror = LocalizationEstimate.getYError(a, b);
        return Math.sqrt(xerror*xerror+yerror*yerror);
    }
    
    /**
     * Returns Euclid distance a from b
     * @param a
     * @return 
     */
    public double getEuclidDistanceFrom(CoordinateRecord a){
        return CoordinateRecord.getEuclidDistance(this, a);
    }
}
