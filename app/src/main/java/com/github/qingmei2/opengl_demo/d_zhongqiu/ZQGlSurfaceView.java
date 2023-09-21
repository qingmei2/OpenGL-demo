package com.github.qingmei2.opengl_demo.d_zhongqiu;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class ZQGlSurfaceView extends GLSurfaceView {

    public ZQGlSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public ZQGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        setRenderer(new ZQRenderer(context));
    }
}
