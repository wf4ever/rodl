package pl.psnc.dl.wf4ever.auth;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation allows to inject request attributes.
 * 
 * Adapted from
 * http://jersey.576304.n2.nabble.com/Why-doesn-t-HttpRequestContext-expose-request-attributes-td3953625.html
 * 
 * @author piotrekhol
 * 
 */
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
@Documented
public @interface RequestAttribute {

    /** request attribute name. */
    String value();
}
