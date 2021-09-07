attribute vec4 av_Position;
attribute vec2 af_Position;
varying vec2 v_texPo;

void main() {
    v_texPo = af_Position;
    gl_Position = av_Position;
}
