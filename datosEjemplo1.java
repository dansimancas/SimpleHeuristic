double[][] output = {{1,0,0,1,1,0,0,0,0},{0,1,0,0,0,0,0,1,0},{0,0,1,0,0,0,0,0,0},{0,0,0,0,0,1,0,0,0},{0,0,0,0,0,0,1,0,0},{0,0,0,0,0,0,0,0,1}};
double[][] input = {{0,0,0,0,0,0,0,0,0},{2,0,0,0,3,0,0,0,0},{3,0,0,0,0,0,0,0,0},{0,2,0,0,1,0,0,0,0},{0,0,0,0,0,1,0,0,0},{0,1,0,0,0,0,0,0,0}};
double[][] proj_req = {{500.0,600.0,400.0,950.0,800.0,300.0,300.0,300.0,100.0,200.0},{0.0,0.0,0.0,0.0,0.0,0.0,230.0,100.0,347.0,900.0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0}};
double foq = 1000;

double[] initialInv = {200.0,150.0,30.0,20.0,90.0,10.0};
double[] hc = {500.0,600.0,560.0,360.0,210.0,356.0};
double[] lt = {1.0,2.0,1.0,5.0,1.0,2.0,1.0,3.0,1.0};
double[] SC = {2000.0,1800.0,1500.0,18000.0,7300.0,4000.0,1800.0,3000.0,2500.0};
double[] OC = {2.0,0.5,0.5,0.5,2.0,1.0,0.5,0.5,0.5};
double[] SOC = {500.0,600.0,400.0,950.0,210.0,356.0};
int horizonte = 5;

FreshStart firstfresh = new FreshStart(proj_req, horizonte,foq,input, output, lt, initialInv, hc, OC, SC,SOC);

//Outputs del sistema
System.out.println("\nFINAL STROKE PRODUCTION: \n");
System.out.println(Arrays.deepToString(firstfresh.getFinalStrokeProduction()).replace("], ", "]\n"));
System.out.println("\nPLANNED RECEIPTS: \n");
System.out.println(Arrays.deepToString(firstfresh.getPlannedReceipts()).replace("], ", "]\n"));
System.out.println("\nINVENTORY AFTER PRODUCTION: \n");
System.out.println(Arrays.deepToString(firstfresh.getInventory()).replace("], ", "]\n"));
System.out.println("\nSTOCK OUTS: \n");
System.out.println(Arrays.deepToString(firstfresh.getStockOuts()).replace("], ", "]\n"));
System.out.println("\nTOTAL PRODUCTION COST: $ "+firstfresh.getTotalProductionCost()+"\n");
