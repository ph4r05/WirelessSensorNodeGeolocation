/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.awt.geom.Ellipse2D;

/**
 *
 * @author ph4r05
 */
public class JFreeChartShapeCircle  extends Ellipse2D.Double {
    public JFreeChartShapeCircle() {
        super(-10, -10, 20, 20);
    }
}
