#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 fragColor;

in vec2 texCoord;

// texture sampler
uniform sampler2D textureA;

void main()
{
    fragColor = texture(textureA, texCoord);
}