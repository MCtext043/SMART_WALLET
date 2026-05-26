package com.example.smartwallet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Затемнение вокруг области сканирования и белая рамка под QR.
 */
public final class QrScanOverlayView extends View {

    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF frameRect = new RectF();

    public QrScanOverlayView(Context context) {
        super(context);
        init();
    }

    public QrScanOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QrScanOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        dimPaint.setColor(0x88000000);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(0xFFFFFFFF);
        strokePaint.setStrokeWidth(3f * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float d = getResources().getDisplayMetrics().density;
        float frame = Math.min(w, h) * 0.68f;
        float left = (w - frame) / 2f;
        float top = (h - frame) / 2f;
        float right = left + frame;
        float bottom = top + frame;
        float rx = 12f * d;
        frameRect.set(left, top, right, bottom);

        int save = canvas.saveLayer(0, 0, w, h, null);
        canvas.drawRect(0, 0, w, h, dimPaint);
        canvas.drawRoundRect(frameRect, rx, rx, clearPaint);
        canvas.restoreToCount(save);

        canvas.drawRoundRect(frameRect, rx, rx, strokePaint);
    }
}
