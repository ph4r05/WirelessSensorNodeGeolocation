/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.rssi.RSSI2DistLogNormalShadowing;
import rssi_graph.rssi.RSSI2DistOptimizer;
import java.util.LinkedList;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.fitting.WeightedObservedPoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ph4r05
 */
public class RSSI2DistOptimizerTest {

    public RSSI2DistOptimizerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of optimizeParameters method, of class RSSI2DistOptimizer.
     * Test optimizer, search for NI best approximating given data points
     *
     * Test performed by plotting log normal shadowing model with own parameters (with NI)
     * and extracted 8 datapoints from graph.
     *
     * Trying to get NI back from datapoints
     */
    @Test
    public void testOptimizeParameters_LinkedList() throws Exception {
        System.out.println("optimizeParameters");
        
        RSSI2DistOptimizer instance = new RSSI2DistOptimizer();
        LinkedList<WeightedObservedPoint> datapoints = new LinkedList<WeightedObservedPoint>();
        RSSI2DistLogNormalShadowing function = (RSSI2DistLogNormalShadowing) instance.getFunction();

        // setup function details
        function.setD0(3);
        // optimizing for NI, set 1 as starting point
        function.setNi(1.0);
        function.setPt(79);
        function.setPl(23);
        // without random variable for now
        function.setStddev(0);

        // add 8 data points
        datapoints.add(new WeightedObservedPoint(1, 1.1, 70.8));
        datapoints.add(new WeightedObservedPoint(1, 4.7, 49.3));
        datapoints.add(new WeightedObservedPoint(1, 8.5, 40.6));
        datapoints.add(new WeightedObservedPoint(1, 15.3, 32));
        datapoints.add(new WeightedObservedPoint(1, 30.1, 21.9));
        datapoints.add(new WeightedObservedPoint(1, 40.0, 17.8));
        datapoints.add(new WeightedObservedPoint(1, 60.3, 11.7));
        datapoints.add(new WeightedObservedPoint(1, 90.0, 5.8));

        double[] result = instance.optimizeParameters(datapoints);
        assertEquals("Should return two parameters only", result.length, 2);
        assertEquals(3.4, result[0], 1e-1);
        System.err.println("Ni="+result[0]+"; const="+result[1]+"; RMS="+instance.getRMS()+"; formula="+function.toString());

        // =====================================================================
        // another curve fitting
        datapoints = new LinkedList<WeightedObservedPoint>();

        
        // setup function details
        function.setD0(1);
        // optimizing for NI, set 1 as starting point
        function.setNi(1.0);
        function.setPt(0);
        function.setPl(0);
        // without random variable for now
        function.setStddev(0);

        // add 8 data points
        datapoints.add(new WeightedObservedPoint(1, 0.9, 0.6));
        datapoints.add(new WeightedObservedPoint(1, 2.5, -7.1));
        datapoints.add(new WeightedObservedPoint(1, 5.2, -13));
        datapoints.add(new WeightedObservedPoint(1, 19.9, -23.4));
        datapoints.add(new WeightedObservedPoint(1, 39.3, -28.7));
        datapoints.add(new WeightedObservedPoint(1, 66, -32.8));
        datapoints.add(new WeightedObservedPoint(1, 120.3, -37.4));

        result = instance.optimizeParameters(datapoints);
        assertEquals("Should return two parameters only", result.length, 2);
        assertEquals(1.8, result[0], 1e-1);
        System.err.println("Ni="+result[0]+"; const="+result[1]+"; RMS="+instance.getRMS()+"; formula="+function.toString());
    }

    /**
     * Test of optimizeParameters method, of class RSSI2DistOptimizer.
     * Test with same data as testOptimizeParameters_LinkedList() does
     * but datapoints are passed as double[][]
     */
    @Test
    public void testOptimizeParameters_doubleArrArr() throws Exception {
        System.out.println("optimizeParameters");

        RSSI2DistOptimizer instance = new RSSI2DistOptimizer();
        RSSI2DistLogNormalShadowing function = (RSSI2DistLogNormalShadowing) instance.getFunction();
        double[][] datapoints = null;

        // setup function details
        function.setD0(3);
        // optimizing for NI, set 1 as starting point
        function.setNi(1.0);
        function.setPt(79);
        function.setPl(23);
        // without random variable for now
        function.setStddev(0);

        // add 8 data points
        datapoints = new double[][] {
                    {1.1, 70.8},
                    {4.7, 49.3},
                    {8.5, 40.6},
                    {15.3, 32},
                    {30.1, 21.9},
                    {90, 5.8},
                    {40, 17.8},
                    {60.3, 11.7}};

        double[] result = instance.optimizeParameters(datapoints);
        
        function.setNi(result[0]);
        if (result.length>1){
            function.setConstant(result[1]);
        }

        // test root mean square / standard deviation computation
        double rMS = instance.getRMS();
        // compute standard deviation by standard way: 1/n * SUM[rssi_predicted-rssi_real]^2
        double stddev = 0;
        double tmpSum = 0;
        for(int i=0, cnI=datapoints.length; i<cnI; i++){
            double tmpEst = function.getRSSIFromDistance(datapoints[i][0]);
            double tmpCur = datapoints[i][1];
            double tmpTest = (tmpEst - tmpCur) * (tmpEst - tmpCur);
            tmpSum += tmpTest*tmpTest;
        }
        stddev = tmpSum / datapoints.length;
        
        System.err.println("Ni="+result[0]+
                "; const="+result[1]+
                "; RMS="+instance.getRMS()+
                "; stddev="+ stddev +
                "; rms^2-stddev="+(rMS*rMS-stddev)+
                " formula="+function.toString());
        assertEquals("Should return two parameter only", result.length, 2);
        assertEquals(3.4, result[0], 1e-1);
        assertEquals(stddev, rMS*rMS, 1e-2);
        

        // =====================================================================
        // another curve fitting
        // setup function details
        function.setD0(1);
        // optimizing for NI, set 1 as starting point
        function.setNi(1.0);
        function.setPt(0);
        function.setPl(0);
        // without random variable for now
        function.setStddev(0);

        // add 8 data points
        datapoints = new double[][] {
                    {0.9, 0.6},
                    {2.5, -7.1},
                    {5.2, -13},
                    {19.9, -23.4},
                    {39.3, -28.7},
                    {66, -32.8},
                    {120.3, -37.4}};
        
        result = instance.optimizeParameters(datapoints);

        function.setNi(result[0]);
        if (result.length>1){
            function.setConstant(result[1]);
        }

        // test root mean square / standard deviation computation
        rMS = instance.getRMS();
        // compute standard deviation by standard way: 1/n * SUM[rssi_predicted-rssi_real]^2
        stddev = 0;
        tmpSum = 0;
        for(int i=0, cnI=datapoints.length; i<cnI; i++){
            double tmpEst = function.getRSSIFromDistance(datapoints[i][0]);
            double tmpCur = datapoints[i][1];
            double tmpTest = (tmpEst - tmpCur) * (tmpEst - tmpCur);
            tmpSum += tmpTest*tmpTest;
        }
        stddev = tmpSum / datapoints.length;

        System.err.println("Ni="+result[0]+
                "; const="+result[1]+
                "; RMS="+instance.getRMS()+
                "; stddev="+ stddev +
                "; rms^2-stddev="+(rMS*rMS-stddev)+
                " formula="+function.toString());
        
        assertEquals("Should return two parameter only", result.length, 2);
        assertEquals(1.8, result[0], 1e-1);
        assertEquals(stddev, rMS*rMS, 1e-2);

        
    }
}