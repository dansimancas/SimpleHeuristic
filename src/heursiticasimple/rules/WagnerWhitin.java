/*
 * By Daniela Simancas Mateus
 */
package heursiticasimple.rules;

import java.util.*;

/**
 *
 * @author Daniela
 */
public class WagnerWhitin {
    
    public WagnerWhitin(double[] req, double holding, double setup, double operation ){
        requirement = req;
        hc = holding;
        sc = setup;
        oc = operation;
        periods = requirement.length;
        k_tl = K_tl();
        Table_WW = WW();
        Plan = calcularPlan();
    };
    
    
    
    private final double[] requirement;
    private final double hc;
    private final double sc;
    private final double oc;
    private final int periods;
    private final Map<Integer,List<Ktl>> k_tl;
    private final double[][] Table_WW;
    private final double[] Plan;
    
    private double getKtlCost(int de, int hasta){
        List<Ktl> lista = k_tl.get(de);
        for(int i =0;i<lista.size();i++){
            if(lista.get(i).de == de && lista.get(i).hasta == hasta){
                return lista.get(i).costo;
            }
        }
        return 0.0;
    }
    private Map<Integer,List<Ktl>> K_tl(){
        Map<Integer,List<Ktl>> MapaLista = new HashMap();
        double quant=0,inv=0;
        for(int t=1;t<=periods;t++){
            
            List<Ktl> ktl = new ArrayList();
            /*for(int l=t;l<periods;l++){ //esta no aparece en el libro
                quant += requirement[l]; 
            }*/
            Ktl temp1 = new Ktl();
            temp1.de = t;
            temp1.hasta = t;
            temp1.costo = sc;
            ktl.add(temp1);
            
            for(int l=t+1;l<=periods;l++){
                for(int j=t+1;j<=l;j++){
                   inv += (j-t)*requirement[j-1];
                }
                Ktl temp = new Ktl();
                temp.de = t;
                temp.hasta = l;
                temp.costo = sc + oc*quant + hc*inv;
                ktl.add(temp);
                inv=0;
            }
            
            int key = t;
            MapaLista.put(key, ktl);
        }
        
        return MapaLista;
       
    }
    
    private double[][] WW(){
        double[][] TableWW = new double [periods+2][periods+1];
        for(int l = 1;l<=periods;l++){
            for(int t=l;t>0;t--){
                TableWW[t][l] = TableWW[periods+1][t-1] + getKtlCost(t,l); //escribiendo las opciones del dia
            }
            double min = 999999.0;
            for(int t=l;t>=1;t--){
                if(TableWW[t][l] < min) min = TableWW[t][l]; //hallando el mínimo
            }
            TableWW[periods+1][l] = min;
        }
        return TableWW;
    }
    
    private double[] calcularPlan(){
        double[] plan = new double[periods];
        for(int t=periods; t>0;t--){
            for(int l = 1; l<=periods;l++){
                if(Table_WW[l][t] == Table_WW[periods+1][t]){ //TableWW = new double [periods+2][periods+1];
                    int de = l;
                    int hasta = t;
                    double suma=0;
                    for(int i=de;i<=hasta;i++){
                        suma += requirement[i-1];
                    }
                    plan[de-1]=suma;
                    t = de;
                    l = periods+1;
                }
            }
        }
        return plan;
    }
    
    public double[] getPlan(){
        return Plan;
    }
    
    /*
    public static void main(String[] args) {
        double[] req = {100,100,50,50,210};
        double holding = 0.5;
        double setup = 50;
        double oper =0.0;
        WagnerWhitin objeto = new WagnerWhitin(req,holding,setup,oper);
        double[][] tabla = objeto.Table_WW;
        System.out.println(Arrays.deepToString(tabla).replaceAll("],", "],\r\n"));
        System.out.println("Plan: "+Arrays.toString(objeto.Plan));
    }
    */
    
}
