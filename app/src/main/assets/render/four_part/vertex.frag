//opengl 4宫格顶点着色器
attribute vec4 aPosition;
varying vec4 vPosition;
precision mediump float;
uniform mat4 uMatrix;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
attribute float alpha;
attribute float index;
varying float inAlpha;
varying vec4 kPos;
varying float outofRange;
varying float inIndex;
void main() {
    vPosition = uMatrix * aPosition;
    gl_Position = vPosition;
    vCoordinate = aCoordinate;
    inAlpha = alpha;
    kPos = gl_Position;
    inIndex = index;

    // 判断纹理坐标是否超出范围
    if (kPos.x > 0.0 || kPos.y < 0.0) {
        // 设置边缘颜色为黑色
        outofRange = 1.0;
    } else {
        outofRange = 0.0;
    }

}