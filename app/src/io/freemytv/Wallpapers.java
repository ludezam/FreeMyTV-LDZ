package io.freemytv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

/** Bundled and photo backgrounds for the personalized home screen, plus weather-driven illustrated scenery. */
final class Wallpapers {
    private Wallpapers() {}

    /** Bundled 16:9 sunset so the background remains usable without network access. */
    static Backdrop sunset(Context context) {
        try (InputStream in = context.getAssets().open("wallpapers/sunset.jpg")) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap != null) return Backdrop.make(bitmap, true, true, Prefs.wallpaperBlurred());
        } catch (Exception e) {
            AppLog.w("Sunset wallpaper: " + e.getMessage());
        }
        return null;
    }

    /** Landscapes are bundled in the APK: selection and drawing never depend on network access. */
    static Backdrop landscape(Context context, String mode) {
        Bitmap bitmap = bundledImage(context, mode);
        if (bitmap == null) return sunset(context);
        return Backdrop.make(bitmap, true, true, Prefs.wallpaperBlurred());
    }

    private static final long SUN_WINDOW_MS = 15 * 60_000L;

    /** "dawn" or "dusk" during the 15 minutes before/after the local solar event; otherwise empty. */
    static String solarSlot(long now, long sunrise, long sunset) {
        if (sunrise > 0 && now >= sunrise - SUN_WINDOW_MS && now <= sunrise + SUN_WINDOW_MS) return "dawn";
        if (sunset > 0 && now >= sunset - SUN_WINDOW_MS && now <= sunset + SUN_WINDOW_MS) return "dusk";
        return "";
    }

    /** Raw 16:9 bitmap for the screensaver, loaded directly from the APK assets. */
    static Bitmap screensaverImage(Context context, String mode, int code, boolean day,
                                   long now, long sunrise, long sunset) {
        if ("weather".equals(mode)) {
            Bitmap image = weatherImage(context, code, day, now, sunrise, sunset);
            return image != null ? image : scene(code, day);
        }
        Bitmap b = bundledImage(context, mode);
        return b != null ? b : bundledImage(context, "sunset");
    }

    private static Bitmap bundledImage(Context context, String mode) {
        String name = "forest".equals(mode) || "snow".equals(mode) || "beach".equals(mode) ? mode : "sunset";
        try (InputStream in = context.getAssets().open("wallpapers/" + name + ".jpg")) {
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            AppLog.w("Bundled wallpaper " + name + ": " + e.getMessage());
            return null;
        }
    }

    /** Weather photo; dawn and dusk take precedence during their 30-minute windows. */
    static Backdrop weather(Context context, int code, boolean day, long now, long sunrise, long sunset) {
        Bitmap image = weatherImage(context, code, day, now, sunrise, sunset);
        return Backdrop.make(image != null ? image : scene(code, day), true, true, Prefs.wallpaperBlurred());
    }

    private static Bitmap weatherImage(Context context, int code, boolean day, long now, long sunrise, long sunset) {
        String slot = solarSlot(now, sunrise, sunset);
        String name;
        if ("dawn".equals(slot)) name = "dawn";
        else if ("dusk".equals(slot)) name = "dusk";
        else if (code >= 95) name = "thunderstorm";
        else if (code >= 71) name = "snow";
        else if (code >= 51 && code <= 67 || code >= 80 && code <= 82) name = day ? "rain_day" : "rain_night";
        else if (code == 45 || code == 48) name = "fog";
        else if (code == 3) name = day ? "overcast_day" : "overcast_night";
        else if (code == 2) name = day ? "partly_cloudy_day" : "few_clouds_night";
        else if (code == 1) name = day ? "few_clouds_day" : "mostly_clear_night";
        else name = day ? "clear_day" : "clear_night";
        try (InputStream in = context.getAssets().open("wallpapers/weather/" + name + ".jpg")) {
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            AppLog.w("Weather photo " + name + ": " + e.getMessage());
            return null;
        }
    }

    private static Bitmap scene(int code, boolean day) {
        int w = 1920, h = 1080;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        int kind = code >= 95 ? 6 : code >= 71 ? 5
                : code >= 51 && code <= 67 || code >= 80 && code <= 82 ? 4
                : code == 45 || code == 48 ? 3 : code == 3 ? 2 : code == 1 || code == 2 ? 1 : 0;
        int[][] daySky = {
                {0xFF3A8DCB, 0xFFF5A978}, {0xFF629DC8, 0xFFE7B38F}, {0xFF8194A3, 0xFFB7B9BB},
                {0xFFB8C6C8, 0xFFD9D8CF}, {0xFF506C83, 0xFF687B88}, {0xFF8EABC1, 0xFFD5E0E2},
                {0xFF3E5169, 0xFF7B6571}};
        int[][] nightSky = {
                {0xFF081942, 0xFF29456C}, {0xFF132544, 0xFF3F5878}, {0xFF172334, 0xFF364658},
                {0xFF26384D, 0xFF63758A}, {0xFF101C2A, 0xFF293B51}, {0xFF243A55, 0xFF465A70},
                {0xFF080D1C, 0xFF1D2A45}};
        int[][] sky = day ? daySky : nightSky;
        p.setShader(new LinearGradient(0, 0, 0, h * 0.7f, sky[kind][0], sky[kind][1], Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        boolean showOrb = kind == 0 || kind == 1 || kind == 3 || kind == 5;
        if (!day && (kind == 0 || kind == 1)) drawStars(c, p, w, h, code);
        // Sun or moon and its soft halo. The larger moon keeps its fine edge detail on a 1080p screen.
        float orbX = w * 0.72f, orbY = h * 0.22f, orbR = h * (day ? 0.07f : 0.075f);
        int orb = day ? 0xFFFFE6A3 : 0xFFF1F3FF;
        if (showOrb) {
            p.setShader(new RadialGradient(orbX, orbY, orbR * 3.8f,
                    new int[] {withAlpha(orb, day ? 0.42f : 0.32f), withAlpha(orb, 0.12f), 0x00FFFFFF},
                    new float[] {0, 0.38f, 1}, Shader.TileMode.CLAMP));
            c.drawCircle(orbX, orbY, orbR * 3.8f, p);
            p.setShader(null);
        }
        if (showOrb && day) {
            p.setColor(orb);
            c.drawCircle(orbX, orbY, orbR, p);
        } else if (showOrb) {
            p.setShader(new RadialGradient(orbX - orbR * 0.28f, orbY - orbR * 0.3f, orbR * 1.65f,
                    new int[] {0xFFFFFFFF, 0xFFF1F3FB, 0xFFB7C4D8}, new float[] {0, 0.68f, 1}, Shader.TileMode.CLAMP));
            c.drawCircle(orbX, orbY, orbR, p);
            p.setShader(null);
            drawMoonCraters(c, p, orbX, orbY, orbR);
        }

        if (kind > 0) drawClouds(c, p, w, h, day, kind);

        // Distant mountain ridges and a wooded shoreline.
        Path ridge = new Path();
        ridge.moveTo(0, h * 0.56f);
        ridge.cubicTo(w * 0.12f, h * 0.49f, w * 0.22f, h * 0.52f, w * 0.31f, h * 0.54f);
        ridge.cubicTo(w * 0.44f, h * 0.43f, w * 0.52f, h * 0.52f, w * 0.63f, h * 0.54f);
        ridge.cubicTo(w * 0.77f, h * 0.47f, w * 0.9f, h * 0.54f, w, h * 0.51f);
        ridge.lineTo(w, h * 0.68f); ridge.lineTo(0, h * 0.68f); ridge.close();
        p.setColor(day ? (kind == 3 ? 0xFF758994 : 0xFF38566A) : 0xFF1C3151);
        c.drawPath(ridge, p);
        Path farRidge = new Path();
        farRidge.moveTo(0, h * 0.59f);
        farRidge.cubicTo(w * 0.13f, h * 0.53f, w * 0.22f, h * 0.56f, w * 0.36f, h * 0.58f);
        farRidge.cubicTo(w * 0.53f, h * 0.51f, w * 0.65f, h * 0.57f, w * 0.78f, h * 0.58f);
        farRidge.cubicTo(w * 0.9f, h * 0.54f, w * 0.96f, h * 0.57f, w, h * 0.55f);
        farRidge.lineTo(w, h * 0.68f); farRidge.lineTo(0, h * 0.68f); farRidge.close();
        p.setColor(day ? (kind == 5 ? 0xFF9FB5C1 : kind == 3 ? 0xFF9AA8A8 : 0xFF536D7D) : 0xFF172941);
        c.drawPath(farRidge, p);
        Path shore = new Path();
        shore.moveTo(0, h * 0.6f);
        shore.cubicTo(w * 0.16f, h * 0.56f, w * 0.2f, h * 0.62f, w * 0.33f, h * 0.6f);
        shore.cubicTo(w * 0.48f, h * 0.59f, w * 0.64f, h * 0.62f, w, h * 0.58f);
        shore.lineTo(w, h * 0.68f); shore.lineTo(0, h * 0.68f); shore.close();
        p.setColor(day ? (kind == 3 ? 0xFF8F9B9A : 0xFF294D48) : 0xFF142B37);
        c.drawPath(shore, p);
        drawPines(c, p, w, h, day, kind);

        // Lake and its sun/moon reflection.
        p.setShader(new LinearGradient(0, h * 0.59f, 0, h, day ? 0xFF568EAA : 0xFF162E53,
                day ? 0xFF183E58 : 0xFF09172F, Shader.TileMode.CLAMP));
        c.drawRect(0, h * 0.59f, w, h, p);
        p.setShader(null);
        int reflection = day ? (kind == 0 ? 0xFFFFCB86 : 0xFFE0D3C1) : 0xFFB7CBEA;
        p.setShader(new LinearGradient(orbX, h * 0.58f, orbX, h, withAlpha(reflection, 0.52f), 0x00FFFFFF, Shader.TileMode.CLAMP));
        Path shine = new Path();
        shine.moveTo(orbX - w * 0.018f, h * 0.59f); shine.lineTo(orbX + w * 0.018f, h * 0.59f);
        shine.lineTo(orbX + w * 0.11f, h); shine.lineTo(orbX - w * 0.11f, h); shine.close();
        c.drawPath(shine, p);
        p.setShader(null);

        Random rng = new Random(code * 37L + (day ? 19 : 71));
        p.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 100; i++) {
            float y = h * (0.61f + rng.nextFloat() * 0.37f);
            float x = rng.nextFloat() * w;
            float length = (8 + rng.nextFloat() * 50) * (w / 1920f);
            p.setStrokeWidth((1 + rng.nextFloat() * 2) * (w / 1920f));
            p.setColor(withAlpha(i % 4 == 0 ? reflection : 0xFFB5D1DB, 0.08f + rng.nextFloat() * 0.18f));
            c.drawLine(x, y, x + length, y, p);
        }

        if (kind == 4) drawRain(c, p, w, h, rng, day, code);
        else if (kind == 5) drawSnow(c, p, w, h, rng, code);
        else if (kind == 6) drawStorm(c, p, w, h, code);
        else if (kind == 3) drawFog(c, p, w, h, day);
        return b;
    }

    private static void drawClouds(Canvas c, Paint p, int w, int h, boolean day, int kind) {
        int color = day ? (kind == 6 ? 0xFF354255 : kind == 5 ? 0xFFDCE5EB : kind == 3 ? 0xFFD7DEDE :
                kind == 4 ? 0xFF718392 : kind == 2 ? 0xFFAAB8C1 : 0xFFB9CBD5) : 0xFF283A55;
        int alpha = kind == 6 ? 0xF0 : kind == 2 || kind == 3 ? 0xE4 : kind == 4 ? 0xD8 : kind == 5 ? 0xC8 : 0xA8;
        p.setColor((alpha << 24) | (color & 0x00FFFFFF));
        cloud(c, p, w * 0.22f, h * 0.34f, h * (kind == 6 ? 0.13f : 0.09f));
        cloud(c, p, w * 0.82f, h * 0.37f, h * (kind == 2 || kind == 6 ? 0.12f : 0.08f));
        if (kind == 2 || kind == 4 || kind == 5 || kind == 6) cloud(c, p, w * 0.54f, h * 0.27f, h * 0.11f);
        if (kind == 3) drawFog(c, p, w, h, day);
    }

    private static void cloud(Canvas c, Paint p, float x, float y, float r) {
        int base = p.getColor();
        float alpha = Color.alpha(base) / 255f;
        int top = withAlpha(0xFFF3F7FA, alpha * 0.94f);
        int bottom = withAlpha(0xFF596B7A, alpha * 0.38f);
        p.setShader(new LinearGradient(0, y - r, 0, y + r, new int[] {top, base, bottom},
                new float[] {0, 0.58f, 1}, Shader.TileMode.CLAMP));
        c.drawCircle(x - r * 0.65f, y + r * 0.2f, r * 0.7f, p);
        c.drawCircle(x, y - r * 0.12f, r, p);
        c.drawCircle(x + r * 0.78f, y + r * 0.12f, r * 0.77f, p);
        c.drawRoundRect(x - r * 1.25f, y, x + r * 1.45f, y + r * 0.8f, r * 0.35f, r * 0.35f, p);
        p.setShader(null);
        p.setColor(withAlpha(0xFF192A3D, alpha * 0.11f));
        c.drawRoundRect(x - r * 0.9f, y + r * 0.56f, x + r * 1.1f, y + r * 0.72f, r * 0.08f, r * 0.08f, p);
        p.setColor(base);
    }

    private static void drawPines(Canvas c, Paint p, int w, int h, boolean day, int kind) {
        Random rng = new Random(0x71AEE + kind);
        int color = day ? (kind == 5 ? 0xFF526E6D : 0xFF183F3D) : 0xFF0A1D2B;
        for (int i = 0; i < 24; i++) {
            float x = w * (0.008f + i * 0.012f);
            float base = h * (0.625f + rng.nextFloat() * 0.035f);
            float height = h * (0.06f + rng.nextFloat() * 0.11f);
            float half = height * (0.22f + rng.nextFloat() * 0.08f);
            p.setColor(color);
            Path tree = new Path();
            tree.moveTo(x, base - height);
            tree.lineTo(x - half, base - height * 0.42f);
            tree.lineTo(x - half * 0.43f, base - height * 0.45f);
            tree.lineTo(x - half * 1.32f, base - height * 0.08f);
            tree.lineTo(x + half * 1.32f, base - height * 0.08f);
            tree.lineTo(x + half * 0.43f, base - height * 0.45f);
            tree.lineTo(x + half, base - height * 0.42f);
            tree.close();
            c.drawPath(tree, p);
            p.setStrokeWidth(Math.max(2, w / 720f));
            c.drawLine(x, base - height * 0.1f, x, base, p);
        }
    }

    private static void drawRain(Canvas c, Paint p, int w, int h, Random rng, boolean day, int code) {
        p.setColor(day ? 0x7DDBEEFF : 0x709CB7D6);
        int drops = code <= 55 ? 75 : code == 80 ? 105 : code >= 65 ? 220 : 150;
        p.setStrokeWidth(Math.max(2, w / (code <= 55 ? 1400f : 900f)));
        for (int i = 0; i < drops; i++) {
            float x = rng.nextFloat() * w, y = rng.nextFloat() * h * 0.9f;
            c.drawLine(x, y, x - w * 0.008f, y + h * 0.035f, p);
        }
    }

    private static void drawSnow(Canvas c, Paint p, int w, int h, Random rng, int code) {
        int flakes = code == 71 ? 60 : code == 75 || code == 86 ? 145 : 95;
        for (int i = 0; i < flakes; i++) {
            float x = rng.nextFloat() * w, y = rng.nextFloat() * h * 0.76f;
            p.setColor(withAlpha(0xFFFFFFFF, 0.42f + rng.nextFloat() * 0.5f));
            c.drawCircle(x, y, Math.max(2, w / 520f + rng.nextFloat() * w / 900f), p);
        }
    }

    private static void drawStorm(Canvas c, Paint p, int w, int h, int code) {
        p.setColor(0xFFFFE6A0);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(5, w / 300f));
        p.setStrokeJoin(Paint.Join.ROUND);
        int bolts = code >= 99 ? 2 : 1;
        for (int i = 0; i < bolts; i++) {
            float x = w * (0.62f + i * 0.2f), y = h * (0.31f + i * 0.035f);
            Path bolt = new Path();
            bolt.moveTo(x, y); bolt.lineTo(x - w * 0.035f, y + h * 0.09f);
            bolt.lineTo(x + w * 0.01f, y + h * 0.09f); bolt.lineTo(x - w * 0.03f, y + h * 0.18f);
            c.drawPath(bolt, p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private static void drawFog(Canvas c, Paint p, int w, int h, boolean day) {
        p.setShader(new LinearGradient(0, h * 0.42f, 0, h * 0.7f,
                new int[] {0x00FFFFFF, day ? 0xA6E1E7E7 : 0x80394B60, 0x00FFFFFF},
                new float[] {0, 0.48f, 1}, Shader.TileMode.CLAMP));
        c.drawRect(0, h * 0.4f, w, h * 0.72f, p);
        p.setShader(null);
    }

    private static void drawStars(Canvas c, Paint p, int w, int h, int seed) {
        Random rng = new Random(0x51A7L + seed);
        for (int i = 0; i < 125; i++) {
            float x = w * (0.34f + rng.nextFloat() * 0.64f);
            float y = h * (0.025f + rng.nextFloat() * 0.37f);
            float r = 1.2f + rng.nextFloat() * 2.2f;
            p.setColor(withAlpha(0xFFEAF2FF, 0.28f + rng.nextFloat() * 0.65f));
            c.drawCircle(x, y, r, p);
            if (i % 19 == 0) {
                p.setStrokeWidth(Math.max(1, w / 1800f));
                c.drawLine(x - r * 2, y, x + r * 2, y, p);
                c.drawLine(x, y - r * 2, x, y + r * 2, p);
            }
        }
    }

    private static void drawMoonCraters(Canvas c, Paint p, float x, float y, float radius) {
        Random rng = new Random(0xC0FFEE);
        for (int i = 0; i < 20; i++) {
            float angle = rng.nextFloat() * (float) (Math.PI * 2);
            float distance = (float) Math.sqrt(rng.nextFloat()) * radius * 0.77f;
            float cx = x + (float) Math.cos(angle) * distance;
            float cy = y + (float) Math.sin(angle) * distance;
            float crater = radius * (0.025f + rng.nextFloat() * 0.07f);
            p.setColor(withAlpha(0xFF8795AB, 0.08f + rng.nextFloat() * 0.11f));
            c.drawCircle(cx, cy, crater, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1, radius * 0.012f));
            p.setColor(withAlpha(0xFFFFFFFF, 0.16f));
            c.drawArc(cx - crater, cy - crater, cx + crater, cy + crater, 205, 220, false, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private static int withAlpha(int color, float alpha) {
        return (Math.max(0, Math.min(255, Math.round(255 * alpha))) << 24) | (color & 0x00FFFFFF);
    }
}
