package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 07/05/2017.
 */

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.program.usingProgram
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
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(DepthTestingView()) {
        run()
        end()
    }
}

private class DepthTestingView {

    val window = initWindow0("Depth Testing View")

    val program: ProgramA

    enum class Object { Cube, Plane }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val tex = intBufferBig<Object>()

    init {

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        // build and compile our shader program
        program = ProgramA("shaders/d/_1_2", "depth-testing")

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
    }

    inner class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
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
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // cubes
            glBindVertexArray(vao[Object.Cube])
            model = model.translate(-1f, 0f, -1f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4().translate(2f, 0f, 0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            // floor
            glBindVertexArray(vao[Object.Plane])
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