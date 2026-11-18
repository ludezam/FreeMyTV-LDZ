package io.freemytv;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Compatibility shell for the old recommendation view model. TV LDZ now has a fixed clock/weather header and
 * user-selected bundled wallpaper; there is no recommendations service, key storage, or network client.
 * The small data types remain temporarily because the legacy screensaver/photo renderer shares them.
 */
final class Recs {
    static final int MAX_CARDS = 6;
    interface Listener { void onRecs(); }

    static final class Pick {
        int id;
        String kind, title, meta, pkg, link;
        File photo;
    }

    static final class Poster {
        final Bitmap bitmap;
        final android.graphics.BitmapShader shader;
        Poster(Bitmap bitmap, android.graphics.BitmapShader shader) { this.bitmap = bitmap; this.shader = shader; }
    }

    private static final Recs INSTANCE = new Recs();
    static Recs get(Context context) { return INSTANCE; }
    static String key(Pick pick) { return pick == null ? "" : String.valueOf(pick.id); }
    static String periodLabel() { return ""; }
    static String serviceName(Pick pick) { return ""; }
    static List<String[]> installedServices(Context context) { return Collections.emptyList(); }

    void addListener(Listener listener) {}
    void removeListener(Listener listener) {}
    void refreshIfStale() {}
    void prefetchAll() {}
    void setLight(boolean light) {}
    void trim() {}
    List<Pick> now(String type) { return Collections.emptyList(); }
    List<Pick> showcase() { return Collections.emptyList(); }
    Backdrop backdrop(Pick pick) { return null; }
    Poster poster(Pick pick) { return null; }
    File readyImage(Pick pick) { return null; }
}
