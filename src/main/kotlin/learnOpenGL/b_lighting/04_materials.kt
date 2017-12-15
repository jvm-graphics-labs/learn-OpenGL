package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 28.04.2017.
 */

import glm_.f
import glm_.func.rad
import glm_.glm
import glm_.glm.sin
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

    with(Materials()) {

        run()
        end()
    }
}

private class Materials {

    val window = initWindow("Materials")

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

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    // lighting
    val lightPos = Vec3(1.2f, 1.0f, 2.0f)

    init {

        with(window) {
            cursorPosCallback = ::mouseCallback
            scrollCallback = { _, yOffset -> camera.processMouseScroll(yOffset.f) }

            cursor = Disabled
        }

        glEnable(GL_DEPTH_TEST)


        // build and compile our shader program
        lighting = Lighting("shaders/b/_04", "materials")
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

        val viewPos = glGetUniformLocation(name, "viewPos")
        val lgt = Light()
        val mtl = Material()

        inner class Light {
            val pos = glGetUniformLocation(name, "light.position")
            val ambient = glGetUniformLocation(name, "light.ambient")
            val diffuse = glGetUniformLocation(name, "light.diffuse")
            val specular = glGetUniformLocation(name, "light.specular")
        }

        inner class Material {
            val ambient = glGetUniformLocation(name, "material.ambient")
            val diffuse = glGetUniformLocation(name, "material.diffuse")
            val specular = glGetUniformLocation(name, "material.specular")
            val shininess = glGetUniformLocation(name, "material.shininess")
        }
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

            glUniform(lighting.lgt.pos, lightPos)
            glUniform(lighting.viewPos, camera.position)

            // light properties
            val lightColor = Vec3(
                    x = sin(glfw.time * 2f),
                    y = sin(glfw.time * 0.7f),
                    z = sin(glfw.time * 1.3f))
            val diffuseColor = lightColor * 0.5f    // decrease the influence
            val ambientColor = diffuseColor * 0.2f  // low influence
            glUniform(lighting.lgt.ambient, ambientColor)
            glUniform(lighting.lgt.diffuse, diffuseColor)
            glUniform3(lighting.lgt.specular, 1f)

            // material properties
            glUniform(lighting.mtl.ambient, 1f, 0.5f, 0.31f)
            glUniform(lighting.mtl.diffuse, 1f, 0.5f, 0.31f)
            glUniform3(lighting.mtl.specular, 0.5f)
            glUniform(lighting.mtl.shininess, 32f)

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

        glDeletePrograms(lighting, lamp)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)

        destroyBuf(vao, vbo)

        window.end()
    }

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