package model.basic;

/** Represents a literal in a boolean term
 *  Literals have a name, and can be negated
 *
 * @author Frank Mugrauer
 */
public class Literal extends Variable{
    public static final boolean NEGATED = true,
                                VANILLA = false;
    private boolean negated;

    public Literal(int id){
        super(id);
        this.negated = VANILLA;
    }
    public Literal(int id, boolean negated){
        super(id);
        this.negated = negated;
    }

    public boolean isNegated() {
        return negated;
    }
}
