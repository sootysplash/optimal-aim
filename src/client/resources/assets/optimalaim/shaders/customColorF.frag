#version 150
out vec4 FragColor;

uniform vec4 customColor;

void main() {
    FragColor = customColor;
//    FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}