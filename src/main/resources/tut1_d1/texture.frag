#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 fragColor;

in vec3 ourColor;
in vec2 texCoord;

// texture sampler
uniform sampler2D texture1;

void main()
{
    fragColor = texture(texture1, texCoord);
}