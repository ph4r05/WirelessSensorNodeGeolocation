/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * jPannelRSSI2DistanceDataLoader.java
 *
 * Created on Apr 1, 2011, 8:50:18 PM
 */

package rssi_graph.rssi;

import com.csvreader.CsvWriter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.lang.String;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.Task.InputBlocker;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.TaskService;
import rssi_localization.JPannelLoggerLogElement;
import rssi_graph.nodeRegister.MoteSpecifics;
import rssi_localization.RSSI_graphApp;
import rssi_graph.db.dbUtils;
import rssi_graph.db.dbUtilsDataFetcher;
import rssi_graph.db.rssi2dist_measurement;
import rssi_graph.utils.BucketHasher;
import rssi_graph.utils.OptionsUtils;
import rssi_graph.utils.TableFullTextCellRenderer;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 * Data loader for stored data from experiments.
 * Some data manipulation is allowed here, data could be plotted to 
 * various graphs, system can be calibrated from measured data, 
 * mobile nodes could be localized offline.
 * 
 * @author ph4r05
 */
public class jPannelRSSI2DistanceDataLoader extends javax.swing.JPanel {

    /**
     * SQLite connection
     */
    private Connection conn = null;

    /**
     * Main worker
     */
    private WorkerR2D workerR2D = null;

    /**
     * MID->TestNo map for currently loaded data
     */
    private HashMap<Integer, Integer> mid2testno = null;

    /**
     * RSSI measurement internal buffer list.
     * Stores data after database load, before processing
     * (graphing, exporting to-csv, display, view generator)
     */
    private List<RSSI2DistInternalBuffer> dataBuffer = null;

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
    private Map<String, ArrayList<RSSIFitFunctionDetails>> fittedFunctionsDetails = null;


    /**
     * Add to graph pop-up
     */
    private JDialogAddToGraph dataParameters = null;

    /**
     * Load data graph pop-up
     */
    private jDialogLoadData loadDataDialog = null;
    
    /**
     * Localization dialog. User specifies some details needed for offline localization.
     */
    private JDialogLocalization localizationDialog = null;
    
    /**
     * Calibration dialog
     */
    private JDialogCalibration calibrationDialog = null;
    
    /**
     * Data options (settings)
     */
    private Map<String,Object> dataOptions = null;
    
    /**
     * DataLoad prepared task for execution
     */
    LoadDataWorker dataLoadWorker = null;

    /** Creates new form jPannelRSSI2DistanceDataLoader */
    public jPannelRSSI2DistanceDataLoader() {
        initComponents();

        // tune tables
        TableFullTextCellRenderer tftcr = new TableFullTextCellRenderer();
        tftcr.extractColorsFrom(this.jTableTest);
        this.jTableTest.setDefaultRenderer(String.class, tftcr);
        this.jTableTest.setCellSelectionEnabled(false);
        this.jTableTest.setColumnSelectionAllowed(false);
        this.jTableTest.setRowSelectionAllowed(true);

        // autosorter
        this.jTableNode.setAutoCreateRowSorter(true);
        this.jTableTest.setAutoCreateRowSorter(true);
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
        this.logToWindow(s, 145, "RSSI2DistanceDataLoader", severity);
    }

    /**
     * Listen on events for 
     * @param turnedOn
     */
    public void turnedOnOff(boolean turnedOn){
        if (turnedOn == false){
            // turned off => clear all tables
            this.clearAllTables();
        }
        else {
            this.clearAllTables();
            try {
                // reload database
                this.reloadDatabase();
            } catch (SQLException ex) {
                Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Enable/Disable panel components according to database connection
     * If database connection is NULL, cannot work with database and this
     * panel is thus disabled
     *
     * @param enable
     */
    public void enableAll(boolean enable){
        //this.jButtonDraw.setEnabled(enable);
        this.jButtonNextNode.setEnabled(enable);
        this.jButtonNextTX.setEnabled(enable);
        this.jButtonNextTest.setEnabled(enable);
        this.jButtonRefreshDatabase.setEnabled(enable);
        this.jButtonExport.setEnabled(enable);
        this.jButtonNextTX2.setEnabled(enable);

        this.jTableModel.setEnabled(enable);
        this.jTableNode.setEnabled(enable);
        this.jTableTest.setEnabled(enable);
    }

    /**
     * Fills first table with saved tests overview
     * @throws SQLException
     */
    public void loadTests() throws SQLException{
        // test connection
        // if is not possible to load anything, just exit
        if (this.conn==null || this.conn.isClosed()) return;
        
        // instantiate new table model
        TestTableModel tmp = new TestTableModel();
        
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select * FROM `rssi2dist_measurement` ORDER BY testno ASC, mid ASC, distance ASC;");

        // data fetching 
        String[] columnNames = rssi2dist_measurement.getSelectedColumns(tmp.getDbColumns());
        String[] columnTypes = rssi2dist_measurement.getSelectedColumnsType(tmp.getDbColumns());
        LinkedList<ArrayList<Object>> tmpData = dbUtils.fetchDataByColumns(rs, columnNames, columnTypes);
        tmp.setData(dbUtils.fetchedResultToObjectArray(tmpData));

        // prepare for garbage collector
        tmpData=null;

        // set table model for table
        this.jTableTest.setModel(tmp);
        this.jTableTest.repaint();
        rs.close();

        logToWindow("Test loading completed", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Fills node table according to selected tests in test table.
     * User now should specify more details about wanted selection (which particular 
     * nodes are interesting for him)
     * 
     * Now doing from background thread. Left old code when wanted to switch back
     * 
     * @throws SQLException
     */
    public void loadNodes() throws SQLException{
        // invoke action manually
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getActionMap(jPannelRSSI2DistanceDataLoader.class, this);
        javax.swing.Action action = actionMap.get("loadTestBackground");
        action.actionPerformed(new ActionEvent(this.jButtonNextTest, 1, "Act!"));
        
        
//        NodeTableModel tmp = new NodeTableModel();
//
//        // determine selected test numbers from table
//        int[] sel = this.jTableTest.getSelectedRows();
//        if (sel==null || sel.length==0) return;
//
//        // get data from selected table
//        TableModel tmod = this.jTableTest.getModel();
//        if (!(tmod instanceof TestTableModel)){
//            throw new IllegalStateException("TableTest is excepted to has TableModel: TestTableModel");
//        }
//
//        // testno -> mid mapping
//        mid2testno = new HashMap<Integer, Integer>();
//
//        // convert type
//        TestTableModel tblModel = (TestTableModel) tmod;
//
//        // build mid IN(a,b,c) statement
//        StringBuilder sb = new StringBuilder(64);
//        for(int i=0, cn=sel.length; i<cn; i++){
//            // we need to select MID from selected table rows
//            // format for SQL IN statement directly
//            if (i>0) sb.append(",");
//            
//            // selected rowID need to be converted to model row id since after
//            // sorting rows does not match
//            int selectedRow = this.jTableTest.convertRowIndexToModel(sel[i]);
//            Object curMid = tblModel.getValueAt(selectedRow, 1);
//            Object testno = tblModel.getValueAt(selectedRow, 0);
//            sb.append(curMid);
//            mid2testno.put((Integer)curMid, (Integer)testno);
//        }
//        
//        // get data 
//        Statement stat = conn.createStatement();
//        String sqlQuery = "SELECT mid,source,txpower FROM rssi2dist_measurement_data "
//                + " WHERE mid IN("+sb.toString()+") "
//                + " GROUP BY mid,source,txpower"
//                + " ORDER BY mid,source,txpower";
//        ResultSet rs = stat.executeQuery(sqlQuery);
//        
//         // data fetching, own column types and fetcher
//        String[] columnNames = {"testno", "mid", "source", "txpower"};
//        String[] columnTypes = {"integer", "integer", "integer", "integer"};
//
//        // using own data fetcher to fetch testno column correctly from internal map
//        dbUtilsDataFetcher dataFetcher = new dbUtilsDataFetcher(){
//            @Override
//            public boolean hasDefinedOwnResult(int column) {
//                return column==0;
//            }
//
//            @Override
//            public Object getOwnResult(int column, String columnName, String columnType, ResultSet rs) {
//                try {
//                    return new Integer(mid2testno.get(new Integer(rs.getInt("mid"))));
//                } catch (SQLException ex) {
//                    Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                
//                return null;
//            }
//        };
//
//        LinkedList<ArrayList<Object>> tmpData = dbUtils.fetchDataByColumns(rs, columnNames, columnTypes, dataFetcher);
//        if (tmpData==null || tmpData.size()==0) return;
//
//        tmp.setData(dbUtils.fetchedResultToObjectArray(tmpData));
//
//        // prepare for garbage collector
//        tmpData=null;
//
//        // set table model for table
//        this.jTableNode.setModel(tmp);
//        this.jTableNode.repaint();
//        rs.close();
//
//        // enable loading next data
//        this.jButtonNextNode.setEnabled(true);
//        logToWindow("Node details loading completed", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }
    
    /**
     * Parameterless
     * @throws SQLException
     */
    public void loadData() throws SQLException{
        this.loadData(new HashMap<String, Object>());
    }

    /**
     * Loads data from database defined by selected rows
     * Load in cycle per 1 row
     *   - one time load would be memory consuming
     *   - one graph curve/data points are uniquely identified exactly by 1 row from
     *      node table
     * 
     * Prepares data for background thread which loads data.
     *
     * @throws SQLException
     */
    public void loadData(Map<String, Object> options) throws SQLException{
        
        // prepare data for background thread
        try {
            // determine selected test numbers from table
            int[] sel = this.jTableNode.getSelectedRows();
            if (sel == null || sel.length == 0) {
                logToWindow("Nothing selected", JPannelLoggerLogElement.SEVERITY_WARNING);
                return;
            }

            // default parameters
            if (options==null){
                options = new HashMap<String, Object>();
            }

            // is method invoked from data load dialog?
            boolean dataLoadDialog=(this.loadDataDialog!=null && this.loadDataDialog.isShowing());                    
            
            // default parameters
            Set<String> defaultXvalueGroup = new HashSet<String>();
            defaultXvalueGroup.add("testno");
            defaultXvalueGroup.add("staticId");
            defaultXvalueGroup.add("txpower");
            
            OptionsUtils.setDefaultOption(options, "XvalueGroup", defaultXvalueGroup);
            OptionsUtils.setDefaultOption(options, "ParamSource", 0);
            OptionsUtils.setDefaultOption(options, "ParamDirectMulti", Double.valueOf(1.0));
            OptionsUtils.setDefaultOption(options, "ParamConstant", Double.valueOf(0));
            OptionsUtils.setDefaultOption(options, "MinimalRefDistance", Double.valueOf(100));
            OptionsUtils.setDefaultOption(options, "doCurveFit", Boolean.valueOf(true));
            this.dataOptions = options;

            // table models
            TableModel tmod = this.jTableNode.getModel();
            if (!(tmod instanceof NodeTableModel)) {
                throw new IllegalStateException("TableNode is excepted to has TableModel: NodeTableModel");
            }
            NodeTableModel tblModel = (NodeTableModel) tmod;

            // store mid separately, need to query for testno, distance, mobileNode
            int[] mids = new int[this.jTableNode.getSelectedRowCount()];
            int[] source = new int[this.jTableNode.getSelectedRowCount()];
            int[] txpower = new int[this.jTableNode.getSelectedRowCount()];

            // build SQLstatements for rssi data query
            String[] sqlWhere = new String[this.jTableNode.getSelectedRowCount()];
            StringBuilder sb = null;
            for (int i = 0, cn = sel.length; i < cn; i++) {
                // build where statement directly from selected data
                sb = new StringBuilder(128);

                 // selected rowID need to be converted to model row id since after
                 // sorting rows does not match
                int selectedRow = this.jTableNode.convertRowIndexToModel(sel[i]);

                sb.append("mid=");
                sb.append(tblModel.getValueAt(selectedRow, 1));
                mids[i] = (Integer) tblModel.getValueAt(selectedRow, 1);

                sb.append(" AND source=");
                sb.append(tblModel.getValueAt(selectedRow, 2));
                source[i] = (Integer) tblModel.getValueAt(selectedRow, 2);

                sb.append(" AND txpower=");
                sb.append(tblModel.getValueAt(selectedRow, 3));
                txpower[i] = (Integer) tblModel.getValueAt(selectedRow, 3);
                
                sqlWhere[i] = sb.toString();
            }
            
            // prepare thread, fill with data
            this.dataLoadWorker = new LoadDataWorker();
            this.dataLoadWorker.setConn(conn);
            this.dataLoadWorker.setMids(mids);
            this.dataLoadWorker.setOptions(options);
            this.dataLoadWorker.setParent(this);
            this.dataLoadWorker.setSource(source);
            this.dataLoadWorker.setSqlWhere(sqlWhere);
            this.dataLoadWorker.setTxpower(txpower);
            
            // invoke action manually
            javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getActionMap(jPannelRSSI2DistanceDataLoader.class, this);
            javax.swing.Action action = actionMap.get("loadDataBackground");
            action.actionPerformed(new ActionEvent(this.jButtonNextTX, 1, "Act!"));
            
////        Task mT = new DoNothingTask();
////        //mT.setInputBlocker(new BusyIndicatorInputBlocker(mT, new BusyIndicator()));
////        
////        ApplicationContext appC = Application.getInstance().getContext();
////        TaskMonitor tM = appC.getTaskMonitor();
////        TaskService tS = appC.getTaskService();
////        tS.execute(mT);
////        tM.setForegroundTask(mT);
        }
        catch (Exception ex){
            Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * After data loaded, process.
     */
    public void postDataLoad(Map<String, Object> options) throws FunctionEvaluationException, OptimizationException{
        logToWindow("Samples loading completed, buffer size=" + this.dataBuffer.size(), JPannelLoggerLogElement.SEVERITY_DEBUG);

        // buckets
        makeBucketsFromData((Set<String>) OptionsUtils.getOption(options, "XvalueGroup"));

        // fit curve
        boolean doCurveFit = (Boolean) OptionsUtils.getOption(options, "doCurveFit");
        if (doCurveFit){
            fitCurve(options);
        }

        // display data to textarea
        displayCurData();

        // finaly enable add to graph button for user
        this.jButtonNextTX.setEnabled(true);
        this.jButtonExport.setEnabled(true);

        // ideal design: now notify data changed listeners...
        // current state is enough for now (KISS principle)
        if (this.localizationDialog!=null){
            try {
                // reset currently loaded data & reload
                this.localizationDialog.reloadData();
                this.localizationDialog.refresView();
            } catch(Exception ex){
                Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Build bucket structure for bufferedData
     * each separate test per bucket
     * in single bucket are distances
     */
    public void makeBucketsFromData(Set<String> xvalueGroup){
        this.testBuckets=null;
        if (this.dataBuffer==null || this.dataBuffer.size()==0){
            this.logToWindow("Data empty, cannot form buckets", JPannelLoggerLogElement.SEVERITY_WARNING);
            return;
        }

        DefaultRSSI2DistInternalBufferHasher hasher = new DefaultRSSI2DistInternalBufferHasher();
        hasher.setXgroup(xvalueGroup);
        
        BucketHasher<RSSI2DistInternalBuffer> bhasher = new BucketHasher<RSSI2DistInternalBuffer>(hasher);

        this.testBuckets = bhasher.makeBuckets(dataBuffer);
        this.logToWindow("Buckets formed; Size=" + this.testBuckets.size(), JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    public void fitCurve() throws FunctionEvaluationException, OptimizationException{
         this.fitCurve(new HashMap<String, Object>());
     }

    /**
     * Fits curve to currently loaded data according to selected algorithms/propagation models.
     *
     *  Get selected models from table on panel.
     *  at time of writing this code assume that each model has to be added
     *  manually at compile time (no runtime adding). During this person who add
     *  new model will change this code to work with new model.
     *
     * @Todo: better design for this. Pass raw data structure to optimizators/fitters for given models.
     * Each model could compute calibration by different way
     */
    public void fitCurve(Map<String, Object> options) throws FunctionEvaluationException, OptimizationException{
        // curve fitting can be done only if buckets are formed
        if (this.testBuckets==null || this.testBuckets.isEmpty()){
            this.logToWindow("Data empty, cannot fit any curve", JPannelLoggerLogElement.SEVERITY_WARNING);
            return;
        }

        // init fit function
        this.fittedFunctions = new HashMap<String, ArrayList<RSSI2DistFunctionInterface>>();

        // get selected models from table
        // is log normal selected?
        if (((Boolean)this.jTableModel.getValueAt(0, 0)) == true){
            // create optimizer for this
            RSSI2DistOptimizer opt = null;

            OptionsUtils.setDefaultOption(options, "setPt", Boolean.valueOf(false));
            OptionsUtils.setDefaultOption(options, "MinimalRefDistance", Double.valueOf(100));
            OptionsUtils.setDefaultOption(options, "MaximalRefDistance", Double.valueOf(150));
            Double minRefDistance = (Double) OptionsUtils.getOption(options, "MinimalRefDistance");
            Double maxRefDistance = (Double) OptionsUtils.getOption(options, "MaximalRefDistance");
            Boolean setPt = (Boolean) OptionsUtils.getOption(options, "setPt");

            // fit for all buckets
            // extract key set and iterate over each bucket
            Set<String> keySet = this.testBuckets.keySet();
            Iterator<String> it = keySet.iterator();
            for(int i=0; it.hasNext(); i++){
                String curKey = it.next();
                ArrayList<RSSI2DistInternalBuffer> tmpArr = this.testBuckets.get(curKey);

                // null-test
                if (tmpArr==null){
                    throw new IllegalStateException("This object cannot be empty(ArrayList<RSSI2DistInternalBuffer>). Thread-safe problem?");
                }

                // we need at least 2 points to do interpolation/regression
                if (tmpArr.size()<=1){
                    continue;
                }

                // we now have single test, fit function to it
                opt = new RSSI2DistOptimizer();
                RSSI2DistLogNormalShadowing function = (RSSI2DistLogNormalShadowing) opt.getFunction();
                double[][] data2optimizer = new double[tmpArr.size()][2];
                boolean functionInitiated=false;

                // iterate over array list and add statistic points
                for(int j=0, cnJ=tmpArr.size(); j<cnJ; j++){
                    RSSI2DistInternalBuffer tmpBuffer = tmpArr.get(j);

                    // null-test
                    if (tmpBuffer==null){
                        throw new IllegalStateException("At position i="+i+";j="+j+"; RSSI2DistInternalBuffer is null");
                    }

                    // set function parameters, refference distance and so on
                    // by default use 100cm as refference distance or anything larger
                    if (functionInitiated==false && 
                            tmpBuffer.getXvalue() >= minRefDistance &&
                            tmpBuffer.getXvalue() <= maxRefDistance){
                        function.setD0(tmpBuffer.getXvalue());
                        function.setStddev(0);
                        function.setPl(tmpBuffer.getStats().getMean());

                        if (setPt){
                            function.setPt(MoteSpecifics.getPowerLevel(tmpBuffer.getTxpower()));
                        }

                        functionInitiated=true;
                    }
                    
                    data2optimizer[j][0] = (tmpBuffer.getXvalue());
                    data2optimizer[j][1] = tmpBuffer.getStats().getMean();
                }

                // if function was not initiated, we cannot continue in interpolation
                if (functionInitiated==false){
                    continue;
                }

                double[] optimizeParameters = opt.optimizeParameters(data2optimizer);
                function.setNi(optimizeParameters[0]);
                function.setConstant(optimizeParameters.length > 1 ? optimizeParameters[1]:0);
                function.setRms(opt.getRMS());
                function.setStddev(opt.getRMS()*opt.getRMS());

                // add to fit function
                ArrayList<RSSI2DistFunctionInterface> tmpFunctions =
                        this.fittedFunctions.containsKey(curKey) ?
                            this.fittedFunctions.get(curKey) :
                            new ArrayList<RSSI2DistFunctionInterface>();
                
                tmpFunctions.add(function);
                this.fittedFunctions.put(curKey, tmpFunctions);

                this.logToWindow("Optimized function: " + function.toString(), JPannelLoggerLogElement.SEVERITY_INFO);
                this.logToWindow("For test: " + tmpArr.get(0).toString(), JPannelLoggerLogElement.SEVERITY_INFO);
                this.logToWindow("RMS for this fit: " + opt.getRMS(), JPannelLoggerLogElement.SEVERITY_INFO);
                this.logToWindow("Stddev of X: " + function.getStddev(), JPannelLoggerLogElement.SEVERITY_INFO);
            }
            
            this.logToWindow("Curve fit complete for model: LogNormalShadowing", JPannelLoggerLogElement.SEVERITY_DEBUG);
        }
    }

    /**
     * Displays current data (headers only) stored in DataBuffer to TextArray.
     * Together with statistical data.
     */
    public void displayCurData(){
        if (this.dataBuffer==null || this.dataBuffer.size()==0){
            logToWindow("Cannot display buffered data, size=0", JPannelLoggerLogElement.SEVERITY_DEBUG);
            return;
        }

        StringBuilder sb = new StringBuilder(256);
        DecimalFormat df = new DecimalFormat("00.000");
        Iterator<RSSI2DistInternalBuffer> it = this.dataBuffer.iterator();
        for(int i=0; it.hasNext(); i++){
            RSSI2DistInternalBuffer tmpBuffer = it.next();
            if (tmpBuffer==null){
                logToWindow("Exception occurred, one element in dataBuffer is null", JPannelLoggerLogElement.SEVERITY_WARNING);
                continue;
            }

            // extra space
            if (i>0) sb.append("\n");

            // header
            sb.append("Testno=")
                    .append(tmpBuffer.getTestno())
                    .append("; mid=")
                    .append(tmpBuffer.getMid())
                    .append("; reporter=")
                    .append(tmpBuffer.getReportingMote())
                    .append("; talking=")
                    .append(tmpBuffer.getTalkingMote())
                    .append("; distance=")
                    .append(tmpBuffer.getDistance())
                    .append("; xvalue=")
                    .append(tmpBuffer.getXvalue())
                    .append("\n");

            // statistics
            RSSI2DistInternalBufferStats stats = tmpBuffer.getStats();
            sb.append("n=").append(stats.getN())
                    .append("; mean=").append(df.format(stats.getMean()))
                    .append("; median=").append(df.format(stats.getMedian()))
                    .append("; stddev=").append(df.format(stats.getStddev()))
                    .append("; q1=").append(df.format(stats.getQ1()))
                    .append("; q3=").append(df.format(stats.getQ3()))
                    .append("; min=").append(stats.getMin())
                    .append("; max=").append(stats.getMax());
        }

        sb.append("\n\n");

        // if there are no functions, do not continue;
        if (this.fittedFunctions == null || this.fittedFunctions.isEmpty()) {
            this.jTextAreaDataStatistics.setText(sb.toString());
            return;
        }

        // display fitted functions parameters
        Set<String> functionKeys = this.fittedFunctions.keySet();
        Iterator<String> fKeyIterator = functionKeys.iterator();
        while(fKeyIterator.hasNext()){
            String curFunctionKey = fKeyIterator.next();

            // array list
            ArrayList<RSSI2DistFunctionInterface> aList = this.fittedFunctions.get(curFunctionKey);
            if (aList==null || aList.isEmpty()) continue;

            for(int j=0, cnJ=aList.size(); j<cnJ; j++){
                RSSI2DistFunctionInterface tmpFunction = aList.get(j);
                if (tmpFunction==null) continue;

                sb.append("dataset=").append(curFunctionKey)
                        .append("; function=").append(tmpFunction.toStringHuman(3))
                        .append("; rms=").append(tmpFunction.getRms())
                        .append("; fname=").append(tmpFunction.getFormulaName())
                        .append("; formula=").append(tmpFunction.getFormula())
                        ;
            }
        }

        this.jTextAreaDataStatistics.setText(sb.toString());
    }

    /**
     * Looks up for anotation from table for given mid
     * 
     * @param mid
     * @return
     */
    public String getAnotationFor(int mid){
        if (this.jTableTest==null || this.jTableTest.getRowCount()==0){
            this.logToWindow("Cannot get anotation, table is empty", JPannelLoggerLogElement.SEVERITY_ERROR);
            return null;
        }

        // get right row - linear search
        for(int i=0, cnI=this.jTableTest.getRowCount(); i<cnI; i++){
            int modelId = this.jTableTest.convertRowIndexToModel(i);
            if ((Integer)this.jTableTest.getModel().getValueAt(modelId, 1) != mid) continue;

            return (String) this.jTableTest.getModel().getValueAt(modelId, 4);
        }

        return null;
    }

    /**
     * Exports currently calculated data statistics to CSV file in order defined by table sorter
     */
    public void exportData(){
        if (this.dataBuffer==null || this.dataBuffer.size()==0){
            logToWindow("Cannot export buffered data, size=0", JPannelLoggerLogElement.SEVERITY_DEBUG);
            return;
        }

        // CSV save to file dialog
         try {
            //Create a file chooser
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);

            // In response to a button click:
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                // use third party CSV writer class
                CsvWriter writer = new CsvWriter(file.getAbsolutePath());

                // change delim since semicolon can be used in time series title/name
                writer.setDelimiter('|');

                // format date like this
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // some comments to CSV file
                writer.writeComment("RSSI stats");
                writer.writeComment("Exported: " + df.format(new Date(System.currentTimeMillis())));

                // write header
                writer.writeRecord(new String[] {
                    // general data
                    "testno", "mid", "txpower", "distance", "xvalue", "talkingMote", "reportingMote",

                    // statistics
                    "n", "min", "max", "mean", "median", "stdev", "q1", "q3",

                    // anotation may be usefull
                    "anotation"
                });

                StringBuilder sb = new StringBuilder(256);
                DecimalFormat decf = new DecimalFormat("00.000");
                Iterator<RSSI2DistInternalBuffer> it = this.dataBuffer.iterator();
                for(int i=0; it.hasNext(); i++){
                    RSSI2DistInternalBuffer tmpBuffer = it.next();
                    if (tmpBuffer==null){
                        logToWindow("Exception occurred, one element in dataBuffer is null", JPannelLoggerLogElement.SEVERITY_WARNING);
                        continue;
                    }

                    String anotation = this.getAnotationFor(tmpBuffer.getMid());
                    if (anotation==null)
                        anotation = "";
                    
                    writer.writeRecord(new String[] {
                        // general data
                        String.valueOf(tmpBuffer.getTestno()), 
                        String.valueOf(tmpBuffer.getMid()),
                        String.valueOf(tmpBuffer.getTxpower()),
                        String.valueOf(tmpBuffer.getDistance()),
                        String.valueOf(tmpBuffer.getXvalue()),
                        String.valueOf(tmpBuffer.getTalkingMote()),
                        String.valueOf(tmpBuffer.getReportingMote()),

                        // statistics
                        String.valueOf(tmpBuffer.getStats().getN()),
                        String.valueOf(tmpBuffer.getStats().getMin()),
                        String.valueOf(tmpBuffer.getStats().getMax()),
                        String.valueOf(tmpBuffer.getStats().getMean()),
                        String.valueOf(tmpBuffer.getStats().getMedian()),
                        String.valueOf(tmpBuffer.getStats().getStddev()),
                        String.valueOf(tmpBuffer.getStats().getQ1()),
                        String.valueOf(tmpBuffer.getStats().getQ3()),

                        // anotation
                        anotation
                    });
                }

                // now write fitted functions
                 writer.writeComment("");
                 writer.writeComment("Fitted functions");
                 writer.writeComment("");

                 // write header
                 writer.writeRecord(new String[] {
                    // general data
                    "dataset", "fid", "function", "rms", "ftype", "formula"
                });

                 // if there is no function, create empty map
                 if (this.fittedFunctions==null){
                     this.fittedFunctions = new HashMap<String, ArrayList<RSSI2DistFunctionInterface>>(0);
                 }
                 
                 Set<String> functionKeys = this.fittedFunctions.keySet();
                 Iterator<String> fKeyIterator = functionKeys.iterator();
                 while(fKeyIterator.hasNext()){
                    String curFunctionKey = fKeyIterator.next();

                    // array list
                    ArrayList<RSSI2DistFunctionInterface> aList = this.fittedFunctions.get(curFunctionKey);
                    if (aList==null || aList.isEmpty()) continue;

                    for(int j=0, cnJ=aList.size(); j<cnJ; j++){
                        RSSI2DistFunctionInterface tmpFunction = aList.get(j);
                        if (tmpFunction==null) continue;

                        writer.writeRecord(new String[] {
                            // general data
                            curFunctionKey,
                            String.valueOf(j),
                            String.valueOf(tmpFunction.toString()),
                            String.valueOf(tmpFunction.getRms()),
                            String.valueOf(tmpFunction.getFormulaName()),
                            String.valueOf(tmpFunction.getFormula()),
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
            e.printStackTrace();
            this.logToWindow("Something bad happened during export (exception thrown): " + e.getMessage() + "; ", JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }

    /**
     * Clears all tables and reloads table no. 1 (tests)
     */
    public void reloadDatabase() throws SQLException{
        this.clearAllTables();
        this.loadTests();
    }
    
    /**
     * clear all tables
     */
    public void clearAllTables(){
        TableModel tabMod = null;
        TableMyAbstractTableModel tabMyMod = null;

        this.jTableTest.setModel(new DefaultTableModel());
        this.jTableTest.repaint();

        this.jTableNode.setModel(new DefaultTableModel());
        this.jTableNode.repaint();

        this.jTextAreaDataStatistics.setText("");
        this.mid2testno=null;
        this.dataBuffer=null;
        this.testBuckets=null;
        this.fittedFunctions=null;
        
        // hide some controls 
        // reasonable circumstances = no data
        this.jButtonNextNode.setEnabled(false);
        this.jButtonNextTX.setEnabled(false);
        this.jButtonExport.setEnabled(false);
    }

    /**
     * =========================================================================
     *
     * GETTERS + SETTERS
     *
     * =========================================================================
     */

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;

        // enable/disable panel?
        this.enableAll(this.conn!=null);
    }

    public WorkerR2D getWorkerR2D() {
        return workerR2D;
    }

    public void setWorkerR2D(WorkerR2D workerR2D) {
        this.workerR2D = workerR2D;
    }

    /**
     * =========================================================================
     *
     * TABLE MODELS
     *
     * =========================================================================
     */

    class TestTableModel extends TableMyAbstractTableModel {
        protected int[] dbColumns = {8, 0, 3, 7, 6, 4, 5, 1};
        protected int selectionKey = 1;

        public TestTableModel() {
            this.columnNames = new String[] {"test", "Mid", "Time",
            "Distance", "Anotation", "Packets", "Delay", "Mobile"};
        }

        public int[] getDbColumns() {
            return dbColumns;
        }

        public int getSelectionKey() {
            return selectionKey;
        }
    }

    class NodeTableModel extends TableMyAbstractTableModel {
        protected int[] dbColumns = {1, 2};
        public NodeTableModel() {
            this.columnNames = new String[] {"TestNo", "MID", "NodeID", "TXpower"};
        }

         public int[] getDbColumns() {
            return dbColumns;
        }
    }

   class TXTableModel extends TableMyAbstractTableModel {
        public TXTableModel() {
            this.columnNames = new String[] {"Use", "TestNo", "NodeID", "TXpower"};
        }
    }

    public List<RSSI2DistInternalBuffer> getDataBuffer() {
        return dataBuffer;
    }

    public Map<String, ArrayList<RSSI2DistFunctionInterface>> getFittedFunctions() {
        return fittedFunctions;
    }

    public Map<String, ArrayList<RSSI2DistInternalBuffer>> getTestBuckets() {
        return testBuckets;
    }

    public Map<String, Object> getDataOptions() {
        return dataOptions;
    }

    
    
    /**
     * =========================================================================
     *
     * AUTO-GENERATED CODE
     *
     * =========================================================================
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableNode = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableModel = new javax.swing.JTable();
        jButtonNextTest = new javax.swing.JButton();
        jButtonNextNode = new javax.swing.JButton();
        jButtonNextTX = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTableTest = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaDataStatistics = new javax.swing.JTextArea();
        jButtonExport = new javax.swing.JButton();
        jButtonNextTX2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButtonLocalize = new javax.swing.JButton();
        jButtonCalibration = new javax.swing.JButton();
        jButtonClear = new javax.swing.JButton();
        jButtonRefreshDatabase = new javax.swing.JButton();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getResourceMap(jPannelRSSI2DistanceDataLoader.class);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTableNode.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableNode.setEnabled(false);
        jTableNode.setName("jTableNode"); // NOI18N
        jTableNode.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(jTableNode);
        jTableNode.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTableModel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Boolean(true), "LogNormal-mean"},
                {new Boolean(false), "LogNormal-median"}
            },
            new String [] {
                "Use", "Model"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTableModel.setEnabled(false);
        jTableModel.setName("jTableModel"); // NOI18N
        jTableModel.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(jTableModel);
        jTableModel.getColumnModel().getColumn(0).setPreferredWidth(1);
        jTableModel.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableModel.columnModel.title0")); // NOI18N
        jTableModel.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableModel.columnModel.title1")); // NOI18N

        jButtonNextTest.setText(resourceMap.getString("jButtonNextTest.text")); // NOI18N
        jButtonNextTest.setActionCommand(resourceMap.getString("jButtonNextTest.actionCommand")); // NOI18N
        jButtonNextTest.setEnabled(false);
        jButtonNextTest.setName("jButtonNextTest"); // NOI18N
        jButtonNextTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextTestActionPerformed(evt);
            }
        });

        jButtonNextNode.setText(resourceMap.getString("jButtonNextNode.text")); // NOI18N
        jButtonNextNode.setActionCommand(resourceMap.getString("jButtonNextNode.actionCommand")); // NOI18N
        jButtonNextNode.setEnabled(false);
        jButtonNextNode.setName("jButtonNextNode"); // NOI18N
        jButtonNextNode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextNodeActionPerformed(evt);
            }
        });

        jButtonNextTX.setText(resourceMap.getString("jButtonNextTX.text")); // NOI18N
        jButtonNextTX.setActionCommand(resourceMap.getString("jButtonNextTX.actionCommand")); // NOI18N
        jButtonNextTX.setEnabled(false);
        jButtonNextTX.setName("jButtonNextTX"); // NOI18N
        jButtonNextTX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextTXActionPerformed(evt);
            }
        });

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTableTest.setAutoCreateRowSorter(true);
        jTableTest.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableTest.setEnabled(false);
        jTableTest.setName("jTableTest"); // NOI18N
        jTableTest.getTableHeader().setReorderingAllowed(false);
        jScrollPane5.setViewportView(jTableTest);
        jTableTest.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaDataStatistics.setColumns(20);
        jTextAreaDataStatistics.setEditable(false);
        jTextAreaDataStatistics.setRows(5);
        jTextAreaDataStatistics.setName("jTextAreaDataStatistics"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaDataStatistics);

        jButtonExport.setText(resourceMap.getString("jButtonExport.text")); // NOI18N
        jButtonExport.setActionCommand(resourceMap.getString("jButtonExport.actionCommand")); // NOI18N
        jButtonExport.setEnabled(false);
        jButtonExport.setName("jButtonExport"); // NOI18N
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });

        jButtonNextTX2.setText(resourceMap.getString("jButtonNextTX2.text")); // NOI18N
        jButtonNextTX2.setActionCommand(resourceMap.getString("jButtonNextTX2.actionCommand")); // NOI18N
        jButtonNextTX2.setEnabled(false);
        jButtonNextTX2.setName("jButtonNextTX2"); // NOI18N
        jButtonNextTX2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextTX2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jButtonLocalize.setText(resourceMap.getString("jButtonLocalize.text")); // NOI18N
        jButtonLocalize.setName("jButtonLocalize"); // NOI18N
        jButtonLocalize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLocalizeActionPerformed(evt);
            }
        });

        jButtonCalibration.setText(resourceMap.getString("jButtonCalibration.text")); // NOI18N
        jButtonCalibration.setToolTipText(resourceMap.getString("jButtonCalibration.toolTipText")); // NOI18N
        jButtonCalibration.setName("jButtonCalibration"); // NOI18N
        jButtonCalibration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalibrationActionPerformed(evt);
            }
        });

        jButtonClear.setText(resourceMap.getString("jButtonClear.text")); // NOI18N
        jButtonClear.setName("jButtonClear"); // NOI18N
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });

        jButtonRefreshDatabase.setText(resourceMap.getString("jButtonRefreshDatabase.text")); // NOI18N
        jButtonRefreshDatabase.setActionCommand(resourceMap.getString("jButtonRefreshDatabase.actionCommand")); // NOI18N
        jButtonRefreshDatabase.setEnabled(false);
        jButtonRefreshDatabase.setName("jButtonRefreshDatabase"); // NOI18N
        jButtonRefreshDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefreshDatabaseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButtonCalibration, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonLocalize, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonClear)
                    .addComponent(jButtonRefreshDatabase))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonCalibration, jButtonClear, jButtonLocalize, jButtonRefreshDatabase});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLocalize)
                    .addComponent(jButtonClear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCalibration)
                    .addComponent(jButtonRefreshDatabase))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                    .addComponent(jButtonNextTest, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane4, 0, 0, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                            .addComponent(jButtonNextNode, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonNextTX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonNextTX2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonExport))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonNextTest, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonNextTX2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonExport, javax.swing.GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonNextNode, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonNextTX, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(1, 1, 1)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
        // TODO add your handling code here:
        this.clearAllTables();
    }//GEN-LAST:event_jButtonClearActionPerformed

    private void jButtonNextTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextTestActionPerformed
        try {
            loadNodes();
        } catch (SQLException ex) {
            Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonNextTestActionPerformed

    private void jButtonNextNodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextNodeActionPerformed
        try {
            // show details specification dialog
            if (this.loadDataDialog == null) {
                JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
                this.loadDataDialog = new jDialogLoadData(mainFrame, true);
                this.loadDataDialog.setLocationRelativeTo(mainFrame);
                this.loadDataDialog.setParentPanel(this);
            }
            
            Dimension size = new Dimension(355, 390);
            Rectangle bounds = this.loadDataDialog.getBounds();
            this.loadDataDialog.setMinimumSize(size);
            this.loadDataDialog.setPreferredSize(size);
            this.loadDataDialog.setBounds(bounds.x, bounds.y, size.width, size.height);
            this.loadDataDialog.setResizable(false);

            RSSI_graphApp.getApplication().show(this.loadDataDialog);

            // TODO add your handling code here:
            // this.loadData();
        } catch (Exception ex) {
            Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonNextNodeActionPerformed

    private void jButtonRefreshDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefreshDatabaseActionPerformed
        try {
            // TODO add your handling code here:
            reloadDatabase();
        } catch (SQLException ex) {
            Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonRefreshDatabaseActionPerformed

    private void jButtonNextTXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextTXActionPerformed
        // TODO add your handling code here:
        if (this.dataParameters == null) {
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            this.dataParameters = new JDialogAddToGraph(mainFrame, true);
            this.dataParameters.setLocationRelativeTo(mainFrame);
            this.dataParameters.setParentPanel(this);
        }
        
        //OptionsUtils.setDefaultOption(options, "XvalueGroup", null);
        //this.dataParameters.setXvaluegroup(OptionsUtils.getOption(, TOOL_TIP_TEXT_KEY));

        Dimension size = new Dimension(220, 310);
        Rectangle bounds = this.dataParameters.getBounds();
        this.dataParameters.setMinimumSize(size);
        this.dataParameters.setPreferredSize(size);
        this.dataParameters.setBounds(bounds.x, bounds.y, size.width, size.height);
        this.dataParameters.setResizable(false);
        
        RSSI_graphApp.getApplication().show(this.dataParameters);
    }//GEN-LAST:event_jButtonNextTXActionPerformed

    private void jButtonNextTX2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextTX2ActionPerformed
        // TODO add your handling code here:
        RSSI_graphApp.getApplication().getGraphViewFrame().getjPannelRSSI2DistanceChart1().clear();
    }//GEN-LAST:event_jButtonNextTX2ActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        // export currently selected data to CSV
        // in order defined in last table
        this.exportData();
        this.logToWindow("Data export finished", JPannelLoggerLogElement.SEVERITY_INFO);
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonLocalizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLocalizeActionPerformed
        // TODO add your handling code here:
        // localize node from data
        // use meassured data to determine mobile node position

        // by default in each bucket is one localizable data set
        // (testno, mobile node, tx power) forms localizable data set.
        // future work: if wanted to localizae data among tests, better implementation would be required
        // GUI as well (assigning data to specified localizable sets, mapping)
        
        // window
        // TODO add your handling code here:
        if (this.localizationDialog == null) {
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            this.localizationDialog = new JDialogLocalization(mainFrame, true);
            this.localizationDialog.setLocationRelativeTo(mainFrame);
            this.localizationDialog.setParentPanel(this);
            this.localizationDialog.reloadData();
            
            // fix some window parameters (size)
            Dimension curDimension = this.localizationDialog.getPreferredSize();
            Rectangle bounds = this.localizationDialog.getBounds();
        
            this.localizationDialog.setMinimumSize(curDimension);
            this.localizationDialog.setPreferredSize(curDimension);
            this.localizationDialog.setBounds(bounds.x, bounds.y, curDimension.width, curDimension.height);
            this.localizationDialog.setResizable(true);
        }
        
        this.localizationDialog.refresView();
        RSSI_graphApp.getApplication().show(this.localizationDialog);
        
        return;
    }//GEN-LAST:event_jButtonLocalizeActionPerformed

    private void jButtonCalibrationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalibrationActionPerformed
        // window
        // TODO add your handling code here:
        if (this.calibrationDialog == null) {
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            this.calibrationDialog = new JDialogCalibration(mainFrame, true);
            this.calibrationDialog.setLocationRelativeTo(mainFrame);
            this.calibrationDialog.setParentPanel(this);
            
            // fix some window parameters (size)
            Dimension curDimension = this.calibrationDialog.getPreferredSize();
            Rectangle bounds = this.calibrationDialog.getBounds();
        
            this.calibrationDialog.setMinimumSize(curDimension);
            this.calibrationDialog.setPreferredSize(curDimension);
            this.calibrationDialog.setBounds(bounds.x, bounds.y, curDimension.width, curDimension.height);
            this.calibrationDialog.setResizable(true);
        }
        
        this.calibrationDialog.refreshData();
        RSSI_graphApp.getApplication().show(this.calibrationDialog);
    }//GEN-LAST:event_jButtonCalibrationActionPerformed

    /**
     * Background task for test loading.
     * It is very time consuming thus load data from sql backend in background task
     * while blocking GUI and displaying progress & messages. Cancelable..
     */
     private class LoadTestWorker extends Task<Boolean, Void> { 
        jPannelRSSI2DistanceDataLoader parent;
        NodeTableModel tmp;
        Connection conn;
        TableModel tmod;
        int[] sel;
        String whereString;
        
        Map<Integer, Integer> mid2testno;
        
        LoadTestWorker(jPannelRSSI2DistanceDataLoader parent){
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to BlockApplicationTask fields, here.
                    
            super(RSSI_graphApp.getApplication());
            setUserCanCancel(true);
            this.parent = parent;
            
            // copy data from EDT
            if (parent==null) return;
            this.conn = parent.conn;
            this.tmod = parent.jTableTest.getModel();
            
            // get selected nodes
            sel = parent.jTableTest.getSelectedRows();
            tmp = new NodeTableModel();
            
            // get data from selected table
            if (!(tmod instanceof TestTableModel)){
                throw new IllegalStateException("TableTest is excepted to has TableModel: TestTableModel");
            }

            // testno -> mid mapping
            mid2testno = new HashMap<Integer, Integer>();

            // convert type
            TestTableModel tblModel = (TestTableModel) tmod;

            // build mid IN(a,b,c) statement
            StringBuilder sb = new StringBuilder(64);
            for(int i=0, cn=sel.length; i<cn; i++){
                // we need to select MID from selected table rows
                // format for SQL IN statement directly
                if (i>0) sb.append(",");

                // selected rowID need to be converted to model row id since after
                // sorting rows does not match
                int selectedRow = parent.jTableTest.convertRowIndexToModel(sel[i]);
                Object curMid = tblModel.getValueAt(selectedRow, 1);
                Object testno = tblModel.getValueAt(selectedRow, 0);
                sb.append(curMid);
                mid2testno.put((Integer)curMid, (Integer)testno);
            }
            
            whereString = sb.toString();
        }

        /**
         * Fill dataBuffer from database in background task.
         * @return
         * @throws InterruptedException 
         */
        @Override
        protected Boolean doInBackground() throws InterruptedException {
            // progress message
            setMessage("Initializing");
            // set progress to task monitor
            setProgress(0, 0, 100);
            
            this.setProgress(2);
            
            // determine selected test numbers from table
            if (sel==null || sel.length==0) return true;

            try {
                // get data 
                Statement stat = conn.createStatement();
                String sqlQuery = "SELECT mid,source,txpower FROM rssi2dist_measurement_data "
                        + " WHERE mid IN("+whereString+") "
                        + " GROUP BY mid,source,txpower"
                        + " ORDER BY mid,source,txpower";
                
                setMessage("Waiting for data");
                ResultSet rs = stat.executeQuery(sqlQuery);

                 // data fetching, own column types and fetcher
                String[] columnNames = {"testno", "mid", "source", "txpower"};
                String[] columnTypes = {"integer", "integer", "integer", "integer"};

                // using own data fetcher to fetch testno column correctly from internal map
                dbUtilsDataFetcher dataFetcher = new dbUtilsDataFetcher(){
                    @Override
                    public boolean hasDefinedOwnResult(int column) {
                        return column==0;
                    }

                    @Override
                    public Object getOwnResult(int column, String columnName, String columnType, ResultSet rs) {
                        try {
                            return new Integer(mid2testno.get(new Integer(rs.getInt("mid"))));
                        } catch (SQLException ex) {
                            Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return null;
                    }
                };

                setMessage("Processing data");
                LinkedList<ArrayList<Object>> tmpData = dbUtils.fetchDataByColumns(rs, columnNames, columnTypes, dataFetcher);
                if (tmpData==null || tmpData.size()==0) return false;

                tmp.setData(dbUtils.fetchedResultToObjectArray(tmpData));

                // prepare for garbage collector
                tmpData=null;
                rs.close();
                
                setMessage("Finished");
            } catch(SQLException ex) {
                setMessage("Something went wrong: " + ex.getMessage());

                // cancel task on error
                this.cancel(true);
            } catch(Exception ex){
                setMessage("Something went wrong: " + ex.getMessage());

                // cancel task on error
                this.cancel(true);
            }
            
            return true;
        }

        @Override
        protected void succeeded(Boolean result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            
            if (result) setMessage("Done");
            else { 
                setMessage("Canceled");
                return;
            }
            
            // set table model for table
            this.parent.jTableNode.setModel(tmp);
            this.parent.jTableNode.repaint();
                
            // enable loading next data
            this.parent.jButtonNextNode.setEnabled(true);
            this.parent.logToWindow("Node details loading completed", JPannelLoggerLogElement.SEVERITY_DEBUG);
        }
        
        @Override
        protected void finished() {
            super.finished();
            
            // remove task
            if (this.parent!=null)
                this.parent.dataLoadWorker=null;
        }

        @Override
        protected void cancelled() {
            setMessage("Canceled");
            try {
                // reload database
                if (this.parent!=null)
                    this.parent.reloadDatabase();
            } catch (SQLException ex) {
                Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }       
    }
    
     @Action(block = Task.BlockingScope.APPLICATION)
     public Task loadTestBackground() {
        Task task = new LoadTestWorker(this); 
        //task.setInputBlocker(new BusyIndicatorInputBlocker(task, new BusyIndicator()));
        return task;
    }
     
    /**
     * Background task for data loading.
     * It is very time consuming thus load data from sql backend in background task
     * while blocking GUI and displaying progress & messages. Cancelable..
     */
     private class LoadDataWorker extends Task<Boolean, Double> {
         Map<String, Object> options;
         LinkedList<RSSI2DistInternalBuffer> dataBuffer;
         String[] sqlWhere;
         Connection conn;
         int[] mids;
         int[] source;
         int[] txpower;
         
         jPannelRSSI2DistanceDataLoader parent;
         
        LoadDataWorker() {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to BlockApplicationTask fields, here.
                    
            super(RSSI_graphApp.getApplication());
            setUserCanCancel(true);
            
            // copy data from EDT
        }

        /**
         * Fill dataBuffer from database in background task.
         * @return
         * @throws InterruptedException 
         */
        @Override
        protected Boolean doInBackground() throws InterruptedException {
            // progress message
            setMessage("Initializing");
            // set progress to task monitor
            setProgress(0, 0, 100);
            
            try {
                // create new data buffer list
                // store loaded data here
                dataBuffer = new LinkedList<RSSI2DistInternalBuffer>();

                // data fetching, own column types and fetcher
                String[] columnNames = {"rssi"};
                String[] columnTypes = {"integer"};
                String metaSqlWhere = "SELECT testno, distance, mobileNodeId FROM rssi2dist_measurement WHERE mid=?;";
                PreparedStatement prep = conn.prepareStatement(metaSqlWhere);
                
                for (int i = 0, cn = sqlWhere.length; i < cn; i++) {
                    // progress message
                    setMessage("Loading data set [" + (i+1) + "/"+cn+"]");
                    // set progress to task monitor
                    setProgress(i, 0, cn);
                    
                    // query for testno, distance, mobileNode
                    int testno = 0;
                    int distance = 0;
                    int mobileNode = 0;

                    prep.setInt(1, mids[i]);
                    ResultSet rs = prep.executeQuery();
                    if (rs.next()) {
                        testno = rs.getInt("testno");
                        distance = rs.getInt("distance");
                        mobileNode = rs.getInt("mobileNodeId");
                    } else {
                        //ERROR cannot load data. no such metadata exists
                        throw new IllegalStateException("Cannot load metadata for mid=" + mids[i] + ". Cannot continue.");
                    }

                    rs.close();

                    // load samples deta
                    String singleSqlWhere = "SELECT rssi FROM rssi2dist_measurement_data WHERE " + sqlWhere[i] + ";";

                    // get data samples now
                    Statement stat = conn.createStatement();
                    rs = stat.executeQuery(singleSqlWhere);

                    // fetch to linked list
                    LinkedList<ArrayList<Object>> tmpData = dbUtils.fetchDataByColumns(rs, columnNames, columnTypes);
                    if (tmpData == null || tmpData.size() == 0) {
                        logToWindow("No data for mid=" + mids[i], JPannelLoggerLogElement.SEVERITY_INFO);
                        continue;
                    }

                    Object[][] integerArray = dbUtils.fetchedResultToObjectArray(tmpData);

                    // fetch data, compute statistics, fit to curve and pass these data to the graph
                    RSSI2DistInternalBuffer tmpBuffer = new RSSI2DistInternalBuffer(testno, mids[i], txpower[i], distance, mobileNode, source[i]);

                    // xvalue calculation
                    Double paramSource = null;
                    Integer paramSourceIndex = (Integer) OptionsUtils.getOption(options, "ParamSource");
                    if (paramSourceIndex==0) paramSource = Double.valueOf(tmpBuffer.getDistance());
                    else if (paramSourceIndex==1) paramSource = Double.valueOf(tmpBuffer.getMid());
                    else if (paramSourceIndex==2) paramSource = Double.valueOf(tmpBuffer.getReportingMote());
                    else if (paramSourceIndex==3) paramSource = Double.valueOf(tmpBuffer.getTxpower());

                    Double paramDirectMulti = (Double) OptionsUtils.getOption(options, "ParamDirectMulti");
                    Double paramConstant = (Double) OptionsUtils.getOption(options, "ParamConstant");
                    double xvalue = paramSource * paramDirectMulti + paramConstant;
                    tmpBuffer.setXvalue(xvalue);

                    // import data and calculate statistics
                    tmpBuffer.loadData(integerArray);
                    tmpBuffer.computeStats();

                    // add to list
                    this.dataBuffer.add(tmpBuffer);
                    rs.close();
                    
                    // set progress to task monitor
                    setProgress(i+1, 0, cn);
                }
                
                prep.close();
            } catch(Exception e){
                setMessage("Something went wrong: " + e.getMessage());

                // cancel task on error
                this.cancel(true);
            }
            
            return true;
        }

        @Override
        protected void succeeded(Boolean result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            
            if (result) setMessage("Done");
            else setMessage("Canceled");
            
            // copy result to EDT
            this.parent.dataBuffer = this.dataBuffer;
            
            try {
                // post process
                this.parent.postDataLoad(options);
            } catch (FunctionEvaluationException ex) {
                Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OptimizationException ex) {
                Logger.getLogger(jPannelRSSI2DistanceDataLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        protected void finished() {
            super.finished();
            
            // remove task
            if (this.parent!=null)
                this.parent.dataLoadWorker=null;
        }

        @Override
        protected void cancelled() {
            setMessage("Canceled");
        }

        @Override
        protected void process(List<Double> values) {
            super.process(values);
        }
        
        public LinkedList<RSSI2DistInternalBuffer> getDataBuffer() {
            return dataBuffer;
        }

        public void setDataBuffer(LinkedList<RSSI2DistInternalBuffer> dataBuffer) {
            this.dataBuffer = dataBuffer;
        }

        public Map<String, Object> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Object> options) {
            this.options = options;
        }

        public String[] getSqlWhere() {
            return sqlWhere;
        }

        public void setSqlWhere(String[] sqlWhere) {
            this.sqlWhere = sqlWhere;
        }

        public Connection getConn() {
            return conn;
        }

        public void setConn(Connection conn) {
            this.conn = conn;
        }

        public int[] getMids() {
            return mids;
        }

        public void setMids(int[] mids) {
            this.mids = mids;
        }

        public int[] getSource() {
            return source;
        }

        public void setSource(int[] source) {
            this.source = source;
        }

        public int[] getTxpower() {
            return txpower;
        }

        public void setTxpower(int[] txpower) {
            this.txpower = txpower;
        }

        public jPannelRSSI2DistanceDataLoader getParent() {
            return parent;
        }

        public void setParent(jPannelRSSI2DistanceDataLoader parent) {
            this.parent = parent;
        }
        
    }
     
     @Action(block = Task.BlockingScope.APPLICATION)
     public Task loadDataBackground() {
        Task task = this.dataLoadWorker;//new LoadDataWorker();
        //task.setInputBlocker(new BusyIndicatorInputBlocker(task, new BusyIndicator()));
        return task;
    }

    /** Progress is interdeterminate for the first 150ms, then
     * run for another 7500ms, marking progress every 150ms.
     */
    private class DoNothingTask extends Task<Boolean, Double> {

        DoNothingTask() {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to BlockApplicationTask fields, here.
                    
            super(RSSI_graphApp.getApplication());
            setUserCanCancel(true);
        }

        @Override
        protected Boolean doInBackground() throws InterruptedException {
            for (int i = 0; i < 50; i++) {
                setMessage("Working... [" + i + "]");
                Thread.sleep(150L);
                setProgress(i, 0, 49);
            }
            
            Thread.sleep(150L);
            return true;
        }

        @Override
        protected void succeeded(Boolean result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().
            
            if (result) setMessage("Done");
            else setMessage("Canceled");
        }

        @Override
        protected void finished() {
            super.finished();
        }

        @Override
        protected void cancelled() {
            setMessage("Canceled");
        }

        @Override
        protected void process(List<Double> values) {
            super.process(values);
        }
    }

    @Action(block = BlockingScope.ACTION)
    public Task blockAction() {
        return new DoNothingTask();
    }

    @Action(block = BlockingScope.COMPONENT)
    public Task blockComponent() {
        return new DoNothingTask();
    }

    @Action(block = Task.BlockingScope.WINDOW)
    public Task blockWindow() {
        return new DoNothingTask();
    }

        @Action(block = Task.BlockingScope.APPLICATION)
    public Task blockApplication() {
        Task task = new DoNothingTask();
        //task.setInputBlocker(new BusyIndicatorInputBlocker(task, new BusyIndicator()));
        return task;
    }

    /* This component is intended to be used as a GlassPane.  It's
     * start method makes this component visible, consumes mouse
     * and keyboard input, and displays a spinning activity indicator
     * animation.  The stop method makes the component not visible.
     * The code for rendering the animation was lifted from
     * org.jdesktop.swingx.painter.BusyPainter.  I've made some
     * simplifications to keep the example small.
     */
    private static class BusyIndicator extends JComponent implements ActionListener {

        private int frame = -1;  // animation frame index
        private final int nBars = 8;
        private final float barWidth = 6;
        private final float outerRadius = 28;
        private final float innerRadius = 12;
        private final int trailLength = 4;
        private final float barGray = 200f;  // shade of gray, 0-255
        private final Timer timer = new Timer(65, this); // 65ms = animation rate

        BusyIndicator() {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MouseInputListener blockMouseEvents = new MouseInputAdapter() {
            };
            addMouseMotionListener(blockMouseEvents);
            addMouseListener(blockMouseEvents);
            InputVerifier retainFocusWhileVisible = new InputVerifier() {

                public boolean verify(JComponent c) {
                    return !c.isVisible();
                }
            };
            setInputVerifier(retainFocusWhileVisible);
        }

        public void actionPerformed(ActionEvent ignored) {
            frame += 1;
            repaint();
        }

        void start() {
            setVisible(true);
            requestFocusInWindow();
            timer.start();
        }

        void stop() {
            setVisible(false);
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            RoundRectangle2D bar = new RoundRectangle2D.Float(
                    innerRadius, -barWidth / 2, outerRadius, barWidth, barWidth, barWidth);
// x,         y,          width,       height,   arc width,arc height
            double angle = Math.PI * 2.0 / (double) nBars; // between bars
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(getWidth() / 2, getHeight() / 2);
            g2d.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < nBars; i++) {
// compute bar i's color based on the frame index
                Color barColor = new Color((int) barGray, (int) barGray, (int) barGray);
                if (frame != -1) {
                    for (int t = 0; t < trailLength; t++) {
                        if (i == ((frame - t + nBars) % nBars)) {
                            float tlf = (float) trailLength;
                            float pct = 1.0f - ((tlf - t) / tlf);
                            int gray = (int) ((barGray - (pct * barGray)) + 0.5f);
                            barColor = new Color(gray, gray, gray);
                        }
                    }
                }
// draw the bar
                g2d.setColor(barColor);
                g2d.fill(bar);
                g2d.rotate(angle);
            }
        }
    }

    /**
     * Input blocker
     */
    private class BusyIndicatorInputBlocker extends InputBlocker {
        private BusyIndicator busyIndicator;
        public BusyIndicatorInputBlocker(Task task, BusyIndicator busyIndicator) {
            super(task, Task.BlockingScope.APPLICATION, busyIndicator);
            this.busyIndicator = busyIndicator;
        }

        @Override
        protected void block() {
            busyIndicator.start();
        }

        @Override
        protected void unblock() {
            busyIndicator.stop();
        }
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCalibration;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonLocalize;
    private javax.swing.JButton jButtonNextNode;
    private javax.swing.JButton jButtonNextTX;
    private javax.swing.JButton jButtonNextTX2;
    private javax.swing.JButton jButtonNextTest;
    private javax.swing.JButton jButtonRefreshDatabase;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable jTableModel;
    private javax.swing.JTable jTableNode;
    private javax.swing.JTable jTableTest;
    private javax.swing.JTextArea jTextAreaDataStatistics;
    // End of variables declaration//GEN-END:variables

}


