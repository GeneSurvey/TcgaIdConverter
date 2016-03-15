/*
TcgaIdConverter Copyright 2014, 2015, 2016 University of Texas MD Anderson Cancer Center

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.mda.bioinfo.ids;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author tdcasasent
 */
public class TcgaIdConverter
{
	protected String mBaseDir = null;
	protected TreeMap<String, String> mMimatTOmirbase = null;
	protected TreeSet<String> mMaturemirs = null;
	protected TreeMap<String, String> mMirbaseTOgenesymbol = null;
	protected TreeMap<String, String> mEntreznumTOgenesymbol = null;
	protected TreeMap<String, String> mUcidTOgenesymbol = null;
	protected TreeMap<String, String> mOneToOne_ucidTOgenesymbol = null;
	protected TreeMap<String, String> mOneToOne_genesymbolToucid = null;
	protected TreeMap<String, TreeSet<String>> mGeneSynonyms = null;

	public TcgaIdConverter(String theBaseDir)
	{
		System.out.println("TcgaIdConvert 2015-12-23-1045");
		mBaseDir = theBaseDir;
	}

	public void getAndPrepFiles() throws IOException, Exception
	{
		System.out.println("getAndPrepFiles start");
		File downloadDir = new File(mBaseDir, "downloads");
		System.out.println("getAndPrepFiles clear download dir");
		FileUtils.deleteQuietly(downloadDir);
		System.out.println("getAndPrepFiles make download dir");
		downloadDir.mkdir();
		System.out.println("getAndPrepFiles DownloadFiles");
		DownloadFiles df = new DownloadFiles(downloadDir.getAbsolutePath());
		df.downloadFiles();
		System.out.println("getAndPrepFiles UncompressFiles");
		UncompressFiles uf = new UncompressFiles(downloadDir.getAbsolutePath());
		uf.uncompressFiles();
		System.out.println("getAndPrepFiles PrepFiles");
		PrepFiles pf = new PrepFiles(downloadDir.getAbsolutePath());
		pf.prepFiles();
		System.out.println("getAndPrepFiles done");
	}

	public void loadFiles() throws IOException, Exception
	{
		System.out.println("TcgaIdConverter::loadFiles start");
		File downloadDir = new File(mBaseDir, "downloads");
		LoadFiles lf = new LoadFiles(downloadDir.getAbsolutePath());
		lf.loadFiles();
		mMimatTOmirbase = lf.mMimatTOmirbase;
		mMaturemirs = lf.mMaturemirs;
		mMirbaseTOgenesymbol = lf.mMirbaseTOgenesymbol;
		mEntreznumTOgenesymbol = lf.mEntreznumTOgenesymbol;
		mUcidTOgenesymbol = lf.mUcidTOgenesymbol;
		mOneToOne_ucidTOgenesymbol = lf.mOneToOne_ucidTOgenesymbol;
		mOneToOne_genesymbolToucid = lf.mOneToOne_genesymbolToucid;
		mGeneSynonyms = lf.mGeneSynonyms;
		System.out.println("TcgaIdConverter::loadFiles done");
	}

	public String convert_mimat_TO_mirbase(String theMimat)
	{
		return mMimatTOmirbase.get(theMimat);
	}

	public boolean is_mature_mirbase(String theMirbase)
	{
		return mMaturemirs.contains(theMirbase);
	}

	public String convert_mirbase_TO_genesymbol(String theMirbase)
	{
		return mMirbaseTOgenesymbol.get(theMirbase);
	}

	public String convert_entreznum_TO_genesymbol(String theEntrezNum)
	{
		return mEntreznumTOgenesymbol.get(theEntrezNum);
	}

	public String convert_ucid_TO_genesymbol(String theUcId)
	{
		return mUcidTOgenesymbol.get(theUcId);
	}

	public String convert_oneToOne_ucid_TO_genesymbol(String theUcId)
	{
		return mOneToOne_ucidTOgenesymbol.get(theUcId);
	}

	public String convert_oneToOne_genesymbol_TO_ucid(String theGeneSymbol)
	{
		return mOneToOne_genesymbolToucid.get(theGeneSymbol);
	}

	public String [] get_oneToOne_ucid_list()
	{
		return mOneToOne_ucidTOgenesymbol.keySet().toArray(new String[0]);
	}

	public String [] get_oneToOne_genesymbol_list()
	{
		return mOneToOne_genesymbolToucid.keySet().toArray(new String[0]);
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			//String baseDir = "/Users/tdcasasent/development/TcgaIdConverter";
			String baseDir = args[0];
			TcgaIdConverter tic = new TcgaIdConverter(baseDir);
			tic.getAndPrepFiles();
			tic.loadFiles();
		}
		catch (Exception exp)
		{
			exp.printStackTrace(System.err);
		}
	}
}
