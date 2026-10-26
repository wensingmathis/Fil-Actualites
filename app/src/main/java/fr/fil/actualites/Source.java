package fr.fil.actualites;

import org.json.JSONException;
import org.json.JSONObject;

final class Source {
    final String name;
    final String url;
    final boolean custom;

    Source(String name, String url, boolean custom) {
        this.name = name;
        this.url = url;
        this.custom = custom;
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("url", url);
        object.put("custom", custom);
        return object;
    }

    static Source fromJson(JSONObject json) throws JSONException {
        return new Source(json.getString("name"), json.getString("url"), json.optBoolean("custom"));
    }
}
