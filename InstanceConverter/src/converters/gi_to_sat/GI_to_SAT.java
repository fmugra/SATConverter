
package converters.gi_to_sat;

import io.OutputWriter;
import java.util.LinkedList;
import model.basic.Literal;
import model.graph.Graph;
import model.sat.SAT_Instance;

/** Converts a problem specification for the GI (Graph Isomorphism) problem (in the form of two graphs) into
 *  a problem specification for the SAT (Satisfiability) problem.
 *  expects the graphs to have equal number of vertices and edges
 *  can be told to simplify the resulting sat instance by checking whether two vertices that could potentially be
 *  mapped to one another have matching degrees
 *  Returns the mapping of variables in matrix form, e.g.
 *      varNames[i][j] = x
 *  means that if the sat solver determines variable to be true, then vertex i+1 in graph 1 should be
 *  mapped to vertex j+1 in graph 2
 *  if varNames[i][j] = -1 this means that the converter has determined that vertex i+1 cannot be mapped to
 *  vertex j+1, and thus the variable is omitted
 *
 *
 * @author Frank Mugrauer
 */
public class GI_to_SAT {
    private static int numVerts;
    private static int[][] varNames;
    private static boolean[][] varIsFalse;

    public static int[][] convert(Graph g1, Graph g2, boolean simplify) throws Exception{
        long startTime = System.currentTimeMillis();
        if(g1.getNumVertices() != g2.getNumVertices())
            throw new Exception("Mismatched number of vertices in graphs: "+g1.getNumVertices()
                    +" and "+g2.getNumVertices()+"\nThe graphs are certainly NOT isomorphic!");
        if(g1.getNumEdges() != g2.getNumEdges())
            throw new Exception("Mismatched number of edges in graphs: "+g1.getNumEdges()
                    +" and "+g2.getNumEdges()+"\nThe graphs are certainly NOT isomorphic!");
        numVerts = g1.getNumVertices();
        long time;
        int remVar = 0;
        varIsFalse = new boolean[numVerts][numVerts];
        if(simplify){
            time = System.currentTimeMillis();
            int degDepth = 5;
            int[][] secDeg1 = new int[numVerts][degDepth];
            int[][] secDeg2 = new int[numVerts][degDepth];
            if(degDepth > 0){
                for(int i=0; i<numVerts; i++){
                    for(int j=0; j<i; j++){
                        //we sum up the degrees of all neighbours of i and j
                        //neghbour-degree-sum of i and j in graph1 are stored in secDeg1
                        //while those in graph2 are stored in secDeg2
                        if(g1.containsEdge(i, j)){
                            secDeg1[i][0] += g1.getDegree(j);
                            secDeg1[j][0] += g1.getDegree(i);
                        }
                        if(g2.containsEdge(i, j)){
                            secDeg2[i][0] += g2.getDegree(j);
                            secDeg2[j][0] += g2.getDegree(i);
                        }
                    }
                }
            }
            for(int depth=1; depth<degDepth; depth++){
                for(int i=0; i<numVerts; i++){
                    for(int j=0; j<i; j++){
                        if(g1.containsEdge(i, j)){
                            secDeg1[i][depth] += secDeg1[j][depth-1];
                            secDeg1[j][depth] += secDeg1[i][depth-1];
                        }
                        if(g2.containsEdge(i, j)){
                            secDeg2[i][depth] += secDeg2[j][depth-1];
                            secDeg2[j][depth] += secDeg2[i][depth-1];
                        }
                    }
                }
            }
            //Remove variables with non-matching degrees
            for(int i=0; i<numVerts; i++){
                for(int j=0; j<numVerts; j++){
                    if((g1.getDegree(i) != g2.getDegree(j))){
                        varIsFalse[i][j] = true;
                        remVar++;
                        continue;
                    }
                    for(int depth=0; depth<degDepth; depth++){
                        if(secDeg1[i][depth] != secDeg2[j][depth]){
                            varIsFalse[i][j] = true;
                            remVar++;
                            break;
                        }
                    }
                }
            }

            //sorting out colours
            for(int i=0; i<numVerts; i++){
                for(int j=0; j<numVerts; j++){
                    if(!varIsFalse[i][j] && g1.getColour(i) != g2.getColour(j)){
                        varIsFalse[i][j] = true;
                        remVar++;
                    }
                }
            }
            double remPercent = 1000 * (((double)remVar)/((double)numVerts*numVerts));
            remPercent = Math.round(remPercent)/10d;
            System.out.println("Simplification removed "+remVar+" variables ("+remPercent+"%) and took "
                    +(System.currentTimeMillis()-time)+"ms.");
        }
        time = System.currentTimeMillis();
        int clauses = numVerts; //type1
        clauses += (numVerts*numVerts*(numVerts-1)/2); //type2
        int edges = g1.getNumEdges();
        clauses += 2 * edges * ((numVerts*(numVerts-1)/2)-edges); //type3
        int numClauses = countClauses(simplify, remVar, g1, g2);
        System.out.println("Count: , calc: \n"+numClauses+"\n"+clauses);
        System.out.println("CountClauses: "+(System.currentTimeMillis()-time));
        varNames = new int[numVerts][numVerts];
        int count = 1;
        for(int i=0; i<numVerts; i++){
            for(int j=0; j<numVerts; j++){
                if(!varIsFalse[i][j]){
                    varNames[i][j] = count;
                    count++;
                }
                else
                    varNames[i][j] = -1;
            }
        }
        OutputWriter.append("p cnf "+(numVerts*numVerts -remVar)+" "+numClauses+"\n");
        System.out.println("Instance has "+(numVerts*numVerts -remVar)+" variables (down from "
                +(numVerts*numVerts)+") and "+numClauses+" clauses!");


        //type 1 clauses
        for(int i=0; i<numVerts; i++){
            StringBuilder clause = new StringBuilder(numVerts*2);
            for(int j=0; j<numVerts; j++){
                if(varIsFalse[i][j])
                    continue;
                clause.append(varNames[i][j]);
                clause.append(" ");
            }
            if(clause.length()==0){
                throw new Exception("Simplifying instance resulted in empty clause: \n"
                        +"Vertex "+(i+1)+"'s degrees do not match the degrees of any vertex in graph 2\n"
                        +"The graphs are certainly NOT isomorphic!");
            }
            clause.append("0\n");
            OutputWriter.append(clause.toString());
        }
        //type 2 clauses
        for(int j=0; j<numVerts; j++){
            for(int k=0; k<numVerts; k++){
                if(varIsFalse[j][k])
                    continue;
                for(int i=0; i<j; i++){
                    //for type two, we create clauses in which every literal is negated
                    //thus, if one of the literals is always false, the clause is always true and
                    //we don't add it (if simplifying the instance is requested)
                    if(varIsFalse[i][k])
                        continue;
                    OutputWriter.append("-"+varNames[i][k]+" -"+varNames[j][k]+" 0\n");
                }
            }
        }
        //type 3 clauses
        for(int j=0; j<numVerts; j++){
            for(int i=0; i<j; i++){
                if(!g1.containsEdge(i, j))
                    continue;
                for(int k=0; k<numVerts; k++){
                    if(varIsFalse[i][k])
                        continue;
                    for(int l=0; l<numVerts; l++){
                        //for type three, we create clauses in which every literal is negated
                        //thus, if one of the literals is always false, the clause is always true and
                        //we don't add it (if simplifying the instance is requested)
                        if(varIsFalse[j][l] || k==l || g2.containsEdge(k, l))
                            continue;
                        OutputWriter.append("-"+varNames[i][k]+" -"+varNames[j][l]+" 0\n");
                    }
                }
            }
        }
        System.out.println("Convertions took "+(System.currentTimeMillis()-time)+" ms.");
        System.out.println("TOTAL TIME: "+(System.currentTimeMillis()-startTime)+" ms.");
        return varNames;
    }

    /* OUTDATED as of 2013-05-08(new version has optimised perfomance)
    public static int[][] convert(Graph g1, Graph g2, boolean simplify) throws Exception{
        //long startTime = System.currentTimeMillis();
        if(g1.getNumVertices() != g2.getNumVertices())
            throw new Exception("Mismatched number of vertices in graphs: "+g1.getNumVertices()
                    +" and "+g2.getNumVertices()+"\nThe graphs are certainly NOT isomorphic!");
        if(g1.getNumEdges() != g2.getNumEdges())
            throw new Exception("Mismatched number of edges in graphs: "+g1.getNumEdges()
                    +" and "+g2.getNumEdges()+"\nThe graphs are certainly NOT isomorphic!");
        numVerts = g1.getNumVertices();

        long time;
        //SAT_Instance sat = new SAT_Instance(numVerts*numVerts);
        int remVar = 0;
        varIsFalse = new boolean[numVerts][numVerts];
        if(simplify){
            time = System.currentTimeMillis();
            //varIsFalse is an array which stores information on variables where we detected that they
            //always need to be set to false
            //for instance, if vertex i in graph1 had a degree of 3, and vertex k in graph2 had degree of 7,
            //we know that i can't be mapped to k, and thus the variable x_{i,k} is always false
            //we store this information in varIsFalse for later use


            //checking degrees
            //secDeg1[i][j] is the sum of the degrees of all j-distance-neighbours of i in graph 1
            //secDeg2[i][j] is the sum of the degrees of all j-distance-neighbours of i in graph 2
            int degDepth = 5;
            int[][] secDeg1 = new int[numVerts][degDepth];
            int[][] secDeg2 = new int[numVerts][degDepth];
            if(degDepth > 0){
                for(int i=0; i<numVerts; i++){
                    for(int j=0; j<i; j++){
                        //we sum up the degrees of all neighbours of i and j
                        //neghbour-degree-sum of i and j in graph1 are stored in secDeg1
                        //while those in graph2 are stored in secDeg2
                        if(g1.containsEdge(i, j)){
                            secDeg1[i][0] += g1.getDegree(j);
                            secDeg1[j][0] += g1.getDegree(i);
                        }
                        if(g2.containsEdge(i, j)){
                            secDeg2[i][0] += g2.getDegree(j);
                            secDeg2[j][0] += g2.getDegree(i);
                        }
                    }
                }
            }
            for(int depth=1; depth<degDepth; depth++){
                for(int i=0; i<numVerts; i++){
                    for(int j=0; j<i; j++){
                        if(g1.containsEdge(i, j)){
                            secDeg1[i][depth] += secDeg1[j][depth-1];
                            secDeg1[j][depth] += secDeg1[i][depth-1];
                        }
                        if(g2.containsEdge(i, j)){
                            secDeg2[i][depth] += secDeg2[j][depth-1];
                            secDeg2[j][depth] += secDeg2[i][depth-1];
                        }
                    }
                }
            }
            for(int i=0; i<numVerts; i++){
                for(int j=0; j<numVerts; j++){
                    if((g1.getDegree(i) != g2.getDegree(j))){
                        varIsFalse[i][j] = true;
                        remVar++;
                        continue;
                    }
                    for(int depth=0; depth<degDepth; depth++){
                        if(secDeg1[i][depth] != secDeg2[j][depth]){
                            varIsFalse[i][j] = true;
                            remVar++;
                            break;
                        }
                    }
                }
            }
            int conDepth = 0;
            secDeg1 = new int[numVerts][conDepth];
            secDeg2 = new int[numVerts][conDepth];
            boolean[][] con1 = new boolean[numVerts][numVerts];
            boolean[][] con2 = new boolean[numVerts][numVerts];
            if(conDepth > 0){
                for(int i=0; i<numVerts; i++){
                    for(int j=0; j<i; j++){
                        if(g1.containsEdge(i, j)){
                            con1[i][j]=true;
                            con1[j][i]=true;
                        }
                        if(g2.containsEdge(i,j)){
                            con2[i][j]=true;
                            con2[j][i]=true;
                        }
                    }
                }
            }
            for(int k=0; k<conDepth; k++){
                for(int i=0; i<numVerts; i++){
                    secDeg1[i][k] = 0;
                    secDeg2[i][k] = 0;
                    LinkedList<Integer> con1l = new LinkedList<Integer>();
                    LinkedList<Integer> con2l = new LinkedList<Integer>();
                    for(int j=0; j<numVerts; j++){
                        if(j==i)
                            continue;
                        if(con1[i][j]){
                            for(int l=0; l<numVerts; l++)
                                if(g1.containsEdge(j, l))
                                    con1l.add(l);
                        }
                        if(con2[i][j]){
                            for(int l=0; l<numVerts; l++)
                                if(g2.containsEdge(j, l))
                                    con2l.add(l);
                        }
                    }
                    while(!con1l.isEmpty())
                        con1[i][con1l.remove()] = true;
                    while(!con2l.isEmpty())
                        con2[i][con2l.remove()] = true;
                    for(int j=0; j<numVerts; j++){
                        if(con1[i][j])
                            secDeg1[i][k]++;
                        if(con2[i][j])
                            secDeg2[i][k]++;
                    }
                }
            }
            for(int i=0; i<numVerts; i++){
                for(int j=0; j<numVerts; j++){
                    for(int k=0; k<conDepth; k++){
                        if(!varIsFalse[i][j] && secDeg1[i][k] != secDeg2[j][k]){
                            varIsFalse[i][j] = true;
                            remVar++;
                            break;
                        }
                    }
                }
            }

            //sorting out colours
            for(int i=0; i<numVerts; i++){
                for(int j=0; j<numVerts; j++){
                    if(!varIsFalse[i][j] && g1.getColour(i) != g2.getColour(j)){
                        varIsFalse[i][j] = true;
                        remVar++;
                    }
                }
            }


            double remPercent = 1000 * (((double)remVar)/((double)numVerts*numVerts));
            remPercent = Math.round(remPercent)/10d;
            System.out.println("Simplification removed "+remVar+" variables ("+remPercent+"%) and took "
                    +(System.currentTimeMillis()-time)+"ms.");
            //System.out.println("simplify: "+(System.currentTimeMillis()-time));
        }
        time = System.currentTimeMillis();
        int numClauses = countClauses(simplify, g1, g2);
        varNames = new int[numVerts][numVerts];
        int count = 1;
        for(int i=0; i<numVerts; i++){
            for(int j=0; j<numVerts; j++){
                if(!varIsFalse[i][j]){
                    varNames[i][j] = count;
                    count++;
                }
                else
                    varNames[i][j] = -1;
            }
        }
        OutputWriter.append("p cnf "+(numVerts*numVerts -remVar)+" "+numClauses+"\n");
        System.out.println("Instance has "+(numVerts*numVerts -remVar)+" variables (down from "
                +(numVerts*numVerts)+") and "+numClauses+" clauses!");
        //type 1 clauses
        for(int i=0; i<numVerts; i++){
            LinkedList<Literal> clause = new LinkedList<Literal>();
            for(int j=0; j<numVerts; j++){
                if(varIsFalse[i][j])
                    continue;
                clause.add(new Literal(varNames[i][j]));
            }
            if(clause.isEmpty()){
                throw new Exception("Simplifying instance resulted in empty clause: \n"
                        +"Vertex "+(i+1)+"'s degrees do not match the degrees of any vertex in graph 2\n"
                        +"The graphs are certainly NOT isomorphic!");
            }
            //sat.addClause(clause);
            OutputWriter.append(SAT_Instance.clauseToString(clause));
        }
        //type 2 clauses
        for(int j=0; j<numVerts; j++){
            for(int k=0; k<numVerts; k++){
                if(varIsFalse[j][k])
                    continue;
                for(int i=0; i<j; i++){
                    //for type two, we create clauses in which every literal is negated
                    //thus, if one of the literals is always false, the clause is always true and
                    //we don't add it (if simplifying the instance is requested)
                    if(varIsFalse[i][k])
                        continue;
                    //sat.addClause(createNegatedBinaryClause(i,k,j,k));
                    OutputWriter.append(SAT_Instance.clauseToString(createNegatedBinaryClause(i,k,j,k)));
                }
            }
        }
        //type 3 clauses
        for(int j=0; j<numVerts; j++){
            for(int i=0; i<j; i++){
                if(!g1.containsEdge(i, j))
                    continue;
                for(int k=0; k<numVerts; k++){
                    if(varIsFalse[i][k])
                        continue;
                    for(int l=0; l<numVerts; l++){
                        //for type three, we create clauses in which every literal is negated
                        //thus, if one of the literals is always false, the clause is always true and
                        //we don't add it (if simplifying the instance is requested)
                        if(varIsFalse[j][l] || k==l || g2.containsEdge(k, l))
                            continue;

                        //sat.addClause(createNegatedBinaryClause(i,k,j,l));
                        OutputWriter.append(SAT_Instance.clauseToString(createNegatedBinaryClause(i,k,j,l)));
                    }
                }
            }
        }
        System.out.println("Convertions took "+(System.currentTimeMillis()-time)+" ms.");
        //System.out.println("TOTAL TIME: "+(System.currentTimeMillis()-startTime)+" ms.");
        return varNames;
    }*/

    /* convenience method to easily create a clause (~x_{a,b} OR ~x_{c,d})
     * ("~" means NOT; a,b,c,d are indexes to x)
     *
     */
    private static LinkedList<Literal> createNegatedBinaryClause(int a, int b, int c, int d){
        LinkedList<Literal> clause = new LinkedList<Literal>();
        Literal l = new Literal(varNames[a][b], Literal.NEGATED);
        clause.add(l);
        l = new Literal(varNames[c][d], Literal.NEGATED);
        clause.add(l);
        return clause;
    }

    private static int countClauses(boolean simplify, int remVar, Graph g1, Graph g2){
        if(!simplify)
            remVar = 0;
        //type 1 clauses
        int clausecounter=numVerts;
        int type1=numVerts, type2, type3;
        System.out.println("Type 1 clauses: "+type1);

        //type 2 clauses
        for(int j=0; j<numVerts; j++){
            for(int k=0; k<numVerts; k++){
                if(simplify && varIsFalse[j][k])
                    continue;
                for(int i=0; i<j; i++){
                    //for type two, we create clauses in which every literal is negated
                    //thus, if one of the literals is always false, the clause is always true and
                    //we don't add it (if simplifying the instance is requested)
                    if(simplify && varIsFalse[i][k])
                        continue;
                    clausecounter++;
                }
            }
        }

        type2 = clausecounter-type1;
        System.out.println("Type 2 clauses: "+type2+" (down from  "+(numVerts*numVerts*(numVerts-1)/2)+")");
        //type 3 clauses
        for(int j=0; j<numVerts; j++){
            for(int i=0; i<j; i++){
                if(!g1.containsEdge(i, j))
                    continue;
                for(int k=0; k<numVerts; k++){
                    if(simplify && varIsFalse[i][k])
                        continue;
                    for(int l=0; l<numVerts; l++){
                        if(k==l || g2.containsEdge(k, l))
                            continue;
                        //for type three, we create clauses in which every literal is negated
                        //thus, if one of the literals is always false, the clause is always true and
                        //we don't add it (if simplifying the instance is requested)
                        if(simplify && varIsFalse[j][l])
                            continue;
                        clausecounter++;
                    }
                }
            }
        }
        type3 = clausecounter-type1-type2;
        int edges = g1.getNumEdges();
        System.out.println("Type 3 clauses: "+type3+" (down from "+(2 * edges * ((numVerts-1)*(numVerts)/2-edges))+")");
        return clausecounter;
    }
}
