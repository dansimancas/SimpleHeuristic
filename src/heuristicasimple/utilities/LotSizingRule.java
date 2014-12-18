/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package heuristicasimple.utilities;

import java.util.*;
import heuristicasimple.dataStructures.*;
import heursiticasimple.rules.*;

/**
 *
 * @author Research
 */
public class LotSizingRule {
    public LotSizingRule(){};
    private double eoq;
    private double[] eoqs;
    
    public double getEOQ(){
        return eoq;
    }
    public double[] getEOQs(){
        return eoqs;
    }
    public int[][] L4L(int[][] requirement){
        int[][] ProductionPlan = requirement;
        return ProductionPlan;
    }
    public double[][] L4L(double[][] requirement){
        double[][] ProductionPlan = requirement;
        return ProductionPlan;
    }
    public int[] L4L(int[] requirement){
        int[] ProductionPlan = requirement;
        return ProductionPlan;
    }
    public double[] L4L(double[] requirement){
        double[] ProductionPlan = requirement;
        return ProductionPlan;
    }
    public double[][] FOQ(double[][] requirement, double quantity){

        int end_items = requirement.length;
        int period_length = requirement[0].length;
        double[][] temp = new double[end_items][period_length];
        double[][] mps = new double[end_items][period_length];

        for(int k = 0; k < end_items; k++){

            int first_request=0;

            for(int m = 0; m < period_length; m++){
                if(requirement[k][m] > 0){
                    mps[k][m] = quantity;
                    first_request = m;
                    m = period_length;
                }
            }

            temp[k][first_request] = mps[k][first_request] - requirement[k][first_request];

            for(int m = first_request+1; m < period_length; m++){
                temp[k][m] = temp[k][m-1] - requirement[k][m];
                if(temp[k][m] >= 0){
                    mps[k][m] = 0;
                }
                else {
                    mps[k][m] = quantity;
                    temp[k][m] = temp[k][m-1] + mps[k][m] - requirement[k][m];
                }
            }
        }

        return mps;
    }
    public double[] FOQ(double[] requirement, double quantity){

        int period_length = requirement.length;
        double[] temp = new double[period_length];
        double[] mps = new double[period_length];

        int first_request=0;

        for(int m = 0; m < period_length; m++){
            if(requirement[m] > 0.0){
                mps[m] = quantity;
                first_request = m;
                m = period_length;
            }
        }

        temp[first_request] = mps[first_request] - requirement[first_request];

        for(int m = first_request+1; m < period_length; m++){
            temp[m] = temp[m-1] - requirement[m];
            if(temp[m] >= 0){
                mps[m] = 0;
            }
            else {
                mps[m] = quantity;
                temp[m] = temp[m-1] + mps[m] - requirement[m];
            }
        }

        return mps;
    }
    public double[] EOQ(int productKey, double[] requirement, double hc, double[][] output, double[] sc){
        /*
        demand: annual demand, in units per year
        holding: cost of holding one unit in inventory for a year, often empressed as a percentage of the item's value. (Not this case)
        setup: cost of ordering or setting up one lot, in dollars per lot. (Here I make an average of the estimated accumulated setup cost)
        */
        int period = 0;            

        for(int i=0;i < requirement.length;i++){
            if(requirement[i]>0) period++;
        }

        double demand=0;

        for(int i=0;i<requirement.length;i++){
            demand += requirement[i];
        }
        double holding = hc;
        List<Integer> strokes = new ArrayList();
        for(int i=0;i<output[0].length;i++){
            if(output[productKey-1][i]>0) {
                strokes.add(i); // se asume que la output es una matriz de unos y ceros
            }
        }
        double addition=0;
        for(Integer strokes1:strokes){
            addition += sc[strokes1];
        }
        double setup = addition/strokes.size();

        RoundDouble roundedDouble = new RoundDouble();
        eoq = roundedDouble.roundHalfEven(Math.sqrt((2.0*demand*setup)/holding),2);

        return FOQ(requirement, eoq);   
    }
    public double[][] EOQ(double[][] requirement, double[] hc, double[][] output, double[] sc){
        /*
        demand: annual demand, in units per year
        holding: cost of holding one unit in inventory for a year, often empressed as a percentage of the item's value. (Not this case)
        setup: cost of ordering or setting up one lot, in dollars per lot. (Here I make an average of the estimated accumulated setup cost)
        */
        int products=requirement.length;

        int[] period = new int[products];            

        for(int j=0;j<products;j++){
            for(int i=0;i < requirement[0].length;i++){
                if(requirement[j][i]>0) period[j]++;
            }
        }


        double[] demand = new double[products];
        for(int j=0;j<products;j++){
            for(int i=0;i<requirement[0].length;i++){
            demand[j] += requirement[j][i];
            }
        }

        double[] holding = new double[products];
        for(int j=0;j<products;j++){
            holding[j] = hc[j];
        }

        List<List<Integer>> strokes = new ArrayList();
        for(int j=0;j<products;j++){
            List<Integer> thisproduct = new ArrayList();
            for(int i=0;i<output[0].length;i++){
                if(output[j][i]>0) {
                    thisproduct.add(i);
                }
            }
            strokes.add(thisproduct);
        }

        double[] addition= new double[products];

        for(int m =0;m<products;m++){
            for(int n=0; n<strokes.get(m).size();n++){
                addition[m] += sc[strokes.get(m).get(n)];
            }
        }
        double[] setup = new double[products];
        for(int j=0;j<products;j++){
            setup[j] = addition[j]/strokes.get(j).size();
        }

        eoqs = new double[products];
        for(int j=0;j<products;j++){
            RoundDouble roundedDouble = new RoundDouble();
            eoqs[j] = roundedDouble.roundHalfEven(Math.sqrt((2.0*demand[j]*setup[j])/holding[j]), 2);
        }

        double[][] newrequirements = new double[products][requirement[0].length];
        for(int j=0;j<products;j++){
            newrequirements[j] = FOQ(requirement[j], eoqs[j]);
        }
        return newrequirements;   
    }
    public double[][] WagnerWhitin(double[][] requirement, double[] hc, double[][] output, double[] sc, double[] oc){
        int products = requirement.length;
        List<List<Integer>> strokes = new ArrayList();
        for (int i=0;i<products;i++){
            
            List<Integer> thisproduct = new ArrayList();
            for(int k=0;k<output[0].length;k++){
                if(output[i][k]>0) {
                    thisproduct.add(k);
                }
            }
            strokes.add(thisproduct);
        }
        double[] additionSC= new double[products];
        double[] additionOC= new double[products];
        for(int m =0;m<products;m++){
            for(int n=0; n<strokes.get(m).size();n++){
                additionSC[m] += sc[strokes.get(m).get(n)];
                additionOC[m] += oc[strokes.get(m).get(n)];
            }
        }
        double[] setup = new double[products];
        double[] operation = new double[products];
        for(int j=0;j<products;j++){
            setup[j] = additionSC[j]/strokes.get(j).size();
            operation[j] = additionOC[j]/strokes.get(j).size();
        }
        
        double[][] plan = new double[products][requirement[0].length];
        for(int j=0;j<products;j++){
            WagnerWhitin ww = new WagnerWhitin(requirement[j],hc[j],setup[j],operation[j]);
            plan[j] = ww.getPlan();
        }
        return plan;
    }
    public double[] WagnerWhitin(int productKey, double[] requirement, double hc, double[][] output, double[] sc, double[] oc){
        double[] plan;
        List<Integer> thisproduct = new ArrayList();
        for(int i=0;i<output[0].length;i++){
            if(output[productKey-1][i]>0) {
                thisproduct.add(i);
            }
        }
        double additionSC = 0, additionOC = 0, setup = 0, operation = 0;
        for(int n=0; n<thisproduct.size();n++){
            additionSC += sc[thisproduct.get(n)];
             additionOC += oc[thisproduct.get(n)];
        }
        setup = additionSC/thisproduct.size();
        operation = additionOC/thisproduct.size();
        
        WagnerWhitin ww = new WagnerWhitin(requirement,hc,setup,operation);
        plan = ww.getPlan();
        return plan;
    }
    
}
