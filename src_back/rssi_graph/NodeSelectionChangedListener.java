/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_localization;

/**
 * Listener to node selection change
 * @author ph4r05
 */
public interface NodeSelectionChangedListener {
    public void nodeChanged(NodeSelectedEvent evt);
}
