/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogCalibration.java
 *
 * Created on May 8, 2011, 10:07:37 PM
 */
package rssi_graph.rssi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import rssi_localization.RSSI_graphApp;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.nodeRegister.SimpleGenericNode;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 *
 * @author ph4r05
 */
public class JDialogCalibration extends javax.swing.JDialog {

    /**
     * parent panel where show button was pressed
     */
    protected javax.swing.JPanel parentPanel = null;
    
    /**
     * One-time flag determining wether node selector
     */
    private boolean isNodeSelectorRegistered=false;
    
    /** Creates new form JDialogCalibration */
    public JDialogCalibration(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // do not show broadcast node here, useless
        //this.jPanelNodeSelector1.setShowBroadcast(false);
        this.jPanelNodeSelector1.loadNodes();
        
        this.jTableDistFunctions.setModel(new FunctionTableModel());
        this.jTableRefDistance.setModel(new RefTableModel());
        
        this.jTableDistFunctions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.jTableRefDistance.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    public void setParentPanel(JPanel parentPanel) {
        this.parentPanel = parentPanel;
    }
    
    /**
     * refresh data from parent panel.
     * Fills table with appropriate data
     */
    public void refreshData(){
        // nothing to do if there is no parent panel
        // something went wrong probably since parent panel is set right after
        // initialization
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        // add node selector as change listener for node register for first time
        NodeRegister nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (nodeRegister!=null && this.isNodeSelectorRegistered==false){
            this.jPanelNodeSelector1.initThis();
            this.isNodeSelectorRegistered=true;
        }
        
        this.jPanelNodeSelector1.loadNodes();
        
        // fill table with fitted functions
        Map<String, ArrayList<RSSI2DistFunctionInterface>> fittedFunctions = myPanel.getFittedFunctions();
        if (fittedFunctions!=null && !fittedFunctions.isEmpty()){
            Iterator<String> iterator = fittedFunctions.keySet().iterator();
            
            Object[][] functionData = new Object[fittedFunctions.keySet().size()][4];
            for(int i=0; iterator.hasNext(); i++){
                String serieKey = iterator.next();
                
                // set default values if something went wrong
                functionData[i][0] = serieKey;
                functionData[i][1] = "-";
                functionData[i][2] = new Double(9999);
                functionData[i][3] = "-";
                
                // always use only one function here
                // @TODO: sometimes in future extend this
                ArrayList<RSSI2DistFunctionInterface> functionList = fittedFunctions.get(serieKey);
                if (functionList==null || functionList.isEmpty()) continue;
                RSSI2DistFunctionInterface curFunction = functionList.get(0);
                
                
                functionData[i][1] = curFunction.toStringHuman(3);
                functionData[i][2] = new Double(curFunction.getRms());
                functionData[i][3] = curFunction.getFormulaName();
            }
            FunctionTableModel functionTableModel = new FunctionTableModel();
            functionTableModel.setData(functionData);
            this.jTableDistFunctions.setModel(functionTableModel);
        }
        
        // fill in table with reference distances
        List<RSSI2DistInternalBuffer> dataBuffer = myPanel.getDataBuffer();
        if (dataBuffer!=null && !dataBuffer.isEmpty()){
            
            // allocate model data
            Object[][] refData = new Object[dataBuffer.size()][7];
            
            // iterate over all dataBuffer elements
            Iterator<RSSI2DistInternalBuffer> iterator = dataBuffer.iterator();
            for(int i=0; iterator.hasNext(); i++){
                
                // set default values if something went wrong
                // "Mid", "Static", "Mobile", "TX", "RSSI", "Annotation"
                refData[i][0] = new Integer(-1);
                refData[i][1] = new Integer(-1);
                refData[i][2] = new Integer(-1);
                refData[i][3] = new Integer(-1);
                refData[i][4] = new Double(0.0);
                refData[i][5] = new Double(9999);
                refData[i][6] = "-";
                
                RSSI2DistInternalBuffer curBuff = iterator.next();
                if (curBuff==null || curBuff.getStats()==null) continue;
                
                refData[i][0] = new Integer(curBuff.getMid());
                refData[i][1] = new Integer(curBuff.getReportingMote());
                refData[i][2] = new Integer(curBuff.getTalkingMote());
                refData[i][3] = new Integer(curBuff.getTxpower());
                refData[i][4] = new Double(curBuff.getDistance());
                refData[i][5] = new Double(curBuff.getStats().getMean());
                refData[i][6] = myPanel.getAnotationFor(curBuff.getMid());
            }
            
            RefTableModel tblModel = new RefTableModel();
            tblModel.setData(refData);
            
            this.jTableRefDistance.setModel(tblModel);
        }
    }
    
     /**
     * =========================================================================
     *
     * TABLE MODELS
     *
     * =========================================================================
     */

    class FunctionTableModel extends TableMyAbstractTableModel {
        public FunctionTableModel() {
            this.columnNames = new String[] {"Serie Key", "Function", "RMS", "Name"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==2) return Double.class;
            else return String.class;
        }
    }
    
    class RefTableModel extends TableMyAbstractTableModel {
        public RefTableModel() {
            this.columnNames = new String[] {"Mid", "Static", "Mobile", "TX", "Distance", "RSSI", "Annotation"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==0 || c==1 || c==2 || c==3) return Integer.class;
            else if (c==6) return String.class;
            else if (c==4 || c==5) return Double.class;
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

        jPanelNodeSelector1 = new rssi_localization.JPanelNodeSelector();
        jPanel1 = new javax.swing.JPanel();
        jButtonCopyFunction = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableDistFunctions = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jButtonCopyRefDist = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableRefDistance = new javax.swing.JTable();
        jButtonSetNormalized = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getResourceMap(JDialogCalibration.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanelNodeSelector1.setName("jPanelNodeSelector1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jButtonCopyFunction.setText(resourceMap.getString("jButtonCopyFunction.text")); // NOI18N
        jButtonCopyFunction.setName("jButtonCopyFunction"); // NOI18N
        jButtonCopyFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyFunctionActionPerformed(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTableDistFunctions.setAutoCreateRowSorter(true);
        jTableDistFunctions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableDistFunctions.setName("jTableDistFunctions"); // NOI18N
        jTableDistFunctions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(jTableDistFunctions);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 597, Short.MAX_VALUE)
                    .addComponent(jButtonCopyFunction, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonCopyFunction, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jButtonCopyRefDist.setText(resourceMap.getString("jButtonCopyRefDist.text")); // NOI18N
        jButtonCopyRefDist.setName("jButtonCopyRefDist"); // NOI18N
        jButtonCopyRefDist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyRefDistActionPerformed(evt);
            }
        });

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTableRefDistance.setAutoCreateRowSorter(true);
        jTableRefDistance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableRefDistance.setName("jTableRefDistance"); // NOI18N
        jTableRefDistance.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(jTableRefDistance);

        jButtonSetNormalized.setText(resourceMap.getString("jButtonSetNormalized.text")); // NOI18N
        jButtonSetNormalized.setToolTipText(resourceMap.getString("jButtonSetNormalized.toolTipText")); // NOI18N
        jButtonSetNormalized.setName("jButtonSetNormalized"); // NOI18N
        jButtonSetNormalized.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSetNormalizedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 595, Short.MAX_VALUE)
                        .addGap(14, 14, 14))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonSetNormalized)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCopyRefDist, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCopyRefDist)
                    .addComponent(jButtonSetNormalized))
                .addContainerGap())
        );

        jButtonCancel.setText(resourceMap.getString("jButtonCancel.text")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelNodeSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelNodeSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Copy selected function to node register distance function for selected nodes
     * in node selector.
     * 
     * @param evt 
     */
    private void jButtonCopyFunctionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyFunctionActionPerformed
        // is selected any row?
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        int selectedRow = this.jTableDistFunctions.getSelectedRow();
        if (selectedRow==-1){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No distance function selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
            
            return;
        }
            
        TableModel model = this.jTableDistFunctions.getModel();
        int modelRow = this.jTableDistFunctions.convertRowIndexToModel(selectedRow);
        
        // get serie key
        String serieKey = (String) model.getValueAt(modelRow, 0);
        Map<String, ArrayList<RSSI2DistFunctionInterface>> fittedFunctions = myPanel.getFittedFunctions();
        if (fittedFunctions==null || fittedFunctions.isEmpty() || !fittedFunctions.containsKey(serieKey)){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "Internal error detected, no such fitted function found",
                        "No data",
                JOptionPane.ERROR_MESSAGE);
            }
                
            return;
        }
        
        ArrayList<RSSI2DistFunctionInterface> fList = fittedFunctions.get(serieKey);
        if (fList==null || fList.isEmpty()){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "Internal error detected, no such fitted function found",
                        "No data",
                JOptionPane.ERROR_MESSAGE);
            }
                
            return;
        }
        RSSI2DistFunctionInterface curFunction = fList.get(0);
        
        int[] selectedNodes = this.jPanelNodeSelector1.getSelectedNodes();
        if (selectedNodes==null || selectedNodes.length==0){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No node selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
                
            return;
        }
        
        NodeRegister nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (nodeRegister==null) return;
        
        // iterate over selected nodes
        for(int i=0, cnI=selectedNodes.length; i<cnI; i++){
            if (selectedNodes[i]==65535) continue;
            
            GenericNode node = nodeRegister.getNode(selectedNodes[i]);
            RSSI2DistLogNormalShadowing rSSI2DistLogNormalShadowing = new RSSI2DistLogNormalShadowing();
            
            rSSI2DistLogNormalShadowing.setParameters(curFunction.getParameters());
            node.setDistanceFunction(rSSI2DistLogNormalShadowing);
        }
        
        nodeRegister.changeNotify(null);
        
        JOptionPane.showMessageDialog(null,
                    "Operation done.",
                    "Operation successfull",
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonCopyFunctionActionPerformed

    /**
     * Copy selected RSSI value as RSSI(d0) for selected nodes
     * @param evt 
     */
    private void jButtonCopyRefDistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyRefDistActionPerformed
        // is selected any row?
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        int selectedRow = this.jTableRefDistance.getSelectedRow();
        if (selectedRow==-1){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No reference distance data selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
            
            return;
        }
            
        TableModel model = this.jTableRefDistance.getModel();
        int modelRow = this.jTableRefDistance.convertRowIndexToModel(selectedRow);
        
        // get RSSI value
        Double rssiData = (Double) model.getValueAt(modelRow, 5);
        if (rssiData==null) return;
        
        int[] selectedNodes = this.jPanelNodeSelector1.getSelectedNodes();
        if (selectedNodes==null || selectedNodes.length==0){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No node selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
                
            return;
        }
        
        NodeRegister nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (nodeRegister==null) return;
        
        // iterate over selected nodes
        for(int i=0, cnI=selectedNodes.length; i<cnI; i++){
            if (selectedNodes[i]==65535) continue;
            
            GenericNode node = nodeRegister.getNode(selectedNodes[i]);
            RSSI2DistFunctionInterface distanceFunction = node.getDistanceFunction();
            if (distanceFunction==null){
                distanceFunction=new RSSI2DistLogNormalShadowing();
                node.setDistanceFunction(distanceFunction);
            }
            
            distanceFunction.setParameter("pl", rssiData);
            node.setDistanceFunction(distanceFunction);
        }
        
        nodeRegister.changeNotify(null);
        
        JOptionPane.showMessageDialog(null,
                    "Operation done.",
                    "Operation successfull",
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonCopyRefDistActionPerformed

    /**
     * Set normalized rssi constant for tx node (mobile node).
     * @param evt 
     */
    private void jButtonSetNormalizedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSetNormalizedActionPerformed
        // TODO add your handling code here:
        // is selected any row?
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        int selectedRow = this.jTableRefDistance.getSelectedRow();
        if (selectedRow==-1){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No reference distance data selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
            
            return;
        }
            
        TableModel model = this.jTableRefDistance.getModel();
        int modelRow = this.jTableRefDistance.convertRowIndexToModel(selectedRow);
        
        // get RSSI value
        //"Mid", "Static", "Mobile", "TX", "Distance", "RSSI", "Annotation"
        Double rssiData = (Double) model.getValueAt(modelRow, 5);
        Integer staticNode = (Integer) model.getValueAt(modelRow, 1);
        if (rssiData==null) return;    
        
        int[] selectedNodes = this.jPanelNodeSelector1.getSelectedNodes();
        if (selectedNodes==null || selectedNodes.length==0){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No node selected",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
                
            return;
        }
        
        NodeRegister nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (nodeRegister==null) return;
        
        // static node does not exists
        if (nodeRegister.existsNode(staticNode)==false){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "Cannot set normalization since there is no such static node in register",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
                
            return;
        }
        
        GenericNode staticNodeObj = nodeRegister.getNode(staticNode);
        RSSI2DistFunctionInterface distanceFunction = staticNodeObj.getDistanceFunction();
        if (distanceFunction==null){
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "Cannot set normalization since static reference node has no distance function",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }
                
            return;
        }
        
        double rssiDifference = distanceFunction.getParameter("pl") - rssiData; 
        
        // iterate over selected nodes
        for(int i=0, cnI=selectedNodes.length; i<cnI; i++){
            if (selectedNodes[i]==65535) continue;
            
            GenericNode node = nodeRegister.getNode(selectedNodes[i]);
            if (node instanceof SimpleGenericNode){
                final SimpleGenericNode gNode = (SimpleGenericNode) node;
                gNode.setRssiNormalizingConstant(rssiDifference);
            }
        }
        
        nodeRegister.changeNotify(null);
        
        JOptionPane.showMessageDialog(null,
                    "Operation done.",
                    "Operation successfull",
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonSetNormalizedActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JDialogCalibration dialog = new JDialogCalibration(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonCopyFunction;
    private javax.swing.JButton jButtonCopyRefDist;
    private javax.swing.JButton jButtonSetNormalized;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private rssi_localization.JPanelNodeSelector jPanelNodeSelector1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTableDistFunctions;
    private javax.swing.JTable jTableRefDistance;
    // End of variables declaration//GEN-END:variables
}
