/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Helper methods
 * @author ph4r05
 */
public class TableHelper {
    
    /**
     * returns selected rows in table
     * @return
     */
    public static int[] getSelectedRows(JTable tbl, boolean translateToModel){
        if (tbl==null){
            throw new NullPointerException("Table is null");
        }

        // determine selected test numbers from table
        int[] sel = tbl.getSelectedRows();
        if (sel==null || sel.length==0) return null;

        // if we want only view rows selected, return now
        // otherwise translate to row selection
        if (translateToModel==false) return sel;

        // get data from selected table
        TableModel tmod = tbl.getModel();
        if (!(tmod instanceof TableModel)){
            throw new IllegalStateException("No table model");
        }

        int[] result = new int[sel.length];
        for(int i=0, cn=sel.length; i<cn; i++){
            // selected rowID need to be converted to model row id since after
            // sorting rows does not match
            result[i] = tbl.convertRowIndexToModel(sel[i]);
        }

        return result;
    }
}
