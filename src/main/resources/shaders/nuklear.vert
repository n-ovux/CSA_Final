#version 430

out vec2 Frag_UV;
out vec4 Frag_Color;

in vec2 Position;
in vec2 TexCoord;
in vec4 Color;

uniform mat4 ProjMtx;

void main() {
    Frag_UV = TexCoord;
    Frag_Color = Color;
    gl_Position = ProjMtx * vec4(Position.xy, 0, 1);
}
