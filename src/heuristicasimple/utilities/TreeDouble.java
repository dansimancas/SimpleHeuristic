/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package heuristicasimple.utilities;
import java.util.*;


public class TreeDouble {
    private NodeDouble root;
    private NodeDouble current;
    private int numNodes;
    
    /**
     * Method has to be initialized with the ROOT. Its parent must be 0.
     * @param k
     * @param v
     * @param andor
     * @param p 
     * @param chil
     */
    public TreeDouble(int k, double v, double andor, int p, ArrayList<NodeDouble> chil){
        numNodes = 1+chil.size();
        this.addNode(k, v, andor, p, chil);
    }
    public TreeDouble(NodeDouble rootNode){
        this.root = rootNode;
    }
    public final void addNode(int k, double v, double andor, int p, ArrayList<NodeDouble> chil){
        //the following suggests that the root will never be null
            
            NodeDouble newnode = new NodeDouble(k,v,andor,p);
            this.current = newnode;
            this.current.setChildren(chil);
            if (this.root==null){
                this.root = this.current;
            } else {
                //if(!this.existsNode(this.current.getKey(), this.root)){
                    if(this.existsNode(this.current.getKey(), this.root)){
                        NodeDouble par = getNode(p, this.root);
                        int curr = current.getKey();
                        par.deleteChild(curr);
                    }
                    numNodes++;
                    if(this.getNode(p, this.root) != null){
                        NodeDouble parent;
                        parent = this.getNode(p, this.root);
                        parent.addChild(newnode);
                    }
                //}
            }
    }
    /**
     * Method that returns a node receiving a nodeKey and an initial node to search from. Preferably the node input
     * should be the root of the tree in matter.
     * @param nodeKey
     * @param curr
     * @return 
     */
    public NodeDouble getNode(int nodeKey, NodeDouble curr){
        if(curr.getKey() == nodeKey){
            return curr;
        }
        ArrayList<NodeDouble> children = curr.getChildren();
        NodeDouble temp = null;
        for (int i=0; temp == null && i <children.size();i++){
            temp = getNode(nodeKey, children.get(i));
        }
        if (temp == null){
            return null;
        }
        return temp;
    }
    
    public boolean existsNode(int nodeKey, NodeDouble ref){
        return this.getNode(nodeKey,ref) != null;
    }
    
    public List getDescendantsList(){
        List lista = new ArrayList();
        int i=0, k=1;
        while(i<numNodes-1){
            if(this.existsNode(k, root)){
                int key = this.getNode(k, root).getKey();
                if(key != root.getKey()) {
                    lista.add(key);
                    i++;
                }
            }
            k++;
        }
        return lista;
    }
    /**
     * Does the same as getDescendantsList except that also offers the value of the stroke
     * @return 
     */
    public Map getDescendantsMap(){
        Map<Integer,double[]> map = new HashMap();
        int i=0, k=0;
        while(i<numNodes){
            if(this.existsNode(k, root)){
                int key = this.getNode(k, root).getKey();
                double value = this.getNode(k, root).getValue();
                double and_or = this.getNode(k, root).getAnd_Or();
                double[] values = {value,and_or};
                if(key != root.getKey()) {
                    map.put(key, values);
                    i++;
                }
            }
            k++;
        }
        return map;
    }
    
    public NodeDouble getRoot(){
        return this.root;
    }
   
}