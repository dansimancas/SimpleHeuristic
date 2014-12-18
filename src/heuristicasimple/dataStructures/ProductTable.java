/*
 * By Daniela Simancas Mateus
 */
package heuristicasimple.dataStructures;

/**
 *
 * @author Daniela
 */
public class ProductTable {
    public int productKey;
    public double[] productRequirements;
    public ProductTable(int period_length){
        productRequirements = new double[period_length];
    }
    public boolean hasNoRequirements(){
        int count=0;
        for(int i=0;i<productRequirements.length;i++){
            if(productRequirements[i]>0) count++;
        }
        return count==0;
    }
}
