#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 fragColor;


uniform vec3 objectColor;
uniform vec3 lightColor;


void main()
{
    fragColor = vec4(lightColor * objectColor, 1.0f);
}