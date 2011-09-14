/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

/**
 * Log element 
 *
 * @author ph4r05
 */
public class JPannelLoggerLogElement {

    /**
     * =========================================================================
     *
     * CONSTANTS
     *
     * =========================================================================
     */

    public static final int SEVERITY_DEBUG=0;
    public static final int SEVERITY_INFO=1;
    public static final int SEVERITY_NOTICE=2;
    public static final int SEVERITY_WARNING=3;
    public static final int SEVERITY_ERROR=4;
    public static final int SEVERITY_FATAL=5;

    public static final String[] SEVERITY_LABEL={"debug", "info", "notice", "waring",
            "error", "fatal"};

    /**
     * =========================================================================
     *
     * ATTRIBUTES
     *
     * =========================================================================
     */

    /**
     * time of occurred event
     */
    private long time;

    /**
     * type & categorizing
     */
    private String typeString;
    private int type;
    private int subtype;
    private int code;
    
    /**
     * Severity
     * Notice, info, warning, critical, fatal
     */
    private int severity;

    /**
     * Log entry
     */
    private String text;

    /**
     * Returns severity level
     * 
     * @return
     */
    public String getSeverityLabel(){
        if (this.getSeverity() >= JPannelLoggerLogElement.SEVERITY_LABEL.length){
            throw new IllegalArgumentException("Severity level does not have string representation");
        }
        
        return JPannelLoggerLogElement.SEVERITY_LABEL[this.getSeverity()];
    }

    /**
     * =========================================================================
     *
     * CONSTRUCTORS
     *
     * =========================================================================
     */

    public JPannelLoggerLogElement(String text) {
        this.text = text;
    }

    public JPannelLoggerLogElement(int severity, String text) {
        this.severity = severity;
        this.text = text;
    }

    public JPannelLoggerLogElement(int type, int severity, String text) {
        this.type = type;
        this.severity = severity;
        this.text = text;
    }

    public JPannelLoggerLogElement(long time, int type, int subtype, int code, int severity, String text) {
        this.time = time;
        this.type = type;
        this.subtype = subtype;
        this.code = code;
        this.severity = severity;
        this.text = text;
    }

    public JPannelLoggerLogElement(long time, String typeString, int type, int subtype, int code, int severity, String text) {
        this.time = time;
        this.typeString = typeString;
        this.type = type;
        this.subtype = subtype;
        this.code = code;
        this.severity = severity;
        this.text = text;
    }

    /**
     * =========================================================================
     *
     * GETTERS+SETTERS
     *
     * =========================================================================
     */

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeString() {
        return typeString;
    }

    public void setTypeString(String typeString) {
        this.typeString = typeString;
    }
    
}
