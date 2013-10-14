/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.io.File;
import java.io.FileWriter;
import model.ProblemInstance;

/** Convenience class for writing problem instance specifications to files
 *
 * @author Frank Mugrauer
 */
public class OutputWriter {
    private static FileWriter writer;
    private static boolean open;
    /* attempts to create a file at fileName, then attempts to write the content of instance into it
     */
    public static void writeInstance(ProblemInstance instance, String fileName) throws Exception{
        File f = new File(fileName);
        if(f.exists()){
            f.delete();
            f = new File(fileName);
        }
        f.createNewFile();
        if(!f.canWrite()){
            throw new Exception("ERROR: Cannot write file: "+fileName);
        }
        FileWriter writer = new FileWriter(f);
        System.out.print("Generating content ... ");
        String content = instance.toString();
        System.out.print("done.\nWriting to file ... ");
        writer.write(content, 0, content.length());
        writer.flush();
        writer.close();
        System.out.println("done.");
    }

    public static void writeFile(String content, String fileName) throws Exception{
        File f = new File(fileName);
        if(f.exists()){
            f.delete();
            f = new File(fileName);
        }
        f.createNewFile();
        if(!f.canWrite()){
            throw new Exception("ERROR: Cannot write file: "+fileName);
        }
        FileWriter writer = new FileWriter(f);
        writer.write(content, 0, content.length());
        writer.flush();
        writer.close();
    }

    public static void beginFile(String fileName) throws Exception{
        File f = new File(fileName);
        if(f.exists()){
            f.delete();
            f = new File(fileName);
        }
        f.createNewFile();
        if(!f.canWrite()){
            throw new Exception("ERROR: Cannot write file: "+fileName);
        }
        writer = new FileWriter(f);
        open = true;
    }
    public static void append(String s) throws Exception{
        writer.append(s);
    }
    public static void closeFile() throws Exception{
        if(open){
            writer.flush();
            writer.close();
            open = false;
        }
    }


    public static void cleanUp(String fileName) throws Exception{
        if(open)
            closeFile();
        File f = new File(fileName);
        if(f.exists())
            f.delete();
    }
}
