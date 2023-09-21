// 顶点坐标
attribute vec4 vPosition;
// 纹理坐标
attribute vec2 vCoordinate;
// 透明度
uniform float aAlpha;
// 变换矩阵
uniform mat4 vMatrix;
// 透明度
varying float vAlpha;
varying vec2 aCoordinate;

void main(){
    vAlpha = aAlpha;
    gl_Position=vMatrix*vPosition;
    aCoordinate=vCoordinate;
}
