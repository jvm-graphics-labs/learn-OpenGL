package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 13/05/17.
 */

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import gln.get
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
import gln.set
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.a_gettingStarted.verticesCube
import learnOpenGL.b_lighting.camera
import learnOpenGL.b_lighting.initWindow0
import learnOpenGL.b_lighting.processFrame
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

    with(CubemapsSkybox()) {
        run()
        end()
    }
}

private class CubemapsSkybox {

    val window = initWindow0("Framebuffers")

    val program = ProgramA()
    val skyboxProgram = ProgramSkybox()

    enum class Object { Cube, Skybox }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()

    enum class Tex { Cube, Cubemap }

    val tex = intBufferBig<Tex>()

    val skyboxVertices = floatArrayOf( // positions
            -1f, +1f, -1f,
            -1f, -1f, -1f,
            +1f, -1f, -1f,
            +1f, -1f, -1f,
            +1f, +1f, -1f,
            -1f, +1f, -1f,

            -1f, -1f, +1f,
            -1f, -1f, -1f,
            -1f, +1f, -1f,
            -1f, +1f, -1f,
            -1f, +1f, +1f,
            -1f, -1f, +1f,

            +1f, -1f, -1f,
            +1f, -1f, +1f,
            +1f, +1f, +1f,
            +1f, +1f, +1f,
            +1f, +1f, -1f,
            +1f, -1f, -1f,

            -1f, -1f, +1f,
            -1f, +1f, +1f,
            +1f, +1f, +1f,
            +1f, +1f, +1f,
            +1f, -1f, +1f,
            -1f, -1f, +1f,

            -1f, +1f, -1f,
            +1f, +1f, -1f,
            +1f, +1f, +1f,
            +1f, +1f, +1f,
            -1f, +1f, +1f,
            -1f, +1f, -1f,

            -1f, -1f, -1f,
            -1f, -1f, +1f,
            +1f, -1f, -1f,
            +1f, -1f, -1f,
            -1f, -1f, +1f,
            +1f, -1f, +1f)

    open inner class ProgramA : ProgramSkybox("cubemaps") {
        val model = glGetUniformLocation(name, "model")
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
                glBufferData(GL_ARRAY_BUFFER, verticesCube, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3_tc2)
                glVertexAttribPointer(glf.pos3_tc2)
            } else {
                glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW)
                glEnableVertexAttribArray(glf.pos3)
                glVertexAttribPointer(glf.pos3)
            }
            glBindVertexArray()
        }
        // load textures
        tex[Tex.Cube] = loadTexture("textures/marble.jpg")
        tex[Tex.Cubemap] = loadCubemap("textures/skybox", "jpg")
    }


    fun run() {

        while (window.open) {

            window.processFrame()


            glClearColor(0.1f, 0.1f, 0.1f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // draw scene as normal
            glUseProgram(program)
            val model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            glUniform(program.model, model)
            glUniform(program.view, view)
            glUniform(program.proj, projection)
            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, tex[Tex.Cube])
            glDrawArrays(GL_TRIANGLES, 0, 36)
            glBindVertexArray()

            // draw skybox as last
            glDepthFunc(GL_LEQUAL)  // change depth function so depth test passes when values are equal to depth buffer's content
            glUseProgram(skyboxProgram)
            view put camera.viewMatrix.toMat3().toMat4() // remove translation from the view matrix
            glUniform(skyboxProgram.view, view)
            glUniform(skyboxProgram.proj, projection)
            // skybox cube
            glBindVertexArray(vao[Object.Skybox])
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_CUBE_MAP, tex[Tex.Cubemap])
            glDrawArrays(GL_TRIANGLES, 0, 36)
            glBindVertexArray(0)
            glDepthFunc(GL_LESS) // set depth function back to default


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(program, skyboxProgram)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuf(vao, vbo, tex)

        window.end()
    }
}