/*
 * JPanelNetworkMap.java
 *
 * Created on Apr 17, 2011, 11:21:12 AM
 */

package rssi_localization.localization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ujmp.core.collections.ArrayIndexList;
import rssi_localization.JPannelLoggerLogElement;
import rssi_localization.RSSI_graphApp;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.MobileNode;
import rssi_graph.nodeRegister.MobileNodeManager;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.nodeRegister.NodeRegisterEventListener;
import rssi_graph.nodeRegister.SimpleGenericNode;
import rssi_graph.utils.JFreeChartShapeCross;
import rssi_graph.utils.JFreeChartShapeTriangle;

/**
 * Component visualizing sensor network, localization process.
 * Component is interactive (zoomable, clickable, tooltips).
 * Use may click on each node to select it. Selected nodes has displayed additional information.
 * 
 * Network map supports history tracking to store last X records in localization history.
 * Draws distance estimations, real vs. estimated position, errors.

 * 
 * @author ph4r05
 */
public class JPanelNetworkMap extends javax.swing.JPanel implements NodeRegisterEventListener, ActionListener,
        ChartMouseListener,MouseListener,MouseMotionListener{

    protected NodeRegister nodeRegister = null;
    protected MobileNodeManager mnm = null;

    private Timer grabTimer = null;
    private WorkerLocalization worker = null;
    private Map<String, TimeSeries> timeSeries = null;
    private TimeSeriesCollection timeDataSets = null;
    private boolean redrawGraphNeeded=false;
    private boolean jpannelDrawed=false;

    /**
     * Boolean map of displayed nodes
     */
    private Map<Integer, Boolean> displayedNodes = null;

    /**
     * Dataset represents nodes on graph
     */
    private NetworkMapXYDataset graphNodes = null;

    /**
     * Jfreechart
     */
    private JFreeChart jfreechart=null;

    /**
     * Chart panel
     */
    private ChartPanel chp=null;

    /**
     * Instance of settings dialog for network map
     */
    private JDialogNetworkMapSettings settingsDialog = null;

    /**
     * Force to keep 1:1 axis ratio
     */
    private boolean keep1to1axisRatio=false;
    
    /**
     * Flag determining whether to show error line connecting real position
     * and computed position.
     */
    private boolean showError=true;
    
    /**
     * if true then display mobile text annotations with arrows
     */
    private boolean showMobileTextAnnot=true;
    
    /**
     * if true then display anchor text annotations with arrows
     */
    private boolean showStaticTextAnnot=true;
    
    /**
     * if true then display circles arround anchors
     */
    private boolean showDistances=true;
    
    /**
     * show estimate history ?
     */
    private boolean showEstimateHistory=true;
    
    /**
     * Show estimate node position and error only for selected node?
     */
    private boolean showEstimateOnlyForSelected=true;
    
    /**
     * max size of history
     */
    private int maxHistorySize=25;
    
    /**
     * Position estimate history
     */
    private Map<Integer, List<LocalizationEstimate>> estimateHistory;
    
    /**
     * Currently selected node from chart
     */
    private int selectedNode=-1;
    
    /**
     * Is selected real position or estimations?
     */
    private ClickBestMatch selected=null;
    
    /**
     * Network nodes positions. This is data source for dataset.
     */
    private Map<String, Set<CoordinateRecord>> networkNodesPositions;

    /** Creates new form JPanelNetworkMap */
    public JPanelNetworkMap() {
        initComponents();
    }

    /**
     * Initialization method invoked from parent module after first start.
     * (during splash screen)
     * 
     */
    public void initThis(){
        this.displayedNodes = new HashMap<Integer, Boolean>();
        this.graphNodes = new NetworkMapXYDataset();
        this.estimateHistory = new HashMap<Integer, List<LocalizationEstimate>>();

        this.nodeRegister.addChangeListener(this);
        XYPolygonAnnotationDemo1("test");

        // alter popup menu, add MapSettings to pop up menu
        JMenuItem jMenuItem = new JMenuItem("Map properties...");
        jMenuItem.setActionCommand("MapSettings");
        jMenuItem.addActionListener((ActionListener) this);
        this.chp.getPopupMenu().addSeparator();
        this.chp.getPopupMenu().add(jMenuItem);

        // alter popup menu, add redraw
        JMenuItem jMenuItemRedraw = new JMenuItem("Redraw");
        jMenuItemRedraw.setActionCommand("Redraw");
        jMenuItemRedraw.addActionListener((ActionListener) this);
        this.chp.getPopupMenu().addSeparator();
        this.chp.getPopupMenu().add(jMenuItemRedraw);

        // 1:1 axis ratio
        

        // init settings dialog
        JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
        this.settingsDialog = new JDialogNetworkMapSettings(mainFrame, true);
        this.settingsDialog.setLocationRelativeTo(mainFrame);
        this.settingsDialog.setParentPanel(this);
        
        //init
        this.networkNodesPositions = new HashMap<String, Set<CoordinateRecord>> ();

        // first redraw here
        this.redraw();
    }

    /**
     * Redraw whole network map.
     * Map is reconstructed from saved data in register and in history.
     */
    public synchronized void redraw(){        
        networkNodesPositions.clear();
        
        // remove all annotations
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)this.jfreechart.getXYPlot().getRenderer();
        xylineandshaperenderer.removeAnnotations();

        this.drawAnchors();

        // draw mobile nodes according to internal settings
        // settings could be set via popup menu
        // draw mobile nodes
        this.drawMobileNodes();
        
        // clear dataset
        this.graphNodes.clear();
        
        // add collected datasets
        Iterator<String> dataIt = networkNodesPositions.keySet().iterator();
        while(dataIt.hasNext()){
            String curKey = dataIt.next();
            Set<CoordinateRecord> set = networkNodesPositions.get(curKey);
            if (set==null) continue;
            
            int curSize = set.size();
            double[][] tmpData = new double[2][curSize];
            
            Iterator<CoordinateRecord> itSet = set.iterator();
            for(int ti=0; itSet.hasNext(); ti++){
                CoordinateRecord next = itSet.next();
                tmpData[0][ti] = next.x; 
                tmpData[1][ti] = next.y;
            }
            
            this.graphNodes.addSeries(curKey, tmpData);
        }
    }
    
    /**
     * Adds point to node positions sets
     * @param key
     * @param point 
     */
    public void addToPositions(String key, CoordinateRecord point){
        if (this.networkNodesPositions==null) return;
        if (!this.networkNodesPositions.containsKey(key)){
            this.networkNodesPositions.put(key, new HashSet<CoordinateRecord>());
        }
        
        Set<CoordinateRecord> set = this.networkNodesPositions.get(key);
        set.add(point);
        
        this.networkNodesPositions.put(key, set);
    }
    
    /**
     * returns clicked node on graph with precision of click < prec.
     * Returns best match (result are sorted and with best match is returned).
     * 
     * Takes into account also history if enabled.
     * @param x
     * @param y
     * @return 
     */
    private ClickBestMatch getClickedNode(double x, double y, double prec){
        // iterate over nodes and compute best match
        // precision sorter
        ArrayList<ClickBestMatch> clickErrors = new ArrayList<ClickBestMatch>(this.nodeRegister.getNodesSet().size()*2);
        Iterator<Integer> iterator = this.nodeRegister.getNodesSet().iterator();
        
        while(iterator.hasNext()){
            Integer curNode = iterator.next();
            GenericNode node = this.nodeRegister.getNode(curNode);
            if (node==null) continue;
            
            // real position
            if (node.getPosition()!=null){
                CoordinateRecord position = node.getPosition();
                double errx = (position.getX() - x);
                double erry = (position.getY() - y);
                
                ClickBestMatch click = new ClickBestMatch();
                click.node = node;
                click.nodeid = node.getNodeId();
                click.realPos = true;
                click.error = errx*errx + erry*erry;
                clickErrors.add(click);
            }
            
            // computed
            if (node.getMobileExtension()!=null && node.getMobileExtension().getComputedPosition()!=null){
                CoordinateRecord computedPosition = node.getMobileExtension().getComputedPosition();
                double errx = (computedPosition.getX() - x);
                double erry = (computedPosition.getY() - y);
                
                ClickBestMatch click = new ClickBestMatch();
                click.node = node;
                click.nodeid = node.getNodeId();
                click.realPos = false;
                click.error = errx*errx + erry*erry;
                clickErrors.add(click);
            }
            
            // now assume history
            if (this.isShowEstimateHistory()){
                List<LocalizationEstimate> history = this.getHistory(node.getNodeId());
                Iterator<LocalizationEstimate> iterator1 = history.iterator();
                for(int ind=0; iterator1.hasNext(); ind++){
                    LocalizationEstimate next = iterator1.next();
                    if (next==null) continue;
                    
                    // real position
                    if (next.getRealPosition()!=null){
                        CoordinateRecord position = next.getRealPosition();
                        double errx = (position.getX() - x);
                        double erry = (position.getY() - y);

                        ClickBestMatch click = new ClickBestMatch();
                        click.node = node;
                        click.nodeid = node.getNodeId();
                        click.realPos = true;
                        click.error = errx*errx + erry*erry;
                        click.fromHistory=true;
                        click.historyIndex=ind;
                        clickErrors.add(click);
                    }

                    // computed
                    if (next.getEstimatedPosition()!=null){
                        CoordinateRecord computedPosition = next.getEstimatedPosition();
                        double errx = (computedPosition.getX() - x);
                        double erry = (computedPosition.getY() - y);

                        ClickBestMatch click = new ClickBestMatch();
                        click.node = node;
                        click.nodeid = node.getNodeId();
                        click.realPos = false;
                        click.fromHistory=true;
                        click.historyIndex=ind;
                        click.error = errx*errx + erry*erry;
                        clickErrors.add(click);
                    }
                }
            }
        }
        
        // empty => no node
        if (clickErrors.isEmpty()) return null;
        
        //sort
        Collections.sort(clickErrors);
        
        // return best match, under precision?
        ClickBestMatch click = clickErrors.get(0);
        if (click.error > prec) return null;
        
        return click;
    }

    /**
     * Add position estimate to estimate history
     * @param est 
     */
    public void addToEstimateHistory(LocalizationEstimate est){
        if (est==null) return;
        
        // get history for this node
        List<LocalizationEstimate> estList = this.getHistory(est.getMobileNodeId());
        
        estList.add(est);
        this.estimateHistory.put(est.getMobileNodeId(), estList);
    }
    
    /**
     * Flush entries from estimate history
     * @param nodeid if NULL, whole history is flushed. Otherwise only particular 
     * nodeid history (if exists)
     */
    public void flushEstimateHistory(Integer nodeid){
        if (nodeid==null){
            this.estimateHistory=new HashMap<Integer, List<LocalizationEstimate>>();
            return;
        }
        
        if (this.estimateHistory.containsKey(nodeid)){
            this.estimateHistory.remove(nodeid);
        }
    }
    
    /**
     * Returns estimate list for given node.
     * If does not exists, creates new one
     * @param nodeid
     * @return 
     */
    public List<LocalizationEstimate> getHistory(int nodeid){
        List<LocalizationEstimate> estList = null;
        if (this.estimateHistory.containsKey(nodeid)){
            estList = this.estimateHistory.get(nodeid);
        } else {
            estList = new ArrayList<LocalizationEstimate> (this.getMaxHistorySize());
        }
        
        return estList;
    }
    
    /**
     * Returns particular estimation from history
     * @param nodeid
     * @param index
     * @return 
     */
    public LocalizationEstimate getHistory(int nodeid, int index){
        List<LocalizationEstimate> history = this.getHistory(nodeid);
        if (index>=history.size() || index<0) return null;
        
        return history.get(index);
    }
    
    /**
     * Prunes history, keeps only last maxEntries in given
     * @param nodeid
     * @param maxEntries 
     */
    public void pruneHistory(Integer nodeid, int maxEntries){
        if (this.estimateHistory==null) return;
        
        if (nodeid==null){
            // prune whole
            Iterator<Integer> iterator = this.estimateHistory.keySet().iterator();
            while(iterator.hasNext()){
                Integer curInt = iterator.next();
                
                // watch for nulls here not to fall to infinite recursion
                if (curInt==null){
                    continue;
                }
                
                this.pruneHistory(curInt, maxEntries);
            }
        } else {
            // prune only given
            if (!this.estimateHistory.containsKey(nodeid)) return;
            List<LocalizationEstimate> history = this.getHistory(nodeid);
            int size = history.size();
            
            // nothing to do, its ok
            if (size<maxEntries) return;
            
            // sublist
            for(int i=0; i<(size-maxEntries);i++){
               history.remove(0);
            }
            
            // re-put
            this.estimateHistory.put(nodeid, history);
        }
    }
    
    
    /**
     * Should display this node?
     * 
     * @param nodeid
     * @return 
     */
    public boolean displayNode(int nodeid){
        Integer curNodeId = Integer.valueOf(nodeid);
        
        // should mobile node be drawed?
        // skip node only if record exists in displayedNodes and is false
        if (this.displayedNodes!=null && this.displayedNodes.containsKey(curNodeId)){
            if (this.displayedNodes.get(curNodeId).booleanValue() == false) return false;
        }
        
        return true;
    }
    
    /**
     * Draw single mobile node from localization estimate
     * @param est 
     */
    public synchronized void drawSingleMobileNode(LocalizationEstimate est){
        if (est==null) return;
        
        DecimalFormat df = new DecimalFormat("00.00");
        String annotReal= est.getRealPosition() != null ? 
                "RealPos: X=" + df.format(est.getRealPosition().getX()) + 
                        "; Y=" + df.format(est.getRealPosition().getY())
                : null;
                          
        String annotComp=est.getRealPosition() != null ? 
                "EstPos: X=" + df.format(est.getEstimatedPosition().getX()) + 
                        "; Y=" + df.format(est.getEstimatedPosition().getY())
                : null;
        
        boolean active=false;
        if (this.isShowEstimateHistory() 
                && this.getSelected()!=null 
                && this.getSelected().nodeid == est.getMobileNodeId()
                && this.getSelected().fromHistory==true){
            
            LocalizationEstimate history = this.getHistory(est.getMobileNodeId(), this.getSelected().historyIndex);
            active = history!=null && history.equals(est);
        }
        
        this.drawSingleMobileNode(est.getMobileNodeId(), 
                est.getRealPosition(), 
                est.getEstimatedPosition(),
                est.getDistancesFromAnchors(), 
                annotReal, annotComp, active, active);
    }
    
    /**
     * Draw single mobile node to network graph
     * 
     * @param nodeid
     * @param realRec
     * @param compRec
     * @param distancesFromAnchors
     * @param textAnnotationComp
     * @param textAnnotationReal 
     */
    public synchronized void drawSingleMobileNode(int nodeid, 
            CoordinateRecord realRec, 
            CoordinateRecord compRec, 
            Map<Integer, Double> distancesFromAnchors,
            String textAnnotationReal,
            String textAnnotationComp,
            boolean drawDistances,
            boolean isSelected){
        
        // graph renderer
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)this.jfreechart.getXYPlot().getRenderer();
        
        // decimal format to rounding
        DecimalFormat df = new DecimalFormat("00.00");
        Integer curNodeId = Integer.valueOf(nodeid);

        // define serie key for real/computed
        String serieNameComputed = "M.C. " + nodeid;
        String serieNameReal = "M.R. " + nodeid;

        // should mobile node be drawed?
        // skip node only if record exists in displayedNodes and is false
        if (this.displayNode(nodeid)==false) return;

        
        // if there is no position to display, skip
        if (realRec==null && compRec==null) return;

        // color of drawed dataset
        Color serieColor = null;

        //
        // real position
        //
        if (realRec!=null){            
            this.graphNodes.addSeries(serieNameReal, new double[][] {{realRec.getX()},{realRec.getY()}});
            this.addToPositions(serieNameReal, realRec);
            
            int serieIndex = this.graphNodes.indexOf(serieNameReal);
            Shape serieShape = new JFreeChartShapeTriangle();

            // get serie paint
            Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(serieIndex);
            if (seriesPaint instanceof Color){
                serieColor = (Color) seriesPaint;
            }

            if ((this.isShowMobileTextAnnot() || isSelected) && textAnnotationReal!=null){
                // prepare annotations
                XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(textAnnotationReal, realRec.getX(), realRec.getY(), -0.78539816339744828D);
                xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                xypointerannotation.setPaint(Color.blue);
                xypointerannotation.setArrowPaint(Color.blue);
                xypointerannotation.setToolTipText(textAnnotationReal);
                xylineandshaperenderer.addAnnotation(xypointerannotation);
            }

            // shape for real
            xylineandshaperenderer.setSeriesShape(serieIndex, serieShape);
        }

        //
        // computed position
        //
        // display only for selected nodes?
        if (this.isShowEstimateOnlyForSelected() && isSelected==false){
            compRec=null;
        }
        
        if (compRec!=null){
            this.graphNodes.addSeries(serieNameComputed, new double[][] {{compRec.getX()},{compRec.getY()}});
            this.addToPositions(serieNameComputed, compRec);
            
            int serieIndex = this.graphNodes.indexOf(serieNameComputed);
            Shape serieShape = new JFreeChartShapeCross();

            // serie paint
            Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(serieIndex);
            if (seriesPaint instanceof Color){
                serieColor = (Color) seriesPaint;
            }

            // prepare annotations
            if ((this.isShowMobileTextAnnot() || isSelected) && textAnnotationComp!=null){
                XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(textAnnotationComp,
                    compRec.getX(), compRec.getY(), -0.78539816339744828D);
                xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                xypointerannotation.setPaint(Color.blue);
                xypointerannotation.setArrowPaint(Color.blue);
                xypointerannotation.setToolTipText(textAnnotationComp);
                xylineandshaperenderer.addAnnotation(xypointerannotation);
            }

            // shape for computed
            xylineandshaperenderer.setSeriesShape(serieIndex, serieShape);
        }

        // synchronize colors if exists both
        if (compRec!=null && realRec!=null){
            int realIndex = this.graphNodes.indexOf(serieNameReal);
            int compIndex = this.graphNodes.indexOf(serieNameComputed);

            xylineandshaperenderer.setSeriesPaint(compIndex,
                    xylineandshaperenderer.getSeriesPaint(realIndex));

            xylineandshaperenderer.setSeriesFillPaint(compIndex,
                    xylineandshaperenderer.getSeriesFillPaint(realIndex));

            Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(realIndex);
            if (seriesPaint instanceof Color){
                serieColor = (Color) seriesPaint;
            }

            // draw error connector
            if (this.isShowError() || isSelected){
                Stroke seriesStroke = xylineandshaperenderer.getSeriesStroke(realIndex);
                if (seriesStroke==null) {
                    seriesStroke = new BasicStroke();
                }

                XYLineAnnotation lineAnnotation = 
                        new XYLineAnnotation(realRec.getX(), realRec.getY(), 
                            compRec.getX(), compRec.getY(), 
                            seriesStroke, 
                            xylineandshaperenderer.getSeriesFillPaint(realIndex));
                // compute error
                Double xerror = Math.abs(compRec.getX() - realRec.getX());
                Double yerror = Math.abs(compRec.getY() - realRec.getY());
                Double derror = Math.sqrt(xerror*xerror + yerror*yerror);

                lineAnnotation.setToolTipText("DError="+df.format(derror)
                        + "; XError=" + df.format(xerror)
                        + "; YError=" + df.format(yerror));
                xylineandshaperenderer.addAnnotation(lineAnnotation);
            }
        }

        // draw circles according to computed distances from static nodes
        // if there is some computed distance
        if (compRec!=null && distancesFromAnchors!=null && this.isShowDistances() && isSelected){
            Set<Integer> staticAnchors = distancesFromAnchors.keySet();
            Iterator<Integer> staticAnchorsIterator = staticAnchors.iterator();

            while(staticAnchorsIterator.hasNext()){
                Integer curStaticAnchor = staticAnchorsIterator.next();
                Double distance = distancesFromAnchors.get(curStaticAnchor);
                GenericNode anchor = this.nodeRegister.getNode(curStaticAnchor);
                if (anchor==null || anchor.getPosition() == null || anchor.isAnchor()==false) continue;

                int r=serieColor.getRed(),g=serieColor.getGreen(),b=serieColor.getBlue();

                XYShapeAnnotation xyshapeannotation = new XYShapeAnnotation( 
                    new Ellipse2D.Double(
                        anchor.getPosition().getX() - distance,
                        anchor.getPosition().getY() - distance,
                        2*distance,
                        2*distance),
                    new BasicStroke(1.0f),
                    new Color(r, g, b, 200),
                    new Color(r, g, b, 30));

                // set tooltip
                xyshapeannotation.setToolTipText("Anchor="+anchor.getNodeId()+"; Mobile="+nodeid+"; d=" + df.format(distance) + ";");
                xylineandshaperenderer.addAnnotation(xyshapeannotation);
            }
        }
    }
    
    /**
     * Draw mobile nodes from node register
     *
     * Loads data from mobile node manager. Fresh nodes are distinguished by text annotations.
     * Now method does not support trajectory tracking / history
     *
     * Per each mobile node are displayed 2 separate data series.
     * 1 for real position (if filled in the node register), triangle shape
     * 2 for computed/estimated position (if filled in the node register), cross shape
     * If are displayed both data series (real, est), colors are synchronized to be same.
     *
     * If particular node is blacklisted in displayNodes list, corresponding information
     * is removed from graph. (to hide node, record has to exist and set to false, otherwise is displayed)
     */
    public synchronized void drawMobileNodes(){
        // get mobile nodes from mobile node manager
        // get only fresh nodes?
        
        // prune location history
        this.pruneHistory(null, maxHistorySize);
        
        // graph renderer
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)this.jfreechart.getXYPlot().getRenderer();

        // decimal format to rounding
        DecimalFormat df = new DecimalFormat("00.00");

        // 1. display all nodes, fresh nodes draw by different color
        Set<MobileNode> freshNodes = mnm.getFreshNodes();
        Set<Integer> allNodesId = mnm.getMobileNodes();
        if (allNodesId==null || allNodesId.isEmpty()) return;

        Iterator<Integer> nodesIdIt = allNodesId.iterator();
        while(nodesIdIt.hasNext()){
            int nodeid = nodesIdIt.next();
            
            // define serie key for real/computed
            String serieNameComputed = "M.C. " + nodeid;
            String serieNameReal = "M.R. " + nodeid;

            // delete from graph if exists
            if (this.graphNodes.indexOf(serieNameComputed)!=-1){
                this.graphNodes.removeSeries(serieNameComputed);
            }

            if (this.graphNodes.indexOf(serieNameReal)!=-1){
                this.graphNodes.removeSeries(serieNameReal);
            }
            
            MobileNode curMobileNode = mnm.getMobileNode(nodeid);
            if (curMobileNode==null) continue;
            GenericNode curGenericNode = this.nodeRegister.getNode(curMobileNode.getNodeId());
            if (curGenericNode==null) continue;
            
            Integer nodeId = Integer.valueOf(curGenericNode.getNodeId());             
            
            // prepare data
            if (this.displayNode(nodeId)==false) continue;
             boolean isActive = freshNodes!=null && freshNodes.contains(curMobileNode);
            CoordinateRecord realRec = curGenericNode.getPosition();
            CoordinateRecord compRec = curMobileNode.getComputedPosition();
            Map<Integer, Double> distancesFromAnchors = curMobileNode.getDistancesFromAnchors();
            
            // draw history?
            if (this.isShowEstimateHistory()){
                List<LocalizationEstimate> history = this.getHistory(nodeId);
                Iterator<LocalizationEstimate> hIterator = history.iterator();
                for(int hcn=0; hIterator.hasNext() && hcn<this.maxHistorySize; hcn++){
                    LocalizationEstimate curEst = hIterator.next();
                    this.drawSingleMobileNode(curEst);
                }
            }
            
            String textAnnotationComp = compRec==null ? null : curMobileNode.getNodeId() +
                    (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
                    (isActive ? " Act " : " NoAct ") +
                    " (Est; "
                    + df.format(compRec.getX()) +", "
                    + df.format(compRec.getY()) +")";
            
            String textAnnotationReal = realRec==null ? null : curMobileNode.getNodeId() +
                    (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
                    (isActive ? " Act " : " NoAct ") +
                    " (Real; "
                    + df.format(realRec.getX()) +", "
                    + df.format(realRec.getY()) +")";
            
             boolean active=false;
             if (      this.getSelected()!=null 
                    && this.getSelected().nodeid == nodeId
                    && this.getSelected().fromHistory==false){

                active=true;
            }
            
            this.drawSingleMobileNode(nodeId, realRec, compRec, 
                    distancesFromAnchors, textAnnotationReal, 
                    textAnnotationComp, active, active);
//            Integer curNodeId = Integer.valueOf(curMobileNode.getNodeId());
//
//            // define serie key for real/computed
//            String serieNameComputed = "M.C. " + curMobileNode.getNodeId();
//            String serieNameReal = "M.R. " + curMobileNode.getNodeId();
//
//            // delete from graph if exists
//            if (this.graphNodes.indexOf(serieNameComputed)!=-1){
//                this.graphNodes.removeSeries(serieNameComputed);
//            }
//
//            if (this.graphNodes.indexOf(serieNameReal)!=-1){
//                this.graphNodes.removeSeries(serieNameReal);
//            }
//
//            // should mobile node be drawed?
//            // skip node only if record exists in displayedNodes and is false
//            if (this.displayedNodes!=null && this.displayedNodes.containsKey(curNodeId)){
//                if (this.displayedNodes.get(curNodeId).booleanValue() == false) continue;
//            }
//
//            // add real dataset for mobile node only if node has real coords filled in
//            CoordinateRecord realRec = curGenericNode.getPosition();
//            CoordinateRecord compRec = curMobileNode.getComputedPosition();
//            boolean isActive = freshNodes!=null && freshNodes.contains(curMobileNode);
//
//            // if there is no position to display, skip
//            if (realRec==null && compRec==null) continue;
//
//            Color serieColor = null;
//
//            //
//            // real position
//            //
//            if (realRec!=null){
//                this.graphNodes.addSeries(serieNameReal, new double[][] {{realRec.getX()},{realRec.getY()}});
//                int serieIndex = this.graphNodes.indexOf(serieNameReal);
//                Shape serieShape = new JFreeChartShapeTriangle();
//
//                // get serie paint
//                Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(serieIndex);
//                if (seriesPaint instanceof Color){
//                    serieColor = (Color) seriesPaint;
//                }
//
//                // prepare annotations
//                XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(
//                    curMobileNode.getNodeId() +
//                    (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
//                    (isActive ? " Act " : " NoAct ") +
//                    " (Real; "
//                    + df.format(realRec.getX()) +", "
//                    + df.format(realRec.getY()) +")",
//                      realRec.getX(), realRec.getY(), -0.78539816339744828D);
//
//                xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
//                xypointerannotation.setPaint(Color.blue);
//                xypointerannotation.setArrowPaint(Color.blue);
//                xypointerannotation.setToolTipText("Mobile node ID="+curMobileNode.getNodeId()+
//                        (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
//                        (isActive ? " Act " : " NoAct ") +
//                        "; PositionReal(X="
//                        + df.format(realRec.getX())+
//                        ";Y="
//                        + df.format(realRec.getY())+")");
//
//                xylineandshaperenderer.addAnnotation(xypointerannotation);
//
//                // shape for real
//                xylineandshaperenderer.setSeriesShape(serieIndex, serieShape);
//            }
//
//            //
//            // computed position
//            //
//            if (compRec!=null){
//                this.graphNodes.addSeries(serieNameComputed, new double[][] {{compRec.getX()},{compRec.getY()}});
//                int serieIndex = this.graphNodes.indexOf(serieNameComputed);
//                Shape serieShape = new JFreeChartShapeCross();
//
//                // serie paint
//                Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(serieIndex);
//                if (seriesPaint instanceof Color){
//                    serieColor = (Color) seriesPaint;
//                }
//
//                // prepare annotations
//                XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(
//                    curMobileNode.getNodeId() +
//                    (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
//                    (isActive ? " Act " : " NoAct ") +
//                    " (Est; "
//                    + df.format(compRec.getX()) +", "
//                    + df.format(compRec.getY()) +")",
//                    compRec.getX(), compRec.getY(), -0.78539816339744828D);
//
//                xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
//                xypointerannotation.setPaint(Color.blue);
//                xypointerannotation.setArrowPaint(Color.blue);
//                xypointerannotation.setToolTipText("Mobile node ID="+curMobileNode.getNodeId()+
//                        (curGenericNode.getTemperature() != Integer.MIN_VALUE ? "; t=" + curGenericNode.getTemperature()+"°C; " : "") +
//                        (isActive ? " Act " : " NoAct ") +
//                        "; PositionEst(X="
//                        + df.format(compRec.getX()) +
//                        ";Y="
//                        + df.format(compRec.getY()) +")");
//
//                xylineandshaperenderer.addAnnotation(xypointerannotation);
//                
//                // shape for computed
//                xylineandshaperenderer.setSeriesShape(serieIndex, serieShape);
//            }
//
//            // synchronize colors if exists both
//            if (compRec!=null && realRec!=null){
//                int realIndex = this.graphNodes.indexOf(serieNameReal);
//                int compIndex = this.graphNodes.indexOf(serieNameComputed);
//
//                xylineandshaperenderer.setSeriesPaint(compIndex,
//                        xylineandshaperenderer.getSeriesPaint(realIndex));
//
//                xylineandshaperenderer.setSeriesFillPaint(compIndex,
//                        xylineandshaperenderer.getSeriesFillPaint(realIndex));
//
//                Paint seriesPaint = xylineandshaperenderer.getSeriesPaint(realIndex);
//                if (seriesPaint instanceof Color){
//                    serieColor = (Color) seriesPaint;
//                }
//                
//                // draw error connector
//                if (this.isShowError()){
//                    Stroke seriesStroke = xylineandshaperenderer.getSeriesStroke(realIndex);
//                    if (seriesStroke==null) {
//                        seriesStroke = new BasicStroke();
//                    }
//                    
//                    XYLineAnnotation lineAnnotation = 
//                            new XYLineAnnotation(realRec.getX(), realRec.getY(), 
//                                compRec.getX(), compRec.getY(), 
//                                seriesStroke, 
//                                xylineandshaperenderer.getSeriesFillPaint(realIndex));
//                    // compute error
//                    Double xerror = Math.abs(compRec.getX() - realRec.getX());
//                    Double yerror = Math.abs(compRec.getX() - realRec.getX());
//                    Double derror = Math.sqrt(xerror*xerror + yerror*yerror);
//                    
//                    lineAnnotation.setToolTipText("DError="+df.format(derror)
//                            + "; XError=" + df.format(xerror)
//                            + "; YError=" + df.format(yerror));
//                    xylineandshaperenderer.addAnnotation(lineAnnotation);
//                }
//            }
//
//            // draw circles according to computed distances from static nodes
//            // if there is some computed distance
//            if (compRec!=null){
//                Map<Integer, Double> distancesFromAnchors = curMobileNode.getDistancesFromAnchors();
//                Set<Integer> staticAnchors = distancesFromAnchors.keySet();
//                Iterator<Integer> staticAnchorsIterator = staticAnchors.iterator();
//
//                while(staticAnchorsIterator.hasNext()){
//                    Integer curStaticAnchor = staticAnchorsIterator.next();
//                    Double distance = distancesFromAnchors.get(curStaticAnchor);
//                    GenericNode anchor = this.nodeRegister.getNode(curStaticAnchor);
//                    if (anchor==null || anchor.getPosition() == null || anchor.isAnchor()==false) continue;
//
//                    int r=serieColor.getRed(),g=serieColor.getGreen(),b=serieColor.getBlue();
//
//                    XYShapeAnnotation xyshapeannotation = new XYShapeAnnotation( 
//                        new Ellipse2D.Double(
//                            anchor.getPosition().getX() - distance,
//                            anchor.getPosition().getY() - distance,
//                            2*distance,
//                            2*distance),
//                        new BasicStroke(1.0f),
//                        new Color(r, g, b, 200),
//                        new Color(r, g, b, 30));
//
//                    // set tooltip
//                    xyshapeannotation.setToolTipText("Anchor="+anchor.getNodeId()+"; Mobile="+curGenericNode.getNodeId()+"; d=" + df.format(distance) + ";");
//
//                    xylineandshaperenderer.addAnnotation(xyshapeannotation);
//                }
//            }
        }
    }

    /**
     * Draws anchor nodes with text annotations
     */
    public synchronized void drawAnchors(){
        // dataset for anchor nodes
        // remove serie for anchors if exists
        if (this.graphNodes.indexOf("Anchors")!=-1){
            this.graphNodes.removeSeries("Anchors");
        }

        // decimal format to rounding
        DecimalFormat df = new DecimalFormat("00.00");

        // to store anchor position
        ArrayList<CoordinateRecord> anchorPositions=new ArrayIndexList<CoordinateRecord>();

        // annotations
        ArrayList<XYAnnotation> anchorAnnotations = new ArrayList<XYAnnotation>();

        // iterate over anchor nodes
        Set<Integer> nodes = this.nodeRegister.getNodesSet();
        Iterator<Integer> nodesIterator = nodes.iterator();
        for(int cnode=0; nodesIterator.hasNext(); cnode++){
            Integer curNode = nodesIterator.next();
            GenericNode objNodeGeneric = this.nodeRegister.getNode(curNode);
            if (!(objNodeGeneric instanceof SimpleGenericNode)) continue;
            final SimpleGenericNode objNode = (SimpleGenericNode) objNodeGeneric;

            // accept only anchors
            if (objNode.isAnchor() == false) continue;

            // position is not null
            if (objNode.getPosition()==null) continue;

            // skip node only if record exists in displayedNodes and is false
            if (this.displayedNodes!=null && this.displayedNodes.containsKey(curNode)){
                if (this.displayedNodes.get(curNode).booleanValue() == false) continue;
            }

            // else display anchor in graph
            // construct dataset for anchors position
            anchorPositions.add(new CoordinateRecord(objNode.getPosition().getX(), objNode.getPosition().getY()));

            // prepare annotations
            XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(
                    curNode+" ("
                    + df.format(objNode.getPosition().getX()) +", "
                    + df.format(objNode.getPosition().getY()) +")",
                    objNode.getPosition().getX(), objNode.getPosition().getY(), -0.78539816339744828D);

            xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
            xypointerannotation.setPaint(Color.blue);
            xypointerannotation.setArrowPaint(Color.blue);
            xypointerannotation.setToolTipText("Anchor node ID="+curNode+"; Position(X="
                    + df.format(objNode.getPosition().getX()) +";Y="
                    + df.format(objNode.getPosition().getY()) +")");
            anchorAnnotations.add(xypointerannotation);
        }

        // construct dataset from positions
        double[][] anchoPositionsValues = new double[2][anchorPositions.size()];
        Iterator<CoordinateRecord> positionsIterator = anchorPositions.iterator();
        for(int cpos=0; positionsIterator.hasNext(); cpos++){
            CoordinateRecord curPos = positionsIterator.next();
            anchoPositionsValues[0][cpos] = curPos.getX();
            anchoPositionsValues[1][cpos] = curPos.getY();
            this.addToPositions("Anchors", curPos);
        }
        this.graphNodes.addSeries("Anchors", anchoPositionsValues);
        int serieIndex = this.graphNodes.indexOf("Anchors");

        // draw annotations
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)this.jfreechart.getXYPlot().getRenderer();
        Iterator<XYAnnotation> annotationIterator = anchorAnnotations.iterator();
        while(annotationIterator.hasNext()){
            xylineandshaperenderer.addAnnotation(annotationIterator.next());
        }

        xylineandshaperenderer.setSeriesPaint(serieIndex, Color.blue);
        xylineandshaperenderer.setSeriesFillPaint(serieIndex, Color.blue);

        this.jfreechart.fireChartChanged();
        this.chp.repaint();
        this.chp.revalidate();
    }
    

    /**
     * implementation of NodeRegisterEventListener interface
     * Accept data change and reflect it to current view
     * @param evt
     */
    public void accept(NodeRegisterEvent evt) {
        if (evt.getEventType() != NodeRegisterEvent.EVENT_TYPE_DATA_CHANGED) return;
        
        // flush history ?
        if (evt.getChanges()==null){
            this.pruneHistory(null, maxHistorySize);
        }
        
        this.redraw();
    }

    
    
    public void XYPolygonAnnotationDemo1(String s)
    {
        this.jPanelNetworkMapBack = createDemoPanel();

        jPanelNetworkMapBack.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelNetworkMapBack.setName("jPanelNetworkMapBack"); // NOI18N

        javax.swing.GroupLayout jPanelNetworkMapBackLayout = new javax.swing.GroupLayout(jPanelNetworkMapBack);
        jPanelNetworkMapBack.setLayout(jPanelNetworkMapBackLayout);
        jPanelNetworkMapBackLayout.setHorizontalGroup(
            jPanelNetworkMapBackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 654, Short.MAX_VALUE)
        );
        jPanelNetworkMapBackLayout.setVerticalGroup(
            jPanelNetworkMapBackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 442, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelNetworkMapBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelNetworkMapBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        this.repaint();
        this.revalidate();
    }

    public XYDataset createDataset()
    {
        return this.graphNodes;
    }

    private static JFreeChart createChart(XYDataset xydataset)
    {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(null, "X", "Y", xydataset, PlotOrientation.VERTICAL, true, true, false);
        jfreechart.setBackgroundPaint(Color.white);
        XYPlot xyplot = (XYPlot)jfreechart.getPlot();
        xyplot.setBackgroundPaint(Color.lightGray);
        xyplot.setDomainGridlinePaint(Color.white);
        xyplot.setRangeGridlinePaint(Color.white);

//        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xyplot.getRenderer();
//        XYPolygonAnnotation xypolygonannotation = new XYPolygonAnnotation(new double[] {
//            2D, 5D, 2.5D, 8D, 3D, 5D, 2.5D, 2D
//        }, null, null, new Color(200, 200, 255, 100));
//        xypolygonannotation.setToolTipText("Target Zone");
//        xylineandshaperenderer.addAnnotation(xypolygonannotation, Layer.BACKGROUND);
//
//        //xylineandshaperenderer.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
//        XYPointerAnnotation xypointerannotation = new XYPointerAnnotation("Annotation 1 (2.0, 167.3)", 2D, 5D, -0.78539816339744828D);
//        xypointerannotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
//        xypointerannotation.setPaint(Color.red);
//        xypointerannotation.setArrowPaint(Color.red);
//        xylineandshaperenderer.addAnnotation(xypointerannotation);
//
//
//        XYPointerAnnotation xypointerannotation1 = new XYPointerAnnotation("Annotation 2 (15.0, 613.2)", 2D, 4D, 1.5707963267948966D);
//        xypointerannotation1.setTextAnchor(TextAnchor.TOP_CENTER);
//        xylineandshaperenderer.addAnnotation(xypointerannotation1);
//
//        XYShapeAnnotation xyshapeannotation = new XYShapeAnnotation( new Ellipse2D.Double(2D, 4D, 0.6, 0.6),
//            new BasicStroke(1.0f),
//            new Color(0F, 0F, 0F, 1F),
//            new Color(0F, 0F, 0F, 0.2F));
//        xylineandshaperenderer.addAnnotation(xyshapeannotation);


        return jfreechart;
    }

    public JPanel createDemoPanel()
    {
        jfreechart = createChart(createDataset());
        chp = new ChartPanel(jfreechart);
        chp.setMouseZoomable(true, true);
        chp.setMouseWheelEnabled(true);
        chp.getChart().getXYPlot().setDomainPannable(true);
        chp.getChart().getXYPlot().setRangePannable(true);
        chp.addChartMouseListener(this);
        chp.addMouseListener(this);
        chp.addMouseMotionListener(this);
        return chp;
    }

    /**
     * Action event handler
     * Primarily used to listen for pop-up settings dialog invocation
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if ("MapSettings".equals(actionCommand)){
            // correct sizing
            Dimension preferredSize = this.settingsDialog.getPreferredSize();
            Rectangle bounds = this.settingsDialog.getBounds();
            this.settingsDialog.setMinimumSize(preferredSize);
            this.settingsDialog.setPreferredSize(preferredSize);
            this.settingsDialog.setBounds(bounds.x, bounds.y, preferredSize.width, preferredSize.height);
            this.settingsDialog.setResizable(false);
            
            // show modal settings popup
            this.settingsDialog.fetchData();
            RSSI_graphApp.getApplication().show(this.settingsDialog);

            // what happened if settings are finished?
        }
        else if ("Redraw".equalsIgnoreCase(actionCommand)){
            // redraw if some timer is fired
            redraw();
        }
    }
    
    /**
     * Select given node from network map. Set appropriate selected noed indicators,
     * to be able to determine active node in other methods.
     * 
     * FutureWork: notify change listeners to new selected nodes to be able to 
     * provide further information in extra box.
     * 
     * Draws crosshair centered on selected node, log to textarea selected node.
     * 
     * @param id
     * @param click 
     */
    protected void selectNode(int id, ClickBestMatch click){
        this.setSelectedNode(id);
        
        // exists node?        
        if (id<0 || this.nodeRegister.existsNode(id)==false){
            this.setSelectedNode(-1);
            this.setSelected(null);
            this.chp.getChart().getXYPlot().setDomainCrosshairVisible(false);
            this.chp.getChart().getXYPlot().setRangeCrosshairVisible(false);
            return;
        }
        
        // get node
        GenericNode node = (click!=null && click.node!=null) ? click.node : this.nodeRegister.getNode(id); 
 
        // draw crossair
        this.setSelected(click);
        CoordinateRecord position=null;
        
        String logMessage="Selected noteId: "+id+";";
        
        if (click==null){
            boolean isReal = ((click!=null && node.getMobileExtension()!=null && node.getMobileExtension().getComputedPosition()!=null) ? click.realPos : true);
            position = isReal ? node.getPosition() : node.getMobileExtension().getComputedPosition();
            
            double derror = node.getPosition()!=null && node.getMobileExtension().getComputedPosition()!=null ?
                        LocalizationEstimate.getDError(node.getPosition(), node.getMobileExtension().getComputedPosition()) : 0.0;
            logMessage = logMessage + (isReal ? " RealPos=" : " EstPos=") + "[" + position.getX() + ";" + position.getY() + "]; DError=" + derror;
        } else {
            if (click.fromHistory){
                LocalizationEstimate history = this.getHistory(id, click.historyIndex);
                if (history==null){
                    this.selectNode(-1, null);
                    return;
                }
                
                position = click.realPos ? history.getRealPosition() : history.getEstimatedPosition();
                logMessage = logMessage + (click.realPos ? " RealPos=" : " EstPos=") + "[" + position.getX() + ";" + position.getY() + "]; DError=" + history.getDError();
                
            } else {
                boolean isReal = ((click!=null && node.getMobileExtension()!=null && node.getMobileExtension().getComputedPosition()!=null) ? click.realPos : true);
                position = isReal ? node.getPosition() : node.getMobileExtension().getComputedPosition();
                double derror = node.getPosition()!=null && node.getMobileExtension()!=null && node.getMobileExtension().getComputedPosition()!=null ?
                        LocalizationEstimate.getDError(node.getPosition(), node.getMobileExtension().getComputedPosition()) : 0.0;
                        
                
                logMessage = logMessage + (isReal ? " RealPos=" : " EstPos=") + "[" + position.getX() + ";" + position.getY() + "]; DError=" + derror;
            }
        }
        
        // both computed and real?
        
        
        if (logMessage!=null){
            this.worker.logToTextarea(logMessage, 65, "NetworkMap", JPannelLoggerLogElement.SEVERITY_INFO);
        }
        
        this.chp.getChart().getXYPlot().setDomainCrosshairValue(position.x, true);
        this.chp.getChart().getXYPlot().setRangeCrosshairValue(position.y, true);
                
        this.chp.getChart().getXYPlot().setDomainCrosshairVisible(true);
        this.chp.getChart().getXYPlot().setRangeCrosshairVisible(true);
    }

    public JPanel getjPanelNetworkMapBack() {
        return jPanelNetworkMapBack;
    }

    public void setjPanelNetworkMapBack(JPanel jPanelNetworkMapBack) {
        this.jPanelNetworkMapBack = jPanelNetworkMapBack;
    }

    public NodeRegister getNodeRegister() {
        return nodeRegister;
    }

    public void setNodeRegister(NodeRegister nodeRegister) {
        this.nodeRegister = nodeRegister;
    }

    public Timer getGrabTimer() {
        return grabTimer;
    }

    public void setGrabTimer(Timer grabTimer) {
        this.grabTimer = grabTimer;
    }

    public boolean isJpannelDrawed() {
        return jpannelDrawed;
    }

    public void setJpannelDrawed(boolean jpannelDrawed) {
        this.jpannelDrawed = jpannelDrawed;
    }

    public boolean isRedrawGraphNeeded() {
        return redrawGraphNeeded;
    }

    public void setRedrawGraphNeeded(boolean redrawGraphNeeded) {
        this.redrawGraphNeeded = redrawGraphNeeded;
    }

    public TimeSeriesCollection getTimeDataSets() {
        return timeDataSets;
    }

    public void setTimeDataSets(TimeSeriesCollection timeDataSets) {
        this.timeDataSets = timeDataSets;
    }

    public Map<String, TimeSeries> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(Map<String, TimeSeries> timeSeries) {
        this.timeSeries = timeSeries;
    }

    public WorkerLocalization getWorker() {
        return worker;
    }

    public void setWorker(WorkerLocalization worker) {
        this.worker = worker;
    }

    public Map<Integer, Boolean> getDisplayedNodes() {
        return displayedNodes;
    }

    public void setDisplayedNodes(Map<Integer, Boolean> displayedNodes) {
        this.displayedNodes = displayedNodes;
    }

    public MobileNodeManager getMnm() {
        return mnm;
    }

    public void setMnm(MobileNodeManager mnm) {
        this.mnm = mnm;
    }

    public boolean isShowError() {
        return showError;
    }

    public void setShowError(boolean showError) {
        this.showError = showError;
    }

    public boolean isKeep1to1axisRatio() {
        return keep1to1axisRatio;
    }

    public void setKeep1to1axisRatio(boolean keep1to1axisRatio) {
        this.keep1to1axisRatio = keep1to1axisRatio;
    }

    public boolean isShowMobileTextAnnot() {
        return showMobileTextAnnot;
    }

    public void setShowMobileTextAnnot(boolean showMobileTextAnnot) {
        this.showMobileTextAnnot = showMobileTextAnnot;
    }

    public boolean isShowStaticTextAnnot() {
        return showStaticTextAnnot;
    }

    public void setShowStaticTextAnnot(boolean showStaticTextAnnot) {
        this.showStaticTextAnnot = showStaticTextAnnot;
    }

    public boolean isShowDistances() {
        return showDistances;
    }

    public void setShowDistances(boolean showDistances) {
        this.showDistances = showDistances;
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public boolean isShowEstimateHistory() {
        return showEstimateHistory;
    }

    public void setShowEstimateHistory(boolean showEstimateHistory) {
        this.showEstimateHistory = showEstimateHistory;
    }

    public int getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(int selectedNode) {
        this.selectedNode = selectedNode;
    }

    public ClickBestMatch getSelected() {
        return selected;
    }

    public void setSelected(ClickBestMatch selected) {
        this.selected = selected;
    }

    public boolean isShowEstimateOnlyForSelected() {
        return showEstimateOnlyForSelected;
    }

    public void setShowEstimateOnlyForSelected(boolean showEstimateOnlyForSelected) {
        this.showEstimateOnlyForSelected = showEstimateOnlyForSelected;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelNetworkMapBack = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(631, 396));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(631, 396));

        jPanelNetworkMapBack.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelNetworkMapBack.setName("jPanelNetworkMapBack"); // NOI18N

        javax.swing.GroupLayout jPanelNetworkMapBackLayout = new javax.swing.GroupLayout(jPanelNetworkMapBack);
        jPanelNetworkMapBack.setLayout(jPanelNetworkMapBackLayout);
        jPanelNetworkMapBackLayout.setHorizontalGroup(
            jPanelNetworkMapBackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 615, Short.MAX_VALUE)
        );
        jPanelNetworkMapBackLayout.setVerticalGroup(
            jPanelNetworkMapBackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 463, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelNetworkMapBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelNetworkMapBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel jPanelNetworkMapBack;
    // End of variables declaration//GEN-END:variables

    public void mouseClicked(MouseEvent e) {
        
    }

    public void mousePressed(MouseEvent e) {
        
    }

    public void mouseReleased(MouseEvent e) {
        
    }

    public void mouseEntered(MouseEvent e) {
        
    }

    public void mouseExited(MouseEvent e) {
        
    }

    public void chartMouseMoved(ChartMouseEvent event) {
        
    }

    public void mouseDragged(MouseEvent e) {
        
    }

    public void mouseMoved(MouseEvent e) {
        
    }

    /**
    * Receives chart mouse click events from chart. Used to select particular
    * node.
    *
    * @param event; the event.
    */
    public void chartMouseClicked(ChartMouseEvent event) {
        int mouseX = event.getTrigger().getX();
        int mouseY = event.getTrigger().getY();
           
        Point2D p = this.chp.translateScreenToJava2D(
                new Point(mouseX, mouseY));
        XYPlot plot = (XYPlot) this.chp.getChart().getPlot();
        Rectangle2D plotArea = this.chp.getScreenDataArea();
        ValueAxis domainAxis = plot.getDomainAxis();
        RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
        ValueAxis rangeAxis = plot.getRangeAxis();
       
        RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
        this.repaint();
       
        double chartX = domainAxis.java2DToValue(p.getX(), plotArea,
                domainAxisEdge);
        double chartY = rangeAxis.java2DToValue(p.getY(), plotArea,
                rangeAxisEdge);
        
        ClickBestMatch clickedNode = this.getClickedNode(chartX, chartY, 1.0);
        if (clickedNode==null) {
            this.selectNode(-1, null);
            System.out.println("Chart mouse clicked: x = " + chartX + ", y = " + chartY + "; ");
        }
        else {
            // select this node....
            // this.selectNode()
            this.selectNode(clickedNode.nodeid, clickedNode);
            System.out.println("Chart mouse clicked: x = " + chartX + ", y = " + chartY + "; nodeid=" + clickedNode.nodeid + "; error=" + clickedNode.error);
        }
        
        this.redraw();
    }


}
