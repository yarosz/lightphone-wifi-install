package io.github.yarosz.unsideloader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lists the apps installed on this phone that nothing else can remove: everything that isn't part of the
 * system, LightOS, or one of Light's own downloadable Tools (those are removed on Light's dashboard, which
 * keeps the account in step). Tapping one opens Android's own uninstall prompt; nothing is removed without
 * the owner confirming it there.
 */
public class MainActivity extends Activity {
    private static final int TEXT = Color.WHITE;
    private static final int DIM = Color.rgb(150, 150, 150);

    private LinearLayout list;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        list.setPadding(pad, dp(32), pad, dp(48));
        scroll.addView(list);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        list.removeAllViews();
        list.addView(text("Uninstall", 30, TEXT, Gravity.CENTER_HORIZONTAL, dp(8)));
        list.addView(text("Apps installed on this phone that LightOS and Light's dashboard can't remove. "
                + "Tap one, then confirm.", 15, DIM, Gravity.CENTER_HORIZONTAL, dp(28)));

        PackageManager pm = getPackageManager();
        List<App> apps = new ArrayList<>();
        App self = null;
        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            if ((info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) continue;
            if (info.packageName.startsWith("com.thelightphone.") || info.packageName.startsWith("com.lightos")) continue;
            App app = new App(String.valueOf(pm.getApplicationLabel(info)), info.packageName);
            if (info.packageName.equals(getPackageName())) self = app; else apps.add(app);
        }
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));

        if (apps.isEmpty()) {
            list.addView(text("No other apps to remove.", 22, DIM, Gravity.CENTER_HORIZONTAL, dp(24)));
        }
        for (App app : apps) list.addView(row(app.label, app.packageName));
        if (self != null) {
            list.addView(text(" ", 12, DIM, Gravity.START, dp(16)));
            list.addView(row("Uninstall (this app)", self.packageName));
        }
    }

    private View row(String label, String packageName) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(14), 0, dp(14));
        row.addView(text(label, 26, TEXT, Gravity.START, 0));
        row.addView(text(packageName, 13, DIM, Gravity.START, 0));
        row.setClickable(true);
        row.setOnClickListener(v -> startActivity(
                new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null))));
        return row;
    }

    private TextView text(String s, int sp, int color, int gravity, int bottomMargin) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setGravity(gravity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = bottomMargin;
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static final class App {
        final String label;
        final String packageName;

        App(String label, String packageName) {
            this.label = label;
            this.packageName = packageName;
        }
    }
}
