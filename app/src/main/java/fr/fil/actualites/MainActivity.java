package fr.fil.actualites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class MainActivity extends Activity {
    private int WHITE;
    private int INK;
    private int GRAY;
    private int LIGHT;
    private static final int GUTTER = 24;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private Storage storage;
    private Typeface regular;
    private Typeface bold;
    private boolean showingSources;
    private boolean darkTheme;
    private boolean destroyed;
    private int generation;
    private LinearLayout screen;
    private ListView articleList;
    private TextView emptyLabel;
    private ProgressBar progress;
    private final List<Article> displayedArticles = new ArrayList<>();
    private final ArticleAdapter adapter = new ArticleAdapter();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        darkTheme = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
        WHITE = darkTheme ? 0xff121212 : Color.WHITE;
        INK = darkTheme ? 0xfff5f5f5 : 0xff151515;
        GRAY = darkTheme ? 0xffaaaaaa : 0xff7d7d7d;
        LIGHT = darkTheme ? 0xff333333 : 0xffe9e9e9;
        storage = new Storage(this);
        regular = Typeface.createFromAsset(getAssets(), "Arimo-Regular.ttf");
        bold = Typeface.createFromAsset(getAssets(), "Arimo-Bold.ttf");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(WHITE);
        window.setNavigationBarColor(WHITE);
        window.getDecorView().setSystemUiVisibility(darkTheme ? 0
            : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showNews();
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int sp, int color, boolean isBold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(isBold ? bold : regular);
        t.setIncludeFontPadding(true);
        return t;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private View rule() {
        View line = new View(this);
        line.setBackgroundColor(LIGHT);
        line.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        return line;
    }

    private void initScreen() {
        screen = vertical();
        screen.setBackgroundColor(WHITE);
        setContentView(screen);
    }

    private TextView action(String label, Runnable runnable) {
        TextView t = text(label, 15, INK, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(12), 0, dp(12));
        t.setContentDescription(label);
        t.setOnClickListener(v -> runnable.run());
        return t;
    }

    private void showNews() {
        generation++;
        showingSources = false;
        initScreen();

        LinearLayout heading = vertical();
        heading.setPadding(dp(GUTTER), dp(20), dp(GUTTER), dp(16));
        screen.addView(heading, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout row = horizontal();
        TextView title = text("Actualités", 35, INK, true);
        row.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(action("Sources", this::showSources));
        heading.addView(row);

        LinearLayout lower = horizontal();
        DateFormat format = new SimpleDateFormat("EEEE d MMMM", Locale.FRANCE);
        String day = format.format(new Date());
        day = Character.toUpperCase(day.charAt(0)) + day.substring(1);
        TextView date = text(day, 13, GRAY, false);
        lower.addView(date, new LinearLayout.LayoutParams(0, -2, 1));
        lower.addView(action("Actualiser", this::refresh));
        heading.addView(lower);
        screen.addView(rule());

        FrameLayout content = new FrameLayout(this);
        screen.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        articleList = new ListView(this);
        articleList.setCacheColorHint(Color.TRANSPARENT);
        articleList.setDivider(null);
        articleList.setSelector(android.R.color.transparent);
        articleList.setVerticalScrollBarEnabled(false);
        articleList.setAdapter(adapter);
        articleList.setOnItemClickListener((parent, view, position, id) -> openArticle(displayedArticles.get(position)));
        content.addView(articleList, new FrameLayout.LayoutParams(-1, -1));

        emptyLabel = text("", 16, GRAY, false);
        emptyLabel.setGravity(Gravity.CENTER);
        emptyLabel.setPadding(dp(42), dp(24), dp(42), dp(24));
        content.addView(emptyLabel, new FrameLayout.LayoutParams(-1, -1));

        progress = new ProgressBar(this);
        progress.setIndeterminateTintList(ColorStateList.valueOf(INK));
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        loadingParams.topMargin = dp(30);
        content.addView(progress, loadingParams);

        if (storage.selected().isEmpty()) {
            showEmpty("Aucune source sélectionnée.\n\nTouchez « Sources » pour choisir vos actualités.");
            progress.setVisibility(View.GONE);
        } else {
            displayFiltered(storage.cached());
            if (displayedArticles.isEmpty()) showEmpty("Chargement des actualités…");
            refresh();
        }
    }

    private void showEmpty(String message) {
        emptyLabel.setText(message);
        emptyLabel.setVisibility(View.VISIBLE);
        articleList.setVisibility(View.GONE);
    }

    private void displayFiltered(List<Article> articles) {
        Set<String> sources = storage.selected();
        displayedArticles.clear();
        for (Article item : articles) {
            if (sources.contains(item.sourceUrl)) displayedArticles.add(item);
        }
        sortAndDeduplicate(displayedArticles);
        adapter.notifyDataSetChanged();
        emptyLabel.setVisibility(displayedArticles.isEmpty() ? View.VISIBLE : View.GONE);
        articleList.setVisibility(displayedArticles.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private static void sortAndDeduplicate(List<Article> articles) {
        articles.sort(Comparator.comparingLong((Article a) -> a.published).reversed());
        Map<String, Article> unique = new LinkedHashMap<>();
        for (Article a : articles) unique.putIfAbsent(a.link, a);
        articles.clear();
        articles.addAll(unique.values());
    }

    private void refresh() {
        if (showingSources) return;
        List<Source> selected = new ArrayList<>();
        Set<String> selectedUrls = storage.selected();
        for (Source source : storage.sources()) {
            if (selectedUrls.contains(source.url)) selected.add(source);
        }
        if (selected.isEmpty()) {
            progress.setVisibility(View.GONE);
            showEmpty("Aucune source sélectionnée.\n\nTouchez « Sources » pour commencer.");
            return;
        }
        final int taskGeneration = ++generation;
        progress.setVisibility(View.VISIBLE);
        List<Article> old = storage.cached();
        // Le coordinateur ne bloque jamais le fil principal Android.
        Thread worker = new Thread(() -> {
            Map<Source, Future<List<Article>>> futures = new LinkedHashMap<>();
            for (Source s : selected) futures.put(s, pool.submit(() -> FeedLoader.load(s)));
            List<Article> combined = new ArrayList<>();
            int failed = 0;
            for (Map.Entry<Source, Future<List<Article>>> job : futures.entrySet()) {
                try {
                    combined.addAll(job.getValue().get(20, TimeUnit.SECONDS));
                } catch (Exception ex) {
                    failed++;
                    job.getValue().cancel(true);
                    for (Article oldArticle : old) {
                        if (oldArticle.sourceUrl.equals(job.getKey().url)) combined.add(oldArticle);
                    }
                }
            }
            sortAndDeduplicate(combined);
            final List<Article> result = combined;
            final int failureCount = failed;
            runOnUiThread(() -> {
                if (destroyed || showingSources || taskGeneration != generation) return;
                progress.setVisibility(View.GONE);
                if (!result.isEmpty()) {
                    storage.saveCache(result);
                    displayFiltered(result);
                    if (failureCount > 0) {
                        Toast.makeText(this, "Certaines sources sont indisponibles", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showEmpty(failureCount > 0
                            ? "Impossible de charger les actualités.\nVérifiez votre connexion et réessayez."
                            : "Aucun article publié pour ces sources.");
                }
            });
        }, "fil-rss-loader");
        worker.start();
    }

    private void openArticle(Article article) {
        if (!FeedLoader.isArticleUrl(article.link)) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.link));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Aucun navigateur disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSources() {
        generation++;
        showingSources = true;
        initScreen();
        LinearLayout heading = vertical();
        heading.setPadding(dp(GUTTER), dp(18), dp(GUTTER), dp(16));
        screen.addView(heading);
        LinearLayout toolbar = horizontal();
        TextView title = text("Sources", 35, INK, true);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        toolbar.addView(action("Terminé", this::showNews));
        heading.addView(toolbar);
        TextView subtitle = text("Choisissez uniquement les médias que vous souhaitez lire.", 14, GRAY, false);
        subtitle.setPadding(0, dp(8), 0, dp(8));
        heading.addView(subtitle);
        screen.addView(rule());

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setVerticalScrollBarEnabled(false);
        screen.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout items = vertical();
        items.setPadding(dp(GUTTER), dp(12), dp(GUTTER), dp(24));
        scroller.addView(items);

        for (Source source : storage.sources()) {
            LinearLayout line = horizontal();
            line.setMinimumHeight(dp(66));
            TextView name = text(source.name, 17, INK, false);
            line.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            Switch check = new Switch(this);
            check.setShowText(false);
            check.setContentDescription("Suivre " + source.name);
            check.setChecked(storage.selected().contains(source.url));
            check.setThumbTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{INK, darkTheme ? 0xff404040 : 0xfff6f6f6}));
            check.setTrackTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                        new int[]{darkTheme ? 0xff888888 : 0xffb5b5b5,
                            darkTheme ? 0xff666666 : 0xffcecece}));
            check.setOnCheckedChangeListener((buttonView, checked) -> storage.setSelected(source.url, checked));
            line.addView(check);
            line.setOnClickListener(v -> check.setChecked(!check.isChecked()));
            if (source.custom) {
                line.setOnLongClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Supprimer cette source ?")
                            .setMessage(source.name)
                            .setNegativeButton("Annuler", null)
                            .setPositiveButton("Supprimer", (dialog, which) -> {
                                storage.removeSource(source.url);
                                showSources();
                            }).show();
                    return true;
                });
            }
            items.addView(line);
            items.addView(rule());
        }

        TextView add = text("+ Ajouter un flux RSS", 16, INK, true);
        add.setPadding(0, dp(24), 0, dp(22));
        add.setOnClickListener(v -> addCustomFeed());
        items.addView(add);
        TextView hint = text("Sans compte, sans publicité, sans suivi.\nAppui long pour supprimer une source ajoutée.", 13, GRAY, false);
        hint.setLineSpacing(dp(5), 1);
        items.addView(hint);
    }

    private void addCustomFeed() {
        LinearLayout inputs = vertical();
        inputs.setPadding(dp(22), dp(8), dp(22), 0);
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setTextSize(16);
        name.setTypeface(regular);
        name.setHint("Nom du média");
        inputs.addView(name);
        EditText url = new EditText(this);
        url.setSingleLine(true);
        url.setTextSize(16);
        url.setTypeface(regular);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        url.setHint("https://exemple.fr/rss.xml");
        inputs.addView(url);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ajouter une source")
                .setView(inputs)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Ajouter", null)
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String label = name.getText().toString().trim();
            String address = url.getText().toString().trim();
            if (label.isEmpty() || label.length() > 80) {
                name.setError("Indiquez un nom (80 caractères maximum)");
                return;
            }
            if (!FeedLoader.isHttpsUrl(address)) {
                url.setError("Utilisez une adresse HTTPS valide");
                return;
            }
            for (Source existing : storage.sources()) {
                if (existing.url.equals(address)) {
                    url.setError("Cette source existe déjà");
                    return;
                }
            }
            storage.addSource(new Source(label, address, true));
            dialog.dismiss();
            showSources();
        }));
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (showingSources) showNews();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        generation++;
        pool.shutdownNow();
        super.onDestroy();
    }

    private final class ArticleAdapter extends BaseAdapter {
        @Override public int getCount() { return displayedArticles.size(); }
        @Override public Object getItem(int position) { return displayedArticles.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout cell = vertical();
            cell.setPadding(dp(GUTTER), dp(21), dp(GUTTER), 0);
            Article article = displayedArticles.get(position);
            LinearLayout meta = horizontal();
            TextView source = text(article.sourceName, 12, GRAY, true);
            meta.addView(source);
            if (article.published > 0) {
                DateFormat clock = new SimpleDateFormat("dd/MM · HH:mm", Locale.FRANCE);
                TextView date = text("  ·  " + clock.format(new Date(article.published)), 12, GRAY, false);
                meta.addView(date);
            }
            cell.addView(meta);
            TextView title = text(article.title, 19, INK, true);
            title.setLineSpacing(dp(4), 1.0f);
            title.setPadding(0, dp(9), 0, 0);
            cell.addView(title);
            if (!article.summary.isEmpty() && !article.summary.equals(article.title)) {
                TextView summary = text(article.summary, 14, GRAY, false);
                summary.setMaxLines(2);
                summary.setEllipsize(android.text.TextUtils.TruncateAt.END);
                summary.setLineSpacing(dp(3), 1.0f);
                summary.setPadding(0, dp(8), 0, 0);
                cell.addView(summary);
            }
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dp(1));
            dividerParams.topMargin = dp(21);
            cell.addView(rule(), dividerParams);
            cell.setContentDescription(article.sourceName + ". " + article.title);
            return cell;
        }
    }
}
