/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_localization.localization;

/**
 *
 * @author ph4r05
 */
public class CoordinateRecord {
    public double x;
    public double y;
    public double z;

    public CoordinateRecord() {
    }

//    public double prec_x;
//    public double prec_y;
//    public double prec_z;

    

    public CoordinateRecord(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public CoordinateRecord(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

//    public double getPrec_x() {
//        return prec_x;
//    }
//
//    public void setPrec_x(double prec_x) {
//        this.prec_x = prec_x;
//    }
//
//    public double getPrec_y() {
//        return prec_y;
//    }
//
//    public void setPrec_y(double prec_y) {
//        this.prec_y = prec_y;
//    }
//
//    public double getPrec_z() {
//        return prec_z;
//    }
//
//    public void setPrec_z(double prec_z) {
//        this.prec_z = prec_z;
//    }

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
}
