
package io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;

/**
 *
 * @author Frank Mugrauer
 */
public class BinaryInputReader {

    public static byte[] readFile(String path) throws Exception{
        File file = new File(path);
        InputStream input =  new BufferedInputStream(new FileInputStream(file));
        int length = (int) file.length();
        byte[] res = new byte[length];
        input.read(res);
        input.close();
        /*
        for(int i=0; i<res.length; i++)
            System.out.print("|"+res[i]);
        System.out.println("");
        */
        return res;
    }

}
