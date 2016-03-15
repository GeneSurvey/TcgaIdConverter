/*
TcgaIdConverter Copyright 2014, 2015, 2016 University of Texas MD Anderson Cancer Center

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.mda.bioinfo.ids;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author tdcasasent
 */
public class PrepFiles
{

	public String mDownloadDir = null;

	public PrepFiles(String theDownloadDir)
	{
		mDownloadDir = theDownloadDir;
	}

	public void prepFiles() throws IOException, Exception
	{
		{
			// mimat TO mirbase
			String rawFile = new File(mDownloadDir, "mimatTOmirbase_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "mimatTOmirbase.tsv").getAbsolutePath();
			prep_mimatTOmirbase(rawFile, preppedFile);
		}
		{
			// mature mirs
			String rawFile = new File(mDownloadDir, "mature_mirs_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "mature_mirs.tsv").getAbsolutePath();
			prep_mature_mirs(rawFile, preppedFile);
		}
		{
			// mirbase to gene symbol
			// Entrez num to gene symbol
			String rawFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol.tsv").getAbsolutePath();
			prep_mirbaseANDentreznumTOgenesymbol(rawFile, preppedFile);
		}
		{
			// UC ID to gene symbol
			String rawFile = new File(mDownloadDir, "ucidTOgenesymbol_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "ucidTOgenesymbol.tsv").getAbsolutePath();
			prep_ucidTOgenesymbol(rawFile, preppedFile);
		}
		{
			// one to one ucsc and hgnc (ucid and gene symbol) mapping
			String rawHgncFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol_raw.tsv").getAbsolutePath();
			String rawUcscFile = new File(mDownloadDir, "ucidTOgenesymbol_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "oneToOneUcscHgnc.tsv").getAbsolutePath();
			prep_oneToOneUcscHgnc(rawHgncFile, rawUcscFile, preppedFile);
		}
		{
			// one to one ucsc and hgnc (ucid and gene symbol) mapping
			String rawHgncFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol_raw.tsv").getAbsolutePath();
			String preppedFile = new File(mDownloadDir, "geneSynonyms.tsv").getAbsolutePath();
			prep_geneSynonyms(rawHgncFile, preppedFile);
		}
	}

	protected void prep_geneSynonyms(String theRawHgncFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		TreeMap<String, TreeSet<String>> symbolToSynonyms = new TreeMap<>();
		// first read the HGNC which has one to one maps
		try(BufferedReader br = new BufferedReader(new FileReader(theRawHgncFile)))
		{
			// index 1 = Approved Symbol
			// index 4 = Status
			// index 6 = Previous Symbols
			// index 8 = Synonyms
			String inLine = br.readLine();
			String hgncIDcolText = "Approved Symbol";
			String previousSym = "Previous Symbols";
			String synonym = "Synonyms";
			ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
			int hgncIndex = columns.indexOf(hgncIDcolText);
			if (-1==hgncIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			int previousSymIndex = columns.indexOf(previousSym);
			if (-1==previousSymIndex)
			{
				throw new Exception("Index for '" + previousSym + "' not found");
			}
			int synonymIndex = columns.indexOf(synonym);
			if (-1==synonymIndex)
			{
				throw new Exception("Index for '" + synonym + "' not found");
			}
			// 
			inLine = br.readLine();
			while(null!=inLine)
			{
				// index 1 = Approved Symbol
				// index 4 = Status
				// index 6 = Previous Symbols
				// index 8 = Synonyms
				String [] tabSplit = inLine.split("\t", -1);
				String symbol = tabSplit[hgncIndex];
				String prevSymbols = tabSplit[previousSymIndex];
				String synonyms = tabSplit[synonymIndex];
				if (symbol.endsWith("~withdrawn"))
				{
					symbol = symbol.replaceFirst("~withdrawn", "");
				}
				TreeSet<String> synList = symbolToSynonyms.get(symbol);
				if (null==synList)
				{
					synList = new TreeSet<>();
				}
				for(String sym : prevSymbols.split(",", -1))
				{
					sym = sym.trim();
					synList.add(sym);
				}
				for(String sym : synonyms.split(",", -1))
				{
					sym = sym.trim();
					synList.add(sym);
				}
				symbolToSynonyms.put(symbol, synList);
				inLine = br.readLine();
			}
		}
		// write file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
		{
			for(Entry<String, TreeSet<String>> entry : symbolToSynonyms.entrySet())
			{
				bw.write(entry.getKey());
				for(String syn : entry.getValue())
				{
					if (false=="".equals(syn))
					{
						bw.write("\t" + syn);
					}
				}
				bw.newLine();
			}
		}
	}
	
	protected void prep_oneToOneUcscHgnc(String theRawHgncFile, String theRawUcscFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		TreeMap<String, String> ucscToHgnc = new TreeMap<>();
		TreeMap<String, String> hgncToUcsc = new TreeMap<>();
		// first read the HGNC which has one to one maps
		try(BufferedReader br = new BufferedReader(new FileReader(theRawHgncFile)))
		{
			String inLine = br.readLine();
			String hgncIDcolText = "Approved Symbol";
			String ucscIDcolText = "UCSC ID (supplied by UCSC)";
			ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
			int hgncIndex = columns.indexOf(hgncIDcolText);
			int ucscIndex = columns.indexOf(ucscIDcolText);
			if (-1==hgncIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			if (-1==ucscIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			// skip header line
			inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String hgnc = tabSplit[hgncIndex];
				String ucsc = tabSplit[ucscIndex];
				if (false=="".equals(ucsc))
				{
					if (hgnc.endsWith("~withdrawn"))
					{
						hgnc = hgnc.replaceFirst("~withdrawn", "");
					}
					String coll = ucscToHgnc.put(ucsc, hgnc);
					if(null!=coll)
					{
						throw new Exception("Collision from HGNC ucscToHgnc " + ucsc + ", " + hgnc + " & " + coll);
					}
					coll = hgncToUcsc.put(hgnc, ucsc);
					if(null!=coll)
					{
						throw new Exception("Collision from HGNC hgncToUcsc " + hgnc + ", " + ucsc + " & " + coll);
					}
				}
				inLine = br.readLine();
			}
		}
		// second read the UCSC which has one to many maps, and only process ones with N on second column
		try(BufferedReader br = new BufferedReader(new FileReader(theRawUcscFile)))
		{
			TreeSet<String> replimatchOld = new TreeSet<>();
			replimatchOld.addAll(ucscToHgnc.keySet());
			replimatchOld.addAll(hgncToUcsc.keySet());
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String ucsc = tabSplit[0];
				//String otherid = tabSplit[1];
				String hgnc = tabSplit[4];
				// file contains non-gene symbols that can be detected with lower case letters
				if (hgnc.equals(hgnc.toUpperCase()))
				{
					if (false==ucscToHgnc.keySet().contains(ucsc))
					{
						if (false==hgncToUcsc.keySet().contains(hgnc))
						{
							if(null!=ucscToHgnc.put(ucsc, hgnc))
							{
								throw new Exception("Collision from UCSC ucscToHgnc " + ucsc + ", " + hgnc);
							}
							if(null!=hgncToUcsc.put(hgnc, ucsc))
							{
								throw new Exception("Collision from UCSC hgncToUcsc " + hgnc + ", " + ucsc);
							}
						}
					}
				}
				//
				inLine = br.readLine();
			}
		}
		// write one to one file oneToOneUcscHgnc
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
		{
			for(Entry<String, String> entry : ucscToHgnc.entrySet())
			{
				bw.write(entry.getKey() + "\t" + entry.getValue());
				bw.newLine();
			}
		}
	}
	
	protected void prep_mimatTOmirbase(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(theRawFile)))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String mimat = tabSplit[0];
					String mirbases = tabSplit[1];
					String [] semicolonSplit = mirbases.split(";", -1);
					String longest = "";
					int length = 0;
					String fivepThreep = "";
					String star = "";
					String output = null;
					mirbases = mirbases.toLowerCase();
					if (mimat.startsWith("MIMAT"))
					{
						if (mirbases.startsWith("hsa"))
						{
							for(String mir : semicolonSplit)
							{
								if ((mir.contains("5p"))||(mir.contains("5P")))
								{
									fivepThreep = fivepThreep + mir + ";";
								}
								if ((mir.contains("3p"))||(mir.contains("3P")))
								{
									fivepThreep = fivepThreep + mir + ";";
								}
								if (mir.contains("*"))
								{
									star = star + mir + ";";
								}
								if (mir.length()>length)
								{
									longest = mir + ";";
									length = mir.length();
								}
								else if (mir.length()==length)
								{
									longest = longest + mir + ";";
								}
							}
							//
							output = mimat + "\t";
							if(false=="".equalsIgnoreCase(fivepThreep))
							{
								output = output + fivepThreep;
							}
							else if(false=="".equalsIgnoreCase(star))
							{
								output = output + star;
							}
							else
							{
								output = output + longest;
							}
							bw.write(output);
						}
					}
					//
					inLine = br.readLine();
					if ((null!=output)&&(null!=inLine))
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_mature_mirs(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(theRawFile)))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String mir = null;
					if (inLine.contains("Homo sapiens"))
					{
						String [] spaceSplit = inLine.split(" ", -1);
						mir = spaceSplit[0];
						mir = mir.replaceFirst(">", "");
						mir = mir.toLowerCase();
						bw.write(mir);
					}
					inLine = br.readLine();
					if ((null!=inLine)&&(null!=mir))
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_mirbaseANDentreznumTOgenesymbol(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		try(BufferedReader br = new BufferedReader(new FileReader(theRawFile)))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				// Approved Symbol
				// Synonyms
				// Entrez Gene ID
				String hgncIDcolText = "Approved Symbol";
				String synonym = "Synonyms";
				String entrezId = "Entrez Gene ID";
				ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
				int hgncIndex = columns.indexOf(hgncIDcolText);
				if (-1==hgncIndex)
				{
					throw new Exception("Index for '" + hgncIDcolText + "' not found");
				}
				int entrezIdIndex = columns.indexOf(entrezId);
				if (-1==entrezIdIndex)
				{
					throw new Exception("Index for '" + entrezId + "' not found");
				}
				int synonymIndex = columns.indexOf(synonym);
				if (-1==synonymIndex)
				{
					throw new Exception("Index for '" + synonym + "' not found");
				}
				// skip header line
				inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String entrezGeneNum = tabSplit[entrezIdIndex];
					String symbol = tabSplit[hgncIndex];
					String mirbase = tabSplit[synonymIndex];
					if (false==mirbase.startsWith("hsa-mir"))
					{
						mirbase = "";
					}
					if (symbol.endsWith("~withdrawn"))
					{
						symbol = symbol.replaceFirst("~withdrawn", "");
						mirbase = "withdrawn";
					}
					String output = symbol + "\t" + entrezGeneNum + "\t" + mirbase;
					bw.write(output);
					//
					inLine = br.readLine();
					if (null!=inLine)
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_ucidTOgenesymbol(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(theRawFile)))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String ucid = tabSplit[0];
					String symbol = tabSplit[4];
					String output = ucid + "\t" + symbol;
					bw.write(output);
					//
					inLine = br.readLine();
					if (null!=inLine)
					{
						bw.newLine();
					}
				}
			}
		}
	}

}