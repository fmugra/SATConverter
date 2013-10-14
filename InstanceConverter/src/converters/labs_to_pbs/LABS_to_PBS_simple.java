
package converters.labs_to_pbs;

import java.util.LinkedList;
import model.basic.Literal;
import model.labs.LABS_Instance;
import model.pseudoBoolean.Constraint;
import model.pseudoBoolean.Operator;
import model.pseudoBoolean.PBS_Instance;
import model.pseudoBoolean.Summand;

/** Converts a problem specification for the LABS (Low Autocorrelation Binary Sequence) problem into
 *  a problem specification for the PBS (Pseudo Boolean Satisfaction) problem
 *  targets are interpreted as maximum values for the autocorrelation of the sequence
 *
 * @author Frank Mugrauer
 */
public class LABS_to_PBS_simple {

    public static PBS_Instance[] convert(LABS_Instance l){
        PBS_Instance[] res = new PBS_Instance[l.getTargets().length];
        for(int i=0; i<res.length; i++)
            res[i] = convert(l.getNumberOfBits(), l.getTargets()[i]);
        return res;
    }

    private static PBS_Instance convert_crap(int numberOfBits, int target){
        boolean simplify = true, evenBitNum=false;
        int simplifyShift = simplify ? 1 : 0;
        if(simplify && numberOfBits%2==0){
            evenBitNum=true;
        }
        Literal[] lits;
        LinkedList<Summand> smds = new LinkedList<Summand>();
        Summand[] s1,s2;
        LinkedList<Constraint> constraints =new LinkedList<Constraint>();


        for(int i=1; i<=numberOfBits-1; i++){
            smds.clear();
            int j;
            int constFactor = ((numberOfBits-i+target)%2 == 0) ? 1 : 2;
            for(j=1; j<=numberOfBits-i; j++){
                if(simplify && (j==1 || j+i==1 || (evenBitNum && (j==numberOfBits || j+i==numberOfBits))))
                    continue;
                lits = new Literal[2];
                lits[0] = new Literal(j-simplifyShift, true);
                lits[1] = new Literal(j+i-simplifyShift, false);
                smds.add(new Summand(constFactor,lits));
                lits = new Literal[2];
                lits[0] = new Literal(j-simplifyShift, false);
                lits[1] = new Literal(j+i-simplifyShift, true);
                smds.add(new Summand(constFactor,lits));
            }
            j--; //j counts how often the previous for-loop has run, will be used at the end of the current loop

            s1 = new Summand[smds.size()];
            s2 = new Summand[smds.size()];
            int count=0;
            for(Summand s : smds){
                s1[count] = s;
                s2[count] = new Summand(-1*s.getWeight(), s.getFactors());
                count++;
            }
            int tar1 = j-target;
            int tar2 = -1*(j+target);
            if(constFactor==1){
                tar1 = tar1/2;
                tar2 = tar2/2;
            }
            if(s1.length>0){
                constraints.add(new Constraint(tar1,Operator.GREATEREQUAL,s1));
                constraints.add(new Constraint(tar2,Operator.GREATEREQUAL,s2));
            }
            //cs[(i-1)*2] = new Constraint(tar1,Operator.GREATEREQUAL,s1);
            //cs[(i-1)*2 +1] = new Constraint(tar2,Operator.GREATEREQUAL,s2);
        }

        int simplifiedNumberOfBits = numberOfBits;
        if(simplify)
            simplifiedNumberOfBits = evenBitNum ? numberOfBits-2 : numberOfBits-1;
        int product = simplifiedNumberOfBits * (simplifiedNumberOfBits-1);
        Constraint[] cs = new Constraint[constraints.size()];
        int count=0;
        for(Constraint c : constraints){
            cs[count] = c;
            count++;
        }
        return new PBS_Instance(cs, simplifiedNumberOfBits, product, 4*product);
    }

    private static PBS_Instance convert_alt(int numberOfBits, int target){
        Literal[] lits;
        LinkedList<Summand> smds = new LinkedList<Summand>();
        Summand[] s1,s2;
        Constraint[] cs = new Constraint[(numberOfBits-1)*2];

        for(int i=1; i<=numberOfBits-1; i++){
            smds.clear();
            int j;
            for(j=1; j<=numberOfBits-i; j++){
                lits = new Literal[1];
                lits[0] = new Literal(j, true);
                smds.add(new Summand(2, lits));
                lits = new Literal[1];
                lits[0] = new Literal(j+i,true);
                smds.add(new Summand(2, lits));
                lits = new Literal[2];
                lits[0] = new Literal(j, true);
                lits[1] = new Literal(j+i, true);
                smds.add(new Summand(-4,lits));

            }
            j--; //j counts how often the previous for-loop has run, will be used at the end of the current loop
            s1 = new Summand[smds.size()];
            s2 = new Summand[smds.size()];
            int count=0;
            for(Summand s : smds){
                s1[count] = s;
                s2[count] = new Summand(-1*s.getWeight(), s.getFactors());
                count++;
            }

            cs[(i-1)*2] = new Constraint(j-target,Operator.GREATEREQUAL,s1);
            cs[(i-1)*2 +1] = new Constraint(-1*(j+target),Operator.GREATEREQUAL, s2);
        }
        int product = numberOfBits * (numberOfBits-1);
        return new PBS_Instance(cs, numberOfBits, product, 4*product);
    }

    private static PBS_Instance convert(int numberOfBits, int target){
        Literal[] lits;
        LinkedList<Summand> smds = new LinkedList<Summand>();
        Summand[] s1,s2;
        Constraint[] cs = new Constraint[(numberOfBits-1)*2];

        for(int i=1; i<=numberOfBits-1; i++){
            smds.clear();
            int j;
            for(j=1; j<=numberOfBits-i; j++){
                lits = new Literal[2];
                lits[0] = new Literal(j, true);
                lits[1] = new Literal(j+i, false);
                smds.add(new Summand(2,lits));
                lits = new Literal[2];
                lits[0] = new Literal(j, false);
                lits[1] = new Literal(j+i, true);
                smds.add(new Summand(2,lits));
            }
            j--; //j counts how often the previous for-loop has run, will be used at the end of the current loop
            s1 = new Summand[smds.size()];
            s2 = new Summand[smds.size()];
            int count=0;
            for(Summand s : smds){
                s1[count] = s;
                s2[count] = new Summand(-1*s.getWeight(), s.getFactors());
                count++;
            }

            cs[(i-1)*2] = new Constraint(j-target,Operator.GREATEREQUAL,s1);
            cs[(i-1)*2 +1] = new Constraint(-1*(j+target),Operator.GREATEREQUAL, s2);
        }
        int product = numberOfBits * (numberOfBits-1);
        return new PBS_Instance(cs, numberOfBits, product, 4*product);
    }
}
