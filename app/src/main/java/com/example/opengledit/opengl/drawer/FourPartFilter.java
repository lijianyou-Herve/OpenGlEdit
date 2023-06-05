package com.example.opengledit.opengl.drawer;

import android.content.Context;

public class FourPartFilter extends BaseRender {
    public FourPartFilter(Context context) {
        super(
                context,
                "render/four_part/vertex.frag",
                "render/four_part/frag.frag"
        );
    }
}
