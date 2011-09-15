/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssi_graph.game;
//package org.matheclipse.examples;

import org.matheclipse.core.eval.EvalUtilities;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.form.output.StringBufferWriter;
import org.matheclipse.core.interfaces.IExpr;
import edu.jas.kern.ComputerThreads;

/**
 *
 * @author ph4r05
 */
public class EnergyCalculator {
    
    EvalUtilities util = null;
    IExpr result = null;
    
    public EnergyCalculator() {
        F.initSymbols();
        util = new EvalUtilities();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            // Call terminate() only one time at the end of the program  
            ComputerThreads.terminate();
        } finally {
            super.finalize();
        }
    }
    
    public double getEnergy(double prevEnergy, double curLight, String expression){
        double newEnergy = 0.0;
        
        //
        //
        // compute energy here
        //
        //
        // Static initialization of the MathEclipse engine instead of null 
        // you can set a file name to overload the default initial
        // rules. This step should be called only once at program setup:
        try {
            // build string, define variables
            String finalExpression = "$prev=" + prevEnergy + "; $x=" +curLight + "; " + expression;
            StringBufferWriter buf = new StringBufferWriter();
            result = util.evaluate(finalExpression);
            
            OutputFormFactory.get().convert(buf, result);
            String output = buf.toString();
            
            newEnergy = Double.parseDouble(output);
            
            if (newEnergy < 0.0){
                newEnergy = 0.0;
            } 

            if (newEnergy > 100.0){
                newEnergy = 100.0;
            }
            
            System.err.println("Comnputed " + finalExpression + " is " + output + "; Energy: " + newEnergy);
        } catch (final Exception e) {
          e.printStackTrace();
        } finally {
          ;
        }
        
        return newEnergy;
    }
}
