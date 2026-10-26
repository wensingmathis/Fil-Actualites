package fr.fil.actualites;

import android.text.Html;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class FeedLoader {
    private static final int MAX_BYTES = 2_000_000;

    static boolean isHttpsUrl(String candidate) {
        try {
            URI uri = new URI(candidate.trim());
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) && host != null
                    && host.contains(".") && uri.getUserInfo() == null
                    && uri.getPort() != 0;
        } catch (Exception ignored) { return false; }
    }

    static boolean isArticleUrl(String url) {
        try {
            URI uri = new URI(url);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && uri.getUserInfo() == null;
        } catch (Exception ignored) { return false; }
    }

    static List<Article> load(Source source) throws IOException, XmlPullParserException {
        if (!isHttpsUrl(source.url)) throw new IOException("Flux non HTTPS");
        URL current = new URL(source.url);
        for (int redirects = 0; redirects <= 5; redirects++) {
            ensurePublicHost(current);
            HttpURLConnection connection = (HttpURLConnection) current.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(9000);
            connection.setReadTimeout(9000);
            connection.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*");
            connection.setRequestProperty("User-Agent", "Fil-Actualites/1.0 (Android; RSS reader)");
            try {
                int status = connection.getResponseCode();
                if (status >= 300 && status < 400) {
                    String location = connection.getHeaderField("Location");
                    if (location == null || redirects == 5) throw new IOException("Redirection RSS invalide");
                    URI next = current.toURI().resolve(location);
                    if (!isHttpsUrl(next.toString())) throw new IOException("Redirection non HTTPS");
                    current = next.toURL();
                    continue;
                }
                if (status < 200 || status >= 300) throw new IOException("HTTP " + status);
                try (InputStream input = connection.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) >= 0) {
                        if (bytes.size() + count > MAX_BYTES) throw new IOException("Flux trop volumineux");
                        bytes.write(buffer, 0, count);
                    }
                    return parse(source, bytes.toByteArray());
                }
            } catch (java.net.URISyntaxException e) {
                throw new IOException("Redirection RSS invalide", e);
            } finally {
                connection.disconnect();
            }
        }
        throw new IOException("Trop de redirections RSS");
    }

    private static void ensurePublicHost(URL url) throws IOException {
        if (!isHttpsUrl(url.toString())) throw new IOException("Flux non HTTPS");
        InetAddress[] addresses = InetAddress.getAllByName(url.getHost());
        for (InetAddress address : addresses) {
            byte[] bytes = address.getAddress();
            boolean uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
            boolean sharedIpv4 = bytes.length == 4 && (bytes[0] & 0xff) == 100
                    && (bytes[1] & 0xc0) == 64;
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                    || address.isMulticastAddress() || uniqueLocalIpv6 || sharedIpv4) {
                throw new IOException("Hôte RSS non public");
            }
        }
    }

    static List<Article> parse(Source source, byte[] xml) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false);
        parser.setInput(new ByteArrayInputStream(xml), null);
        List<Article> articles = new ArrayList<>();
        String title = "", description = "", link = "", date = "";
        boolean inside = false;
        int event;
        while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if ("item".equalsIgnoreCase(tag) || "entry".equalsIgnoreCase(tag)) {
                    inside = true;
                    title = description = link = date = "";
                } else if (inside) {
                    if ("link".equalsIgnoreCase(tag)) {
                        String href = parser.getAttributeValue(null, "href");
                        String rel = parser.getAttributeValue(null, "rel");
                        if (href != null && (rel == null || "alternate".equals(rel))) link = href;
                        else if (href == null) link = safeText(parser);
                    } else if ("title".equalsIgnoreCase(tag)) {
                        title = safeText(parser);
                    } else if ("description".equalsIgnoreCase(tag)
                            || "summary".equalsIgnoreCase(tag) || "encoded".equalsIgnoreCase(tag)) {
                        if (description.isEmpty()) description = safeText(parser);
                    } else if ("pubDate".equalsIgnoreCase(tag) || "published".equalsIgnoreCase(tag)
                            || "updated".equalsIgnoreCase(tag) || "date".equalsIgnoreCase(tag)) {
                        if (date.isEmpty()) date = safeText(parser);
                    }
                }
            } else if (event == XmlPullParser.END_TAG && inside) {
                String tag = parser.getName();
                if ("item".equalsIgnoreCase(tag) || "entry".equalsIgnoreCase(tag)) {
                    String cleanTitle = plain(title, 300);
                    String cleanSummary = plain(description, 450);
                    String cleanLink = link.trim();
                    if (!cleanTitle.isEmpty() && isArticleUrl(cleanLink)) {
                        articles.add(new Article(cleanTitle, cleanSummary, cleanLink,
                                source.url, source.name, parseDate(date)));
                    }
                    inside = false;
                    if (articles.size() >= 75) break;
                }
            }
        }
        return articles;
    }

    private static String safeText(XmlPullParser parser) throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    private static String plain(String html, int limit) {
        String text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
                .replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
        return text.length() <= limit ? text : text.substring(0, limit).trim() + "…";
    }

    private static long parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return 0;
        try { return ZonedDateTime.parse(raw.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli(); }
        catch (Exception ignored) { }
        try { return OffsetDateTime.parse(raw.trim()).toInstant().toEpochMilli(); }
        catch (Exception ignored) { }
        try { return ZonedDateTime.parse(raw.trim()).toInstant().toEpochMilli(); }
        catch (Exception ignored) { }
        for (String pattern : new String[]{"EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssZ"}) {
            try {
                return ZonedDateTime.parse(raw.trim(), DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)).toInstant().toEpochMilli();
            } catch (Exception ignored) { }
        }
        return 0;
    }
}
