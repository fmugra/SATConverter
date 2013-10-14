
package converters.labs_to_pbs;

import java.util.LinkedList;
import java.util.List;

/** Represents a number of elements (either Atoms or Terms), that are all connected by the same operator
 *  within an equation, e.g.:
 *  1 + 5 + -11 + 18
 *  x1 * -x5 * 4
 *  (1 + 4 + x3) * 5 * -3 * -x3
 *
 * @author Frank Mugrauer
 */
public class Term {
    private boolean isAtom;
    private Atom a;
    private LinkedList<Term> terms;

    public Term(Atom a){
        this.isAtom = true;
        this.a = a;
        this.terms = null;
    }
    public Term(){
        this.isAtom = false;
        this.a = null;
        this.terms = new LinkedList<Term>();
    }

    public Atom getA() {
        return a;
    }

    public boolean isAtom() {
        return isAtom;
    }

    public LinkedList<Term> getTerms() {
        return terms;
    }

    public Term peekFirst(){
        return terms.get(0);
    }

    public Term removeFirst(){
        return terms.remove();
    }

    public void addLast(Term t){
        terms.addLast(t);
    }

    public void addFirst(Term t){
        terms.addFirst(t);
    }

    public void addAll(List<Term> terms){
        this.terms.addAll(terms);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Term other = (Term) obj;
        if (this.isAtom != other.isAtom) {
            return false;
        }
        if (this.a != other.a && (this.a == null || !this.a.equals(other.a))) {
            return false;
        }
        if (this.terms != other.terms && (this.terms == null || !this.terms.equals(other.terms))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.isAtom ? 1 : 0);
        hash = 67 * hash + (this.a != null ? this.a.hashCode() : 0);
        hash = 67 * hash + (this.terms != null ? this.terms.hashCode() : 0);
        return hash;
    }

    

    @Override
    public String toString(){
        String s = "";
        if(this.isAtom)
            return a.toString();
        if(!isEmpty()){
            for(Term t : terms)
                s = s + "(" + t.toString() + ") ";
            return s.substring(0, s.length()-1); //cut off superfluous " o "
        }else{
            return s;
        }
    }

    public int size(){
        return terms.size();
    }
    public boolean isEmpty(){
        return !isAtom && size()==0;
    }
}
