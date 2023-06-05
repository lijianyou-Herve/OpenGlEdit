//opengl 4宫格顶点着色器
attribute vec4 aPosition;
varying vec4 vPosition;
precision mediump float;
uniform mat4 uMatrix;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
attribute float alpha;
varying float inAlpha;
varying vec4 kPos;
void main() {
    vPosition = uMatrix * aPosition;
    gl_Position = vPosition;
    vCoordinate = aCoordinate;
    inAlpha = alpha;
    kPos = gl_Position;
}