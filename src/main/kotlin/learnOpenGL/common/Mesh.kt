package learnOpenGL.common

import assimp.AiMaterial
import assimp.AiMesh
import assimp.AiScene
import assimp.AiTexture
import glm.set
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroy
import uno.buffer.floatBufferBig
import uno.buffer.intBufferBig
import uno.buffer.intBufferOf
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*
import java.nio.IntBuffer

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Mesh(assimpMesh: AiMesh, scene: AiScene) {

    val vao = intBufferBig(1)

    object Buffer {
        val VERTEX = 0
        val ELEMENT = 1
        val MAX = 2
    }

    val buffers = intBufferBig(Buffer.MAX)

    val indexCount: Int

    var diffuseMap: IntBuffer? = null
    var specularMap: IntBuffer? = null

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
        indexCount = assimpMesh.mNumFaces * 3
        val indices = intBufferBig(indexCount)
        repeat(indexCount) { indices[it] = assimpMesh.mFaces[it / 3][it % 3] }
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


        // Process materials
        with(scene.mMaterials[assimpMesh.mMaterialIndex]) {

            textures.firstOrNull { it.type == AiTexture.Type.diffuse }?.let {
                diffuseMap = intBufferOf(loadMaterialTexture(it, scene))
            }
            textures.firstOrNull { it.type == AiTexture.Type.specular }?.let {
                specularMap = intBufferOf(loadMaterialTexture(it, scene))
            }
        }
    }

    /**
     * Checks all material textures of a given type and loads the textures if they're not loaded yet.
     * The required info is returned as a Texture struct.
     */
    fun loadMaterialTexture(assimpTex: AiMaterial.Texture, scene: AiScene): Int {

        val textureID = glGenTextures()

        val gliTexture = scene.textures[assimpTex.file]!!

        glBindTexture(GL_TEXTURE_2D, textureID)
        val format = gli.gl.translate(gliTexture.format, gliTexture.swizzles)
        glTexImage2D(format, gliTexture)
        glGenerateMipmap(GL_TEXTURE_2D)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        return textureID
    }

    fun draw(diffuse: Boolean = false, specular: Boolean = false) {

        if (diffuse && diffuseMap != null) {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, diffuseMap!!)
        }
        if (specular && specularMap != null) {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.SPECULAR)
            glBindTexture(GL_TEXTURE_2D, specularMap!!)
        }

        // Draw mesh
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT)
        glBindVertexArray()

        // Always good practice to set everything back to defaults once configured.
        if (diffuse && diffuseMap != null) {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D)
        }
        if (specular && specularMap != null) {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.SPECULAR)
            glBindTexture(GL_TEXTURE_2D)
        }
    }

    fun dispose() {

        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)
        diffuseMap?.let {
            glDeleteTextures(it)
            it.destroy()
        }
        specularMap?.let {
            glDeleteTextures(it)
            it.destroy()
        }
    }
}
