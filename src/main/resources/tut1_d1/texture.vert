#version 330 core

#define POSITION    0
#define COLOR       3
#define TEX_COORD   4

layout (location = POSITION) in vec3 aPos;
layout (location = COLOR) in vec3 aColor;
layout (location = TEX_COORD) in vec2 aTexCoord;

out vec3 ourColor;
out vec2 texCoord;

void main()
{
    gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
    ourColor = aColor;
    texCoord = aTexCoord;
}