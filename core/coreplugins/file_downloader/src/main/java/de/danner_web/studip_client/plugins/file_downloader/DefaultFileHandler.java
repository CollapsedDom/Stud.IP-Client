package de.danner_web.studip_client.plugins.file_downloader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.Leaf;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.SemesterNode;
import de.danner_web.studip_client.plugins.file_downloader.treeModel.TreeRootNode;
import de.danner_web.studip_client.utils.JSONParserUtil;
import de.danner_web.studip_client.utils.OSValidationUtil;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

/**
 * This Class stores the tree as a serialized Java Object.
 * 
 * @author Dominik Danner
 * 
 */
public class DefaultFileHandler extends Observable {

	/**
	 * Logger dieser Klasse.
	 */
	private static Logger logger = LogManager.getLogger(DefaultFileHandler.class);

	private static final String FILE_NAME = ".database.xml";
	private static final String TREE_PATTERN = "studip-client-core/documenttree/";
	private static final String SEMESTER_PATTERN = "studip-client-core/semesterlist/";

	// private static final String GROUP_PATTERN = "studip-client-core/groups/";

	/**
	 * Returns the Api route to get meta data only for one semester by id.
	 * 
	 * @param id
	 * @return
	 */
	private static final String getSemesterPattern(String id) {
		return "studip-client-core/documenttree/" + id;
	}

	// Persistent saving
	private File database;

	private FileDownloader loader;

	private TreeRootNode tree = new TreeRootNode();

	// Constructor variables
	private Context context;
	private OAuthConnector con;
	private PluginSettings settings;

	/**
	 * Constructor for the DefaultFileHandler
	 */
	public DefaultFileHandler(Context context, OAuthConnector con, PluginSettings settings) {
		this.context = context;
		this.settings = settings;
		// this.rootPath = settings.getSettings().get(ROOT_PATH);
		this.loader = new FileDownloader(context, con, settings);
		this.con = con;

		File databaseFolder = new File(settings.get(FileDownloadPlugin.SYNC_FOLDER));
		databaseFolder.mkdirs();
		this.database = new File(settings.get(FileDownloadPlugin.SYNC_FOLDER) + File.separator + FILE_NAME);
		loadData();
	}

	public TreeRootNode getTree() {
		return tree;
	}

	public String getDir() {
		return settings.get(FileDownloadPlugin.SYNC_FOLDER);
	}

	/**
	 * Changes the destination of the Files to be download.
	 * 
	 * @param path
	 *            where the new folder should be
	 * @return
	 */
	public boolean changeDestination(String path) {
		// TODO test
		if (path != null) {
			File oldDestination = new File(settings.get(FileDownloadPlugin.SYNC_FOLDER));
			File newDestination = new File(path);

			newDestination.mkdirs();
			if (newDestination.isDirectory() && newDestination.listFiles().length == 0 && newDestination.canWrite()) {
				try {
					// Copy Files to new folder
					Files.copy(oldDestination.toPath(), newDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
					
					// delete old folder
					Files.walkFileTree(oldDestination.toPath(), new SimpleFileVisitor<Path>() {
						   @Override
						   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							   Files.delete(file);
							   return FileVisitResult.CONTINUE;
						   }

						   @Override
						   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							   Files.delete(dir);
							   return FileVisitResult.CONTINUE;
						   }

					   });
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

				settings.put(FileDownloadPlugin.SYNC_FOLDER, path);
				this.database = new File(settings.get(FileDownloadPlugin.SYNC_FOLDER) + File.separator + FILE_NAME);
				tree.updateFileSystem(path);

				setChanged();
				notifyObservers(this);
			}
		}
		return false;
	}

	/**
	 * This Method saves the current Metadata Tree to the disk.
	 * 
	 * @return true if the tree is saved, otherwise false.
	 */
	public synchronized boolean saveData() {
		logger.entry();
		setFileVisible(database);
		if (!database.exists()) {
			try {
				database.createNewFile();
			} catch (IOException e) {
				return logger.exit(false);
			}
		}

		try {
			JAXBContext jc = JAXBContext.newInstance(TreeRootNode.class);
			// Save tree
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(tree, database);
		} catch (JAXBException e) {
			logger.warn("Could not save Metatree to xml File.");
			return logger.exit(false);
		}
		setFileHidden(database);
		return logger.exit(true);
	}

	/**
	 * This method loads the existing database to the protected variable tree.
	 * 
	 * Reads Data from a serialized Java object from the plugin Folder
	 * 
	 * @return true if success, otherwise false.
	 */
	protected boolean loadData() {
		logger.entry();
		setFileVisible(database);
		if (!database.exists()) {
			try {
				// If database does not exist -> save one default
				// SemesterTreeRoot()
				database.createNewFile();
				saveData();
			} catch (IOException e) {
				return logger.exit(false);
			}
		}

		try {
			JAXBContext jc = JAXBContext.newInstance(TreeRootNode.class);
			// Load tree from xml file
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			tree = (TreeRootNode) unmarshaller.unmarshal(database);
			tree.updateFather(null);
		} catch (JAXBException e) {
			logger.warn("Could not load Metatree -> generated a new one.");
			tree = new TreeRootNode();
		}
		setFileHidden(database);
		return logger.exit(true);
	}

	/**
	 * This method updates the local tree with the new entries form the server.
	 * 
	 * It download the semester List and the Group List (TODO later)
	 * 
	 * @throws UpdateFailureException
	 *             if an error occures.
	 */
	public void updateDatabaseTree() throws UpdateFailureException {

		String pattern = TREE_PATTERN;
		if (tree.semesters.isEmpty()) {
			// new installation -> only load semester meta data
			pattern = SEMESTER_PATTERN;
		}

		HttpURLConnection response = null;
		try {
			response = con.get(pattern);
		} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			throw new UpdateFailureException("RootNode");
		}

		List<SemesterNode> newSemesterList = JSONParserUtil.parse(response, SemesterNode.class);
		if (newSemesterList == null) {
			throw new UpdateFailureException("Could not parse SemesterList");
		}

		this.tree.updateSemesters(newSemesterList);

		// TODO test api route
		// try {
		// response = con.get(TREE_PATTERN);
		// } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
		// | OAuthExpectationFailedException | OAuthCommunicationException e) {
		// throw new UpdateFailureException("RootNode");
		// }
		//
		// List<GroupNode> newGroupList = JSONParserUtil.parse(response,
		// GroupNode.class);
		// if (newGroupList == null) {
		// throw new UpdateFailureException("Could not parse GroupList");
		// }
		//
		// this.tree.updateGroups(newGroupList);

		setChanged();
		notifyObservers(tree);
	}

	/**
	 * This method updates the local tree with the new entries form the server.
	 * 
	 * It download the semester List and the Group List (TODO later)
	 * 
	 * @throws UpdateFailureException
	 *             if an error occures.
	 */
	public void updateDatabaseSemesters() throws UpdateFailureException {

		// 1. Update semester list
		HttpURLConnection response = null;
		try {
			response = con.get(SEMESTER_PATTERN);
		} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			throw new UpdateFailureException("RootNode");
		}

		List<SemesterNode> newSemesterList = JSONParserUtil.parse(response, SemesterNode.class);
		if (newSemesterList == null) {
			throw new UpdateFailureException("Could not parse SemesterList");
		}
		this.tree.updateSemesters(newSemesterList);

		// 2. Update active semesters
		List<SemesterNode> activeSemesters = new LinkedList<SemesterNode>();
		for (SemesterNode se : tree.semesters) {

			if (se.isDownloadActive()) {
				response = null;
				try {
					response = con.get(getSemesterPattern(se.semester_id));
				} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
						| OAuthCommunicationException e) {
					throw new UpdateFailureException("RootNode");
				}

				List<SemesterNode> temp = JSONParserUtil.parse(response, SemesterNode.class);
				if (temp == null) {
					throw new UpdateFailureException("Could not parse SemesterList");
				}
				activeSemesters.addAll(temp);
			}
		}
		this.tree.updateSemesters(activeSemesters);

		// TODO test api route
		// try {
		// response = con.get(TREE_PATTERN);
		// } catch (OAuthNotAuthorizedException | OAuthMessageSignerException
		// | OAuthExpectationFailedException | OAuthCommunicationException e) {
		// throw new UpdateFailureException("RootNode");
		// }
		//
		// List<GroupNode> newGroupList = JSONParserUtil.parse(response,
		// GroupNode.class);
		// if (newGroupList == null) {
		// throw new UpdateFailureException("Could not parse GroupList");
		// }
		//
		// this.tree.updateGroups(newGroupList);

		setChanged();
		notifyObservers(tree);
	}

	/**
	 * 
	 * This method updates the local tree with the new entries form the given
	 * tree. The tree structure should be saved with every change. After
	 * updating the database the FileHanlder should download new files due to
	 * his configuration. The fileHandler knows where to save the new files and
	 * which files (from the tree) should be download.
	 * 
	 * Updates the Java Object and write it back. Then downloads new Files and
	 * stores it on disk.
	 * 
	 * @return number of files changed, if -1 something went wrong
	 * @throws UpdateFailureException
	 */
	public synchronized int updateTree() throws UpdateFailureException {
		logger.entry();
		long milis = System.currentTimeMillis();

		// merge trees
		updateDatabaseSemesters();

		context.appendHistory(
				"Updated Meta Information from Server in " + (System.currentTimeMillis() - milis) + " ms.");

		/*
		 * Create new Folders / collect new Files (Metadata) / hand over to
		 * Downloader
		 * 
		 * Also check for missing files and requeue
		 */
		boolean success = this.tree.updateFileSystem(settings.get(FileDownloadPlugin.SYNC_FOLDER));
		if(!success){
			logger.warn("Error in updateFileSystem.");
		}
		List<Leaf> list = this.tree.collectDocuments();

		if (!saveData()) {
			return -1;
		}

		if (!list.isEmpty()) {
			setChanged();
			notifyObservers(tree);

			// hand over files to Downloader and start it
			loader.addAll(list);
			loader.start();
		}

		return logger.exit(list.size());
	}

	/**
	 * Returns the FileDownloader.
	 */
	public FileDownloader getFileDownloader() {
		return loader;
	}

	private static void setFileHidden(File file) {
		if (file != null && file.exists() && OSValidationUtil.isWindows()) {
			try {
				Files.setAttribute(file.toPath(), "dos:hidden", true);
			} catch (IOException e) {
				logger.warn("Error in hidding the file " + file.getAbsolutePath() + " on Windows.");
			}
		}
	}
	
	private static void setFileVisible(File file) {
		if (file != null && file.exists() && OSValidationUtil.isWindows()) {
			try {
				Files.setAttribute(file.toPath(), "dos:hidden", false);
			} catch (IOException e) {
				logger.warn("Error in hidding the file " + file.getAbsolutePath() + " on Windows.");
			}
		}
	}
}