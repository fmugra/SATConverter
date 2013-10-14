
package model.pseudoBoolean;

/** Represents a Constraint in a PBS or PBO problem specification
 *  Constraints consist of a at least one Summand, a target, and an Operator (equals or greaterequal) to compare them
 *  Summand1 "+" Summand2 "+" ... "+" SummandN operator target
 *  e.g. 2x1 + 5x2x3 = 15
 *
 * @author Frank Mugrauer
 */
public class Constraint {
    private int target;
    private Operator op;
    private Summand[] summands;

    public Constraint(int target, Operator op, Summand[] summands){
        this.target = target;
        this.op = op;
        this.summands = summands;
    }

    public Summand[] getSummands() {
        return summands;
    }

    public Operator getOp() {
        return op;
    }

    public int getTarget() {
        return target;
    }

    public int countSummands(){
        return summands.length;
    }
}
