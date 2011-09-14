/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * jDialogLoadData.java
 *
 * Created on Apr 8, 2011, 4:18:00 PM
 */

package rssi_graph.rssi;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import rssi_graph.JPannelLoggerLogElement;
import rssi_graph.RSSI_graphApp;
import rssi_graph.localization.CoordinateRecord;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.utils.PositionExtractor;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 *
 * @author ph4r05
 */
public class jDialogLoadData extends javax.swing.JDialog {

    /**
     * parent panel where show button was pressed
     */
    protected javax.swing.JPanel parentPanel = null;
    
    /**
     * Position extractor from annotation
     */
    protected PositionExtractor pe = null;

    /** Creates new form jDialogLoadData */
    public jDialogLoadData(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        this.pe = new PositionExtractor();
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
            this.columnNames = new String[] {"Position [x;y]", "Distance", "Mobile", "Static", "Mid", "Annotation"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col==0 || col==1;
        }

        @Override
        public Class getColumnClass(int c) {
            if (c==0) return String.class;
            else if (c==1) return Double.class;
            else if (c==2 || c==3 || c==4) return Integer.class;
            else return String.class;
        }
    }

    
    /**
     * reloads current selection data
     */
    public void reloadData(){
        // nothing to do if there is no parent panel
        // something went wrong probably since parent panel is set right after
        // initialization
        if (this.parentPanel==null || !(this.parentPanel instanceof jPannelRSSI2DistanceDataLoader)){
            return;
        }
        
        final jPannelRSSI2DistanceDataLoader myPanel = 
                (jPannelRSSI2DistanceDataLoader) this.parentPanel;
        
        List<DistanceFromPositionData> selectedData = myPanel.getSelectedData();

        // if there is no data to load, display message box if visible, then return
        if (selectedData==null || selectedData.isEmpty()){
            // display message box if is showing
            if (this.isShowing()){
                JOptionPane.showMessageDialog(null,
                        "No data to load found!",
                        "No data",
                JOptionPane.WARNING_MESSAGE);
            }

            return;
        }
        
        // refresh view
        // build new table model
        PositionTableModel model = new PositionTableModel();
        Object[][] data = null;
        
        DecimalFormat df = new DecimalFormat("00.0");

        // real data count
        int datacount = selectedData.size();
        data = new Object[datacount][6];
            
        // my positions
        // fill model data
        Iterator<DistanceFromPositionData> iterator = selectedData.iterator();
        
        // local cache for coordinate record
        Map<Integer, CoordinateRecord> coordCache = new HashMap<Integer, CoordinateRecord>();
        NodeRegister nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (nodeRegister==null) return;
                
        for(int i=0; iterator.hasNext(); i++){
            DistanceFromPositionData curKey = iterator.next();

            // determine position for this MID
            CoordinateRecord realPos = null;

            if (coordCache.containsKey(curKey.mid)){
                realPos = coordCache.get(curKey.mid);
            } else {
                // nothing, try to determine from annotation
                // "-" means null
                realPos = this.pe.determineCoordinatesFromAnnotation(curKey.annotation);
            }

            // position
            data[i][0] = realPos==null ? "-" : df.format(realPos.getX()) + ";" + df.format(realPos.getY());

            
            if (nodeRegister.existsNode(curKey.staticNodeId)
                    && nodeRegister.getNode(curKey.staticNodeId).getPosition() != null){
                // distance
                data[i][1] = Double.valueOf(realPos.getEuclidDistanceFrom(nodeRegister.getNode(curKey.staticNodeId).getPosition()));
            } else {
                data[i][1] = Double.valueOf(-1.0);
            }
            
            // mobile
            data[i][2] = Integer.valueOf(curKey.mobileNodeId);
            
            // static
            data[i][3] = Integer.valueOf(curKey.staticNodeId);
            
            // mid
            data[i][4] = Integer.valueOf(curKey.mid);

            // annotation
            data[i][5] = curKey.annotation;
        }
        
        model.setData(data);
        this.jTablePositions.setModel(model);
        this.jTablePositions.repaint();
        this.jTablePositions.revalidate();
    }
    
    /**
     * Return distance. Builds list of DistanceFromPositionData filled from 
     * data currently filled in table. 
     * @return 
     */
    List<DistanceFromPositionData> getDistanceDataFromTable(){
        List<DistanceFromPositionData> list = new LinkedList<DistanceFromPositionData>();
        for(int i=0, cnI=this.jTablePositions.getRowCount(); i<cnI; i++){
            int rowModelIndex = this.jTablePositions.convertRowIndexToModel(i);
            DistanceFromPositionData curData = new DistanceFromPositionData();
            curData.annotation = (String) this.jTablePositions.getModel().getValueAt(rowModelIndex, 5);
            curData.distance = (Double) this.jTablePositions.getModel().getValueAt(rowModelIndex, 1);
            curData.mid = (Integer) this.jTablePositions.getModel().getValueAt(rowModelIndex, 4);
            curData.mobileNodeId = (Integer) this.jTablePositions.getModel().getValueAt(rowModelIndex, 2);
            curData.staticNodeId = (Integer) this.jTablePositions.getModel().getValueAt(rowModelIndex, 3);
            curData.mobilePosition = this.pe.determineCoordinatesFromAnnotation((String) this.jTablePositions.getModel().getValueAt(rowModelIndex, 0));
            list.add(curData);
        }
        
        return list;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jTextMinimalRefDistance = new javax.swing.JTextField();
        jTextMaximalRefDistance = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jCheckFitCurve = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jCheckXGroupTest = new javax.swing.JCheckBox();
        jCheckXGroupMobileId = new javax.swing.JCheckBox();
        jCheckXGroupStaticId = new javax.swing.JCheckBox();
        jCheckXGroupTx = new javax.swing.JCheckBox();
        jCheckXGroupMid = new javax.swing.JCheckBox();
        jTabbedXvalueSelector = new javax.swing.JTabbedPane();
        jPanelXvaluePosition = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTablePositions = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jPanelXvalueParameter = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboXValue = new javax.swing.JComboBox();
        jTextParamDirectMulti = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextParamConstant = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jCheckOptimizeConstant = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(jDialogLoadData.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextMinimalRefDistance.setText(resourceMap.getString("jTextMinimalRefDistance.text")); // NOI18N
        jTextMinimalRefDistance.setName("jTextMinimalRefDistance"); // NOI18N

        jTextMaximalRefDistance.setText(resourceMap.getString("jTextMaximalRefDistance.text")); // NOI18N
        jTextMaximalRefDistance.setName("jTextMaximalRefDistance"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jCheckFitCurve.setSelected(true);
        jCheckFitCurve.setText(resourceMap.getString("jCheckFitCurve.text")); // NOI18N
        jCheckFitCurve.setName("jCheckFitCurve"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setToolTipText(resourceMap.getString("jPanel1.toolTipText")); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jCheckXGroupTest.setSelected(true);
        jCheckXGroupTest.setText(resourceMap.getString("jCheckXGroupTest.text")); // NOI18N
        jCheckXGroupTest.setName("jCheckXGroupTest"); // NOI18N

        jCheckXGroupMobileId.setText(resourceMap.getString("jCheckXGroupMobileId.text")); // NOI18N
        jCheckXGroupMobileId.setName("jCheckXGroupMobileId"); // NOI18N

        jCheckXGroupStaticId.setSelected(true);
        jCheckXGroupStaticId.setText(resourceMap.getString("jCheckXGroupStaticId.text")); // NOI18N
        jCheckXGroupStaticId.setName("jCheckXGroupStaticId"); // NOI18N

        jCheckXGroupTx.setSelected(true);
        jCheckXGroupTx.setText(resourceMap.getString("jCheckXGroupTx.text")); // NOI18N
        jCheckXGroupTx.setName("jCheckXGroupTx"); // NOI18N

        jCheckXGroupMid.setText(resourceMap.getString("jCheckXGroupMid.text")); // NOI18N
        jCheckXGroupMid.setName("jCheckXGroupMid"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckXGroupTest)
                    .addComponent(jCheckXGroupMobileId)
                    .addComponent(jCheckXGroupStaticId))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckXGroupTx)
                    .addComponent(jCheckXGroupMid))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckXGroupTest)
                    .addComponent(jCheckXGroupTx))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckXGroupMobileId)
                    .addComponent(jCheckXGroupMid))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckXGroupStaticId)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jTabbedXvalueSelector.setName("jTabbedXvalueSelector"); // NOI18N

        jPanelXvaluePosition.setName("jPanelXvaluePosition"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTablePositions.setAutoCreateRowSorter(true);
        jTablePositions.setModel(new javax.swing.table.DefaultTableModel(
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
        jTablePositions.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTablePositions.setName("jTablePositions"); // NOI18N
        jScrollPane1.setViewportView(jTablePositions);

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        javax.swing.GroupLayout jPanelXvaluePositionLayout = new javax.swing.GroupLayout(jPanelXvaluePosition);
        jPanelXvaluePosition.setLayout(jPanelXvaluePositionLayout);
        jPanelXvaluePositionLayout.setHorizontalGroup(
            jPanelXvaluePositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelXvaluePositionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelXvaluePositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanelXvaluePositionLayout.setVerticalGroup(
            jPanelXvaluePositionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelXvaluePositionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedXvalueSelector.addTab(resourceMap.getString("jPanelXvaluePosition.TabConstraints.tabTitle"), jPanelXvaluePosition); // NOI18N

        jPanelXvalueParameter.setName("jPanelXvalueParameter"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jComboXValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Distance [cm]", "mid", "NodeID", "TxPower", "NodeID+TxPower" }));
        jComboXValue.setName("jComboXValue"); // NOI18N
        jComboXValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboXValueItemStateChanged(evt);
            }
        });

        jTextParamDirectMulti.setText(resourceMap.getString("jTextParamDirectMulti.text")); // NOI18N
        jTextParamDirectMulti.setToolTipText(resourceMap.getString("jTextParamDirectMulti.toolTipText")); // NOI18N
        jTextParamDirectMulti.setName("jTextParamDirectMulti"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextParamConstant.setText(resourceMap.getString("jTextParamConstant.text")); // NOI18N
        jTextParamConstant.setName("jTextParamConstant"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        javax.swing.GroupLayout jPanelXvalueParameterLayout = new javax.swing.GroupLayout(jPanelXvalueParameter);
        jPanelXvalueParameter.setLayout(jPanelXvalueParameterLayout);
        jPanelXvalueParameterLayout.setHorizontalGroup(
            jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelXvalueParameterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelXvalueParameterLayout.createSequentialGroup()
                        .addGroup(jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelXvalueParameterLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(50, 50, 50)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextParamDirectMulti, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextParamConstant, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4)
                            .addGroup(jPanelXvalueParameterLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(6, 6, 6)
                                .addComponent(jComboXValue, 0, 251, Short.MAX_VALUE)))
                        .addContainerGap(373, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelXvalueParameterLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(364, Short.MAX_VALUE))))
        );

        jPanelXvalueParameterLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextParamConstant, jTextParamDirectMulti});

        jPanelXvalueParameterLayout.setVerticalGroup(
            jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelXvalueParameterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboXValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(27, 27, 27)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelXvalueParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(jTextParamDirectMulti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jTextParamConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(66, 66, 66))
        );

        jTabbedXvalueSelector.addTab(resourceMap.getString("jPanelXvalueParameter.TabConstraints.tabTitle"), jPanelXvalueParameter); // NOI18N

        jCheckOptimizeConstant.setSelected(true);
        jCheckOptimizeConstant.setText(resourceMap.getString("jCheckOptimizeConstant.text")); // NOI18N
        jCheckOptimizeConstant.setName("jCheckOptimizeConstant"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedXvalueSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 734, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextMaximalRefDistance)
                                    .addComponent(jTextMinimalRefDistance, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jCheckFitCurve)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckOptimizeConstant)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2)))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedXvalueSelector, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jTextMinimalRefDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jTextMaximalRefDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckFitCurve)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckOptimizeConstant)
                            .addComponent(jButton2)
                            .addComponent(jButton1))
                        .addGap(43, 43, 43))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // dispose
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // load data
        final jPannelRSSI2DistanceDataLoader dataLoader;
        if (this.parentPanel instanceof jPannelRSSI2DistanceDataLoader){
            dataLoader = (jPannelRSSI2DistanceDataLoader) this.parentPanel;

            try {
                // my parameters write here
                Map<String, Object> options = new HashMap<String, Object>();
                
                // determine computation method from selected panel in jTabbedPane
                int xvalueComputationMethod = this.jPanelXvaluePosition.isShowing() ? 1 : 0;
                
                // parameter verification
                // if is empty, set default values
                if (this.jTextParamDirectMulti.getText()==null || "".equals(this.jTextParamDirectMulti.getText())){
                    this.jTextParamDirectMulti.setText("1");
                }
                
                if (this.jTextParamConstant.getText()==null || "".equals(this.jTextParamConstant.getText())){
                    this.jTextParamConstant.setText("0");
                }
                
                if (this.jTextMinimalRefDistance.getText()==null || "".equals(this.jTextMinimalRefDistance.getText())){
                    this.jTextMinimalRefDistance.setText("1");
                }
                
                if (this.jTextMaximalRefDistance.getText()==null || "".equals(this.jTextMaximalRefDistance.getText())){
                    this.jTextMaximalRefDistance.setText("1");
                }
                
                // get parameters from fields to Objects (later stored to map)
                Double directMulti = Double.valueOf(this.jTextParamDirectMulti.getText());
                Double constant = Double.valueOf(this.jTextParamConstant.getText());
                Double minimalRefDistance = Double.valueOf(this.jTextMinimalRefDistance.getText());
                Double maximalRefDistance = Double.valueOf(this.jTextMaximalRefDistance.getText());
                
                // xvalue group (define one data serie)
                Set<String> xvalueGroup = new HashSet<String>();
                // determine selected elements from group
                if (this.jCheckXGroupMid.isSelected()){
                    xvalueGroup.add("mid");
                }
                
                if (this.jCheckXGroupMobileId.isSelected()){
                    xvalueGroup.add("mobileId");
                }
                
                if (this.jCheckXGroupStaticId.isSelected()){
                    xvalueGroup.add("staticId");
                }
                
                if (this.jCheckXGroupTest.isSelected()){
                    xvalueGroup.add("testno");
                }
                
                if (this.jCheckXGroupTx.isSelected()){
                    xvalueGroup.add("txpower");
                }
                
                // if xgroup is empty, display error and exit
                if (xvalueGroup.isEmpty()){
                    JOptionPane.showMessageDialog(null,
                        "Incorrect data filled in!" + RSSI_graphApp.getLineSeparator() +
                        "Please select at least one element from X Value Group.",
                        "Data error",
                        JOptionPane.WARNING_MESSAGE);
                    
                    return;
                }
                
                options.put("XvalueComputationMethod", xvalueComputationMethod);
                options.put("XvaluePositionsData", this.getDistanceDataFromTable());
                options.put("OptimizeConstant", this.jCheckOptimizeConstant.isSelected());
                options.put("XvalueGroup", xvalueGroup);
                options.put("ParamSource", this.jComboXValue.getSelectedIndex());
                options.put("ParamDirectMulti", directMulti);
                options.put("ParamConstant", constant);
                options.put("MinimalRefDistance", minimalRefDistance);
                options.put("MaximalRefDistance", maximalRefDistance);
                options.put("doCurveFit", this.jCheckFitCurve.isSelected());

                 // display info
                JOptionPane.showMessageDialog(null,
                    "Prepared for data loading." + RSSI_graphApp.getLineSeparator() +
                    "Data load will start after closing this information window, please wait until load is finished." + RSSI_graphApp.getLineSeparator() +
                    "When finished, settings dialog dissapears."    ,
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                
                
                // finaly, load data
                // pass options
                dataLoader.loadData(options);
            } catch (SQLException ex) {
                Logger.getLogger(jDialogLoadData.class.getName()).log(Level.SEVERE, null, ex);
                dataLoader.logToWindow("Exception thrown when loading data. E: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
                dispose();
            } catch (Exception ex) {
                Logger.getLogger(jDialogLoadData.class.getName()).log(Level.SEVERE, null, ex);
                dataLoader.logToWindow("Exception thrown when loading data. E: " + ex.getMessage(), JPannelLoggerLogElement.SEVERITY_ERROR);
                dispose();
            }

            dispose();
        }
        else{
            throw new IllegalStateException("Unknown parent panel, don't know what to do");
        }

        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * set default multiplicator
     */ 
    private void jComboXValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboXValueItemStateChanged
        int selectedIndex = this.jComboXValue.getSelectedIndex();
        if (selectedIndex==0) {
            this.jTextParamDirectMulti.setText("0.01");
        } else {
            this.jTextParamDirectMulti.setText("1");
        }
    }//GEN-LAST:event_jComboXValueItemStateChanged

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                jDialogLoadData dialog = new jDialogLoadData(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    public JPanel getParentPanel() {
        return parentPanel;
    }

    public void setParentPanel(JPanel parentPanel) {
        this.parentPanel = parentPanel;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckFitCurve;
    private javax.swing.JCheckBox jCheckOptimizeConstant;
    private javax.swing.JCheckBox jCheckXGroupMid;
    private javax.swing.JCheckBox jCheckXGroupMobileId;
    private javax.swing.JCheckBox jCheckXGroupStaticId;
    private javax.swing.JCheckBox jCheckXGroupTest;
    private javax.swing.JCheckBox jCheckXGroupTx;
    private javax.swing.JComboBox jComboXValue;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelXvalueParameter;
    private javax.swing.JPanel jPanelXvaluePosition;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedXvalueSelector;
    private javax.swing.JTable jTablePositions;
    private javax.swing.JTextField jTextMaximalRefDistance;
    private javax.swing.JTextField jTextMinimalRefDistance;
    private javax.swing.JTextField jTextParamConstant;
    private javax.swing.JTextField jTextParamDirectMulti;
    // End of variables declaration//GEN-END:variables

}
