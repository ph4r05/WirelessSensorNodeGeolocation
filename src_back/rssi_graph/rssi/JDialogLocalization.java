/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogLocalization.java
 *
 * Created on May 8, 2011, 5:50:59 PM
 */
package rssi_graph.rssi;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import rssi_localization.RSSI_graphApp;
import rssi_localization.WorkerBase;
import rssi_localization.localization.WorkerLocalization;
import rssi_localization.localization.CoordinateRecord;
import rssi_graph.utils.BucketHasher;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 * Offline localization dialog.
 * Here user specifies parameters necessary for offline localization such as
 * position of mobile node.
 * 
 * @author ph4r05
 */
public class JDialogLocalization extends javax.swing.JDialog {

    /**
     * parent panel where show button was pressed
     */
    protected javax.swing.JPanel parentPanel = null;
    
    /**
     * Stored localization data
     */
    protected OfflineLocalizationSettings locData = null;
    
    public static final String patternPosition=
            "([+\\-]?[0-9]+)(?:\\s*[\\.,]\\s*([0-9]+))?\\s*[;]\\s*([+\\-]?[0-9]+)(?:\\s*[\\.,]\\s*([0-9]+))?";
    
    public static final String bracedPatternPosition=
            "\\[\\s*"+patternPosition+"\\s*\\]";
    
    public static final String fixedMobilePatternPosition=
            "mobilePos\\s*=\\s*" + bracedPatternPosition;
    
    public static final String specifiedMobilePatternPosition=
            "p[0-9]+\\s*=\\s*" + bracedPatternPosition;
    
    protected List<Pattern> annotPosHeuristic=null;
    
    /** Creates new form JDialogLocalization */
    public JDialogLocalization(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    public void setParentPanel(JPanel parentPanel) {
        this.parentPanel = parentPanel;
    }
    
    /**
     * Clears currently set data to default state.
     * May happen when user clicks on Reset button or new data is loaded.
     */
    public void resetData(){
        this.locData = new OfflineLocalizationSettings();
    }
    
    /**
     * Try to determine mobile node real position guess from annotation text
     * 
     * @param annotation
     * @return 
     */
    public CoordinateRecord determineCoordinatesFromAnnotation(String annotation){
        if (this.annotPosHeuristic==null){
            this.annotPosHeuristic = new ArrayList<Pattern>();
            this.annotPosHeuristic.add(Pattern.compile(".*?" + JDialogLocalization.specifiedMobilePatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + JDialogLocalization.fixedMobilePatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + JDialogLocalization.bracedPatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + JDialogLocalization.patternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
        }
        
        CoordinateRecord result = null;
        
        // build matchers, iterate
        Iterator<Pattern> iterator = this.annotPosHeuristic.iterator();
        while(iterator.hasNext()){
            Pattern curPattern = iterator.next();
            Matcher m = curPattern.matcher(annotation);
            
            boolean b = m.matches();
            if(b==false) continue;
            
            // matched, extract data
            String group = m.group();
            if (group==null || group.isEmpty()) continue;
            
            String xvalue = null;
            String yvalue = null;
            
            if (m.group(2)==null){
                xvalue = m.group(1);
            } else {
                xvalue = m.group(1) + "." + m.group(2);
            }
            
            if (m.group(4)==null){
                yvalue = m.group(3);
            } else {
                yvalue = m.group(3) + "." + m.group(4);
            }
            
            // convert string representation to double
            Double xvalueDouble = Double.parseDouble(xvalue);
            Double yvalueDouble = Double.parseDouble(yvalue);
            
            result=new CoordinateRecord();
            result.setX(xvalueDouble.doubleValue());
            result.setY(yvalueDouble.doubleValue());
        }
            
        return result;
    }
    
    /**
     * Refresh application view from stored model
     * (especially positions)
     */
    public void refresView(){
        // build new table model
        PositionTableModel model = new PositionTableModel();
        Object[][] data = null;
        
        DecimalFormat df = new DecimalFormat("00.0");
        
        // iterate over saved MID data (if some)
        if (this.locData!=null 
                && this.locData.getRssiData()!=null 
                && !this.locData.getRssiData().isEmpty()
                && this.parentPanel instanceof jPannelRSSI2DistanceDataLoader){
            
            final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
            
            // real data count
            int datacount = 0;
            
            // load just data counts
            Iterator<String> iterator = this.locData.getRssiData().keySet().iterator();
            for(int i=0; iterator.hasNext(); i++){
                String curKey = iterator.next();
                ArrayList<RSSI2DistInternalBuffer> bufferList = this.locData.getRssiData().get(curKey);
                
                if (bufferList==null || bufferList.isEmpty()){
                    --i;
                    continue;
                }
                
                // nonempty buffer list here
                datacount=i+1;
            }
            
            data = new Object[datacount][3];
            
            // my positions
            Map<Integer, CoordinateRecord> realPositions = this.locData.getRealPositions();
            
            // fill model data
            iterator = this.locData.getRssiData().keySet().iterator();
            for(int i=0; iterator.hasNext(); i++){
                String curKey = iterator.next();
                ArrayList<RSSI2DistInternalBuffer> bufferList = this.locData.getRssiData().get(curKey);
                
                if (bufferList==null || bufferList.isEmpty()){
                    --i;
                    continue;
                }
                
                int curMid = bufferList.get(0).getMid(); 
                String curAnnotation = myPanel.getAnotationFor(curMid); 
                
                // determine position for this MID
                CoordinateRecord realPos = null;
                
                if (realPositions!=null && realPositions.containsKey(Integer.valueOf(curMid))){
                    realPos = realPositions.get(curMid);
                } else {
                    // nothing, try to determine from annotation
                    // "-" means null
                    realPos = this.determineCoordinatesFromAnnotation(curAnnotation);
                }
                
                // position
                data[i][0] = realPos==null ? "-" : df.format(realPos.getX()) + ";" + df.format(realPos.getY());
                
                // mid
                data[i][1] = Integer.valueOf(curMid);
                        
                // annotation
                data[i][2] = curAnnotation;
            }
        }
        model.setData(data);
        
        this.jTable1.setModel(model);
        this.jTable1.repaint();
        this.jTable1.revalidate();
    }
    
    /**
     * Fetch set data to options to store it.
     */
    public void fetchData(){
        TableModel model = this.jTable1.getModel();
        if (!(model instanceof PositionTableModel)) return;
        
        final PositionTableModel myModel = (PositionTableModel) model;
        if (this.locData==null || this.locData.getRssiData()==null) return;
        
        if (this.locData.getRealPositions()==null){
            this.locData.setRealPositions(new HashMap<Integer, CoordinateRecord>());
        }
        
        Map<Integer, CoordinateRecord> realPositions = this.locData.getRealPositions();
        
        // now iterate over model and parse coordinates
        int rowCount = myModel.getRowCount();
        for(int i=0; i<rowCount; i++){
            String curPos = (String) myModel.getValueAt(i, 0);
            Integer curMid = (Integer) myModel.getValueAt(i, 1);
            
            CoordinateRecord determinedCoord = this.determineCoordinatesFromAnnotation(curPos);
            realPositions.put(curMid, determinedCoord);
        }
    }
    
    /**
     * reload data on change from dataLoader (parent panel)
     */
    public void reloadData(){
        this.resetData();
        
        // nothing to do if there is no parent panel
        // something went wrong probably since parent panel is set right after
        // initialization
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        // load data
        List<RSSI2DistInternalBuffer> dataBuffer = myPanel.getDataBuffer();
        
        // if there is no data to load, display message box if visible, then return
        if (dataBuffer==null || dataBuffer.isEmpty()){
            // display message box if is showing
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No data to load found!",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
            
            return;
        }
        
        // now we know that dataBuffer is initialized and has some elements
        // store useful info to options structure
        this.locData = new OfflineLocalizationSettings();
        //this.locData.setRssiData(dataBuffer);
        this.locData.rssiSource = this.jRadioMean.isSelected() ? OfflineLocalizationSettings.RSSI_SOURCE_MEAN : OfflineLocalizationSettings.RSSI_SOURCE_MEDIAN;
        this.locData.realPositions = new HashMap<Integer, CoordinateRecord>();
                
        Iterator<RSSI2DistInternalBuffer> iterator = dataBuffer.iterator();
        for(int i=0; iterator.hasNext(); i++){
            // current buffer data
            RSSI2DistInternalBuffer curBuffer = iterator.next();
            if (curBuffer==null){
                --i;
                continue;
            }
            
            // unitialized ?
            if (i==0){
                this.locData.setMobileNodeId(curBuffer.getTalkingMote());
            }
        }
        
        // make buckets
        Set<String> group = new HashSet<String>();
        group.add("mid");
        
        DefaultRSSI2DistInternalBufferHasher hasher = new DefaultRSSI2DistInternalBufferHasher();
        hasher.setXgroup(group);
        
        BucketHasher<RSSI2DistInternalBuffer> bhasher = new BucketHasher<RSSI2DistInternalBuffer>(hasher);
        this.locData.setRssiData(bhasher.makeBuckets(dataBuffer));
        
        // save to my data structures
        // build view from model
        this.refresView();
    }
    
    /**
     * =========================================================================
     *
     * TABLE MODELS
     *
     * =========================================================================
     */

    class PositionTableModel extends TableMyAbstractTableModel {
        public PositionTableModel() {
            this.columnNames = new String[] {"Position [x;y]", "Mid", "Annotation"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col==0;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==0) return String.class;
            else if (c==1) return Integer.class;
            else return String.class;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jRadioMedian = new javax.swing.JRadioButton();
        jRadioMean = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jComboTxPower = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jFormatedStaticCompensation = new javax.swing.JFormattedTextField(NumberFormat.getIntegerInstance());
        jButtonCancel = new javax.swing.JButton();
        jButtonLocalize = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jButtonReset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getResourceMap(JDialogLocalization.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Position", "Mid", "Annotation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTable1.columnModel.title0")); // NOI18N
        jTable1.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTable1.columnModel.title1")); // NOI18N
        jTable1.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTable1.columnModel.title2")); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        buttonGroup1.add(jRadioMedian);
        jRadioMedian.setText(resourceMap.getString("jRadioMedian.text")); // NOI18N
        jRadioMedian.setToolTipText(resourceMap.getString("jRadioMedian.toolTipText")); // NOI18N
        jRadioMedian.setName("jRadioMedian"); // NOI18N

        buttonGroup1.add(jRadioMean);
        jRadioMean.setSelected(true);
        jRadioMean.setText(resourceMap.getString("jRadioMean.text")); // NOI18N
        jRadioMean.setToolTipText(resourceMap.getString("jRadioMean.toolTipText")); // NOI18N
        jRadioMean.setName("jRadioMean"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioMedian)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadioMean)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jRadioMean)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioMedian)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jComboTxPower.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboTxPower.setToolTipText(resourceMap.getString("jComboTxPower.toolTipText")); // NOI18N
        jComboTxPower.setEnabled(false);
        jComboTxPower.setName("jComboTxPower"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jFormatedStaticCompensation.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormatedStaticCompensation.setText(resourceMap.getString("jFormatedStaticCompensation.text")); // NOI18N
        jFormatedStaticCompensation.setToolTipText(resourceMap.getString("jFormatedStaticCompensation.toolTipText")); // NOI18N
        jFormatedStaticCompensation.setEnabled(false);
        jFormatedStaticCompensation.setName("jFormatedStaticCompensation"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboTxPower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFormatedStaticCompensation, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboTxPower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jFormatedStaticCompensation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jButtonCancel.setText(resourceMap.getString("jButtonCancel.text")); // NOI18N
        jButtonCancel.setToolTipText(resourceMap.getString("jButtonCancel.toolTipText")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonLocalize.setText(resourceMap.getString("jButtonLocalize.text")); // NOI18N
        jButtonLocalize.setToolTipText(resourceMap.getString("jButtonLocalize.toolTipText")); // NOI18N
        jButtonLocalize.setName("jButtonLocalize"); // NOI18N
        jButtonLocalize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLocalizeActionPerformed(evt);
            }
        });

        jButtonApply.setText(resourceMap.getString("jButtonApply.text")); // NOI18N
        jButtonApply.setToolTipText(resourceMap.getString("jButtonApply.toolTipText")); // NOI18N
        jButtonApply.setName("jButtonApply"); // NOI18N
        jButtonApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonApplyActionPerformed(evt);
            }
        });

        jButtonReset.setText(resourceMap.getString("jButtonReset.text")); // NOI18N
        jButtonReset.setToolTipText(resourceMap.getString("jButtonReset.toolTipText")); // NOI18N
        jButtonReset.setName("jButtonReset"); // NOI18N
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonLocalize)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonCancel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonApply, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonReset)))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButtonApply, jButtonCancel, jButtonLocalize, jButtonReset});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonLocalize)
                            .addComponent(jButtonCancel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonApply)
                            .addComponent(jButtonReset))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Cancel button, dispose window
     * @param evt 
     */
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Localize button, save data, send for localization
     * @param evt 
     */
    private void jButtonLocalizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLocalizeActionPerformed
        this.fetchData();
        WorkerBase[] workers = RSSI_graphApp.getApplication().getWorkers();
        
        boolean error=false;
        
        if (workers==null){
            error=true;
            if (this.isShowing()){
                // display info
                JOptionPane.showMessageDialog(this,
                    "Cannot find localization worker or is shutted off.",
                    "Data error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        WorkerLocalization locWorker = null;
	
	// get selected nodes
	int[] selection = this.jTable1.getSelectedRows();
	if (selection==null || selection.length==0){
	    locData.setMidToLocalize(null);
	} else {
	    Set<Integer> tmpSet = new HashSet<Integer>();
	    for (int i = 0; i < selection.length; i++) {
		selection[i] = this.jTable1.convertRowIndexToModel(selection[i]);
		tmpSet.add(Integer.valueOf((Integer) this.jTable1.getModel().getValueAt(selection[i], 1)));
	    }
	    
	    locData.setMidToLocalize(tmpSet);
	}
	// selection is now in terms of the underlying TableModel
        
        // iterate over workers to find localization
        for(int i=0, cnI=workers.length; i<cnI; i++){
            if (workers[i] instanceof WorkerLocalization){
                locWorker = (WorkerLocalization) workers[i];
                break;
            }
        }
        
        if (locWorker==null){
            return;
        }
        
        try {
            locWorker.localize(locData);
        } catch (FunctionEvaluationException ex) {
            Logger.getLogger(JDialogLocalization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OptimizationException ex) {
            Logger.getLogger(JDialogLocalization.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JOptionPane.showMessageDialog(this,
                    "Localization process finished, please check Localization tab.",
                    "Localization ended",
                    JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
    }//GEN-LAST:event_jButtonLocalizeActionPerformed

    /**
     * Apply button pressed, just save filled data
     * @param evt 
     */
    private void jButtonApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonApplyActionPerformed
        this.fetchData();
    }//GEN-LAST:event_jButtonApplyActionPerformed

    /**
     * Reset data
     * @param evt 
     */
    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        this.reloadData();
    }//GEN-LAST:event_jButtonResetActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JDialogLocalization dialog = new JDialogLocalization(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonLocalize;
    private javax.swing.JButton jButtonReset;
    private javax.swing.JComboBox jComboTxPower;
    private javax.swing.JFormattedTextField jFormatedStaticCompensation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioMean;
    private javax.swing.JRadioButton jRadioMedian;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
