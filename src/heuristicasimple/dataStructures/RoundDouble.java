/*
 * By Daniela Simancas Mateus
 */
package heuristicasimple.dataStructures;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Daniela
 */
public class RoundDouble {
    public RoundDouble(){};
    public double roundHalfEven(double value, int positions){
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(positions, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }
}
