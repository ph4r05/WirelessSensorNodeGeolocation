/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi;
import java.util.Map;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.jfree.data.function.Function2D;

/**
 * Default interface for propagation model function
 * Uses as distance estimator during localization, for curve fitting problem in
 * environment propagation model calibration.
 *
 * @todo: specify methods to get parameter names in user friendly format(to be displayed in table)
 * + parameter tooltips(longer description)
 *
 * @author ph4r05
 */
public interface RSSI2DistFunctionInterface extends ParametricRealFunction, Function2D {
    /**
     * Get inverse of gain formula, return distance from given RSSI,
     * random variables and errors are not incorporated yet
     *
     * @param rssi
     * @return distance
     */
    public double getDistanceFromRSSI(double rssi);

    /**
     * Get gain from distance, should be inverse to getDistanceFromRSSI
     *
     * @param distance
     * @param addRandomVariable - if TRUE add random variable with defined properties ~ N(0,sttdev)
     * @return
     */
    public double getRSSIFromDistance(double distance, boolean addRandomVariable);

    /**
     * Get gain from distance
     * simplified parameters
     *
     * @param distance
     * @return
     */
    public double getRSSIFromDistance(double distance);

    /**
     * Compute expectation (E[x], middle value) of estimated distance for given RSSI
     * Works for statistical models. When cannot compute, return
     * @param rssi
     * @return
     */
    public double getExpectedDistanceEstimateFromRSSI(double rssi);

    /**
     * Compute variance (VAR[x]) of estimated distance for given RSSI.
     * Works for statistical models. When cannot compute, return this.getDistanceFromRSSI(rssi);
     *
     * @param rssi
     * @return
     */
    public double getVarianceDistanceEstimateFromRSSI(double rssi);

    /**
     * ParametricRealFunction for curve fitting problem.
     * Parametrized function by parameters:
     *  [0] = 1st
     *  [1] = 2ns
     *
     * Another parameters can be static and set at the beginning.
     * Optimizer optimizes parameters.
     *
     * {@inheritDoc}
     * @param x
     * @param parameters
     * @return
     * @throws FunctionEvaluationException
     */
    public double value(double x, double[] parameters) throws FunctionEvaluationException;

    /**
     * ParametricRealFunction for curve fitting problem.
     * Returns first derivation of getRSSIFromDistance with respect to the parameters
     *
     * {@inheritDoc}
     * @param x
     * @param parameters
     * @return
     * @throws FunctionEvaluationException
     */
    public double[] gradient(double x, double[] parameters) throws FunctionEvaluationException;

    /**
     * To string should return formula with defined parameters filled in.
     *
     * @return
     */
    @Override
    public String toString();

    /**
     * Returns formula values in human readable format.
     * Numbers are rounded with given precision
     *
     * @param decimals = number of decimal places for each double value. 0=no decimal place=integer
     * @return
     */
    public String toStringHuman(int decimals);

    /**
     * Returns formula name
     * @return
     */
    public String getFormulaName();

    /**
     * Returns formula with parameter names. (formula for RSSI from distance)
     * @return
     */
    public String getFormula();

    /**
     * Returns formula for function inversion (formula for distance with respect to the RSSI)
     * @return
     */
    public String getFormulaInversion();

    /**
     * Setting parameters with configuration stored in map.
     * String - parameter name
     * Double - parameter value
     *
     * @param parameters
     * @return
     */
    public boolean setParameters(Map<String, Double> parameters);

    /**
     * Returns map with parameter names and current value
     * @return
     */
    public Map<String, Double> getParameters();

    /**
     * Set particular parameter for function
     *
     * @param paramName
     * @param paramValue
     * @return
     */
    public boolean setParameter(String paramName, Double paramValue);

    /**
     * Returns current value of particular parameter
     * @param paramName
     * @return
     */
    public Double getParameter(String paramName);

    /**
     * Root mean square after optimization process.
     * (When optimizing with least squares method. Otherwise undefined/0)
     * @return
     */
    public double getRms();
    public void setRms(double rms);

}
