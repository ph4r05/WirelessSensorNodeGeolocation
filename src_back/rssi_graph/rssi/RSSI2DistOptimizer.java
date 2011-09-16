/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.WeightedObservedPoint;
import org.apache.commons.math.optimization.general.AbstractLeastSquaresOptimizer;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import rssi_graph.rssi.RSSI2DistFunctionInterface;
import rssi_graph.rssi.RSSI2DistFunctionOptimizerInterface;

/**
 * Computes ideal parameters for rssi2distance model to best fit given data set
 *
 * @author ph4r05
 */
public class RSSI2DistOptimizer implements RSSI2DistFunctionOptimizerInterface {

    /**
     * Function to be fitted to data curve
     */
    private RSSI2DistFunctionInterface function = null;

    /**
     * CurveFitter/optimizer
     */
    private CurveFitter fitter = null;

    /**
     * Optimizer used with curve fitter
     */
    private DifferentiableMultivariateVectorialOptimizer optimizer = null;

    /**
     * Initialization in constructor
     */
    public RSSI2DistOptimizer() {
        this.function = new RSSI2DistLogNormalShadowing();
        this.optimizer = new LevenbergMarquardtOptimizer();
        this.fitter = new CurveFitter(this.optimizer);
    }

    /**
     * Optimization/curve fitting
     *
     * @param datapoints 
     * @return
     */
    public double[] optimizeParameters(LinkedList<WeightedObservedPoint> datapoints) throws FunctionEvaluationException, OptimizationException{
        if (datapoints == null){
            throw new IllegalArgumentException("Null pointer exception - datapoints == null");
        }

        // clear all observation points added before
        this.getFitter().clearObservations();

        // add data points to fitter
        Iterator<WeightedObservedPoint> it = datapoints.iterator();
        while(it.hasNext()){
            WeightedObservedPoint tmp = it.next();
            this.getFitter().addObservedPoint(tmp);
        }

        double[] initialTip = new double[2];
        initialTip[0]=3.0;
        initialTip[1]=0.0;

        return this.getFitter().fit(function, initialTip);
    }

    /**
     * Optimization/curve fitting, construct linkedList of datapoints and call anopther method
     *
     * @param datapoints - double[][]
     *      1. index = number of given datapoint
     *      2. index = 0 = x
     *                 1 = y
     *                 2 = weight (if exists), otherwise weight = 1
     * @return
     * @throws FunctionEvaluationException
     * @throws OptimizationException
     */
    public double[] optimizeParameters(double[][] datapoints) throws FunctionEvaluationException, OptimizationException{
        LinkedList<WeightedObservedPoint> datapointsLL = new LinkedList<WeightedObservedPoint>();

        for(int i=0; i<datapoints.length; i++){
            // check subarray
            if (datapoints[i]==null || datapoints[i].length<1 || datapoints[i].length>3){
                throw new IllegalArgumentException("Illegal datapoint argument, subarray problem on index: " + i);
            }

            // construct and fill with data
            WeightedObservedPoint tmp = new WeightedObservedPoint(
                    // for weight if exists
                    (datapoints[i].length>=3 ? datapoints[i][2] : 1),
                    // for x
                    datapoints[i][0],
                    // for y
                    datapoints[i][1]
                    );
            datapointsLL.add(tmp);
        }
        
        // observed points are filled, call another method to do optimization
        return this.optimizeParameters(datapointsLL);
    }

    /**
     * Sugar method, cleans all observation points already set
     */
    public void clearObservationPoints(){
        // clear all observation points
        this.getFitter().clearObservations();
    }

    /**
     * Get root mean square for AbstractLeastSquaresOptimizer
     * @return
     */
    public double getRMS() {
        if (this.optimizer instanceof AbstractLeastSquaresOptimizer){
            final AbstractLeastSquaresOptimizer tmpOpt = (AbstractLeastSquaresOptimizer) this.optimizer;
            return tmpOpt.getRMS();
        }
        else {
            return 0.0;
        }
    }



    /**
     * GETTERS + SETTERS
     */
    public CurveFitter getFitter() {
        return fitter;
    }

    public void setFitter(CurveFitter fitter) {
        this.fitter = fitter;
    }

    public RSSI2DistFunctionInterface getFunction() {
        return function;
    }

    public void setFunction(RSSI2DistFunctionInterface function) {
        this.function = function;
    }

    public DifferentiableMultivariateVectorialOptimizer getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(DifferentiableMultivariateVectorialOptimizer optimizer) {
        this.optimizer = optimizer;
    }

}
