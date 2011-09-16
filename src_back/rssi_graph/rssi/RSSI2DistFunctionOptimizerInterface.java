/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;

import java.util.LinkedList;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.WeightedObservedPoint;

/**
 * Interface for each function optimizer for RSSI->Distance relation
 *
 * @author ph4r05
 */
public interface RSSI2DistFunctionOptimizerInterface {

    /**
     * Optimization/curve fitting
     *
     * @param datapoints
     * @return
     */
    public double[] optimizeParameters(LinkedList<WeightedObservedPoint> datapoints) throws FunctionEvaluationException, OptimizationException;

    /**
     * Optimization/curve fitting, construct linkedList of datapoints and call another method
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
    public double[] optimizeParameters(double[][] datapoints) throws FunctionEvaluationException, OptimizationException;

    /**
     * Sugar method, cleans all observation points already set
     */
    public void clearObservationPoints();

    /**
     * Get root mean square after optimization, if uses DifferentiableMultivariateVectorialOptimizer
     * @return
     */
    public double getRMS();

    /**
     * GETTERS + SETTERS
     */
    public CurveFitter getFitter();

    public void setFitter(CurveFitter fitter);

    public RSSI2DistFunctionInterface getFunction();

    public void setFunction(RSSI2DistFunctionInterface function);

    public DifferentiableMultivariateVectorialOptimizer getOptimizer();

    public void setOptimizer(DifferentiableMultivariateVectorialOptimizer optimizer);
}
