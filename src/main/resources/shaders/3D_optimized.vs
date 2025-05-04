#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in float colorIndex;
layout(location = 2) in float normalIndex;

uniform mat4 view;
uniform mat4 projection;

out float fragmentColorIndex;
out float fragmentNormalIndex;

void main() {
    gl_Position = projection * view * vec4(pos, 1.0);
    fragmentColorIndex = colorIndex;
    fragmentNormalIndex = normalIndex;
}
