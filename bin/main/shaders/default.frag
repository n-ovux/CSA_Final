#version 330

out vec4 fragColor;

in vec3 color;
in vec2 texCoord;

uniform sampler2D ourTexture;

void main()
{
    fragColor = texture(ourTexture, texCoord);
}
