/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanelGame.java
 *
 * Created on Sep 15, 2011, 1:44:50 PM
 */
package rssi_graph.game;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import rssi_graph.RSSI_graphApp;

/**
 *
 * @author ph4r05
 */
public class JPanelGame extends javax.swing.JPanel {

    protected GameWorker gameWorker = null;
    protected Set<Integer> mobileNodes = null;
    
    boolean updatePlayer1Gui=false;
    boolean updatePlayer2Gui=false;
    
    
    /** Creates new form JPanelGame */
    public JPanelGame() {
        initComponents();
    }
    
    /**
     * Initialization procedure - should do here, netbeans executes constructor when editing GUI
     */
    public void initThis(){
        System.err.println("Init this - panel");
        JPanelGame.enableTree(this.jPanelPlayer1, false);
        JPanelGame.enableTree(this.jPanelPlayer2, false);
    }
    
    /**
     * Recursively enable/disable component and its children.
     * @param player
     * @param enable 
     */
    public void enablePlayerEdit(int player, boolean enable){
        System.err.println("PlayerEditEnable: " + player + "; enable: " + enable);
        if ((player & 1)>0){
            JPanelGame.enableTree(this.jPanelPlayer1, enable);
        }
        
        if ((player & 2)>0){
            JPanelGame.enableTree(this.jPanelPlayer2, enable);
        }
    }
    
    /**
     * Synchronizes model -> view
     * 
     * @param player
     * @param curplayer 
     */
    public void playerModel2View(int player, Player curplayer){
        if (player == 1 && curplayer!=null){
            this.jTextEnergy1.setText(String.valueOf(curplayer.getEnergy()));
            //this.jTextEnergyExpression1.setText(curplayer.getEnergyExpression());
            this.jTextLight1.setText(String.valueOf(curplayer.getLight()));
            this.jTextResponse1.setText(String.valueOf(curplayer.getLastResponse()));
        } else if (player == 2 && curplayer!=null){
            this.jTextEnergy2.setText(String.valueOf(curplayer.getEnergy()));
            //this.jTextEnergyExpression2.setText(curplayer.getEnergyExpression());
            this.jTextLight2.setText(String.valueOf(curplayer.getLight()));
            this.jTextResponse2.setText(String.valueOf(curplayer.getLastResponse()));
        }
    }
    
    /**
     * Synchronizes view -> model
     * @param player
     * @param curplayer 
     */
    public void playerView2Model(int player, Player curplayer){
        if (player == 1){
            curplayer.setEnergy(Double.valueOf(this.jTextEnergy1.getText()));
            curplayer.setEnergyExpression(this.jTextEnergyExpression1.getText());
            curplayer.setLight(Double.valueOf(this.jTextLight1.getText()));
        } else if (player == 2){
            curplayer.setEnergy(Double.valueOf(this.jTextEnergy2.getText()));
            curplayer.setEnergyExpression(this.jTextEnergyExpression2.getText());
            curplayer.setLight(Double.valueOf(this.jTextLight2.getText()));
        }
    }
    
    /**
     * refresh combo boxes node choosers
     */
    public void refreshMobileNodes(int updateCombo){
        // currently selected
        System.err.println("newExec " + updateCombo);
        String oldSelection1 = (String) this.jComboPlayer1Node.getSelectedItem();
        String oldSelection2 = (String) this.jComboPlayer2Node.getSelectedItem();
        Integer selected1 = null;
        Integer selected2 = null;
        
        if (oldSelection1!=null && !("NONE".equalsIgnoreCase(oldSelection1)) 
            && mobileNodes.contains(Integer.valueOf(oldSelection1))){
            selected1 = Integer.valueOf(oldSelection1);
        }
        
         if (oldSelection2!=null && !("NONE".equalsIgnoreCase(oldSelection2)) 
            && mobileNodes.contains(Integer.valueOf(oldSelection2))){
            selected2 = Integer.valueOf(oldSelection2);
        }
        
        
        // assume size+1 = default is none
        int size = mobileNodes.size();
        Object[] modelData1 = new Object[selected2 != null ? size : size+1];
        modelData1[0] = "NONE";
        Object[] modelData2 = new Object[selected1 != null ? size : size+1];
        modelData2[0] = "NONE";
        
        // sort increasingly
        ArrayList<Integer> alist = new ArrayList<Integer>(mobileNodes);
        Collections.sort(alist);
        
        Iterator<Integer> iterator = alist.iterator();
        for(int i1=1,i2=1; iterator.hasNext(); ){
            Integer curNode = iterator.next();
            
            // fill data for first model
            if (!curNode.equals(selected2)){
                modelData1[i1++] = String.valueOf(curNode);
            }
            
            // fill data for second model
            if (!curNode.equals(selected1)){
                modelData2[i2++] = String.valueOf(curNode);
            }
        }
        
        // create new combo box model
        ComboBoxModel model1 = new DefaultComboBoxModel(modelData1);
        ComboBoxModel model2 = new DefaultComboBoxModel(modelData2);
        if ((updateCombo & 1)>0){
            this.jComboPlayer1Node.setModel(model1);
            // selection, if found
            if (selected1!=null 
                    && oldSelection1!=null
                    && ! oldSelection1.equals(this.jComboPlayer1Node.getSelectedItem())){
                this.jComboPlayer1Node.getModel().setSelectedItem(oldSelection1);
            }
        }
        
        if ((updateCombo & 2)>0){
            this.jComboPlayer2Node.setModel(model2);
            if (selected2!=null 
                    && oldSelection2!=null
                    && ! oldSelection2.equals(this.jComboPlayer2Node.getSelectedItem())){
                this.jComboPlayer2Node.getModel().setSelectedItem(oldSelection2);
            }
        }
    }
    
    /**
     * Load mobile nodes to combo boxes.
     */
    public void setMobileNodes(Set<Integer> mobileNodes){
        // copy localy
        this.mobileNodes = mobileNodes;
        this.refreshMobileNodes(3);
    }
    
    /**
     * Event fired when game screen is disposed.
     * Correct toggle button state
     */
    public void screenDisposed(){
        this.jToggleDisplayScreen.setSelected(false);
    }
    
    /**
     * Get player name
     * 
     * @param player
     * @return 
     */
    public String GetPlayerName(int player){
        return player==1 ? this.jTextPlayer1Name.getText() : this.jTextPlayer2Name.getText();
    }
    
    /**
     * Get player node ID
     * 
     * @param player
     * @return 
     */
    public String GetPlayerNode(int player){
        return (String) (player==1 ? this.jComboPlayer1Node.getSelectedItem() : this.jComboPlayer2Node.getSelectedItem());
    }
    
    /**
     * Update gui timer fired event
     */
    public void updateGuiTimerFired(){
        if (this.updatePlayer1Gui && this.gameWorker.getPlayer1() instanceof Player){
            this.playerModel2View(1, this.gameWorker.getPlayer1());
        }
        
        if (this.updatePlayer2Gui && this.gameWorker.getPlayer2() instanceof Player){
            this.playerModel2View(2, this.gameWorker.getPlayer2());
        }
    }
    
    /**
     * Used for enable/disable
     * 
     * @param root
     * @param enable 
     */
    static void enableTree(Container root, boolean enable) {
        Component children[] = root.getComponents();        
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Container) {
                enableTree((Container) children[i], enable);
            } else {
                children[i].setEnabled(enable);
            }
        }
        
        root.setEnabled(enable);
    }
    
    /**
     * Returns game time in seconds
     * 
     * @return 
     */
    public int getGameTime(){
       int gameTime = 0;
       
       try {
           gameTime = Integer.parseInt(this.jTextGameTime.getText());
       } catch(Exception e) {
           ;
       }
       
       return gameTime;
    }
    
    /**
     * Minimal time needed to do 1 round on ring
     * @return 
     */
    public int getMinimalLapTime(){
        return 0;
    }
    
    public GameWorker getGameWorker() {
        return gameWorker;
    }

    public void setGameWorker(GameWorker gameWorker) {
        this.gameWorker = gameWorker;
    }

    public boolean isUpdatePlayer1Gui() {
        return updatePlayer1Gui;
    }

    public void setUpdatePlayer1Gui(boolean updatePlayer1Gui) {
        this.updatePlayer1Gui = updatePlayer1Gui;
    }

    public boolean isUpdatePlayer2Gui() {
        return updatePlayer2Gui;
    }

    public void setUpdatePlayer2Gui(boolean updatePlayer2Gui) {
        this.updatePlayer2Gui = updatePlayer2Gui;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboPlayer1Node = new javax.swing.JComboBox();
        jComboPlayer2Node = new javax.swing.JComboBox();
        jTextPlayer1Name = new javax.swing.JTextField();
        jTextPlayer2Name = new javax.swing.JTextField();
        jButtonSubmitPlayerSettings = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        jToggleDisplayScreen = new javax.swing.JToggleButton();
        jToggleStarted = new javax.swing.JToggleButton();
        jToggleUpdateGUI = new javax.swing.JToggleButton();
        jToggleDoWatchdog = new javax.swing.JToggleButton();
        jTogglePlaySounds = new javax.swing.JToggleButton();
        jButtonResetNodes = new javax.swing.JButton();
        jPanelPlayer1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextEnergy1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextLight1 = new javax.swing.JTextField();
        jTextResponse1 = new javax.swing.JTextField();
        jToolBar2 = new javax.swing.JToolBar();
        jButtonUpdate1 = new javax.swing.JButton();
        jToggleLoadLiveData1 = new javax.swing.JToggleButton();
        jButtonRecompute1 = new javax.swing.JButton();
        jButtonRequest1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextX1 = new javax.swing.JTextField();
        jTextY1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextEnergyExpression1 = new javax.swing.JTextField();
        jPanelPlayer2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextEnergy2 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextLight2 = new javax.swing.JTextField();
        jTextResponse2 = new javax.swing.JTextField();
        jToolBar3 = new javax.swing.JToolBar();
        jButtonUpdate2 = new javax.swing.JButton();
        jToggleLoadLiveData2 = new javax.swing.JToggleButton();
        jButtonRecompute2 = new javax.swing.JButton();
        jButtonRequest2 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jTextX2 = new javax.swing.JTextField();
        jTextY2 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jTextEnergyExpression2 = new javax.swing.JTextField();
        jPanelOptions = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jTextRequestTimeout = new javax.swing.JTextField();
        jButtonOptionsApply = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jTextWatchdogThreshold = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextSmoothingSensor = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jToolBar4 = new javax.swing.JToolBar();
        jButtonGameStartNew = new javax.swing.JButton();
        jButtonGamePause = new javax.swing.JButton();
        jButtonGameStop = new javax.swing.JButton();
        jButtonGameReset = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jTextGameTime = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextLapTime = new javax.swing.JTextField();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JPanelGame.class);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jComboPlayer1Node.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE" }));
        jComboPlayer1Node.setName("jComboPlayer1Node"); // NOI18N
        jComboPlayer1Node.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboPlayer1NodeActionPerformed(evt);
            }
        });

        jComboPlayer2Node.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE" }));
        jComboPlayer2Node.setName("jComboPlayer2Node"); // NOI18N
        jComboPlayer2Node.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboPlayer2NodeActionPerformed(evt);
            }
        });

        jTextPlayer1Name.setText(resourceMap.getString("jTextPlayer1Name.text")); // NOI18N
        jTextPlayer1Name.setName("jTextPlayer1Name"); // NOI18N

        jTextPlayer2Name.setText(resourceMap.getString("jTextPlayer2Name.text")); // NOI18N
        jTextPlayer2Name.setName("jTextPlayer2Name"); // NOI18N

        jButtonSubmitPlayerSettings.setText(resourceMap.getString("jButtonSubmitPlayerSettings.text")); // NOI18N
        jButtonSubmitPlayerSettings.setName("jButtonSubmitPlayerSettings"); // NOI18N
        jButtonSubmitPlayerSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSubmitPlayerSettingsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonSubmitPlayerSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(jTextPlayer1Name, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(jComboPlayer1Node, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextPlayer2Name, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(jComboPlayer2Node, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextPlayer1Name, jTextPlayer2Name});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboPlayer1Node, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboPlayer2Node, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextPlayer1Name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextPlayer2Name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonSubmitPlayerSettings))
        );

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        jToggleDisplayScreen.setText(resourceMap.getString("jToggleDisplayScreen.text")); // NOI18N
        jToggleDisplayScreen.setName("jToggleDisplayScreen"); // NOI18N
        jToggleDisplayScreen.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleDisplayScreenItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleDisplayScreen);

        jToggleStarted.setText(resourceMap.getString("jToggleStarted.text")); // NOI18N
        jToggleStarted.setFocusable(false);
        jToggleStarted.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleStarted.setName("jToggleStarted"); // NOI18N
        jToggleStarted.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleStarted.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleStartedItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleStarted);

        jToggleUpdateGUI.setText(resourceMap.getString("jToggleUpdateGUI.text")); // NOI18N
        jToggleUpdateGUI.setFocusable(false);
        jToggleUpdateGUI.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleUpdateGUI.setName("jToggleUpdateGUI"); // NOI18N
        jToggleUpdateGUI.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleUpdateGUI.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleUpdateGUIItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleUpdateGUI);

        jToggleDoWatchdog.setText(resourceMap.getString("jToggleDoWatchdog.text")); // NOI18N
        jToggleDoWatchdog.setFocusable(false);
        jToggleDoWatchdog.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleDoWatchdog.setName("jToggleDoWatchdog"); // NOI18N
        jToggleDoWatchdog.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToggleDoWatchdog.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleDoWatchdogItemStateChanged(evt);
            }
        });
        jToolBar1.add(jToggleDoWatchdog);

        jTogglePlaySounds.setText(resourceMap.getString("jTogglePlaySounds.text")); // NOI18N
        jTogglePlaySounds.setFocusable(false);
        jTogglePlaySounds.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jTogglePlaySounds.setName("jTogglePlaySounds"); // NOI18N
        jTogglePlaySounds.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jTogglePlaySounds.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jTogglePlaySoundsItemStateChanged(evt);
            }
        });
        jToolBar1.add(jTogglePlaySounds);

        jButtonResetNodes.setText(resourceMap.getString("jButtonResetNodes.text")); // NOI18N
        jButtonResetNodes.setFocusable(false);
        jButtonResetNodes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonResetNodes.setName("jButtonResetNodes"); // NOI18N
        jButtonResetNodes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonResetNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetNodesActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonResetNodes);

        jPanelPlayer1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelPlayer1.border.title"))); // NOI18N
        jPanelPlayer1.setName("jPanelPlayer1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jTextEnergy1.setText(resourceMap.getString("jTextEnergy1.text")); // NOI18N
        jTextEnergy1.setName("jTextEnergy1"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jTextLight1.setText(resourceMap.getString("jTextLight1.text")); // NOI18N
        jTextLight1.setName("jTextLight1"); // NOI18N

        jTextResponse1.setText(resourceMap.getString("jTextResponse1.text")); // NOI18N
        jTextResponse1.setName("jTextResponse1"); // NOI18N

        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        jButtonUpdate1.setText(resourceMap.getString("jButtonUpdate1.text")); // NOI18N
        jButtonUpdate1.setName("jButtonUpdate1"); // NOI18N
        jButtonUpdate1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdate1ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonUpdate1);

        jToggleLoadLiveData1.setText(resourceMap.getString("jToggleLoadLiveData1.text")); // NOI18N
        jToggleLoadLiveData1.setName("jToggleLoadLiveData1"); // NOI18N
        jToggleLoadLiveData1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleLoadLiveData1ItemStateChanged(evt);
            }
        });
        jToolBar2.add(jToggleLoadLiveData1);

        jButtonRecompute1.setText(resourceMap.getString("jButtonRecompute1.text")); // NOI18N
        jButtonRecompute1.setFocusable(false);
        jButtonRecompute1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRecompute1.setName("jButtonRecompute1"); // NOI18N
        jButtonRecompute1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRecompute1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRecompute1ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonRecompute1);

        jButtonRequest1.setText(resourceMap.getString("jButtonRequest1.text")); // NOI18N
        jButtonRequest1.setFocusable(false);
        jButtonRequest1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRequest1.setName("jButtonRequest1"); // NOI18N
        jButtonRequest1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRequest1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRequest1ActionPerformed(evt);
            }
        });
        jToolBar2.add(jButtonRequest1);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jTextX1.setText(resourceMap.getString("jTextX1.text")); // NOI18N
        jTextX1.setName("jTextX1"); // NOI18N

        jTextY1.setText(resourceMap.getString("jTextY1.text")); // NOI18N
        jTextY1.setName("jTextY1"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jTextEnergyExpression1.setText(resourceMap.getString("jTextEnergyExpression1.text")); // NOI18N
        jTextEnergyExpression1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextEnergyExpression1.setName("jTextEnergyExpression1"); // NOI18N

        javax.swing.GroupLayout jPanelPlayer1Layout = new javax.swing.GroupLayout(jPanelPlayer1);
        jPanelPlayer1.setLayout(jPanelPlayer1Layout);
        jPanelPlayer1Layout.setHorizontalGroup(
            jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPlayer1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPlayer1Layout.createSequentialGroup()
                                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel5)
                                        .addComponent(jLabel3))
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextResponse1, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                    .addComponent(jTextLight1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextEnergy1, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPlayer1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextX1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextY1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jTextEnergyExpression1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanelPlayer1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel4, jLabel5, jLabel7});

        jPanelPlayer1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextX1, jTextY1});

        jPanelPlayer1Layout.setVerticalGroup(
            jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextEnergy1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextLight1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextResponse1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextX1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextEnergyExpression1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelPlayer2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelPlayer2.border.title"))); // NOI18N
        jPanelPlayer2.setName("jPanelPlayer2"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jTextEnergy2.setText(resourceMap.getString("jTextEnergy2.text")); // NOI18N
        jTextEnergy2.setName("jTextEnergy2"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jTextLight2.setText(resourceMap.getString("jTextLight2.text")); // NOI18N
        jTextLight2.setName("jTextLight2"); // NOI18N

        jTextResponse2.setText(resourceMap.getString("jTextResponse2.text")); // NOI18N
        jTextResponse2.setName("jTextResponse2"); // NOI18N

        jToolBar3.setRollover(true);
        jToolBar3.setName("jToolBar3"); // NOI18N

        jButtonUpdate2.setText(resourceMap.getString("jButtonUpdate2.text")); // NOI18N
        jButtonUpdate2.setName("jButtonUpdate2"); // NOI18N
        jButtonUpdate2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdate2ActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonUpdate2);

        jToggleLoadLiveData2.setText(resourceMap.getString("jToggleLoadLiveData2.text")); // NOI18N
        jToggleLoadLiveData2.setName("jToggleLoadLiveData2"); // NOI18N
        jToggleLoadLiveData2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jToggleLoadLiveData2ItemStateChanged(evt);
            }
        });
        jToolBar3.add(jToggleLoadLiveData2);

        jButtonRecompute2.setText(resourceMap.getString("jButtonRecompute2.text")); // NOI18N
        jButtonRecompute2.setFocusable(false);
        jButtonRecompute2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRecompute2.setName("jButtonRecompute2"); // NOI18N
        jButtonRecompute2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRecompute2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRecompute2ActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonRecompute2);

        jButtonRequest2.setText(resourceMap.getString("jButtonRequest2.text")); // NOI18N
        jButtonRequest2.setFocusable(false);
        jButtonRequest2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonRequest2.setName("jButtonRequest2"); // NOI18N
        jButtonRequest2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonRequest2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRequest2ActionPerformed(evt);
            }
        });
        jToolBar3.add(jButtonRequest2);

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jTextX2.setText(resourceMap.getString("jTextX2.text")); // NOI18N
        jTextX2.setName("jTextX2"); // NOI18N

        jTextY2.setText(resourceMap.getString("jTextY2.text")); // NOI18N
        jTextY2.setName("jTextY2"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        jTextEnergyExpression2.setText(resourceMap.getString("jTextEnergyExpression2.text")); // NOI18N
        jTextEnergyExpression2.setName("jTextEnergyExpression2"); // NOI18N

        javax.swing.GroupLayout jPanelPlayer2Layout = new javax.swing.GroupLayout(jPanelPlayer2);
        jPanelPlayer2.setLayout(jPanelPlayer2Layout);
        jPanelPlayer2Layout.setHorizontalGroup(
            jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer2Layout.createSequentialGroup()
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPlayer2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPlayer2Layout.createSequentialGroup()
                                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel8)
                                        .addComponent(jLabel6))
                                    .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextResponse2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                                    .addComponent(jTextLight2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextEnergy2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPlayer2Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(jTextX2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextY2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPlayer2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jTextEnergyExpression2, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelPlayer2Layout.setVerticalGroup(
            jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer2Layout.createSequentialGroup()
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextEnergy2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextLight2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextResponse2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jTextX2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextEnergyExpression2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelOptions.border.title"))); // NOI18N
        jPanelOptions.setName("jPanelOptions"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jTextRequestTimeout.setText(resourceMap.getString("jTextRequestTimeout.text")); // NOI18N
        jTextRequestTimeout.setName("jTextRequestTimeout"); // NOI18N

        jButtonOptionsApply.setText(resourceMap.getString("jButtonOptionsApply.text")); // NOI18N
        jButtonOptionsApply.setName("jButtonOptionsApply"); // NOI18N
        jButtonOptionsApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOptionsApplyActionPerformed(evt);
            }
        });

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jTextWatchdogThreshold.setText(resourceMap.getString("jTextWatchdogThreshold.text")); // NOI18N
        jTextWatchdogThreshold.setName("jTextWatchdogThreshold"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jTextSmoothingSensor.setText(resourceMap.getString("jTextSmoothingSensor.text")); // NOI18N
        jTextSmoothingSensor.setName("jTextSmoothingSensor"); // NOI18N

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOptionsApply, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptionsLayout.createSequentialGroup()
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel19)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextSmoothingSensor)
                            .addComponent(jTextWatchdogThreshold, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextRequestTimeout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jTextRequestTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextWatchdogThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextSmoothingSensor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonOptionsApply, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jToolBar4.setRollover(true);
        jToolBar4.setName("jToolBar4"); // NOI18N

        jButtonGameStartNew.setText(resourceMap.getString("jButtonGameStartNew.text")); // NOI18N
        jButtonGameStartNew.setFocusable(false);
        jButtonGameStartNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGameStartNew.setName("jButtonGameStartNew"); // NOI18N
        jButtonGameStartNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGameStartNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGameStartNewActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonGameStartNew);

        jButtonGamePause.setText(resourceMap.getString("jButtonGamePause.text")); // NOI18N
        jButtonGamePause.setEnabled(false);
        jButtonGamePause.setFocusable(false);
        jButtonGamePause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGamePause.setName("jButtonGamePause"); // NOI18N
        jButtonGamePause.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGamePause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGamePauseActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonGamePause);

        jButtonGameStop.setText(resourceMap.getString("jButtonGameStop.text")); // NOI18N
        jButtonGameStop.setEnabled(false);
        jButtonGameStop.setFocusable(false);
        jButtonGameStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGameStop.setName("jButtonGameStop"); // NOI18N
        jButtonGameStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGameStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGameStopActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonGameStop);

        jButtonGameReset.setText(resourceMap.getString("jButtonGameReset.text")); // NOI18N
        jButtonGameReset.setFocusable(false);
        jButtonGameReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonGameReset.setName("jButtonGameReset"); // NOI18N
        jButtonGameReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonGameReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGameResetActionPerformed(evt);
            }
        });
        jToolBar4.add(jButtonGameReset);

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        jTextGameTime.setText(resourceMap.getString("jTextGameTime.text")); // NOI18N
        jTextGameTime.setName("jTextGameTime"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jTextLapTime.setText(resourceMap.getString("jTextLapTime.text")); // NOI18N
        jTextLapTime.setName("jTextLapTime"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addGap(127, 127, 127)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextLapTime, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextGameTime, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addContainerGap(403, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextGameTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextLapTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1556, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelPlayer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelPlayer2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(414, 414, 414))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jPanel1, jPanelOptions});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelPlayer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelPlayer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Submitted player settings
     * 
     * @param evt 
     */
    private void jButtonSubmitPlayerSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSubmitPlayerSettingsActionPerformed
        if (this.gameWorker instanceof GameWorker){
            // sanitize form, first player cannot be NONE when second player is set
            String playerNode1 = this.GetPlayerNode(1);
            String playerNode2 = this.GetPlayerNode(2);
            if ("NONE".equalsIgnoreCase(playerNode1) && !("NONE".equalsIgnoreCase(playerNode2))){
                // swap users here
                ComboBoxModel model = this.jComboPlayer2Node.getModel();
                this.jComboPlayer2Node.setModel(this.jComboPlayer1Node.getModel());
                this.jComboPlayer1Node.setModel(model);
                
                String tmpName = this.jTextPlayer2Name.getText();
                this.jTextPlayer2Name.setText(this.jTextPlayer1Name.getText());
                this.jTextPlayer1Name.setText(tmpName);
                
                JOptionPane.showMessageDialog(null,
                    "Second player cannot be set when first is not, swapping!",
                    "Settings error",
                    JOptionPane.WARNING_MESSAGE);
            }
            
            this.gameWorker.settingsChanged();
        }
    }//GEN-LAST:event_jButtonSubmitPlayerSettingsActionPerformed

    /**
     * Player 1 selected - exclude selected player from other lists
     * @param evt 
     */
    private void jComboPlayer1NodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboPlayer1NodeActionPerformed
        // TODO add your handling code here:
        System.err.println("ActionCommand: " + evt.getActionCommand());
        this.refreshMobileNodes(2);
    }//GEN-LAST:event_jComboPlayer1NodeActionPerformed

    /**
     * Player 2 selected - exclude selected player from other lists
     * @param evt 
     */
    private void jComboPlayer2NodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboPlayer2NodeActionPerformed
        // TODO add your handling code here:
        System.err.println("ActionCommand: " + evt.getActionCommand());
        this.refreshMobileNodes(1);
    }//GEN-LAST:event_jComboPlayer2NodeActionPerformed

    /**
     * react on toggle button - display screen
     * @param evt 
     */
    private void jToggleDisplayScreenItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleDisplayScreenItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED){
            // selected - set visible
            this.gameWorker.setScreenVisible(true);
        } else {
            // deselected - dispose
            this.gameWorker.setScreenVisible(false);
        }
    }//GEN-LAST:event_jToggleDisplayScreenItemStateChanged

    /**
     * Game started
     * 
     * @param evt 
     */
    private void jToggleStartedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleStartedItemStateChanged
        // TODO add your handling code here:
        this.gameWorker.setGameTimer(evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_jToggleStartedItemStateChanged

    /**
     * Recompute energy manually according to data
     * @param evt 
     */
    private void jButtonRecompute1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRecompute1ActionPerformed
        // TODO add your handling code here:
        Double newEnergy = this.gameWorker.getPlayer1().getNewEnergy();
        if (newEnergy!=null){
            this.gameWorker.getPlayer1().setEnergy(newEnergy);
        }
        
        this.playerModel2View(1, this.gameWorker.getPlayer1());
    }//GEN-LAST:event_jButtonRecompute1ActionPerformed

    /**
     * Recompute energy manually according to data
     * @param evt 
     */
    private void jButtonRecompute2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRecompute2ActionPerformed
        // TODO add your handling code here:
        Double newEnergy = this.gameWorker.getPlayer2().getNewEnergy();
        if (newEnergy!=null){
            this.gameWorker.getPlayer2().setEnergy(newEnergy);
        }
        
        this.playerModel2View(2, this.gameWorker.getPlayer2());
    }//GEN-LAST:event_jButtonRecompute2ActionPerformed

    /**
     * Update form data to data structure
     * @param evt 
     */
    private void jButtonUpdate1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdate1ActionPerformed
        // TODO add your handling code here:
        // @BUG: abstraction violation / model viewer violation
        this.playerView2Model(1, this.gameWorker.getPlayer1());
    }//GEN-LAST:event_jButtonUpdate1ActionPerformed

    /**
     * Update form data to data structure
     * @param evt 
     */
    private void jButtonUpdate2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdate2ActionPerformed
        // TODO add your handling code here:
        this.playerView2Model(2, this.gameWorker.getPlayer2());
    }//GEN-LAST:event_jButtonUpdate2ActionPerformed

    /**
     * Set whether should be form data updated from data structure periodicaly 
     * @param evt 
     */
    private void jToggleLoadLiveData2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleLoadLiveData2ItemStateChanged
        // TODO add your handling code here:
        this.updatePlayer2Gui = evt.getStateChange() == ItemEvent.SELECTED;
}//GEN-LAST:event_jToggleLoadLiveData2ItemStateChanged

    /**
     * Set whether should be form data updated from data structure periodicaly 
     * @param evt 
     */
    private void jToggleLoadLiveData1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleLoadLiveData1ItemStateChanged
        // TODO add your handling code here:
        this.updatePlayer1Gui = evt.getStateChange() == ItemEvent.SELECTED;
}//GEN-LAST:event_jToggleLoadLiveData1ItemStateChanged

    /**
     * Update gui toggle
     * 
     * @param evt 
     */
    private void jToggleUpdateGUIItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleUpdateGUIItemStateChanged
        // TODO add your handling code here:
        this.gameWorker.setUpdateGui(evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_jToggleUpdateGUIItemStateChanged

    private void jButtonRequest1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRequest1ActionPerformed
        // TODO add your handling code here:
        this.gameWorker.sendRequest(1);
    }//GEN-LAST:event_jButtonRequest1ActionPerformed

    private void jButtonRequest2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRequest2ActionPerformed
        // TODO add your handling code here:
        this.gameWorker.sendRequest(2);
    }//GEN-LAST:event_jButtonRequest2ActionPerformed

    /**
     * Sending parameters
     * @param evt 
     */
    private void jButtonOptionsApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOptionsApplyActionPerformed
        // TODO add your handling code here:
        this.gameWorker.setWatchdogThreshold(Integer.parseInt(this.jTextWatchdogThreshold.getText()));
        this.gameWorker.setRequestTimeout(Integer.parseInt(this.jTextRequestTimeout.getText()));
        this.gameWorker.setSmoothingSensor(Double.parseDouble(this.jTextSmoothingSensor.getText()));
    }//GEN-LAST:event_jButtonOptionsApplyActionPerformed

    /**
     * watchdog enabled/disabled
     * @param evt 
     */
    private void jToggleDoWatchdogItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jToggleDoWatchdogItemStateChanged
        // TODO add your handling code here:
        this.gameWorker.setWatchdogEnabled(evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_jToggleDoWatchdogItemStateChanged

    private void jButtonResetNodesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetNodesActionPerformed
        // TODO add your handling code here:
        this.gameWorker.sendReset(1);
        this.gameWorker.sendReset(2);
    }//GEN-LAST:event_jButtonResetNodesActionPerformed

    /**
     * Play sounds ?
     * @param evt 
     */
    private void jTogglePlaySoundsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jTogglePlaySoundsItemStateChanged
        // TODO add your handling code here:
        this.gameWorker.setSoundEnabled(evt.getStateChange() == ItemEvent.SELECTED);
    }//GEN-LAST:event_jTogglePlaySoundsItemStateChanged

    private void jButtonGameStartNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGameStartNewActionPerformed
        // we want to change game state
        boolean result = this.gameWorker.changeGameState(GameWorker.GAME_STATE_STARTED);
        if (result){
            // change was sucessfull
            this.jButtonGamePause.setEnabled(true);
            this.jButtonGameStop.setEnabled(true);
            this.updateGuiTimerFired();
        }
    }//GEN-LAST:event_jButtonGameStartNewActionPerformed

    private void jButtonGamePauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGamePauseActionPerformed
        // we want to change game state
        boolean result = this.gameWorker.changeGameState(GameWorker.GAME_STATE_PAUSED);
        if (result){
            // change was sucessfull
            this.jButtonGamePause.setEnabled(true);
            this.jButtonGameStop.setEnabled(true);
            this.updateGuiTimerFired();
        }
    }//GEN-LAST:event_jButtonGamePauseActionPerformed

    private void jButtonGameStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGameStopActionPerformed
        // TODO add your handling code here:
        boolean result = this.gameWorker.changeGameState(GameWorker.GAME_STATE_STOPPED);
        if (result){
            // change was sucessfull
            this.jButtonGamePause.setEnabled(false);
            this.updateGuiTimerFired();
        }
    }//GEN-LAST:event_jButtonGameStopActionPerformed

    private void jButtonGameResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGameResetActionPerformed
        // TODO add your handling code here:
        boolean result = this.gameWorker.changeGameState(GameWorker.GAME_STATE_STOPPED);
        
        // change was sucessfull
        this.jButtonGamePause.setEnabled(false);

        // clean timers, init new game, do not start now
        this.gameWorker.initNewGame();
        this.updateGuiTimerFired();
    }//GEN-LAST:event_jButtonGameResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonGamePause;
    private javax.swing.JButton jButtonGameReset;
    private javax.swing.JButton jButtonGameStartNew;
    private javax.swing.JButton jButtonGameStop;
    private javax.swing.JButton jButtonOptionsApply;
    private javax.swing.JButton jButtonRecompute1;
    private javax.swing.JButton jButtonRecompute2;
    private javax.swing.JButton jButtonRequest1;
    private javax.swing.JButton jButtonRequest2;
    private javax.swing.JButton jButtonResetNodes;
    private javax.swing.JButton jButtonSubmitPlayerSettings;
    private javax.swing.JButton jButtonUpdate1;
    private javax.swing.JButton jButtonUpdate2;
    private javax.swing.JComboBox jComboPlayer1Node;
    private javax.swing.JComboBox jComboPlayer2Node;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelPlayer1;
    private javax.swing.JPanel jPanelPlayer2;
    private javax.swing.JTextField jTextEnergy1;
    private javax.swing.JTextField jTextEnergy2;
    private javax.swing.JTextField jTextEnergyExpression1;
    private javax.swing.JTextField jTextEnergyExpression2;
    private javax.swing.JTextField jTextGameTime;
    private javax.swing.JTextField jTextLapTime;
    private javax.swing.JTextField jTextLight1;
    private javax.swing.JTextField jTextLight2;
    private javax.swing.JTextField jTextPlayer1Name;
    private javax.swing.JTextField jTextPlayer2Name;
    private javax.swing.JTextField jTextRequestTimeout;
    private javax.swing.JTextField jTextResponse1;
    private javax.swing.JTextField jTextResponse2;
    private javax.swing.JTextField jTextSmoothingSensor;
    private javax.swing.JTextField jTextWatchdogThreshold;
    private javax.swing.JTextField jTextX1;
    private javax.swing.JTextField jTextX2;
    private javax.swing.JTextField jTextY1;
    private javax.swing.JTextField jTextY2;
    private javax.swing.JToggleButton jToggleDisplayScreen;
    private javax.swing.JToggleButton jToggleDoWatchdog;
    private javax.swing.JToggleButton jToggleLoadLiveData1;
    private javax.swing.JToggleButton jToggleLoadLiveData2;
    private javax.swing.JToggleButton jTogglePlaySounds;
    private javax.swing.JToggleButton jToggleStarted;
    private javax.swing.JToggleButton jToggleUpdateGUI;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    // End of variables declaration//GEN-END:variables
}
