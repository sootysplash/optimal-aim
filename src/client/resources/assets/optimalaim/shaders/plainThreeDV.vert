#version 150
#extension GL_ARB_explicit_attrib_location : enable

layout (location = 0) in vec3 aPos;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0);
//    gl_Position = projection * view * vec4(aPos, 1.0);
}