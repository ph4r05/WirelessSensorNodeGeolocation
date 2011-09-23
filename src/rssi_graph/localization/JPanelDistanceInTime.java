/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.localization;

import com.csvreader.CsvWriter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.nodeRegister.MobileNode;
import rssi_graph.RSSI_graphApp;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.rssi.RSSI2DistFunctionInterface;

/**
 *
 * @author ph4r05
 */
public class JPanelDistanceInTime extends javax.swing.JPanel implements ActionListener {

    private Timer grabTimer = null;
    private WorkerLocalization worker = null;
    private Map<String, TimeSeries> timeSeries = null;
    private TimeSeriesCollection timeDataSets = null;
    private boolean redrawGraphNeeded=false;
    private boolean jpannelDrawed=false;
    //private Timer initTimer

    /**
     * Main ChartPanel
     */
     private ChartPanel chartPanel=null;

    /** Creates new form JPanelRSSIInTime */
    public JPanelDistanceInTime() {
        initComponents();

        // center label
        this.jLabelpreGraph.setPreferredSize(new Dimension(this.jPanelDraw.getWidth(), this.jPanelDraw.getHeight()));
        //this.jLabelpreGraph.setl
    }

    public final void initThis(){
        this.grabTimer = new Timer(this.lookupSampleInterval(), (ActionListener) this);
        this.grabTimer.setActionCommand("grabTimer");

        this.worker = (WorkerLocalization) RSSI_graphApp.getApplication().getWorker(0);
        this.jButtonResetGraphActionPerformed(null);

        // do this only once
        this.drawGraph();
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
        this.logToWindow(s, 145, "Localization,DistanceinTime", severity);
    }

    /**
     * extract time interval from text field
     * @return
     */
    public int lookupSampleInterval(){
        return Integer.parseInt(this.jSampleInterval.getText());
    }

    public int lookupSampleTimeWindow(){
        try{
            return Integer.parseInt(this.jTextFieldTimeWindow.getText());
        } catch(java.lang.NumberFormatException ex){
            return 1;
        }
    }

    protected void updateValues(){
        // grab data from worker, then
        Set<MobileNode> activeMobileNodes = this.worker.getActiveMobileNodes();
        if (activeMobileNodes==null){
            this.logToWindow("Null active mobile nodes!", JPannelLoggerLogElement.SEVERITY_ERROR);
            this.grabTimer.stop();
            return;
        }

        redrawGraphNeeded=false;
        final Millisecond now = new Millisecond();

        // get iterator and iterate over set
        Iterator<MobileNode> it = activeMobileNodes.iterator();
        for(int i=0; it.hasNext(); i++){
            // get mobile node
            MobileNode mn = it.next();
            Map<Integer, Double> floatingMean = mn.getFloatingMean();
            Iterator<Integer> itAnchor = floatingMean.keySet().iterator();

            for(int j=0; itAnchor.hasNext(); j++){
                Integer curAnchor = itAnchor.next();
                GenericNode anchorNode = this.worker.getNodeRegister().getNode(curAnchor);
                RSSI2DistFunctionInterface distanceFunction = anchorNode.getDistanceFunction();
                if (distanceFunction==null) continue;

                // generate key for time serie
                String curTimeSerieKey = "T"+mn.getMobile_nodeID()+";R"+curAnchor;
                TimeSeries ts = null;

                // does time serie exist?
                if (!this.timeSeries.containsKey(curTimeSerieKey)){
                    redrawGraphNeeded=true;
                    ts = new TimeSeries(curTimeSerieKey);
                    this.timeDataSets.addSeries(ts);
                }
                else {
                    ts = this.timeSeries.get(curTimeSerieKey);
                }

                // age items in time series
                ts.removeAgedItems(300000, false);
                ts.add(now, distanceFunction.getDistanceFromRSSI(floatingMean.get(curAnchor)));
                this.timeSeries.put(curTimeSerieKey, ts);
            }
        }

        // redraw graph if needed (new time serie added/removed) for example
        if (redrawGraphNeeded){
            final JFreeChart chart = createChart(this.timeDataSets);
            chart.getXYPlot().getDomainAxis().setFixedAutoRange(lookupSampleTimeWindow() * 1000.0);
            this.chartPanel.setChart(chart);
            this.chartPanel.repaint();
            this.chartPanel.revalidate();
        }
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
     * @return A sample chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            "Distance in time",
            "Time",
            "Distance",
            dataset,
            true,
            true,
            false
        );
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(lookupSampleTimeWindow() * 1000.0);  // 120 seconds
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 200.0);
        return result;
    }


    protected void drawGraph(){
        final JFreeChart chart = createChart(this.timeDataSets);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.setMouseZoomable(true, true);
        this.chartPanel.setDomainZoomable(true);
        this.chartPanel.setMouseWheelEnabled(true);
        this.chartPanel.getChart().getXYPlot().setDomainPannable(true);
        this.chartPanel.getChart().getXYPlot().setRangePannable(true);
        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 320));

//        this.chartPanel.setName("jPanelDraw");
//        this.jPanelDraw=this.chartPanel;
//        this.jPanelDraw.validate();
//        this.jPanelDraw.repaint();;
        //this.
        // first, remove component
        this.remove(this.jPanelDraw);

        /**
         * code borrowed from auto-generated section
         */
        this.chartPanel.setName("jPanelDraw"); // NOI18N

        javax.swing.GroupLayout jPanelDrawLayout = new javax.swing.GroupLayout(this.chartPanel);
        this.chartPanel.setLayout(jPanelDrawLayout);
        jPanelDrawLayout.setHorizontalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 607, Short.MAX_VALUE)
        );
        jPanelDrawLayout.setVerticalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 308, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(this.chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(this.chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        // repaint and revalidate to redraw
        this.repaint();
        this.revalidate();
    }

    public void actionPerformed(ActionEvent e) {
        if ("grabTimer".equals(e.getActionCommand())){
            this.updateValues();
        }
        else{
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jToggleGrab = new javax.swing.JToggleButton();
        jSampleInterval = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButtonResetGraph = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldTimeWindow = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanelDraw = new javax.swing.JPanel();
        jLabelpreGraph = new javax.swing.JLabel();

        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(631, 396));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JPanelRSSIInTime.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jToggleGrab.setText(resourceMap.getString("jToggleGrab.text")); // NOI18N
        jToggleGrab.setName("jToggleGrab"); // NOI18N
        jToggleGrab.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleGrabItemStateChanged(evt);
            }
        });

        jSampleInterval.setText(resourceMap.getString("jSampleInterval.text")); // NOI18N
        jSampleInterval.setName("jSampleInterval"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jButtonResetGraph.setText(resourceMap.getString("jButtonResetGraph.text")); // NOI18N
        jButtonResetGraph.setName("jButtonResetGraph"); // NOI18N
        jButtonResetGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetGraphActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldTimeWindow.setText(resourceMap.getString("jTextFieldTimeWindow.text")); // NOI18N
        jTextFieldTimeWindow.setName("jTextFieldTimeWindow"); // NOI18N
        jTextFieldTimeWindow.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldTimeWindowFocusLost(evt);
            }
        });
        jTextFieldTimeWindow.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldTimeWindowKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldTimeWindowKeyTyped(evt);
            }
        });

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSampleInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(6, 6, 6)
                .addComponent(jTextFieldTimeWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetGraph, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleGrab, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jSampleInterval, jTextFieldTimeWindow});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButtonResetGraph});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToggleGrab, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jSampleInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonResetGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextFieldTimeWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)
                        .addComponent(jButton1)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanelDraw.setName("jPanelDraw"); // NOI18N

        jLabelpreGraph.setFont(resourceMap.getFont("jLabelpreGraph.font")); // NOI18N
        jLabelpreGraph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelpreGraph.setText(resourceMap.getString("jLabelpreGraph.text")); // NOI18N
        jLabelpreGraph.setName("jLabelpreGraph"); // NOI18N

        javax.swing.GroupLayout jPanelDrawLayout = new javax.swing.GroupLayout(jPanelDraw);
        jPanelDraw.setLayout(jPanelDrawLayout);
        jPanelDrawLayout.setHorizontalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDrawLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelpreGraph)
                .addContainerGap(183, Short.MAX_VALUE))
        );
        jPanelDrawLayout.setVerticalGroup(
            jPanelDrawLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDrawLayout.createSequentialGroup()
                .addComponent(jLabelpreGraph, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(208, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanelDraw, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelDraw, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>

    private void jToggleGrabItemStateChanged(java.awt.event.ItemEvent evt) {

        if (this.grabTimer==null){
            this.logToWindow("Cannot change graph since module is OFF", JPannelLoggerLogElement.SEVERITY_ERROR);
            return;
        }

        if (evt.getStateChange()==ItemEvent.SELECTED){
            // ON
            // activate timer
            this.grabTimer.setDelay(this.lookupSampleInterval());
            this.grabTimer.setInitialDelay(this.lookupSampleInterval()*2);
            this.grabTimer.start();

            this.logToWindow("Starting to sample RSSI data in time", JPannelLoggerLogElement.SEVERITY_DEBUG);
        }
        else{
            // OFF
            // deactivate timer
            this.grabTimer.stop();

            this.logToWindow("Stoping sampling RSSI data in time", JPannelLoggerLogElement.SEVERITY_DEBUG);
        }
    }

    private void jButtonResetGraphActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        this.timeSeries = new HashMap<String, TimeSeries>();
        this.timeDataSets = new TimeSeriesCollection();
        redrawGraphNeeded=true;

    }

    private void jTextFieldTimeWindowFocusLost(java.awt.event.FocusEvent evt) {
        // TODO add your handling code here:
        if (this.chartPanel==null) return;
        this.chartPanel.getChart().getXYPlot().getDomainAxis().setFixedAutoRange(lookupSampleTimeWindow() * 1000.0);
    }

    private void jTextFieldTimeWindowKeyTyped(java.awt.event.KeyEvent evt) {
        // NOTHING FOR NOW
    }

    private void jTextFieldTimeWindowKeyReleased(java.awt.event.KeyEvent evt) {
        // null test
        if (this.chartPanel==null) return;

        // was enter pressed?
        // if yes then update value
        if (evt.getKeyCode() == KeyEvent.VK_ENTER){
            this.chartPanel.getChart().getXYPlot().getDomainAxis().setFixedAutoRange(lookupSampleTimeWindow() * 1000.0);
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // export current data to CSV file for further processing
        if (this.chartPanel==null || this.chartPanel.getChart()==null
                || this.chartPanel.getChart().getPlot() == null
//                || this.timeSeries == null
//                || this.timeSeries.isEmpty()
                ){
            this.logToWindow("Something is wrong with plot or data, cannot export.", JPannelLoggerLogElement.SEVERITY_ERROR);
            return;
        }

        try {
            //Create a file chooser
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);

            // In response to a button click:
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                CsvWriter writer = new CsvWriter(file.getAbsolutePath());

                // change delim since semicolon can be used in time series title/name
                writer.setDelimiter('|');

                // get currently selected range
                Range range = this.chartPanel.getChart().getXYPlot().getDomainAxis().getRange();

                // load data from graph and save it to file
                // we need to filter out data not displayed in graph
                Set<String> timeSeriesKeys = this.timeSeries.keySet();
                Iterator<String> it = timeSeriesKeys.iterator();

                // format date like this
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // some comments to CSV file
                writer.writeComment("Distance in time");
                writer.writeComment("Exported: " + df.format(new Date(System.currentTimeMillis())));
                writer.writeComment("Exporting range from: " + range.getLowerBound()
                        + " (" + df.format(new Date((long)range.getLowerBound())) + ")"
                        + "; to: " + range.getUpperBound() + " ( " +  df.format(new Date((long)range.getUpperBound()))  + " )");

                while(it.hasNext()){
                    String curTimeSerieKey = it.next();
                    TimeSeries curTimeSerie = this.timeSeries.get(curTimeSerieKey);

                    // write header
                    writer.writeComment("Now follows serie with name: " + curTimeSerieKey);

                    // get data for this time serie
                    List<TimeSeriesDataItem> items = curTimeSerie.getItems();
                    Iterator<TimeSeriesDataItem> itemsIt = items.iterator();
                    while(itemsIt.hasNext()){
                        TimeSeriesDataItem curDataItem = itemsIt.next();

                        // filter here unwanted data
                        if (!range.contains(curDataItem.getPeriod().getMiddleMillisecond())) continue;

                        // write to CSV file
                        writer.writeRecord( new String[] {
                            "#serieKey", curTimeSerieKey,
                            "#xvalue", ""+curDataItem.getPeriod().getFirstMillisecond(),
                            "#distance", curDataItem.getValue().toString()
                        });
                    }
                }

                writer.flush();
                writer.close();
                this.logToWindow("CSV written successfully ("+file.getCanonicalPath()+")", JPannelLoggerLogElement.SEVERITY_INFO);
            } else {
                this.logToWindow("Not exported, action canceled", JPannelLoggerLogElement.SEVERITY_DEBUG);
            }
        }
        catch(Exception e){
            this.logToWindow("Something bad happened during export (exception thrown): " + e.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }

    /**
     * data description for graphing
     * Hasher would create from this time series key; then categorizes each data dynamicaly
     * based on preset rules.
     *
     * When doing it like this user could be able dynamically change grouping variables(hasher config),
     * dependent variable(Yvalue) and displayed data series(data descriptor; graphData->String)
     *
     * Better way: store graph values in MAP<String, Double>
     *                                       ParamName, ParamValue
     */
    private class graphData {
        public int talkingMote;
        public int reportingMote;
        public long time;
        public double rssi;
    }

    // INTERNAL CLASS
    private class graphDrawer extends Thread {

        @Override
        public void run() {

        }

    }



    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonResetGraph;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelpreGraph;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelDraw;
    private javax.swing.JTextField jSampleInterval;
    private javax.swing.JTextField jTextFieldTimeWindow;
    private javax.swing.JToggleButton jToggleGrab;
    // End of variables declaration


}
