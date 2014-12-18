/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package heuristicasimple.utilities;

import java.util.*;

/**
 *
 * @author Research
 */

public class NodeDouble{
    private final int KEY;
    private final double VALUE;
    private final double AND_OR_CONDITION;
    private final int PARENT_KEY;
    private  double accumOC;
    private  double accumSC;
    private ArrayList<NodeDouble> children;
    
    public NodeDouble (int key, double value, double andor, int parent){
        this.KEY = key;
        this.VALUE = value;
        this.AND_OR_CONDITION = andor;
        this.PARENT_KEY = parent;
        children = new ArrayList();
    }
    public ArrayList<NodeDouble> getChildren(){
        return children;
    }
    public List<Integer> getChildrenAsList(){
        List<Integer> child = new ArrayList();
        for (NodeDouble children1 : this.children) {
            child.add(children1.KEY);
        }
        return child;
    }
    public Map<Integer,double[]> getChildrenAsMap(){
        
        Map<Integer,double[]> childrenMap = new HashMap();
        for (NodeDouble children1 : children) {
            int key = children1.getKey();
            double value = children1.getValue();
            double and_or = children1.getAnd_Or();
            double[] values = {value,and_or};
            childrenMap.put(key, values);
        }
        
        return childrenMap;
    }
    public void addChild(NodeDouble n){
        children.add(n);
    }
    
    public void deleteChild(int key_n){
        for(int i=0;i<children.size();i++){
            if(key_n == children.get(i).getKey()){
                NodeDouble node = children.get(i);
                children.remove(node);
            }
        }
    }
    
    public void setChildren(ArrayList<NodeDouble> chil){
        children = chil;
    }
    public int getKey(){
        return KEY;
    }
    public double getValue(){
        return VALUE;
    }
    public void setAccumOC(double v){
        accumOC = v;
    }
    public double getAccumOC(){
        return accumOC;
    }
    public void setAccumSC(double v){
        accumSC = v;
    }
    public double getAccumSC(){
        return accumSC;
    }    
    public double getAnd_Or(){
        return AND_OR_CONDITION;
    }
    public int getParentKey(){
        return PARENT_KEY;
    }
    public String Print(){
        return "El stroke "+KEY+" tiene un valor de "+VALUE;
    }
}