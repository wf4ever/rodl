package pl.psnc.dl.wf4ever.rosrs;

import java.net.URI;
import java.util.List;

/**
 * JSON representation of an annotation.
 * 
 * @author piotrekhol
 * 
 */
public class Annotation {

    private URI annotationBody;

    private List<URI> annotationTargets;


    public URI getAnnotationBody() {
        return annotationBody;
    }


    public void setAnnotationBody(URI annotationBody) {
        this.annotationBody = annotationBody;
    }


    public List<URI> getAnnotationTargets() {
        return annotationTargets;
    }


    public void setAnnotationTargets(List<URI> annotationTargets) {
        this.annotationTargets = annotationTargets;
    }

}
