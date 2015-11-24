package de.danner_web.studip_client.plugins.file_downloader;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import de.danner_web.studip_client.data.AcceptPluginMessage;
import de.danner_web.studip_client.data.ClickListener;
import de.danner_web.studip_client.data.PluginMessage;
import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.data.TextPluginMessage;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.DocumentLeaf;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.Leaf;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

/**
 * This Class manages the download of Files.
 * 
 * The files that must be download are added to the toDownload list. A Thread
 * then can be started to begin to download this items. With the method stop()
 * and reset() the download can be halted and the List be reseted.
 * 
 * @author Dominik Danner
 * 
 */
public class FileDownloader {

	private static Logger logger = LogManager.getLogger(FileDownloader.class);

	/**
	 * List to store upcoming downloads And lock that can be interrupted but
	 * protects this List.
	 */
	private Queue<DocumentLeaf> toDownload = new LinkedList<DocumentLeaf>();
	private final Lock lock = new ReentrantLock();

	private PluginSettings settings;

	/**
	 * Returns the download Pattern of the Core API to download a file by id
	 * 
	 * @param id
	 *            of the document to download
	 * @return Pattern
	 */
	private static String downloadPattern(String id) {
		return "file/" + id + "/content";
	}

	/**
	 * Returns the put Pattern to set "download" for the file with id "id"
	 * 
	 * @param id
	 *            of document
	 * @return url pattern
	 */
	private static String setFileDownloaded(String id) {
		return "studip-client-core/visited_document/" + id;
	}

	/**
	 * The thread approaching the download.
	 */
	private DownloadThread thread;

	/**
	 * Reference to the OAuthConnector.
	 */
	private OAuthConnector con;

	private Context context;

	/**
	 * Constructor for the Class.
	 */
	public FileDownloader(Context context, OAuthConnector con, PluginSettings settings) {
		logger.entry(context, con, settings);
		this.context = context;
		this.con = con;
		this.settings = settings;
		this.thread = new DownloadThread();

		logger.exit();
	}

	/**
	 * Adds the new List at the end of the toDownload list and starts the
	 * Download thread.
	 * 
	 * @param list
	 *            new elements
	 * @throws InterruptedException
	 */
	public void addAllAndStart(List<Leaf> list) {
		logger.entry(list);
		StringBuilder sb = new StringBuilder();
		final List<Leaf> toBeAllowedToDownload = new LinkedList<Leaf>();

		/**
		 * Add new Items synchronized on the List, no Problems with transparent
		 * Downloadthread.
		 */
		try {
			lock.lockInterruptibly();
			try {
				for (Leaf leaf : list) {
					if (!this.toDownload.contains(leaf)) {
						if (leaf instanceof DocumentLeaf) {
							DocumentLeaf dleaf = (DocumentLeaf) leaf;
							/*
							 * If file is not protected, add it to the download
							 * queue, otherwise generate a user input message to
							 * allow download.
							 */
							if (dleaf.isProtected() && !dleaf.isDownloadAllowed()) {
								sb.append(leaf.getName() + ", " + leaf.getAbsolutePath() + "\n");
								toBeAllowedToDownload.add(leaf);
							} else {
								this.toDownload.add((DocumentLeaf) leaf);
							}
						} else {
							// cast to possible other types
						}
					}
				}
			} finally {
				lock.unlock();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Show notification for protected files
		if (toBeAllowedToDownload.size() != 0) {
			ClickListener protectedFileListener = new ClickListener() {
				@Override
				public void onClick(PluginMessage message) {
					// Allow download for all files in List
					for (Leaf l : toBeAllowedToDownload) {
						if (l instanceof DocumentLeaf) {
							((DocumentLeaf) l).setDownloadAllowed();
						}
					}
					FileDownloader.this.addAllAndStart(toBeAllowedToDownload);
				}
			};
			context.appendPopup(new AcceptPluginMessage("Eine Datei ist urheberrechlich geschützt.",
					"Folgende Dateien dürfen nur im Rahmen der Veranstaltungen verwendet werden,"
							+ " jede weitere Verbreitung ist unzulässig! \n" + sb.toString(),
					null, protectedFileListener));
			logger.exit();
		}

		this.start();
	}

	/**
	 * Starts the download thread, if it is not already running
	 */
	public boolean start() {
		logger.entry();
		// thread is not null, because it is created by initialization
		if (!thread.running && !this.toDownload.isEmpty()) {
			thread = new DownloadThread();
			thread.start();
		}

		return logger.exit(true);
	}

	/**
	 * Stops Downloads.
	 */
	public boolean stop() {
		logger.entry();
		// thread is not null.
		thread.running = false;
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			thread.interrupt();
		}
		return logger.exit(true);
	}

	/**
	 * This is the download thread.
	 * 
	 * @author Dominik Danner
	 * 
	 */
	class DownloadThread extends Thread {

		/**
		 * Indicator if this thread is still running.
		 */
		private boolean running = false;

		/**
		 * Constructor for the Thread
		 */
		public DownloadThread() {
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				try {
					lock.lockInterruptibly();
					try {
						if (!toDownload.isEmpty()) {
							// remove and get first element in queue
							final DocumentLeaf activeDownlaod = toDownload.poll();
							if (activeDownlaod.isDownloadActive()) {
								if (activeDownlaod.isProtected() && !activeDownlaod.isDownloadAllowed()) {
									// ignore file. Wait till next time will get
									// queued and confirmed.
								} else {
									boolean success = downloadFile(activeDownlaod);
									if (success) {
										// only remove file from download list
										// if
										// successfully download

										HttpURLConnection connection = null;
										try {
											connection = con.put(setFileDownloaded(activeDownlaod.document_id));
										} catch (OAuthNotAuthorizedException | OAuthMessageSignerException
												| OAuthExpectationFailedException | OAuthCommunicationException e) {
											e.printStackTrace();
										}
										try {
											if (connection.getResponseCode() != 200) {
												// ignore this case, user must
												// click
												// on red icon in studIP himself
											}
										} catch (IOException e) {
											e.printStackTrace();
										}
										context.appendHistory(
												"Dokument " + activeDownlaod.filename + " erfolgreich nach "
														+ activeDownlaod.getAbsolutePath() + " heruntergeladen.");
									} else {
										context.appendHistory(
												"Fehler beim Download der Datei " + activeDownlaod.filename);
									}
								}
							}
						} else {
							ClickListener openWindow = new ClickListener() {

								@Override
								public void onClick(PluginMessage message) {
									File file = new File(settings.get(FileDownloadPlugin.SYNC_FOLDER));
									Desktop desktop = Desktop.getDesktop();
									try {
										desktop.open(file);
									} catch (IOException e) {
									}
								}
							};
							context.appendPopup(new TextPluginMessage("Alle Dokumente erfolgreich heruntergeladen", "",
									openWindow));
							running = false;
						}
					} finally {
						lock.unlock();
					}
				} catch (InterruptedException e1) {
					running = false;
				}
			}
		}

		/**
		 * Downloads the given File to the target location.
		 * 
		 * @param fileLeaf
		 *            given file to download
		 */
		private boolean downloadFile(DocumentLeaf fileLeaf) {
			logger.entry();
			HttpURLConnection response = null;
			/* Get Document */
			try {
				response = con.get(downloadPattern(fileLeaf.document_id));
			} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
					| OAuthCommunicationException e) {
				// Connection not available
				return logger.exit(false);
			}

			/* Copy File to destiny */
			// Get Lock on FileLeaf (No data restructuring is possible)
			synchronized (fileLeaf) {
				File target;
				String filePathAndName = fileLeaf.getPath() + File.separator + fileLeaf.getName();
				target = new File(filePathAndName);

				// Delete file if already there
				if (target.canWrite()) {
					target.delete();
				}

				// Copy File
				try {
					Files.copy(response.getInputStream(), target.toPath());
				} catch (IOException e) {
					// Could not copy file
					return logger.exit(false);
				}
			}

			return logger.exit(true);

		}
	}

}
