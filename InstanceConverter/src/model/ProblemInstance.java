
package model;

/** Interface for all representations of problem instances (e.g. SAT, PBS, LABS, ...)
 *
 * @author Frank Mugrauer
 */
public interface ProblemInstance {

    /* String representation of this instance, to be written to a file
     */
    @Override
    public String toString();

    /* Abbreviation of this instance type, e.g. "SAT" or "PBS"
     */
    public String instanceType();

    /* when reading/writing instances from/to files, information about the file name can be stored here
     */
    public void appendFileNameInfo(String info);

    /* when reading/writing instances from/to files, stored information about the file name can be retreived here
     */
    public String getFileNameInfo();
}
