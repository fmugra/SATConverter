
package converters.labs_to_pbs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import model.basic.Literal;
import model.basic.Variable;
import model.basic.VariableNames;
import model.labs.LABS_Instance;
import model.pseudoBoolean.Constraint;
import model.pseudoBoolean.Operator;
import model.pseudoBoolean.PBS_Instance;
import model.pseudoBoolean.Summand;

/** Converts a problem specification for the LABS (Low Autocorrelation Binary Sequence) problem into
 *  a problem specification for the PBS (Pseudo Boolean Satisfaction) problem
 *  targets are interpreted as values for the energy of the sequence
 *
 * @author Frank Mugrauer
 */
public class LABS_to_PBS {
    private static final Operator op = Operator.GREATEREQUAL;
    private static int targetModifier;
    private static int numberOfBits;

    public static PBS_Instance[] convert(LABS_Instance labs){
        targetModifier = 0;
        numberOfBits = labs.getNumberOfBits();
        //transformation
        Term t = createEquation();
        t = multiplySquares(t);
        t = trim(t);
        //System.out.println("targetModifier: "+targetModifier+", numberOfTerms: "+t.size());
        List<Summand> summands = transformToBoolean(t);
        summands = reverseSummandSigns(summands);

        //calculate targets and complete constraints
        PBS_Instance[] pbsInstances= new PBS_Instance[labs.getTargets().length];
        int count = 0;
        Constraint c;
        for(int target : labs.getTargets()){
            Summand[] smds = new Summand[summands.size()];
            smds = summands.toArray(smds);
            int adjustedTarget = (-1 * target) + targetModifier;
            c = new Constraint(adjustedTarget, Operator.GREATEREQUAL, smds);
            int p = numberOfBits;
            p = p * p * p * p;
            pbsInstances[count] = new PBS_Instance(c, numberOfBits, p, p);
           count++;
        }
        return pbsInstances;
    }

    /* creates an equation that represents the energy of the autocorrelation of a LABS problem
     * The Term class does not contain any explicit operators; the resulting Term should be
     *      interpreted as a sum of squares of sums of products of Atoms, e.g.:
     *          a term that says
     *          ( ((x1x2) (x3x4)) ((x2x3) (x5x3)) ((x1x5)) )
     *          should be interpreted as
     *          ( (x1x2 + x3x4)^2) + ((x2x3 + x5x3)^2) + ((x1x5)^2) )
     * (formula for the equations can be found here: http://www2.lirmm.fr/chocolib/prob/prob005/spec.php)
     */
    private static Term createEquation(){
        Variable[] vars = new Variable[numberOfBits];
        for(int i=0; i<vars.length; i++)
            vars[i] = new Variable(VariableNames.newVarID());
        Term equation = new Term();
        for(int k=1; k<=numberOfBits; k++){
            Term term = new Term();
            for(int i=1; i<=(numberOfBits-k); i++){
                Atom si = new Atom(false, vars[i-1]);
                Atom sik = new Atom(false, vars[i+k-1]);

                Term tmp = new Term();
                tmp.addLast(new Term(si));
                tmp.addLast(new Term(sik));

                term.addLast(tmp);
            }
            if(!term.isEmpty())
                equation.addLast(term);
        }
        return equation;
    }

    /* takes a term of depth 4, and transforms it into a term of depth 2 by squaring all elements:
     *      Input Term should be interpreted as a sum of squares of sums of products of Atoms:
     *          ( (x1x2 + x3x4)^2) + ((x2x3 + x5x3)^2) + ((x1x5)^2) +  ...
     *      Output Term should be interpreted as a sum of products of Atoms:
     *          (x1x2) + (2 x3x4x1x2) + (2 x4x2x1x6) + ...
     * Note: This method should be called directly after createEquation()
     *
     */
    private static Term multiplySquares(Term original){
        Term res = new Term();
        while(!original.isEmpty()){
            Term source = original.removeFirst();
            //source contains a variable number of smaller terms, each of which contains precisely 2 variables
            List<Term> sourceNodes = source.getTerms();
            //in this loop, we take a term out of source, then square it, and put the results in sink
            //e.g. (x1 x2)   -> ((x1 x1) (2 x1 x2) (x2 x2))
           for(int i=0; i<sourceNodes.size(); i++){
                for(int j=i; j<sourceNodes.size(); j++){
                    Term tmp = new Term();
                    if(j>i)
                        tmp.addLast(new Term(new Atom(2)));
                    tmp.addAll(sourceNodes.get(i).getTerms());
                    tmp.addAll(sourceNodes.get(j).getTerms());
                    res.addLast(tmp);
                }
            }
        }
        return res;
    }

    /* straightens out the terms by
     *      - removing duplicate variables, e.g. (x1 x2 x1 x3) becomes (x2 x3), (x1 x1) becomes 1
     *              (Note: x1 * x1 = 1, since all variables are in {-1,1})
     *      - combining terms when possible, e.g. (2 x1 x2) + (2 x2 x1) becomes (4 x1 x2)
     *      - removes constant parts from the term and sums them up in the static targetModifier field
     *              e.g. a term (1 + (2 x1 x2) + 1 + (4 x1 x2 x3 x4) + 1) becomes ((2 x1 x2) + (4 x1 x2 x3 x4))
     *              and the targetModifier becomes 3
     * Note:    This method expects a Term of depth 2: a sum of products of Atoms
     *              e.g. ((x1 x2) (2 x3 x1 x1 x2) (x2 x3 x1 x4))
     *          This method needs to be called after multiplySquares()
     */
    private static Term trim(Term original){
        //remove duplicate variables and, in the process, sort variables: e.g. (x2 x3 x1 x3) -> (x1 x2)
        boolean[] containsvars = new boolean[numberOfBits];
        Term res = new Term();
        while(!original.isEmpty()){
            for(int i=0; i<containsvars.length; i++)
                        containsvars[i] = false;
            Term source = original.removeFirst();
            Term sink = new Term();
            for(Term t : source.getTerms()){ //t is an Atom
                if(!t.getA().isVariable())
                    sink.addLast(t);
                else{ //t is an Variable
                    int varID = t.getA().getVariable().getID();
                    if(containsvars[varID-1]) // duplicate variables cancel each other out
                        containsvars[varID-1] = false; //(x1*x1) = (1*1) = (-1*-1) = 1
                    else
                        containsvars[varID-1] = true;
                }
            }
            for(int i=0; i<containsvars.length; i++){
                if(containsvars[i])
                    sink.addLast(new Term(new Atom(false, new Variable(i+1))));
            }
            if(sink.isEmpty())
                sink = new Term(new Atom(1));
            res.addLast(sink);
        }

        //combine Terms and remove constant terms
        Term source = res;
        HashMap<Term,Integer> hm= new HashMap<Term,Integer>();
        while(!source.isEmpty()){
            int i;
            Term t = source.removeFirst();
            if(t.isAtom()){ //this means the Term contains only "(1)"
                targetModifier++;
                continue;
            }else if(!t.peekFirst().getA().isVariable())
                i = t.removeFirst().getA().getInteger();
            else
                i = 1;
            if(hm.containsKey(t)){
                i = i + hm.remove(t);
            }
            hm.put(t, i);
        }
        res = new Term();
        for(Entry<Term,Integer> entry : hm.entrySet()){
            Term t = entry.getKey();
            t.addFirst(new Term(new Atom(entry.getValue())));
            res.addLast(t);
        }
        return res;
    }

    /* transforms the equations from arithmetic to boolean (i.e. variable domains are not {true, false} rather
     * than {-1,1}
     *  e.g. a Term (2 x1 x2) becomes (-2 + (4 x1 ~x2) + (4 ~x1 x2))
     * The method then removes all constant parts it creates, and sums them up in the static targetModifier field
     * This method needs to be called afer trim()
     */
    private static List<Summand> transformToBoolean(Term source){
        LinkedList<Summand> summands= new LinkedList<Summand>();
        while(!source.isEmpty()){
            Term term = source.removeFirst();
            if(term.peekFirst().getA().isVariable())//most terms have constant multipliers of 2, 4, etc, but some
                term.addFirst(new Term(new Atom(1)));//might not have a constant multiplier, so we multiply with 1
            int multiplier = term.removeFirst().getA().getInteger();
            targetModifier = targetModifier - multiplier;

            //create Summands
            int weight = multiplier*2;
            Variable[] vars = new Variable[term.size()];
            int count=0;
            for(Term t : term.getTerms()){
                vars[count] = t.getA().getVariable();
                count++;
            }
            Summand s;
            Literal[] lits;
            if(vars.length == 2){
                final boolean[] neg1 = {true,false};
                final boolean[] neg2 = {false,true};
                lits = createLiteralArray(vars,neg1);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg2);
                summands.add(new Summand(weight, lits));
            }
            else if(vars.length == 4){
                final boolean[] neg1 = {true,true,true,true};
                final boolean[] neg2 = {false,false,false,false};
                final boolean[] neg3 = {false,false,true,true};
                final boolean[] neg4 = {false,true,false,true};
                final boolean[] neg5 = {false,true,true,false};
                final boolean[] neg6 = {true,false,false,true};
                final boolean[] neg7 = {true,false,true,false};
                final boolean[] neg8 = {true,true,false,false};
                lits = createLiteralArray(vars,neg1);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg2);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg3);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg4);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg5);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg6);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg7);
                summands.add(new Summand(weight, lits));
                lits = createLiteralArray(vars,neg8);
                summands.add(new Summand(weight, lits));
            }
            else
                System.out.println("ERROR: A term ("+term.toString()+") has neither 2 nor 4 variables!");
        }
        return summands;
    }

    /* creates an array of Literals from an array of variables and an array of negations
     */
    private static Literal[] createLiteralArray(Variable[] vars, boolean[] negations){
        Literal[] lits = new Literal[vars.length];
        for(int i=0; i<vars.length; i++)
            lits[i] = new Literal(vars[i].getID(), negations[i]);
        return lits;
    }

    /* reverses the signs on all summands so that a greaterequal Operator can be used instead of a lesserequal
     * when making constraints
     * This method should be called after transformToBoolean()
     */
    public static List<Summand> reverseSummandSigns(List<Summand> smds){
        LinkedList<Summand> summands = new LinkedList<Summand>();
        for(Summand s : smds){
            s.flipSign();
            summands.add(s);
        }
        return summands;
    }
}