package fr.fil.actualites;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Storage {
    private static final String PREFS = "fil_prefs_v1";
    private final SharedPreferences prefs;

    Storage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    List<Source> sources() {
        List<Source> sources = new ArrayList<>();
        sources.add(new Source("Franceinfo", "https://www.franceinfo.fr/titres.rss", false));
        sources.add(new Source("Le Monde", "https://www.lemonde.fr/rss/en_continu.xml", false));
        sources.add(new Source("Franceinfo · Monde", "https://www.franceinfo.fr/monde.rss", false));
        sources.add(new Source("Franceinfo · France", "https://www.franceinfo.fr/france.rss", false));
        sources.add(new Source("Franceinfo · Technologie", "https://www.franceinfo.fr/internet.rss", false));
        sources.add(new Source("Service-Public", "https://www.service-public.gouv.fr/abonnements/rss/actu-actualites-particuliers.rss", false));
        try {
            JSONArray custom = new JSONArray(prefs.getString("custom", "[]"));
            for (int i = 0; i < custom.length(); i++) {
                Source s = Source.fromJson(custom.getJSONObject(i));
                if (s.custom && FeedLoader.isHttpsUrl(s.url)) sources.add(s);
            }
        } catch (JSONException ignored) { }
        return sources;
    }

    void addSource(Source added) {
        List<Source> existing = sources();
        JSONArray custom = new JSONArray();
        for (Source source : existing) {
            if (source.custom && !source.url.equals(added.url)) {
                try { custom.put(source.toJson()); } catch (JSONException ignored) { }
            }
        }
        try { custom.put(added.toJson()); } catch (JSONException ignored) { }
        prefs.edit().putString("custom", custom.toString()).apply();
        setSelected(added.url, true);
    }

    void removeSource(String url) {
        JSONArray custom = new JSONArray();
        for (Source s : sources()) {
            if (s.custom && !s.url.equals(url)) {
                try { custom.put(s.toJson()); } catch (JSONException ignored) { }
            }
        }
        prefs.edit().putString("custom", custom.toString()).apply();
        setSelected(url, false);
    }

    Set<String> selected() {
        Set<String> selected = new HashSet<>();
        try {
            JSONArray list = new JSONArray(prefs.getString("selected", "[]"));
            for (int i = 0; i < list.length(); i++) selected.add(list.getString(i));
        } catch (JSONException ignored) { }
        return selected;
    }

    void setSelected(String url, boolean value) {
        Set<String> selected = selected();
        if (value) selected.add(url);
        else selected.remove(url);
        JSONArray array = new JSONArray();
        for (String entry : selected) array.put(entry);
        prefs.edit().putString("selected", array.toString()).apply();
    }

    List<Article> cached() {
        List<Article> articles = new ArrayList<>();
        try {
            JSONArray list = new JSONArray(prefs.getString("cache", "[]"));
            for (int i = 0; i < list.length(); i++) articles.add(Article.fromJson(list.getJSONObject(i)));
        } catch (JSONException ignored) { }
        return articles;
    }

    void saveCache(List<Article> articles) {
        JSONArray array = new JSONArray();
        int limit = Math.min(articles.size(), 200);
        for (int i = 0; i < limit; i++) {
            try { array.put(articles.get(i).toJson()); } catch (JSONException ignored) { }
        }
        prefs.edit().putString("cache", array.toString()).apply();
    }
}
