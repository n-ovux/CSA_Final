#version 430

out vec4 fragColor;

in float eHeight;

void main()
{
    fragColor = vec4(1.0 - eHeight, 1.0 - eHeight, 1.0 - eHeight, 1.0);
}
