package com.hector.cleanlauncher;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

final class Wallpapers {
    static final class Item {
        String id, name, credit;
        boolean light;
    }

    static List<Item> load(Context c) {
        List<Item> out = new ArrayList<>();
        try {
            InputStream in = c.getAssets().open("wallpapers/wallpapers.json");
            byte[] buf = new byte[in.available()];
            int off = 0, n;
            while (off < buf.length && (n = in.read(buf, off, buf.length - off)) > 0) off += n;
            in.close();
            JSONArray arr = new JSONArray(new String(buf, "UTF-8"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Item it = new Item();
                it.id = o.getString("id");
                it.name = o.getString("name");
                it.credit = o.optString("credit");
                it.light = "light".equals(o.optString("tone"));
                out.add(it);
            }
        } catch (Exception ignored) { }
        return out;
    }

    private Wallpapers() { }
}
