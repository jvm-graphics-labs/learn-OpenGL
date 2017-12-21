#version 330 core

#define POSITION    0
#define COLOR       3

layout (location = POSITION) in vec3 aPos;
layout (location = COLOR) in vec3 aColor;

out vec3 ourColor;

void main()
{
    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
    ourColor = aColor;
}