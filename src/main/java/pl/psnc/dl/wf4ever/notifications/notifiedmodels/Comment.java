package pl.psnc.dl.wf4ever.notifications.notifiedmodels;

import pl.psnc.dl.wf4ever.model.AO.Annotation;

/**
 *  The simple wrapper for annotation which represents comment.
 */
public class Comment {
	
	private Annotation annotation;
	
	public Comment(Annotation annotation) {
		this.setAnnotation(annotation);
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
}
