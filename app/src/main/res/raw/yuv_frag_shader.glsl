precision mediump float;
uniform sampler2D u_TextureY;
uniform sampler2D u_TextureU;
uniform sampler2D u_TextureV;
varying vec2 v_TextureCoordinates;

void main() {
   float  y,u,v;
     vec3 rgb;
//    mat3 convmatrix = mat3(vec3(1.164,  1.164, 1.164),
//                              vec3(0.0,   -0.392, 2.017),
//                              vec3(1.596, -0.813, 0.0));
   mat3 convmatrix = mat3(vec3(1,  1, 1),
                              vec3(0.0,  -0.18732, 1.8556   ),
                              vec3(1.57481, -0.46813,      0));

       y = (texture2D(u_TextureY, v_TextureCoordinates).r );
       u = (texture2D(u_TextureU, v_TextureCoordinates).r - (128.0 / 255.0));
       v = (texture2D(u_TextureV, v_TextureCoordinates).r - (128.0 / 255.0));

           rgb.r = y + 1.403 * v;
           rgb.g = y - 0.344 * u - 0.714 * v;
           rgb.b = y + 1.770 * u;
       gl_FragColor = vec4(rgb ,1.0);
}