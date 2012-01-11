/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author piotrhol
 *
 */
public class UriExtensionsConfig
	extends PackagesResourceConfig
{

	private Map<String, MediaType> mediaTypeMap;


	public UriExtensionsConfig(Map<String, Object> props)
	{
		super(props);
	}


	@Override
	public Map<String, MediaType> getMediaTypeMappings()
	{
		if (mediaTypeMap == null) {
			mediaTypeMap = new HashMap<String, MediaType>();
			mediaTypeMap.put("rdf", new MediaType("application", "rdf+xml"));
			mediaTypeMap.put("ttl", new MediaType("application", "x-turtle"));
			mediaTypeMap.put("trig", new MediaType("application", "x-trig"));
			mediaTypeMap.put("trix", new MediaType("application", "trix"));
			mediaTypeMap.put("n3", new MediaType("text", "rdf+n3"));
		}
		return mediaTypeMap;
	}
}
