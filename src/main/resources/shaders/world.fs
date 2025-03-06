#version 330 core

in vec3 color; 
in vec3 normal; 
in vec3 position; 

out vec4 FragColor;

uniform vec3 light_position;
uniform vec3 camera_position;

void main() {
    float specularStrength = 0.5;
    float ambientStrength = 0.7;

    vec3 lightColor = vec3(1.0, 1.0, 1.0);
    vec3 ambient = ambientStrength * lightColor;

    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(light_position - position);  

    vec3 viewDir = normalize(camera_position - position);
    vec3 reflectDir = reflect(-lightDir, norm);  

    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor; 


    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
    vec3 specular = specularStrength * spec * lightColor;  

    vec3 result = (ambient + diffuse + specular) * color;

    FragColor = vec4(result, 1.0);
}