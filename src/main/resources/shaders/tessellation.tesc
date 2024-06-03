#version 430 core

layout(vertices = 4) out;

in vec2 vTexCoord[];

out vec2 cTexCoord[];

uniform float subdivisions;

void main()
{
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    cTexCoord[gl_InvocationID] = vTexCoord[gl_InvocationID];

    gl_TessLevelOuter[0] = subdivisions;
    gl_TessLevelOuter[1] = subdivisions;
    gl_TessLevelOuter[2] = subdivisions;
    gl_TessLevelOuter[3] = subdivisions;

    gl_TessLevelInner[0] = subdivisions;
    gl_TessLevelInner[1] = subdivisions;
}
