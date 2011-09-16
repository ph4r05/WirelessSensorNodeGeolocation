/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Default Data Fetcher for dbUtils
 * @author ph4r05
 */
public class dbUtilsDataFetcher {

    /**
     * Gets specified column of specified type from ResultSet as Object
     *
     * @param column
     * @param columnName
     * @param columnType
     * @param rs
     * @return
     * @throws SQLException
     */
    public Object getDataValue(int column, String columnName, String columnType, ResultSet rs) throws SQLException{
        // check if this columnt is defined by me
        if (this.hasDefinedOwnResult(column)){
            return this.getOwnResult(column, columnName, columnType, rs);
        }

        // else branch
        if ("string".equals(columnType)){
            return (new String(rs.getString(columnName)));
        }
        else if ("integer".equals(columnType)){
            return (new Integer(rs.getInt(columnName)));
        }
        else if ("boolean".equals(columnType)){
            return (new Boolean(rs.getBoolean(columnName)));
        }
        else{
            // try this method
            return this.getDataValueSpecial(column, columnName, columnType, rs);
        }
    }

    /**
     * returns TRUE if for given column we have defined own result
     * 
     * @param column
     * @return
     */
    public boolean hasDefinedOwnResult(int column){
        return false;
    }

    /**
     * Get own result for this column
     *
     * @param column
     * @param columnName
     * @param columnType
     * @param rs
     * @return
     */
    public Object getOwnResult(int column, String columnName, String columnType, ResultSet rs){
        return null;
    }

    /**
     * Handle special user defined types here
     * 
     * @param column
     * @param columnName
     * @param columnType
     * @param rs
     * @return
     */
    public Object getDataValueSpecial(int column, String columnName, String columnType, ResultSet rs){
        throw new IllegalArgumentException("Cannot handle such column type: " + columnName);
    }
}
