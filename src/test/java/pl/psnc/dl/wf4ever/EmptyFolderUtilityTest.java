/**
 * 
 */
package pl.psnc.dl.wf4ever;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author piotrhol
 *
 */
public class EmptyFolderUtilityTest
{

	private final String realPath = "resources/series1";

	private final String realPathSlash = "resources/series1/";

	private final String dlibraPath = "resources/series1.emptyfolder";


	/**
	 * Metoda testu dla {@link pl.psnc.dl.wf4ever.EmptyFoldersUtility#isDlibraPath(java.lang.String)}.
	 */
	@Test
	public void testIsDlibraPath()
	{
		Assert.assertFalse("Real path",
			EmptyFoldersUtility.isDlibraPath(realPath));
		Assert.assertFalse("Real path with slash",
			EmptyFoldersUtility.isDlibraPath(realPathSlash));
		Assert.assertTrue("Dlibra path",
			EmptyFoldersUtility.isDlibraPath(dlibraPath));
	}


	/**
	 * Metoda testu dla {@link pl.psnc.dl.wf4ever.EmptyFoldersUtility#convertDlibra2Real(java.lang.String)}.
	 */
	@Test
	public void testConvertDlibra2Real()
	{
		Assert.assertEquals("dlibra path", realPathSlash,
			EmptyFoldersUtility.convertDlibra2Real(dlibraPath));
		try {
			EmptyFoldersUtility.convertDlibra2Real(realPath);
			Assert.fail("Convert non-dlibra path");
		}
		catch (IllegalArgumentException ex) {
		}
	}


	/**
	 * Metoda testu dla {@link pl.psnc.dl.wf4ever.EmptyFoldersUtility#convertReal2Dlibra(java.lang.String)}.
	 */
	@Test
	public void testConvertReal2Dlibra()
	{
		Assert.assertEquals("Real path", dlibraPath,
			EmptyFoldersUtility.convertReal2Dlibra(realPath));
		Assert.assertEquals("Real path with slash", dlibraPath,
			EmptyFoldersUtility.convertReal2Dlibra(realPathSlash));
	}

}
