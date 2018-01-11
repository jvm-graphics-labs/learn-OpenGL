#version 330 core

#define POSITION    0
#define NORMAL      1
#define COLOR       3
#define TEX_COORD   4

layout (location = POSITION) in vec3 aPos;
layout (location = NORMAL) in vec3 aNormal;

out vec3 Normal;
out vec3 Position;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    Normal = mat3(transpose(inverse(model))) * aNormal;
    Position = vec3(model * vec4(aPos, 1.0));
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}