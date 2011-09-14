/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.awt.Polygon;

/**
 *
 * @author ph4r05
 */
public class JFreeChartShapeCross2 extends Polygon {

    public JFreeChartShapeCross2() {
        super.addPoint(-12, 12);
        super.addPoint(-8, 12);
        super.addPoint(0, 4);
        super.addPoint(8, 12);
        super.addPoint(12, 12);
        //
        super.addPoint(4, 0);
        super.addPoint(12, -12);
        super.addPoint(8, -12);
        super.addPoint(0, -4);
        //
        super.addPoint(-8, -12);
        super.addPoint(-12, -12);
        super.addPoint(-4, 0);
    }
}