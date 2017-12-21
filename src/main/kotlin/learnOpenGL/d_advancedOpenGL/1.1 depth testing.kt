package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 06/05/2017.
 */

import gli_.gli
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
import gln.set
import gln.texture.glTexImage2D
import gln.uniform.glUniform
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.a_gettingStarted.verticesCube
import learnOpenGL.b_lighting.camera
import learnOpenGL.b_lighting.clearColor0
import learnOpenGL.b_lighting.initWindow0
import learnOpenGL.b_lighting.processFrame
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram
import uno.kotlin.uri


fun main(args: Array<String>) {

    with(DepthTesting()) {
        run()
        end()
    }
}

val planeVertices = floatArrayOf(
        /* positions        | texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture
                                                wrapping mode). this will cause the floor texture to repeat)    */
        +5f, -0.5f, +5f, 2f, 0f,
        -5f, -0.5f, +5f, 0f, 0f,
        -5f, -0.5f, -5f, 0f, 2f,

        +5f, -0.5f, +5f, 2f, 0f,
        -5f, -0.5f, -5f, 0f, 2f,
        +5f, -0.5f, -5f, 2f, 2f)

private class DepthTesting {

    val window = initWindow0("Depth Testing")

    val program = ProgramA()

    enum class Object { Cube, Plane }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val tex = intBufferBig<Object>()

    inner class ProgramA : Program("shaders/d/_1_1", "depth-testing.vert", "depth-testing.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_ALWAYS) // always pass the depth test (same effect as glDisable(GL_DEPTH_TEST))

        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in Object.values()) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            glBufferData(GL_ARRAY_BUFFER, if (i == Object.Cube) verticesCube else planeVertices, GL_STATIC_DRAW)
            glEnableVertexAttribArray(glf.pos3_tc2)
            glVertexAttribPointer(glf.pos3_tc2)
            glBindVertexArray()
        }

        // load textures
        tex[Object.Cube] = loadTexture("textures/marble.jpg")
        tex[Object.Plane] = loadTexture("textures/metal.png")
    }

    fun loadTexture(path: String): Int {

        val textureID = glGenTextures()

        val texture = gli.load(path.uri)
        val format = gli.gl.translate(texture.format, texture.swizzles)
        val extent = texture.extent()

        glBindTexture(GL_TEXTURE_2D, textureID)
        glTexImage2D(format.internal, extent.x, extent.y, format.external, format.type, texture.data())
        glGenerateMipmap(GL_TEXTURE_2D)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        texture.dispose()

        return textureID
    }

    fun run() {

        while (window.open) {

            window.processFrame()

            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glUseProgram(program)
            var model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Object.Cube])
            model = model.translate(-1f, 0f, -1f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4().translate(2f, 0f, 0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            // floor
            glBindVertexArray(vao[Object.Plane])
            glBindTexture(GL_TEXTURE_2D, tex[Object.Plane])
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 6)
            glBindVertexArray()


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuf(vao, vbo, tex)

        window.end()
    }
}