/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.db;

/**
 *
 * @author ph4r05
 */
public class rssi2dist_measurement {
    public static final String[] columns = {"mid", "mobileNodeId", "staticNodeId",
        "timeStart", "packets", "delay", "anotation", "distance", "testno", "timeend"};

    public static final String[] columnsTypes = {"integer","integer","integer",
        "integer","integer","integer","string","integer","integer","integer"};

    public Integer mid;
    public Integer mobileNodeId;
    public Integer staticNodeId;
    public Integer timeStart;
    public Integer packets;
    public Integer delay;
    public String anotation;
    public Integer distance;
    public Integer testno;
    public Integer timeend;

    /**
     * Return selected columns names in order specified by sel argument
     * Useful when we have wanted column numbers and want to process data
     * from database automatically
     *
     * @param sel
     * @return
     */
    public static String[] getSelectedColumns(int[] sel){
        if (sel==null) throw new IllegalArgumentException("Selection is null");
        String[] result = new String[sel.length];
        
        for(int i=0; i<sel.length; i++){
            if (i >= rssi2dist_measurement.columns.length){
                throw new IllegalArgumentException("No such collumn exists");
            }

            result[i] = rssi2dist_measurement.columns[sel[i]];
        }
        return result;
    }

    /**
     * Returns selected columns types specified by sel argument
     * @see getSelectedColumns
     *
     * @param sel
     * @return
     */
    public static String[] getSelectedColumnsType(int[] sel){
        if (sel==null) throw new IllegalArgumentException("Selection is null");
        String[] result = new String[sel.length];

        for(int i=0; i<sel.length; i++){
            if (i >= rssi2dist_measurement.columnsTypes.length){
                throw new IllegalArgumentException("No such collumn exists");
            }

            result[i] = rssi2dist_measurement.columnsTypes[sel[i]];
        }
        return result;
    }
    
}
