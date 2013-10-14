
package model.basic;

/** Base type for variables, has a name and nothing else
 *  see VariableNames for an easy way to generate unique names for your variables
 *
 * @author Frank Mugrauer
 */
public class Variable {
    private int id;

    public Variable(int id) {
        this.id = id;
    }

    public String getName() {
        return "x"+id;
    }

    public int getID(){
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.id;
        return hash;
    }
}
