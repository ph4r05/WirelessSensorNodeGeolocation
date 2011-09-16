/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.ujmp.core.collections.ArrayIndexList;

/**
 * textual histogram
 * @author ph4r05
 */
public class TextHistogram {
    private double step;
    private Map<Double, ArrayList<TextHistogramData>> data;

    public TextHistogram() {
        data = new HashMap<Double, ArrayList<TextHistogramData>>();
    }
    
    /**
     * add data to histogram
     * 
     * @param data 
     */
    public void add(TextHistogramData data){
        double category = this.getCategory(data.getHistogramData(null));
        Double catD = Double.valueOf(category);
        
        if (!this.data.containsKey(catD)){
            this.data.put(catD, new ArrayIndexList<TextHistogramData>());
        }
        
        ArrayList<TextHistogramData> tmp = this.data.get(catD);
        tmp.add(data);
        
        this.data.put(catD, tmp);
    }
    
    /**
     * get representant according to step. 
     * @param data 
     */
    public double getCategory(double data){
        return step * Math.floor(data/step);
    }
    
    /**
     * Clear histogram data
     */
    public void clear(){
        this.data = new HashMap<Double, ArrayList<TextHistogramData>>();
    }
    
    /**
     * Export histogram data
     */
    public String export(){
        if (this.data==null) return null;
        
        StringBuilder sb = new StringBuilder();
        
        // copy collection and sort it.
        ArrayList<Double> keys = new ArrayList<Double>(this.data.keySet());
        Collections.sort(keys);
        Iterator<Double> iterator = keys.iterator();
        
        double lastCategory=-1;
        int c=0;
        
        while(iterator.hasNext()){
            Double curKey = iterator.next();
            ArrayList<TextHistogramData> list = this.data.get(curKey);
            if(c==0){
                lastCategory=curKey;
            }
            
            ++c;
            
            // fill empty data categories between
            int missingCn = (int) Math.floor((curKey - lastCategory) / step) - 1;
            
            if (missingCn > 0){
                for(int j=0; j<missingCn; j++){
                    double curCategory = lastCategory + (j+1) * step;
                    sb.append("key;").append(curCategory).append(";").append(0).append(": ").append(rssi_localization.RSSI_graphApp.getLineSeparator());
                }
            }
            
            sb.append("key;").append(curKey).append(";").append(list.size()).append(": ");
            
            Iterator<TextHistogramData> iterator1 = list.iterator();
            while(iterator1.hasNext()){
                TextHistogramData histData = iterator1.next();
                if (histData==null) continue;
                
                String toStringForHistogram = histData.toStringForHistogram();
                if (toStringForHistogram==null) continue;
                
                sb.append(toStringForHistogram).append(';');
            }
            
            sb.append(rssi_localization.RSSI_graphApp.getLineSeparator());
            lastCategory=curKey;
        }
        
        
        return sb.toString();
    }
    
    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }
}
