package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 13/05/17.
 */

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.set
import gln.draw.glDrawArrays
import gln.get
import gln.set
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
import gln.texture.glBindTexture
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.a_gettingStarted.verticesCube
import learnOpenGL.b_lighting.*
import learnOpenGL.common.loadCubemap
import learnOpenGL.common.loadTexture
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(CubemapsEnvironmentMapping()) {
        run()
        end()
    }
}

private class CubemapsEnvironmentMapping {

    val window = initWindow0("Cubemaps Environment Mapping")

    val program = ProgramA()
    val skyboxProgram = ProgramSkybox()

    enum class Object { Cube, Skybox }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val texCubemap = intBufferBig(1)

    open inner class ProgramA : Program("shaders/d/_6_2", "cubemaps.vert", "cubemaps.frag") {
        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
        val cameraPos = glGetUniformLocation(name, "cameraPos")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    open inner class ProgramSkybox(shader: String = "skybox") : Program("shaders/d/_6_1", "$shader.vert", "$shader.frag") {
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {

        glEnable(GL_DEPTH_TEST)

        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in Object.values()) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            if (i == Object.Cube) {
                glBufferData(GL_ARRAY_BUFFER, verticesCube0, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3_nor3)
                glVertexAttribPointer(glf.pos3_nor3)
            } else {
                glBufferData(GL_ARRAY_BUFFER, verticesSkybox, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3)
                glVertexAttribPointer(glf.pos3)
            }
            glBindVertexArray()
        }
        // load textures
        texCubemap[0] = loadCubemap("textures/skybox", "jpg")
    }


    fun run() {

        while (window.open) {

            window.processFrame()


            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // draw scene as normal
            glUseProgram(program)
            val model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            glUniform(program.model, model)
            glUniform(program.view, view)
            glUniform(program.proj, projection)
            glUniform(program.cameraPos, camera.position)
            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_CUBE_MAP, texCubemap)
            glDrawArrays(GL_TRIANGLES, 36)
            glBindVertexArray()

            // draw skybox as last
            glDepthFunc(GL_LEQUAL)  // change depth function so depth test passes when values are equal to depth buffer's content
            glUseProgram(skyboxProgram)
            view put camera.viewMatrix.toMat3().toMat4() // remove translation from the view matrix
            glUniform(skyboxProgram.view, view)
            glUniform(skyboxProgram.proj, projection)
            // skybox cube
            glBindVertexArray(vao[Object.Skybox])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_CUBE_MAP, texCubemap)
            glDrawArrays(GL_TRIANGLES, 36)
            glBindVertexArray()
            glDepthFunc(GL_LESS) // set depth function back to default


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(program, skyboxProgram)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(texCubemap)

        destroyBuf(vao, vbo, texCubemap)

        window.end()
    }
}