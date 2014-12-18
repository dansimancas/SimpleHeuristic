/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package heuristicasimple;

import java.util.Arrays;

/**
 *
 * @author Research
 */
public class Datos {
    
    public static void main(String[] args) {
        
        double[][] output = {{1,0,0,1,1,0,0,0,0},{0,1,0,0,0,0,0,1,0},{0,0,1,0,0,0,0,0,0},{0,0,0,0,0,1,0,0,0},{0,0,0,0,0,0,1,0,0},{0,0,0,0,0,0,0,0,1}};
        double[][] input = {{0,0,0,0,0,0,0,0,0},{2,0,0,0,3,0,0,0,0},{3,0,0,0,0,0,0,0,0},{0,2,0,0,1,0,0,0,0},{0,0,0,0,0,1,0,0,0},{0,1,0,0,0,0,0,0,0}};
        double[][] proj_req = {{0,0,0,0,800.0,300.0,300.0,300.0,100.0,200.0},{0.0,0.0,0.0,0.0,0.0,0.0,230.0,100.0,347.0,900.0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0}};
        double[][] prog_rec = {{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0}};
        double[] foq = {5000,5000,5000,5000,5000,5000};

        double[] initialInv = {200.0,150.0,30.0,20.0,90.0,10.0};
        double[] hc = {50.0,60.0,56.0,36.0,21.0,35.0};
        double[] lt = {1.0,2.0,1.0,5.0,1.0,2.0,1.0,3.0,1.0};
        double[] SC = {2000.0,1800.0,1500.0,18000.0,7300.0,4000.0,1800.0,3000.0,2500.0};
        double[] OC = {2.0,0.5,0.5,0.5,2.0,1.0,0.5,0.5,0.5};
        double[] SOC = {500.0,600.0,400.0,950.0,210.0,356.0};
        int horizonte = 4;
        int[] lotrules = {4,4};
        
        FreshStart firstfresh = new FreshStart(proj_req, prog_rec,horizonte,foq,lotrules,input, output, lt, initialInv, hc, OC, SC,SOC);
        
        //Outputs del sistema
        System.out.println("\nACCUMULATED SETUP COSTS:"+Arrays.toString(firstfresh.getAccumSCTable()));
        System.out.println("\nACCUMULATED OPERATION COSTS:"+Arrays.toString(firstfresh.getAccumOCTable()));
        System.out.println("\nPROJECTED REQUIREMENTS:");
        firstfresh.printProductVsTime(firstfresh.getProjectedRequirements());
        System.out.println("\nSCHEDULED RECEIPTS:");
        firstfresh.printProductVsTime(firstfresh.getScheduledReceipts());
        System.out.println("\nFINAL STROKE PRODUCTION:");
        firstfresh.printStrokeVsTime(firstfresh.getFinalStrokeProduction());
        System.out.println("\nPLANNED RECEIPTS:");
        firstfresh.printProductVsTime(firstfresh.getPlannedReceipts());
        System.out.println("\nINVENTORY AFTER PRODUCTION:");
        firstfresh.printProductVsTime(firstfresh.getInventory());
        System.out.println("\nSTOCK OUTS:");
        firstfresh.printProductVsTime(firstfresh.getStockOuts());
        System.out.print("\nTOTAL PRODUCTION COST: ");
        firstfresh.printCost(firstfresh.getTotalProductionCost());
        System.out.println();
        System.out.print("\nEXECUTION TIME IN NANOSECONDS: "+firstfresh.getExecutionTime()+"\n");
    }
}
