/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi.chart;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ExtensionFileFilter;

/**
 * My modification of ChartPanel to modify PNG export (better quality for Bc. thesis)
 * @author ph4r05
 */
public class RSSIChartPanel extends ChartPanel {

    public RSSIChartPanel(JFreeChart chart) {
        super(chart);
    }

    public RSSIChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean copy, boolean save, boolean print, boolean zoom, boolean tooltips) {
        super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, copy, save, print, zoom, tooltips);
    }

    public RSSIChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
        super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save, print, zoom, tooltips);
    }

    public RSSIChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
        super(chart, properties, save, print, zoom, tooltips);
    }

    public RSSIChartPanel(JFreeChart chart, boolean useBuffer) {
        super(chart, useBuffer);
    }



     /**
     * Opens a file chooser and gives the user an opportunity to save the chart
     * in PNG format.
     *
     * @throws IOException if there is an I/O error.
     */
    @Override
    public void doSaveAs() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        ExtensionFileFilter filter = new ExtensionFileFilter(
                localizationResources.getString("PNG_Image_Files"), ".png");
        fileChooser.addChoosableFileFilter(filter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            if (isEnforceFileExtensions()) {
                if (!filename.endsWith(".png")) {
                    filename = filename + ".png";
                }
            }
//            ChartUtilities.saveChartAsPNG(new File(filename), this.getChart(),
//                    getWidth(), getHeight(), null, true, 0);
            ChartUtilities.saveChartAsPNG(new File(filename), this.getChart(),
                   (int)Math.round(getWidth()*1.0), (int)Math.round(getHeight()*1.0), null, true, 9);
        }

    }

    
}
