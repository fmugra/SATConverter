
package model.pseudoBoolean;

import model.ProblemInstance;
import model.basic.Literal;

/** Represents a problem specification for the PBS (Pseudo Boolean Satisfaction) problem
 *  PBS specifications consist of one or more Constraints
 *
 * @author Frank Mugrauer
 */
public class PBS_Instance implements ProblemInstance{
    private String fileNameInfo;
    private Constraint[] constraints;
    private int variables, product, sizeproduct;

    public PBS_Instance(Constraint[] constraints, int variables, int product, int sizeproduct){
        this.constraints = constraints;
        this.variables = variables;
        this.product = product;
        this.sizeproduct = sizeproduct;
    }
    public PBS_Instance(Constraint c, int variables, int product, int sizeproduct){
        this.constraints = new Constraint[1];
        constraints[0] = c;
        this.variables = variables;
        this.product = product;
        this.sizeproduct = sizeproduct;
    }

    public Constraint[] getConstraints() {
        return constraints;
    }

    @Override
    public void appendFileNameInfo(String info) {
        fileNameInfo = fileNameInfo + info;
    }

    @Override
    public String getFileNameInfo() {
        return fileNameInfo;
    }

    @Override
    public String instanceType(){
        return "PBS";
    }

    /* String representation of this instance, to be written into a file
     * (format: Pseudo Boolean Competition 2012: http://www.cril.univ-artois.fr/PB12/)
     */
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("* #variable= ");
        b.append(variables);
        b.append(" #constraint= ");
        b.append(constraints.length);
        b.append(" #product= ");
        b.append(product);
        b.append(" sizeproduct= ");
        b.append(sizeproduct);
        b.append(" \n");
        for(Constraint c : constraints){
            b.append(toStringConstraint(c));
            b.append("\n");
        }
        return b.toString();
        /* OUTDATED as of 2013-3-24; new code is more efficient
        String res = "* #variable= "+variables+" #constraint= "+constraints.length
                    +" #product= "+product+" sizeproduct= "+sizeproduct+" \n";
        for(Constraint c : constraints)
            res = res + toStringConstraint(c) + "\n";
        return res.substring(0, res.length()-1); //cut off superfluous "\n"
        */
    }

    private String toStringLiteral(Literal l){
        if(l.isNegated())
            return "~"+l.getName();
        return l.getName();
    }
    private String toStringSummand(Summand s){
        String res = "";
        //if(s.getWeight() != 1)
        res = res + ((s.getWeight()>=0) ? "+" : "") + s.getWeight()+" ";
        Literal[] lits = s.getFactors();
        for(Literal l : lits)
            res = res + toStringLiteral(l) + " ";
        return res.substring(0, res.length()-1); //cut off superfluous whitespace
    }
    private String toStringConstraint(Constraint c){
        StringBuilder b = new StringBuilder();
        Summand[] smds = c.getSummands();
        for(Summand s : smds){
            b.append(toStringSummand(s));
            b.append(" ");
        }
        b.append((c.getOp() == Operator.EQUALS) ? " = " : " >= ");
        b.append(c.getTarget());
        b.append(";");
        return b.toString();
        /* OUTDATED as of 2013-3-24; new code is more efficient
        String res = "";
        Summand[] smds = c.getSummands();
        for(Summand s : smds)
            res = res + toStringSummand(s) + " ";
        //res = res.substring(0, res.length()); //cut off "+ "
        res = res + ((c.getOp() == Operator.EQUALS) ? " = " : " >= ");
        res = res + c.getTarget()+";";
        return res;
        */
    }
}
