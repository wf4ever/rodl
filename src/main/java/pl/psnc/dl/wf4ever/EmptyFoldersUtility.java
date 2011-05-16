/**
 * 
 */
package pl.psnc.dl.wf4ever;

import java.util.regex.Pattern;

/**
 * @author piotrhol
 *
 * Terms:
 * - real path - file path of an empty folder, i.e. /results/series1/
 * - dLibra path - file path of a file representing an empty folder, i.e. /results/series1.emptyfolder
 */
public class EmptyFoldersUtility
{

	private static final Pattern dlibraPathPattern = Pattern
			.compile(".*\\.emptyfolder");

	private static String dLibraPathSuffix = ".emptyfolder";


	/**
	 * Checks if supplied file path represents an empty folder in dLibra. Checks file path syntax only, without connecting to dLibra.
	 * @param filePath File path, i.e. /results/series1.emptyfolder
	 * @return true if this filepath represents
	 */
	public static boolean isDlibraPath(String filePath)
	{
		return dlibraPathPattern.matcher(filePath).matches();
	}


	public static String convertDlibra2Real(String filePath)
	{
		if (!isDlibraPath(filePath))
			throw new IllegalArgumentException(
					"Argument is not dlibra empty folder path");
		return filePath.substring(0, filePath.indexOf(dLibraPathSuffix))
				.concat("/");
	}


	public static String convertReal2Dlibra(String filePath)
	{
		if (filePath.endsWith("/"))
			filePath = filePath.substring(0, filePath.length() - 1);
		return filePath.concat(dLibraPathSuffix);
	}
}
