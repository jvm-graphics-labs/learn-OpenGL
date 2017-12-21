package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 27.04.2017.
 */

import glm_.f
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.uniform.glUniform
import gln.uniform.glUniform3
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.*
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glfw.GlfwWindow
import uno.glfw.GlfwWindow.Cursor.Disabled
import uno.glfw.glfw
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(Colors()) {
        run()
        end()
    }
}

// camera
val camera = Camera(position = Vec3(0f, 0f, 3f))
val last = Vec2d(windowSize) / 2

var firstMouse = true

var deltaTime = 0f    // time between current frame and last frame
var lastFrame = 0f

fun initWindow0(title: String) = initWindow(title).apply {

    cursorPosCallback = ::mouseCallback
    scrollCallback = { offset -> camera.processMouseScroll(offset.y.f) }

    cursor = Disabled
}

private class Colors {

    val window = initWindow0("Colors")

    val lighting = Lighting()
    val lamp = Lamp()

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    val vertices = floatArrayOf(
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

    // lighting
    val lightPos = Vec3(1.2f, 1f, 2f)

    inner class Lighting : Lamp("colors") {

        val objCol = glGetUniformLocation(name, "objectColor")
        val lgtCol = glGetUniformLocation(name, "lightColor")
    }

    inner open class Lamp(shader: String = "lamp") : Program("shaders/b/_1", "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

    init {

        glEnable(GL_DEPTH_TEST)


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        // position attribute
        glVertexAttribPointer(glf.pos3)
        glEnableVertexAttribArray(glf.pos3)

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        // we only need to bind to the VBO (to link it with glVertexAttribPointer), no need to fill it; the VBO's data already contains all we need (it's already bound, but we do it again for educational purposes)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        glVertexAttribPointer(glf.pos3[0])
        glEnableVertexAttribArray(glf.pos3[0])
    }

    fun run() {

        while (window.open) {

            window.processFrame()


            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting)

            glUniform(lighting.objCol, 1f, 0.5f, 0.31f)
            glUniform3(lighting.lgtCol, 1f)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            val view = camera.viewMatrix
            glUniform(lighting.proj, projection)
            glUniform(lighting.view, view)

            // world transformation
            var model = Mat4()
            glUniform(lighting.model, model)

            // render the cube
            glBindVertexArray(vao[VA.Cube])
            glDrawArrays(GL_TRIANGLES, 36)


            // also draw the lamp object
            glUseProgram(lamp)

            glUniform(lamp.proj, projection)
            glUniform(lamp.view, view)
            model = model
                    .translate(lightPos)
                    .scale(0.2f) // a smaller cube
            glUniform(lamp.model, model)

            glBindVertexArray(vao[VA.Light])
            glDrawArrays(GL_TRIANGLES, 36)


            window.swapAndPoll()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeletePrograms(lighting, lamp)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)

        destroyBuf(vao, vbo)

        window.end()
    }
}

/** process:
 *  - frame time logic
 *  - all input, query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
fun GlfwWindow.processFrame() {

    val currentFrame = glfw.time
    deltaTime = currentFrame - lastFrame
    lastFrame = currentFrame

    processInput()

    if (pressed(GLFW_KEY_W)) camera.processKeyboard(Forward, deltaTime)
    if (pressed(GLFW_KEY_S)) camera.processKeyboard(Backward, deltaTime)
    if (pressed(GLFW_KEY_A)) camera.processKeyboard(Left, deltaTime)
    if (pressed(GLFW_KEY_D)) camera.processKeyboard(Right, deltaTime)

    // TODO up/down?
}

fun mouseCallback(pos: Vec2d) {

    if (firstMouse) {
        last put pos
        firstMouse = false
    }

    val offset = Vec2d(pos.x - last.x, last.y - pos.y)
    last put pos

    offset *= 0.1

    camera processMouseMovement offset
}

val clearColor0 = Vec4(0.1f, 0.1f, 0.1f, 1f)