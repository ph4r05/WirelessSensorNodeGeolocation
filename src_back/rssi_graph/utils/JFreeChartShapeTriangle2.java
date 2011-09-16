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
public class JFreeChartShapeTriangle2 extends Polygon
{
	public JFreeChartShapeTriangle2()
	{
		super.addPoint(-3, -3);
		super.addPoint(3, -3);
		super.addPoint(0, 3);
	}
}
