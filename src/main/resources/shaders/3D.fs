#version 330 core

in vec3 color;
in vec3 normal;

out vec4 FragColor;

void main() {
    float brightness = 1.0;

    if (normal.y > 0.5) {
        brightness = 1.0;
    } else if (normal.y < -0.5) {
        brightness = 0.5;
    } else {
        brightness = 0.8;
    }

    vec3 litColor = color * brightness;
    FragColor = vec4(litColor, 1.0);
}
