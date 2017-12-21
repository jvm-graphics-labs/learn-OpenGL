#version 330 core

#define POSITION    0
#define COLOR       3
#define TEX_COORD   4

layout (location = POSITION) in vec2 aPos;
layout (location = TEX_COORD) in vec2 aTexCoords;

out vec2 TexCoords;

void main()
{
    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);
    TexCoords = aTexCoords;
}