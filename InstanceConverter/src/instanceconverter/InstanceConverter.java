
package instanceconverter;

import converters.gi_to_sat.GI_to_SAT;
import converters.labs_to_pbs.LABS_to_PBS;
import converters.labs_to_pbs.LABS_to_PBS_simple;
import converters.sat_to_graph.SAT_to_Graph;
import io.BinaryInputReader;
import io.InputReader;
import io.OutputWriter;
import java.io.File;
import java.util.List;
import java.util.Random;
import model.graph.Graph;
import model.labs.LABS_Instance;
import model.pseudoBoolean.PBS_Instance;
import model.sat.SAT_Instance;

/**
 *
 * @author Frank Mugrauer
 */
public class InstanceConverter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length < 1)
            printUsage();
        while(args[0].startsWith("-")){
            String[] tmp = new String[args.length-1];
            for(int i=0; i<tmp.length; i++)
                tmp[i] = args[i+1];
            args = tmp;
        }
        if(args[0].equalsIgnoreCase("LABS")){
            labs(args);
            return;
        }
        if(args[0].equalsIgnoreCase("GI")){
            gi(args);
            return;
        }
        if(args[0].equalsIgnoreCase("CG")){
            cg(args);
            return;
        }
        if(args[0].equalsIgnoreCase("cnfg")){
            cnfg(args);
            return;
        }
        if(args[0].equalsIgnoreCase("cnfiso")){
            cnfiso(args);
            return;
        }
        if(args[0].equalsIgnoreCase("coliso")){
            coliso(args);
            return;
        }
        if(args[0].equalsIgnoreCase("buildgraphs")){
            buildgraphs(args);
            return;
        }
        if(args[0].equalsIgnoreCase("convertGenreg")){
            convertGenreg(args);
            return;
        }
        printUsage();
    }
     /* Program logic for convertGenreg mode
     */
    private static void convertGenreg(String[] args){
        if(args.length < 2 || args.length > 3)
            printUsageCONVERTGENREG();
        try{
            int numberOfGraphs = Integer.MAX_VALUE;
            if(args.length==3)
                numberOfGraphs = Integer.parseInt(args[2]);
            String inFile = args[1];
            String[] split = inFile.split("_");
            if(split.length < 3 ){
                System.out.println("Invalid file name!");
                printUsageCONVERTGENREG();
            }
            int n = Integer.parseInt(split[0]);
            int k = Integer.parseInt(split[1]);
            byte[] inBytes = BinaryInputReader.readFile(inFile);
            List<Graph> graphs = Graph.readGraphsFromFileGENREG(inBytes, n, k, numberOfGraphs);
            int count = 1;
            for(Graph g : graphs){
                OutputWriter.writeFile(g.toString(), inFile+"_"+count+".col");
                count++;
            }
        }catch(Exception e){
            System.out.println("Error while reading files:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    /* Program logic for buildgraphs mode
     */
    private static void buildgraphs(String[] args){
        if(args.length < 3 || args.length > 4)
            printUsageBUILDGRAPHS();
        try{
            int numVertsPerColour = Integer.parseInt(args[2]);
            String inputFile = args[1];
            Graph g;
            if(args.length == 4 && args[3].equalsIgnoreCase("SIVALab")){
                System.out.println("InputFormat: SIVALab");
                g = Graph.readGraphFromFileSIVALab(BinaryInputReader.readFile(inputFile));
            }else{
                System.out.println("InputFormat: DIMACS");
                g = Graph.readGraphFromFileDIMACS(InputReader.readFile(inputFile));
            }
            System.out.println("Graph has "+g.getNumVertices()+" vertices and "+g.getNumEdges()+" edges.");
            System.out.println("Assigning random colours ...");
            g.assignRandomColours(numVertsPerColour);
            System.out.println("Creating Graphs ...");
            Graph[] graphs = Graph.createGraphGroup(g);
            System.out.println("Writing Graphs ...");
            OutputWriter.writeFile(graphs[0].toStringColoured(), inputFile+"_"+numVertsPerColour+"-1.clr");
            OutputWriter.writeFile(graphs[1].toStringColoured(), inputFile+"_"+numVertsPerColour+"-2.clr");
            OutputWriter.writeFile(graphs[2].toStringColoured(), inputFile+"_"+numVertsPerColour+"-3.clr");
            System.out.println("done.");
            /*for(int i=0; i<graphs[0].getNumVertices(); i++){
                for(int j=0; j<graphs[0].getNumVertices(); j++){
                    System.out.print(graphs[0].containsEdge(i, j) ? 1 : "_");
                }
                * System.out.print("     ");
                for(int j=0; j<graphs[0].getNumVertices(); j++){
                    System.out.print(graphs[1].containsEdge(i, j) ? 1 : "_");
                }
                System.out.print("     ");
                for(int j=0; j<graphs[0].getNumVertices(); j++){
                    System.out.print(graphs[2].containsEdge(i, j) ? 1 : "_");
                }
                System.out.println();
            }*/
        }catch(Exception e){
            System.out.println("Error while reading files:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* Program logic for coliso mode
     */
    private static void coliso(String[] args){
        if(args.length < 3 || args.length > 4)
            printUsageCOLISO();
        try{
            String inputFile = args[1];
            int numVertsPerCol = Integer.parseInt(args[2]);
            System.out.println("InputFile: "+inputFile);
            System.out.println("NumVertsPerCol: "+numVertsPerCol);
            if(numVertsPerCol < 0)
                throw new Exception("Illegal (negative) value for numVertsPerCol: "+numVertsPerCol);
            System.out.println("Reading graph ...");
            Graph g;
            if(args.length == 4 && args[3].equalsIgnoreCase("SIVALab")){
                System.out.println("InputFormat: SIVALab");
                g = Graph.readGraphFromFileSIVALab(BinaryInputReader.readFile(inputFile));
            }else{
                System.out.println("InputFormat: DIMACS");
                g = Graph.readGraphFromFileDIMACS(InputReader.readFile(inputFile));
            }
            System.out.println("Assigning colours ...");
            g.assignRandomColours(numVertsPerCol);
            System.out.println("Creating isomorphic graph ...");
            Graph g2 = Graph.createIsomorphicGraph(g);
            System.out.println("Writing graphs ...");
            OutputWriter.writeFile(g.toStringColoured(), inputFile+"-1.clr");
            OutputWriter.writeFile(g2.toStringColoured(), inputFile+"-2.clr");
            System.out.println("done.");
        }catch(Exception e){
            System.out.println("Error while reading files:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    /* Program logic for cnfiso mode
     */
    private static void cnfiso(String[] args){
        if(args.length != 4)
            printUsageCNFISO();
        String inFile1 = args[1];
        String inFile2 = args[2];
        String outFile = args[3];
        try{
            System.out.println("Reading SAT instances ...");
            SAT_Instance sat1 = SAT_Instance.createSAT_InstanceFromFile(InputReader.readFile(inFile1));
            SAT_Instance sat2 = SAT_Instance.createSAT_InstanceFromFile(InputReader.readFile(inFile2));
            System.out.println("Converting to graphs ...");
            Graph g1 = SAT_to_Graph.convert(sat1);
            Graph g2 = SAT_to_Graph.convert(sat2);

            System.out.println("Converting to SAT ...");
            OutputWriter.beginFile(outFile);
            OutputWriter.append("c Instance representing the isomorphism of two sat instances:\nc "
                +inFile1+"\nc "+inFile2+"\n");
            int[][] varNames = GI_to_SAT.convert(g1, g2, true);
            OutputWriter.closeFile();
            System.out.println("Writing variable mapping ...");
            OutputWriter.beginFile(outFile+"_varMapping");
            for(int i=0; i<varNames.length; i++)
                for(int j=0; j<varNames[0].length; j++)
                    if(varNames[i][j] != -1)
                        OutputWriter.append(varNames[i][j]+" "+(i+1)+" "+(j+1)+"\n");
            OutputWriter.closeFile();
            System.out.println("done.");
        }catch(Exception e){
            System.out.println("Error while reading files:");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    /* Program logic for cnfg mode
     */
    private static void cnfg(String[] args){
            try{
                if(args.length != 3)
                    printUsageCNFG();
                System.out.println("Reading instance ...");
                SAT_Instance sat = SAT_Instance.createSAT_InstanceFromFile(InputReader.readFile(args[1]));
                sat.appendFileNameInfo(args[1]);
                System.out.println("Converting ...");
                OutputWriter.beginFile(args[2]);
                Graph g = SAT_to_Graph.convert(sat);
                System.out.println("Writing ...");
                OutputWriter.append(g.toString());
                OutputWriter.closeFile();
                System.out.println("Done!");
            }catch(Exception e){
                System.out.println("Error while reading file: \n");
                System.out.println(e.getMessage());
                System.exit(1);
            }
    }


    /* Program logic for labs mode
     */
    private static void labs(String[] args){
            if(args.length < 4 || args.length > 5)
                printUsageLABS();
            int length = stringToInt(args[1]);
            int minTarget = stringToInt(args[2]);
            int maxTarget = stringToInt(args[3]);
            boolean energy = false;
            if(args.length == 5){
                if(args[4].equalsIgnoreCase("energy"))
                    energy = true;
                else if(!args[4].equalsIgnoreCase("autoCorr"))
                    printUsageLABS();
            }
            System.out.print("Converting to PBS ... ");
            labs_to_pbs(energy, length, minTarget, maxTarget);
            System.out.println("done.");
    }

    /* program logic for cg mode
     */
    private static void cg(String[] args){
        if(args.length != 3)
                printUsageCG();
            try{
                Random rng = new Random();
                System.out.print("Creating graphs ... ");
                int numVertices = Integer.parseInt(args[1]);
                double edgeProbability = Double.parseDouble(args[2]);
                if(numVertices < 0)
                    throw new Exception("Number of Vertices is negative: "+numVertices);
                if(edgeProbability < 0 || edgeProbability > 1)
                    throw new Exception("Edge probability is not in [0,1]: "+edgeProbability);
                Graph g = Graph.createRandomGraph(numVertices-1, edgeProbability);
                Graph g1 = Graph.createVariation(g, rng);
                Graph g2 = Graph.createIsomorphicGraph(g1);
                Graph g3 = Graph.createVariation(g, rng);
                System.out.print("done\nWriting graphs to files ... ");
                String outputFileName = "graph_"+toThreeDigits(numVertices)
                                        +"_"+toThreeDigits(toInteger(edgeProbability))+"_";
                OutputWriter.writeFile(g1.toString(), outputFileName+"1.col");
                OutputWriter.writeFile(g2.toString(), outputFileName+"2.col");
                OutputWriter.writeFile(g3.toString(), outputFileName+"3.col");
                System.out.println("done.");
            }catch(NumberFormatException e){
                System.out.println("ERROR: One of the parameters is not a valid number!");
                System.out.println(e.getMessage());
            }catch(Exception e){
                System.out.println("ERROR:");
                System.out.println(e.getMessage());
                System.exit(1);
            }
    }
    /* Program logic for gi mode
     */
    private static void gi(String[] args){
            if(args.length < 3 || args.length > 6)
                printUsageGI();
            String outputFile = "out.cnf";
            try{
                boolean simplify = false;
                String inputFormat = "DIMACS";
                if(args.length > 3){
                    for(int i=3; i<args.length; i++){
                        if(args[i].equalsIgnoreCase("simplify"))
                            simplify=true;
                        else if(args[i].startsWith("out="))
                            outputFile = args[i].substring(4);
                        else if(args[i].equalsIgnoreCase("SIVALab"))
                            inputFormat = "SIVALab";
                        else if(args[i].equalsIgnoreCase("COLOURED"))
                            inputFormat = "COLOURED";
                        else
                            System.out.println("Unrecognised parameter: "+args[i]);
                    }
                }
                System.out.println("Input format is: "+inputFormat);
                System.out.println("Output file: "+outputFile);
                System.out.println("Simplify: "+simplify);
                Graph g1, g2;
                System.out.println("Reading graph files ... ");
                if(inputFormat.equalsIgnoreCase("DIMACS")){
                    g1 = Graph.readGraphFromFileDIMACS(InputReader.readFile(args[1]));
                    g2 = Graph.readGraphFromFileDIMACS(InputReader.readFile(args[2]));

                }else if(inputFormat.equalsIgnoreCase("COLOURED")){
                    g1 = Graph.readGraphFromFileCOLOURED(InputReader.readFile(args[1]));
                    g2 = Graph.readGraphFromFileCOLOURED(InputReader.readFile(args[2]));
                }else{
                    g1 = Graph.readGraphFromFileSIVALab(BinaryInputReader.readFile(args[1]));
                    g2 = Graph.readGraphFromFileSIVALab(BinaryInputReader.readFile(args[2]));
                }
                //OutputWriter.writeFile(g1.toString(), "graph1.col");
                //OutputWriter.writeFile(g2.toString(), "graph2.col");
                System.out.println("Converting to SAT ...");
                OutputWriter.beginFile(outputFile);
                OutputWriter.append("c Instance representing the graph isomorphism of two graphs:\nc "
                    +args[1]+"\nc "+args[2]+"\n");
                OutputWriter.append("c Graphs have "+g1.getNumVertices()+" vertices and "+g1.getNumEdges()+" edges\n");
                int[][] varNames = GI_to_SAT.convert(g1, g2, simplify);
                OutputWriter.closeFile();
                System.out.println("Writing variable mapping ...");
                OutputWriter.beginFile(outputFile+"_varMapping");
                for(int i=0; i<varNames.length; i++)
                    for(int j=0; j<varNames[0].length; j++)
                        if(varNames[i][j] != -1)
                            OutputWriter.append(varNames[i][j]+" "+(i+1)+" "+(j+1)+"\n");
                OutputWriter.closeFile();
                System.out.println("done.");
                //sat.setGraphNames(args[1], args[2]);

                //OutputWriter.writeInstance(sat, outputFile);
            }catch(Exception e){
                System.out.println("ERROR: "+e.getMessage());
                //e.printStackTrace();
                try{
                    //clean up files
                    OutputWriter.cleanUp(outputFile);
                    OutputWriter.cleanUp(outputFile+"_varMapping");
                }catch(Exception e1){}
                System.exit(1);
            }
    }

     /* prints out instuctions on how to use the program
     */
    private static void printUsage(){
        System.out.println("Use java -jar InstanceConverter.jar <mode> to see how to use the program.");
        System.out.println("Currently supported modes are:");
        System.out.println("labs - converts the low autocorrelation binary sequence (labs) or minimum peak side lobes (psl) problems into a "
                            +"series of pseudo boolean satisfaction (pbs) problems");
        System.out.println("gi - converts the graph isomorphism (gi) problem into the sat (satisfiability) problem");
        System.out.println("cg - creates 3 random undirected graphs: 2 are isomorphic, the third one is *probably* not");
        System.out.println("cnfg - converts a SAT instance in cnf into a graph in DIMACS format. Used to test for isomorphism in two SAT instances");
        System.out.println("cnfiso - takes two SAT instances in cnf format and converts them into a single SAT instance that is satisfiable if and only if the two input instances are isomorph");
        System.out.println("coliso - takes an non-coloured input graph and a number of vertices per colour, and outputs the (randomly coloured) original graph aswell as a (coloured) isomorphic graph");
        System.out.println("buildgraphs - takes a graph, assigns random colours to it's vertices, then creates three (substantially bigger) graphs for isomorphism testing");
        System.out.println("convertGenreg - takes a file in GENREG format, and converts the graphs into the more usable DIMACS format");
        System.exit(1);
    }
     /* prints out instructions on how to usethe program in convertGenreg mode
     */
    private static void printUsageCONVERTGENREG(){
        System.out.println("Usage:\njava -jar InstanceConverter.jar convertGenreg <inputFile> [<maxGraphNumber>]");
        System.out.println("inputFile - input file containing the graphs in GENREG format");
        System.out.println("maxGraphNumber - if the input file contains more than <maxGraphNumber> graphs, only the first <maxGraphNumber> graphs will be converted");
        System.out.println("GENREG: http://www.mathe2.uni-bayreuth.de/markus/reggraphs.html");
        System.out.println("DIMACS: http://prolland.free.fr/works/research/dsat/dimacs.html");
        System.out.println("Note that the naming conventions for input files need to be followed"
                +"\ni.e. the filename must be <numVertices>_<degree>_<minGirth>.scd"
                +"\nThe <minGirth> parameter can be ommitted, but both \"_\" need to be present");
        System.exit(1);
    }
    /* prints out instructions on how to usethe program in buildgraphs mode
     */
    private static void printUsageBUILDGRAPHS(){
        System.out.println("Usage:\njava -jar InstanceConverter.jar buildgraphs <inputFile> <numVertsPerCol> [<inputFormat>]");
        System.out.println("inputFile - input file containing a non-coloured graph in either DIMACS or SIVALab format");
        System.out.println("numVertsPerCol - maximum number of vertices that will share a single colour (in the *original* graph");
        System.out.println("inputFormat - format of <inputFile>. Can be either \"DIMACS\" or \"SIVALab\" (default is DIMACS)\n"
                +"DIMACS: http://prolland.free.fr/works/research/dsat/dimacs.html\n"
                +"SIVALab: https://docs.google.com/viewer?a=v&q=cache:0_k1HsL70CsJ:amalfi.dis.unina.it/graph/doc/graphdb.pdf+sivalab+graph&hl=es&pid=bl&srcid=ADGEESjAkVTGU9xlfHuKq8VqTAOeV-HkkEUJISO-gqu5GBImNt_imx8PUCHzAJhF478_BrGln2zcIwbqT53-uXmQA8gqY94nLOLldosEXrOGN0HDSBPlDFViBgoYdLr8dZbKzAG4q7e-&sig=AHIEtbQ_4A1vb8gtlrCBzyCAxBfgZJgVdA");
        System.out.println("Output graphs will be written to \"<inputFile>_<numVertsPerCol>-1.clr\" , \"<inputFile>_<numVertsPerCol>-2.clr\" and \"<inputFile>_<numVertsPerCol>-3.clr\", in COLOURED format");
        System.out.println("Graphs 1 and 2 will be isomorphic, graphs 1 and 3 aswell as 2 and 3 will not be isomorphic.");
        System.exit(1);
    }
    /* prints out instructions on how to use the program in coliso mode
     */
    private static void printUsageCOLISO(){
        System.out.println("Usage:\njava -jar InstanceConverter.jar coliso <inputFile> <numVertsPerCol> [<inputFormat>]");
        System.out.println("inputFile - input file containing a non-coloured graph in either DIMACS or SIVALab format");
        System.out.println("numVertsPerCol - maximum number of vertices that will share a single colour");
        System.out.println("inputFormat - format of <inputFile>. Can be either \"DIMACS\" or \"SIVALab\" (default is DIMACS)\n"
                +"DIMACS: http://prolland.free.fr/works/research/dsat/dimacs.html\n"
                +"SIVALab: https://docs.google.com/viewer?a=v&q=cache:0_k1HsL70CsJ:amalfi.dis.unina.it/graph/doc/graphdb.pdf+sivalab+graph&hl=es&pid=bl&srcid=ADGEESjAkVTGU9xlfHuKq8VqTAOeV-HkkEUJISO-gqu5GBImNt_imx8PUCHzAJhF478_BrGln2zcIwbqT53-uXmQA8gqY94nLOLldosEXrOGN0HDSBPlDFViBgoYdLr8dZbKzAG4q7e-&sig=AHIEtbQ_4A1vb8gtlrCBzyCAxBfgZJgVdA");
        System.out.println("Output graphs will be written to \"<inputFile>-1.clr\" and \"<inputFile>-2.clr\", in COLOURED format");
        System.exit(1);
    }

    /* prints out instructions on how to use the program in cnfiso mode
     */
    private static void printUsageCNFISO(){
        System.out.println("Usage: \njava -jar InstanceConverter.jar cnfiso <inputCNF1> <inputCNF2> <outputCNF>");
        System.out.println("inputCNF1/2 - input files containing sat instances in cnf");
        System.out.println("outputCNF - sat instance that is satisfiable in and only if the two input instances are isomorph");
        System.exit(1);
    }

    /* prints out instructions on how to use the program in cnfg mode
     */
    private static void printUsageCNFG(){
        System.out.println("Usage: \njava -jar InstanceConverter.jar cnfg <inputFile> <outputFile>");
        System.out.println("inputFile - File containing a SAT instance in cnf");
        System.out.println("outputFile - The graph will be written to this file");
        System.exit(1);
    }

    /* prints out instructions on how to use the program in cg mode
     */
    private static void printUsageCG(){
        System.out.println("Usage: \njava- jar InstanceConverter.jar cg <numVertexes> <edgeProbability>");
        System.out.println("numVertexes - The number of vertexes the graphs are supposed to have");
        System.out.println("edgeProbability - The approx. probability that "
                +"there will be an edge between any two vertexes");
        System.out.println("The graphs will be written to graph1.col, graph2.col, graph3.col");
        System.out.println("Graphs 1 and 2 are isomorphic, graph 1 and 3 (and 2 and 3) are *probably* not");
        System.exit(1);
    }

    /* prints out instructions on how to use the program in gi-to-sat mode
     */
    private static void printUsageGI(){
        System.out.println("Usage: \njava -jar InstanceConverter.jar gi <graph1> <graph2> "
                +"[out=<outputFile>] [simplify] [SIVALab]");
        System.out.println("graph1/2 - Files containing one graph each, default format is DIMACS "
                +"(http://prolland.free.fr/works/research/dsat/dimacs.html)");
        System.out.println("simplify - if set, the converter will attempt to simplify the "
                +"resulting sat instance (simplification is highly recommended)");
        System.out.println("SIVALab - set this, if the input graphs are in SIVALab format "
                +"(https://docs.google.com/viewer?a=v&q=cache:0_k1HsL70CsJ:amalfi.dis.unina.it/graph/doc/graphdb.pdf+sivalab+graph&hl=es&pid=bl&srcid=ADGEESjAkVTGU9xlfHuKq8VqTAOeV-HkkEUJISO-gqu5GBImNt_imx8PUCHzAJhF478_BrGln2zcIwbqT53-uXmQA8gqY94nLOLldosEXrOGN0HDSBPlDFViBgoYdLr8dZbKzAG4q7e-&sig=AHIEtbQ_4A1vb8gtlrCBzyCAxBfgZJgVdA) instead of DIMACS");
        System.out.println("COLOURED - set this, if the input graphs are in COLOURED format");
        System.out.println("outputFile - the generated sat instance will be written to this file "
                +"(default filename: out.cnf). Also, the mapping of variables (cnf vars and graph vertexes) "
                +"will be written to <outputFile>_varMapping");
        System.exit(1);
    }

    /* prints out instructions on how to use the program in labs-to-pbs mode
     */
    private static void printUsageLABS(){
        System.out.println("Usage: \njava -jar InstanceConverter.jar"
                +" labs <length> <minTarget> <maxTarget> <targetType>");
        System.out.println("length - The number of bits in the binary sequence");
        System.out.println("minTarget/maxTarget - The (inclusive) range of target values for which"
                +" PBS instances will be created");
        System.out.println("targetType - [energy|autoCorr] whether the specified targets are to be understood "
                +"as energies, or maximum values for the autocorrelation");
        System.exit(1);
    }

    private static void labs_to_pbs(boolean energy, int length, int minTarget, int maxTarget){
        int[] ts = new int[maxTarget-minTarget+1];
        for(int i=0; i<ts.length; i++)
            ts[i] = i + minTarget;
        LABS_Instance labs = new LABS_Instance(length, ts);
        String outputFileName = "PSL_n"+toThreeDigits(length);

        PBS_Instance[] instances;
        if(energy)
            instances = LABS_to_PBS.convert(labs);
        else
            instances = LABS_to_PBS_simple.convert(labs);
        int count=0;
        int[] targets = labs.getTargets();
        for(PBS_Instance i : instances){
            String n = outputFileName+"_goal"+toThreeDigits(targets[count]);
            try{
                OutputWriter.writeInstance(i, n+".obp");
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            count++;
        }
    }

    /* converts a double in [0,1] to an integer in [0,100]
     */
    private static int toInteger(double d){
        if(d > 1 || d < 0)
            return -1;
        d = d * 100;
        return (int) Math.round(d);
    }

    /* converts an integer number into a string of length 3, with up to 2 leading zeroes if necessary
     */
    private static String toThreeDigits(int i){
        if(i<0 || i>999){
            System.out.println("WARNING: This program wasn't designed to work with filename-numbering > 999.\n"
                    +"Output filenames might be screwed up: "+i);
            return ""+i;
        }
        if(i>99)
            return ""+i;
        if(i<10)
            return "00"+i;
        return "0"+i;
    }

    private static int stringToInt(String s){
        try{
            return Integer.parseInt(s);
        }catch(Exception e){
            System.out.println("ERROR: "+s+" is not an integer!");
            printUsage();
        }
        return -1; //this never happens,  printUsage calls System.exit()
    }
}
