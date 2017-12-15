package learnOpenGL.b_lighting

/**
 * Created by elect on 29/04/17.
 */

import glm_.f
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.set
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.texture.glBindTexture
import gln.uniform.glUniform
import gln.uniform.glUniform3
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.*
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import learnOpenGL.common.loadTexture
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUseProgram
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

    with(LightingMapsDiffuse()) {
        run()
        end()
    }
}

val verticesCube1 = floatArrayOf(
        // positions       | normals      | texture coords
        -0.5f, -0.5f, -0.5f, +0f, +0f, -1f, 0f, 0f,
        +0.5f, -0.5f, -0.5f, +0f, +0f, -1f, 1f, 0f,
        +0.5f, +0.5f, -0.5f, +0f, +0f, -1f, 1f, 1f,
        +0.5f, +0.5f, -0.5f, +0f, +0f, -1f, 1f, 1f,
        -0.5f, +0.5f, -0.5f, +0f, +0f, -1f, 0f, 1f,
        -0.5f, -0.5f, -0.5f, +0f, +0f, -1f, 0f, 0f,

        -0.5f, -0.5f, +0.5f, +0f, +0f, +1f, 0f, 0f,
        +0.5f, -0.5f, +0.5f, +0f, +0f, +1f, 1f, 0f,
        +0.5f, +0.5f, +0.5f, +0f, +0f, +1f, 1f, 1f,
        +0.5f, +0.5f, +0.5f, +0f, +0f, +1f, 1f, 1f,
        -0.5f, +0.5f, +0.5f, +0f, +0f, +1f, 0f, 1f,
        -0.5f, -0.5f, +0.5f, +0f, +0f, +1f, 0f, 0f,

        -0.5f, +0.5f, +0.5f, -1f, +0f, +0f, 1f, 0f,
        -0.5f, +0.5f, -0.5f, -1f, +0f, +0f, 1f, 1f,
        -0.5f, -0.5f, -0.5f, -1f, +0f, +0f, 0f, 1f,
        -0.5f, -0.5f, -0.5f, -1f, +0f, +0f, 0f, 1f,
        -0.5f, -0.5f, +0.5f, -1f, +0f, +0f, 0f, 0f,
        -0.5f, +0.5f, +0.5f, -1f, +0f, +0f, 1f, 0f,

        +0.5f, +0.5f, +0.5f, +1f, +0f, +0f, 1f, 0f,
        +0.5f, +0.5f, -0.5f, +1f, +0f, +0f, 1f, 1f,
        +0.5f, -0.5f, -0.5f, +1f, +0f, +0f, 0f, 1f,
        +0.5f, -0.5f, -0.5f, +1f, +0f, +0f, 0f, 1f,
        +0.5f, -0.5f, +0.5f, +1f, +0f, +0f, 0f, 0f,
        +0.5f, +0.5f, +0.5f, +1f, +0f, +0f, 1f, 0f,

        -0.5f, -0.5f, -0.5f, +0f, -1f, +0f, 0f, 1f,
        +0.5f, -0.5f, -0.5f, +0f, -1f, +0f, 1f, 1f,
        +0.5f, -0.5f, +0.5f, +0f, -1f, +0f, 1f, 0f,
        +0.5f, -0.5f, +0.5f, +0f, -1f, +0f, 1f, 0f,
        -0.5f, -0.5f, +0.5f, +0f, -1f, +0f, 0f, 0f,
        -0.5f, -0.5f, -0.5f, +0f, -1f, +0f, 0f, 1f,

        -0.5f, +0.5f, -0.5f, +0f, +1f, +0f, 0f, 1f,
        +0.5f, +0.5f, -0.5f, +0f, +1f, +0f, 1f, 1f,
        +0.5f, +0.5f, +0.5f, +0f, +1f, +0f, 1f, 0f,
        +0.5f, +0.5f, +0.5f, +0f, +1f, +0f, 1f, 0f,
        -0.5f, +0.5f, +0.5f, +0f, +1f, +0f, 0f, 0f,
        -0.5f, +0.5f, -0.5f, +0f, +1f, +0f, 0f, 1f)

private class LightingMapsDiffuse {

    val window = initWindow("Lighting Maps Diffuse")

    val lighting: Lighting
    val lamp: Lamp

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    // camera
    val camera = Camera(position = Vec3(0f, 0f, 3f))
    var last = Vec2d(800, 600) / 2

    var firstMouse = true

    var deltaTime = 0f    // time between current frame and last frame
    var lastFrame = 0f

    val lightPos = Vec3(1.2f, 1f, 2f)

    val diffuseMap = intBufferBig(1)

    init {

        with(window) {
            cursorPosCallback = ::mouseCallback
            scrollCallback = { _, yOffset -> camera.processMouseScroll(yOffset.f) }

            cursor = Disabled
        }

        glEnable(GL_DEPTH_TEST)


        // build and compile our shader program
        lighting = Lighting("shaders/b/_05", "lighting-maps")
        lamp = Lamp("shaders/b/_01", "lamp")


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube1, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3_tc2)
        glEnableVertexAttribArray(glf.pos3_nor3_tc2)

        glVertexAttribPointer(glf.pos3_nor3_tc2[1])
        glEnableVertexAttribArray(glf.pos3_nor3_tc2[1])

        glVertexAttribPointer(glf.pos3_nor3_tc2[2])
        glEnableVertexAttribArray(glf.pos3_nor3_tc2[2])

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3_tc2)
        glEnableVertexAttribArray(glf.pos3_nor3_tc2)

        // load textures (we now use a utility function to keep the code more organized)
        diffuseMap[0] = loadTexture("textures/container2.png")
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
            glUniform3(lighting.lgt.ambient, 0.2f)
            glUniform3(lighting.lgt.diffuse, 0.5f)
            glUniform3(lighting.lgt.specular, 1f)

            // material properties
            glUniform3(lighting.mtl.specular, 0.5f)
            glUniform(lighting.mtl.shininess, 64f)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100f)
            val view = camera.viewMatrix
            glUniform(lighting.proj, projection)
            glUniform(lighting.view, view)

            // world transformation
            var model = Mat4()
            glUniform(lighting.model, model)

            // bind diffuse map
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, diffuseMap)

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
        glDeleteTextures(diffuseMap)

        destroyBuf(vao, vbo, diffuseMap)

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