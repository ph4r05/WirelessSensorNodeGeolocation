/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;

/**
 *
 * @author ph4r05
 */
public class Player {
    public String name;
    public int node;
    
    public long lastResponse;
    public long lastResponseTime;
    public long lastWatchdogTime;
    
    public double energy;
    public double light;
    
    public String energyExpression=null;
    public EnergyCalculator energyCalculator;
    
    /**
     * Computes new energy
     * @return Double null on error
     */
    public Double getNewEnergy(){
        if (!(this.energyCalculator instanceof EnergyCalculator)) return null;
        if ("".equals(energyExpression)) return null;
        
        return Double.valueOf(this.energyCalculator.getEnergy(energy, light, energyExpression));
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public EnergyCalculator getEnergyCalculator() {
        return energyCalculator;
    }

    public void setEnergyCalculator(EnergyCalculator energyCalculator) {
        this.energyCalculator = energyCalculator;
    }

    public String getEnergyExpression() {
        return energyExpression;
    }

    public void setEnergyExpression(String energyExpression) {
        this.energyExpression = energyExpression;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public long getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(long lastResponse) {
        this.lastResponse = lastResponse;
    }

    public long getLastResponseTime() {
        return lastResponseTime;
    }

    public void setLastResponseTime(long lastResponseTime) {
        this.lastResponseTime = lastResponseTime;
    }

    public long getLastWatchdogTime() {
        return lastWatchdogTime;
    }

    public void setLastWatchdogTime(long lastWatchdogTime) {
        this.lastWatchdogTime = lastWatchdogTime;
    }
    
    
   
}
