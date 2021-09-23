// 顶点坐标
attribute vec4 av_Position;
// 纹理坐标
attribute vec2 af_Position;
uniform mat4 u_Matrix;
varying vec2 v_texPo;

void main() {
    v_texPo = af_Position;
    gl_Position =  u_Matrix * av_Position;
}
