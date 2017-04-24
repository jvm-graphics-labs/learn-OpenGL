#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 fragColor;

in vec3 ourColor;

void main()
{
    fragColor = vec4(ourColor, 1.0f);
}