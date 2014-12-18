    public double getAccumOC(nodo, and_or, OCk, Nik){
        grafoActual = construirGrafo(nodo, and_or);
        listaNodosHijos = nodo.obtenerHijos();
        double accumAND = 0.0;
        double accumOR = 0.0;
        if(listaNodoHijos.estaVacia()){
            nodo.setOCAcumulado(getOCk(nodo.getNombre(), OCk));
            return relative.getOCAcumulado();
        }

        valorStrokesAND = obtenerHijosAND(listaNodosHijos);
        valorStrokesORporProducto = obtenerHijosORporProducto(listaNodosHijos, Nik);

        for (int i=0;i<valorStrokesAND.size();i++){
            accumAND += valorStrokesAND(i).getValor()*getAccumOC(grafoActual.getNodo(valorStrokesAND(i).getKey(),nodo), and_or, OCk, Nik);
        }

        double temp = 0.0;

        for (int i=0;i<valorStrokesOR.size();i++){
            for(int k=0;k<valorStrokesORporProducto(i).productosenOR.size();k++){
                double value = mytree.getNode(e.getValue()[k], relative).getValue();
                temp += value*getAccumOC(mytree.getNode(e.getValue()[k],relative), and_or, oc, OUTPUT);
            }
            temp = temp/e.getValue().length;
            accumOR += temp;
        }
        double accumValue = getRelOC(relative.getKey(), oc)+accumAND+accumOR;
        relative.setAccumValue(accumValue);
        return relative.getAccumValue();
        }
        = seleccionarStroke(i,t, listaProductos(i).mps(t))
        private Struct seleccionarStroke(i, t, listaProductos(i).mps(t)){
            double amount = listaProductos(i).mps(t);
            Struct bestStroke;
            List strokes = obtenerStrokes(i); //main strokes
            List structStrokes = new ArrayList();


            Iterator<Integer> it = strokes.iterator();
            for(int s =0;s<strokes.length;c++){

                int strokeKey = strokes(c);
                double lt = LEAD_TIME[strokeKey];

                if(t >= lt) {
                    Struct mystruct = new Struct();
                    mystruct.strokeKey = strokeKey;
                    mystruct.timeWhenRuns = (int)(time-lt);
                    int temp = (int)(amount/OUTPUT[i-1][i-1]);
                    if(amount % OUTPUT[i-1][i-1] != 0) temp++;
                    mystruct.RunsXTimes = temp;
                    structStrokes.add(mystruct);
                }else{
                  strokes.delete(c);
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
