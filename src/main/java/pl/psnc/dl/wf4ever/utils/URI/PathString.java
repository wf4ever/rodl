package pl.psnc.dl.wf4ever.utils.URI;

public class PathString {

    public static String removeFirstSlash(String path) {
        if (path.length() == 0)
            return path;
        if (path.toCharArray()[0] == '/')
            return path.substring(1);
        else
            return path;
    }
    
    public static String getFileName(String path) {
        String[] list = path.split("/");
        return list[list.length-1];
    }

}
