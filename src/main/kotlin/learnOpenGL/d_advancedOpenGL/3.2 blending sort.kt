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
import uno.glsl.glDeleteProgram
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(BlendingSort()) {
        run()
        end()
    }
}

private class BlendingSort {

    val window = initWindow0("Blending Sort")

    val program = ProgramA()

    enum class Object { Cube, Plane, Transparent }

    val vao = intBufferBig<Object>()
    val vbo = intBufferBig<Object>()
    val tex = intBufferBig<Object>()

    inner open class ProgramA : Program("shaders/d/_3_1", "blending.vert", "blending.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(name) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    init {

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in Object.values()) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            glBufferData(GL_ARRAY_BUFFER, verticesCube2[i.ordinal], GL_STATIC_DRAW)
            glEnableVertexAttribArray(glf.pos3_tc2)
            glVertexAttribPointer(glf.pos3_tc2)
            glBindVertexArray()
        }

        // load textures
        tex[Object.Cube] = loadTexture("textures/marble.jpg")
        tex[Object.Plane] = loadTexture("textures/metal.png")
        tex[Object.Transparent] = loadTexture("textures/window.png")
    }

    fun run() {

        while (window.open) {

            window.processFrame()

            // sort the transparent windows before rendering
            val sorted = positionCube2.sortedByDescending { glm.length(camera.position - it) }

            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // draw objects
            glUseProgram(program)
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            val view = camera.viewMatrix
            var model = Mat4()
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // cubes
            glBindVertexArray(vao[Object.Cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Object.Cube])
            model.translate_(-1f, 0f, -1f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4()
                    .translate_(2f, 0f, 0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)

            // floor
            glBindVertexArray(vao[Object.Plane])
            glBindTexture(GL_TEXTURE_2D, tex[Object.Plane])
            model = Mat4()
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 6)

            // windows (from furthest to nearest)
            glBindVertexArray(vao[Object.Transparent])
            glBindTexture(GL_TEXTURE_2D, tex[Object.Transparent])
            sorted.forEach {
                model = Mat4().translate_(it)
                glUniform(program.model, model)
                glDrawArrays(GL_TRIANGLES, 6)
            }


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuf(vao, vbo, tex)

        window.end()
    }
}