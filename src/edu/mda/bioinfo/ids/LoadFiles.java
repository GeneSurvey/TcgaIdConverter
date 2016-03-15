/*
TcgaIdConverter Copyright 2014, 2015, 2016 University of Texas MD Anderson Cancer Center

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.mda.bioinfo.ids;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author tdcasasent
 */
public class LoadFiles
{
	public TreeMap<String, String> mMimatTOmirbase = null;
	public TreeSet<String> mMaturemirs = null;
	public TreeMap<String, String> mMirbaseTOgenesymbol = null;
	public TreeMap<String, String> mEntreznumTOgenesymbol = null;
	public TreeMap<String, String> mUcidTOgenesymbol = null;
	public TreeMap<String, String> mOneToOne_ucidTOgenesymbol = null;
	public TreeMap<String, String> mOneToOne_genesymbolToucid = null;
	public TreeMap<String, TreeSet<String>> mGeneSynonyms = null;

	public String mDownloadDir = null;

	public LoadFiles(String theDownloadDir)
	{
		mDownloadDir = theDownloadDir;
		mMimatTOmirbase = new TreeMap<>();
		mMaturemirs = new TreeSet<>();
		mMirbaseTOgenesymbol = new TreeMap<>();
		mEntreznumTOgenesymbol = new TreeMap<>();
		mUcidTOgenesymbol = new TreeMap<>();
		mOneToOne_ucidTOgenesymbol = new TreeMap<>();
		mOneToOne_genesymbolToucid = new TreeMap<>();
		mGeneSynonyms = new TreeMap<>();
	}

	public void loadFiles() throws IOException, Exception
	{
		System.out.println("LoadFiles::loadFiles start");
		{
			System.out.println("LoadFiles::loadFiles mimatTOmirbase.tsv");
			// mimat TO mirbase
			String preppedFile = new File(mDownloadDir, "mimatTOmirbase.tsv").getAbsolutePath();
			loadMimatTOmirbase(preppedFile);
		}
		{
			System.out.println("LoadFiles::loadFiles mature_mirs.tsv");
			// mature mirs
			String preppedFile = new File(mDownloadDir, "mature_mirs.tsv").getAbsolutePath();
			loadMature_mirs(preppedFile);
		}
		{
			System.out.println("LoadFiles::loadFiles mirbaseANDentreznumTOgenesymbol.tsv");
			// mirbase to gene symbol
			// Entrez num to gene symbol
			String preppedFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol.tsv").getAbsolutePath();
			loadMirbaseANDentreznumTOgenesymbol(preppedFile);
		}
		{
			System.out.println("LoadFiles::loadFiles ucidTOgenesymbol.tsv");
			// UC ID to gene symbol
			String preppedFile = new File(mDownloadDir, "ucidTOgenesymbol.tsv").getAbsolutePath();
			loadUcidTOgenesymbol(preppedFile);
		}
		{
			System.out.println("LoadFiles::loadFiles oneToOneUcscHgnc.tsv");
			// UC ID to gene symbol - one to one map
			String preppedFile = new File(mDownloadDir, "oneToOneUcscHgnc.tsv").getAbsolutePath();
			loadOneToOne(preppedFile);
		}
		{
			System.out.println("LoadFiles::loadFiles geneSynonyms.tsv");
			// gene synonyms
			String preppedFile = new File(mDownloadDir, "geneSynonyms.tsv").getAbsolutePath();
			loadGeneSynonyms(preppedFile);
		}
		System.out.println("LoadFiles::loadFiles done");
	}

	protected void loadGeneSynonyms(String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String symbol = tabSplit[0];
				mGeneSynonyms.put(symbol, new TreeSet(Arrays.asList(tabSplit)));
				inLine = br.readLine();
			}
		}
	}

	protected void loadMimatTOmirbase(String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String mimat = tabSplit[0];
				String mirbases = tabSplit[1];
				mMimatTOmirbase.put(mimat, mirbases);
				inLine = br.readLine();
			}
		}
	}

	protected void loadMature_mirs(String thePreppedFile) throws FileNotFoundException, IOException
	{

		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				mMaturemirs.add(inLine);
				inLine = br.readLine();
			}
		}
	}

	protected void loadMirbaseANDentreznumTOgenesymbol(String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String gene = tabSplit[0];
				String entreznum = tabSplit[1];
				String mirbase = tabSplit[2];
				mMirbaseTOgenesymbol.put(mirbase, gene);
				mEntreznumTOgenesymbol.put(entreznum, gene);
				inLine = br.readLine();
			}
		}
	}

	protected void loadUcidTOgenesymbol(String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String ucid = tabSplit[0];
				String symbol = tabSplit[1];
				mUcidTOgenesymbol.put(ucid, symbol);
				inLine = br.readLine();
			}
		}
	}

	protected void loadOneToOne(String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(thePreppedFile)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String ucid = tabSplit[0];
				String symbol = tabSplit[1];
				mOneToOne_ucidTOgenesymbol.put(ucid, symbol);
				mOneToOne_genesymbolToucid.put(symbol, ucid);
				inLine = br.readLine();
			}
		}
	}

}