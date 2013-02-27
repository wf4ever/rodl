package pl.psnc.dl.wf4ever.sparql;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.xmloutput.impl.Basic;

public class RO_RDFXMLWriter
	extends Basic
	implements ResearchObjectRelativeWriter
{

	private static final Logger log = Logger.getLogger(RO_RDFXMLWriter.class);

	private URI researchObjectURI;

	private URI baseURI;


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.sms.ResearchObjectRelativeWriter#setResearchObjectURI(java.net.URI)
	 */
	@Override
	public void setResearchObjectURI(URI researchObjectURI)
	{
		this.researchObjectURI = researchObjectURI;
	}


	@Override
	protected String relativize(String uri)
	{
		if (researchObjectURI == null || baseURI == null) {
			return super.relativize(uri);
		}
		URI resourceURI = URI.create(uri).normalize();
		if (resourceURI.toString().startsWith(researchObjectURI.toString())) {
			Path localPath = Paths.get(baseURI.resolve(".").getPath()).relativize(Paths.get(resourceURI.getPath()));
			String path = localPath.toString();
			try {
				return new URI(null, null, path, resourceURI.getQuery(), resourceURI.getFragment()).toString();
			}
			catch (URISyntaxException e) {
				log.error("Can't relativize the URI " + resourceURI, e);
				return path;
			}
		}
		else {
			return super.relativize(uri);
		}
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.sms.ResearchObjectRelativeWriter#getResearchObjectURI()
	 */
	@Override
	public URI getResearchObjectURI()
	{
		return researchObjectURI;
	}


	/* (non-Javadoc)
	 * @see pl.psnc.dl.wf4ever.sms.ResearchObjectRelativeWriter#setBaseURI(java.net.URI)
	 */
	@Override
	public void setBaseURI(URI baseURI)
	{
		this.baseURI = baseURI;
	}
}
