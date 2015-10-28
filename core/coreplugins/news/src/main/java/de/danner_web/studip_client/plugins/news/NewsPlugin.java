package de.danner_web.studip_client.plugins.news;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import de.danner_web.studip_client.data.ClickListener;
import de.danner_web.studip_client.data.PluginMessage;
import de.danner_web.studip_client.data.PluginSettings;
import de.danner_web.studip_client.data.TextPluginMessage;
import de.danner_web.studip_client.model.Context;
import de.danner_web.studip_client.plugin.Plugin;
import de.danner_web.studip_client.plugins.news.data.News;
import de.danner_web.studip_client.plugins.news.view.SettingsView;
import de.danner_web.studip_client.utils.JSONParserUtil;
import de.danner_web.studip_client.utils.oauth.OAuthConnector;

/**
 * Plugin class for plugin News
 * 
 * @author Philipp
 *
 */
public class NewsPlugin extends Plugin {

	private PluginSettings settings;

	public NewsPlugin(Context context, OAuthConnector con, PluginSettings settings) {
		super(context, con, settings);

		// If not done yet: init settings for this plugin
		if (settings.isEmpty()) {
			settings.put(SettingsView.NEWS_STUDIP, "true");
			settings.put(SettingsView.NEWS_INSTITUTE, "false");
			settings.put(SettingsView.NEWS_COURSES, "true");
		}
		this.settings = settings;
	}

	@Override
	public int doWork() {

		// Load settings
		boolean studip = new Boolean(settings.get(SettingsView.NEWS_STUDIP));
		boolean institute = new Boolean(settings.get(SettingsView.NEWS_INSTITUTE));
		boolean courses = new Boolean(settings.get(SettingsView.NEWS_COURSES));
		if (studip) {
			downloadAndShowNews(NewsRange.studip);
		}
		if (institute) {
			downloadAndShowNews(NewsRange.institute);
		}
		if (courses) {
			downloadAndShowNews(NewsRange.courses);
		}
		return 0;
	}

	@Override
	public JPanel buildsettingsGUI() {
		return new SettingsView(settings);
	}

	@Override
	public boolean onActivate() {
		return true;
	}

	@Override
	public boolean onDeactivate() {
		return true;
	}

	@Override
	public boolean onPause() {
		return true;
	}

	@Override
	public boolean onResume() {
		return true;
	}

	/**
	 * Helper method to download and show all news within the given range.
	 * 
	 * @param range
	 * @return 0 if success, otherwise error code
	 */
	private int downloadAndShowNews(NewsRange range) {
		HttpURLConnection connection = null;
		try {
			connection = con.get(getUnreadNewsFromRange(range));
		} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			e.printStackTrace();
		}
		if (connection == null) {
			return 500;
		}

		List<News> newsList = JSONParserUtil.parse(connection, News.class);
		if (newsList != null) {
			for (News news : newsList) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				Date dt = new Date(Long.valueOf(news.date) * 1000);
				String time = sdf.format(dt);
				context.appendPopup(new TextPluginMessage(news.topic + " (" + news.author + " " + time + ")",
						news.body, new NewsListener(news)));
			}
		}
		return 0;
	}

	/**
	 * possible ranges of news
	 */
	private enum NewsRange {
		studip, institute, courses
	}

	/**
	 * This method return the pattern to get all unread news with the given
	 * range
	 * 
	 * @param range
	 *            to search for
	 * @return url pattern
	 */
	private static String getUnreadNewsFromRange(NewsRange range) {
		return "studip-client-core/news/" + range.toString();
	}

	/**
	 * This method return the pattern to set the news with the given id to read.
	 * 
	 * @param id
	 *            of the news
	 * @return url pattern
	 */
	private static String setNewsReaded(String id) {
		return "studip-client-core/visited_news/" + id;
	}

	/**
	 * This class implements a MessagListener by extending its functionality to
	 * hold a news object, which can be set to read on server side by sending a
	 * sepcific put request.
	 * 
	 * @author Philipp
	 *
	 */
	class NewsListener implements ClickListener {

		private News news;

		public NewsListener(News news) {
			this.news = news;
		}

		@Override
		public void onClick(PluginMessage message) {
			message.setShowDetailView(true);
			
			SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

				@Override
				protected String doInBackground() throws Exception {
					HttpURLConnection connection = null;
					try {
						connection = con.put(setNewsReaded(news.news_id));
					} catch (OAuthNotAuthorizedException | OAuthMessageSignerException | OAuthExpectationFailedException
							| OAuthCommunicationException e) {
						e.printStackTrace();
					}
					try {
						if (connection.getResponseCode() != 200) {
							// TODO error handling
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return "done";
				}
			};
			worker.execute();
		}

	}
}
