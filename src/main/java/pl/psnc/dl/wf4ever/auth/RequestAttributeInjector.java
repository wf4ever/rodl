package pl.psnc.dl.wf4ever.auth;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

@Provider
public class RequestAttributeInjector implements InjectableProvider<RequestAttribute, Type> {

    private final HttpContext c;
    private final HttpServletRequest r;


    public RequestAttributeInjector(@Context HttpContext c, @Context HttpServletRequest r) {
        this.c = c;
        this.r = r;
    }


    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }


    public Injectable getInjectable(ComponentContext ic, RequestAttribute a, final Type t) {
        final String name = a.value();

        return new Injectable() {

            public Object getValue() {
                Object o = c.getProperties().get(name);
                if (o == null) {
                    o = r.getAttribute(name);
                }
                if (o == null) {
                    return null;
                }
                // TODO verify that o.getClass() is compatible with Type t 
                return o;
            }
        };
    }
}
