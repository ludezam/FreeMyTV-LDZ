package io.freemytv;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;

/** Narrow, package-checked bridge for Control LDZ personalization. */
public final class RemoteSettingsProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }
    private void authorize() {
        String[] packages = getContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
        if (packages != null) for (String pkg : packages) if ("br.com.ldz.controle".equals(pkg)) return;
        throw new SecurityException("Control LDZ is required");
    }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        authorize(); Bundle out = new Bundle();
        try {
            Prefs.reload();
            if ("get".equals(method)) out.putString("settings", new JSONObject()
                    .put("wallpaperMode", Prefs.wallpaperMode()).put("wallpaperBlurred", Prefs.wallpaperBlurred())
                    .put("saverBackground", Prefs.saverBackground()).put("photos", Prefs.photos())
                    .put("photoCount", Photos.files(getContext()).size()).toString());
            else if ("set".equals(method)) {
                String key = extras == null ? "" : extras.getString("key", ""), value = extras == null ? "" : extras.getString("value", "");
                if ("wallpaperMode".equals(key) && oneOf(value,"sunset","forest","snow","beach","weather")) Prefs.setWallpaperMode(value);
                else if ("wallpaperBlurred".equals(key) && ("true".equals(value)||"false".equals(value))) Prefs.setWallpaperBlurred(Boolean.parseBoolean(value));
                else if ("saverBackground".equals(key) && oneOf(value,"selected","rotate","weather","sunset","forest","snow","beach")) Prefs.setSaverBackground(value);
                else throw new IllegalArgumentException("Opção inválida");
                out.putBoolean("ok",true);
            } else if ("import".equals(method)) { Prefs.setPhotos(true); new Handler(Looper.getMainLooper()).postDelayed(() -> Photos.importInbox(getContext(), () -> Prefs.setPhotos(true)), 300); out.putBoolean("ok",true); }
            else if ("clearPhotos".equals(method)) { Photos.clear(getContext()); Prefs.setPhotos(false); out.putBoolean("ok",true); }
            else throw new IllegalArgumentException("Ação indisponível");
        } catch (Exception e) { out.putBoolean("ok",false); out.putString("error",e.getMessage()==null?"Falha ao aplicar opção":e.getMessage()); }
        return out;
    }
    private static boolean oneOf(String v,String... all) { for(String s:all) if(s.equals(v)) return true; return false; }
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){return null;}
    @Override public String getType(Uri u){return null;}
    @Override public Uri insert(Uri u,ContentValues v){return null;}
    @Override public int delete(Uri u,String s,String[] a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}