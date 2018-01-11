package learnOpenGL.d_advancedOpenGL

import glm_.L
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.set
import gln.buffer.glBindBuffer
import gln.buffer.glBindBufferRange
import gln.buffer.glBufferData
import gln.buffer.glBufferSubData
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
import gln.vertexArray.initVertexArray
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
import org.lwjgl.opengl.GL31.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(AdvancedGlslUbo()) {
        run()
        end()
    }
}

private class AdvancedGlslUbo {

    val window = initWindow0("Cubemaps Environment Mapping")

    val programRed = ProgramA("red.frag")
    val programGreen = ProgramA("green.frag")
    val programBlue = ProgramA("blue.frag")
    val programYellow = ProgramA("yellow.frag")

    val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f,
            +0.5f, -0.5f, -0.5f,
            +0.5f, +0.5f, -0.5f,
            +0.5f, +0.5f, -0.5f,
            -0.5f, +0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,

            -0.5f, -0.5f, +0.5f,
            +0.5f, -0.5f, +0.5f,
            +0.5f, +0.5f, +0.5f,
            +0.5f, +0.5f, +0.5f,
            -0.5f, +0.5f, +0.5f,
            -0.5f, -0.5f, +0.5f,

            -0.5f, +0.5f, +0.5f,
            -0.5f, +0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, +0.5f,
            -0.5f, +0.5f, +0.5f,

            +0.5f, +0.5f, +0.5f,
            +0.5f, +0.5f, -0.5f,
            +0.5f, -0.5f, -0.5f,
            +0.5f, -0.5f, -0.5f,
            +0.5f, -0.5f, +0.5f,
            +0.5f, +0.5f, +0.5f,

            -0.5f, -0.5f, -0.5f,
            +0.5f, -0.5f, -0.5f,
            +0.5f, -0.5f, +0.5f,
            +0.5f, -0.5f, +0.5f,
            -0.5f, -0.5f, +0.5f,
            -0.5f, -0.5f, -0.5f,

            -0.5f, +0.5f, -0.5f,
            +0.5f, +0.5f, -0.5f,
            +0.5f, +0.5f, +0.5f,
            +0.5f, +0.5f, +0.5f,
            -0.5f, +0.5f, +0.5f,
            -0.5f, +0.5f, -0.5f)

    val vao = intBufferBig(1)

    enum class Buffer {Vertex, Ubo}

    val bufferName = intBufferBig<Buffer>()

    open inner class ProgramA(fragment: String) : Program("shaders/d/_8", "advanced-glsl.vert", fragment) {
        val model = glGetUniformLocation(name, "model")
    }

    init {

        glEnable(GL_DEPTH_TEST)

        glGenBuffers(bufferName)

        initVertexArray(vao) { array(bufferName[Buffer.Vertex], glf.pos3) }

        // configure a uniform buffer object
        // ---------------------------------
        // first. We get the relevant block indices
        val uniformBlockIndexRed = glGetUniformBlockIndex(programRed.name, "Matrices")
        val uniformBlockIndexGreen = glGetUniformBlockIndex(programGreen.name, "Matrices")
        val uniformBlockIndexBlue = glGetUniformBlockIndex(programBlue.name, "Matrices")
        val uniformBlockIndexYellow = glGetUniformBlockIndex(programYellow.name, "Matrices")
        // then we link each shader's uniform block to this uniform binding point
        glUniformBlockBinding(programRed.name, uniformBlockIndexRed, semantic.uniform.TRANSFORM0)
        glUniformBlockBinding(programGreen.name, uniformBlockIndexGreen, semantic.uniform.TRANSFORM0)
        glUniformBlockBinding(programBlue.name, uniformBlockIndexBlue, semantic.uniform.TRANSFORM0)
        glUniformBlockBinding(programYellow.name, uniformBlockIndexYellow, semantic.uniform.TRANSFORM0)

        glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.Ubo])
        glBufferData(GL_UNIFORM_BUFFER, 2 * Mat4.size, GL_STATIC_DRAW)
        glBindBuffer(GL_UNIFORM_BUFFER)
        // define the range of the buffer that links to a uniform binding point
        glBindBufferRange(GL_UNIFORM_BUFFER, semantic.uniform.TRANSFORM0, bufferName[Buffer.Ubo], 0L, 2 * Mat4.size.L)

        // store the projection matrix (we only do this once now) (note: we're not using zoom anymore by changing the FoV)
        val projection = glm.perspective(45f, window.aspect, 0.1f, 100f)
        glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.Ubo])
        glBufferSubData(GL_UNIFORM_BUFFER, 0, projection)
        glBindBuffer(GL_UNIFORM_BUFFER)
    }


    fun run() {

        while (window.open) {

            window.processFrame()


            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // set the view and projection matrix in the uniform block - we only have to do this once per loop iteration.
            val view = camera.viewMatrix
            glBindBuffer(GL_UNIFORM_BUFFER, bufferName[Buffer.Ubo])
            glBufferSubData(GL_UNIFORM_BUFFER, Mat4.size, view)
            glBindBuffer(GL_UNIFORM_BUFFER)

            // draw 4 cubes
            // RED
            glBindVertexArray(vao)
            glUseProgram(programRed)
            var model = Mat4().translate(-0.75f, 0.75f, 0f) // move top-left
            glUniform(programRed.model, model)
            glDrawArrays(36)
            // GREEN
            glUseProgram(programGreen)
            model = Mat4().translate(0.75f, 0.75f, 0f) // move top-right
            glUniform(programGreen.model, model)
            glDrawArrays(36)
            // YELLOW
            glUseProgram(programBlue)
            model = Mat4().translate(-0.75f, -0.75f, 0f) // move bottom-left
            glUniform(programBlue.model, model)
            glDrawArrays( 36)
            // BLUE
            glUseProgram(programYellow)
            model = Mat4().translate(0.75f, -0.75f, 0f) // move bottom-right
            glUniform(programYellow.model, model)
            glDrawArrays(36)


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(programRed, programGreen, programBlue, programYellow)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(bufferName)

        destroyBuf(vao, bufferName)

        window.end()
    }
}