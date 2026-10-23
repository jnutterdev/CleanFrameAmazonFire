package com.hector.cleanlauncher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Tells HomeWatcherService about Home presses as soon as Fire OS logs them. ActivityManager
 * logs the HOME intent before Amazon's home draws, and it is the only sign of a press made
 * while Amazon's home is already in front (no window changes then).
 *
 * Needs READ_LOGS, granted over ADB (install.sh does it). A process only gets log access if
 * it starts after the grant, so this runs in its own ":homekey" process, started on bind.
 * Only ActivityManager lines are read, and nothing is kept.
 */
public class HomeKeyWatcher extends Service {

    static final int MSG_REGISTER = 1;
    /** A Home press; arg1 is the uid that started the HOME intent. */
    static final int MSG_HOME = 2;

    private static final String TAG = "CleanLauncher";
    private static final String PERM = "android.permission.READ_LOGS";

    private volatile Messenger client;
    private volatile boolean running;
    private volatile Process proc;

    private final Messenger inbox = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message m) {
            if (m.what == MSG_REGISTER) client = m.replyTo;
        }
    });

    static boolean permitted(Context c) {
        return c.checkPermission(PERM, android.os.Process.myPid(), android.os.Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!permitted(this)) return;
        running = true;
        new Thread(this::follow, "homekey").start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return inbox.getBinder();
    }

    @Override
    public void onDestroy() {
        running = false;
        Process p = proc;
        if (p != null) p.destroy();
        super.onDestroy();
    }

    /** Follow the log, restarting logcat with a growing pause if it ever exits. */
    private void follow() {
        long pause = 2000;
        while (running) {
            long since = System.currentTimeMillis();
            try {
                // "-T 1" starts at the newest line instead of replaying the whole buffer.
                proc = new ProcessBuilder("logcat", "-b", "main", "-b", "system", "-v", "epoch",
                        "-T", "1", "ActivityManager:I", "ActivityTaskManager:I", "*:S")
                        .redirectErrorStream(true).start();
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                Log.i(TAG, "watching for Home presses");
                String line;
                while (running && (line = in.readLine()) != null) {
                    int uid = homeStartUid(line, since);
                    if (uid >= 0) report(uid);
                }
            } catch (Exception e) {
                if (running) Log.w(TAG, "Home key watch failed: " + e);
            } finally {
                Process p = proc;
                if (p != null) p.destroy();
                proc = null;
            }
            if (!running) return;
            if (System.currentTimeMillis() - since > 60000) pause = 2000;
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                return;
            }
            pause = Math.min(pause * 2, 60000);
        }
    }

    /**
     * For a log line like
     *   "1729630000.123  487  648 I ActivityManager: START u0 {act=android.intent.action.MAIN
     *    cat=[android.intent.category.HOME] ... } from uid 1000"
     * returns the uid, or -1 if the line is not a HOME start made after {@code notBefore}.
     */
    static int homeStartUid(String line, long notBefore) {
        if (!line.contains("START u0 {") || !line.contains("cat=[android.intent.category.HOME]")) {
            return -1;
        }
        int at = line.lastIndexOf(" from uid ");
        if (at < 0) return -1;
        int s = at + " from uid ".length(), e = s;
        while (e < line.length() && Character.isDigit(line.charAt(e))) e++;
        if (e == s) return -1;
        try {
            String t = line.trim();
            long when = (long) (Double.parseDouble(t.substring(0, t.indexOf(' '))) * 1000);
            if (when < notBefore) return -1;   // "-T 1" replays one old line
        } catch (Exception ignored) { }
        try {
            return Integer.parseInt(line.substring(s, e));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void report(int uid) {
        Messenger c = client;
        if (c == null) return;
        try {
            c.send(Message.obtain(null, MSG_HOME, uid, 0));
        } catch (RemoteException e) {
            client = null;
        }
    }
}
