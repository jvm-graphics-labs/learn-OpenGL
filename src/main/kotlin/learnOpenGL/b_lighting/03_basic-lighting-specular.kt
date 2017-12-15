package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 28.04.2017.
 */

import glm_.f
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
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
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glfw.GlfwWindow
import uno.glfw.GlfwWindow.Cursor.Disabled
import uno.glfw.glfw
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram


fun main(args: Array<String>) {

    with(BasicLightingSpecular()) {
        run()
        end()
    }
}

private class BasicLightingSpecular {

    val window = initWindow("Basic Lighting Specular")

    val lighting: Lighting
    val lamp: Lamp

    val vbo = intBufferBig(1)

    object VA {
        val Cube = 0
        val Light = 1
        val Max = 2
    }

    val vao = intBufferBig(VA.Max)

    val camera = Camera(position = Vec3(0f, 0f, 3f))
    var last = Vec2d(800, 600) / 2

    var firstMouse = true

    var deltaTime = 0f    // time between current frame and last frame
    var lastFrame = 0f

    val lightPos = Vec3(1.2f, 1f, 2f)

    init {

        with(window) {
            cursorPosCallback = ::mouseCallback
            scrollCallback = { _, yOffset -> camera.processMouseScroll(yOffset.f) }

            cursor = Disabled
        }

        glEnable(GL_DEPTH_TEST)


        // build and compile our shader program
        lighting = Lighting("shaders/b/_03", "basic-lighting")
        lamp = Lamp("shaders/b/_01", "lamp")


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube0, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3)
        glEnableVertexAttribArray(glf.pos3_nor3)

        glVertexAttribPointer(glf.pos3_nor3[1])
        glEnableVertexAttribArray(glf.pos3_nor3[1])

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3)
        glEnableVertexAttribArray(glf.pos3_nor3)
    }

    inner class Lighting(root: String, shader: String) : Lamp(root, shader) {

        val objCol = glGetUniformLocation(name, "objectColor")
        val lgtCol = glGetUniformLocation(name, "lightColor")
        val lgtPos = glGetUniformLocation(name, "lightPos")
        val viewPos = glGetUniformLocation(name, "viewPos")
    }

    inner open class Lamp(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

    fun run() {

        while (window.open) {

            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            window.processInput0()


            // render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting)

            glUniform(lighting.objCol, 1f, 0.5f, 0.31f)
            /*  we can avoid to write this
                glUniform(lighting.lgtCol, 1.0f, 1.0f, 1.0f)
                but we have to specify explicit the dimensionality with 3*/
            glUniform3(lighting.lgtCol, 1f)
            glUniform(lighting.lgtPos, lightPos)
            glUniform(lighting.viewPos, camera.position)

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

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun GlfwWindow.processInput0() {

        processInput()

        if (pressed(GLFW_KEY_W)) camera.processKeyboard(Forward, deltaTime)
        if (pressed(GLFW_KEY_S)) camera.processKeyboard(Backward, deltaTime)
        if (pressed(GLFW_KEY_A)) camera.processKeyboard(Left, deltaTime)
        if (pressed(GLFW_KEY_D)) camera.processKeyboard(Right, deltaTime)

        // TODO up/down?
    }

    fun mouseCallback(xpos: Double, ypos: Double) {

        if (firstMouse) {
            last.put(xpos, ypos)
            firstMouse = false
        }

        var xoffset = xpos - last.x
        var yoffset = last.y - ypos // reversed since y-coordinates go from bottom to top
        last.put(xpos, ypos)

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        camera.processMouseMovement(xoffset.f, yoffset.f)
    }
}