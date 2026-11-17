package com.example.tiktokbooster;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String TIKTOK = "com.zhiliaoapp.musically";
    private LinearLayout root;
    private TextView ramValue, storageValue, status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        refreshStats();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String s, int size, boolean bold, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(12, 16, 25));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(30), dp(20), dp(24));
        scroll.addView(root);
        setContentView(scroll);

        TextView title = text("TikTok Booster", 28, true, Color.WHITE);
        root.addView(title);
        TextView sub = text("Диагностика и безопасная настройка", 14, false, 0xFFADB8CC);
        LinearLayout.LayoutParams sp = params(); sp.topMargin = dp(6); root.addView(sub, sp);

        ramValue = metricCard("Оперативная память", "Считываем…");
        storageValue = metricCard("Свободное место", "Считываем…");

        status = text("Готов к проверке. Приложение не вмешивается во внутренние процессы TikTok.", 14, false, 0xFFE1E7F0);
        status.setPadding(dp(14), dp(14), dp(14), dp(14));
        status.setBackgroundColor(0xFF1B2638);
        LinearLayout.LayoutParams stp = params(); stp.topMargin = dp(16); root.addView(status, stp);

        addButton("Проверить состояние", v -> refreshStats());
        addButton("Открыть настройки TikTok", v -> openTikTokSettings());
        addButton("Открыть настройки батареи", v -> openBatterySettings());
        addButton("Запустить TikTok", v -> launchTikTok());

        TextView note = text("Важно: Android не разрешает обычному приложению принудительно очищать кэш или RAM другого приложения. Здесь доступны безопасная диагностика и переходы к системным настройкам. Не нажимайте «Очистить данные», если не хотите сбросить TikTok и выйти из аккаунта.", 12, false, 0xFF9EABC0);
        LinearLayout.LayoutParams np = params(); np.topMargin = dp(18); root.addView(note, np);
    }

    private LinearLayout.LayoutParams params() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private TextView metricCard(String label, String initial) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(0xFF1B2638);
        LinearLayout.LayoutParams cp = params(); cp.topMargin = dp(14);
        root.addView(card, cp);
        card.addView(text(label, 13, false, 0xFFADB8CC));
        TextView value = text(initial, 22, true, Color.WHITE);
        LinearLayout.LayoutParams vp = params(); vp.topMargin = dp(5); card.addView(value, vp);
        return value;
    }

    private void addButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = params(); p.topMargin = dp(12);
        root.addView(b, p);
    }

    private void refreshStats() {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long total = mi.totalMem;
        long avail = mi.availMem;
        ramValue.setText(String.format(Locale.getDefault(), "%.1f ГБ свободно из %.1f ГБ",
                avail / 1073741824.0, total / 1073741824.0));
        StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
        long free = fs.getAvailableBytes();
        long all = fs.getTotalBytes();
        storageValue.setText(String.format(Locale.getDefault(), "%.1f ГБ свободно из %.1f ГБ",
                free / 1073741824.0, all / 1073741824.0));
        status.setText(mi.lowMemory
                ? "Система сообщает о нехватке памяти. Закройте тяжёлые приложения обычным способом и перезапустите TikTok."
                : "Критической нехватки RAM сейчас не обнаружено. Если лента всё равно зависает, вероятны кэш TikTok, сеть, перегрев или проблема версии приложения.");
    }

    private void openTikTokSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + TIKTOK));
            startActivity(i);
        } catch (Exception e) {
            status.setText("Не удалось открыть карточку TikTok. Проверьте, установлен ли он.");
        }
    }

    private void openBatterySettings() {
        try { startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); }
        catch (Exception e) { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
    }

    private void launchTikTok() {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLaunchIntentForPackage(TIKTOK);
        if (i != null) startActivity(i);
        else status.setText("TikTok не найден. Возможно, установлена другая региональная сборка.");
    }
}
