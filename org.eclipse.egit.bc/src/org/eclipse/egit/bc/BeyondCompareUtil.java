package org.eclipse.egit.bc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.egit.bc.preferences.BeyondCompareEgitPreferencePage;
import org.eclipse.egit.core.internal.CompareCoreUtils;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.team.core.history.IFileRevision;

/**
 *
 * @author AXJRD
 *
 */
public class BeyondCompareUtil {
	private static File BC_TEMP_DIR = null;

	/**
	 *
	 * @param leftFile
	 * @param rightFile
	 */
	public static void execBeyondCompare(String leftFile, String rightFile) {
		String beyondCompareCommand = BeyondCompareEgitPreferencePage.getBeyondCompareExecutablePath();
		if ( beyondCompareCommand == null )
			return;
		
		exec(beyondCompareCommand, leftFile, rightFile, "/rightreadonly"); //$NON-NLS-1$
	}

	/**
	 * @param cmds
	 * @throws RuntimeException
	 */
	public static void exec(String... cmds) throws RuntimeException {
		if (cmds == null) {
			throw new NullPointerException("Commands can't be null"); //$NON-NLS-1$
		}

		try {
			Runtime.getRuntime().exec(cmds);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param content
	 * @param name
	 * @param version
	 * @param encoding
	 * @param tempDir
	 * @param addVersionSuffix
	 * @param monitor
	 * @return -
	 * @throws IOException
	 * @throws CoreException
	 */
	public static String storeVersionTempFile(InputStream content, String name, RevCommit version, String encoding, File tempDir, boolean addVersionSuffix, IProgressMonitor monitor) throws IOException ,CoreException {
		String taskName = "Retrieving Git revision for " + name + "..."; //$NON-NLS-1$ //$NON-NLS-2$
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(taskName, 100);

		String ext = ""; //$NON-NLS-1$
		int dot = name.lastIndexOf("."); //$NON-NLS-1$
		if (dot != -1) {
			ext = name.substring(dot);
			name = name.substring(0, dot);
		}

		if (addVersionSuffix) {
			name = name + "_" + version.getId().getName(); //$NON-NLS-1$
		}

		if (tempDir == null) {
			tempDir = getBCTempDirectory();
		}
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
		File tempFile = new File(tempDir, name + ext);
		//System.out.println("remote SVN file v" + version.getVersion() + ": " + tempFile.getAbsolutePath());
		monitor.worked(10);
		// write out the file if it doesn't exist or if the revision is newer than the cached file
		Date versionDate = new Date();
		if (readAndWriteStringByDate(content, encoding, tempFile, versionDate, new SubProgressMonitor(monitor, 90))) {
			// successfully saved
		} else {
			// remote file revision date is older than the cached file's last modified date
			monitor.worked(90);
		}
		monitor.done();
		return tempFile.getAbsolutePath();
	}

	/**
	 * @return -
	 * @throws IOException
	 */
	public static final File getBCTempDirectory() throws IOException {
		if (BC_TEMP_DIR == null) {
			// use the workspace plugin state location
			String loc = Activator.getDefault().getStateLocation().toOSString();
			BC_TEMP_DIR = new File(loc);
			BC_TEMP_DIR.mkdir();
		}
		return BC_TEMP_DIR;
	}

	/**
	 * @param is
	 * @param encoding
	 * @param outputFile
	 * @param date
	 * @param monitor
	 * @return -
	 * @throws IOException
	 */
	public static boolean readAndWriteStringByDate(InputStream is, String encoding, File outputFile, Date date,
			IProgressMonitor monitor) throws IOException {
		boolean wrote = false;
		if (!outputFile.exists()) {
			readAndWriteString(is, encoding, outputFile, monitor);
			wrote = true;
		} else {
			// check if the file revision date is newer than the cached file's
			// last modified date
			Date fileDate = new Date(outputFile.lastModified());
			int compare = fileDate.compareTo(date);
			if (compare < 0) {
				// File's last modified date is older than the given date, so
				// overwrite
				readAndWriteString(is, encoding, outputFile, monitor, true /* overwrite */);
//				System.out.println("Remote file is newer than the cached version, updating."); //$NON-NLS-1$
				wrote = true;
			} else {
				// remote file revision date is older than the cached file's
				// last modified date
//				System.out.println("Remote file already exists."); //$NON-NLS-1$
			}
		}
		return wrote;
	}

	/**
	 * Reads in from the input stream and writes it out to the given file at the
	 * same time. Does NOT overwrite an existing file.
	 *
	 * @param is
	 *            The input stream to read in from
	 * @param encoding
	 *            The encoding for the input stream
	 * @param outputFile
	 *            The output file to write to
	 * @param monitor
	 *            The progress monitor
	 * @throws IOException
	 */
	public static void readAndWriteString(InputStream is, String encoding,
			File outputFile, IProgressMonitor monitor) throws IOException {
		readAndWriteString(is, encoding, outputFile, monitor, false);
	}

	/**
	 * Reads in from the input stream and writes it out to the given file at the
	 * same time.
	 *
	 * @param is
	 *            The input stream to read in from
	 * @param encoding
	 *            The encoding for the input stream
	 * @param outputFile
	 *            The output file to write to
	 * @param monitor
	 *            The progress monitor
	 * @param overwrite
	 *            If true an the outputFile exists then it is overwritten
	 * @throws IOException
	 */
	public static void readAndWriteString(InputStream is, String encoding,
			File outputFile, IProgressMonitor monitor, boolean overwrite)
			throws IOException {
		if (is == null) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			if (!outputFile.exists() || overwrite) {
				monitor.beginTask(
						"Retrieving data from input stream and writing it out to a file...", //$NON-NLS-1$
						is.available() * 2);
				char[] part = new char[2048];
				int read = 0;
				writer = new BufferedWriter(new FileWriter(outputFile, false));
				reader = new BufferedReader(new InputStreamReader(is, encoding));
				while ((read = reader.read(part)) != -1) {
					monitor.worked(read);
					writer.write(part, 0, read);
					monitor.worked(read);
				}
			} else {
//				System.out.println("Output file already exists (" + outputFile.getCanonicalPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} finally {
			monitor.done();
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	/**
	 *
	 * @param repoRelativeBasePath
	 * @param commit
	 * @param localRepo
	 * @return -
	 */
	public static String getCompareFilePath(String repoRelativeBasePath, RevCommit commit, Repository localRepo) {
		try {
			IFileRevision fileRev = CompareUtils.getFileRevision(repoRelativeBasePath, commit, localRepo, null);
			String encoding = CompareCoreUtils.getResourceEncoding(localRepo,repoRelativeBasePath);

			IStorage fileStorage = fileRev.getStorage(null);
			InputStream content = fileStorage.getContents();
			String fileName = fileRev.getName();
			File tmpDir = BeyondCompareUtil.getBCTempDirectory();
			String tmpFilePath = BeyondCompareUtil.storeVersionTempFile(
					content, fileName, commit, encoding, tmpDir, true, null);
			return tmpFilePath;
		} catch (Exception e) {
			Activator.logError("Error while getting file revision to compare", e);
			return null;
		}
	}

}
