
package converters.sat_to_graph;

import io.OutputWriter;
import java.util.LinkedList;
import model.basic.Literal;
import model.graph.Graph;
import model.sat.SAT_Instance;

/**
 *
 * @author Frank Mugrauer
 */
public class SAT_to_Graph {

    public static Graph convert(SAT_Instance s) throws Exception{
        int numVars = s.getNumVars();
        int numVerts = 3*numVars;
        //verts 0..(numVars-1) = positive literals
        //verts numVars+..(2*numVars-1) = negated literals
        //verts 2*numVars..(3*numVars-1) = connection vertices, each connecting one positive literal to one negated one
        boolean[][] mat = new boolean[numVerts][numVerts];
        
        //form connections from positive literals, via connection vertices, to negated literals
        for(int i=0;i<numVars; i++){
            mat[i][2*numVars+i] = true;
            mat[numVars+i][2*numVars +i] = true;
        }
        LinkedList<LinkedList<Literal>> clauses = s.getClauses();
        for(LinkedList<Literal> clause : clauses){
            for(Literal l1 : clause){
                int l1VertexNum = l1.getID()-1;
                if(l1.isNegated())
                    l1VertexNum += numVars;
                for(Literal l2 : clause){
                    int l2VertexNum = l2.getID()-1;
                    if(l1.getID() == l2.getID())
                        continue;
                    if(l2.isNegated())
                        l2VertexNum += numVars;
                    mat[l1VertexNum][l2VertexNum] = true;
                    mat[l2VertexNum][l1VertexNum] = true;
                }
            }
        }
        return Graph.fromAdjacencyMatrix(mat);

        /* OLD
        //count edges
        int numEdges = numVerts; //type 1 edges, see below
        LinkedList<LinkedList<Literal>> clauses = s.getClauses();
        for(LinkedList<Literal> clause : clauses){
            for(Literal l : clause){
                numEdges++;     //type 2 edges, see below
            }
        }
        OutputWriter.append("p edge "+numVerts+" "+numEdges+"\n");
        //type 1: One edge between every literal i and it's complement ~i
        for(int i=1; i<=numVars; i++){
            OutputWriter.append("e "+i+" "+(numVars+i)+"\n");
        }
        //type 2: for every clause c, an edge to every literal in the clause
        int clauseCount = 2*numVars+1;
        for(LinkedList<Literal> clause : clauses){
            for(Literal l : clause){
                int vertexNum = (l.isNegated()) ? l.getID()+numVars : l.getID();
                OutputWriter.append("e "+clauseCount+" "+vertexNum+"\n");
            }
        }*/
    }
}
