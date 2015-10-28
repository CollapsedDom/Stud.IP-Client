package de.danner_web.studip_client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONParserUtil {

	private static Logger logger = LogManager.getLogger(JSONParserUtil.class);

	/**
	 * This method tries to parse the given json String into the given class
	 * clazz.
	 * 
	 * @param <T>
	 * 
	 * @param json
	 *            given json string to parse
	 * @param clazz
	 *            classtype of the return value
	 * @return Object of type clazz or null, for failure case
	 */
	public static <T> List<T> parse(String json, Class<T> clazz) {

		List<T> obj = null; 
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			obj = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(
					List.class, clazz));
			
		} catch (Exception e) {
			logger.debug("Error while parsing json to given object class "
					+ clazz);
			logger.debug(e);
		}

		return obj;
	}

	/**
	 * This method tries to parse the json post content of the given
	 * HttpUrlConnection connection into the given class clazz.
	 * 
	 * @param connection
	 *            HttpUrlConnection with json response from server
	 * @param clazz
	 *            classtype of the return value
	 * @return Object of type clazz or null, for failure case
	 */
	public static <T> List<T> parse(HttpURLConnection connection, Class<T> clazz) {

		if (connection == null) {
			throw new IllegalArgumentException("response must not be null");
		}

		// Extract JSON as String
		String json = "";

		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				sb.append(line + '\n');
			}
			rd.close();
			json = sb.toString();
		} catch (IOException e) {
			logger.debug("Error while reading json response from connection");
		}
		return parse(json, clazz);
	}
}
