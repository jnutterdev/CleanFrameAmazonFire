package com.hector.cleanlauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Frosted-glass panel. Draws the slice of the pre-blurred wallpaper that sits
 * behind it (CleanFrame's "material" approach: blur rendered ahead of time,
 * so the TV only has to copy pixels), then a veil and an optional scrim.
 */
public class GlassView extends FrameLayout {

    static Bitmap blur;
    static boolean lightTone;

    private final float radius;
    private final boolean scrim;
    private int veilDark = 0x29080E12;   // rgba(8,14,18,.16)
    private int veilLight = 0x45080E12;  // rgba(8,14,18,.27)

    private final Paint shaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final RectF rect = new RectF();
    private final int[] loc = new int[2];
    private Bitmap shaderFor;
    private BitmapShader shader;

    public GlassView(Context c, float radiusPx, boolean scrim) {
        super(c);
        this.radius = radiusPx;
        this.scrim = scrim;
        setWillNotDraw(false);
        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(Math.max(1f, c.getResources().getDisplayMetrics().density * 0.5f));
        edge.setColor(0x1FFFFFFF);
    }

    public void setVeil(int dark, int light) {
        veilDark = dark;
        veilLight = light;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        rect.set(0, 0, w, h);
        Bitmap b = blur;
        if (b != null) {
            if (b != shaderFor) {
                shader = new BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                shaderFor = b;
            }
            View root = getRootView();
            getLocationInWindow(loc);
            float sx = root.getWidth() / (float) b.getWidth();
            float sy = root.getHeight() / (float) b.getHeight();
            matrix.setScale(sx, sy);
            matrix.postTranslate(-loc[0], -loc[1]);
            shader.setLocalMatrix(matrix);
            shaderPaint.setShader(shader);
            c.drawRoundRect(rect, radius, radius, shaderPaint);
        } else {
            fill.setColor(0xFF1D2A30);
            c.drawRoundRect(rect, radius, radius, fill);
        }
        fill.setShader(null);
        fill.setColor(lightTone ? veilLight : veilDark);
        c.drawRoundRect(rect, radius, radius, fill);
        if (scrim) {
            fill.setShader(new LinearGradient(0, 0, 0, h, 0x00040A0C, 0x45040A0C, Shader.TileMode.CLAMP));
            c.drawRoundRect(rect, radius, radius, fill);
            fill.setShader(null);
        }
        if (radius > 0) {
            float i = edge.getStrokeWidth() / 2f;
            rect.inset(i, i);
            c.drawRoundRect(rect, radius, radius, edge);
        }
    }
}
