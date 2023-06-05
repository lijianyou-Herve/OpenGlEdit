#extension GL_OES_EGL_image_external: require
precision mediump float;
varying vec2 vCoordinate;
varying float inAlpha;
varying vec4 kPos;
varying float outofRange;
uniform mat4 uMatrix;
uniform samplerExternalOES uTexture;
varying float inIndex;

void main() {

    float x = vCoordinate.x;
    float y = vCoordinate.y;

    //原始图像
    //    vec4 color = texture2D(uTexture, vCoordinate);
    //    gl_FragColor = vec4(color.r, color.g, color.b, inAlpha);


    vec4 color = texture2D(uTexture, vec2(x, y));

    // 判断纹理坐标是否超出范围
    if(inIndex==0.0){
        if (kPos.x > 0.0 || kPos.y < 0.0) {
            // 设置边缘颜色为黑色
//            color = vec4(0.0, 0.0, 0.0, 0);
            //丢弃这个像素
            discard;
        }
    }
    if(inIndex==1.0){
        if (kPos.x < 0.0 || kPos.y < 0.0) {
            // 设置边缘颜色为黑色
            discard;
        }
    }
    if(inIndex==2.0){
        if (kPos.x > 0.0 || kPos.y > 0.0) {
            //丢弃这个像素
            discard;
        }
    }
    if(inIndex==3.0){
        if (kPos.x < 0.0 || kPos.y > 0.0) {
            //丢弃这个像素
            discard;
        }
    }

    gl_FragColor = vec4(color.r, color.g, color.b, inAlpha);

}