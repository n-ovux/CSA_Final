#version 430

uniform sampler2D imageSampler;

out vec4 fragColor;

in float eHeight;

void main()
{
    fragColor = vec4(1 - eHeight, 1 - eHeight, 1 - eHeight, 1.0);
}
