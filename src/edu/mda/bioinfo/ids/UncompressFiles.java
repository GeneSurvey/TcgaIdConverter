/*
TcgaIdConverter Copyright 2014, 2015, 2016 University of Texas MD Anderson Cancer Center

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.mda.bioinfo.ids;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/**
 *
 * @author tdcasasent
 */
public class UncompressFiles
{

	public String mDownloadDir = null;

	public UncompressFiles(String theDownloadDir)
	{
		mDownloadDir = theDownloadDir;
	}

	public void uncompressFiles() throws IOException, Exception
	{
		{
			// mimat TO mirbase
			String archiveFile = new File(mDownloadDir, "aliases.txt.gz").getAbsolutePath();
			String uncompressedFile = new File(mDownloadDir, "mimatTOmirbase_raw.tsv").getAbsolutePath();
			unGz(archiveFile, uncompressedFile);
		}
		{
			// mature mirs
			String archiveFile = new File(mDownloadDir, "mature.fa.gz").getAbsolutePath();
			String uncompressedFile = new File(mDownloadDir, "mature_mirs_raw.tsv").getAbsolutePath();
			unGz(archiveFile, uncompressedFile);
		}
		{
			// mirbase to gene symbol
			// Entrez num to gene symbol
			String archiveFile = new File(mDownloadDir, "hgnc_complete_set.txt.gz").getAbsolutePath();
			String uncompressedFile = new File(mDownloadDir, "mirbaseANDentreznumTOgenesymbol_raw.tsv").getAbsolutePath();
			unGz(archiveFile, uncompressedFile);
		}
		{
			// UC ID to gene symbol
			String archiveFile = new File(mDownloadDir, "kgXref.txt.gz").getAbsolutePath();
			String uncompressedFile = new File(mDownloadDir, "ucidTOgenesymbol_raw.tsv").getAbsolutePath();
			unGz(archiveFile, uncompressedFile);
		}

	}

	protected boolean unGz(String theGzFile, String theUngzFile) throws IOException
	{
		boolean uncompressed = false;
		// uncompress
		FileInputStream fin = null;
		BufferedInputStream in = null;
		FileOutputStream out = null;
		GzipCompressorInputStream gzIn = null;
		try
		{
			System.out.println("Uncompress::unGz starting for the file " + theGzFile);
			fin = new FileInputStream(theGzFile);
			in = new BufferedInputStream(fin);
			File outfile = new File(theUngzFile);
			out = new FileOutputStream(outfile);
			gzIn = new GzipCompressorInputStream(in);
			final byte[] buffer = new byte[1024];
			int n = 0;
			while (-1 != (n = gzIn.read(buffer)))
			{
				out.write(buffer, 0, n);
			}
			uncompressed = true;
		}
		catch (java.io.FileNotFoundException exp)
		{
			System.err.println("Uncompress::uncompress File not found decompressing " + theGzFile);
			throw exp;
		}
		catch (java.io.IOException exp)
		{
			System.err.println("Uncompress::uncompress IOException decompressing " + theGzFile);
			throw exp;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception ignore)
			{
				//ignore
			}
			try
			{
				gzIn.close();
			}
			catch (Exception ignore)
			{
				//ignore
			}
		}
		return(uncompressed);
	}

}