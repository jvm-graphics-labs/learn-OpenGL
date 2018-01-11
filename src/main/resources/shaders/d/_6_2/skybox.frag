#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 FragColor;

in vec3 TexCoords;

uniform samplerCube texture1;

void main()
{
    FragColor = texture(texture1, TexCoords);
}