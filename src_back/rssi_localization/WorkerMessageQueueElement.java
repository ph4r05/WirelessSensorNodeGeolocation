/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_localization;

/**
 *
 * @author ph4r05
 */
public class WorkerMessageQueueElement {
    private int round_id;
    private int source;
    private int txpower;
    private int counter;
    private int rssi;

    public WorkerMessageQueueElement() {
    }

    public WorkerMessageQueueElement(int round_id, int source, int txpower, int counter, int rssi) {
        this.round_id = round_id;
        this.source = source;
        this.txpower = txpower;
        this.counter = counter;
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorkerMessageQueueElement other = (WorkerMessageQueueElement) obj;
        if (this.round_id != other.round_id) {
            return false;
        }
        if (this.source != other.source) {
            return false;
        }
        if (this.txpower != other.txpower) {
            return false;
        }
        if (this.counter != other.counter) {
            return false;
        }
        if (this.rssi != other.rssi) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.round_id;
        hash = 67 * hash + this.source;
        hash = 67 * hash + this.txpower;
        hash = 67 * hash + this.counter;
        hash = 67 * hash + this.rssi;
        return hash;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getRound_id() {
        return round_id;
    }

    public void setRound_id(int round_id) {
        this.round_id = round_id;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
