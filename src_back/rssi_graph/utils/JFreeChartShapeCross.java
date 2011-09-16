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
public class JFreeChartShapeCross extends Polygon {

    public JFreeChartShapeCross() {
        super.addPoint(-6, 2);
        super.addPoint(-2, 2);
        super.addPoint(-2, 6);
        super.addPoint(2, 6);
        super.addPoint(2, 2);
        super.addPoint(6, 2);
        //
        super.addPoint(6, -2);
        super.addPoint(2, -2);
        super.addPoint(2, -6);
        super.addPoint(-2, -6);
        super.addPoint(-2, -2);
        super.addPoint(-6, -2);
    }
}
