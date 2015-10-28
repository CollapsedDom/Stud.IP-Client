package de.danner_web.studip_client.plugins.news.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data class for the json parser
 * 
 * @author Philipp
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class News {

	public News() {
	}

	public News(String news_id, String topic, String body, Long date,
			String author, Long chdate, Long mkdate, Long expire,
			int allow_comments, String chdate_uid, String body_original) {
		this.news_id = news_id;
		this.topic = topic;
		this.body = body;
		this.date = date;
		this.author = author;
		this.chdate = chdate;
		this.mkdate = mkdate;
		this.expire = expire;
		this.allow_comments = allow_comments;
		this.chdate_uid = chdate_uid;
	}

	public String news_id;
	public String topic;
	public String body;
	public Long date;
	public String author;
	public Long chdate;
	public Long mkdate;
	public Long expire;
	public int allow_comments;
	public String chdate_uid;
}
