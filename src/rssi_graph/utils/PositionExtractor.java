/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rssi_graph.localization.CoordinateRecord;

/**
 * Extract position of node written in text, typicaly in annotation
 * @author ph4r05
 */
public class PositionExtractor {
    public static final String patternPosition=
            "([+\\-]?[0-9]+)(?:\\s*[\\.,]\\s*([0-9]+))?\\s*[;]\\s*([+\\-]?[0-9]+)(?:\\s*[\\.,]\\s*([0-9]+))?";
    
    public static final String bracedPatternPosition=
            "\\[\\s*"+patternPosition+"\\s*\\]";
    
    public static final String fixedMobilePatternPosition=
            "mobilePos\\s*=\\s*" + bracedPatternPosition;
    
    public static final String specifiedMobilePatternPosition=
            "p[0-9]+\\s*=\\s*" + bracedPatternPosition;
    
    protected List<Pattern> annotPosHeuristic=null;

    public PositionExtractor() {
        if (this.annotPosHeuristic==null){
            this.annotPosHeuristic = new ArrayList<Pattern>();
            this.annotPosHeuristic.add(Pattern.compile(".*?" + PositionExtractor.specifiedMobilePatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + PositionExtractor.fixedMobilePatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + PositionExtractor.bracedPatternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
            this.annotPosHeuristic.add(Pattern.compile(".*?" + PositionExtractor.patternPosition + ".*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL));
        }
    }
    
    /**
     * Try to determine mobile node real position guess from annotation text
     * 
     * @param annotation
     * @return 
     */
    public CoordinateRecord determineCoordinatesFromAnnotation(String annotation){
        CoordinateRecord result = null;
        
        // build matchers, iterate
        Iterator<Pattern> iterator = this.annotPosHeuristic.iterator();
        while(iterator.hasNext()){
            Pattern curPattern = iterator.next();
            Matcher m = curPattern.matcher(annotation);
            
            boolean b = m.matches();
            if(b==false) continue;
            
            // matched, extract data
            String group = m.group();
            if (group==null || group.isEmpty()) continue;
            
            String xvalue = null;
            String yvalue = null;
            
            if (m.group(2)==null){
                xvalue = m.group(1);
            } else {
                xvalue = m.group(1) + "." + m.group(2);
            }
            
            if (m.group(4)==null){
                yvalue = m.group(3);
            } else {
                yvalue = m.group(3) + "." + m.group(4);
            }
            
            // convert string representation to double
            Double xvalueDouble = Double.parseDouble(xvalue);
            Double yvalueDouble = Double.parseDouble(yvalue);
            
            result=new CoordinateRecord();
            result.setX(xvalueDouble.doubleValue());
            result.setY(yvalueDouble.doubleValue());
        }
            
        return result;
    }
}
