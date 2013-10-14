
package model.sat;

import java.util.LinkedList;
import java.util.List;
import model.ProblemInstance;
import model.basic.Literal;

/** Represents an instance of the SAT problem, in form of a list of clauses, with each clause being
 *  a list of literals
 *
 * @author Frank Mugrauer
 */
public class SAT_Instance implements ProblemInstance{
    private int numVars;
    private LinkedList<LinkedList<Literal>> clauses;
    private String graphName1, graphName2;
    private String fileNameInfo;

    public SAT_Instance(int numVars){
        this.numVars = numVars;
        this.clauses = new LinkedList<LinkedList<Literal>>();
    }
    public void addClause(LinkedList<Literal> clause){
        clauses.add(clause);
    }
    public void setGraphNames(String g1, String g2){
        graphName1 = g1;
        graphName2 = g2;
    }
    public int getNumVars(){
        return numVars;
    }
    public int getNumClauses(){
        return clauses.size();
    }
    public LinkedList<LinkedList<Literal>> getClauses(){
        return clauses;
    }

    /* creates a SAT_Instance from a cnf file
     */
    public static SAT_Instance createSAT_InstanceFromFile(List<String> inputLines) throws Exception{
        if(inputLines.size() < 1)
            throw new Exception("Input file appears to be empty!");
        while(inputLines.get(0).startsWith("c"))
            inputLines.remove(0);
        String[] split = inputLines.get(0).split(" ");
        if(split.length != 4 || !split[0].equals("p") || !split[1].equals("cnf"))
            throw new Exception("First non comment input line should read \"p cnf <numVars> <numClauses>\""
                    +"but instead reads: "+inputLines.get(0));
        int numVars = Integer.parseInt(split[2]);
        int numClauses = Integer.parseInt(split[3]);
        inputLines.remove(0);
        SAT_Instance sat = new SAT_Instance(numVars);
        LinkedList<Literal> clause;
        Literal l;
        while(!inputLines.isEmpty()){
            String s = inputLines.remove(0);
            if(s.startsWith("c") || s.isEmpty()){
                continue;
            }
            split = s.split(" ");
            clause = new LinkedList<Literal>();
            for(int i=0; i<split.length-1; i++){ //last entry in split is always 0
                int var = Integer.parseInt(split[i]);
                if(var < 0)
                    l = new Literal(-var, Literal.NEGATED);
                else
                    l = new Literal(var);
                clause.add(l);
            }
            sat.addClause(clause);
        }
        if(sat.getNumClauses() != numClauses)
            System.out.println("WARNING: Number of clauses specified in cnf file ("
                    +numClauses+") does not match number of clauses found in file ("
                    +sat.getNumClauses()+")!");
        return sat;
    }

    @Override
    public String toString(){
        String s = "c Instance representing the graph isomorphism of two graphs:\nc "
                    +graphName1+"\nc "+graphName2+"\n";
        s = s + "p cnf "+numVars+" "+clauses.size()+"\n";
        StringBuilder builder = new StringBuilder(clauses.size()*10);
        builder.append(s);
        for(LinkedList<Literal> clause : clauses){
            for(Literal l : clause){
                if(l.isNegated())
                    builder.append("-");
                builder.append(l.getID());
                builder.append(" ");
            }
            builder.append("0\n");
        }
        return builder.toString();
    }

    public static String clauseToString(LinkedList<Literal> clause){
        StringBuilder builder = new StringBuilder();
        for(Literal l : clause){
            if(l.isNegated())
                builder.append("-");
            builder.append(l.getID());
            builder.append(" ");
        }
        builder.append("0\n");
        return builder.toString();
    }

    @Override
    public String instanceType() {
        return "SAT";
    }

    @Override
    public void appendFileNameInfo(String info) {
        fileNameInfo = info;
    }

    @Override
    public String getFileNameInfo() {
        return fileNameInfo;
    }
}
