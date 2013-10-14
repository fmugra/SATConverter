/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.labs;

import model.ProblemInstance;

/** Problem specification for the LABS (Low Autocorrelation Binary Sequences) Problem
 *  contains the number of bits in the binary sequence, as well as a list of maximum energies
 *  for which a solution is desired
 *
 * @author Frank Mugrauer
 */
public class LABS_Instance implements ProblemInstance{
    private String fileNameInfo = "";
    private int numberOfBits;
    private int[] targets;

    public LABS_Instance(int numberOfBits, int[] targets){
        this.numberOfBits = numberOfBits;
        this.targets = targets;
    }

    public int[] getTargets() {
        return targets;
    }

    public int getNumberOfBits() {
        return numberOfBits;
    }

    /* String representation of this instance, to be written into a csv file
     *
     */
    @Override
    public String toString(){
        String s = numberOfBits+"\n";
        for(int i : targets)
            s = s + i + ",";
        return s.substring(0, s.length()-1); //cut off superfluous ","
    }
    @Override
    public String instanceType() {
        return "LABS";
    }
    @Override
    public void appendFileNameInfo(String info) {
        fileNameInfo = fileNameInfo + info;
    }

    @Override
    public String getFileNameInfo() {
        return fileNameInfo;
    }

    /* THIS METHOD IS OUTDATED!
     *
     * Attempts to generate a LABS_Instance from lines of Strings read from a file
     * File Syntax should be:
     *
     * <numberOfBits><newline>
     * <energy1>,<energy2>, ... ,<energyN><newline>
     * <EOF>
     *
     * @param inputLines content of the input file (list of lines)
     * @return LABS problem specification

    public static LABS_Instance instanceFromIO(List<String> inputLines) throws Exception{
        int bits = Integer.parseInt(inputLines.get(0));
        if(bits <= 0)
            throw new Exception("ERROR: Input file contains non-positive length for LABS binary sequence: "+bits);
        String[] targets = inputLines.get(1).split(",");
        int[] ens = new int[targets.length];
        for(int i=0; i<ens.length; i++){
            ens[i] = Integer.parseInt(targets[i]);
            if(ens[i] < 0)
                throw new Exception("ERROR: Input file contains negative energy for LABS autocorrelation: "+ens[i]);
        }
        return new LABS_Instance(bits, ens);
    }*/
}
