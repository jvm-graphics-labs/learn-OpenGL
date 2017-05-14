#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 FragColor;

void main()
{
    FragColor = vec4(0.04, 0.28, 0.26, 1.0);
}