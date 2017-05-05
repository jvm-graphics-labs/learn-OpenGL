package learnOpenGL.common

import assimp.AiMesh
import assimp.AiScene
import glm.BYTES
import glm.set
import glm.vec2.Vec2
import glm.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.byteBufferBig
import uno.buffer.floatBufferBig
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Mesh(
        assimpMesh: AiMesh,
        scene: AiScene
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
        val vertexSize = 3 + 3 + 2
        val vertices = floatBufferBig(vertexSize * assimpMesh.mNumVertices)
        assimpMesh.mVertices.forEachIndexed { i, v ->
            val n = assimpMesh.mNormals[i]
            v.to(vertices, i * vertexSize)
            n.to(vertices, i * vertexSize + 3)
            if (assimpMesh.mTextureCoords[0].isNotEmpty()) {
                val tc = assimpMesh.mTextureCoords[0][i]
                vertices[i * vertexSize + 3 + 3] = tc[0]
                vertices[i * vertexSize + 3 + 3 + 1] = tc[1]
            }
        }
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.ELEMENT])
        val indices = intBufferBig(assimpMesh.mNumFaces * 3)
        repeat(assimpMesh.mNumFaces * 3) { indices[it] = assimpMesh.mFaces[it / 3][it % 3] }
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        // Set the vertex attribute pointers
        // Vertex Positions
        glEnableVertexAttribArray(semantic.attr.POSITION)
        glVertexAttribPointer(glf.pos3_nor3_tc2)
        // Vertex Normals
        glEnableVertexAttribArray(semantic.attr.NORMAL)
        glVertexAttribPointer(glf.pos3_nor3_tc2[1])
        // Vertex Texture Coords
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)
        glVertexAttribPointer(glf.pos3_nor3_tc2[2])
        // Vertex Tangent
//        glEnableVertexAttribArray(3)
//        glVertexAttribPointer(3, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, Tangent))
//        // Vertex Bitangent
//        glEnableVertexAttribArray(4)
//        glVertexAttribPointer(4, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), (GLvoid *) offsetof (Vertex, Bitangent))

        glBindVertexArray()


    }
}
