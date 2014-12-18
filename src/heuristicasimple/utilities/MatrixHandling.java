/*
 * By: Daniela Simancas Mateus
 * (C) 2014
 */

package heuristicasimple.utilities;
import heuristicasimple.dataStructures.ChildComponents;
import heuristicasimple.dataStructures.DoubleInt;
import java.util.*;

/**
 *
 * @author Daniela Simancas Mateus
 */
public class MatrixHandling {
    
    public MatrixHandling(){};
    
    /**
     * Method to calculate the matrix product of two int matrices, they must be 
     * @param MatrixA
     * @param MatrixB
     * @return The product of two matrices
     * @throws IndexOutOfBoundsException when the given matrices are not multipliable: columns of A != rows of B 
     */
    public double[][] MatrixProduct (double[][] MatrixA, double[][]MatrixB){
        int colA = MatrixA[0].length;
        int rowA = MatrixA.length;
        int colB = MatrixB[0].length;
        double[][] MatrixC = new double[rowA][colB];

        for (int i = 0; i < rowA; i++) { // aRow
            for (int j = 0; j < colB; j++) { // bColumn
                for (int k = 0; k < colA; k++) { // aColumn
                    MatrixC[i][j] += MatrixA[i][k] * MatrixB[k][j];
                }
            }
        }
        return MatrixC;
    }
    
    public double[][] TransposeMatrix(double[][] Matrix){
        
        int MatrixRows = Matrix.length;
        int MatrixColumns = Matrix[0].length;
        double[][] Transpose = new double[MatrixColumns][MatrixRows];
        
        for(int i=0; i<MatrixRows;i++){
            for(int j=0;j<MatrixColumns;j++){
                Transpose[j][i] = Matrix[i][j];
            }
        }
        return Transpose;
    }
    
    /**
        And_or [#strokes][#strokes][2]
                    S1               S2                S3
        S1  [[dato][and/or]   [dato][and/or]    [dato][and/or]]
        S2  [[dato][and/or]   [dato][and/or]    [dato][and/or]]
        S3  [[dato][and/or]   [dato][and/or]    [dato][and/or]]
     * @param output
     * @param stroke
     * @return 
    */
    public double[][][] And_Or (double[][] output, double[][] stroke){
        int numProducts = output.length;
        int numStrokes = stroke.length;
        double[][][] andor = new double[numStrokes][numStrokes][2];
        //Arrays.fill(andor, 0);
        for(int i=0;i<numStrokes;i++){
            for(int j=0;j<numStrokes;j++){
                andor[i][j][0] = stroke[i][j];
                andor[i][j][1] = 0;
            }
        }
        
        int posStroke =0, posProd =0, accum =0;
        for (int j=0;j<numStrokes;j++){
            for(int i=0; i<numStrokes;i++){
                if(stroke[i][j] > 0){
                    posStroke = i;
                    for(int k=0;k<numProducts;k++){
                        if(output[k][posStroke]>0){
                            posProd = k;
                        }
                    }
                    for(int m=0;m<numStrokes;m++){
                        if(output[posProd][m]>0) accum++;
                    }
                    if (accum>1) {
                       andor[i][j][1] = 1; 
                    }else {andor[i][j][1] = 0; }
                }
                accum = 0;
            }
        }
        
        return andor;
    }
    
    public TreeDouble Transform2Tree(int rootKey, double[][][] matrix){
        //The root key of every tree will be the given rootKey and it will always have a value of 1, and_or of 0 and no father.
        TreeDouble tree = new TreeDouble(rootKey,1,0,0, this.getChildrenList(rootKey,matrix));
        int numStrokes = matrix.length;
        
        //The descendants of the selected strokes
        for(int j=rootKey;j<numStrokes;j++){
            for(int i=0;i<numStrokes;i++){
                if( (i!=j) && (matrix[i][j][0] != 0)){
                   //tree.addNode(key,value,and_or,parent,children);
                    
                    if(tree.existsNode(j+1, tree.getRoot())){
                        tree.addNode(i+1,matrix[i][j][0],matrix[i][j][1],j+1,this.getChildrenList(i+1, matrix));
                    }
                }
            }
        }
        return tree;
    }
    
    public TreeDouble Transform2Tree(NodeDouble rootNode, double[][][] matrix){
        //The root key of every tree will be the given rootKey and it will always have a value of 1, and_or of 0 and no father.
        TreeDouble tree = new TreeDouble(rootNode);
        int numStrokes = matrix.length;
        
        //The descendants of the selected strokes
        int j=rootNode.getKey() - 1;
        for(int i=0;i<numStrokes;i++){
            if( (i!=j) && (matrix[i][j][0] != 0.0)){
                tree.addNode(i+1,matrix[i][j][0],matrix[i][j][1],j+1,this.getChildrenList(i+1, matrix));//tree.addNode(key,value,and_or,parent,children);
            }
        }
        
        return tree;
    }
    
    public ArrayList<NodeDouble> getChildrenList (int stroke, double[][][] and_or){
        int numStrokes = and_or.length;
        List<ChildComponents> descendants = new ArrayList();
        int count =0,k=0;
        
        for(int i=0;i<numStrokes;i++){
            if((and_or[i][stroke-1][0]>0)) {
                ChildComponents components = new ChildComponents();
                count++;
                components.key = i+1;
                components.value = and_or[i][stroke-1][0];
                components.and_or = and_or[i][stroke-1][1];
                components.parentKey = stroke;
                descendants.add(k, components);
                k++;
            }
        }
                
        ArrayList<NodeDouble> children = new ArrayList();
        if(count == 0) descendants.clear();
        for (ChildComponents descendant : descendants) {
            NodeDouble nodeDouble = new NodeDouble(descendant.key, descendant.value, descendant.and_or, descendant.parentKey);
            children.add(nodeDouble);
        }
        
        return children;
    }
    
    int getProductfromStroke(int strkKey, double[][] OUTPUT ){
                for(int i=0;i<OUTPUT.length;i++){
                    if(OUTPUT[i][strkKey-1] > 0){
                        return i+1;
                    }
                }
            return 0;
        }
    
    public double getAccumSC(NodeDouble relative, double[][][] and_or, double[] sc, double[][] OUTPUT){

            MatrixHandling mymatrix = new MatrixHandling();
            TreeDouble mytree = mymatrix.Transform2Tree(relative, and_or);

            ArrayList<NodeDouble> children = relative.getChildren();
            double accumAND = 0.0;
            double accumOR = 0.0;
            if(children.isEmpty()){
                relative.setAccumSC(getRelSC(relative.getKey(),sc));
                return relative.getAccumSC();
            }

            Map<Integer,Integer> ANDamount = productANDMaps(relative.getChildrenAsMap());
            Map<Integer,int[]> ORamount = SeparateORbyProduct(relative.getChildrenAsMap(), OUTPUT);
            Iterator it = ANDamount.entrySet().iterator();

            while (it.hasNext()){
                Map.Entry<Integer,Integer> e = (Map.Entry)it.next();
                //accumAND += getRelSC(e.getKey());
                accumAND += getAccumSC(mytree.getNode(e.getKey(),relative),and_or,sc, OUTPUT);
            }
            Iterator it2 = ORamount.entrySet().iterator();
            double temp = 0.0;

            while(it2.hasNext()){
                Map.Entry<Integer,int[]> e = (Map.Entry)it2.next();
                    for(int k=0;k<e.getValue().length;k++){
                    temp += getAccumSC(mytree.getNode(e.getValue()[k],relative),and_or,sc, OUTPUT);
                }
                temp = temp/e.getValue().length;
                accumOR += temp;
            }
            double accumValue = getRelSC(relative.getKey(),sc) + accumAND + accumOR;
            relative.setAccumSC(accumValue);
            return relative.getAccumSC();
        }
    
    double getRelSC(int stroke, double[]sc){
            for(int i=0;i<sc.length;i++){
                if(i+1 == stroke){
                    return sc[i];
                }
            }
            return 0.0;
        }
    
    Map productANDMaps(Map<Integer,double[]> child){

        Map<Integer,Double> amounts = new HashMap();
        Iterator it = child.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer,double[]> e = (Map.Entry)it.next();
            if (e.getValue()[1] == 0){
                int key = e.getKey();
                double value = e.getValue()[0];
                amounts.put(key, value);
            }
        }
        return amounts;
    }
    
    Map<Integer,DoubleInt> productORMaps(Map<Integer,double []> child, double[][] OUTPUT){

        Iterator it = child.entrySet().iterator();
        Map<Integer,DoubleInt> amounts = new HashMap();
        while (it.hasNext()){
            Map.Entry<Integer,double[]> e = (Map.Entry)it.next();
            if (e.getValue()[1] == 1){
                int key = e.getKey();
                double value = e.getValue()[0];
                int product = getProductfromStroke(key, OUTPUT);
                DoubleInt values = new DoubleInt();
                values.value = value;
                values.product = product;
                amounts.put(key, values);
            }
        }
        return amounts;

    }
    
    Map<Integer,int[]> SeparateORbyProduct(Map<Integer,double[]> child, double[][] OUTPUT){
        Map<Integer,DoubleInt> amounts = productORMaps(child, OUTPUT);//key: strokeKey, lt, product
        Map<Integer,int[]> mymap = new HashMap();
        List<int[]> products = new ArrayList(); //0. Number of or strokes with that product asociated  1. Product name
        Iterator it = amounts.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Integer,DoubleInt> e = (Map.Entry)it.next();
            int[] temp = new int[2];
            temp[1]=e.getValue().product; 
            int count=0;
            for (int[] product : products) {
                if (product[1] == e.getValue().product) {
                    count++;
                }
            }
            if(count==0) products.add(temp);
        }
        for (int[] product : products) {
            Iterator it2 = amounts.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<Integer,DoubleInt> e = (Map.Entry)it2.next();
                int temp = e.getValue().product;//product name related to the input amounts map
                if (product[1] == temp) {
                    product[0]++;
                }
            }
        }
        /**
         * products is a list of arrays with the product name and the number of strokes associated to it.
         */
        for (int[] product : products) {
            int[] vector = new int[product[1]];
            mymap.put(product[0], vector);
        }
        Iterator it3 = mymap.entrySet().iterator();
        Iterator it4 = amounts.entrySet().iterator();
        while (it3.hasNext()){
            Map.Entry<Integer,int[]> map = (Map.Entry)it3.next();
            int i=0;
            while (it4.hasNext()){
                Map.Entry<Integer,DoubleInt> temp = (Map.Entry)it4.next();
                if(map.getKey() == temp.getValue().product){
                    map.getValue()[i] = temp.getKey();
                    i++;
                }
            }
        }
        return mymap;
    }
    
    public double getAccumOC(NodeDouble relative, double[][][] and_or, double[]oc, double[][] OUTPUT){
        TreeDouble mytree = Transform2Tree(relative, and_or);
        ArrayList<NodeDouble> children = relative.getChildren();
        double accumAND = 0.0;
        double accumOR = 0.0;
        if(children.isEmpty()){
            relative.setAccumOC(getRelOC(relative.getKey(), oc));
            return relative.getAccumOC();
        }

        Map<Integer,Double> ANDamount = productANDMaps(relative.getChildrenAsMap());
        Map<Integer,int[]> ORamount = SeparateORbyProduct(relative.getChildrenAsMap(), OUTPUT);
        Iterator it = ANDamount.entrySet().iterator();

        while (it.hasNext()){
            Map.Entry<Integer,Double> e = (Map.Entry)it.next();
            accumAND += e.getValue()*getAccumOC(mytree.getNode(e.getKey(),relative), and_or, oc, OUTPUT);
        }
        Iterator it2 = ORamount.entrySet().iterator();
        double temp = 0.0;

        while(it2.hasNext()){
            Map.Entry<Integer,int[]> e = (Map.Entry)it2.next();
            for(int k=0;k<e.getValue().length;k++){
                double value = mytree.getNode(e.getValue()[k], relative).getValue();
                temp += value*getAccumOC(mytree.getNode(e.getValue()[k],relative), and_or, oc, OUTPUT);
            }
            temp = temp/e.getValue().length;
            accumOR += temp;
        }
        double accumValue = getRelOC(relative.getKey(), oc)+accumAND+accumOR;
        relative.setAccumOC(accumValue);
        return relative.getAccumOC();
        }
    
    double getRelOC(int stroke, double[]oc){
            for(int i=0;i<oc.length;i++){
                if(i+1 == stroke){
                    return oc[i];
                }
            }
            return 0.0;
        }
    
    public List<Integer> getDescendantsofProduct(int A, double[][] output, double[][][] and_or){
        List<Integer> descendantsA = new ArrayList();
        List<Integer> strokeList = new ArrayList();
        
        //Definiendo cuales son los strokes que producen A
        for(int j = 0; j < output[0].length; j++){
            if(output[A-1][j]>0){
                int stroke = j+1;
                strokeList.add(stroke);
            }
        }
        //Agregando los descendientes de cada stroke a la lista de descendientes de A.
        for (Integer strokeList1 : strokeList) {
            TreeDouble treeS = Transform2Tree(strokeList1, and_or);
            List<Integer> descendantsS = treeS.getDescendantsList();
            descendantsA.addAll(descendantsS);
        }
        
        return descendantsA;
    }
}