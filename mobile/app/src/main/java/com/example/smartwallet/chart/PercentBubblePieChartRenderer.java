package com.example.smartwallet.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.ContextCompat;

import com.example.smartwallet.R;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Проценты с полупрозрачной подложкой без blur.
 */
public class PercentBubblePieChartRenderer extends PieChartRenderer {

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bubbleRect = new RectF();

    public PercentBubblePieChartRenderer(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setColor(ContextCompat.getColor(chart.getContext(), R.color.analytics_percent_label_bg));
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        float padH = Utils.convertDpToPixel(9f);
        float padV = Utils.convertDpToPixel(5f);
        float corner = Utils.convertDpToPixel(13f);

        float textWidth = mValuePaint.measureText(valueText);
        Paint.FontMetrics fm = mValuePaint.getFontMetrics();

        float left;
        float right;
        Paint.Align align = mValuePaint.getTextAlign();
        if (align == Paint.Align.RIGHT) {
            right = x + padH;
            left = right - textWidth - padH * 2f;
        } else if (align == Paint.Align.CENTER) {
            left = x - textWidth / 2f - padH;
            right = x + textWidth / 2f + padH;
        } else {
            left = x - padH;
            right = left + textWidth + padH * 2f;
        }

        float top = y + fm.ascent - padV;
        float bottom = y + fm.descent + padV;
        bubbleRect.set(left, top, right, bottom);
        c.drawRoundRect(bubbleRect, corner, corner, bubblePaint);

        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }
}
