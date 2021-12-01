package com.github.qingmei2.opengl_demo.d_video.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

public class OpenglTextUtils {

    /**
     * android中绘制字体，使用画布canvas
     */
    public static Bitmap createFontBitmap(String text, float textSize, int maxWidth) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        TextPaint paint = new TextPaint();
//        if (typeface != null) {
//            paint.setTypeface(typeface);
//        }
//        paint.setShadowLayer(3, 0, 0, Color.parseColor("#99000000"));

        //字体设置
        //String fontType = "宋体";
        //Typeface typeface = Typeface.create(fontType, Typeface.NORMAL);
        //p.setTypeface(typeface);

        //消除锯齿
        paint.setAntiAlias(true);

        //字体
        paint.setColor(Color.GREEN);
        paint.setTextSize(textSize);

        //获取高度
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = metrics.bottom - metrics.top;

        //获取文本宽度
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int width = rect.right;//rect.width();  // left可能不是0，故使用right作为文本宽度

        //背景颜色
        //canvas.drawColor(Color.LTGRAY);

        int maxLines = 1;

        //绘制字体
        if (width > maxWidth) { // 换行
            int numLines = Math.min(width / maxWidth + 1, maxLines);
            int lineSpacingHeight = (int) (0.2f * (numLines - 1) * height);
            Bitmap bitmap = Bitmap.createBitmap(maxWidth, height * numLines + lineSpacingHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            StaticLayout layout;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                layout = StaticLayout.Builder.obtain(text, 0, text.length(),
                        paint, maxWidth)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0.0F, 1.2F)
                        .setIncludePad(true)
                        .setEllipsize(TextUtils.TruncateAt.END)
                        .setMaxLines(maxLines)
                        .build();
            } else {
                layout = new StaticLayout(text, 0, text.length(),
                        paint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true,
                        TextUtils.TruncateAt.END, width);
            }
            layout.draw(canvas);

            return bitmap;
        } else {                // 单行
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(text, 0, -metrics.ascent, paint);

            return bitmap;
        }
    }

}
