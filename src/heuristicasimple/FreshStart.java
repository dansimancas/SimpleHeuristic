/*
 * By Daniela Simancas Mateus
 * (C) 2014.
 */

/*
k index for END_ITEMS (0 - NUMBER_OF_END_ITEMS)
    for(int k = 0; k < NUMBER_OF_END_ITEMS; k++){
    
    }
m index for PERIOD (0 - PERIOD_LENGTH)
    for(int m = 0; m < PERIOD_LENGTH; m++){
                
    }
i index for PRODUCTS (0 - NUMBER_OF_PRODUCTS)
    for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
    
    }   
j index for STROKES (0 - NUMBER_OF_STROKES)
    for(int j = 0; j < NUMBER_OF_STROKES; j++){
    
    }
*/
package heuristicasimple;
import heuristicasimple.utilities.*;
import heuristicasimple.dataStructures.*;
import heursiticasimple.rules.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Fresh start to the simple heuristic algorithm
 * @author Daniela Simancas-Mateus
 */
public class FreshStart {
    FreshStart(double[][] projected_requirements, double[][] prog_rece, int hp, double[] foq, int[] lotrules,double[][] input, double[][] output, double[] lt,double[] initial_inventory, double[] holding_costs, double[] operation_costs,double[] setup_costs, double[] stock_out_costs){
        long startTime = System.nanoTime();
        
        PLANNING_HORIZON = hp;
        for(int n=0;n<PLANNING_HORIZON;n++){
            for (double[] projected_requirement : projected_requirements) {
                projected_requirement[n] = 0;
            }
        }
        PROJECTED_REQUIREMENTS = projected_requirements;
        SCHEDULED_RECEIPTS = prog_rece;
        FOQ = foq;
        LOT_SIZING_RULES = lotrules;
        INPUT = input;
        OUTPUT = output;
        PERIOD_LENGTH = PROJECTED_REQUIREMENTS[0].length;
        LEAD_TIME = lt;
        NUMBER_OF_PRODUCTS = INPUT.length;
        NUMBER_OF_STROKES = INPUT[0].length;
        END_ITEMS = new ArrayList();
        setEndItems();
        NUMBER_OF_END_ITEMS = END_ITEMS.size();
        INITIAL_INVENTORY = initial_inventory;
        INVENTORY = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
            INVENTORY[i][0] = INITIAL_INVENTORY[i];
        }
        STOCK_OUTS = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        STOCK_OUT_PRODUCT_COST = stock_out_costs;
        
        OPERATION_COSTS = operation_costs;
        SETUP_COSTS = setup_costs;
        MatrixHandling mymatrix = new MatrixHandling();
        AND_OR = mymatrix.And_Or(OUTPUT, mymatrix.MatrixProduct(mymatrix.TransposeMatrix(OUTPUT), INPUT));
        ACCUMULATED_SC = getAccumSCTable();
        ACCUMULATED_OC = getAccumOCTable();
        HOLDING_COSTS = holding_costs;
        
        MAIN_PRODUCTION_SCHEDULE = setMPS();
        
        
        updateInventoryCosts();
        
                
        //RUN THE HEURISTIC
        runsHeuristic();
        
        long endTime = System.nanoTime();
        EXECUTION_TIME = (endTime-startTime); //divide by 1000000 to get milliseconds
        
        }
    
    /**
     * Method to determine which of all products are the End Items: those whose projected requirements are greater than 0.
     * @return  The list of the "names" (integers) of the end items
     */
    private void setEndItems(){
        for(int i = 0;i < NUMBER_OF_PRODUCTS; i++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                if(PROJECTED_REQUIREMENTS[i][m]>0 ){
                    int product = i+1;
                    END_ITEMS.add(product);
                    m = PERIOD_LENGTH;
                }
            }
        }
    }
    
    /**
     * Method to apply a lot-sizing rule to a requirements matrix. The lot-sizing rule used will be determined by chooseLotSizingRule().
     * @param req
     * @return The requirements matrix adapted to the indicated lot-sizing rule
     */
    private double[][] applyLotSizingRule (double[][] req){
        int lot = chooseLotSizingRule();
        LotSizingRule lot_size_Rule= new LotSizingRule();
        double[][] Plan = new double[req.length][PERIOD_LENGTH];
        
        if (lot==1){
            Plan = lot_size_Rule.L4L(req);
        }
        if (lot==2){
            Plan = lot_size_Rule.FOQ(req,5000);
        }
        if (lot==3){
            Plan = lot_size_Rule.EOQ(req,HOLDING_COSTS,OUTPUT, ACCUMULATED_SC);
        }
        if (lot==4){
            Plan = lot_size_Rule.WagnerWhitin(req,HOLDING_COSTS,OUTPUT, ACCUMULATED_SC,ACCUMULATED_OC);
        }
        return Plan;
    }
    
    /**
     * Method to apply a lot-sizing rule to a requirements vector. The lot-sizing rule used will be determined by chooseLotSizingRule().
     * @param req
     * @param lot
     * @return The requirements matrix adapted to the indicated lot-sizing rule
     */
    private double[] applyLotSizingRule (int productKey, double[] req, int lot){
        LotSizingRule lot_size_Rule= new LotSizingRule();
        double[] Plan;
        
        switch(lot){
            case 1: 
                Plan = lot_size_Rule.L4L(req);
                break;
            case 2: 
                Plan = lot_size_Rule.FOQ(req,FOQ[productKey-1]);
                break;
            case 3: 
                Plan = lot_size_Rule.EOQ(productKey,req,HOLDING_COSTS[productKey-1],OUTPUT, ACCUMULATED_SC);
                System.out.println("eoq: "+lot_size_Rule.getEOQ());
                break;
            case 4: 
                Plan = lot_size_Rule.WagnerWhitin(productKey,req,HOLDING_COSTS[productKey-1],OUTPUT, ACCUMULATED_SC, ACCUMULATED_OC);
                break;
            case 5: 
                Plan = lot_size_Rule.SilverMeal(productKey,req,HOLDING_COSTS[productKey-1],OUTPUT, ACCUMULATED_SC);
                break;
            default: Plan = lot_size_Rule.L4L(req);
                System.out.println("Invalid selection. Chosing L4L.");
                break;
        }
        return Plan;
    }
    
    /**
     * Method to smartly select a lot-sizing rule. Currently available only 1: LxL and 2:FOQ.
     * @return An integer that will be interpreted in applyLotSizingRule() as a determined lot-sizing rule. Currently available only 1: LxL and 2:FOQ.
     */
    private int chooseLotSizingRule(){
        return LOT_SIZING_RULES[0];
    }
    
    /**
     * Method to develop the Master Production Schedule. Also updates INVENTORY and uses applyLotSizingRule().
     * @return The Master Production Schedule according to the selected lot-sizing rule
     */
    private double[][] setMPS(){
        RoundDouble round = new RoundDouble();
        double[][] temp_MPS = new double[NUMBER_OF_END_ITEMS][PERIOD_LENGTH];
        
        double[][] inventory_copy = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
            System.arraycopy(INVENTORY[i], 0, inventory_copy[i], 0, PERIOD_LENGTH);
        }
        double[][] on_hand = new double[NUMBER_OF_END_ITEMS][PERIOD_LENGTH+1];
        for(int k = 0; k < NUMBER_OF_END_ITEMS; k++){
            System.arraycopy(INVENTORY[END_ITEMS.get(k)-1], 0, on_hand[k], 0, PERIOD_LENGTH);
        }
        
        for(int i=0;i<NUMBER_OF_PRODUCTS;i++){
            int t=i+1;
            int index = END_ITEMS.indexOf(t);
            if(END_ITEMS.contains(t)){
                for(int m=0;m<PERIOD_LENGTH;m++){
                    inventory_copy[i][m] = on_hand[i][m] + temp_MPS[index][m] + SCHEDULED_RECEIPTS[index][m] - PROJECTED_REQUIREMENTS[index][m];
                    on_hand[i][m+1] = inventory_copy[i][m];
                    if(inventory_copy[i][m] < 0.0) {
                        temp_MPS[index][m] = inventory_copy[i][m]*(-1);
                        inventory_copy[i][m] = 0.0;
                        on_hand[i][m+1] = 0.0;
                    }
                }
            }
        }
        temp_MPS = applyLotSizingRule(temp_MPS);
        for(int i=0;i<NUMBER_OF_PRODUCTS;i++){
            int k=i+1;
            int index = END_ITEMS.indexOf(k);
            if(END_ITEMS.contains(k)){
                for(int m=0;m<PERIOD_LENGTH;m++){
                    INVENTORY[i][m] = round.roundHalfEven(on_hand[i][m] + temp_MPS[index][m] + SCHEDULED_RECEIPTS[index][m] - PROJECTED_REQUIREMENTS[index][m],2);
                    on_hand[i][m+1] = INVENTORY[i][m];
                }
            }
        }
        for(int i=0;i<NUMBER_OF_PRODUCTS;i++){
            for(int m=0;m<PERIOD_LENGTH;m++){
                if(INVENTORY[i][m] < 0){
                    STOCK_OUTS[i][m] = INVENTORY[i][m]*(-1);
                    INVENTORY[i][m] = 0;
                }
            }
        }
        
        return temp_MPS;
    }
    
    private double[] MPSof(ProductTable productData, int lotsize_rule){
        
        int productKey = productData.productKey;
        
        if(isEndItem(productKey)){
            double[] productMPS = MAIN_PRODUCTION_SCHEDULE[productKey-1];
            return productMPS;
            
        }else {
            
            if (productData.hasNoRequirements()) System.out.println("Warning! There are no requirements registered for this product.\n");
            
            double[] on_hand = new double[PERIOD_LENGTH+1];
            System.arraycopy(INVENTORY[productKey-1], 0, on_hand, 0, PERIOD_LENGTH);

            double[] inventory_copy = new double[PERIOD_LENGTH];
            System.arraycopy(INVENTORY[productKey-1], 0, inventory_copy, 0, PERIOD_LENGTH);
            
            double[] tempRequirement = new double[PERIOD_LENGTH];
            System.arraycopy(productData.productRequirements, 0, tempRequirement, 0, PERIOD_LENGTH);
            
            double[] productMPS = new double[PERIOD_LENGTH];
            
            for(int m = 0; m < PERIOD_LENGTH; m++){
                
                inventory_copy[m] = on_hand[m] + productMPS[m] + SCHEDULED_RECEIPTS[productKey][m] - tempRequirement[m];
                on_hand[m+1] = inventory_copy[m];
                if(inventory_copy[m] < 0.0) {
                    productMPS[m] = inventory_copy[m]*(-1);
                    inventory_copy[m] = 0.0;
                    on_hand[m+1] = 0.0;
                }
            }
            
            productMPS = applyLotSizingRule(productKey,productMPS, lotsize_rule);
            RoundDouble rounded = new RoundDouble();
            for(int m=0;m<PERIOD_LENGTH;m++){
                INVENTORY[productKey-1][m] = rounded.roundHalfEven((on_hand[m] + productMPS[m] + SCHEDULED_RECEIPTS[productKey][m] - tempRequirement[m]),2);
                on_hand[m+1] = INVENTORY[productKey-1][m];
            }
            
            for(int i=0;i<NUMBER_OF_PRODUCTS;i++){
                for(int m=0;m<PERIOD_LENGTH;m++){
                    if(INVENTORY[i][m] < 0){
                        STOCK_OUTS[i][m] = INVENTORY[i][m]*(-1);
                        INVENTORY[i][m] = 0;
                    }
                }
            }
            
            return productMPS;
        }
    }
    
    /**
     * Method to determine weather a product is an end item or not.
     * @param productKey
     * @return True if the productKey is an end item.
     */
    private boolean isEndItem(int productKey){
        return END_ITEMS.contains(productKey);
    }
    
    /**
     * Method to update the Inventory Costs for each product and period
     */
    private void updateInventoryCosts(){
        RoundDouble round = new RoundDouble();
        inventoryCosts = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                inventoryCosts[i][m] = round.roundHalfEven((INVENTORY[i][m] * HOLDING_COSTS[i]),2);
            }
        }
    }
    
    /**
     * Method to update the costs for Stock outs.
     */
    private void updateStockOutCosts(){
        RoundDouble round = new RoundDouble();
        stockOutCosts = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                stockOutCosts[i][m] = round.roundHalfEven((STOCK_OUTS[i][m] * STOCK_OUT_PRODUCT_COST[i]),2);
            }
        }
    }
    
    /**
     * Calculates the when, which and how many are the required strokes for the entire production plan.
     * @param productsTables
     * @param strokeMatrix
     * @return A matrix with length: the number of strokes and width: the period length. 
     */
    private double[][] freshHeuristic(List<ProductTable> productsTables, double[][] strokeMatrix){
        
        productsTables = sortProductList(productsTables);
        
        for (ProductTable productsTable : productsTables) {
            
            System.out.println("\nREQ. DEL PRODUCTO "+productsTable.productKey+": "+Arrays.toString(productsTable.productRequirements));
            
            int lot_size_rule = LOT_SIZING_RULES[1];
            productsTable.productRequirements = MPSof(productsTable,lot_size_rule);
            
            System.out.println("\nMPS DEL PRODUCTO "+productsTable.productKey+": "+Arrays.toString(productsTable.productRequirements));
            
            for (int t = 0; t<PERIOD_LENGTH; t++) {
                
                if (productsTable.productRequirements[t] > 0) {
                    Struct beststroke = bestStroke(productsTable.productKey, t, productsTable.productRequirements[t]);
                    
                    if(beststroke != null){
                        double value = beststroke.RunsXTimes;
                        strokeMatrix[beststroke.strokeKey-1][beststroke.timeWhenRuns] += value;

                        productsTable.productRequirements[t] = 0.0;
                        productsTables = updateProductsTables(beststroke, productsTables);
                    }
                    else{
                        throw new IndexOutOfBoundsException("Couldn't complete request of product "+(char)(productsTable.productKey +64)+", with amount: "+productsTable.productRequirements[t]+". Not enough time: t="+t+". Try increasing the planning horizon.");
                    }
                }
            }
            for (int p=0;p<productsTables.size();p++) {
                
                if (productsTables.get(p).hasNoRequirements() && productsTables.size()>1) {
                    productsTables.remove(p);
                    p=-1; //Para que comience a evaluar la lista desde el principio nuevamente
                }
                else if(productsTables.get(p).hasNoRequirements() && productsTables.size()==1){
                    return strokeMatrix;
                }
            }
            
            return freshHeuristic(productsTables, strokeMatrix);
        }
        
        return strokeMatrix;
        
    }
            
        /**
         * Method to determine if the product A can be descendant of the product B.
         * @param A
         * @param B
         * @return True if A belongs to the list of all possible descendants of B.
         */
        private boolean canBeDescendantOf(int A, int B){

            MatrixHandling mymatrix = new MatrixHandling();
            List<Integer> descendantsB = mymatrix.getDescendantsofProduct(B, OUTPUT, AND_OR);
            List<Integer> descendantsA = mymatrix.getDescendantsofProduct(A, OUTPUT, AND_OR);

            for(Integer descendantsB1 : descendantsB){
                if(descendantsB1 == A) {
                    for(Integer descendantsA1 : descendantsA){
                        if(descendantsA1 == A) return false;
                    }
                    return true;
                }
            }        
            return false;
        }
    
        /**
         * Method to sort the list of products and put first those which contain others.
         * @param productsTables
         * @return The sorted list.
         */
        private List<ProductTable> sortProductList(List<ProductTable> productsTables){

            int n = productsTables.size();
            if(n == 1){
                return productsTables;
            }else if(productsTables.isEmpty()) return null;

            boolean swapped = true;
            int j = 0;
            while (swapped) {
                  swapped = false;
                  j++;
                  for (int i = 0; i < n - j; i++) {                                       
                        if (canBeDescendantOf(productsTables.get(i).productKey, productsTables.get(i+1).productKey)) {                          
                              ProductTable tmp = productsTables.get(i);
                              ProductTable tmp2 = productsTables.get(i+1);
                              productsTables.set(i, tmp2);
                              productsTables.set(i+1, tmp);
                              swapped = true;
                        }
                  }
            }

            return productsTables;
        }
        
        /**
         * Updates the tables of products in the FreshHeuristic method. Adds the new requisitions of products given a single stroke.
         * @param stroke
         * @param productsTables
         * @return The list augmented in new product requisitions.
         */
        private List<ProductTable> updateProductsTables(Struct stroke, List<ProductTable>productsTables){
        
            for(int i = 0; i < NUMBER_OF_PRODUCTS; i++){
                int count=0;
                if(INPUT[i][stroke.strokeKey-1] > 0){
                    for(int p=0;p<productsTables.size();p++){
                        if(productsTables.get(p).productKey == i+1){
                            count++;
                            productsTables.get(p).productRequirements[stroke.timeWhenRuns] += stroke.RunsXTimes * INPUT[i][stroke.strokeKey-1];
                        }        
                    }
                    if(count==0){
                        ProductTable thisProduct = new ProductTable(PERIOD_LENGTH);
                        thisProduct.productKey = i+1;
                        thisProduct.productRequirements[stroke.timeWhenRuns] += stroke.RunsXTimes * INPUT[i][stroke.strokeKey-1];

                        productsTables.add(thisProduct);
                    }
                }
            }
            return productsTables;
    }
        
        /**
         * Method to estimate the cheapest stroke to run at a period of time having the product key and the number of times that needs to be performed.
         * @param productKey
         * @param time
         * @param amount
         * @return A <Struct> including the information of the cheapest stroke.
         */
        private Struct bestStroke(int productKey, int time, double amount){
            Struct bestStroke;
            List<Integer> strokes = getMainStrokes(productKey);
            List<Struct> structStrokes = new ArrayList();
            
            
            Iterator<Integer> it = strokes.iterator();
            while(it.hasNext()){
                
                int strokeKey = it.next();
                double lt = LEAD_TIME[strokeKey-1];
                
                if(time >= lt) {
                    Struct mystruct = new Struct();
                    mystruct.strokeKey = strokeKey;
                    mystruct.timeWhenRuns = (int)(time-lt);
                    int temp = (int)(amount/OUTPUT[productKey-1][strokeKey-1]);
                    if(amount % OUTPUT[productKey-1][strokeKey-1] != 0) temp++;
                    mystruct.RunsXTimes = temp;
                    structStrokes.add(mystruct);
                }
            }

            double[] costs = new double[structStrokes.size()];
            for(int c=0; c<structStrokes.size();c++){
                costs[c] = getStrokeTotalCosts(structStrokes.get(c).strokeKey, amount);
            }
            double[] copy = new double[structStrokes.size()];
            System.arraycopy(costs, 0, copy, 0, structStrokes.size());
            Arrays.sort(copy);
            //Sort ordena de menor a mayor.
            int stroke=0;
            for(int i=0;i<structStrokes.size();i++){
                if(copy[0] == costs[i]) stroke = i;
            }
            int index=0;
            for(int c=0; c<structStrokes.size();c++){
                if (structStrokes.get(c).strokeKey == stroke) index = c;
            }
            if(structStrokes.isEmpty()) return null;
            bestStroke = structStrokes.get(index);
            
            return bestStroke;
        }
        
        /**
         * Generates a list of the strokes that produce the main product.
         * @param MPN The key of the main product to be tested.
         * @return A list of the strokes that produce the main product.
         */
        private List<Integer> getMainStrokes (int MPN){
            int numStrokes = OUTPUT[0].length;

            List<Integer> RelMainStrokes = new ArrayList();
            for (int j=0; j<numStrokes;j++){
                if(OUTPUT[MPN-1][j]>0) {
                    int temp = j+1;
                    RelMainStrokes.add(temp);
                }
            }
            return RelMainStrokes;
        }
        
        /**
         * Calculates the total estimated accumulative costs related to a stroke, these are: Setup costs and Operations costs.
         * @param strokeKey
         * @param mps
         * @return The total estimated accumulative cost.
         */
        private double getStrokeTotalCosts(int strokeKey, double mps){
            double total;
            double OC = ACCUMULATED_OC[strokeKey-1]; //TIMES MPS
            double SC = ACCUMULATED_SC[strokeKey-1]; //Independent of MPS
            total = OC*mps + SC;
            return total;
        }
        
        /**
         * The table that contains the estimated accumulative Setup costs for every stroke.
         * @return A table that contains the estimated accumulative Setup costs for every stroke.
         */
        public final double[] getAccumSCTable(){
            
            double[] sc = new double[NUMBER_OF_STROKES];
            MatrixHandling matrix = new MatrixHandling();
            
            for(int i=0;i<OUTPUT[0].length;i++){
                TreeDouble mytree = matrix.Transform2Tree(i+1, AND_OR);
                sc[i] = matrix.getAccumSC(mytree.getRoot(),AND_OR, SETUP_COSTS, OUTPUT);
            }
            return sc;
        }
        
        /**
         * The table that contains the estimated accumulative Operations costs for every stroke.
         * @return A table that contains the estimated accumulative Operations costs for every stroke.
         */
        public final double[] getAccumOCTable(){
            
            double[] oc = new double[NUMBER_OF_STROKES];
            MatrixHandling matrix = new MatrixHandling();

            for(int i=0;i<OUTPUT[0].length;i++){
                TreeDouble mytree = matrix.Transform2Tree(i+1, AND_OR);
                oc[i] = matrix.getAccumOC(mytree.getRoot(), AND_OR, OPERATION_COSTS, OUTPUT);
            }
            return oc;
        }
        
        /**
         * Indicates which product produces a given stroke.
         * @param strkKey The stroke that produces the product.
         * @return The product produced by the stroke.
         */
        private int getProductfromStroke(int strkKey){
                for(int i=0;i<OUTPUT.length;i++){
                    if(OUTPUT[i][strkKey-1] > 0){
                        return i+1;
                    }
                }
            return 0;
        }
        
    /**
     * Method to calculate when, how many and which product produces every stroke in the finalStrokeProduction matrix.
     * @return The planned receipts for the entire production plan.
     */
    public final double[][] getPlannedReceipts(){
        plannedReceipts = new double[NUMBER_OF_PRODUCTS][PERIOD_LENGTH];
        for(int j = 0; j < NUMBER_OF_STROKES; j++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                if(finalStrokeProduction[j][m] > 0.0){
                    int index = getProductfromStroke(j+1)-1;
                    plannedReceipts[index][m+(int)(LEAD_TIME[j])] = finalStrokeProduction[j][m] * OUTPUT[index][j];
                }
            }
        }
        return plannedReceipts;
    }
    
    /**
     * Method that runs the FreshHeuristic.
     */
    private void runsHeuristic(){
        
        List<ProductTable> productList = new ArrayList();

        for (Integer END_ITEMS1 : END_ITEMS) {
            ProductTable myTable = new ProductTable(PERIOD_LENGTH);
            myTable.productKey = END_ITEMS1;
            productList.add(myTable);
        }

        double[][] strokeMatrix = new double[NUMBER_OF_STROKES][PERIOD_LENGTH];

        strokeMatrix = freshHeuristic(productList, strokeMatrix);
        
        if (strokeMatrix == null){
            int[] names = new int[productList.size()];
            for(int c=0;c<productList.size();c++){
                names[c] = productList.get(c).productKey;
            }
            System.out.println("There is not enough time to produce: "+Arrays.toString(names));
        }
        finalStrokeProduction = strokeMatrix;        
    }
    
    /**
     * Returns the number of strokes in this object.
     * @return The number of strokes in this object.
     */
    public int getNumberOfStrokes(){
        return NUMBER_OF_STROKES;
    }
    
    /**
     * Gets the calculation of the products that are for sale to final customers.
     * @return A list with the end items.
     */
    public final List<Integer> getEndItems(){
        return END_ITEMS;
    }
    
    /**
     * The inventory after performed the production planning.
     * @return The inventory.
     */
    public double[][] getInventory(){
        return INVENTORY;
    }
    
    /**
     * Gets the calculation of the costs generated for holding products in-stock.
     * @return A matrix with costs per product per time.
     */
    public double[][] getInventoryCosts(){
        updateInventoryCosts();
        return inventoryCosts;
    }
    
    /**
     * Gets the costs generated for Stock outs.
     * @return A matrix that contains the costs per product, per time.
     */
    public double[][] getStockOutCosts(){
        updateStockOutCosts();
        return stockOutCosts;
    }
    
    /**
     * Returns the Main Production Schedule for the end items.
     * @return The Main Production Schedule
     */
    public double[][] getMPS(){
        return MAIN_PRODUCTION_SCHEDULE;
    }
    
    /**
     * Calculates the final production of strokes after the whole planning.
     * @return A matrix with the number of times to be executed per stroke, per time.
     */
    public double[][] getFinalStrokeProduction(){
        return finalStrokeProduction;
    }
    
    /**
     * Returns the projected requirements of each product or component.
     * @return The projected requirements of each product or component.
     */
    public double[][] getProjectedRequirements(){
        return PROJECTED_REQUIREMENTS;
    }
    
    /**
     * Returns the scheduled receipts of each product or component.
     * @return The scheduled receipts of each product or component.
     */
    public double[][] getScheduledReceipts(){
        return SCHEDULED_RECEIPTS;
    }
    
    /**
     * The amount of product missing to be produced every time.
     * @return A matrix with the Stock outs.
     */
    public double[][] getStockOuts(){
        return STOCK_OUTS;
    }
    /**
     * The time in nanoseconds to run the entire constructor.
     * @return 
     */
    public long getExecutionTime(){
        return EXECUTION_TIME;
    }
    
    /**
     * Gathers all the costs: stroke-related (setup and operations costs) and inventory-related (holding and stock out costs).
     * @return The total cost of the production plan.
     */
    public double getTotalProductionCost(){
        double total=0;
        for(int j = 0; j < NUMBER_OF_STROKES; j++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                total += finalStrokeProduction[j][m]*OPERATION_COSTS[j] + SETUP_COSTS[j];
            }
        }
        updateInventoryCosts();
        updateStockOutCosts();
        for(int j = 0; j < NUMBER_OF_PRODUCTS; j++){
            for(int m = 0; m < PERIOD_LENGTH; m++){
                total += inventoryCosts[j][m] + stockOutCosts[j][m];
            }
        }
        return total;
    }
    
    public void printStrokeVsTime (double[][] matrix){
        System.out.println();
        for(int i=0;i<matrix.length+1;i++){
            
            if(i!=0) System.out.print("\nS"+(i)+"\t");
            for(int j=0;j<matrix[0].length;j++){
                if(i==0) {
                    System.out.print("\tt="+(j+1));
                }else{
                    System.out.print(matrix[i-1][j]+"\t");
                }
            }
        }
        System.out.println();
    }
    
    public void printProductVsTime (double[][] matrix){
        System.out.println();
        for(int i=0;i<matrix.length+1;i++){
            int num = i+64;
            if(i!=0) System.out.print("\n"+(char)num+"\t");
            for(int j=0;j<matrix[0].length;j++){
                if(i==0) {
                    System.out.print("\tt="+(j+1));
                }else{
                    System.out.print(matrix[i-1][j]+"\t");
                }
            }
        }
        System.out.println();
    }
    public void printCost(double cost){
        String out = NumberFormat.getCurrencyInstance().format(cost);
        System.out.print(out);
    }
    
    
    //FINALS
    private final double[][] PROJECTED_REQUIREMENTS;//Posible problema: que el usuario ingrese una matriz de END_ITEMS filas, pues solo de estos productos se tienen requerimientos
    private final double[][] SCHEDULED_RECEIPTS;
    private final int PLANNING_HORIZON;
    private final double[] FOQ;
    private final int[] LOT_SIZING_RULES;
    private final double[][] INPUT; //Posible problema: que el usuario ingrese solo inventario inicial, pues es el unico del que tiene conocimiento
    private final double[][] OUTPUT;
    private final int PERIOD_LENGTH;
    private final double[] LEAD_TIME;
    private final int NUMBER_OF_PRODUCTS;
    private final int NUMBER_OF_STROKES;
    private final List<Integer> END_ITEMS;
    private final int NUMBER_OF_END_ITEMS;
    private final double[][] MAIN_PRODUCTION_SCHEDULE;
    private final double[] INITIAL_INVENTORY;
    private final double[] HOLDING_COSTS;
    private final double[] STOCK_OUT_PRODUCT_COST;
    private final double[] OPERATION_COSTS;
    private final double[] SETUP_COSTS;
    private final double[] ACCUMULATED_SC;
    private final double[] ACCUMULATED_OC;
    private final double[][][] AND_OR;
    private final double[][] INVENTORY;
    private final double[][] STOCK_OUTS;
    private final long EXECUTION_TIME;
    
    //not Finals
    private double[][] inventoryCosts;
    private double[][] stockOutCosts;
    private double[][] plannedReceipts;
    private double[][] finalStrokeProduction;
   
}
