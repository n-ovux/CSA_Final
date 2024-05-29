#version 430

layout (quads, fractional_odd_spacing, ccw) in;

void main()
{
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;

    vec4 p0 = gl_in[0].gl_Position;
    vec4 p1 = gl_in[1].gl_Position;
    vec4 p2 = gl_in[2].gl_Position;
    vec4 p3 = gl_in[3].gl_Position;

    vec4 p = p0 * (1.0 - u) * (1.0 - v) + 
             p1 * u * (1.0 - v) +
             p3 * v * (1.0 - u) +
             p2 * u * v;

    gl_Position = p;
}
