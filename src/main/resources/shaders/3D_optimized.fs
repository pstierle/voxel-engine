#version 330 core

in float fragmentColorIndex;
in float fragmentNormalIndex;

out vec4 FragColor;

layout(std140) uniform colorPalette {
    vec3 colors[250];
};

layout(std140) uniform normalPalette {
    vec3 normals[6];
};

void main() {
    vec3 color = colors[int(fragmentColorIndex)];
    vec3 normal = normals[int(fragmentNormalIndex)];

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
