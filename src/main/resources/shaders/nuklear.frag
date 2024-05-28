#version 430

out vec4 Out_Color;

in vec2 Frag_UV;
in vec4 Frag_Color;

precision mediump float;

uniform sampler2D Texture;

void main() {
    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);
}
