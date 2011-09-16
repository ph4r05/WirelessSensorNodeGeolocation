/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.localization.LocalizationNllsLM;
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
public class LocalizationNllsLMTest {

    public LocalizationNllsLMTest() {
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
     * Test of getPosition method, of class LocalizationNllsLM.
     * Test with given position, single intersection of 3 circles
     * Optimize with another initialPoint, if passed, trilateration works
     */
    @Test
    public void testGetPosition_0args() throws Exception {
        System.out.println("getPosition");
        LocalizationNllsLM instance = new LocalizationNllsLM();

        // expected result of trilateration
        double[] expResult = new double[] {2,1};

        // initialize function
        instance.getFunction().setDistances(new double[] {1,1,1});
        instance.getFunction().setApplicableDistances(new boolean[] {true,true,true});
        instance.getFunction().setPositions(new double[][] { {1,1}, {3,1}, {2,2} });

        // setup algorithm
        //instance.getLmOptimizer().
        double[] testPoint = new double[] {0,0};


        // compute result
        double[] result = instance.getPosition(testPoint);
        System.out.println("Evals=" + instance.getLmOptimizer().getEvaluations()
                    + "; iter=" + instance.getLmOptimizer().getIterations()
                    + "; jacobEval=" + instance.getLmOptimizer().getJacobianEvaluations()
                    + "; RMS=" + instance.getLmOptimizer().getRMS()
                    + "; chiSquare=" + instance.getLmOptimizer().getChiSquare());

        assertEquals(expResult[0], result[0], 0.005);
        assertEquals(expResult[1], result[1], 0.005);
    }

    @Test
    public void testGetMyPosition_0args() throws Exception {
        System.out.println("getPosition");
        LocalizationNllsLM instance = new LocalizationNllsLM();

        // expected result of trilateration
        double[] expResult = new double[] {4.376,-2.789};

        // initialize function
        instance.getFunction().setDistances(new double[] {5.9922616193,10.159729599,16.545413898,9.0237586109});
        instance.getFunction().setApplicableDistances(new boolean[] {true,true,false,true});
        instance.getFunction().setPositions(new double[][] { {0.0,0.0}, {10.1,5.45}, {10.1,0.0}, {0,5.45} });

        // setup algorithm
        //instance.getLmOptimizer().
        double[] testPoint = new double[] {0,0};


        // compute result
        double[] result = instance.getPosition(testPoint);
        System.out.println("#Evals=" + instance.getLmOptimizer().getEvaluations()
                    + "; iter=" + instance.getLmOptimizer().getIterations()
                    + "; jacobEval=" + instance.getLmOptimizer().getJacobianEvaluations()
                    + "; RMS=" + instance.getLmOptimizer().getRMS()
                    + "; chiSquare=" + instance.getLmOptimizer().getChiSquare());
        System.out.println("Result, x="+result[0]+"; y="+result[1]);
        assertEquals(expResult[0], result[0], 1.0);
        assertEquals(expResult[1], result[1], 1.0);
    }
}