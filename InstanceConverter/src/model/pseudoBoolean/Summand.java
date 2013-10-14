
package model.pseudoBoolean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import model.basic.Literal;

/** Represents a Summand in a PBS- or PBO Constraint
 *  Summands have a weight, and can have one or more factors (each factor is a literal)
 *  weight "*" factor1 "*" factor2 "*" ... "*" factorN
 *
 * @author Frank Mugrauer
 */
public class Summand {
    private int weight;
    private Literal[] factors;


    public Summand(int weight){
        this.weight = weight;
        this.factors = new Literal[0];
    }
    public Summand(int weight, Literal[] factors){
        this(weight);
        this.factors = factors;
    }

    public int getWeight(){
        return weight;
    }
    public Literal[] getFactors(){
        return factors;
    }

    /* flips the sign of this summand, e.g. (2 x1 x2) becomes (-2 x1 x2)
     */
    public void flipSign(){
        weight = weight * -1;
    }
}