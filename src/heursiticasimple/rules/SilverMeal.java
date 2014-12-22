/*
 * By Daniela Simancas Mateus
 */
package heursiticasimple.rules;

import java.util.*;

/**
 *
 * @author Daniela
 */
public class SilverMeal {
    public SilverMeal(double[] req, double hc, double sc){
        REQUIREMENT = req;
        PERIODS = REQUIREMENT.length;
        HC = hc;
        SC = sc;
        ORDEN = calcularCostoVariable();
    }
    private final double[] REQUIREMENT;
    private final int PERIODS;
    private final double HC;
    private final double SC;
    private final double[] ORDEN;
    public final boolean checkSum(){
        
        double sum1=0,sum2=0;
        for(int i=0;i<REQUIREMENT.length;i++){
            sum1 += REQUIREMENT[i];
            sum2 += ORDEN[i];
        }
        return sum1 == sum2;
    }
    
    private double[] calcularCostoVariable(){
        
        double[] local = new double[PERIODS];
        int ordenarHasta = 0, ordenarDesde=1;
        while(ordenarHasta<=PERIODS){
            Map<Integer, Double> costos = new HashMap();
            for(int m=1;m<= PERIODS;m++){
                
                double temp=0;
                int k=ordenarDesde;
                
                for(int i=1;i<m;i++){
                    temp += i*HC*REQUIREMENT[k]; //Demanda de i+1, pero como los índices están alterados, queda i.
                    k++;
                    if(k == PERIODS){
                        for(int j=ordenarHasta;j<=PERIODS-1;j++){
                            local[j-1] += REQUIREMENT[j-1];
                        }
                        return local;
                    }
                }
                temp += SC;
                double costo = temp/m;
                int key = m;
                costos.put(key, costo);
                if(costos.containsKey(key-1) && costos.get(key)>costos.get(key-1)){
                    //detenerse
                    ordenarHasta = m-1+ordenarHasta;
                    m = PERIODS;
                }
            }
            double suma=0;
            for(int i=ordenarDesde;i<=ordenarHasta;i++){
                suma += REQUIREMENT[i-1];
            }
            local[ordenarDesde-1] = suma;
            ordenarDesde = ordenarHasta+1;
        }
        
        return local;
    }
    
    public double[] getOrden(){
        return ORDEN;
    }
    
    public static void main(String[] args) {
        double[] req = {0,0,0,1770,1800,0,1200,0,600,0};
        double holding = 56;
        double setup = 1500;
        SilverMeal objeto = new SilverMeal(req,holding,setup);
        double[] orden = objeto.ORDEN;
        System.out.println("Plan: "+Arrays.toString(orden));
        System.out.println("CheckSum: "+objeto.checkSum());
    }
}
