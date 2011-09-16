/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * database related utils/helper methods
 * @author ph4r05
 */
public class dbUtils {

    /**
     * Fetch data from resultset by given column names and types
     * Use default fetcher
     *
     * @param rs
     * @param columnNames
     * @param columnTypes
     * @return
     * @throws SQLException
     */
    public static LinkedList<ArrayList<Object>> fetchDataByColumns(ResultSet rs, String[] columnNames, String[] columnTypes) throws SQLException{
        dbUtilsDataFetcher fetcher = new dbUtilsDataFetcher();
        return dbUtils.fetchDataByColumns(rs, columnNames, columnTypes, fetcher);
    }

    /**
     * Fetch data from resultset by given column names and types
     *
     * @param rs
     * @param columnNames
     * @param columnTypes
     * @return
     * @throws SQLException
     */
    public static LinkedList<ArrayList<Object>> fetchDataByColumns(ResultSet rs, String[] columnNames, String[] columnTypes, dbUtilsDataFetcher fetcher) throws SQLException{
        // argument check
        if (columnNames==null || columnTypes==null || columnNames.length != columnTypes.length){
            throw new IllegalArgumentException("Illegal column parameters (null or size mismatch)");
        }

        // closed resultset?
        if (rs.isClosed()){
            return null;
        }

        // data fetching
        LinkedList<ArrayList<Object>> tmpData = new LinkedList<ArrayList<Object>>();
        for(int i=0; rs.next(); i++){
            // new data object
            ArrayList<Object> tmpArrObject = new ArrayList<Object>(columnNames.length);
            tmpArrObject.ensureCapacity(columnNames.length);
            
            for(int j=0, toJ=columnNames.length; j<toJ; j++){
                tmpArrObject.add(fetcher.getDataValue(j, columnNames[j], columnTypes[j], rs));
            }

            // store to list
            tmpData.add(tmpArrObject);
        }

        return tmpData;
    }

    /**
     * Convert dynamic structure to pure 2D array
     * (for tableModels and so on)
     * @param a
     * @return
     */
    public static Object[][] fetchedResultToObjectArray(LinkedList<ArrayList<Object>> a){
        // nullpointer?
        if (a==null){
            throw new IllegalArgumentException("Cannot be null!");
        }

        // empty?
        if (a.size() == 0){
            return new Object[0][0];
        }

        // size of first record
        // for testing on same size of Arraylists
        int firstSize = a.getFirst().size();
        int listSize = a.size();

        // all arrayLists should has equal length
        Object[][] result = new Object[listSize][firstSize];

        for(int i=0; i<listSize; i++){
            // check size of array
            ArrayList<Object> tmp = a.get(i);

            // cannot be null
            if (tmp==null){
                throw new IllegalArgumentException("ArrayList is null");
            }

            // size test, same resultset => same number of columns
            if (tmp.size() != firstSize){
                throw new IllegalArgumentException("Different sizes of arrayLists detected.");
            }

            for(int j=0; j<firstSize; j++){
                result[i][j] = tmp.get(j);
            }
        }

        return result;
    }
}
