package rssi_graph.rssi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.fitting.ParametricRealFunction;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import rssi_graph.rssi.RSSI2DistFunctionInterface;

/**
 * RSSI to distance mapper
 * should be able to use multiple radio propagation & attenuation models
 * 
 * @author ph4r05
 */
public class RSSI2DistLogNormalShadowing implements RSSI2DistFunctionInterface, ParametricRealFunction {

    /**
     * tx power (in Db)
     */
    private double pt=0;

    /**
     * the path loss for the reference distance d0 (in dB)
     */
    private double pl=30.1;

    /**
     * reference distance (in meters)
     */
    private double d0=1;

    /**
     * path loss exponent
     */
    private double ni=3.3;

    /**
     * stddev of gaussian zero mean white noise
     * meassurement errors
     */
    private double stddev=0;

    /**
     * Constant to mitigate mote specific constant properties
     */
    private double constant=0;

    /**
     * RandomDataImpl from apache commons to generate random variable with Gaussian
     * distribution (added to gain formula as error term)
     */
     private RandomData rndData = null;

     /**
      * Root mean square after optimization with least squares method.
      * Determines quality of fitting / data sample regularity
      */
     private double rms=0.0;

    public RSSI2DistLogNormalShadowing() {
        this.rndData = new RandomDataImpl();
    }
    
    /**
     * Works with Gauss zero-mean random variable N(0, stddev) to compute expectation
     * of estimated distance.
     *
     *  Source: title={{Position location techniques and applications}},
     *           author={Mu{\~n}oz, D. and Vargas, C.},
     *           isbn={9780123743534},
     *           url={http://books.google.com/books?id=2sWdiZainyoC},
     *           page=61
     *
     *
     * @param rssi
     * @return
     */
    public double getExpectedDistanceEstimateFromRSSI(double rssi){
        return this.getDistanceFromRSSI(rssi) * 
                Math.exp(this.getStddev()/
                    (200*this.getNi()*this.getNi()
                        *Math.log10(Math.E)*Math.log10(Math.E)));
    }

    /**
     * Works with Gauss zero-mean random variable N(0, stddev) to compute expectation
     * of estimated distance.
     *
     *  Source: title={{Position location techniques and applications}},
     *           author={Mu{\~n}oz, D. and Vargas, C.},
     *           isbn={9780123743534},
     *           url={http://books.google.com/books?id=2sWdiZainyoC},
     *           page=61
     *
     *
     * @param rssi
     * @return
     */
    public double getVarianceDistanceEstimateFromRSSI(double rssi){
        double d = this.getDistanceFromRSSI(rssi);
        return d*d*(
                Math.exp(this.stddev/(50*this.getNi()*this.getNi()
                        *Math.log10(Math.E)*Math.log10(Math.E)))
                -
                Math.exp(this.stddev/(100*this.getNi()*this.getNi()
                        *Math.log10(Math.E)*Math.log10(Math.E)))
                );
    }

    /**
     * Log normal shadowing propagation model
     * Get inverse of gain formula, return distance from given RSSI,
     * random variables and errors are not incorporated yet
     *
     * @param rssi
     * @return distance
     */
    public double getDistanceFromRSSI(double rssi){
        return getD0() * Math.pow(10, (-1*rssi + this.getPt() + this.getPl() + this.getConstant())/(10*this.getNi()) );
        //return getD0() * Math.pow(10, (-1*rssi + this.getPt() + this.getPl())/(10*this.getNi()) );
    }

    /**
     * Get gain from distance, should be inverse to getDistanceFromRSSI
     *
     * @param distance
     * @param addRandomVariable - if TRUE add random variable with defined properties ~ N(0,sttdev)
     * @return
     */
    public double getRSSIFromDistance(double distance, boolean addRandomVariable){
        return 
                // signal TX power
                this.getPt() +
                // attenuation at reference distance
                +1*this.getPl() +
                // main term for signal attenuation with distance
                -10 * this.getNi() * Math.log10(distance/this.getD0()) +
                // error random variable, zero mean Gaussian, add only if wanted
                (addRandomVariable ? this.getRndData().nextGaussian(0, this.getStddev()) : 0)
                //constant
                + this.getConstant()
                ;
    }

    /**
     * Get gain from distance
     * simplified parameters, omit random variable
     *
     * @param distance
     * @return
     */
    public double getRSSIFromDistance(double distance){
        return this.getRSSIFromDistance(distance, false);
    }

    /**
     * Function2D implementation for jfreeChart
     * @param x
     * @return
     */
    public double getValue(double x) {
        return this.getRSSIFromDistance(x, false);
    }

    /**
     * Implements ParametricRealFunction for curve fitting problem.
     * Parametrized function by parameters:
     *  [0] = ni, path loss exponent
     *  [1] = constant
     *  [2] = error stddev
     *
     * Another parameters are static and set at the beginning 
     * (txpower, reference distance, attenuation at reference distance)
     *
     * Optimizer optimizes parameters. Now we cannot optimize error
     *
     * {@inheritDoc}
     * @param x
     * @param parameters
     * @return
     * @throws FunctionEvaluationException
     */
    public double value(double x, double[] parameters) throws FunctionEvaluationException {
        // check parameters length
        if (parameters == null || parameters.length < 1 || parameters.length > 2) {
            throw new IllegalArgumentException("Illegal parameters data");
        }
        
        // set parameters, ni
        this.setNi(parameters[0]);
        this.setConstant(parameters.length>1 ? parameters[1] : 0);
        this.setStddev(parameters.length>2 ? parameters[2] : 0);
        return this.getRSSIFromDistance(x, false);
    }

    /**
     * Implements ParametricRealFunction for curve fitting problem.
     * Returns first derivation of getRSSIFromDistance with respect to the parameters
     * 
     * {@inheritDoc}
     * @param x
     * @param parameters
     * @return
     * @throws FunctionEvaluationException
     */
    public double[] gradient(double x, double[] parameters) throws FunctionEvaluationException {
        // check parameters length
        if (parameters == null || parameters.length < 1 || parameters.length > 2) {
            throw new IllegalArgumentException("Illegal parameters data");
        }

        // I cannot compute first derivate of random variable. For now it is unsupported
        if (parameters.length>=3){
            throw new UnsupportedOperationException("Cannot compute gradient for random variable, unsupported for now");
        }

        // set parameters, ni
        this.setNi(parameters[0]);
        this.setConstant(parameters.length > 1 ? parameters[1] : 0);

        double[] result = new double[2];

        // first derivate with respect to the Ni parameter (path loss exponent)
        result[0] = -10*Math.log10(x/this.getD0());
        result[1] = 1;
        return result;
    }

    /**
     * Write signal attenuation with distance formula with current parameters
     * @return
     */
    @Override
    public String toString() {
        return "RSSI(d)=" + this.getPt() 
                + (this.getPl() < 0 ? " ":" + ") + this.getPl()
                + " -10*" + this.getNi() + "*log10(d/" + this.getD0() + ")"
                + (this.getConstant() < 0 ? " ":" + ") + this.getConstant()
                + " + X[0,"+this.getStddev()+"]";
    }

    /**
     * Returns formula values in human readable format.
     * Numbers are rounded with given precision
     *
     * @param decimals = number of decimal places for each double value. 0=no decimal place=integer
     * @return
     */
    public String toStringHuman(int decimals){
        double roundMultiplicator = Math.pow(10, decimals);
        return "RSSI(d)=" + (Math.round(this.getPt() * roundMultiplicator) / roundMultiplicator)
                + (this.getPl() < 0 ? " ":" + ") + (Math.round(this.getPl() * roundMultiplicator) / roundMultiplicator)
                + " -10*" + (Math.round(this.getNi() * roundMultiplicator) / roundMultiplicator) + "*log10(d/" + (Math.round(this.getD0() * roundMultiplicator) / roundMultiplicator) + ")"
                + (this.getConstant() < 0 ? " ":" + ") + (Math.round(this.getConstant() * roundMultiplicator) / roundMultiplicator)
                + " + X[0,"+(Math.round(this.getStddev() * roundMultiplicator) / roundMultiplicator)+"]";
    }

    public String getFormula() {
        return "RSSI(d) = Pt + Pl(d0) - 10*Ni*log(d/d0)/log(10) + Xq + constant";
    }

    public String getFormulaInversion() {
        return "Distance(rssi) = d0 * 10^((-rssi + Pt + Pl(d0) + constant)/(10*Ni))";
    }

    public String getFormulaName(){
        return "Log normal shadowing";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RSSI2DistLogNormalShadowing other = (RSSI2DistLogNormalShadowing) obj;
        if (Double.doubleToLongBits(this.pt) != Double.doubleToLongBits(other.pt)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pl) != Double.doubleToLongBits(other.pl)) {
            return false;
        }
        if (Double.doubleToLongBits(this.d0) != Double.doubleToLongBits(other.d0)) {
            return false;
        }
        if (Double.doubleToLongBits(this.ni) != Double.doubleToLongBits(other.ni)) {
            return false;
        }
        if (Double.doubleToLongBits(this.stddev) != Double.doubleToLongBits(other.stddev)) {
            return false;
        }
        if (Double.doubleToLongBits(this.constant) != Double.doubleToLongBits(other.constant)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.pt) ^ (Double.doubleToLongBits(this.pt) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.pl) ^ (Double.doubleToLongBits(this.pl) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.d0) ^ (Double.doubleToLongBits(this.d0) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.ni) ^ (Double.doubleToLongBits(this.ni) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.stddev) ^ (Double.doubleToLongBits(this.stddev) >>> 32));
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.constant) ^ (Double.doubleToLongBits(this.constant) >>> 32));
        return hash;
    }

    public boolean setParameter(String paramName, Double paramValue){
        if ("constant".equalsIgnoreCase(paramName)){
            this.setConstant(paramValue);
        }
        else if("exponent".equalsIgnoreCase(paramName)){
            this.setNi(paramValue);
        }
        else if ("pt".equalsIgnoreCase(paramName)){
            this.setPt(paramValue);
        }
        else if ("pl".equalsIgnoreCase(paramName)){
            this.setPl(paramValue);
        }
        else if ("d0".equalsIgnoreCase(paramName)){
            this.setD0(paramValue);
        }
        else {
            return false;
        }

        return true;
    }

    public boolean setParameters(Map<String, Double> parameters) {
        if (parameters==null){
            throw new NullPointerException("Parameters map is null!");
        }

        Set<String> keySet = parameters.keySet();
        Iterator<String> it = keySet.iterator();
        while(it.hasNext()){
            String curKey = it.next();
            this.setParameter(curKey, parameters.get(curKey));
        }

        return true;
    }

    public Double getParameter(String paramName){
        if ("constant".equalsIgnoreCase(paramName)){
            return this.getConstant();
        }
        else if("exponent".equalsIgnoreCase(paramName)){
            return this.getNi();
        }
        else if ("pt".equalsIgnoreCase(paramName)){
            return this.getPt();
        }
        else if ("pl".equalsIgnoreCase(paramName)){
            return this.getPl();
        }
        else if ("d0".equalsIgnoreCase(paramName)){
            return this.getD0();
        }
        else {
            return null;
        }
    }

    public Map<String, Double> getParameters() {
        Map<String, Double> parameters = new HashMap<String, Double>();
        parameters.put("constant", this.getConstant());
        parameters.put("exponent", this.getNi());
        parameters.put("pt", this.getPt());
        parameters.put("pl", this.getPl());
        parameters.put("d0", this.getD0());
        return parameters;
    }

    /**
     * GETTERS+SETTERS below
     */

    public double getD0() {
        return d0;
    }

    public void setD0(double d0) {
        this.d0 = d0;
    }

    public double getNi() {
        return ni;
    }

    public void setNi(double ni) {
        this.ni = ni;
    }

    public double getPl() {
        return pl;
    }

    public void setPl(double pl) {
        this.pl = pl;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public double getStddev() {
        return stddev;
    }

    public void setStddev(double stddev) {
        this.stddev = stddev;
    }

    public RandomData getRndData() {
        return rndData;
    }

    public void setRndData(RandomData rndData) {
        this.rndData = rndData;
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    public double getRms() {
        return rms;
    }

    public void setRms(double rms) {
        this.rms = rms;
    }
    
}
