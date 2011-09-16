/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph;

import rssi_graph.localization.LocalizationNllsFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ph4r05
 */
public class LocalizationNllsFunctionTest {

    public LocalizationNllsFunctionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of jacobian method, of class LocalizationNllsFunction.
     */
    @Test
    public void testJacobian() {
        try {
            System.out.println("jacobian");
            LocalizationNllsFunction instance = new LocalizationNllsFunction();
            // initialize function
            instance.setDistances(new double[]{1, 1, 1});
            instance.setApplicableDistances(new boolean[]{true, true, true});
            instance.setPositions(new double[][]{{1, 1}, {3, 1}, {2, 2}});
            // test point {2,1} - intersection
            double[] point = new double[]{2, 1};
            double[][] result = instance.jacobian().value(point);
            
            // dimmension checks
            assertEquals(result.length, 3);
            assertEquals(result[0].length, 2);
            assertEquals(result[1].length, 2);
            assertEquals(result[2].length, 2);

            // jacobian check
            assertEquals(result[0][0], 2, 0.005);
            assertEquals(result[0][1], 0, 0.005);
            assertEquals(result[1][0], -2, 0.005);
            assertEquals(result[1][1], 0, 0.005);
            assertEquals(result[2][0], 0, 0.005);
            assertEquals(result[2][1], -2, 0.005);

            // another test point
            point = new double[]{3, 3};
            result = instance.jacobian().value(point);
            // dimmension checks
            assertEquals(result.length, 3);
            assertEquals(result[0].length, 2);
            assertEquals(result[1].length, 2);
            assertEquals(result[2].length, 2);

            // jacobian check
            assertEquals(result[0][0], 4, 0.005);
            assertEquals(result[0][1], 4, 0.005);
            assertEquals(result[1][0], 0, 0.005);
            assertEquals(result[1][1], 4, 0.005);
            assertEquals(result[2][0], 2, 0.005);
            assertEquals(result[2][1], 2, 0.005);
        } catch (FunctionEvaluationException ex) {
            fail("Should not get here");
        } catch (IllegalArgumentException ex) {
            fail("Should not get here");
        }
    }

    /**
     * Test of value method, of class LocalizationNllsFunction.
     */
    @Test
    public void testValue() throws Exception {
        System.out.println("value");
        
        LocalizationNllsFunction instance = new LocalizationNllsFunction();

        // initialize function
        instance.setDistances(new double[] {1,1,1});
        instance.setApplicableDistances(new boolean[] {true,true,true});
        instance.setPositions(new double[][] { {1,1}, {3,1}, {2,2} });

        // test point {2,1} - intersection
        double[] point = new double[] {2,1};
        double[] result = instance.value(point);
        assertEquals(result[0], 0, 0.005);
        assertEquals(result[1], 0, 0.005);
        assertEquals(result[2], 0, 0.005);

        // another test point
        point = new double[] {3,3};
        result = instance.value(point);
        assertEquals(result[0], 7, 0.005);
        assertEquals(result[1], 3, 0.005);
        assertEquals(result[2], 1, 0.005);
    }

    /**
     * Test of computeNumOfApplicableDistances method, of class LocalizationNllsFunction.
     */
    @Test
    public void testComputeNumOfApplicableDistances() {
        System.out.println("computeNumOfApplicableDistances");
        LocalizationNllsFunction instance = new LocalizationNllsFunction();

        instance.setDistances(new double[] {0,0,0,0,0});

        try {
            // set 3 applicable distances
            instance.setApplicableDistances(new boolean[] {true,true,false,false,true});
            assertEquals(instance.getNumOfApplicableDistances(), 3);

            instance.setApplicableDistances(new boolean[] {false,false,false,false,false});
            assertEquals(instance.getNumOfApplicableDistances(), 0);

            instance.setApplicableDistances(new boolean[] {true,true,true,true,true});
            assertEquals(instance.getNumOfApplicableDistances(), 5);
        }
        catch(Exception e){
            fail("Should not throw exception");
        }

        try {
            instance.setApplicableDistances(new boolean[] {true});
            fail("Should throw exception");
        }
        catch(IllegalArgumentException e){
            // normal behaviour
        }
    }

//    /**
//     * Test of getDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testGetDistances() {
//        System.out.println("getDistances");
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        double[] expResult = null;
//        double[] result = instance.getDistances();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testSetDistances() {
//        System.out.println("setDistances");
//        double[] distances = null;
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        instance.setDistances(distances);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPositions method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testGetPositions() {
//        System.out.println("getPositions");
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        double[][] expResult = null;
//        double[][] result = instance.getPositions();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPositions method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testSetPositions() {
//        System.out.println("setPositions");
//        double[][] positions = null;
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        instance.setPositions(positions);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getApplicableDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testGetApplicableDistances() {
//        System.out.println("getApplicableDistances");
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        boolean[] expResult = null;
//        boolean[] result = instance.getApplicableDistances();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setApplicableDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testSetApplicableDistances() {
//        System.out.println("setApplicableDistances");
//        boolean[] applicableDistances = null;
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        instance.setApplicableDistances(applicableDistances);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNumOfApplicableDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testGetNumOfApplicableDistances() {
//        System.out.println("getNumOfApplicableDistances");
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        int expResult = 0;
//        int result = instance.getNumOfApplicableDistances();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setNumOfApplicableDistances method, of class LocalizationNllsFunction.
//     */
//    @Test
//    public void testSetNumOfApplicableDistances() {
//        System.out.println("setNumOfApplicableDistances");
//        int numOfApplicableDistances = 0;
//        LocalizationNllsFunction instance = new LocalizationNllsFunction();
//        instance.setNumOfApplicableDistances(numOfApplicableDistances);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}