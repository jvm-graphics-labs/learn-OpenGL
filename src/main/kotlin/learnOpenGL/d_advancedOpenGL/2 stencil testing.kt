package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 13/05/17.
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
import gln.set
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
import learnOpenGL.common.loadTexture
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


fun main(args: Array<String>) {

    with(StencilTesting()) {
        run()
        end()
    }
}

private class StencilTesting {

    val window = initWindow0("Depth Testing View")

    val program = ProgramB()
    val programSingleColor = ProgramA()

    enum class Object { Cube, Plane }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val tex = intBufferBig<Object>()

    inner open class ProgramA(vertex: String = "stencil-testing.vert", fragment: String = "stencil-single-color.frag") : Program("shaders/d/_2", vertex, fragment) {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

    inner class ProgramB(shader: String = "stencil-testing") : ProgramA("$shader.vert", "$shader.frag") {
        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE)


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

    fun run() {

        while (window.open) {

            window.processFrame()

            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT) // don't forget to clear the stencil buffer!

            // set uniforms
            glUseProgram(programSingleColor)
            var model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            glUniform(programSingleColor.proj, projection)
            glUniform(programSingleColor.view, view)

            glUseProgram(program)
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            /*  draw floor as normal, but don't write the floor to the stencil buffer, we only care about the containers.
                We set its mask to 0x00 to not write to the stencil buffer. */
            glStencilMask(0x00)
            // floor
            glBindVertexArray(vao[Object.Plane])
            glBindTexture(GL_TEXTURE_2D, tex[Object.Plane])
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 6)
            glBindVertexArray()

            // 1st. render pass, draw objects as normal, writing to the stencil buffer
            glStencilFunc(GL_ALWAYS, 1, 0xFF)
            glStencilMask(0xFF)
            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Object.Cube])
            model.translate_(-1f, 0f, -1f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4().translate_(2f, 0f, 0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)

            /*  2nd. render pass: now draw slightly scaled versions of the objects, this time disabling stencil writing.
                Because the stencil buffer is now filled with several 1s. The parts of the buffer that are 1 are not
                drawn, thus only drawing the objects' size differences, making it look like borders.    */
            glStencilFunc(GL_NOTEQUAL, 1, 0xFF)
            glStencilMask(0x00)
            glDisable(GL_DEPTH_TEST)
            glUseProgram(programSingleColor)
            val scale = 1.1f
            // cubes
            glBindVertexArray(vao[Object.Cube])
            model = Mat4()
                    .translate_(-1f, 0f, -1f)
                    .scale_(scale)
            glUniform(programSingleColor.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4()
                    .translate_(2f, 0f, 0f)
                    .scale_(scale)
            glUniform(programSingleColor.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            glBindVertexArray(0)
            glStencilMask(0xFF)
            glEnable(GL_DEPTH_TEST)


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(program, programSingleColor)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuf(vao, vbo, tex)

        window.end()
    }
}