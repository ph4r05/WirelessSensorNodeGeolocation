/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.utils;

/**
 *
 * @author ph4r05
 */
public interface TextHistogramData {
    public double getHistogramData(String identification);
    public String toStringForHistogram();
}
