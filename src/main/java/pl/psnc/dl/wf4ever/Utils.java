/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dlibra.DLibraDataSource;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.service.DLibraException;

/**
 * @author piotrhol
 *
 */
public class Utils
{
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(Utils.class);


	public static EditionId getEditionId(DLibraDataSource dLibra, String RO,
			String version, long editionId)
		throws RemoteException, DLibraException
	{
		if (editionId == Constants.EDITION_QUERY_PARAM_DEFAULT) {
			return dLibra.getEditionHelper().getEditionId(RO, version);
		}
		else {
			return new EditionId(editionId);
		}
	}
}
