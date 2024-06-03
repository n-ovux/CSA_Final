#version 430

uniform sampler2D imageSampler;

out vec4 fragColor;

in vec2 eTexCoord;

void main()
{
  float value = texture(imageSampler, eTexCoord).r;
  fragColor = vec4(value, value, value, 1.0);
}
