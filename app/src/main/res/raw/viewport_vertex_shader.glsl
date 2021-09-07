// 顶点坐标
attribute vec4 av_Position;
// 纹理坐标
attribute vec2 af_Position;
varying vec2 v_texPo;

void main() {
    v_texPo = af_Position;
    gl_Position = av_Position;
}
