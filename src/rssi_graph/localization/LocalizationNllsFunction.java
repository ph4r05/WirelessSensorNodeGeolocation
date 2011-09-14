/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.localization;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;

/**
 * Function to be optimized, passed to optimizers
 * Trilateration problem formulation for nonlinear least squares optimizer
 *
 * @author ph4r05
 */
public class LocalizationNllsFunction implements DifferentiableMultivariateVectorialFunction {
    
    /**
     * Known positions of static nodes used in trilateration process
     */
    private double positions[][] = null;

    /**
     * Measured distances from static nodes to mobile node
     */
    private double distances[] = null;

    /**
     * Use only applicable distances when computing trilateration problem
     */
    private boolean applicableDistances[] = null;

    /**
     * pre-computed number of applicable distances.
     * if this.applicableDistances[i]==true => distance i is applicable. (has meaningful value)
     * so we can compute with this. Is always updated on setters
     */
    private int numOfApplicableDistances=0;

    /**
     * constructor
     */
    public LocalizationNllsFunction() {
        // init only distances
        // positions of static nodes/anchors are passed as references
        distances = new double[100];
    }

    /**
     * Calculate and return Jacobian function
     * Actually return initialized function
     * 
     * Jacobian matrix, [row][column]
     * at J[i][0] = delta_[(x0-xi)^2 + (y0-yi)^2 - ri^2]/delta_[x0]
     * at J[i][1] = delta_[(x0-xi)^2 + (y0-yi)^2 - ri^2]/delta_[y0]
     * partial derivate with respect to the parameters passed to value() method
     *
     * {@inheritDoc}
     * @return
     */
    public MultivariateMatrixFunction jacobian() {
        return new MultivariateMatrixFunction() {
                 public double[][] value(double[] point) {
                     
                     // consider only applicable distances
                     double[][] jacobian = new double[getNumOfApplicableDistances()][2];
                     for (int i = 0, c = 0, cnI = distances.length; i < cnI; ++i) {
                         
                         // skip not applicable distances
                         if (applicableDistances[i]==false) continue;
                         
                         jacobian[c][0]= 2*point[0] - 2*positions[i][0];
                         jacobian[c][1]= 2*point[1] - 2*positions[i][1];
                         ++c;
                     }

                     return jacobian;
                 }
             };
    }

    /**
     * {@inheritDoc}
     * @param point
     * @return
     * @throws FunctionEvaluationException
     * @throws IllegalArgumentException
     */
    public double[] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
        // check point dimension
        if (point.length!=2 && point.length!=3){
            throw new IllegalArgumentException("Point has to have 2 or 3 dimensions.");
        }

        // null pointer exception?
        if (this.distances == null || this.positions == null || this.applicableDistances == null){
            throw new IllegalArgumentException("One of internal parameter is null");
        }

        // check if we have enough data for localization
        if (this.getNumOfApplicableDistances() < point.length){
            throw new IllegalArgumentException("Not enough data for localization");
        }

        // computing least squares. for each applicable node will have separate equation
        double[] resultPoint = new double[getNumOfApplicableDistances()];

        // perform SUM for all distances defined
        for(int i=0, c=0, cnI=resultPoint.length; i<cnI; i++){
            resultPoint[i] = 0;
            
            // if this value is not applicable, continue and ignore it
            if (this.applicableDistances[i]==false) continue;

            // calculate sum, add to overall
            resultPoint[c] = 0
                    + (point[0] - this.getPositions()[i][0])*(point[0] - this.getPositions()[i][0])
                    + (point[1] - this.getPositions()[i][1])*(point[1] - this.getPositions()[i][1])
                    - (this.getDistances()[i]) * (this.getDistances()[i]);
            
            ++c;
        }

        return resultPoint;
    }

    /**
     * Returns number of applicable distances
     * if this.applicableDistances[i]==true => distance i is applicable. (has meaningful value)
     * so we can compute with this
     *
     * @return
     */
    public int computeNumOfApplicableDistances(){
        // applicable data counter, count at first
        this.setNumOfApplicableDistances(0);
        for(int i=0; i<this.getApplicableDistances().length; i++){
            if (this.applicableDistances[i]==true) this.numOfApplicableDistances+=1;
        }

        return this.getNumOfApplicableDistances();
    }



    /**
     * GETTERS + SETTERS below
     * Setters updates Jacobian instance as well
     */
    
    public double[] getDistances() {
        return distances;
    }

    public void setDistances(double[] distances) {
        this.distances = distances;
    }

    public double[][] getPositions() {
        return positions;
    }

    public void setPositions(double[][] positions) {
        this.positions = positions;
    }

    public boolean[] getApplicableDistances() {
        return applicableDistances;
    }

    public void setApplicableDistances(boolean[] applicableDistances) {
        // check size consistency
        if (applicableDistances.length != this.distances.length){
            throw new IllegalArgumentException("Size does not match to distances");
        }

        this.applicableDistances = applicableDistances;
        this.computeNumOfApplicableDistances();
    }

    public int getNumOfApplicableDistances() {
        return numOfApplicableDistances;
    }

    public void setNumOfApplicableDistances(int numOfApplicableDistances) {
        this.numOfApplicableDistances = numOfApplicableDistances;
    }
    
}
