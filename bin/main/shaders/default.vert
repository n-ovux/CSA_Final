#version 330

layout (location=0) in vec2 aPosition;
layout (location=1) in vec3 aColor;
layout (location=2) in vec2 aTexCoord;

out vec3 color;
out vec2 texCoord;

void main() 
{
  gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);
  color = aColor;
  texCoord = aTexCoord;
}
