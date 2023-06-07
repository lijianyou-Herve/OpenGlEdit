#extension GL_OES_EGL_image_external: require
precision mediump float;
varying vec2 vCoordinate;
varying float inAlpha;
uniform samplerExternalOES uTexture;
void main() {
    vec4 color = texture2D(uTexture, vCoordinate);

    if (vCoordinate.x > 0.5) {
        //将输出视频帧的一半渲染成经典黑白风格的图像
        vec4 outColor = vec4(vec3(color.r * 0.299 + color.g * 0.587 + color.b * 0.114), color.a);
        gl_FragColor = outColor;
    } else {
        gl_FragColor = vec4(color.r, color.g, color.b, inAlpha);
    }
}