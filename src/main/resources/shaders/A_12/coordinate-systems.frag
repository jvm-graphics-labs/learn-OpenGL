#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 fragColor;

in vec2 texCoord;

// texture samplers
uniform sampler2D textureA;
uniform sampler2D textureB;

void main()
{
    // linearly interpolate between both textures (80% container, 20% awesomeface)
    fragColor = mix(texture(textureA, texCoord), texture(textureB, texCoord), 0.2);
}