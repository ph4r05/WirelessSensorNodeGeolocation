/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import javax.swing.table.AbstractTableModel;

/**
 * My basic abstract table model
 * @author ph4r05
 */
public class TableMyAbstractTableModel extends AbstractTableModel {
        protected String[] columnNames = {"No such column"};
        protected Object[][] data = null;

        public void setData(Object[][] data){
            this.data = data;
        }

        public void clearData(){
            this.data = null;
        }

        public int getColumnCount() {
            if (columnNames==null) return 0;
            return columnNames.length;
        }

        public int getRowCount() {
            if (data==null) return 0;
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @Override
        public Class getColumnClass(int c) {
            // has any data ?
            // if not, return String class as default
            if (this.getRowCount()==0){
                return String.class;
            }
            
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        /**
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col==0;
        }
}
