package learnOpenGL.common

import glm.vec2.Vec2
import glm.vec3.Vec3

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Vertex(
        // Position
        val position: Vec3,
        // Normal
        val normal: Vec3,
        // TexCoords
        val texCoords: Vec2 = Vec2(),
        // Tangent
        val tangent: Vec3,
        // Bitangent
        val bitangent: Vec3)

class Texture(
        val id: Int,
        val type: String,
        val path: String)

class mesh