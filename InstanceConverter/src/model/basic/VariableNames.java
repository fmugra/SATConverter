/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.basic;

/**
 *
 * @author fr
 */
public class VariableNames {
    private static int count = 0;

    public static int newVarID(){
        count++;
        if(count < 0)
            System.out.println("ERROR: Integer overflow in VariableNames: too many variables!");
        return count;
    }
}
