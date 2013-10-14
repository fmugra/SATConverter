
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/** Convenience class for reading input files
 *
 * @author Frank Mugrauer
 */
public class InputReader {

    /* Attempts to read an input file
     *
     * @param path path to the input file to be read
     * @return List of strings, each string is a line in the input file
     */
    public static List<String> readFile(String path) throws Exception{
        File f = new File(path);
        if(!f.canRead())
            throw new Exception("Unable to read file: "+path);
        BufferedReader reader = new BufferedReader(new FileReader(f));
        LinkedList<String> content = new LinkedList<String>();
        String line;
        while((line = reader.readLine()) != null)
            content.add(line);
        return content;
    }
}
