#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vCoordinate; //接收从顶点着色器过来的参数
uniform samplerExternalOES uTexture;//纹理内容数据
void main()
{
    //给此片元从纹理中采样出颜色值
    gl_FragColor = texture2D(uTexture, vCoordinate);
}
