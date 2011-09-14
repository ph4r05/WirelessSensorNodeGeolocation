/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelLogger.java
 *
 * Created on Apr 1, 2011, 11:41:16 AM
 */

package rssi_graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 * Logger window
 *
 * @author ph4r05
 */
public class JPanelLogger extends javax.swing.JPanel {

    /**
     * Do auto scroll?
     */
    private boolean autoscroll=true;

    /**
     * Log window lines
     */
    private LinkedList<JPannelLoggerLogElement> lines=null;

    /**
     * Date format object
     * Format date for log window
     */
    private DateFormat dateFormat = null;

    /**
     * Date object
     * Date information for log entries
     */
    private Date date = null;

    /**
     * Date format string for log entries displayed in area
     */
    private String dateFormatString="HH:mm:ss";

    /**
     * Panel ID for global hash map
     */
    private String panelID=null;

    /** Creates new form JPanelLogger */
    public JPanelLogger() {
        initComponents();

        this.panelID = this.getClass().getName();
        this.initMyPanel();
    }

    public JPanelLogger(String panelID){
        initComponents();

        this.panelID = panelID;
        this.initMyPanel();
    }

    /**
     * Initializes panel attributes
     * registers to global hash map
     */
    public final void initMyPanel(){
        // create linked list
        this.lines = new LinkedList<JPannelLoggerLogElement>();

        // init another components
        this.dateFormat = new SimpleDateFormat(this.dateFormatString);
        this.date = new Date();
    }

    /**
     * Registers to global panel map
     * Cannot do in register since netBeans IDE need to instantiate this when building class
     * But couldn't because registration is only possible when main application is running
     * Thus we need to register it to global space manual somewhere in code.
     *
     */
    public final void registerToGlobal(){
        // register to base
        JPanel put = RSSI_graphApp.getApplication().getPanelMap().put(panelID, this);
    }

    /**
     * Clear all log window
     */
    public synchronized void clearData(){
        this.lines.clear();
        this.clearArea();
    }

    /**
     * clearData log window text area
     */
    public void clearArea(){
        this.jTextArea1.setText("");
    }

    /**
     * Rewrites log window according to parameters
     * Source = this.lines
     */
    public synchronized void rewrite(){
        // reset text area at first
        this.clearArea();

        Iterator<JPannelLoggerLogElement> it = this.lines.iterator();
        while(it.hasNext()){

            // filtering here!
            this.appendLog(it.next());
        }
    }

    /**
     * Appends log element to text area in given format
     * @param a
     */
    public synchronized void appendLog(JPannelLoggerLogElement a){
        // get date string from time
        // this could call jPannelLoggerLogElement method to be polymorphic
        // adding this class to linked list should be allowed
        this.date.setTime(a.getTime());
        this.append("[" + this.dateFormat.format(this.date) + "; " + a.getSeverityLabel() + " ;" + a.getTypeString() + "]: " + a.getText());
    }


    /**
     * Add specified log entry to log window and to linked list
     * @param a
     */
    public void addLogEntry(JPannelLoggerLogElement a){
        this.lines.offer(a);
        this.appendLog(a);
        this.jTextArea1.repaint();
        this.jTextArea1.revalidate();
    }

    /**
     * Builds log entry element and add it
     * @param s
     */
    public void addLogEntry(String s){
        this.addLogEntry(s, 0, "Default", 0, System.currentTimeMillis());
    }

    public void addLogEntry(String s, int type, String typeString, int subtype, int code,  int severity){
        JPannelLoggerLogElement tmp = new JPannelLoggerLogElement(System.currentTimeMillis(), typeString, type, subtype, code, severity, s);
        this.addLogEntry(tmp);
    }

    public void addLogEntry(String s, int type, String typeString, int severity){
        this.addLogEntry(s, type, typeString, severity, System.currentTimeMillis());
    }

    public void addLogEntry(String s, int type, String typeString, int severity, long time){
        JPannelLoggerLogElement tmp = new JPannelLoggerLogElement(time, typeString, type, 0, 0, severity, s);
        this.addLogEntry(tmp);
    }

    /**
     * Append string to log window with line separator
     * @param s
     */
    public synchronized void append(String s){
        this.appendStr(s + RSSI_graphApp.getLineSeparator());
    }

    /**
     * Append string to log window without separator
     * @param s
     */
    public synchronized void appendStr(String s){
        this.jTextArea1.append(s);

        // autoscroll
        if (this.isAutoscroll()){
            this.jTextArea1.setCaretPosition(this.jTextArea1.getText().length());
        }
    }


    /**
     * =========================================================================
     *
     * GETTERS+SETTERS
     *
     * =========================================================================
     */

    public boolean isAutoscroll() {
        return autoscroll;
    }

    public synchronized void setAutoscroll(boolean autoscroll) {
        this.autoscroll = autoscroll;
    }

    public String getDateFormatString() {
        return dateFormatString;
    }

    public synchronized void setDateFormatString(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    public String getPanelID() {
        return panelID;
    }



    /**
     * =========================================================================
     *
     * AUTO-GENERATED CODE
     *
     * =========================================================================
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JPanelLogger.class);
        setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("Form.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setAutoscrolls(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

}
