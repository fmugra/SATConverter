
package converters.labs_to_pbs;

import model.basic.Variable;

/** Represents a part of the Equation which does not need to be broken down further.
 *  This can be an Integer or a Variable, and it can be negated
 *
 * @author Frank Mugrauer
 */
public class Atom {
    private boolean isVariable;
    private boolean isNegated;
    private Variable v;
    private int i;

    public Atom(boolean isNegated, Variable v) {
        this.isVariable = true;
        this.isNegated = isNegated;
        this.v = v;
        this.i = 0;
    }

    public Atom(int i) {
        this.isVariable = false;
        this.isNegated = (i<0);
        this.v = null;
        this.i = (i<0) ? -1*i : i; //abs(i)
    }

    public int getInteger() {
        return i;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public Variable getVariable() {
        return v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Atom other = (Atom) obj;
        if (this.isVariable != other.isVariable) {
            return false;
        }
        if (this.isNegated != other.isNegated) {
            return false;
        }
        if (this.v != other.v && (this.v == null || !this.v.equals(other.v))) {
            return false;
        }
        if (this.i != other.i) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.isVariable ? 1 : 0);
        hash = 37 * hash + (this.isNegated ? 1 : 0);
        hash = 37 * hash + (this.v != null ? this.v.hashCode() : 0);
        hash = 37 * hash + this.i;
        return hash;
    }



    @Override
    public String toString(){
        return ((isNegated) ? "-" : "") + ((isVariable) ? v.getName() : i);
    }
}