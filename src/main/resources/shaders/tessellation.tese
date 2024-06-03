#version 430

layout(quads, fractional_odd_spacing, ccw) in;

uniform sampler2D heightMap;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

in vec2 cTexCoord[];

out float eHeight;

void main()
{
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;

    vec2 uv0 = cTexCoord[0];
    vec2 uv1 = cTexCoord[1];
    vec2 uv2 = cTexCoord[2];
    vec2 uv3 = cTexCoord[3];

    vec2 leftUV = uv0 + v * (uv3 - uv0);
    vec2 rightUV = uv1 + v * (uv2 - uv1);
    vec2 texCoord = leftUV + u * (rightUV - leftUV);

    vec4 pos0 = gl_in[0].gl_Position;
    vec4 pos1 = gl_in[1].gl_Position;
    vec4 pos2 = gl_in[2].gl_Position;
    vec4 pos3 = gl_in[3].gl_Position;

    vec4 leftPos = pos0 + v * (pos3 - pos0);
    vec4 rightPos = pos1 + v * (pos2 - pos1);
    vec4 pos = leftPos + u * (rightPos - leftPos);

    float height = texture(heightMap, texCoord).r;
    pos += vec4(0.0, 0.0, 10*height, 0.0);

    gl_Position = projection * view * model * pos; // Matrix transformations go here
    eHeight = height;
}
