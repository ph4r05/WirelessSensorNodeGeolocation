/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.util.Map;

/**
 * Utilities for options manipulation
 *
 * @author ph4r05
 */
public class OptionsUtils {

    /**
     * Sets default options to map if not set yet
     *
     * @param opt
     * @param paramName
     * @param paramValue
     */
    public static void setDefaultOption(Map<String, Object> opt, String paramName, Object paramValue){
        if (opt.containsKey(paramName)) return;
        opt.put(paramName, paramValue);
    }

    public static boolean isOption(Map<String, Object> opt, String paramName){
        return opt.containsKey(paramName);
    }

    public static Object getOption(Map<String, Object> opt, String paramName){
        if (!OptionsUtils.isOption(opt, paramName)) throw new NullPointerException("Cannot find such option: " + paramName);
        return opt.get(paramName);
    }
}
