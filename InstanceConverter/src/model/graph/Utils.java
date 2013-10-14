package model.graph;

import java.util.LinkedList;
import java.util.List;

/** This class provides utility methods for use in this package's class(es)
 *
 * @author Frank Mugrauer
 */
public class Utils {
    private static boolean[] mask;

    
    public static boolean[][] createNodeGraph(int degree){
        int size = 2*degree + (int)Math.pow(2, degree-1);
        boolean[][] adjmatrix = new boolean[size][size];
        //Array is build as follows: [a_1 ... a_deg, b_1 ... b_deg, m_1 ... m_2^(deg-1)]
        int mPos = 2*degree;
        mask = new boolean[degree];
        while(mPos < size){
            for(int i=0; i<mask.length; i++){
                if(mask[i]){ //connect mPos with a_i
                    adjmatrix[i][mPos] = true;
                    adjmatrix[mPos][i] = true;
                }else{      //connect mPos with b_i
                    adjmatrix[degree+i][mPos] = true;
                    adjmatrix[mPos][degree+i] = true;
                }
            }

            mPos++;
            int count;
            do{
                count = 0;
                increaseMask();
                for(int i=0; i<mask.length; i++)
                    if(mask[i])
                        count++;
            }while(count%2 != 0);
        }
        return adjmatrix;
    }

    private static void increaseMask(){
        int i = 0;
        while(mask[i]){
            mask[i]=false;
            i++;
            if(i>=mask.length)
                return;
        }
        mask[i]=true;
    }

}
