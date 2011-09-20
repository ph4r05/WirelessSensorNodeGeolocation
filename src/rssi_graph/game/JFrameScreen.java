/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JFrameScreen.java
 *
 * Created on Sep 15, 2011, 1:27:19 PM
 */
package rssi_graph.game;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;

/**
 * Represents online status of game visible for users.
 * Usually displayed on separate screen.
 *  
 * @author ph4r05
 */
public class JFrameScreen extends javax.swing.JFrame {

    protected JPanelGame parentPanel = null;
    protected GameWorker gameWorker = null;
    
    /**
     * Flag indicating current state of window sizing for multiplayer.
     * TRUE by default = window is split at 2 parts
     */
    protected boolean currentMultiplayer=true;
    
    /** Creates new form JFrameScreen */
    public JFrameScreen() {
        initComponents();
    }

    /**
     * Generic initial method
     */
    public void initThis(){
        this.energyMeter1.initMeter();
        this.energyMeter2.initMeter();
        
        this.jProgressLight1.setMinimum(0);
        this.jProgressLight1.setMaximum(700);
        
        this.jProgressLight2.setMinimum(0);
        this.jProgressLight2.setMaximum(700);
    }
    
    
    /**
     * Massively enables/disabled this window
     * @param enabled 
     */
    public void setEnabledWindows(boolean enabled){
        GameWorker.enableTree(this.jPanelCommon, enabled);
        GameWorker.enableTree(this.jPanelPlayer1, enabled);
        GameWorker.enableTree(this.jPanelPlayer2, enabled);
    }
    
    /**
     * event trigered on game state change
     */
    public void setGameState(int newState){
        if (newState==GameWorker.GAME_STATE_STOPPED){
            this.setEnabledWindows(false);
        } else if (newState==GameWorker.GAME_STATE_STARTED || newState==GameWorker.GAME_STATE_CLEAN){
            this.setEnabledWindows(true);
        }
    }
    
    /**
     * Set player name for frames & labels
     * @param player 
     */
    public void setPlayerName(int player, String playername){
        if (player==1){
            jPanelPlayer1.setBorder(javax.swing.BorderFactory.createTitledBorder(playername));
            jLabelPlayer1Name.setText(playername);
        } else if (player==2){
            jPanelPlayer2.setBorder(javax.swing.BorderFactory.createTitledBorder(playername));
            jLabelPlayer2Name.setText(playername);
        }
    }
    
    /**
     * Set multiplayer mode.
     * Disable second viewer, expand first to the full size/split half
     */
    public void setMultiplayer(boolean multiplayer){
        // if nothing changed, leave it
        if (multiplayer==currentMultiplayer) return;
        
        Rectangle bounds2 = this.jPanelPlayer2.getBounds();
        Dimension preferredSize2 = this.jPanelPlayer2.getPreferredSize();
        
        Rectangle bounds1 = this.jPanelPlayer1.getBounds();
        Dimension preferredSize1 = this.jPanelPlayer1.getPreferredSize();
        
        if (multiplayer==true){
            // here => now IS NOT multiplayer screen, but should be => split
            this.jPanelPlayer2.setVisible(true);
            this.jPanelPlayer1.setBounds(new Rectangle(bounds1.x, bounds1.y, bounds1.width - bounds2.width, bounds1.height));
            this.jPanelPlayer1.setPreferredSize(new Dimension(preferredSize1.width - preferredSize2.width, preferredSize1.height));
        } else {
            // here => now IS multiplayer screen, but shouldn't be => extend
            this.jPanelPlayer2.setVisible(false);
            this.jPanelPlayer1.setBounds(new Rectangle(bounds1.x, bounds1.y, bounds1.width + bounds2.width, bounds1.height));
            this.jPanelPlayer1.setPreferredSize(new Dimension(preferredSize1.width + preferredSize2.width, preferredSize1.height));
        }
        
        // update current state
        currentMultiplayer = multiplayer;
    }
    
    /**
     * Set energy
     * 
     * @param player
     * @param energy 
     */
    public void setEnergy(int player, double energy){
        NumberFormat formatter = new DecimalFormat("#00.00");
        String fEnergy = formatter.format(energy); 
        
        if (player==1){
            //this.jLabelEnergy1.setText(fEnergy);
            this.energyMeter1.setCurrentValue(energy);
        } else if (player==2){
            //this.jLabelEnergy2.setText(fEnergy);
            this.energyMeter2.setCurrentValue(energy);
        }
    }
    
    /**
     * Update light indicator
     */
    public void setLight(int player, double light){
        if (player==1){
            light = this.jProgressLight1.getMaximum() < light ? this.jProgressLight1.getMaximum() : light;
            this.jProgressLight1.setValue((int) light);
        } else if (player==2){
            light = this.jProgressLight2.getMaximum() < light ? this.jProgressLight2.getMaximum() : light;
            this.jProgressLight2.setValue((int) light);
        }
    }
    
    /**
     * Sets new displayed time
     * 
     * @param newTime 
     */
    public void setTime(long newTime){       
        double minutes = Math.floor(newTime/60.0);
        double seconds = newTime - minutes*60;
        
        NumberFormat formatter = new DecimalFormat("00");
        this.jLabelGameTime.setText(formatter.format(minutes) + ":" + formatter.format(seconds));
    }
    
    /**
     * Event fired - gui update
     */
    void updateGuiTimerFired() {
        // game time
        this.setTime(this.gameWorker.getGameTimeRemaining());
        
        double energy = this.gameWorker.getPlayer1().getEnergy();
        this.setEnergy(1, energy);
        this.setLight(1, this.gameWorker.getPlayer1().getLight());
        
        if (this.gameWorker.getPlayer2() instanceof Player){
            energy = this.gameWorker.getPlayer2().getEnergy();
            this.setEnergy(2, energy);
            this.setLight(2, this.gameWorker.getPlayer2().getLight());
        }
    }
    
    public JPanelGame getParentPanel() {
        return parentPanel;
    }

    public void setParentPanel(JPanelGame parentPanel) {
        this.parentPanel = parentPanel;
    }

    public GameWorker getGameWorker() {
        return gameWorker;
    }

    public void setGameWorker(GameWorker gameWorker) {
        this.gameWorker = gameWorker;
    }

    public JPanel getjPanelPlayer1() {
        return jPanelPlayer1;
    }

    public void setjPanelPlayer1(JPanel jPanelPlayer1) {
        this.jPanelPlayer1 = jPanelPlayer1;
    }

    public JPanel getjPanelPlayer2() {
        return jPanelPlayer2;
    }

    public void setjPanelPlayer2(JPanel jPanelPlayer2) {
        this.jPanelPlayer2 = jPanelPlayer2;
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanelPlayer1 = new javax.swing.JPanel();
        jLabelPlayer1Name = new javax.swing.JLabel();
        energyMeter1 = new rssi_graph.game.energyMeter();
        jPanel1 = new javax.swing.JPanel();
        jProgressLight1 = new javax.swing.JProgressBar();
        jPanelPlayer2 = new javax.swing.JPanel();
        jLabelPlayer2Name = new javax.swing.JLabel();
        energyMeter2 = new rssi_graph.game.energyMeter();
        jPanel2 = new javax.swing.JPanel();
        jProgressLight2 = new javax.swing.JProgressBar();
        jPanelCommon = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabelGameTime = new javax.swing.JLabel();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(rssi_graph.RSSI_graphApp.class).getContext().getResourceMap(JFrameScreen.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        jPanelPlayer1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelPlayer1.border.title"))); // NOI18N
        jPanelPlayer1.setName("jPanelPlayer1"); // NOI18N

        jLabelPlayer1Name.setFont(new java.awt.Font("DejaVu Sans", 1, 24));
        jLabelPlayer1Name.setForeground(resourceMap.getColor("jLabelPlayer1Name.foreground")); // NOI18N
        jLabelPlayer1Name.setText(resourceMap.getString("jLabelPlayer1Name.text")); // NOI18N
        jLabelPlayer1Name.setName("jLabelPlayer1Name"); // NOI18N

        energyMeter1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        energyMeter1.setName("energyMeter1"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setName("jPanel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 316, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 117, Short.MAX_VALUE)
        );

        jProgressLight1.setOrientation(1);
        jProgressLight1.setName("jProgressLight1"); // NOI18N

        javax.swing.GroupLayout jPanelPlayer1Layout = new javax.swing.GroupLayout(jPanelPlayer1);
        jPanelPlayer1.setLayout(jPanelPlayer1Layout);
        jPanelPlayer1Layout.setHorizontalGroup(
            jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                        .addComponent(jProgressLight1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(energyMeter1, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelPlayer1Name))
                .addContainerGap())
        );
        jPanelPlayer1Layout.setVerticalGroup(
            jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelPlayer1Layout.createSequentialGroup()
                .addComponent(jLabelPlayer1Name)
                .addGap(6, 6, 6)
                .addGroup(jPanelPlayer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressLight1, 0, 236, Short.MAX_VALUE)
                    .addComponent(energyMeter1, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanelPlayer2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanelPlayer2.border.title"))); // NOI18N
        jPanelPlayer2.setName("jPanelPlayer2"); // NOI18N

        jLabelPlayer2Name.setFont(new java.awt.Font("DejaVu Sans", 1, 24));
        jLabelPlayer2Name.setForeground(resourceMap.getColor("jLabelPlayer2Name.foreground")); // NOI18N
        jLabelPlayer2Name.setText(resourceMap.getString("jLabelPlayer2Name.text")); // NOI18N
        jLabelPlayer2Name.setName("jLabelPlayer2Name"); // NOI18N

        energyMeter2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        energyMeter2.setName("energyMeter2"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 318, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 117, Short.MAX_VALUE)
        );

        jProgressLight2.setOrientation(1);
        jProgressLight2.setName("jProgressLight2"); // NOI18N

        javax.swing.GroupLayout jPanelPlayer2Layout = new javax.swing.GroupLayout(jPanelPlayer2);
        jPanelPlayer2.setLayout(jPanelPlayer2Layout);
        jPanelPlayer2Layout.setHorizontalGroup(
            jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPlayer2Layout.createSequentialGroup()
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelPlayer2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelPlayer2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelPlayer2Name)
                            .addGroup(jPanelPlayer2Layout.createSequentialGroup()
                                .addComponent(jProgressLight2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(energyMeter2, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanelPlayer2Layout.setVerticalGroup(
            jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelPlayer2Layout.createSequentialGroup()
                .addComponent(jLabelPlayer2Name)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelPlayer2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressLight2, 0, 0, Short.MAX_VALUE)
                    .addComponent(energyMeter2, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanelCommon.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCommon.setName("jPanelCommon"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabelGameTime.setFont(resourceMap.getFont("jLabelGameTime.font")); // NOI18N
        jLabelGameTime.setText(resourceMap.getString("jLabelGameTime.text")); // NOI18N
        jLabelGameTime.setName("jLabelGameTime"); // NOI18N

        javax.swing.GroupLayout jPanelCommonLayout = new javax.swing.GroupLayout(jPanelCommon);
        jPanelCommon.setLayout(jPanelCommonLayout);
        jPanelCommonLayout.setHorizontalGroup(
            jPanelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCommonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelGameTime, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelCommonLayout.setVerticalGroup(
            jPanelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelCommonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabelGameTime)
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelCommon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanelPlayer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelPlayer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelCommon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelPlayer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelPlayer2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new JFrameScreen().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private rssi_graph.game.energyMeter energyMeter1;
    private rssi_graph.game.energyMeter energyMeter2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelGameTime;
    private javax.swing.JLabel jLabelPlayer1Name;
    private javax.swing.JLabel jLabelPlayer2Name;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelCommon;
    private javax.swing.JPanel jPanelPlayer1;
    private javax.swing.JPanel jPanelPlayer2;
    private javax.swing.JProgressBar jProgressLight1;
    private javax.swing.JProgressBar jProgressLight2;
    // End of variables declaration//GEN-END:variables

}
