/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.rssi.RSSI2DistLogNormalShadowing;
import org.apache.commons.math.random.RandomData;
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
public class RSSI2DistLogNormalShadowingTest {

    public RSSI2DistLogNormalShadowingTest() {
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
     * Test of getDistanceFromRSSI method, of class RSSI2DistLogNormalShadowingTest.
     * Inversion of function distance2RSSI, without random variable
     *
     * Test on 2 given sample data. If passed, function successfully computes distance
     * from 2 RSSI precomputed samples data with accuracy 1e-4
     */
    @Test
    public void testGetDistanceFromRSSI() {
        System.out.println("getDistanceFromRSSI");

        RSSI2DistLogNormalShadowing instance = new RSSI2DistLogNormalShadowing();
        double rssi = -25.53797084;
        double expResult = 1.40985068260977;
        instance.setPt(-15);
        instance.setPl(-23);
        instance.setNi(3.8);
        instance.setD0(3);
        instance.setStddev(0);

        double result = instance.getDistanceFromRSSI(rssi);
        assertEquals(expResult, result, 1e-4);

        // another sample of data
        rssi = -119.03388;
        expResult = 13652.9234910078;
        instance.setPt(-25);
        instance.setPl(-15);
        instance.setNi(2.3);
        instance.setD0(5);
        instance.setStddev(0);

        result = instance.getDistanceFromRSSI(rssi);
        assertEquals(expResult, result, 1e-4);
    }

    /**
     * Test of getRSSIFromDistance method, of class RSSI2DistLogNormalShadowing.
     * Test with manually precomputed results (2 samples)
     * Test random variable stddev
     */
    @Test
    public void testGetRSSIFromDistance_double_boolean() {
        System.out.println("getRSSIFromDistance");

        RSSI2DistLogNormalShadowing instance = new RSSI2DistLogNormalShadowing();
        double distance = 19.0;
        double expectedResult = -68.4620291568603;
        double stddev=0.5;
        instance.setPt(-15);
        instance.setPl(-23);
        instance.setNi(3.8);
        instance.setD0(3);
        instance.setStddev(stddev);

        double result = instance.getRSSIFromDistance(distance, false);
        assertEquals(expectedResult, result, 1e-5);

        // try 10 times, test random variable
        int randomVariableOk = 0;
        try {
            result = instance.getRSSIFromDistance(distance);
            for(int i=0; i<10; i++){
               result = instance.getRSSIFromDistance(distance, true);
               double delta = Math.abs(result - expectedResult);

               // test for standard deviation
               assertTrue("Random variable too big: " + delta, delta<2);

               // exact result, cannot continue
               if (delta < 1e5) continue;

               // good delta
               randomVariableOk += 1;
            }
        }
        catch(Exception e){
            fail("Exception thrown during rssi2distance. Error");
        }

        assertTrue("Random variable does not work", randomVariableOk == 0);

        // just another test with another parameters
        distance = 111.0;
        instance.setPt(-20);
        instance.setPl(-5);
        instance.setNi(2.3);
        instance.setD0(5);
        instance.setStddev(0);

        result = instance.getRSSIFromDistance(distance, false);
        assertEquals(-55.9661184123647, result, 1e-5);
    }

    /**
     * Test of value method, of class RSSI2DistLogNormalShadowing.
     * Since function is expected to return same results as GetRSSIFromDistance
     * tests are very similar.
     * + checking exception throwing on Illegal arguments
     */
    @Test
    public void testValue() throws Exception {
        System.out.println("value");

        RSSI2DistLogNormalShadowing instance = new RSSI2DistLogNormalShadowing();
        double distance = 19.0;
        double expectedResult = 71.53797;
        double stddev=0.5;
        double ni=3.8;

        // set another to test parameter usage
        instance.setNi(0.0);

        // another parameters set manualy
        instance.setPt(79);
        instance.setPl(23);
        instance.setD0(3);
        instance.setStddev(stddev);

        double result = instance.value(distance, new double[] {ni});
        assertEquals(expectedResult, result, 1e-5);

        // just another test with another parameters
        distance = 111.0;
        ni = 2.3;
        instance.setPt(179);
        instance.setPl(29);
        instance.setNi(0.0);
        instance.setD0(5);
        instance.setStddev(0);

        result = instance.value(distance, new double[] {ni});
        assertEquals(177.03388, result, 1e-5);

        // try false arguments
        try {
            result = instance.value(distance, null);
            fail("Should throw exception, given null as double param .value");
        }
        catch(Exception e){
            // normal behaviour
        }

        // try false arguments
        try {
            result = instance.value(distance, new double[] {});
            fail("Should throw exception, 0 parameters passed to .value");
        }
        catch(Exception e){
            // normal behaviour
        }

        // try false arguments
        try {
            result = instance.value(distance, new double[] {0,0,0});
            fail("Should throw exception, 3 parameters passed to .value");
        }
        catch(Exception e){
            // normal behaviour
        }  
    }

    /**
     * Test of gradient method, of class RSSI2DistLogNormalShadowing.
     * Should be first derivation of RSSIfromDistance, with respect to the parameters
     * (NI - path loss exponent)
     * (random variable not tested here, in time of writing test, random variable was not incorporated
     *  in gradient method)
     *
     * Test 2 samples data.
     * Test same data with various Ni parameter - should return same results since first derivation
     * does not contain Ni parameter
     *
     * 
     */
    @Test
    public void testGradient() throws Exception {
        System.out.println("gradient");

        RSSI2DistLogNormalShadowing instance = new RSSI2DistLogNormalShadowing();
        instance.setD0(1);

        double[] result = instance.gradient(19, new double[] {3.8});
        assertTrue("Result is null", result!=null);
        assertEquals("Result does not contain exactly 1 partial derivaton", result.length, 2);
        assertEquals(-12.78753601, result[0], 1e-6);

        // test with another parameter NI - should return same result since in first derivation
        // does not occurr NI
        result = instance.gradient(19, new double[] {2.1});
        assertTrue("Result is null", result!=null);
        assertEquals("Result does not contain exactly 1 partial derivaton", result.length, 2);
        assertEquals(-12.78753601, result[0], 1e-6);

        // another sample of data
        instance.setD0(3);
        result = instance.gradient(1013, new double[] {2.1});
        assertTrue("Result is null", result!=null);
        assertEquals("Result does not contain exactly 1 partial derivaton", result.length, 2);
        assertEquals(-25.28488191, result[0], 1e-6);
    }
}