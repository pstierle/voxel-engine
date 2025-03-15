#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in float colorIndex;
layout(location = 2) in float normalIndex;

uniform mat4 view;
uniform mat4 projection;

out vec3 color; 
out vec3 normal; 
out vec3 position;

layout(std140) uniform colorPalette {
    vec3 colors[250];
};

layout(std140) uniform normalPalette {
    vec3 normals[6];
};

void main() {
    gl_Position = projection * view * vec4(pos, 1.0);
    color = colors[int(colorIndex)];
    normal = normals[int(normalIndex)];
    position = pos;
}
