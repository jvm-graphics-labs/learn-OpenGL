#version 330 core

#define POSITION    0
#define NORMAL      1
#define COLOR       3
#define TEX_COORD   4

layout (location = POSITION) in vec3 aPos;
layout (location = NORMAL) in vec3 aNormal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 FragPos;
out vec3 Normal;

void main()
{
    FragPos = vec3(model * vec4(aPos, 1.0f));
    Normal = aNormal;

    gl_Position = projection * (view * vec4(FragPos, 1.0f));
}