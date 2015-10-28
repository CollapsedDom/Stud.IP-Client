package de.danner_web.studip_client.utils.oauth;

/**
 * This class provides all patterns for the StudIP core RestApi. Note: it is
 * only usable if the APIUrl in the OAuthServer is ending with:
 * "dispatch.php/api"
 * 
 * @author Philipp
 *
 */
public class CoreApiPattern {
	public final static String USER = "user";
	public final static String NEWS = "news";

	public static String getNewsFromRange(String range) {
		return "news/range/" + range;
	}

	// TODO: issue #6 add all patterns
}
