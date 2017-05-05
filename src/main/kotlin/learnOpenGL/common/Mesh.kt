package learnOpenGL.common

import glm.BYTES
import glm.vec2.Vec2
import glm.vec3.Vec3
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.byteBufferBig
import uno.buffer.intBufferBig
import uno.gln.glBindBuffer
import uno.gln.glBindVertexArray
import uno.gln.intBuffer

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Vertex(
        // Position
        val position: Vec3,
        // Normal
        val normal: Vec3,
        // TexCoords
        val texCoords: Vec2 = Vec2()
        // Tangent
//        val tangent: Vec3,
//        // Bitangent
//        val bitangent: Vec3
) {
    companion object {
        val size = 2 * Vec3.size + Vec2.size
    }
}

class Texture(
        val id: Int,
        val type: String,
        val path: String)

class Mesh(
        val vertices: List<Vertex>,
        val indices: List<Int>,
        val textures: List<Texture>
) {
    val vao = intBufferBig(1)

    object Buffer {
        val VERTEX = 0
        val ELEMENT = 1
        val MAX = 2
    }

    val buffers = intBufferBig(Buffer.MAX)

    init {  // Now that we have all the required data, set the vertex buffers and its attribute pointers.

        // Create buffers/arrays
        glGenVertexArrays(vao)
        glGenBuffers(buffers)

        glBindVertexArray(vao)
        // Load data into vertex buffers
        glBindBuffer(GL_ARRAY_BUFFER, buffers[Buffer.VERTEX])
        val vertexBuffer = byteBufferBig(Vertex.size * vertices.size)
        vertices.forEachIndexed { i, it ->
            it.position.to(vertexBuffer, i * Vertex.size)
            it.normal.to(vertexBuffer, i * Vertex.size + Vec3.size)
            it.texCoords.to(vertexBuffer, i * Vertex.size + Vec3.size * 2)
        }
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.ELEMENT])
        val indexBuffer = byteBufferBig(indices.size)
        repeat(indices.size) { indexBuffer.putInt(it * Int.BYTES, indices[it])}
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

        // Set the vertex attribute pointers
        // Vertex Positions
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *)0)
        // Vertex Normals
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, Normal))
        // Vertex Texture Coords
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(2, 2, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, TexCoords))
        // Vertex Tangent
        glEnableVertexAttribArray(3)
        glVertexAttribPointer(3, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, Tangent))
        // Vertex Bitangent
        glEnableVertexAttribArray(4)
        glVertexAttribPointer(4, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, Bitangent))

        glBindVertexArray(0)
    }
}
