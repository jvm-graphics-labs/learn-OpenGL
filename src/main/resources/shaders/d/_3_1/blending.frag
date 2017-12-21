#version 330 core

#define FRAG_COLOR    0

layout (location = FRAG_COLOR) out vec4 FragColor;


in vec2 TexCoords;

uniform sampler2D texture1;

void main()
{
    vec4 texColor = texture(texture1, TexCoords);
    if(texColor.a < 0.1)
        discard;
    FragColor = texColor;
}