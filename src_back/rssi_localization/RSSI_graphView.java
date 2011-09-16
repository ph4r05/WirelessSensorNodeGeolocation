/*
 * RSSI_graphView.java
 */

package rssi_localization;

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import org.jdesktop.application.Task;
import rssi_graph.localization.JPanelLocalizationMain;
import rssi_graph.localization.JPanelTXpowerSelector;
import rssi_graph.nodeRegister.JPanelNodeRegisterMain;
import rssi_graph.rssi.jPannelRSSI2DistanceDataLoader;
import rssi_graph.rssi.chart.JPannelRSSI2DistanceChart;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class RSSI_graphView extends FrameView {

    public RSSI_graphView(SingleFrameApplication app) {
        super(app);
        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });

        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }

        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });

        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
//                    st progressBar.setVisible(true);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setValue(value);
                }
            }
        });

        // add action listener for next meassurement button
//        jBNextMeassurement.addActionListener(new buttonListener(this));

        // action listeners for worker R2D
        jButton_save.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_drop.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_R2D_start.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jToggle_R2D_ON.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(1));
        jButtonFinishTry.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jToggle_R2D_Timer.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_R2D_AddToGraph.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_R2D_MoveStep.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jCheck_R2D_resetBeforeRound.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_R2D_prepMass.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));
        jButton_R2D_prepMed.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(1));

        // action listeners for localization
//        jButton_localSave.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(0));
//        jButton_localDrop.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(0));
//        jButton_Loc_start.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(0));
//        jToggle_Local_ON.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(0));

        // action listeners for command worker
        jButton_comm_sendRT.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendRTValue.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendTinyReports.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendTXpower.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendRepStatus.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendAbort.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_sendReset.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jButton_comm_discover.addActionListener((ActionListener) RSSI_graphApp.getApplication().getWorker(2));
        jToggle_comm_on.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(2));
        jCheck_comm_autoNodeDiscovery.addItemListener((ItemListener) RSSI_graphApp.getApplication().getWorker(2));

        // handle with special method here
        //jBMeasurementDone.addActionListener(null);

//        // distance spinner model
//        AbstractSpinnerModel spinnerNumberModelDistance = new SpinnerNumberModel(5.5, 0.5, 50.0, 0.5);
//        jSDistance.setModel(spinnerNumberModelDistance);
//        //jSDistance.setEditor(menuBar);

        //
        // Add logger panel to MsgSender
        //
        RSSI_graphApp.getApplication().getMsgSender().setConsolePanel(jPanelLogger1);
        RSSI_graphApp.getApplication().getNodeDiscovery().setLogWindow(jPanelLogger1);

        // mnemonics to JTabbedPane
        this.jTabbedPaneMain.setMnemonicAt(0, KeyEvent.VK_R);
        this.jTabbedPaneMain.setMnemonicAt(1, KeyEvent.VK_L);
        this.jTabbedPaneMain.setMnemonicAt(2, KeyEvent.VK_C);

        this.jTabbedPane_R2D.setMnemonicAt(0, KeyEvent.VK_W);
        this.jTabbedPane_R2D.setMnemonicAt(1, KeyEvent.VK_O);

        // exit on close
        this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Set busy icon in status bar
     * @param busy
     */
    public void setBusy(boolean busy){
        if (busy == true) {
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }

        } else {
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
        }
    }

    /**
     * Sets status bar message
     * @param s
     */
    public void setStatusMessage(String s){
            String text = (String)(s);
            statusMessageLabel.setText((text == null) ? "" : text);
            messageTimer.restart();
    }

    /**
     * Helper method, bring window to front if is not.
     * Helps to alert user on some event.
     *
     * @throws InterruptedException
     */
    public void toFront() throws InterruptedException{
        if (!this.getFrame().isFocused() && !this.getFrame().isFocusOwner()){
            this.getFrame().setVisible(false);
            this.getFrame().setVisible(true);
        }

        // reset aplways on top
        this.getFrame().setAlwaysOnTop(true);
        this.getFrame().setAlwaysOnTop(false);

        // bring to front
        this.getFrame().toFront();
    }

    public javax.swing.JLabel getCommandLabel(){
        return null;
    }

    public javax.swing.JLabel getProgressLabel(){
        return null;
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = RSSI_graphApp.getApplication().getMainFrame();
            aboutBox = new RSSI_graphAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        RSSI_graphApp.getApplication().show(aboutBox);
    }
    
    /**
     * Return wanted report protocol
     * 
     * @return
     */
    public int getCommandReportProtocol(){
        // @deprecated
        //if (this.jRadio_comm_report_tiny.isSelected()) return MessageTypes.REPORTING_TINY;
        if (this.jRadio_comm_report_medium.isSelected()) return MessageTypes.REPORTING_MEDIUM;
        if (this.jRadio_comm_report_mass.isSelected()) return MessageTypes.REPORTING_MASS;
        return MessageTypes.REPORTING_MEDIUM;
    }

    /**
     * Highlight parameter when needed
     * in rssi2distance (distance, angle, step)
     * @param highlight
     */
    public void setR2DParameterHighlight(boolean highlight){
        final Color normColor = new Color(255, 255, 255);
        final Color highColor = new Color(100, 100, 100);
        this.jText_R2DDistance.setOpaque(true);
        this.jText_R2DDistance.setBackground(highlight ? highColor : normColor);

        final Color highColor2 = new Color(254,136,18);
        final Color normColor2 = new Color(237,236,235);
        this.jMarkerParameter.setOpaque(true);
        this.jMarkerParameter.setBackground(highlight ? highColor2 : normColor2);
        this.jMarkerParameter.setText(highlight ? "#" : " ");
    }

    /**
     * Mark packet counter for this round
     * @param highlight
     */
    public void setR2DPacketsHighlight(boolean highlight){
        final Color highColor2 = new Color(254,136,18);
        final Color normColor2 = new Color(237,236,235);
        this.jMarkerPackets.setOpaque(true);
        this.jMarkerPackets.setBackground(highlight ? highColor2 : normColor2);
        this.jMarkerPackets.setText(highlight ? "#" : " ");
    }

    public void setR2DStartHighlight(boolean highlight){
        final Color highColor2 = new Color(254,136,18);
        final Color normColor2 = new Color(237,236,235);
        this.jMarkerStart.setOpaque(true);
        this.jMarkerStart.setBackground(highlight ? highColor2 : normColor2);
        this.jMarkerStart.setText(highlight ? "#" : " ");
    }

    public void setR2DSaveNextHighlight(boolean highlight){
        final Color highColor2 = new Color(254,136,18);
        final Color normColor2 = new Color(237,236,235);
        this.jMarkerSaveNext.setOpaque(true);
        this.jMarkerSaveNext.setBackground(highlight ? highColor2 : normColor2);
        this.jMarkerSaveNext.setText(highlight ? "#" : " ");
    }

    public void setR2DAddToGraph(boolean highlight){
        final Color highColor2 = new Color(254,136,18);
        final Color normColor2 = new Color(237,236,235);
        this.jMarkerGraph.setOpaque(true);
        this.jMarkerGraph.setBackground(highlight ? highColor2 : normColor2);
        this.jMarkerGraph.setText(highlight ? "#" : " ");
    }

    /**
     * =========================================================================
     *
     * GETTERS+SETTERS
     *
     * =========================================================================
     */
    public JButton getjButton_drop() {
        return jButton_drop;
    }

    public JButton getjButton_save() {
        return jButton_save;
    }

    public JPanel getjPanelR2D() {
        return jPanelR2D;
    }

    public JTextField getjText_R2DDistance() {
        return jText_R2DDistance;
    }

    public JTextArea getjText_anotate() {
        return jText_anotate;
    }

    public JTextField getjText_delay() {
        return jText_delay;
    }

    public JTextField getjText_packets() {
        return jText_packets;
    }

    public JTextField getjText_packetsReceived() {
        return jText_packetsReceived;
    }

    public JTextField getjText_roundId() {
        return jText_roundId;
    }

    public JTextField getjText_static() {
        return jText_static;
    }

    public JToggleButton getjToggle_R2D_ON() {
        return jToggle_R2D_ON;
    }

    public JButton getjButton_R2D_start() {
        return jButton_R2D_start;
    }

    public JTextField getjText_R2D_testNo() {
        return jText_R2D_testNo;
    }

    public JButton getjButton_comm_sendRT() {
        return jButton_comm_sendRT;
    }

    public JButton getjButton_comm_sendRTValue() {
        return jButton_comm_sendRTValue;
    }

    public JButton getjButton_comm_sendTinyReports() {
        return jButton_comm_sendTinyReports;
    }

    public JCheckBox getjCheck_com_randomizedThresholding() {
        return jCheck_com_randomizedThresholding;
    }

    public JTextField getjText_comm_queueFlushThreshold() {
        return jText_comm_queueFlushThreshold;
    }

    public JToggleButton getjToggle_comm_on() {
        return jToggle_comm_on;
    }

    public JButton getjButton_comm_sendRepStatus() {
        return jButton_comm_sendRepStatus;
    }

    public JButton getjButton_comm_sendTXpower() {
        return jButton_comm_sendTXpower;
    }

    public JCheckBox getjCheck_comm_reportingStatus() {
        return jCheck_comm_reportingStatus;
    }

    public JSlider getjSlider_comm_txpower() {
        return jSlider_comm_txpower;
    }

    public JTextField getjText_comm_reportGap() {
        return jText_comm_reportGap;
    }

    public JProgressBar getjProgress_R2D_progress() {
        return jProgress_R2D_progress;
    }
    
    public JPanel getjPanelComm() {
        return jPanelComm;
    }

    public JPanelLogger getjPanelLogger1() {
        return jPanelLogger1;
    }

    public JCheckBox getjCheck_R2D_resetBeforeRound() {
        return jCheck_R2D_resetBeforeRound;
    }

    public JPannelRSSI2DistanceChart getjPannelRSSI2DistanceChart1() {
        return jPannelRSSI2DistanceChart1;
    }

    public jPannelRSSI2DistanceDataLoader getjPannelRSSI2DistanceDataLoader2() {
        return jPannelRSSI2DistanceDataLoader2;
    }

    public JToggleButton getjToggle_R2D_Timer() {
        return jToggle_R2D_Timer;
    }

    public JPanelLocalizationMain getjPanelLocalizationMain1() {
        return jPanelLocalizationMain1;
    }

    public JPanelNodeSelector getjPanel_comm_NodeSelector() {
        return jPanel_comm_NodeSelector1;
    }

    public JPanelNodeRegisterMain getjPanelNodeRegisterMain1() {
        return jPanelNodeRegisterMain1;
    }

    public JPanelTXpowerSelector getjPanelTXpowerSelector1() {
        return jPanelTXpowerSelector1;
    }

    public JPanelNodeSelector getjPanel_comm_NodeSelector1() {
        return jPanel_comm_NodeSelector1;
    }

    public JComboBox getjCombo_R2D_mobile() {
        return jCombo_R2D_mobile;
    }

    public JTable getjTableReceiveList() {
        return jTableReceiveList;
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

        mainPanel = new javax.swing.JPanel();
        jTabbedPaneMain = new javax.swing.JTabbedPane();
        jPanelR2D = new javax.swing.JPanel();
        jTabbedPane_R2D = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jMarkerGraph = new javax.swing.JLabel();
        jMarkerSaveNext = new javax.swing.JLabel();
        jMarkerStart = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jButtonFinishTry = new javax.swing.JButton();
        jMarkerPackets = new javax.swing.JLabel();
        jButton_R2D_start = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jText_anotate = new javax.swing.JTextArea();
        jLabel13 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jText_packetsReceived = new javax.swing.JTextField();
        jButton_R2D_prepMed = new javax.swing.JButton();
        jText_roundId = new javax.swing.JTextField();
        jToggle_R2D_ON = new javax.swing.JToggleButton();
        jButton_R2D_MoveStep = new javax.swing.JButton();
        jToggle_R2D_Timer = new javax.swing.JToggleButton();
        jButton_R2D_prepMass = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jButton_R2D_AddToGraph = new javax.swing.JButton();
        jButton_save = new javax.swing.JButton();
        jLabel39 = new javax.swing.JLabel();
        jButton_drop = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableReceiveList = new javax.swing.JTable();
        jPanel13 = new javax.swing.JPanel();
        jMarkerParameter = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jCheck_R2D_resetBeforeRound = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jCombo_R2D_Parameter = new javax.swing.JComboBox();
        jText_R2DDistance = new javax.swing.JTextField();
        jText_delay = new javax.swing.JTextField();
        jText_R2D_testNo = new javax.swing.JTextField();
        jText_static = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jText_packets = new javax.swing.JTextField();
        jProgress_R2D_progress = new javax.swing.JProgressBar();
        jPanelTXpowerSelector1 = new rssi_graph.localization.JPanelTXpowerSelector();
        jCombo_R2D_mobile = new javax.swing.JComboBox();
        jPannelRSSI2DistanceDataLoader2 = new rssi_graph.rssi.jPannelRSSI2DistanceDataLoader();
        jPannelRSSI2DistanceChart1 = new rssi_graph.rssi.chart.JPannelRSSI2DistanceChart();
        jPanelLocalizationMain1 = new rssi_graph.localization.JPanelLocalizationMain();
        jPanelComm = new javax.swing.JPanel();
        jToggle_comm_on = new javax.swing.JToggleButton();
        jButton_comm_sendAbort = new javax.swing.JButton();
        jButton_comm_sendReset = new javax.swing.JButton();
        jPanel_comm_NodeSelector1 = new rssi_localization.JPanelNodeSelector();
        jButton_comm_discover = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jCheck_com_randomizedThresholding = new javax.swing.JCheckBox();
        jButton_comm_sendRT = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jText_comm_queueFlushThreshold = new javax.swing.JTextField();
        jButton_comm_sendRTValue = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton_comm_sendRepStatus = new javax.swing.JButton();
        jCheck_comm_reportingStatus = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jSlider_comm_txpower = new javax.swing.JSlider();
        jButton_comm_sendTXpower = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jRadio_comm_report_mass = new javax.swing.JRadioButton();
        jRadio_comm_report_medium = new javax.swing.JRadioButton();
        jButton_comm_sendTinyReports = new javax.swing.JButton();
        jCheck_comm_autoNodeDiscovery = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jText_comm_reportGap = new javax.swing.JTextField();
        jButton_comm_setReportGap = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jButton_comm_sensorReading = new javax.swing.JButton();
        jRadio_com_temperature = new javax.swing.JRadioButton();
        jRadio_com_tempHumidity = new javax.swing.JRadioButton();
        jRadio_com_light = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jRadio_com_oneTime = new javax.swing.JRadioButton();
        jRadio_com_SensorPeriodic = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jField_com_period = new javax.swing.JFormattedTextField();
        jRadio_com_humidity = new javax.swing.JRadioButton();
        jPanelNodeRegisterMain1 = new rssi_graph.nodeRegister.JPanelNodeRegisterMain();
        jPanelLogger1 = new rssi_localization.JPanelLogger();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup_reportProtocol = new javax.swing.ButtonGroup();
        jPanel7 = new javax.swing.JPanel();
        buttonGroup_rssi2d_txpower = new javax.swing.ButtonGroup();
        buttonGroup_sensors = new javax.swing.ButtonGroup();
        buttonGroup_sensorSamplingFrequency = new javax.swing.ButtonGroup();

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setMinimumSize(new java.awt.Dimension(400, 400));
        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPaneMain.setName("jTabbedPaneMain"); // NOI18N

        jPanelR2D.setName("jPanelR2D"); // NOI18N

        jTabbedPane_R2D.setName("jTabbedPane_R2D"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel6.setName("jPanel6"); // NOI18N

        jMarkerGraph.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jMarkerGraph.setName("jMarkerGraph"); // NOI18N
        jMarkerGraph.setOpaque(true);

        jMarkerSaveNext.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jMarkerSaveNext.setName("jMarkerSaveNext"); // NOI18N
        jMarkerSaveNext.setOpaque(true);

        jMarkerStart.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jMarkerStart.setName("jMarkerStart"); // NOI18N
        jMarkerStart.setOpaque(true);

        jPanel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel12.setName("jPanel12"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getResourceMap(RSSI_graphView.class);
        jButtonFinishTry.setText(resourceMap.getString("jButtonFinishTry.text")); // NOI18N
        jButtonFinishTry.setToolTipText(resourceMap.getString("jButtonFinishTry.toolTipText")); // NOI18N
        jButtonFinishTry.setActionCommand(resourceMap.getString("jButtonFinishTry.actionCommand")); // NOI18N
        jButtonFinishTry.setName("jButtonFinishTry"); // NOI18N

        jMarkerPackets.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jMarkerPackets.setText(resourceMap.getString("jMarkerPackets.text")); // NOI18N
        jMarkerPackets.setName("jMarkerPackets"); // NOI18N
        jMarkerPackets.setOpaque(true);

        jButton_R2D_start.setMnemonic('S');
        jButton_R2D_start.setText(resourceMap.getString("jButton_R2D_start.text")); // NOI18N
        jButton_R2D_start.setName("jButton_R2D_start"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jText_anotate.setColumns(20);
        jText_anotate.setRows(5);
        jText_anotate.setFocusAccelerator('A');
        jText_anotate.setName("jText_anotate"); // NOI18N
        jScrollPane2.setViewportView(jText_anotate);

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jText_packetsReceived.setText(resourceMap.getString("jText_packetsReceived.text")); // NOI18N
        jText_packetsReceived.setName("jText_packetsReceived"); // NOI18N

        jButton_R2D_prepMed.setMnemonic('e');
        jButton_R2D_prepMed.setText(resourceMap.getString("jButton_R2D_prepMed.text")); // NOI18N
        jButton_R2D_prepMed.setActionCommand(resourceMap.getString("jButton_R2D_prepMed.actionCommand")); // NOI18N
        jButton_R2D_prepMed.setName("jButton_R2D_prepMed"); // NOI18N

        jText_roundId.setText(resourceMap.getString("jText_roundId.text")); // NOI18N
        jText_roundId.setName("jText_roundId"); // NOI18N

        jToggle_R2D_ON.setSelected(true);
        jToggle_R2D_ON.setText(resourceMap.getString("jToggle_R2D_ON.text")); // NOI18N
        jToggle_R2D_ON.setActionCommand(resourceMap.getString("jToggle_R2D_ON.actionCommand")); // NOI18N
        jToggle_R2D_ON.setName("jToggle_R2D_ON"); // NOI18N

        jButton_R2D_MoveStep.setText(resourceMap.getString("jButton_R2D_MoveStep.text")); // NOI18N
        jButton_R2D_MoveStep.setToolTipText(resourceMap.getString("jButton_R2D_MoveStep.toolTipText")); // NOI18N
        jButton_R2D_MoveStep.setActionCommand(resourceMap.getString("jButton_R2D_MoveStep.actionCommand")); // NOI18N
        jButton_R2D_MoveStep.setName("jButton_R2D_MoveStep"); // NOI18N

        jToggle_R2D_Timer.setText(resourceMap.getString("jToggle_R2D_Timer.text")); // NOI18N
        jToggle_R2D_Timer.setToolTipText(resourceMap.getString("jToggle_R2D_Timer.toolTipText")); // NOI18N
        jToggle_R2D_Timer.setActionCommand(resourceMap.getString("jToggle_R2D_Timer.actionCommand")); // NOI18N
        jToggle_R2D_Timer.setEnabled(false);
        jToggle_R2D_Timer.setName("jToggle_R2D_Timer"); // NOI18N

        jButton_R2D_prepMass.setMnemonic('m');
        jButton_R2D_prepMass.setText(resourceMap.getString("jButton_R2D_prepMass.text")); // NOI18N
        jButton_R2D_prepMass.setActionCommand(resourceMap.getString("jButton_R2D_prepMass.actionCommand")); // NOI18N
        jButton_R2D_prepMass.setName("jButton_R2D_prepMass"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jButton_R2D_AddToGraph.setText(resourceMap.getString("jButton_R2D_AddToGraph.text")); // NOI18N
        jButton_R2D_AddToGraph.setToolTipText(resourceMap.getString("jButton_R2D_AddToGraph.toolTipText")); // NOI18N
        jButton_R2D_AddToGraph.setActionCommand(resourceMap.getString("jButton_R2D_AddToGraph.actionCommand")); // NOI18N
        jButton_R2D_AddToGraph.setName("jButton_R2D_AddToGraph"); // NOI18N

        jButton_save.setMnemonic('N');
        jButton_save.setText(resourceMap.getString("jButton_save.text")); // NOI18N
        jButton_save.setActionCommand(resourceMap.getString("jButton_save.actionCommand")); // NOI18N
        jButton_save.setName("jButton_save"); // NOI18N

        jLabel39.setText(resourceMap.getString("jLabel39.text")); // NOI18N
        jLabel39.setName("jLabel39"); // NOI18N

        jButton_drop.setMnemonic('D');
        jButton_drop.setText(resourceMap.getString("jButton_drop.text")); // NOI18N
        jButton_drop.setToolTipText(resourceMap.getString("jButton_drop.toolTipText")); // NOI18N
        jButton_drop.setActionCommand(resourceMap.getString("jButton_drop.actionCommand")); // NOI18N
        jButton_drop.setName("jButton_drop"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableReceiveList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Use", "Node", "Packets"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableReceiveList.setName("jTableReceiveList"); // NOI18N
        jScrollPane1.setViewportView(jTableReceiveList);
        jTableReceiveList.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableReceiveList.columnModel.title0")); // NOI18N
        jTableReceiveList.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableReceiveList.columnModel.title1")); // NOI18N
        jTableReceiveList.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableReceiveList.columnModel.title2")); // NOI18N

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel12)
                        .addGap(63, 63, 63)
                        .addComponent(jText_roundId, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel11)
                        .addGap(12, 12, 12)
                        .addComponent(jText_packetsReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jMarkerPackets, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel13))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel39)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_R2D_start, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton_R2D_prepMass)
                            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButtonFinishTry, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel12Layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(jButton_R2D_AddToGraph, 0, 0, Short.MAX_VALUE))
                                    .addComponent(jButton_drop, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton_save, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggle_R2D_ON, 0, 0, Short.MAX_VALUE)
                            .addComponent(jButton_R2D_prepMed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggle_R2D_Timer, 0, 0, Short.MAX_VALUE)
                            .addComponent(jButton_R2D_MoveStep, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel12))
                    .addComponent(jText_roundId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel11))
                    .addComponent(jText_packetsReceived, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jMarkerPackets)))
                .addGap(12, 12, 12)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel39)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton_R2D_start)
                                    .addComponent(jButton_save))
                                .addGap(7, 7, 7)
                                .addComponent(jButtonFinishTry))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(jToggle_R2D_Timer)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_R2D_AddToGraph)
                            .addComponent(jButton_R2D_MoveStep))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_drop)
                            .addComponent(jToggle_R2D_ON))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_R2D_prepMass)
                            .addComponent(jButton_R2D_prepMed))))
                .addGap(27, 27, 27))
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel13.setName("jPanel13"); // NOI18N

        jMarkerParameter.setBackground(resourceMap.getColor("jMarkerParameter.background")); // NOI18N
        jMarkerParameter.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jMarkerParameter.setText(resourceMap.getString("jMarkerParameter.text")); // NOI18N
        jMarkerParameter.setName("jMarkerParameter"); // NOI18N
        jMarkerParameter.setOpaque(true);

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jCheck_R2D_resetBeforeRound.setMnemonic('t');
        jCheck_R2D_resetBeforeRound.setSelected(true);
        jCheck_R2D_resetBeforeRound.setText(resourceMap.getString("jCheck_R2D_resetBeforeRound.text")); // NOI18N
        jCheck_R2D_resetBeforeRound.setActionCommand(resourceMap.getString("jCheck_R2D_resetBeforeRound.actionCommand")); // NOI18N
        jCheck_R2D_resetBeforeRound.setName("jCheck_R2D_resetBeforeRound"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setEnabled(false);
        jLabel7.setName("jLabel7"); // NOI18N

        jCombo_R2D_Parameter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Distance [dm]", "Angle [deg]", "Step" }));
        jCombo_R2D_Parameter.setName("jCombo_R2D_Parameter"); // NOI18N

        jText_R2DDistance.setBackground(resourceMap.getColor("jText_R2DDistance.background")); // NOI18N
        jText_R2DDistance.setText(resourceMap.getString("jText_R2DDistance.text")); // NOI18N
        jText_R2DDistance.setFocusAccelerator('I');
        jText_R2DDistance.setFocusCycleRoot(true);
        jText_R2DDistance.setName("jText_R2DDistance"); // NOI18N
        jText_R2DDistance.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jText_R2DDistanceFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jText_R2DDistanceFocusLost(evt);
            }
        });

        jText_delay.setText(resourceMap.getString("jText_delay.text")); // NOI18N
        jText_delay.setName("jText_delay"); // NOI18N

        jText_R2D_testNo.setText(resourceMap.getString("jText_R2D_testNo.text")); // NOI18N
        jText_R2D_testNo.setName("jText_R2D_testNo"); // NOI18N

        jText_static.setText(resourceMap.getString("jText_static.text")); // NOI18N
        jText_static.setEnabled(false);
        jText_static.setName("jText_static"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel35.setText(resourceMap.getString("jLabel35.text")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N

        jText_packets.setText(resourceMap.getString("jText_packets.text")); // NOI18N
        jText_packets.setName("jText_packets"); // NOI18N

        jProgress_R2D_progress.setName("jProgress_R2D_progress"); // NOI18N

        jPanelTXpowerSelector1.setName("jPanelTXpowerSelector1"); // NOI18N

        jCombo_R2D_mobile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jCombo_R2D_mobile.setActionCommand(resourceMap.getString("jCombo_R2D_mobile.actionCommand")); // NOI18N
        jCombo_R2D_mobile.setName("jCombo_R2D_mobile"); // NOI18N
        jCombo_R2D_mobile.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCombo_R2D_mobileItemStateChanged(evt);
            }
        });
        jCombo_R2D_mobile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCombo_R2D_mobileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel35)))
                            .addComponent(jCombo_R2D_Parameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCombo_R2D_mobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jText_packets)
                                .addComponent(jText_static)
                                .addComponent(jText_delay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jText_R2DDistance, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jText_R2D_testNo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1)
                                .addComponent(jMarkerParameter, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jProgress_R2D_progress, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelTXpowerSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheck_R2D_resetBeforeRound, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jPanel13Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jText_R2DDistance, jText_R2D_testNo, jText_delay, jText_packets, jText_static});

        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jText_static, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jCombo_R2D_mobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_packets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_delay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jText_R2DDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCombo_R2D_Parameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMarkerParameter))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(jText_R2D_testNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelTXpowerSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jCheck_R2D_resetBeforeRound)
                .addGap(14, 14, 14)
                .addComponent(jProgress_R2D_progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(462, 462, 462)
                        .addComponent(jMarkerStart, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jMarkerSaveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                    .addComponent(jMarkerSaveNext, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jMarkerStart, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane_R2D.addTab("New test", jPanel6);

        jPannelRSSI2DistanceDataLoader2.setName("jPannelRSSI2DistanceDataLoader2"); // NOI18N
        jTabbedPane_R2D.addTab(resourceMap.getString("jPannelRSSI2DistanceDataLoader2.TabConstraints.tabTitle"), jPannelRSSI2DistanceDataLoader2); // NOI18N

        jPannelRSSI2DistanceChart1.setName("jPannelRSSI2DistanceChart1"); // NOI18N

        javax.swing.GroupLayout jPanelR2DLayout = new javax.swing.GroupLayout(jPanelR2D);
        jPanelR2D.setLayout(jPanelR2DLayout);
        jPanelR2DLayout.setHorizontalGroup(
            jPanelR2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelR2DLayout.createSequentialGroup()
                .addComponent(jPannelRSSI2DistanceChart1, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane_R2D, javax.swing.GroupLayout.PREFERRED_SIZE, 715, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelR2DLayout.setVerticalGroup(
            jPanelR2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelR2DLayout.createSequentialGroup()
                .addComponent(jPannelRSSI2DistanceChart1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelR2DLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jTabbedPane_R2D, javax.swing.GroupLayout.PREFERRED_SIZE, 516, Short.MAX_VALUE))
        );

        jTabbedPaneMain.addTab(resourceMap.getString("jPanelR2D.TabConstraints.tabTitle"), jPanelR2D); // NOI18N

        jPanelLocalizationMain1.setName("jPanelLocalizationMain1"); // NOI18N
        jTabbedPaneMain.addTab(resourceMap.getString("jPanelLocalizationMain1.TabConstraints.tabTitle"), jPanelLocalizationMain1); // NOI18N

        jPanelComm.setName("jPanelComm"); // NOI18N

        jToggle_comm_on.setSelected(true);
        jToggle_comm_on.setText(resourceMap.getString("jToggle_comm_on.text")); // NOI18N
        jToggle_comm_on.setActionCommand(resourceMap.getString("jToggle_comm_on.actionCommand")); // NOI18N
        jToggle_comm_on.setName("jToggle_comm_on"); // NOI18N

        jButton_comm_sendAbort.setMnemonic('A');
        jButton_comm_sendAbort.setText(resourceMap.getString("jButton_comm_sendAbort.text")); // NOI18N
        jButton_comm_sendAbort.setActionCommand(resourceMap.getString("jButton_comm_sendAbort.actionCommand")); // NOI18N
        jButton_comm_sendAbort.setName("jButton_comm_sendAbort"); // NOI18N

        jButton_comm_sendReset.setMnemonic('e');
        jButton_comm_sendReset.setText(resourceMap.getString("jButton_comm_sendReset.text")); // NOI18N
        jButton_comm_sendReset.setActionCommand(resourceMap.getString("jButton_comm_sendReset.actionCommand")); // NOI18N
        jButton_comm_sendReset.setName("jButton_comm_sendReset"); // NOI18N

        jPanel_comm_NodeSelector1.setName("jPanel_comm_NodeSelector1"); // NOI18N

        jButton_comm_discover.setMnemonic('D');
        jButton_comm_discover.setText(resourceMap.getString("jButton_comm_discover.text")); // NOI18N
        jButton_comm_discover.setActionCommand(resourceMap.getString("jButton_comm_discover.actionCommand")); // NOI18N
        jButton_comm_discover.setName("jButton_comm_discover"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jCheck_com_randomizedThresholding.setSelected(true);
        jCheck_com_randomizedThresholding.setText(resourceMap.getString("jCheck_com_randomizedThresholding.text")); // NOI18N
        jCheck_com_randomizedThresholding.setName("jCheck_com_randomizedThresholding"); // NOI18N

        jButton_comm_sendRT.setText(resourceMap.getString("jButton_comm_sendRT.text")); // NOI18N
        jButton_comm_sendRT.setActionCommand(resourceMap.getString("jButton_comm_sendRT.actionCommand")); // NOI18N
        jButton_comm_sendRT.setName("jButton_comm_sendRT"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton_comm_sendRT)
                    .addComponent(jCheck_com_randomizedThresholding))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheck_com_randomizedThresholding)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_comm_sendRT)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel36.setText(resourceMap.getString("jLabel36.text")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N

        jText_comm_queueFlushThreshold.setText(resourceMap.getString("jText_comm_queueFlushThreshold.text")); // NOI18N
        jText_comm_queueFlushThreshold.setName("jText_comm_queueFlushThreshold"); // NOI18N

        jButton_comm_sendRTValue.setText(resourceMap.getString("jButton_comm_sendRTValue.text")); // NOI18N
        jButton_comm_sendRTValue.setActionCommand(resourceMap.getString("jButton_comm_sendRTValue.actionCommand")); // NOI18N
        jButton_comm_sendRTValue.setName("jButton_comm_sendRTValue"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel36)
                .addGap(18, 18, 18)
                .addComponent(jText_comm_queueFlushThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(82, Short.MAX_VALUE)
                .addComponent(jButton_comm_sendRTValue)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jText_comm_queueFlushThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_comm_sendRTValue)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jButton_comm_sendRepStatus.setText(resourceMap.getString("jButton_comm_sendRepStatus.text")); // NOI18N
        jButton_comm_sendRepStatus.setActionCommand(resourceMap.getString("jButton_comm_sendRepStatus.actionCommand")); // NOI18N
        jButton_comm_sendRepStatus.setName("jButton_comm_sendRepStatus"); // NOI18N

        jCheck_comm_reportingStatus.setSelected(true);
        jCheck_comm_reportingStatus.setText(resourceMap.getString("jCheck_comm_reportingStatus.text")); // NOI18N
        jCheck_comm_reportingStatus.setName("jCheck_comm_reportingStatus"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheck_comm_reportingStatus)
                .addContainerGap(72, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(82, Short.MAX_VALUE)
                .addComponent(jButton_comm_sendRepStatus)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheck_comm_reportingStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_comm_sendRepStatus)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jSlider_comm_txpower.setMaximum(8);
        jSlider_comm_txpower.setMinimum(1);
        jSlider_comm_txpower.setName("jSlider_comm_txpower"); // NOI18N

        jButton_comm_sendTXpower.setText(resourceMap.getString("jButton_comm_sendTXpower.text")); // NOI18N
        jButton_comm_sendTXpower.setActionCommand(resourceMap.getString("jButton_comm_sendTXpower.actionCommand")); // NOI18N
        jButton_comm_sendTXpower.setName("jButton_comm_sendTXpower"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSlider_comm_txpower, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(jButton_comm_sendTXpower, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jSlider_comm_txpower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_comm_sendTXpower)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        buttonGroup_reportProtocol.add(jRadio_comm_report_mass);
        jRadio_comm_report_mass.setText(resourceMap.getString("jRadio_comm_report_mass.text")); // NOI18N
        jRadio_comm_report_mass.setName("jRadio_comm_report_mass"); // NOI18N
        jRadio_comm_report_mass.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadio_comm_report_massItemStateChanged(evt);
            }
        });

        buttonGroup_reportProtocol.add(jRadio_comm_report_medium);
        jRadio_comm_report_medium.setSelected(true);
        jRadio_comm_report_medium.setText(resourceMap.getString("jRadio_comm_report_medium.text")); // NOI18N
        jRadio_comm_report_medium.setName("jRadio_comm_report_medium"); // NOI18N
        jRadio_comm_report_medium.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadio_comm_report_mediumItemStateChanged(evt);
            }
        });

        jButton_comm_sendTinyReports.setText(resourceMap.getString("jButton_comm_sendTinyReports.text")); // NOI18N
        jButton_comm_sendTinyReports.setActionCommand(resourceMap.getString("jButton_comm_sendTinyReports.actionCommand")); // NOI18N
        jButton_comm_sendTinyReports.setName("jButton_comm_sendTinyReports"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(82, Short.MAX_VALUE)
                .addComponent(jButton_comm_sendTinyReports)
                .addContainerGap())
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadio_comm_report_medium)
                .addContainerGap(123, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadio_comm_report_mass)
                .addContainerGap(91, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jRadio_comm_report_medium)
                .addGap(4, 4, 4)
                .addComponent(jRadio_comm_report_mass)
                .addGap(18, 18, 18)
                .addComponent(jButton_comm_sendTinyReports)
                .addContainerGap())
        );

        jCheck_comm_autoNodeDiscovery.setMnemonic('t');
        jCheck_comm_autoNodeDiscovery.setSelected(true);
        jCheck_comm_autoNodeDiscovery.setText(resourceMap.getString("jCheck_comm_autoNodeDiscovery.text")); // NOI18N
        jCheck_comm_autoNodeDiscovery.setActionCommand(resourceMap.getString("jCheck_comm_autoNodeDiscovery.actionCommand")); // NOI18N
        jCheck_comm_autoNodeDiscovery.setName("jCheck_comm_autoNodeDiscovery"); // NOI18N

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel10.border.title"))); // NOI18N
        jPanel10.setToolTipText(resourceMap.getString("jPanel10.toolTipText")); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N

        jText_comm_reportGap.setText(resourceMap.getString("jText_comm_reportGap.text")); // NOI18N
        jText_comm_reportGap.setName("jText_comm_reportGap"); // NOI18N

        jButton_comm_setReportGap.setText(resourceMap.getString("jButton_comm_setReportGap.text")); // NOI18N
        jButton_comm_setReportGap.setActionCommand(resourceMap.getString("jButton_comm_setReportGap.actionCommand")); // NOI18N
        jButton_comm_setReportGap.setName("jButton_comm_setReportGap"); // NOI18N
        jButton_comm_setReportGap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_comm_setReportGapActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(49, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jText_comm_reportGap, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_comm_setReportGap, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jText_comm_reportGap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_comm_setReportGap)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel11.border.title"))); // NOI18N
        jPanel11.setToolTipText(resourceMap.getString("jPanel11.toolTipText")); // NOI18N
        jPanel11.setName("jPanel11"); // NOI18N

        jButton_comm_sensorReading.setText(resourceMap.getString("jButton_comm_sensorReading.text")); // NOI18N
        jButton_comm_sensorReading.setActionCommand(resourceMap.getString("jButton_comm_sensorReading.actionCommand")); // NOI18N
        jButton_comm_sensorReading.setName("jButton_comm_sensorReading"); // NOI18N
        jButton_comm_sensorReading.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_comm_sensorReadingActionPerformed(evt);
            }
        });

        buttonGroup_sensors.add(jRadio_com_temperature);
        jRadio_com_temperature.setSelected(true);
        jRadio_com_temperature.setText(resourceMap.getString("jRadio_com_temperature.text")); // NOI18N
        jRadio_com_temperature.setActionCommand(resourceMap.getString("jRadio_com_temperature.actionCommand")); // NOI18N
        jRadio_com_temperature.setName("jRadio_com_temperature"); // NOI18N

        buttonGroup_sensors.add(jRadio_com_tempHumidity);
        jRadio_com_tempHumidity.setText(resourceMap.getString("jRadio_com_tempHumidity.text")); // NOI18N
        jRadio_com_tempHumidity.setToolTipText(resourceMap.getString("jRadio_com_tempHumidity.toolTipText")); // NOI18N
        jRadio_com_tempHumidity.setActionCommand(resourceMap.getString("jRadio_com_tempHumidity.actionCommand")); // NOI18N
        jRadio_com_tempHumidity.setName("jRadio_com_tempHumidity"); // NOI18N

        buttonGroup_sensors.add(jRadio_com_light);
        jRadio_com_light.setText(resourceMap.getString("jRadio_com_light.text")); // NOI18N
        jRadio_com_light.setActionCommand(resourceMap.getString("jRadio_com_light.actionCommand")); // NOI18N
        jRadio_com_light.setName("jRadio_com_light"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        buttonGroup_sensorSamplingFrequency.add(jRadio_com_oneTime);
        jRadio_com_oneTime.setSelected(true);
        jRadio_com_oneTime.setText(resourceMap.getString("jRadio_com_oneTime.text")); // NOI18N
        jRadio_com_oneTime.setName("jRadio_com_oneTime"); // NOI18N

        buttonGroup_sensorSamplingFrequency.add(jRadio_com_SensorPeriodic);
        jRadio_com_SensorPeriodic.setText(resourceMap.getString("jRadio_com_SensorPeriodic.text")); // NOI18N
        jRadio_com_SensorPeriodic.setName("jRadio_com_SensorPeriodic"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jField_com_period.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jField_com_period.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jField_com_period.setText(resourceMap.getString("jField_com_period.text")); // NOI18N
        jField_com_period.setName("jField_com_period"); // NOI18N

        buttonGroup_sensors.add(jRadio_com_humidity);
        jRadio_com_humidity.setText(resourceMap.getString("jRadio_com_humidity.text")); // NOI18N
        jRadio_com_humidity.setToolTipText(resourceMap.getString("jRadio_com_humidity.toolTipText")); // NOI18N
        jRadio_com_humidity.setActionCommand(resourceMap.getString("jRadio_com_humidity.actionCommand")); // NOI18N
        jRadio_com_humidity.setName("jRadio_com_humidity"); // NOI18N

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadio_com_temperature)
                            .addComponent(jRadio_com_tempHumidity)
                            .addComponent(jRadio_com_humidity)
                            .addComponent(jRadio_com_light)))
                    .addComponent(jLabel1)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadio_com_oneTime)
                            .addComponent(jRadio_com_SensorPeriodic)
                            .addGroup(jPanel11Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(3, 3, 3)
                                .addComponent(jField_com_period, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel2)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jButton_comm_sensorReading)))
                .addGap(8, 8, 8))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadio_com_temperature)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadio_com_tempHumidity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadio_com_humidity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadio_com_light)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadio_com_oneTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadio_com_SensorPeriodic)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jField_com_period, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_comm_sensorReading)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelCommLayout = new javax.swing.GroupLayout(jPanelComm);
        jPanelComm.setLayout(jPanelCommLayout);
        jPanelCommLayout.setHorizontalGroup(
            jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCommLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel_comm_NodeSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCommLayout.createSequentialGroup()
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheck_comm_autoNodeDiscovery)
                            .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jButton_comm_discover, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton_comm_sendReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton_comm_sendAbort, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelCommLayout.createSequentialGroup()
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggle_comm_on, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(600, 600, 600))
        );

        jPanelCommLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel1, jPanel2, jPanel3, jPanel4, jPanel8});

        jPanelCommLayout.setVerticalGroup(
            jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCommLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelCommLayout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelCommLayout.createSequentialGroup()
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelCommLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jButton_comm_sendAbort, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_comm_sendReset, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_comm_discover, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                                .addComponent(jCheck_comm_autoNodeDiscovery)
                                .addGap(10, 10, 10))
                            .addGroup(jPanelCommLayout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelCommLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelCommLayout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelCommLayout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggle_comm_on, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(11, 11, 11))
                    .addComponent(jPanel_comm_NodeSelector1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelCommLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton_comm_sendAbort, jButton_comm_sendReset});

        jPanel11.getAccessibleContext().setAccessibleDescription(resourceMap.getString("jPanel11.AccessibleContext.accessibleDescription")); // NOI18N

        jTabbedPaneMain.addTab(resourceMap.getString("jPanelComm.TabConstraints.tabTitle"), jPanelComm); // NOI18N

        jPanelNodeRegisterMain1.setName("jPanelNodeRegisterMain1"); // NOI18N
        jTabbedPaneMain.addTab(resourceMap.getString("jPanelNodeRegisterMain1.TabConstraints.tabTitle"), jPanelNodeRegisterMain1); // NOI18N

        jPanelLogger1.setName("jPanelLogger1"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jPanelLogger1, javax.swing.GroupLayout.DEFAULT_SIZE, 1418, Short.MAX_VALUE))
                    .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1430, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jTabbedPaneMain, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelLogger1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(rssi_localization.RSSI_graphApp.class).getContext().getActionMap(RSSI_graphView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1420, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1396, Short.MAX_VALUE)
                .addGap(24, 24, 24))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusPanelSeparator, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(statusMessageLabel)
                            .addComponent(statusAnimationLabel))))
                .addContainerGap())
        );

        jPanel7.setName("jPanel7"); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // exit on click
    private void jText_R2DDistanceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jText_R2DDistanceFocusGained
        // TODO add your handling code here:
        this.setR2DParameterHighlight(true);
    }//GEN-LAST:event_jText_R2DDistanceFocusGained

    private void jText_R2DDistanceFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jText_R2DDistanceFocusLost
        // TODO add your handling code here:
        this.setR2DParameterHighlight(false);
    }//GEN-LAST:event_jText_R2DDistanceFocusLost

    private void jButton_comm_setReportGapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_comm_setReportGapActionPerformed
        // TODO add your handling code here:
        RSSI_graphApp.getApplication().getWorker(2).actionPerformed(evt);
    }//GEN-LAST:event_jButton_comm_setReportGapActionPerformed

    private void jButton_comm_sensorReadingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_comm_sensorReadingActionPerformed
        // TODO add your handling code here:
        // get sensor type
        int sensorType = 1;
        for (Enumeration e=this.buttonGroup_sensors.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == this.buttonGroup_sensors.getSelection()) {
                sensorType = Integer.parseInt(b.getActionCommand());
            }
        }

        // get type of request (periodical/one time)
        boolean isPeriodical = this.jRadio_com_SensorPeriodic.isSelected();

        // get delay
        int periodDelay = Integer.parseInt(this.jField_com_period.getText());
        
        // call worker
        WorkerCommands commands = (WorkerCommands) RSSI_graphApp.getApplication().getWorker(2);
        commands.sendSensorReadingRequest(sensorType, isPeriodical, periodDelay);
    }//GEN-LAST:event_jButton_comm_sensorReadingActionPerformed

    /**
     * Hide some elements not necessary for this protocol
     * @param evt 
     */
    private void jRadio_comm_report_massItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadio_comm_report_massItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadio_comm_report_massItemStateChanged

    /**
     * Hide some elements not necessary for this protocol 
     * @param evt 
     */
    private void jRadio_comm_report_mediumItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadio_comm_report_mediumItemStateChanged
        // TODO add your handling code here:
        if (this.jRadio_comm_report_medium.isSelected()){
            // disable gap parameters
            this.jText_comm_reportGap.setEnabled(false);
            this.jButton_comm_setReportGap.setEnabled(false);
        } else {
            this.jText_comm_reportGap.setEnabled(true);
            this.jButton_comm_setReportGap.setEnabled(true);
        }
    }//GEN-LAST:event_jRadio_comm_report_mediumItemStateChanged
//
    private void jCombo_R2D_mobileItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCombo_R2D_mobileItemStateChanged
        // TODO add your handling code here:
//        final ItemListener a = RSSI_graphApp.getApplication().getWorker(1);
//        a.itemStateChanged(evt);
    }//GEN-LAST:event_jCombo_R2D_mobileItemStateChanged

    private void jCombo_R2D_mobileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCombo_R2D_mobileActionPerformed
        // TODO add your handling code here:z
        final ActionListener a = RSSI_graphApp.getApplication().getWorker(1);
        a.actionPerformed(evt);
    }//GEN-LAST:event_jCombo_R2D_mobileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup_reportProtocol;
    private javax.swing.ButtonGroup buttonGroup_rssi2d_txpower;
    private javax.swing.ButtonGroup buttonGroup_sensorSamplingFrequency;
    private javax.swing.ButtonGroup buttonGroup_sensors;
    private javax.swing.JButton jButtonFinishTry;
    private javax.swing.JButton jButton_R2D_AddToGraph;
    private javax.swing.JButton jButton_R2D_MoveStep;
    private javax.swing.JButton jButton_R2D_prepMass;
    private javax.swing.JButton jButton_R2D_prepMed;
    private javax.swing.JButton jButton_R2D_start;
    private javax.swing.JButton jButton_comm_discover;
    private javax.swing.JButton jButton_comm_sendAbort;
    private javax.swing.JButton jButton_comm_sendRT;
    private javax.swing.JButton jButton_comm_sendRTValue;
    private javax.swing.JButton jButton_comm_sendRepStatus;
    private javax.swing.JButton jButton_comm_sendReset;
    private javax.swing.JButton jButton_comm_sendTXpower;
    private javax.swing.JButton jButton_comm_sendTinyReports;
    private javax.swing.JButton jButton_comm_sensorReading;
    private javax.swing.JButton jButton_comm_setReportGap;
    private javax.swing.JButton jButton_drop;
    private javax.swing.JButton jButton_save;
    private javax.swing.JCheckBox jCheck_R2D_resetBeforeRound;
    private javax.swing.JCheckBox jCheck_com_randomizedThresholding;
    private javax.swing.JCheckBox jCheck_comm_autoNodeDiscovery;
    private javax.swing.JCheckBox jCheck_comm_reportingStatus;
    private javax.swing.JComboBox jCombo_R2D_Parameter;
    private javax.swing.JComboBox jCombo_R2D_mobile;
    private javax.swing.JFormattedTextField jField_com_period;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jMarkerGraph;
    private javax.swing.JLabel jMarkerPackets;
    private javax.swing.JLabel jMarkerParameter;
    private javax.swing.JLabel jMarkerSaveNext;
    private javax.swing.JLabel jMarkerStart;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanelComm;
    private rssi_graph.localization.JPanelLocalizationMain jPanelLocalizationMain1;
    private rssi_localization.JPanelLogger jPanelLogger1;
    private rssi_graph.nodeRegister.JPanelNodeRegisterMain jPanelNodeRegisterMain1;
    private javax.swing.JPanel jPanelR2D;
    private rssi_graph.localization.JPanelTXpowerSelector jPanelTXpowerSelector1;
    private rssi_localization.JPanelNodeSelector jPanel_comm_NodeSelector1;
    private rssi_graph.rssi.chart.JPannelRSSI2DistanceChart jPannelRSSI2DistanceChart1;
    private rssi_graph.rssi.jPannelRSSI2DistanceDataLoader jPannelRSSI2DistanceDataLoader2;
    private javax.swing.JProgressBar jProgress_R2D_progress;
    private javax.swing.JRadioButton jRadio_com_SensorPeriodic;
    private javax.swing.JRadioButton jRadio_com_humidity;
    private javax.swing.JRadioButton jRadio_com_light;
    private javax.swing.JRadioButton jRadio_com_oneTime;
    private javax.swing.JRadioButton jRadio_com_tempHumidity;
    private javax.swing.JRadioButton jRadio_com_temperature;
    private javax.swing.JRadioButton jRadio_comm_report_mass;
    private javax.swing.JRadioButton jRadio_comm_report_medium;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSlider jSlider_comm_txpower;
    private javax.swing.JTabbedPane jTabbedPaneMain;
    private javax.swing.JTabbedPane jTabbedPane_R2D;
    private javax.swing.JTable jTableReceiveList;
    private javax.swing.JTextField jText_R2DDistance;
    private javax.swing.JTextField jText_R2D_testNo;
    private javax.swing.JTextArea jText_anotate;
    private javax.swing.JTextField jText_comm_queueFlushThreshold;
    private javax.swing.JTextField jText_comm_reportGap;
    private javax.swing.JTextField jText_delay;
    private javax.swing.JTextField jText_packets;
    private javax.swing.JTextField jText_packetsReceived;
    private javax.swing.JTextField jText_roundId;
    private javax.swing.JTextField jText_static;
    private javax.swing.JToggleButton jToggle_R2D_ON;
    private javax.swing.JToggleButton jToggle_R2D_Timer;
    private javax.swing.JToggleButton jToggle_comm_on;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
