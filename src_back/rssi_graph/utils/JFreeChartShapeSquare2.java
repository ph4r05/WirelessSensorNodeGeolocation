/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.awt.Polygon;

final public class JFreeChartShapeSquare2 extends Polygon
{
	public JFreeChartShapeSquare2()
	{
		super.addPoint(-12, 0);
		super.addPoint(0, -12);
		super.addPoint(12, 0);
		super.addPoint(0, 12);
	}
}

