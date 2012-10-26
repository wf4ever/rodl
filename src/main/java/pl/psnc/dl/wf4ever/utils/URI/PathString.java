package pl.psnc.dl.wf4ever.utils.URI;

/**
 * Responsible for obtaining certain data from file path given in the string format.
 * 
 * @author pejot
 * 
 */
public final class PathString {

    /**
     * Private constructor given to not create any object of utility class type.
     */
    private PathString() {
        return;
    }


    /**
     * Get the name of file from path file path given in the string format.
     * 
     * @param path
     *            the file path
     * 
     * @return the name of file
     */
    public static String getFileName(String path) {
        String[] list = path.split("/");
        return list[list.length - 1];
    }

}
