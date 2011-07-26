/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.util.Comparator;

import pl.psnc.dlibra.metadata.Edition;

/**
 * @author piotrhol
 * Sorts editions according to their creation date; older editions are before newer.
 */
public class EditionCreatedComparator
	implements Comparator<Edition>
{

	@Override
	public int compare(Edition ed1, Edition ed2)
	{
		if (ed1.getCreationDate().before(ed2.getCreationDate()))
			return -1;
		if (ed1.getCreationDate().after(ed2.getCreationDate()))
			return 1;
		return 0;
	}
}
