/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelNodeSelector.java
 *
 * Created on Apr 5, 2011, 8:32:14 PM
 */

package rssi_graph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import rssi_graph.nodeRegister.GenericNode;
import rssi_graph.nodeRegister.NodeRegister;
import rssi_graph.nodeRegister.NodeRegisterEvent;
import rssi_graph.nodeRegister.NodeRegisterEventListener;
import rssi_graph.utils.TableMyAbstractTableModel;

/**
 *
 * @author ph4r05
 */
public class JPanelNodeSelector extends javax.swing.JPanel implements NodeRegisterEventListener {

    protected NodeRegister nodeRegister;
    
    /**
     * registered selection listeners
     */
    protected List<NodeSelectionChangedListener> selectionListeners;

    /** Creates new form JPanelNodeSelector */
    public JPanelNodeSelector() {
        initComponents();
        
        SelectionListener listener = new SelectionListener(this.jTableNodes);
        this.jTableNodes.getSelectionModel().addListSelectionListener(listener);
        this.jTableNodes.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    public void initThis(){
        this.selectionListeners = new LinkedList<NodeSelectionChangedListener> ();
        this.nodeRegister = RSSI_graphApp.sGetNodeRegister();
        if (this.nodeRegister==null) return;

        // register as change listener
        this.nodeRegister.addChangeListener(this);

        // load nodes from register to list
        this.loadNodes();
        
//        ListSelectionModel rowSM = this.jTableNodes.getSelectionModel();
//        rowSM.addListSelectionListener(this);
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
                if (sel==null){
                    sel = new int[0];
                }
                
                // notify change listeners about change, convert & get node ID
                int nodes[] = new int[sel.length];
                
                for(int i=0, cnI=sel.length; i<cnI; i++){
                     int modelI = this.table.convertRowIndexToModel(sel[i]);
                     nodes[i] = (Integer)this.table.getModel().getValueAt(modelI, 0);
                }
                
                notifyListeners(nodes);
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
     * notify registered listeners for change in node selection
     * @param selected 
     */
    protected void notifyListeners(int selected[]){
        if (selected==null) return;
        NodeSelectedEvent evt = new NodeSelectedEvent();
        evt.selectedNodes = selected;
        
        Iterator<NodeSelectionChangedListener> iterator = this.selectionListeners.iterator();
        while(iterator.hasNext()){
            iterator.next().nodeChanged(evt);
        }
    }
    
    /**
     * Adds given listener to list
     * @param listener 
     */
    public void addNodeSelectionChangedListener(NodeSelectionChangedListener listener){
        if (listener==null) throw new NullPointerException("Empty NodeSelectionChangedListener given");
        this.selectionListeners.add(listener);
    }
   
    /**
     * Removes given listener to list
     * @param listener 
     */
    public void removeNodeSelectionChangedListener(NodeSelectionChangedListener listener){
        if (listener==null) throw new NullPointerException("Empty NodeSelectionChangedListener given");
        this.selectionListeners.remove(listener);
    }

    /**
     * React on node discovery
     * @param evt
     */
    public void accept(NodeRegisterEvent evt) {
        Map<Integer, String> changes = evt.getChanges();
        if (changes==null){
            this.loadNodes();
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
            this.loadNodes();
        }
    }

    /**
     * loads nodes from node register
     */
    public void loadNodes(){
        if (this.nodeRegister==null) return;
        Set<Integer> nodesSet = this.nodeRegister.getNodesSet();
        Iterator<Integer> nodeIt = nodesSet.iterator();
        Object[][] modelData = new Object[nodesSet.size()+1][1];

        // table data model for position table
        NodeIdTableModel pmod = new NodeIdTableModel();

        for(int i=0; nodeIt.hasNext(); i++){
            GenericNode curNode = this.nodeRegister.getNode(nodeIt.next());
            if (curNode==null) throw new NullPointerException("Registered node is null");

            modelData[i][0] = new Integer(curNode.getNodeId());
        }

        // always add broadcast
        modelData[nodesSet.size()][0] = new Integer(65535);

        pmod.setData(modelData);
        jTableNodes.setModel(pmod);
    }

    /**
     * return selected nodes
     * @return
     */
    public int[] getSelectedNodes(){
        // scan table for selected nodes
        //Array<Integer> result = new Array<Integer>();
        int sel[] = this.jTableNodes.getSelectedRows();
        if (sel==null || sel.length==0){
            return new int[0];
        }

        int[] result = new int[sel.length];
        for(int i=0, cnI=sel.length; i<cnI; i++){
            int modelI = this.jTableNodes.convertRowIndexToModel(sel[i]);
            result[i] = (Integer)this.jTableNodes.getModel().getValueAt(modelI, 0);
        }

        return result;
    }


    /**
     * Table model for nodes ID
     */
     class NodeIdTableModel extends TableMyAbstractTableModel {
        public NodeIdTableModel() {
            this.columnNames = new String[] {"NodeID"};
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableNodes = new javax.swing.JTable();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JPanelNodeSelector.class);
        setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("Form.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableNodes.setAutoCreateRowSorter(true);
        jTableNodes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Integer(65535)}
            },
            new String [] {
                "NodeID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableNodes.setName("jTableNodes"); // NOI18N
        jScrollPane1.setViewportView(jTableNodes);
        jTableNodes.getColumnModel().getColumn(0).setResizable(false);
        jTableNodes.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableNodes.columnModel.title0")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableNodes;
    // End of variables declaration//GEN-END:variables

}
