/**
 * Calculates the when, which and how many are the required strokes for the entire production plan.
 * @param productsTables
 * @param strokeMatrix
 * @return A matrix with length: the number of strokes and width: the period length.
 */
private double[][] freshHeuristic(List<ProductTable> productsTables, double[][] strokeMatrix){

    productsTables = sortProductList(productsTables);

    for (ProductTable productsTable : productsTables) {

        int lot_size_rule = 2;
        productsTable.productRequirements = MPSof(productsTable,lot_size_rule);

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
                    productsTable.productRequirements[t] = 0.0;
                    System.out.println("Couldn't complete request: ["+productsTable.productKey+"] with amount: "
                    + "["+productsTable.productRequirements[t]+"]. Not enough time: t="+t+". Moving on to day "+(t+1)+".");
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
