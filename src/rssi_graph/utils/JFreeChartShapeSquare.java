/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

// used for jgraph, we introduce these classes to distinguish the lines in

import java.awt.Polygon;
import java.awt.geom.Ellipse2D;

// jgraph when printing in black nad white
final public class JFreeChartShapeSquare extends Polygon
{
	public JFreeChartShapeSquare()
	{
		super.addPoint(-10, -10);
		super.addPoint(10, -10);
		super.addPoint(10, 10);
		super.addPoint(-10, 10);
	}
}

