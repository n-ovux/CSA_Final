#version 430 core

out vec4 FragColor;

in vec2 TexCoord;

uniform sampler2D Sampler;

void main() {
    FragColor = vec4(texture(Sampler, TexCoord).rgb, 1.0);
}
