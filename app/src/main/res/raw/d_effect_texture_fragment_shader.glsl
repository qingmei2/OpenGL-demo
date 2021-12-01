precision mediump float;

uniform sampler2D vTexture;
varying vec2 aCoordinate;
varying float vAlpha;

void main(){
    vec4 nColor=texture2D(vTexture, aCoordinate);
    gl_FragColor=vec4(nColor.r * vAlpha, nColor.g * vAlpha, nColor.b * vAlpha, nColor.a * vAlpha);
}
