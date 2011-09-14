/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelLocalizationSettings.java
 *
 * Created on Apr 3, 2011, 8:48:36 PM
 */

package rssi_graph.localization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.RSSI_graphApp;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.nodeRegister.NodeRegisterEventListener;
import rssi_graph.nodeRegister.SimpleGenericNode;
import rssi_graph.rssi.RSSI2DistFunctionInterface;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 *
 * @author ph4r05
 */
public class JPanelLocalizationSettings extends javax.swing.JPanel implements NodeRegisterEventListener {

    /**
     * Node register
     */
    protected NodeRegister nodeRegister = null;

    /**
     * Current node id
     */
    private int currentNodeId=1;

    /**
     * parameters for distance functions are double buffered
     * store here and when apply is clicked, ...
     */

    /** Creates new form JPanelLocalizationSettings */
    public JPanelLocalizationSettings() {
        initComponents();

        SelectionListener listener = new SelectionListener(this.jTabPositions);
        this.jTabPositions.getSelectionModel().addListSelectionListener(listener);
        this.jTabPositions.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * Initialization routine
     */
    public void initThis(){
        this.nodeRegister = RSSI_graphApp.sGetNodeRegister();
        this.loadPositionsData();

        // register as node register change listener
        this.nodeRegister.addChangeListener(this);
    }

    /**
     * Node register data changed (data model)
     * @param evt
     */
    public void accept(NodeRegisterEvent evt) {
        Map<Integer, String> changes = evt.getChanges();
        if (changes==null){
            this.loadPositionsData();
            return;
        }

        // do not refresh by default
        boolean doRefresh=false;

        // scan changes and detect what is changed
        Iterator<Integer> nodeIt = changes.keySet().iterator();
        while(nodeIt.hasNext()){
            Integer curNodeId = nodeIt.next();
            String change = changes.get(curNodeId);
            if (change==null || "position".equalsIgnoreCase(change)){
                doRefresh=true;
                break;
            }
        }

        if (doRefresh){
            this.loadPositionsData();
        }
    }

    /**
     * Selection listener for node selection - change current node for parameter settings
     * when node selection is changed.
     */
    private class SelectionListener implements ListSelectionListener {
        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
            if (e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed()
                  && !e.getValueIsAdjusting()) {
                // Column selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();

                // do the thing now
                // select given node and set its function parameters
                //System.err.println("Selected node on row: " + first + "; last=" + last);

                int selCount = this.table.getSelectedRowCount();
                //System.err.println("SelectedRows: " + selCount);
                
                int sel[] = this.table.getSelectedRows();
                if (sel==null || sel.length==0){
                    return;
                }

                // can set only one node at time
                int modelI = this.table.convertRowIndexToModel(sel[0]);
                int curNodeId = (Integer)this.table.getModel().getValueAt(modelI, 0);
                changeCurrentNode(curNodeId);

            } else if (e.getSource() == table.getColumnModel().getSelectionModel()
                   && table.getColumnSelectionAllowed() ){
                // Row selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                return;
            }

            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
                return;
            }
        }
    }

    /**
     * Change current node settings
     * @param newNode
     */
    public void changeCurrentNode(int newNode){
        jLabelCurrentNode.setText(""+newNode);
        currentNodeId = newNode;

        loadModelSettingsToTable(newNode);
        return;
//
//        // distance function for each mote should be stored in this register
//        // load current function settings from node register
//        GenericNode curNode = nodeRegister.getNode(newNode);
//        if (curNode==null) return;
//
//        // get used distance function
//        RSSI2DistFunctionInterface distanceFunction = curNode.getDistanceFunction();
//
//        // load settings to table
//        loadModelSettingsToTable(curNode.getNodeId());
    }

    /**
     * returns currently selected node
     * 
     * @return
     */
    public int getCurrentNodeId() {
        return currentNodeId;
    }

    /**
     * loads positions data from mote database to table
     */
    public void loadPositionsData(){
        if (this.nodeRegister==null) return;
        Set<Integer> nodesSet = this.nodeRegister.getNodesSet();
        Iterator<Integer> nodeIt = nodesSet.iterator();
        Object[][] modelData = new Object[nodesSet.size()][7];

        // table data model for position table
        PositionsTableModel pmod = new PositionsTableModel();

        // random generator
        Random generator = new Random();

        for(int i=0; nodeIt.hasNext(); i++){
            GenericNode curNode = this.nodeRegister.getNode(nodeIt.next());
            if (curNode==null) throw new NullPointerException("Registered node is null");
            CoordinateRecord curPos = curNode.getPosition();
            
            double txCorr = 0.0;
            if (curNode instanceof SimpleGenericNode){
                final SimpleGenericNode sgNode = (SimpleGenericNode) curNode;
                txCorr = sgNode.getRssiNormalizingConstant();
            }

            modelData[i][0] = new Integer(curNode.getNodeId());
            modelData[i][1] = new Boolean(curNode.isAnchor());
            modelData[i][2] = curPos == null ? new Double(generator.nextDouble()*2.0) : curPos.getX();
            modelData[i][3] = curPos == null ? new Double(generator.nextDouble()*2.0) : curPos.getY();
            modelData[i][4] = new Double(0.0);
            modelData[i][5] = new Double(txCorr);
            modelData[i][6] = new String(curNode.getDistanceFunction().getFormulaName());
        }

        pmod.setData(modelData);
        jTabPositions.setModel(pmod);
    }

    /**
     * Table model for nodes positions
     */
     class PositionsTableModel extends TableMyAbstractTableModel {
        public PositionsTableModel() {
            this.columnNames = new String[] {"NodeID", "Anchor", "X", "Y", "Z", "TXNorm", "Function"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col==1 || col==2 || col==3 || col==4 || col==5;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==0) return Integer.class;
            if (c==1) return Boolean.class;
            if (c==2||c==3||c==4||c==5) return Double.class;
            return String.class;
        }
    }

    /**
     * table model for function parameters.
     * Optimal solution: with tree and jpanel switching. Each function model
     * should has own settings panel with corresponding interface to nodeRegister
     * tied to specified localization model.
     */
    class FunctionTableModel extends TableMyAbstractTableModel {
        public FunctionTableModel() {
            this.columnNames = new String[] {"Parameter", "Value"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col==1;
        }
    }

    /**
     * Loads settings for current node to table
     * @param nodeid
     */
    public void loadModelSettingsToTable(int nodeid){
        GenericNode curNode = this.nodeRegister.getNode(nodeid);
        if (curNode==null) return;

        // load parameters from register
        Map<String, Double> parameters = curNode.getDistanceFunction().getParameters();

        // update table model
        FunctionTableModel fmod = new FunctionTableModel();
        Object[][] modelData = new Object[parameters.size()][2];

        Set<String> paramKeysSet = parameters.keySet();
        Iterator<String> paramKeysSetIterator = paramKeysSet.iterator();
        for(int i=0; paramKeysSetIterator.hasNext(); i++){
            String paramKey = paramKeysSetIterator.next();
            modelData[i][0] = paramKey;
            modelData[i][1] = parameters.get(paramKey);
        }

        fmod.setData(modelData);
        jTableModelParameters.setModel(fmod);
        jTableModelParameters.repaint();
        jTableModelParameters.revalidate();
    }

    /**
     * Lookup for model parameters of rssi -> distance mapping function set by user
     * in table.
     * @return
     */
    public Map<String, Double> getModelParameters(){
        Map<String, Double> resultMap = new HashMap<String, Double>();
        int rowCount = this.jTableModelParameters.getRowCount();

        for(int i=0; i<rowCount; i++){
            String paramName = (String) this.jTableModelParameters.getModel().getValueAt(i, 0);
            Double paramValue = (Double) this.jTableModelParameters.getModel().getValueAt(i, 1);
            resultMap.put(paramName, paramValue);
        }
        
        return resultMap;
    }

    /**
     * Get node id position from settings table
     * @param nodeid
     * @return
     */
    public CoordinateRecord getCoordsFor(int nodeid){
        Integer iNodeId = new Integer(nodeid);
        CoordinateRecord coordPoint = null;

        int rowCount = this.jTabPositions.getRowCount();
        for(int i=0; i<rowCount; i++){
            Integer curNodeid = (Integer) this.jTabPositions.getModel().getValueAt(i, 0);
            if (iNodeId.equals(curNodeid)){
                Double x=null, y=null, z=null;

                // this could throw null pointer exception if there are no 
                // data filled in table cells for positions
                try {
                    x = (Double) this.jTabPositions.getModel().getValueAt(i, 2);
                    y = (Double) this.jTabPositions.getModel().getValueAt(i, 3);
                    z = (Double) this.jTabPositions.getModel().getValueAt(i, 4);

                    coordPoint = new CoordinateRecord(x, y, z);
                } catch(NullPointerException e){
                    ;
                }
            }
        }

        return coordPoint;
    }

    /**
     * Returns anchor nodes
     * @return
     */
    public Set<Integer> getAnchorNodes(){
        Set<Integer> anchors = new HashSet<Integer>();
        int rowCount = this.jTabPositions.getRowCount();
        for(int i=0; i<rowCount; i++){
            Integer curNodeid = (Integer) this.jTabPositions.getModel().getValueAt(i, 0);
            Boolean isAnchor = (Boolean) this.jTabPositions.getModel().getValueAt(i, 1);
            if (isAnchor){
                anchors.add(curNodeid);
            }
        }

        return anchors;
    }

    public double lookupSmoothingAlpha(){
        return Double.parseDouble(this.jTextFieldSmoothingAlpha.getText());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabPositions = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableModelParameters = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabelCurrentNode = new javax.swing.JLabel();
        jButton_copyDistToALl = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldSmoothingAlpha = new javax.swing.JTextField();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JPanelLocalizationSettings.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTabPositions.setAutoCreateRowSorter(true);
        jTabPositions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Integer(20), new Boolean(true), new Double(0.0), new Double(0.0), new Double(1.0), "LogNormal"},
                {new Integer(21), new Boolean(true), new Double(3.0), new Double(0.0), new Double(1.0), "LogNormal"},
                {new Integer(22), new Boolean(true), new Double(0.4), new Double(2.5), new Double(1.0), "LogNormal"},
                {new Integer(250), new Boolean(false), null, null, new Double(1.0), "LogNormal"},
                {new Integer(1), new Boolean(true), new Double(1.0), new Double(1.0), new Double(1.0), "LogNormal"}
            },
            new String [] {
                "NodeID", "Anchor", "X", "Y", "Z", "Function"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Boolean.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTabPositions.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTabPositions.setName("jTabPositions"); // NOI18N
        jScrollPane1.setViewportView(jTabPositions);
        jTabPositions.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title0")); // NOI18N
        jTabPositions.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title4")); // NOI18N
        jTabPositions.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title1")); // NOI18N
        jTabPositions.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title2")); // NOI18N
        jTabPositions.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title3")); // NOI18N
        jTabPositions.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("jTabPositions.columnModel.title5")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTableModelParameters.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTableModelParameters.setName("jTableModelParameters"); // NOI18N
        jScrollPane3.setViewportView(jTableModelParameters);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelCurrentNode.setFont(resourceMap.getFont("jLabelCurrentNode.font")); // NOI18N
        jLabelCurrentNode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelCurrentNode.setText(resourceMap.getString("jLabelCurrentNode.text")); // NOI18N
        jLabelCurrentNode.setName("jLabelCurrentNode"); // NOI18N

        jButton_copyDistToALl.setText(resourceMap.getString("jButton_copyDistToALl.text")); // NOI18N
        jButton_copyDistToALl.setToolTipText(resourceMap.getString("jButton_copyDistToALl.toolTipText")); // NOI18N
        jButton_copyDistToALl.setName("jButton_copyDistToALl"); // NOI18N
        jButton_copyDistToALl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_copyDistToALlActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelCurrentNode, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton_copyDistToALl, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelCurrentNode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_copyDistToALl)
                .addContainerGap(6, Short.MAX_VALUE))
        );

        jButton1.setMnemonic('a');
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setActionCommand(resourceMap.getString("jButton1.actionCommand")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextFieldSmoothingAlpha.setText(resourceMap.getString("jTextFieldSmoothingAlpha.text")); // NOI18N
        jTextFieldSmoothingAlpha.setName("jTextFieldSmoothingAlpha"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSmoothingAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jTextFieldSmoothingAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1))
                .addContainerGap(40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Apply user settings (position, distance)
     * 
     * @param evt
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        RSSI_graphApp.getApplication().getWorker(0).actionPerformed(evt);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Copy RSSI->Distance function to all nodes in node registry.
     * Just GUI helper.
     * 
     * @param evt
     */
    private void jButton_copyDistToALlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_copyDistToALlActionPerformed
        try{
            Iterator<Integer> nodes = this.nodeRegister.getNodesSet().iterator();
            while(nodes.hasNext()){
                GenericNode curNode = this.nodeRegister.getNode(nodes.next());
                curNode.getDistanceFunction().setParameters(this.getModelParameters());
            }

            RSSI_graphApp.getApplication().getWorker(0).logToTextarea("Parameters copied", JPannelLoggerLogElement.SEVERITY_DEBUG);
        } catch(Exception e){
            RSSI_graphApp.getApplication().getWorker(0).logToTextarea("Cannot copy settings, error: " + e.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
        }
    }//GEN-LAST:event_jButton_copyDistToALlActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton_copyDistToALl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelCurrentNode;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTabPositions;
    private javax.swing.JTable jTableModelParameters;
    private javax.swing.JTextField jTextFieldSmoothingAlpha;
    // End of variables declaration//GEN-END:variables

}
