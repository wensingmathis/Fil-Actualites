package fr.fil.actualites;

import org.json.JSONException;
import org.json.JSONObject;

final class Article {
    final String title;
    final String summary;
    final String link;
    final String sourceUrl;
    final String sourceName;
    final long published;

    Article(String title, String summary, String link, String sourceUrl, String sourceName, long published) {
        this.title = title;
        this.summary = summary;
        this.link = link;
        this.sourceUrl = sourceUrl;
        this.sourceName = sourceName;
        this.published = published;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("summary", summary);
        object.put("link", link);
        object.put("sourceUrl", sourceUrl);
        object.put("sourceName", sourceName);
        object.put("published", published);
        return object;
    }

    static Article fromJson(JSONObject json) throws JSONException {
        return new Article(json.getString("title"), json.optString("summary"),
                json.getString("link"), json.getString("sourceUrl"),
                json.getString("sourceName"), json.optLong("published", 0));
    }
}
