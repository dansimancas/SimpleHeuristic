SimpleHeuristic(OCk, SCk, HCi, Nik, Mik){

  SSk = Nik(t)*Mik;
  G(N,A)/N <- k, A <- Nik;
  estimateAccumOC(G(N,A),OCk);
  estimateAccumSC(G(N,A),SCk);

  List recursiveAlgorithm(productList,strokeList){

    sortProductList();
    for(int i = 0; i < CARD(i); i++){

      for(int t = 0; t < CARD(t); t++){
        MPSHeuristic();

        if(projectedRequirements(i,t) > 0){
          for(int k = 0; k < CARD(k); k++){
            if( isRelatedTo(i,k) && (t - LeadTimeOf(k) > 0) ){
              optionCost(i,k,t - LeadTimeOf(k));
            }
          }

          stroke(k,t) = argMin(optionCost(i,k,t - LeadTimeOf(k)));
          strokeList.add(stroke(k,t));
          productList.add(productsUnder(stroke(k,t)));
        }
      }

      return recursiveAlgorithm(productList,strokeList);
    }
    return strokeList;
  }
}


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
