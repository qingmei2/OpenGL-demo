attribute vec4 aPosition;  //顶点位置
attribute vec2 aCoordinate; //顶点纹理坐标
varying vec2 vCoordinate;  //用于传递给片元着色器的变量
uniform mat4 uMatrix;
void main()
{
    gl_Position = uMatrix*aPosition; //根据总变换矩阵计算此次绘制此顶点位置
    vCoordinate = aCoordinate;//将接收的纹理坐标传递给片元着色器
}
