/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Full text render for tables
 * Renders cell as Label + gives tooltip in HTML format (newlines)
 * Given text is stripped for html tags
 * @author ph4r05
 */
public class TableFullTextCellRenderer extends JLabel implements TableCellRenderer {
    private Color selectedBackgroundColor=null;
    private Color selectedForegroundColor=null;
    private Color normalBackgroundColor=null;
    private Color normalForegroundColor=null;

    public Component getTableCellRendererComponent(JTable table, Object string,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (!(string instanceof String)){
            throw new IllegalArgumentException("String expected here");
        }

        if (isSelected){
            this.setOpaque(true);
            this.setForeground(selectedForegroundColor);
            this.setBackground(selectedBackgroundColor);
        }
        else {
            this.setForeground(normalForegroundColor);
            this.setBackground(normalBackgroundColor);
        }

        String newString = (String) string;
        String newStringNoHTML = new String(newString);
        newStringNoHTML = newStringNoHTML.replaceAll("\\<.*?\\>", "");
        newStringNoHTML = newStringNoHTML.replaceAll("\n", "<br/>");

        this.setText(newString);
        setToolTipText("<html>Fulltext: " + newStringNoHTML);
        return this;
    }

    /**
     * Extracts colors from table for selection
     * @param src
     */
    public void extractColorsFrom(JTable src){
        if (src==null){
            throw new IllegalArgumentException("Cannot be null");
        }

        this.setNormalBackgroundColor(src.getBackground());
        this.setNormalForegroundColor(src.getForeground());
        this.setSelectedBackgroundColor(src.getSelectionBackground());
        this.setSelectedForegroundColor(src.getSelectionForeground());

    }

    public Color getSelectedBackgroundColor() {
        return selectedBackgroundColor;
    }

    public void setSelectedBackgroundColor(Color selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
    }

    public Color getSelectedForegroundColor() {
        return selectedForegroundColor;
    }

    public void setSelectedForegroundColor(Color selectedForegroundColor) {
        this.selectedForegroundColor = selectedForegroundColor;
    }

    public Color getNormalBackgroundColor() {
        return normalBackgroundColor;
    }

    public void setNormalBackgroundColor(Color normalBackgroundColor) {
        this.normalBackgroundColor = normalBackgroundColor;
    }

    public Color getNormalForegroundColor() {
        return normalForegroundColor;
    }

    public void setNormalForegroundColor(Color normalForegroundColor) {
        this.normalForegroundColor = normalForegroundColor;
    }
    
}