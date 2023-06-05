
//opengl 4宫格顶点着色器
attribute vec4 aPosition;
precision mediump float;
uniform mat4 uMatrix;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
attribute float alpha;
varying float inAlpha;
void main() {
    gl_Position = uMatrix*aPosition;
    vCoordinate = aCoordinate;
    inAlpha = alpha;
}