/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_localization.localization;

import rssi_graph.nodeRegister.GenericNode;

/**
 * Represents node selection
 * used to determine best choice of node point click on network map (closest)
 * 
 * @author ph4r05
 */

public class ClickBestMatch implements Comparable<ClickBestMatch> {
        public int nodeid;
        public GenericNode node;
        public boolean realPos;
        public double error;
        
        public boolean fromHistory=false;
        public int historyIndex=-1;

        public int compareTo(ClickBestMatch o) {
            if (o==null) return -1;
            if (error == o.error) return 0;
            return error < o.error ? -1 : 1;
        }
    }
