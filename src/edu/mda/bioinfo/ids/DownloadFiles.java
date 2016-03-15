/*
TcgaIdConverter Copyright 2014, 2015, 2016 University of Texas MD Anderson Cancer Center

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.mda.bioinfo.ids;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author tdcasasent
 */
public class DownloadFiles
{

	public String mDownloadDir = null;

	public DownloadFiles(String theDownloadDir)
	{
		mDownloadDir = theDownloadDir;
	}

	public void downloadFiles() throws IOException, Exception
	{
		System.out.println("DownloadFiles start");
		{
			System.out.println("DownloadFiles mirbase.org:/pub/mirbase/CURRENT/aliases.txt.gz");
			// mimat TO mirbase
			String ftpServer = "mirbase.org";
			String ftpFile = "/pub/mirbase/CURRENT/aliases.txt.gz";
			String localFile = new File(mDownloadDir, "aliases.txt.gz").getAbsolutePath();
			downloadFromFtpToFile(ftpServer, ftpFile, localFile);
		}
		{
			System.out.println("DownloadFiles mirbase.org:/pub/mirbase/CURRENT/mature.fa.gz");
			// mature mirs
			String ftpServer = "mirbase.org";
			String ftpFile = "/pub/mirbase/CURRENT/mature.fa.gz";
			String localFile = new File(mDownloadDir, "mature.fa.gz").getAbsolutePath();
			downloadFromFtpToFile(ftpServer, ftpFile, localFile);
		}
		{
			System.out.println("DownloadFiles ftp.ebi.ac.uk:/pub/databases/genenames/hgnc_complete_set.txt.gz");
			// mirbase to gene symbol
			// Entrez num to gene symbol
			String ftpServer = "ftp.ebi.ac.uk";
			String ftpFile = "/pub/databases/genenames/hgnc_complete_set.txt.gz";
			String localFile = new File(mDownloadDir, "hgnc_complete_set.txt.gz").getAbsolutePath();
			downloadFromFtpToFile(ftpServer, ftpFile, localFile);
		}
		{
			System.out.println("DownloadFiles http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/kgXref.txt.gz");
			// UC ID to gene symbol
			String getUrl = "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/kgXref.txt.gz";
			String localFile = new File(mDownloadDir, "kgXref.txt.gz").getAbsolutePath();
			downloadFromHttpToFile(getUrl, null, localFile);
		}
		System.out.println("DownloadFiles done");
	}

	private void downloadFromHttpToFile(String theURL, String theParameters, String theFile) throws IOException
	{
		// both in milliseconds
		//int connectionTimeout = 1000 * 60 * 3;
		//int readTimeout = 1000 * 60 * 3;
		try
		{
			URL myURL = new URL(theURL);
			HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain");
			//connection.setRequestProperty("charset", "utf-8");
			if (null != theParameters)
			{
				System.out.println("Content-Length " + Integer.toString(theParameters.getBytes().length));
				connection.setRequestProperty("Content-Length", "" + Integer.toString(theParameters.getBytes().length));
			}
			connection.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream()))
			{
				if (null != theParameters)
				{
					wr.write(theParameters.getBytes());
				}
				wr.flush();
				File myFile = new File(theFile);
				System.out.println("Requesting " + myURL);
				FileUtils.copyInputStreamToFile(connection.getInputStream(), myFile);
				System.out.println("Downloaded " + myURL);
			}
			catch (IOException rethrownExp)
			{
				InputStream errorStr = connection.getErrorStream();
				if (null != errorStr)
				{
					System.err.println("Error stream returned: " + IOUtils.toString(errorStr));
				}
				else
				{
					System.err.println("No error stream returned");
				}
				throw rethrownExp;
			}
		}
		catch (IOException rethrownExp)
		{
			System.err.println("exception " + rethrownExp.getMessage() + " thrown while downloading " + theURL + " to " + theFile);
			throw rethrownExp;
		}
	}

	private void downloadFromFtpToFile(String theServer, String theServerFile, String theLocalFile) throws IOException, Exception
	{
		FTPClient ftp = new FTPClient();
		try
		{
			int reply = 0;
			boolean replyB = false;
			ftp.connect(theServer);
			System.out.print(ftp.getReplyString());
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
			}
			else
			{
				ftp.login("anonymous", "anonymous");
				replyB = ftp.setFileType(FTP.BINARY_FILE_TYPE);
				System.out.print(ftp.getReplyString());
				System.out.println("replyB= " + replyB);
				if (false==replyB)
				{
					throw new Exception("Unable to login to " + theServer);
				}
				OutputStream output = new FileOutputStream(theLocalFile);
				replyB = ftp.retrieveFile(theServerFile, output);
				System.out.print(ftp.getReplyString());
				System.out.println("replyB= " + replyB);
				if (false==replyB)
				{
					throw new Exception("Unable to retrieve " + theServerFile);
				}
			}
			ftp.logout();
		}
		catch (IOException rethrownExp)
		{
			System.err.println("exception " + rethrownExp.getMessage());
			throw rethrownExp;
		}
		finally
		{
			if (ftp.isConnected())
			{
				try
				{
					ftp.disconnect();
				}
				catch (IOException ignore)
				{
					// do nothing
				}
			}
		}
	}
}