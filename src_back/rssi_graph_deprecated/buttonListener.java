///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package rssi_graph_deprecated;
//
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import rssi_graph.RSSI_graphView;
//
///**
// *
// * @author ph4r05
// */
//public class buttonListener implements ActionListener {
//
//    protected RSSI_graphView fw = null;
//    public buttonListener(RSSI_graphView fw)
//    {
//        this.fw = fw;
//
//        RSSI_controller controller = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getController();
//        controller.setGw(this.fw);
//    }
//
//
//    public void actionPerformed(ActionEvent e) {
//        RSSI_controller controller = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getController();
//
//        // action handler for buttons
//        System.err.println("ActionCommand: " + e.getActionCommand());
//        System.err.println("Modifiers: " + e.getModifiers());
//        System.err.println("eventID: " + e.getID());
//        System.err.println("paramString: " + e.paramString());
//        System.err.println("SourceToString: " + e.getSource().toString());
//        System.err.println("SourceClassToString: " + e.getSource().getClass());
//        System.err.println("class: " + e.getClass().toString());
//        System.err.println("Action performed: " + ActionEvent.ACTION_PERFORMED);
//
//        controller.actionButton();
//    }
//}
