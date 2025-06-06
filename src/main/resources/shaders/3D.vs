#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aColor;
layout(location = 2) in vec3 aNormal;

uniform mat4 view;
uniform mat4 projection;

out vec3 color; 
out vec3 normal;

void main() {
    gl_Position = projection * view * vec4(aPos, 1.0);
    color = aColor; 
    normal = aNormal;
}