public double[][] freshHeuristic(List<Struct2> productList, double[][] strokeMatrix, int t){

        productList = sortProductList(productList, t);
        int[] names = new int[productList.size()];
        double[] values = new double[productList.size()];
        for(int c=0;c<productList.size();c++){
            names[c] = productList.get(c).product;
            values[c] = productList.get(c).value;
        }

        if(t==0){
            System.out.println("Couldn't complete request: "+Arrays.toString(names)+" with amounts: "+Arrays.toString(values)+". Not enough time. t="+t);
            return null;
        }

        //System.out.println("\nProducing: "+Arrays.toString(names)+" with these amounts: "+Arrays.toString(values)+", on day: "+t);

        for (Struct2 productList1 : productList) {

            Struct beststroke = bestStroke(productList1.product, t, productList1.value);

            if(beststroke==null){
                System.out.println("Couldn't complete request: "+Arrays.toString(names)+" with amounts: "+Arrays.toString(values)+". Not enough time. t="+t);
                return null;
            }

            double value = beststroke.RunsXTimes;
            strokeMatrix[beststroke.strokeKey-1][beststroke.timeWhenRuns] += value;
        }

        /*System.out.println("Strokes despues de escojer el mejor:\n");
        System.out.println(Arrays.deepToString(strokeMatrix).replace("], ", "]\n"));*/


        List<Struct> strokeList = new ArrayList();
        int when = t+1, count=0;
        for(int c=t-1;c>=0; c--){
            for(int s=0;s<NUMBER_OF_STROKES;s++){
                if(strokeMatrix[s][c] > 0.0 && !isLeaf(s+1)){
                    when = c;
                    s=NUMBER_OF_STROKES;
                    c=-1;
                    count++;
                }
            }
        }

        if(count!=0){
            for(int s=0;s<NUMBER_OF_STROKES;s++){
                if(strokeMatrix[s][when] > 0.0 && !isLeaf(s+1)){
                    Struct newstruct = new Struct();
                    newstruct.strokeKey=s+1;
                    newstruct.timeWhenRuns=when;
                    newstruct.RunsXTimes = (int) (strokeMatrix[s][when]);
                    strokeList.add(newstruct);
                }
            }
        }

        if(strokeList.size()>0){
           List<Struct2> nextProductList;
           nextProductList = getProductsUnder(strokeList);
           return freshHeuristic(nextProductList, strokeMatrix, when);
        }

        return strokeMatrix;

    }

    public void runsHeuristic(){
        System.out.println("\nRuns heuristic:");
        List<double[][]> strokeMatrixList = new ArrayList();

        for(int m = 0; m < PERIOD_LENGTH; m++){

            List<Struct2> productList = new ArrayList();

            for (Integer END_ITEMS1 : END_ITEMS) {
                Struct2 mystruct = new Struct2();
                mystruct.product = END_ITEMS1;
                productList.add(mystruct);
            }

            int count=0;
            for (Struct2 productList1 : productList) {
                if (MAIN_PRODUCTION_SCHEDULE[productList1.product - 1][m] == 0.0) count++;
            }
            //Si la demanda todos los productos no es cero
            if(count != productList.size()){

                for(int k = 0; k < productList.size(); k++){
                    if(MAIN_PRODUCTION_SCHEDULE[productList.get(k).product-1][m] == 0.0) productList.remove(k);
                }

                double[][] strokeMatrix = new double[NUMBER_OF_STROKES][PERIOD_LENGTH];

                for (Struct2 productList1 : productList) {
                    double temp;
                    temp = MAIN_PRODUCTION_SCHEDULE[productList1.product - 1][m];
                    productList1.value = temp;
                }

                System.out.println("\nRunning the heuristic for "+productList.size()+" products, at day "+m+"...\n");
                double[][] strokesMatrix = freshHeuristic(productList, strokeMatrix, m);
                if (strokesMatrix != null){
                    System.out.println("Necesidad del dia: "+m+"\n");
                    System.out.println(Arrays.deepToString(strokesMatrix).replace("], ", "]\n"));
                    strokeMatrixList.add(strokesMatrix);
                }
                else {
                    int[] names = new int[productList.size()];
                    double[] values = new double[productList.size()];
                    for(int c=0;c<productList.size();c++){
                        names[c] = productList.get(c).product;
                        values[c] = productList.get(c).value;
                    }
                    System.out.println("There is not enough time to produce: "+Arrays.toString(names)+" with amounts: "+Arrays.toString(values)+" on day "+m);
                }
            }
        }
        MatrixHandling mymatrix = new MatrixHandling();
        finalStrokeProduction = mymatrix.addItems(strokeMatrixList);
    }

    private List<Struct2> getProductsUnder(List<Struct> strokeList){
            List<Struct2> mylist = new ArrayList();
        for (Struct strokeList1 : strokeList) {
            int strokeKey = strokeList1.strokeKey;
            double mps = strokeList1.RunsXTimes;
            for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
                if (INPUT[i][strokeKey-1] > 0.0){
                    Struct2 mystruct = new Struct2();
                    mystruct.product = i+1;
                    mystruct.value = INPUT[i][strokeKey-1]*mps;
                    int count=0;
                    for (Struct2 mylist1 : mylist) {
                        if (mylist1.product == mystruct.product) {
                            mylist1.value += mystruct.value;
                            count++;
                        }
                    }
                    if (count==0) mylist.add(mystruct);
                }
            }
        }

            return mylist;
        }

        /**
         * Method to determine if a stroke has or not any component requirement.
         * @param key
         * @return
         */
        private boolean isLeaf(int key){
            for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
                if(INPUT[i][key-1] > 0) return false;
            }
            return true;
        }

        //Main para el ejemplo2
        public static void main(String[] args) {

        double[][] output = {{1.0,0.0,0.0,0.0},{0.0,0.0,0.0,1.0},{0.0,1.0,0.0,0.0},{0.0,0.0,1.0,0.0}};
        double[][] input = {{0.0,0.0,0.0,0.0},{2.0,0.0,0.0,0.0},{1.0,0.0,0.0,1.0},{1.0,2.0,0.0,0.0}};
        double[][] proj_req = {{500.0,600.0,400.0,950.0,800.0,300.0,300.0,300.0,100.0,200.0,300.0,400.0},{0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0}};
        double foq = 1000;

        double[] initialInv = {200.0,150.0,30.0,20.0};
        double[] hc = {500.0,600.0,560.0,360.0};
        double[] lt = {2.0,1.0,1.0,1.0};
        double[] SC = {2000.0,1800.0,1500.0,5000.0};
        double[] OC = {2.0,0.5,0.5,0.5};
        int horizonte = 5;

        FreshStart firstfresh = new FreshStart(proj_req, horizonte,foq,input, output, lt, initialInv, hc, OC, SC);

        System.out.println("\nEnd Items: \n");
        System.out.println(firstfresh.getEndItems().toString());
        System.out.println("\nMain Production Schedule: \n");
        System.out.println(Arrays.deepToString(firstfresh.getMPS()).replace("], ", "]\n"));
        System.out.println("\nInventory: \n");
        System.out.println(Arrays.deepToString(firstfresh.getInventory()).replace("], ", "]\n"));
        System.out.println("\nInventory Costs: \n");
        System.out.println(Arrays.deepToString(firstfresh.getInventoryCosts()).replace("], ", "]\n"));

        //Outputs del sistema
        System.out.println("\nFINAL STROKE PRODUCTION: ");
        System.out.println(Arrays.deepToString(firstfresh.getFinalStrokeProduction()).replace("], ", "]\n"));
        System.out.println("\nHeuristic Inventory after production: \n");
        System.out.println(Arrays.deepToString(firstfresh.getHeuristicInventory()).replace("], ", "]\n"));
        System.out.println("\nInventory after production: \n");
        System.out.println(Arrays.deepToString(firstfresh.getInventory()).replace("], ", "]\n"));

        System.out.println("\nProjected Requirements: \n");
        System.out.println(Arrays.deepToString(firstfresh.getProjectedRequirements()).replace("], ", "]\n"));

        System.out.println("\nAccumulated Setup Costs ");
        for(int j = 0; j < firstfresh.getNumberOfStrokes(); j++){
            System.out.println(j+1+": ["+firstfresh.getAccumSCTable()[j]+"]");
        }

        System.out.println("\nAccumulated Operations Costs ");
        for(int j = 0; j < firstfresh.getNumberOfStrokes(); j++){
            System.out.println(j+1+": ["+firstfresh.getAccumOCTable()[j]+"]");
        }
        /*System.out.println("\nRelative Setup Costs ");
        for(int j = 0; j < firstfresh.NUMBER_OF_STROKES; j++){
            System.out.println(j+1+": ["+firstfresh.SETUP_COSTS[j]+"]");
        }
        System.out.println("\nRelative Operations Costs ");
        for(int j = 0; j < firstfresh.NUMBER_OF_STROKES; j++){
            System.out.println(j+1+": ["+firstfresh.OPERATION_COSTS[j]+"]");
        }*/
    }
