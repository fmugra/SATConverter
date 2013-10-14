
package model.graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/** Represents an undirected graph without multiple edges in form of an adjacency matrix
 * vertices are addressed by index, from 0..(n-1)
 * This class can (optionally) handle graphs with coloured vertices, although some of the
 * methods IGNORE COLOURS (since many input formats don't support colours).
 * If in doubt, read the documentation for the method - those that ignore colours will
 * clearly state this behaviour.
 *
 * @author Frank Mugrauer
 */
public class Graph {

    private boolean[][] adjMatrix;
    private int[] degrees;
    private int[] colours;
    private int numColours;
    private int numEdges;

    /* creates an empty graph with <numVertices> vertices, and no edges. All vertices
     * have the same colour.
     * To be used by the generator methods.
     */
    private Graph(int numVertices){
        this.adjMatrix = new boolean[numVertices][numVertices];
        this.degrees = new int[numVertices];
        this.colours = new int[numVertices];
        for(int i=0; i<numVertices; i++){
            this.degrees[i]=0;
            this.colours[i]=1;
            for(int j=0; j<numVertices; j++)
                this.adjMatrix[i][j] = false;
        }
        this.numEdges = 0;
        this.numColours=1;
    }

    public boolean containsEdge(int v1, int v2){
        return adjMatrix[v1][v2];
    }
    public int getDegree(int vertex){
        return degrees[vertex];
    }
    public int getNumVertices(){
        return degrees.length;
    }
    public int getNumEdges(){
        return numEdges;
    }
    public int getColour(int vertex){
        return colours[vertex];
    }
    public int getNumColours(){
        return numColours;
    }

    /* assings random colours to the vertices of this graph. Assigns each colour to <numVertsPerColour>
     * vertices. Depending on the number of vertices in the graph, this might not be possible, in which case one
     * colour-group will have fewer than <numVertsPerColour> vertices
     *
     */
    public void assignRandomColours(int numVertsPerColour){
        LinkedList<Integer> vertList = new LinkedList<Integer>();
        for(int i=0; i<degrees.length; i++)
            vertList.add(i);
        Collections.shuffle(vertList);
        int colour = 1;
        int count = 0;
        while(!vertList.isEmpty()){
            if(count==numVertsPerColour){
                count=0;
                colour++;
            }
            colours[vertList.remove()] = colour;
            count++;
        }
        numColours = colour;
    }

    /* Creates three graphs (0 and 1 are isomorphic, 0-2 and 1-2 are not) based on the original graph.
     * Graphs are created via the algorithm described in
     * Cai, Fuerer, Immerman: An Optimal Lower Bound on the Number of Variables for Graph Identification
     */
    public static Graph[] createGraphGroup(Graph original){
        Random random = new Random();
        int edgeToFlip = random.nextInt(original.getNumEdges());
        Graph[] graphs = new Graph[3];
        LinkedList<boolean[][]> subgraphs = new LinkedList<boolean[][]>();
        //calculate total number of vertices, create subgraph for each vertex, and caluclate the offsets
        //for each subgraph
        int numVertices = 0, tmp;
        int[] subGraphOffsets = new int[original.degrees.length];
        for(int i=0; i<original.degrees.length; i++){
            tmp = original.degrees[i];
            subGraphOffsets[i] = numVertices;
            numVertices += 2*tmp + (int)Math.pow(2, tmp-1);
            subgraphs.addLast(Utils.createNodeGraph(tmp));
        }
        graphs[0] = new Graph(numVertices);
        //graphs[1] will be created right at the end of this method, right before the return statement
        graphs[2] = new Graph(numVertices);


        //sort out edges between original vertices
        int[] subVertexIndices = new int[original.degrees.length]; //will store how many of the edge slots for
        for(int i=0; i<subVertexIndices.length; i++) //each original vertex are already in use
            subVertexIndices[i] = 0;
        int k, l; //tmp variables, see comment below
        int indexA_k, indexA_l, indexB_k, indexB_l;
        int edgecounter = 0;
        for(int i=0; i<original.degrees.length; i++){
            //for an edge that this vertice has to another vertice j, we need to create edges
            //between one vertex a_k of vertex i, and one vertex a_l of vertex j, aswell as one for b_k of i,
            //and b_l of j.
            for(int j=0; j<i; j++){
                if(original.containsEdge(i, j)){
                    k = subVertexIndices[i];
                    l = subVertexIndices[j];
                    subVertexIndices[i]++;
                    subVertexIndices[j]++;

                    indexA_k = subGraphOffsets[i]+k;
                    indexA_l = subGraphOffsets[j]+l;
                    indexB_k = indexA_k +original.degrees[i];
                    indexB_l = indexA_l + original.degrees[j];

                    graphs[0].adjMatrix[indexA_k][indexA_l] = true;
                    graphs[0].adjMatrix[indexA_l][indexA_k] = true;
                    graphs[0].adjMatrix[indexB_k][indexB_l] = true;
                    graphs[0].adjMatrix[indexB_l][indexB_k] = true;
                    if(edgecounter != edgeToFlip){
                        graphs[2].adjMatrix[indexA_k][indexA_l] = true;
                        graphs[2].adjMatrix[indexA_l][indexA_k] = true;
                        graphs[2].adjMatrix[indexB_k][indexB_l] = true;
                        graphs[2].adjMatrix[indexB_l][indexB_k] = true;
                    }else{//one edge in graphs[2] will be flipped, to ensure the graphs are not isomorphic
                        //instead of connecting a_k to a_l and b_k to b_l, we will connect
                        //a_k to b_l, and b_k to a_l
                        graphs[2].adjMatrix[indexA_k][indexB_l] = true;
                        graphs[2].adjMatrix[indexB_l][indexA_k] = true;
                        graphs[2].adjMatrix[indexB_k][indexA_l] = true;
                        graphs[2].adjMatrix[indexA_l][indexB_k] = true;
                    }
                    edgecounter++;
                }
            }
        }
        //sort out edges (and vertex colours) between subgraph vertices
        graphs[0].numColours = original.numColours;
        graphs[2].numColours = original.numColours;
        int offset;
        boolean[][] subGraph;
        for(int i=0; i<original.degrees.length; i++){
            offset = subGraphOffsets[i];
            subGraph = subgraphs.removeFirst();
            for(int j=0; j<subGraph.length; j++){
                graphs[0].colours[offset+j] = original.colours[i];
                graphs[2].colours[offset+j] = original.colours[i];
                for(k=0; k<j; k++){
                    if(subGraph[j][k]){
                        graphs[0].adjMatrix[offset+j][offset+k] = true;
                        graphs[0].adjMatrix[offset+k][offset+j] = true;
                        graphs[2].adjMatrix[offset+j][offset+k] = true;
                        graphs[2].adjMatrix[offset+k][offset+j] = true;
                    }
                }
            }
        }
        //sort out leftovers (degrees, numberOfEdges)
        int numberOfEdges = 0;
        int degree;
        for(int i=0; i<graphs[0].adjMatrix.length; i++){
            degree = 0;
            for(int j=0; j<graphs[0].adjMatrix.length; j++){
                if(graphs[0].containsEdge(i, j))
                    degree++;
            }
            graphs[0].degrees[i] = degree;
            graphs[2].degrees[i] = degree;
            numberOfEdges += degree;
        }
        numberOfEdges = numberOfEdges / 2; //above, we counted every edge twice
        graphs[0].numEdges = numberOfEdges;
        graphs[2].numEdges = numberOfEdges;
        graphs[1] = Graph.createIsomorphicGraph(graphs[0]);
        return graphs;
    }

    /* attempts to create a (coloured) Graph object by parsing the lines of text (as represented by a list of strings)
     * in a file
     * assumes the graph-file to be in COLOURED format:
     * - every line starting with "c" is a comment
     * - first non-comment line reads "p cols <numVertices> <numEdges> <numColours>"
     * - edges are specified in a line "e <vertex1> <vertex2>"
     * - vertex colours are specified in a line "v <vertex> <colour>"
     */
    public static Graph readGraphFromFileCOLOURED(List<String> inputLines) throws Exception{
        while(inputLines.get(0).startsWith("c "))
            inputLines.remove(0);
        if(inputLines.get(0)==null)
            throw new Exception("Input file is empty or contains nothing but comments!");
        String[] split = inputLines.get(0).split(" ");
        if(split.length != 5 || !split[0].equals("p") || !split[1].equals("cols"))
            throw new Exception("First non comment input line should read "
                    +"\"p cols <numberOfVertices> <numberOfEdges> <numberOfColours>\", but doesn't: \n"
                    +inputLines.get(0));
        int numVerts = Integer.parseInt(split[2]);
        int numEdges = Integer.parseInt(split[3]);
        int numCols = Integer.parseInt(split[4]);
        if(numVerts < 1 )
            throw new Exception("Illegal number of vertices: "+numVerts);
        if(numEdges < 0)
            throw new Exception("Illegal number of edges: "+numEdges);
        if(numCols < 1)
            throw new Exception("Illegal number of colours: "+numCols);
        Graph graph = new Graph(numVerts);
        inputLines.remove(0);
        int edgeCount = 0;
        while(!inputLines.isEmpty()){
            String s = inputLines.remove(0);
            if(s.startsWith("c "))
                continue;
            if(s.startsWith("e ")){
                split = s.split(" ");
                if(split.length != 3)
                    throw new Exception("edge lines should read \"e <vertex1> <vertex2>\""
                            +", but this one doesn't:\n"+s);
                int v1 = Integer.parseInt(split[1]);
                int v2 = Integer.parseInt(split[2]);
                if(v1 < 1 || v1 > numVerts || v2 < 1 || v2 > numVerts)
                    throw new Exception("Illegal vertex number in line "+s+"\nOnly vertex numbers 1..."
                            +numVerts+"(inclusive) are allowed for a graph with "+numVerts+" vertices!");
                if(!graph.adjMatrix[v1-1][v2-1]){
                    graph.degrees[v1-1]++;
                    graph.degrees[v2-1]++;
                    edgeCount++;
                }
                graph.adjMatrix[v1-1][v2-1] = true;
                graph.adjMatrix[v2-1][v1-1] = true;
            }
            else if(s.startsWith("v ")){
                split = s.split(" ");
                if(split.length != 3)
                    throw new Exception("colour lines should read \"v <vertex> <colour>\""
                            +", but this one doesn't:\n"+s);
                int v = Integer.parseInt(split[1]);
                int c = Integer.parseInt(split[2]);
                if(v < 1 || v > numVerts)
                    throw new Exception("Illegal vertex number in line "+s+"\nOnly vertex numbers 1..."
                            +numVerts+"(inclusive) are allowed for a graph with "+numVerts+" vertices!");
                if(c < 1 || c > numVerts)
                    throw new Exception("Illegal colour in line "+s+"\nOnly colours 1..."+numCols
                            +" (inclusive) are allowed for a graph with "+numCols+" colours");
                graph.colours[v-1] = c;
            }
            else
                throw new Exception("Illegal input file line: "+s);
        }
        graph.numEdges = edgeCount;
        return graph;
    }

    /* attempts to create a Graph object by parsing the lines of text (as represented by a list of strings)
     * in a file
     * assumes the graph-file to be in DIMACS format (http://prolland.free.fr/works/research/dsat/dimacs.html)
     * Since the DIMACS format does not contain colours, all vertices will have the same colour.
     */
    public static Graph readGraphFromFileDIMACS(List<String> inputLines) throws Exception{
        while(inputLines.get(0).startsWith("c "))
            inputLines.remove(0);
        if(inputLines.get(0)==null)
            throw new Exception("Input file is empty or contains nothing but comments!");
        String[] split = inputLines.get(0).split(" ");
        if(split.length != 4 || !split[0].equals("p") || !split[1].equals("edge"))
            throw new Exception("First non comment input line should read "
                    +"\"p edge <numberOfVertices> <numberOfEdges>\", but doesn't: \n"+inputLines.get(0));
        int numVerts = Integer.parseInt(split[2]);
        int numEdges = Integer.parseInt(split[3]);
        if(numVerts < 1 )
            throw new Exception("Illegal number of Vertices: "+numVerts);
        if(numEdges < 0)
            throw new Exception("Illegal number of Edges: "+numEdges);
        Graph graph = new Graph(numVerts);
        inputLines.remove(0);
        int edgeCount = 0;
        while(!inputLines.isEmpty()){
            String s = inputLines.remove(0);
            if(s.startsWith("c "))
                continue;
            if(s.startsWith("e ")){
                split = s.split(" ");
                if(split.length != 3)
                    throw new Exception("edge lines should read \"e <vertex1> <vertex2>\""
                            +", but this one doesn't:\n"+s);
                int v1 = Integer.parseInt(split[1]);
                int v2 = Integer.parseInt(split[2]);
                if(v1 < 1 || v1 > numVerts || v2 < 1 || v2 > numVerts)
                    throw new Exception("Illegal vertex number in line "+s+"\nOnly vertex numbers 1..."
                            +numVerts+"(inclusive) are allowed for a graph with "+numVerts+" vertices!");
                if(!graph.adjMatrix[v1-1][v2-1]){
                    graph.degrees[v1-1]++;
                    graph.degrees[v2-1]++;
                    edgeCount++;
                }
                graph.adjMatrix[v1-1][v2-1] = true;
                graph.adjMatrix[v2-1][v1-1] = true;
            }
            else
                throw new Exception("Illegal input file line: "+s);
        }
        graph.numEdges = edgeCount;
        return graph;
    }

    /* attempts to create a Graph object by parsing an array of bytes (representing the bytes stored in a file)
     * assumes the graph-file to be in SIVALab format:
     * https://docs.google.com/viewer?a=v&q=cache:0_k1HsL70CsJ:amalfi.dis.unina.it/graph/doc/graphdb.pdf+sivalab+graph&hl=es&pid=bl&srcid=ADGEESjAkVTGU9xlfHuKq8VqTAOeV-HkkEUJISO-gqu5GBImNt_imx8PUCHzAJhF478_BrGln2zcIwbqT53-uXmQA8gqY94nLOLldosEXrOGN0HDSBPlDFViBgoYdLr8dZbKzAG4q7e-&sig=AHIEtbQ_4A1vb8gtlrCBzyCAxBfgZJgVdA
     * Since the SIVALab format does not contain colours, all vertices will have the same colour.
     */
    public static Graph readGraphFromFileSIVALab(byte[] bytes) throws Exception{
        if(bytes.length < 2)
            throw new Exception("File has less than 2 bytes of content, cannot contain graph");
        int[] ints = new int[(bytes.length/2)-1];
        //convert two bytes into an int (Little-Endian, first byte is least significant)
        int numNodes =  bytes[1] << 8 | (bytes[0] & 0xFF);//Bitwise AND with 0xFF gets you a byte's unsigned value
        if(numNodes < 1)
            throw new Exception("File says the graph has "+numNodes+" vertices");
        for(int i=2; i<bytes.length; i=i+2){
            ints[i/2 -1] = bytes[i+1] << 8 | (bytes[i] & 0xFF);
        }
        Graph graph = new Graph(numNodes);
        int nodeIndex = 0;
        int nodeNumber = 0;
        while(nodeIndex < ints.length){
            if(nodeIndex >= ints.length)
                throw new Exception("Error while reading file: File says graph contains "+numNodes
                        +" nodes, but file ends after "+(nodeNumber-1)+" nodes.");
            int numEdges = ints[nodeIndex];
            for(int edgeIndex=1; edgeIndex<=numEdges; edgeIndex++){
                if(nodeIndex+edgeIndex >= ints.length)
                    throw new Exception("Error while reading file: File says node "+nodeNumber+" has "+numEdges
                            +" edges, but file ends after reading "+(edgeIndex-1)+" edges.");
                if(ints[nodeIndex+edgeIndex] >= graph.adjMatrix.length)
                    throw new Exception("Error while reading file: File says graph contains "+numNodes
                            +", but specifies an edge from "+nodeNumber+" to "+ints[nodeIndex+edgeIndex]);
                if(!graph.adjMatrix[nodeNumber][ints[nodeIndex+edgeIndex]]){
                    graph.numEdges++;
                    graph.degrees[nodeNumber]++;
                    graph.degrees[ints[nodeIndex+edgeIndex]]++;
                }
                graph.adjMatrix[nodeNumber][ints[nodeIndex+edgeIndex]] = true;
                graph.adjMatrix[ints[nodeIndex+edgeIndex]][nodeNumber] = true;
            }
            nodeIndex += numEdges+1;
            nodeNumber++;
        }
        //graph.checkGraph();
        return graph;
    }


    /* takes a byte[] representation of a file in GENREG shortcode (.scd), and returns the contained
     * graphs as a list
     * http://www.mathe2.uni-bayreuth.de/markus/manual/genreg.html
     * The method can be instructed to return a specific number of graphs. If the file contains more graphs than
     * requested, the first <maxGraphNumber> graphs will be returned. Otherwise, all graphs (i.e. fewer than
     * <maxGraphNumber>) will be returned.
     *
     * @param bytes: byte[] representation of the graph file
     * @param n: number of vertices of the graphs
     * @param k: degree of graphs (all graphs are regular)
     * @param maxGraphNumber: maximum number of graphs that this method should return
     *
     */
    public static List<Graph> readGraphsFromFileGENREG(byte[] bytes, int n,
                                                    int k, int maxGraphNumber) throws Exception{
        LinkedList<Graph> graphList = new LinkedList<Graph>();
        int length = n*k/2;
        int count = 0;
        byte[] oldGraph=null,currentGraph;
        while(count < bytes.length){
            int same = bytes[count];
            count++;
            currentGraph = new byte[length];
            for(int i=0; i<same; i++)
                currentGraph[i] = oldGraph[i];
            for(int i=same; i<length; i++){
                currentGraph[i] = bytes[count];
                count++;
            }
            oldGraph = currentGraph;
            graphList.add(graphFromByteArrayGENREG(oldGraph,n,k));
            if(graphList.size() == maxGraphNumber)
                return graphList;
        }
        return graphList;
    }
    /* helper method for readGraphsFromFileGENREG:
     * turns a full array of edges into a Graph (see readGraphsFromFileGENREG for further info)
     *
     * @param bytes: list of edges
     * @param n: number of vertices
     * @param k: degree of graph
     *
     */
    private static Graph graphFromByteArrayGENREG(byte[] bytes, int n, int k){
        Graph g = new Graph(n);
        g.numEdges = bytes.length;
        g.numColours = 1;
        int vertex = 0, index = 0, target;
        while(index < bytes.length){
            while(g.degrees[vertex] < k){
                target = bytes[index]-1;
                g.degrees[vertex]++;
                g.degrees[target]++;
                g.adjMatrix[vertex][target] = true;
                g.adjMatrix[target][vertex] = true;
                index++;
            }
            vertex++;
        }
        return g;
    }

    /* Creates a graph from an adjacency matrix. Since adjacency matrices don't contain colours,
     * all vertices will have the same colour.
     */
    public static Graph fromAdjacencyMatrix(boolean[][] adjMat) throws Exception{
        if(adjMat.length != adjMat[0].length)
            throw new Exception("Adjacency matrix has "+adjMat.length+"rows and "+adjMat[0].length+" columns!");
        Graph g = new Graph(adjMat.length);
        int edgeCount =0;
        for(int i=0; i<adjMat.length; i++){
            for(int j=0; j<adjMat.length; j++){
                if(i==j)
                    continue;
                if(adjMat[i][j]){
                    if(!g.adjMatrix[i][j]){
                        g.degrees[i]++;
                        g.degrees[j]++;
                        edgeCount++;
                    }
                    g.adjMatrix[i][j] = true;
                    g.adjMatrix[j][i] = true;
                }
            }
        }
        g.numEdges = edgeCount;
        return g;
    }

    /* creates a String representing this (coloured) graph, in a format similar, but not compatible, to DIMACS
     * found at (http://prolland.free.fr/works/research/dsat/dimacs.html).
     * The format is as follows:
     * - every line starting with "c" is a comment
     * - first non-comment line reads "p cols <numVertices> <numEdges> <numColours>"
     * - edges are specified in a line "e <vertex1> <vertex2>"
     * - vertex colours are specified in a line "v <vertex> <colour>"
     *
     */
    public String toStringColoured(){
        StringBuilder b = new StringBuilder();
        b.append("p cols ");
        b.append(degrees.length);
        b.append(" ");
        b.append(numEdges);
        b.append(" ");
        b.append(numColours);
        b.append("\n");
        for(int i=0; i<adjMatrix.length; i++){
            for(int j=i; j<adjMatrix.length; j++){
                if(adjMatrix[i][j]){
                    b.append("e ");
                    b.append(i+1);
                    b.append(" ");
                    b.append(j+1);
                    b.append("\n");
                }
            }
        }
        for(int i=0; i<colours.length; i++){
            b.append("v ");
            b.append(i+1);
            b.append(" ");
            b.append(colours[i]);
            b.append("\n");
        }

        return b.toString();
    }

    /* creates a String representing this graph
     * This method ignores colours!
     * (in DIMACS format (http://prolland.free.fr/works/research/dsat/dimacs.html)
     */
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("p edge ");
        b.append(degrees.length);
        b.append(" ");
        b.append(numEdges);
        b.append("\n");
        for(int i=0; i<adjMatrix.length; i++){
            for(int j=i; j<adjMatrix.length; j++){
                if(adjMatrix[i][j]){
                    b.append("e ");
                    b.append(i+1);
                    b.append(" ");
                    b.append(j+1);
                    b.append("\n");
                }
            }
        }
        return b.toString();
    }


    /* checks whether or not this graph is undirected, i.e. if it contains an
     * edge (i,j) for every edge (j,i)
     *
     */
    public boolean isGraphUndirected(){
        for(int i=0; i<degrees.length; i++){
            for(int j=0; j<degrees.length; j++){
                if(adjMatrix[i][j] != adjMatrix[j][i])
                    return false;
            }
        }
        return true;
    }

    /* checks whether or not the degrees specified in this graphs degree vector actually fit the
     * number of edges specified in the adjacency matrix
     */
    public boolean checkDegrees(){
        for(int i=0; i<degrees.length; i++){
            int tmp = 0;
            for(int j=0; j<degrees.length; j++)
                if(adjMatrix[i][j])
                    tmp++;
            if(tmp != degrees[i])
                return false;
        }
        return true;
    }

    /* creates a random undirected graph with n vertexes
     * This method ignores colours!
     * @param n number of vertexes in each graph
     * @param p approx. probability that there is an edge between any two vertexes
     */
    public static Graph createRandomGraph(int n, double p){
        if(n<1)
            n = 1;
        if(p<=0 || p>2)
            p = 0.03d;
        Graph g = new Graph(n);
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                if(i != j && Math.random() < p){
                    if(!g.adjMatrix[i][j]){
                        g.degrees[i]++;
                        g.degrees[j]++;
                        g.numEdges++;
                    }
                    g.adjMatrix[i][j] = true;
                    g.adjMatrix[j][i] = true;
                }
            }
        }
        return g;
    }

    /* Returns a new graph, which is isomorpthic to <g>. The new graph is created by randomly switching
     * vertex labels of <g>.
     * This method observes colours in the graph, so the result will be a coloured graph that is isomorphic to
     * the coloured original graph.
     */
    public static Graph createIsomorphicGraph(Graph g){
        Graph res = new Graph(g.getNumVertices());
        res.numEdges = g.numEdges;
        res.numColours = g.numColours;
        int[] map = new int[g.getNumVertices()];
        LinkedList<Integer> vertexBucket = new LinkedList<Integer>();
        for(int i=0; i<g.getNumVertices(); i++)
            vertexBucket.add(i);
        Collections.shuffle(vertexBucket);
        for(int i=0; i<g.getNumVertices(); i++){
            map[i] = vertexBucket.remove();
        }
        for(int i=0; i<g.getNumVertices(); i++){
            res.degrees[map[i]] = g.degrees[i];
            res.colours[map[i]] = g.colours[i];
            for(int j=0; j<g.getNumVertices(); j++){
                res.adjMatrix[map[i]][map[j]] = g.adjMatrix[i][j];
            }
        }
        return res;
    }

    /* returns whether or not the graph is regular
     *
     */
    public boolean isRegular(){
        int k = degrees[0];
        for(int i=1; i<degrees.length; i++){
            if(degrees[i]!=k)
                return false;
        }
        return true;
    }

    /* returns a random variation of the provided graph with 1 more vertexes and (vertexes/10) more edges
     * This method ignores colours!
     */
    public static Graph createVariation(Graph g, Random rng){
        int n = g.getNumVertices();
        Graph res = new Graph(n+1);
        //copy all edges and degrees
        for(int i=0; i<n; i++){
            res.degrees[i] = g.degrees[i];
            for(int j=0; j<n; j++)
                res.adjMatrix[i][j] = g.adjMatrix[i][j];
        }
        //create new edges from the new vertex to a random n/10 vertexes
        for(int i=0; i<n/10; i++){
            int rnd = rng.nextInt(n);
            while(res.adjMatrix[n][rnd])//if a previous iteration of the for loop has already set this edge,
                rnd = rng.nextInt(n); //we need to select a new one
            res.adjMatrix[n][rnd] = true;
            res.adjMatrix[rnd][n] = true;
            res.degrees[n]++;
            res.degrees[rnd]++;
        }
        res.numEdges = g.numEdges + (n/10);
        return res;
    }

    /* Returns an exact copy of the graph that can be manipulated without affecting the original graph
     */
    private Graph copy(){
        Graph g = new Graph(this.degrees.length);
        g.numEdges = this.numEdges;
        g.numColours = this.numColours;
        for(int i=0; i<this.adjMatrix.length; i++){
            g.degrees[i] = this.degrees[i];
            g.colours[i] = this.colours[i];
            for(int j=0; j<this.adjMatrix.length; j++)
                g.adjMatrix[i][j] = this.adjMatrix[i][j];
        }
        return g;
    }
}
