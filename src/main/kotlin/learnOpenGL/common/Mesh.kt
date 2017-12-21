package learnOpenGL.common

import assimp.AiMaterial
import assimp.AiMesh
import assimp.AiScene
import assimp.AiTexture
import glm_.set
import gln.draw.glDrawElements
import gln.get
import gln.glf.glf
import gln.glf.semantic
import gln.texture.glBindTexture
import gln.texture.initTexture2d
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import gln.vertexArray.withVertexArray
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
import java.nio.IntBuffer

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Mesh(assimpMesh: AiMesh, scene: AiScene) {

    val vao = intBufferBig(1)

    enum class Buffer { Vertex, Element }

    val buffers = intBufferBig<Buffer>()

    val indexCount: Int

    var diffuseMap: IntBuffer? = null
    var specularMap: IntBuffer? = null

    init {  // Now that we have all the required data, set the vertex buffers and its attribute pointers.

        // Create buffers/arrays
        glGenVertexArrays(vao)
        glGenBuffers(buffers)

        glBindVertexArray(vao)
        // Load data into vertex buffers
        glBindBuffer(GL_ARRAY_BUFFER, buffers[Buffer.Vertex])
        val vertexSize = 3 + 3 + 2
        val vertices = floatBufferBig(vertexSize * assimpMesh.numVertices)
        assimpMesh.vertices.forEachIndexed { i, v ->
            val n = assimpMesh.normals[i]
            v.to(vertices, i * vertexSize)
            n.to(vertices, i * vertexSize + 3)
            if (assimpMesh.textureCoords[0].isNotEmpty()) {
                val tc = assimpMesh.textureCoords[0][i]
                vertices[i * vertexSize + 3 + 3] = tc[0]
                vertices[i * vertexSize + 3 + 3 + 1] = tc[1]
            }
        }
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.Element])
        indexCount = assimpMesh.numFaces * 3
        val indices = intBufferBig(indexCount)
        repeat(indexCount) { indices[it] = assimpMesh.faces[it / 3][it % 3] }
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
        with(scene.materials[assimpMesh.materialIndex]) {

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
    fun loadMaterialTexture(assimpTex: AiMaterial.Texture, scene: AiScene) = initTexture2d {

        val gliTexture = scene.textures[assimpTex.file]!!

//        val format = gli.gl.translate(gliTexture.format, gliTexture.swizzles)
//        image(format.internal, gliTexture.)
//        glTexImage2D(format, gliTexture)
        image(gliTexture)
        glGenerateMipmap(GL_TEXTURE_2D)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }

    fun draw(diffuse: Boolean = false, specular: Boolean = false) {

        if (diffuse) diffuseMap?.let {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, it)
        }
        if (specular) specularMap?.let {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.SPECULAR)
            glBindTexture(GL_TEXTURE_2D, it)
        }

        // Draw mesh
        withVertexArray(vao) {
            glDrawElements(indexCount)
        }

        // Always good practice to set everything back to defaults once configured.
        if (diffuse) diffuseMap?.let {
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D)
        }
        if (specular) specularMap?.let {
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
