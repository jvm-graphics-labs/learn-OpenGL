package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 27.04.2017.
 */

import glm_.f
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.semantic
import gln.program.usingProgram
import gln.texture.glTexImage2D
import gln.texture.plus
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import learnOpenGL.common.flipY
import learnOpenGL.common.readImage
import learnOpenGL.common.toBuffer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.EXTABGR
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.buffer.use
import uno.glfw.GlfwWindow
import uno.glfw.GlfwWindow.Cursor.Disabled
import uno.glfw.glfw
import uno.glsl.Program
import uno.glsl.glDeleteProgram
import uno.glsl.usingProgram


fun main(args: Array<String>) {

    with(CameraClass()) {
        run()
        end()
    }
}

private class CameraClass {

    val window = initWindow("Camera Class")

    val program = ProgramA()

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    enum class Texture { A, B }

    val textures = intBufferBig<Texture>()

    // camera
    val camera = Camera(position = Vec3(0f, 0f, 3f))
    val last = Vec2d(windowSize) / 2

    var firstMouse = true

    var deltaTime = 0f
    var lastFrame = 0f

    inner class ProgramA : Program("shaders/a/_7_1", "camera.vert", "camera.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            /*  Tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
            Code passed to usingProgram() {..] is executed using the given program, which at the end gets unbound   */
            usingProgram(name) {
                "textureA".unitE = Texture.A
                "textureB".unitE = Texture.B
            }
        }
    }

    init {

        with(window) {
            cursorPosCallback = ::mouseCallback
            scrollCallback = { offset -> camera.processMouseScroll(offset.y.f) }

            cursor = Disabled
        }

        glEnable(GL_DEPTH_TEST)

        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEX_COORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)


        // load and create a texture
        glGenTextures(textures)

        //  texture A
        glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        var image = readImage("textures/container.jpg").flipY()
        image.toBuffer().use {
            glTexImage2D(GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }


        //  texture B
        glBindTexture(GL_TEXTURE_2D, textures[Texture.B])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        image = readImage("textures/awesomeface.png").flipY()
        image.toBuffer().use {
            glTexImage2D(GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }


        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    fun run() {

        while (window.open) {

            // per-frame time logic
            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            window.processInput0()

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // also clear the depth buffer now!

            //  bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + Texture.A)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + Texture.B)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            usingProgram(program) {

                // pass projection matrix to shader (note that in this case it could change every frame)
                val projection = glm.perspective(camera.zoom.rad, 800.0f / 600.0f, 0.1f, 100.0f)
                projection to program.proj

                // camera/view transformation
                val view = camera.viewMatrix
                view to program.view

                // render boxes
                glBindVertexArray(vao)
                cubePositions.forEachIndexed { i, vec3 ->

                    // calculate the model matrix for each object and pass it to shader before drawing
                    val model = Mat4() translate_ vec3
                    val angle = 20f * i
                    model.rotate_(angle.rad, 1f, 0.3f, 0.5f)
                    model to program.model

                    glDrawArrays(GL_TRIANGLES, 36)
                }
            }

            window.swapAndPoll()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)

        destroyBuf(vao, vbo, textures)

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

    /** glfw: whenever the mouse moves, this callback is called */
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
}