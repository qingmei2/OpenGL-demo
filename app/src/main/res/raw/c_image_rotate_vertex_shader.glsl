// 顶点坐标
attribute vec4 vPosition;
// 纹理坐标
attribute vec2 vCoordinate;
// 变换矩阵
uniform mat4 vMatrix;

varying vec2 aCoordinate;

void main(){
    gl_Position=vMatrix*vPosition;
    aCoordinate=vCoordinate;
}
