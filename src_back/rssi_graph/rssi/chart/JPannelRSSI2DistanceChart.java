/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPannelRSSI2DistanceChart.java
 *
 * Created on Apr 1, 2011, 3:00:04 PM
 */

package rssi_graph.rssi.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import rssi_localization.JPannelLoggerLogElement;
import rssi_localization.RSSI_graphApp;
import rssi_graph.rssi.DefaultRSSI2DistInternalBufferHasher;
import rssi_graph.rssi.RSSI2DistFunctionInterface;
import rssi_graph.rssi.RSSI2DistInternalBuffer;
import rssi_graph.utils.BucketHasher;
import rssi_graph.utils.OptionsUtils;

/**
 * Drawing RSSI data in statistical graph
 * able to draw signal propagation models
 *
 * @author ph4r05
 */
public class JPannelRSSI2DistanceChart extends javax.swing.JPanel {

    /**
     * Test buckets for sampled data
     * For this purpose 1test sample data has constant: node, txpower, testno.
     *
     * Every another test with different above mentioned attributes forms new own test.
     */
    private Map<String, ArrayList<RSSI2DistInternalBuffer>> testBuckets = null;

    /**
     * Fitted functions for testBuckets
     */
    private Map<String, ArrayList<RSSI2DistFunctionInterface>> fittedFunctions = null;

    /**
     * Main ChartPanel
     */
    JPanel jPanelDraw  = null;

    /**
     * Is chartPanel drawed?
     */
    boolean jpannelDrawed = false;

    /**
     * Graph options
     */
    Map<String, Object> options;

    /** Creates new form JPannelRSSI2DistanceChart */
    public JPannelRSSI2DistanceChart() {
        initComponents();

        // register if we can
        registerToGlobal();

        // new options
        options=new HashMap<String, Object>();
    }

    /**
     * Logging to main log window
     * @param s
     * @param type
     * @param typeString
     * @param severity
     */
    public void logToWindow(String s,int type, String typeString, int severity){
        RSSI_graphApp.getApplication().getGraphViewFrame().getjPanelLogger1().addLogEntry(s, type, typeString, severity);
    }

    /**
     * Logging to main log window, filled type and type string with module id
     * @param s
     * @param severity
     */
    public void logToWindow(String s, int severity){
        this.logToWindow(s, 145, "RSSI2DistanceChart", severity);
    }

    /**
     * Registers to global panel map
     * Cannot do in register since netBeans IDE need to instantiate this when building class
     * But couldn't because registration is only possible when main application is running
     * Thus we need to register it to global space manual somewhere in code.
     *
     */
    public final void registerToGlobal(){
        // is it possible to register?
        if (RSSI_graphApp.isAppStarted()==false) return;

        // register to base
        String registrationId = "JPannelRSSI2DistanceChart";
        if (RSSI_graphApp.getApplication().getPanelMap().containsKey(registrationId)) return;

        // now we can register
        JPanel put = RSSI_graphApp.getApplication().getPanelMap().put(registrationId, this);
    }

    /**
     * Clear whole chart
     */
    public void clear(){
        this.testBuckets = new HashMap<String, ArrayList<RSSI2DistInternalBuffer>>();
        this.fittedFunctions = new HashMap<String, ArrayList<RSSI2DistFunctionInterface>>();

        // chart redraw
        fireDataChanged();
    }

    /**
     * Should redraw graph
     */
    public void fireDataChanged(){
        // chart redraw
        // rebuild datasets
        if (jpannelDrawed==true && this.jPanelDraw!=null){
            this.remove(this.jPanelDraw);
        }

        this.jPanelDraw = this.createGraphPanel();
        this.jPanelDraw.setPreferredSize(new Dimension(660, 450));
        jPanelDraw.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanelDraw.setName("jPanelDraw"); // NOI18N

        javax.swing.GroupLayout jPanelDrawLayout = new javax.swing.GroupLayout(jPanelDraw);
        jPanelDraw.setLayout(jPanelDrawLayout);
        jPanelDrawLayout.setHorizontalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 550, Short.MAX_VALUE)
        );
        jPanelDrawLayout.setVerticalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 281, Short.MAX_VALUE)
        );
            
      
        add(jPanelDraw, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 21, -1, -1));
        jpannelDrawed=true;
        this.repaint();
        this.revalidate();
    }

    /**
     * Compute function boudnaries from already defined buckets
     * 
     * @param serieKey
     * @return
     */
    private double[] getFunctionBoudnary(String serieKey){
        double minimum=Double.POSITIVE_INFINITY;
        double maximum=Double.NEGATIVE_INFINITY;

        // get minimum and maximum from corresponding data samples (distance)
        ArrayList<RSSI2DistInternalBuffer> tmpBuffs = this.testBuckets.get(serieKey);
        Iterator<RSSI2DistInternalBuffer> itBuff = tmpBuffs.iterator();
        while(itBuff.hasNext()){
            RSSI2DistInternalBuffer tmpbuff = itBuff.next();
            double curDistance = tmpbuff.getXvalue();
            if (curDistance > maximum) maximum = curDistance;
            if (curDistance < minimum) minimum = curDistance;
        }

        // always use 20 samples in one distance unit
        double samplesCount = Math.ceil((maximum - minimum) * 20);
        double[] tmpResult = new double[3];
        tmpResult[0]=minimum;
        tmpResult[1]=maximum;
        tmpResult[2]=samplesCount;

        return tmpResult; //new double[3] {minimum,maximum,samplesCount};
    }

    /**
     * Creates dataset for function by sampling it.
     * Bounds could be passed as 3rd parameter or calculated automatically from
     * existing samples data
     *
     * @param serieKey
     * @param i
     * @return
     */
    private XYDataset createDatasetFunctions(String serieKey, int i, double[] boudnaries){
        // lookup in map
        if (!this.testBuckets.containsKey(serieKey)){
            this.logToWindow("Series with serieKey [" + serieKey + "] not found", JPannelLoggerLogElement.SEVERITY_WARNING);
            return null;
        }

        // in fitted function as well
        if (!this.fittedFunctions.containsKey(serieKey)){
            this.logToWindow("Series with serieKey [" + serieKey + "] not found in fitted functions", JPannelLoggerLogElement.SEVERITY_WARNING);
            return null;
        }

        // retrieve current set
        ArrayList<RSSI2DistFunctionInterface> tmpSerieSet = this.fittedFunctions.get(serieKey);
        if (tmpSerieSet==null){
            this.logToWindow("Series with serieKey [" + serieKey + "] found with null container", JPannelLoggerLogElement.SEVERITY_WARNING);
            throw new IllegalStateException("Retrieved serie is null");
        }

        XYDataset resultDataset = null;

        // fill with data
        Iterator<RSSI2DistFunctionInterface> it = tmpSerieSet.iterator();
        for(int j=0; it.hasNext(); j++){
            if (j!=i) continue;
            RSSI2DistFunctionInterface tmpFunction = it.next();

            // determine boudnaries
            if (boudnaries==null){
                // determine my own
                boudnaries = getFunctionBoudnary(serieKey);
                if (boudnaries[0] >= boudnaries[1] || boudnaries[2]<=1){
                    this.logToWindow("Cannot draw function in single point [" + serieKey + "]", JPannelLoggerLogElement.SEVERITY_WARNING);
                    continue;
                }
                resultDataset = DatasetUtilities.sampleFunction2D(tmpFunction, boudnaries[0], boudnaries[1], (int)boudnaries[2], serieKey);
            }
            else{
                if (boudnaries[0] >= boudnaries[1] || boudnaries[2]<=1){
                    this.logToWindow("Cannot draw function in single point [" + serieKey + "]", JPannelLoggerLogElement.SEVERITY_WARNING);
                    continue;
                }
                // use given boudnaries
                resultDataset = DatasetUtilities.sampleFunction2D(tmpFunction, boudnaries[0], boudnaries[1], (int)boudnaries[2], serieKey);
            }
        }

        return resultDataset;
    }

    /**
     * Creates default data set for given test according to bucket identificator
     * Returns statistical dataset.
     *
     * @return
     */
    private synchronized Dataset createDataset(String serieKey){
        Dataset dataset = null;
        
        // return correct dataset according to parameter interpretation
        OptionsUtils.setDefaultOption(options, "ParameterInterpretation", 0);
        int paramInterpretation = (Integer) OptionsUtils.getOption(options, "ParameterInterpretation");
        
        if (paramInterpretation==0 && serieKey==null){
            this.logToWindow("Null serie key given, unexpected here", JPannelLoggerLogElement.SEVERITY_WARNING);
            return null;
        }
       
        if (paramInterpretation==0){
            // lookup in map
            if (!this.testBuckets.containsKey(serieKey)){
                this.logToWindow("Series with serieKey [" + serieKey + "] not found", JPannelLoggerLogElement.SEVERITY_WARNING);
                return null;
            }

            // retrieve current set
            ArrayList<RSSI2DistInternalBuffer> tmpSerieSet = this.testBuckets.get(serieKey);
            if (tmpSerieSet==null){
                this.logToWindow("Series with serieKey [" + serieKey + "] found with null container", JPannelLoggerLogElement.SEVERITY_WARNING);
                throw new IllegalStateException("Retrieved serie is null");
            }
            
            // create new dataset
            dataset = new RSSIBoxAndWhiskerXYDataset(serieKey);
            final RSSIBoxAndWhiskerXYDataset myDataset = (RSSIBoxAndWhiskerXYDataset) dataset;

            // fill with data
            Iterator<RSSI2DistInternalBuffer> it = tmpSerieSet.iterator();
            while(it.hasNext()){
                RSSI2DistInternalBuffer tmpBuffer = it.next();
                myDataset.add(
                        tmpBuffer.getXvalue(),
                        BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(tmpBuffer.exportData()));
            }
        } else if(paramInterpretation==1 || paramInterpretation==2) {            
            // Common dataset for category chart.
            // Category charts needs all datasets in one dataset object to correctly draw
            // chart under some circumstances.
            //
            // When using one dataset per data chart is drawn incorrectly since chart does not
            // correctly determines number of used categories what leads to  misplacement of 
            // chart artifacts on chart panel (one dataset doesn't know about other).
           
            boolean sortXvalues = false;
            OptionsUtils.setDefaultOption(options, "SortXvalues", new Boolean(false));
            sortXvalues = (Boolean) OptionsUtils.getOption(options, "SortXvalues");
            
            // create new dataset
            dataset = new RSSIBoxAndWhiskerCategoryDataset();
            final RSSIBoxAndWhiskerCategoryDataset myDataset = (RSSIBoxAndWhiskerCategoryDataset) dataset;
            
            // create sorted dataSet keys to assign same color to same data set everytime
            ArrayList<String> dataSeriesKeysSorted = new ArrayList<String>();
            
            // if serie key is null, return all data sets
            // otherwise add single serie key if exists
            // otherwise return null
            if (serieKey==null){
                dataSeriesKeysSorted.addAll(this.testBuckets.keySet());
            } else if (this.testBuckets.containsKey(serieKey)){
                dataSeriesKeysSorted.add(serieKey);
            } else {
                this.logToWindow("Series with serieKey [" + serieKey + "] not found", JPannelLoggerLogElement.SEVERITY_WARNING);
                return null;
            }
            
            // sort now
            Collections.sort(dataSeriesKeysSorted, new serieComparator());
            Iterator<String> selectedKeys = dataSeriesKeysSorted.iterator();
            while(selectedKeys.hasNext()){
                String curKey = selectedKeys.next();
                
                // get data series referenced by current serie key
                ArrayList<RSSI2DistInternalBuffer> tmpSerieSet = this.testBuckets.get(curKey);
                
                // data list for all values in dataset
                List dataValues = new LinkedList();
                double xvalue=0;
                
                // sort according to Xvalue?
                if (sortXvalues){
                    Collections.sort(tmpSerieSet, new XvalueComparator());
                }
                
                // fill with data
                Iterator<RSSI2DistInternalBuffer> it = tmpSerieSet.iterator();
                while(it.hasNext()){
                    RSSI2DistInternalBuffer tmpBuffer = it.next();
                    if(tmpBuffer==null) continue;
                    
                    List<Integer> exportData = tmpBuffer.exportData();
                    if (exportData==null) continue;
                    
                    xvalue = tmpBuffer.getXvalue();
                    dataValues.addAll(exportData);
                    
                    // add only once not to duplicate entries in datasets
                    myDataset.add(
                        BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(exportData),
                        curKey,
                        xvalue);
                }
                
                
            }
        }

        return dataset;
    }
    
    /**
     * Comparator class to compare serie keys. Used for sorting.
     */
    protected class serieComparator implements Comparator<String>{
        public int compare(String o1, String o2) {
            if (o1==null) return 1;
            if (o2==null) return -1;
            return o1.compareToIgnoreCase(o2);
        }
        
    } 
    
    protected class XvalueComparator implements Comparator<RSSI2DistInternalBuffer>{
        public int compare(RSSI2DistInternalBuffer o1, RSSI2DistInternalBuffer o2) {
            if (o1==null) return 1;
            if (o2==null) return -1;
            
            if (o1.getXvalue() == o2.getXvalue()) return 0;
            return o1.getXvalue() < o2.getXvalue() ? -1:1;
        }
    } 

    /**
     * Main method responsible for drawing JFreeChart graph for
     * all loaded RSSI2Dist, Function graphs.
     *
     * Loads all data need-to-be displayed and displays it in single plot.
     * Visual look of chart is set here.
     * 
     * @return JPanel
     */
    public JPanel createGraphPanel() {
        // iterate over map
        if (this.testBuckets==null){
            this.logToWindow("Cannot draw, no data here", JPannelLoggerLogElement.SEVERITY_WARNING);
            return null;
        }

        // chart init
        JFreeChart jfreechart = null;

        // axis
        NumberAxis numberaxis = new NumberAxis("RSSI");
        Axis numberXaxis = null;

        // default options
        OptionsUtils.setDefaultOption(options, "ShowQuartiles", Boolean.valueOf(true));
        OptionsUtils.setDefaultOption(options, "ShowMinMax", Boolean.valueOf(true));
        OptionsUtils.setDefaultOption(options, "ShowOutliers", Boolean.valueOf(false));
        OptionsUtils.setDefaultOption(options, "ChartFillBox", Boolean.valueOf(false));
        OptionsUtils.setDefaultOption(options, "ParameterInterpretation", 0);

        // graph details
        boolean showQuartiles = (Boolean) OptionsUtils.getOption(options, "ShowQuartiles");
        boolean showMinMax = (Boolean) OptionsUtils.getOption(options, "ShowMinMax");
        boolean showOutliers = (Boolean) OptionsUtils.getOption(options, "ShowOutliers");
        boolean chartFillBox = (Boolean) OptionsUtils.getOption(options, "ChartFillBox");

        // axis
        int paramInterpretation = (Integer) OptionsUtils.getOption(options, "ParameterInterpretation");
        if (paramInterpretation==0){
            numberXaxis = new NumberAxis("Distance [m]");
        } else if(paramInterpretation==1) {
            numberXaxis = new CategoryAxis("Angle [Deg]");
        } else if(paramInterpretation==2) {
            numberXaxis = new CategoryAxis("Step");
        }

        // get all entries
        Set<String> keySet =this.testBuckets.keySet();

        //
        // Take parameter interpretation into account
        //
        // Parameter = distance
        if (paramInterpretation==0){
            // prepare plot
            XYPlot xyplot = null;

            // sort             
            // create sorted dataSet keys to assign same color to same data set everytime
            ArrayList<String> dataSeriesKeysSorted = new ArrayList<String>(keySet);
            
            // sort now
            Collections.sort(dataSeriesKeysSorted, new serieComparator());
            Iterator<String> it = dataSeriesKeysSorted.iterator();
            
            int datasetCounter=0;
            for(int i=0; it.hasNext(); i++){
                String curKey = it.next();
                
                // assertion
                if (!keySet.contains(curKey)) continue;

                // create render
                RSSIXYBoxAndWhiskerRenderer xyboxandwhiskerrenderer = new RSSIXYBoxAndWhiskerRenderer();
                xyboxandwhiskerrenderer.setMinMaxVisible(showMinMax);
                xyboxandwhiskerrenderer.setQuartilsVisible(showQuartiles);
                xyboxandwhiskerrenderer.setOutliersVisible(showOutliers);
                
                // init xy plot if null
                if (xyplot == null){
                    xyplot = new XYPlot((BoxAndWhiskerXYDataset) this.createDataset(curKey), (ValueAxis) numberXaxis, numberaxis, xyboxandwhiskerrenderer);
                    ++datasetCounter;
                }
                else{
                    // add as secondary renders & datasets
                    xyplot.setDataset(datasetCounter, (BoxAndWhiskerXYDataset) this.createDataset(curKey));
                    xyplot.setRenderer(datasetCounter, xyboxandwhiskerrenderer);
                    ++datasetCounter;
                }

                xyboxandwhiskerrenderer.setFillBox(chartFillBox);
                xyboxandwhiskerrenderer.setBoxWidth(xyboxandwhiskerrenderer.getBoxWidth()*2/5.0);
                Paint p = xyboxandwhiskerrenderer.lookupSeriesPaint(0);

                if (p!=null){
                    Paint newPaint = p;

                    // modify paint, if paint is color && box is filled
                    // need to change color a little to change
                    if (p instanceof Color && chartFillBox){
                        final Color tmpColor = (Color) p;
                        newPaint = new Color(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 125);
                    }

                    xyboxandwhiskerrenderer.setArtifactPaint(p);
                    xyboxandwhiskerrenderer.setBoxPaint(newPaint);
                }

                // look for functions now
                if (this.fittedFunctions != null
                        && !this.fittedFunctions.isEmpty()
                        && this.fittedFunctions.containsKey(curKey)){
                    ArrayList<RSSI2DistFunctionInterface> tmpFunctions = this.fittedFunctions.get(curKey);
                    for(int j=0, cnJ=tmpFunctions.size(); j<cnJ; j++){
                        // create render
                        SamplingXYLineRenderer tmpRenderer = new SamplingXYLineRenderer();
                        tmpRenderer.setBaseFillPaint(p);
                        tmpRenderer.setSeriesFillPaint(0, p);
                        tmpRenderer.setSeriesPaint(0, p);

                        // do not display this data serie in legend
                        tmpRenderer.setSeriesVisibleInLegend(0, false);

                        // create dataset
                        XYDataset tmpXYDataset = this.createDatasetFunctions(curKey, j, null);

                        // add to plot
                        xyplot.setDataset(datasetCounter, tmpXYDataset);
                        xyplot.setRenderer(datasetCounter, tmpRenderer);
                        ++datasetCounter;
                    }
                }
            }

            // if no data, draw empty graph
            if (xyplot==null){
                xyplot = new XYPlot();
            }

            xyplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            xyplot.setDomainPannable(true);
            xyplot.setRangePannable(true);

            jfreechart = new JFreeChart("Distance -> RSSI plot", xyplot);
            jfreechart.setBackgroundPaint(Color.white);

            xyplot.setOrientation(PlotOrientation.VERTICAL);
            xyplot.setBackgroundPaint(Color.lightGray);
            xyplot.setDomainGridlinePaint(Color.white);
            xyplot.setDomainGridlinesVisible(true);
            xyplot.setRangeGridlinePaint(Color.white);


        // parameter = angle
        } else if(paramInterpretation==1 || paramInterpretation==2) {
            // prepare plot
            CategoryPlot plot = null;
            
            // sort             
            // create sorted dataSet keys to assign same color to same data set everytime
            ArrayList<String> dataSeriesKeysSorted = new ArrayList<String>(keySet);
            
            // sort now
            Collections.sort(dataSeriesKeysSorted, new serieComparator());
            Iterator<String> it = dataSeriesKeysSorted.iterator();
            
            DefaultBoxAndWhiskerCategoryDataset totalCommonDataSet = 
                            (DefaultBoxAndWhiskerCategoryDataset) this.createDataset(null);
            
             // create per-dataset render
                RSSIBoxAndWhiskerRenderer boxandwhiskerrenderer = new RSSIBoxAndWhiskerRenderer();
                boxandwhiskerrenderer.setMinMaxVisible(showMinMax);
                boxandwhiskerrenderer.setQuartilsVisible(showQuartiles);
                boxandwhiskerrenderer.setOutliersVisible(showOutliers);
                boxandwhiskerrenderer.setFillBox(chartFillBox);
                boxandwhiskerrenderer.setSeparateCategoriesOnRangeAxis(false);
                boxandwhiskerrenderer.setBoxFillAlpha(100);
                plot = new CategoryPlot(totalCommonDataSet, (CategoryAxis) numberXaxis, numberaxis, boxandwhiskerrenderer);
                
                boxandwhiskerrenderer.setMaximumBarWidth(0.2);
            
//            int datasetCounter=0;
//            for(int i=0; it.hasNext(); i++){
//                String curKey = it.next();
//                
//                // assertion
//                if (!keySet.contains(curKey)) continue;
//                
//                // common dataset
//                DefaultBoxAndWhiskerCategoryDataset commonDataSet = 
//                        (DefaultBoxAndWhiskerCategoryDataset) this.createDataset(curKey);
//                        //totalCommonDataSet;
//                
//                // create per-dataset render
//                RSSIBoxAndWhiskerRenderer boxandwhiskerrenderer = new RSSIBoxAndWhiskerRenderer();
//                boxandwhiskerrenderer.setMinMaxVisible(showMinMax);
//                boxandwhiskerrenderer.setQuartilsVisible(showQuartiles);
//                boxandwhiskerrenderer.setOutliersVisible(showOutliers);
//                
//
//                // init xy plot if null
//                if (plot == null){
//                    plot = new CategoryPlot(commonDataSet, (CategoryAxis) numberXaxis, numberaxis, boxandwhiskerrenderer);
//                    ++datasetCounter;
//                }
//                else{
//                    // add as secondary renders & datasets
//                    plot.setDataset(datasetCounter, commonDataSet);
//                    plot.setRenderer(datasetCounter, boxandwhiskerrenderer);
//                    ++datasetCounter;
//                }
//
//                boxandwhiskerrenderer.setFillBox(chartFillBox);
//                Paint p = boxandwhiskerrenderer.lookupSeriesPaint(0);
//
//                if (p!=null){
//                    Paint newPaint = p;
//
//                    // modify paint, if paint is color && box is filled
//                    // need to change color a little to change
//                    if (p instanceof Color && chartFillBox){
//                        final Color tmpColor = (Color) p;
//                        newPaint = new Color(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 125);
//                    }
//
//                    boxandwhiskerrenderer.setArtifactPaint(p);
//                    boxandwhiskerrenderer.setBoxPaint(newPaint);
//                    boxandwhiskerrenderer.setMaximumBarWidth(0.1);
//                }
//            }

            // if no data, draw empty graph
            if (plot == null){
                    plot = new CategoryPlot();
            }

            if(paramInterpretation==1){
                jfreechart = new JFreeChart("Angle -> RSSI plot", plot);
            } else if (paramInterpretation==2){
                jfreechart = new JFreeChart("Step -> RSSI plot", plot);
            }
            
            jfreechart.setBackgroundPaint(Color.white);

            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            plot.setRangePannable(true);
            plot.setOrientation(PlotOrientation.VERTICAL);
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinePaint(Color.white);
            plot.setDomainGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.white);
            
        // end of parameter \in {angle,step}
        } else {
            throw new IllegalArgumentException("ParameterInterpretation unknown");
        }
        
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        ChartPanel resultPanel = new RSSIChartPanel(jfreechart);
        resultPanel.setMouseZoomable(true, true);
        resultPanel.setRangeZoomable(true);
        resultPanel.setDomainZoomable(true);
        resultPanel.setMouseWheelEnabled(true);
        return resultPanel;
    }

    /**
     * Merge given map structure with graph storage.
     * Duplicates are ignored
     * 
     * @param a
     */
    public void addRSSIData(Map<String, ArrayList<RSSI2DistInternalBuffer>> a){
        // init testBucket if null
        if (this.testBuckets==null){
            this.testBuckets = new HashMap<String, ArrayList<RSSI2DistInternalBuffer>>();
        }

        if (a==null || a.isEmpty()){
            this.logToWindow("Nothing to add, empty parameter", JPannelLoggerLogElement.SEVERITY_WARNING);
            return;
        }

        Set<String> keySet = a.keySet();
        Iterator<String> it = keySet.iterator();
        while(it.hasNext()){
            this.addRSSIData(a.get(it.next()));
        }
    }

    /**
     * Add given list to appropriate bucket in graph storage.
     * Duplicates are ignored (based on .equals()).
     *
     * @BUG: assuming same bucketing as source, does not have to hold always
     * Could be problem when merging with fitted functions
     * @param a
     */
    public void addRSSIData(List<RSSI2DistInternalBuffer> a){
        // init testBucket if null
        if (this.testBuckets==null){
            this.testBuckets = new HashMap<String, ArrayList<RSSI2DistInternalBuffer>>();
        }
        
        OptionsUtils.setDefaultOption(options, "XvalueGroup", null);
        Set<String> xvaluegroup = (Set<String>) OptionsUtils.getOption(options, "XvalueGroup");
        if (xvaluegroup==null || xvaluegroup.isEmpty()){
            xvaluegroup=null;
        }

        // buckets object
        DefaultRSSI2DistInternalBufferHasher hasher = new DefaultRSSI2DistInternalBufferHasher();
        hasher.setXgroup(xvaluegroup);
        
        BucketHasher<RSSI2DistInternalBuffer> bhasher = new BucketHasher<RSSI2DistInternalBuffer>(hasher);

        // categorize to buckets with existing map
        this.testBuckets = bhasher.makeBuckets(a, this.testBuckets, true);
        this.logToWindow("Buckets formed; Size=" + this.testBuckets.size(), JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Add curve for given test to graph
     * Do not add duplicates (based on .equals())
     * 
     * @param a
     */
    public void addCurve(Map<String, ArrayList<RSSI2DistFunctionInterface>> a){
        // init fittedFunctions if null
        if (this.fittedFunctions==null){
            this.fittedFunctions = new HashMap<String, ArrayList<RSSI2DistFunctionInterface>>();
        }

        // test on empty input
        if (a==null || a.isEmpty()){
            return;
        }

        // merge with current
        // iterate ove new map and try to add if currently exists in current
        // otherwise create new bucket
        Set<String> keySet = a.keySet();
        Iterator<String> it = keySet.iterator();

        for(int i=0; it.hasNext(); i++){
            String curKey = it.next();

            // if old map cointains arrayList with given key, load it
            // otherwise new is created and data are puted here, finaly,
            // map is updated with this new list
            ArrayList<RSSI2DistFunctionInterface> tmpList =
                    this.fittedFunctions.containsKey(curKey) ?
                        this.fittedFunctions.get(curKey) :
                        new ArrayList<RSSI2DistFunctionInterface>();

            ArrayList<RSSI2DistFunctionInterface> tmpNewList = a.get(curKey);
            if (tmpNewList==null || !(tmpNewList instanceof List)){
                this.logToWindow("Null pointer exception, tmpNewList, i="+i+";", JPannelLoggerLogElement.SEVERITY_WARNING);
                continue;
            }

            // check for duplicity
            for(int j=0, cnJ=tmpNewList.size(); j<cnJ; j++){
                RSSI2DistFunctionInterface tmpNewFunction = tmpNewList.get(j);
                if (tmpNewFunction==null || !(tmpNewFunction instanceof RSSI2DistFunctionInterface)){
                    this.logToWindow("Null pointer exception, i="+i+"; j="+j, JPannelLoggerLogElement.SEVERITY_WARNING);
                    continue;
                }

                // duplicity check/scan
                boolean isDuplicate=false;
                Iterator<RSSI2DistFunctionInterface> itTmpList = tmpList.iterator();
                while(itTmpList.hasNext()){
                    RSSI2DistFunctionInterface tmpOldFunction = itTmpList.next();
                    if (tmpOldFunction==null || !(tmpOldFunction instanceof RSSI2DistFunctionInterface)) continue;
                    
                    if (tmpNewFunction.equals(tmpOldFunction)){
                        isDuplicate=true;
                        break;
                    }
                }

                if (isDuplicate==false){
                    tmpList.add(tmpNewFunction);
                }
            }

            // update map
            this.fittedFunctions.put(curKey, tmpList);
        }

        this.logToWindow("Fitted functions map updated; Size=" + this.fittedFunctions.size(), JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * hides info label from panel
     * (label displayed before graph initialization)
     */
    public void hideInfoLabel(){
        this.jLabel1.setVisible(false);
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getResourceMap(JPannelRSSI2DistanceChart.class);
        setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("Form.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 640, 120));
    }// </editor-fold>//GEN-END:initComponents

    /**
     * =========================================================================
     *
     * AUTO-GENERATED CODE
     *
     * =========================================================================
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}
