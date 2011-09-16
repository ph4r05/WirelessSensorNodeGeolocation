/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_localization.localization;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

/**
 * Trilateration problem solver
 * Solves with nonlinear least squares with LevenbergMarquardtOptimizer
 *
 * @author ph4r05
 */
public class LocalizationNllsLM {

    /**
     * optimizer
     */
    private LevenbergMarquardtOptimizer lmOptimizer = null;

    /**
     * function to be optimized
     */
    private LocalizationNllsFunction function = null;

    public LocalizationNllsLM() {
        // initialize optimizer
        lmOptimizer = new LevenbergMarquardtOptimizer();

        // initialize function-to-be-optimized. It performs some maintenance during
        // init so it is faster to have initialized function here. It has own internal state
        function = new LocalizationNllsFunction();
    }

    /**
     * calculates position with trilateration with nonlinear least squares with LM method
     * all parameters needs to be set to function directly before calling this method
     *
     * Maybe later another interface will be added to be able to setup function directly from this class
     * ∑weighti(objectivei-targeti)2
     * 
     * (wrapper/adapter)
     * @param target
     * @param weights
     * @param startPoint
     * @return
     */
    public double[] getPosition(double[] target, double[] weights, double[] startPoint) throws FunctionEvaluationException, OptimizationException{
        VectorialPointValuePair vpvp = lmOptimizer.optimize(function, target, weights, startPoint);
        return vpvp.getPointRef();
    }

    /**
     * Parameterless getPosition. It will fill parameters in automatically.
     * 
     * @return
     * @throws FunctionEvaluationException
     * @throws OptimizationException
     */
    public double[] getPosition(double[] initialPoint) throws FunctionEvaluationException, OptimizationException{
        // init target array to 0. optimize to 0
        double[] target = new double[function.getPositions().length];
        double[] weights = new double[target.length];
        for(int i=0; i<target.length; i++){
            target[i]=0.0;
            weights[i]=1.0;
        }

        VectorialPointValuePair vpvp = lmOptimizer.optimize(
                // function to be optimized
                function,
                // target values at optimal point in least square equation
                // (x0+xi)^2 + (y0+yi)^2 + ri^2 = target[i]
                target,
                // set uniform weights
                weights,
                // where to start with optimization
                // at constant, [0,0]
                initialPoint);
        
        return vpvp.getPoint();
    }

    public double[] getPosition() throws FunctionEvaluationException, OptimizationException{
        return this.getPosition(new double[] {0,0});
    }

    public LevenbergMarquardtOptimizer getLmOptimizer() {
        return lmOptimizer;
    }

    public void setLmOptimizer(LevenbergMarquardtOptimizer lmOptimizer) {
        this.lmOptimizer = lmOptimizer;
    }

    public LocalizationNllsFunction getFunction() {
        return function;
    }

    public void setFunction(LocalizationNllsFunction function) {
        this.function = function;
    }

    
}
