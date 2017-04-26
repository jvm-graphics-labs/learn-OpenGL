package learnOpenGL.A_gettingStarted

/**
 * Created by elect on 24/04/17.
 */

import glm.vec2.Vec2
import glm.vec3.Vec3
import learnOpenGL.common.*
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.*
import uno.buffer.*
import uno.glf.semantic
import uno.gln.glBindTexture
import uno.gln.glBindVertexArray
import uno.gln.glDrawElements
import uno.gln.glVertexAttribPointer

fun main(args: Array<String>) {

    with(learnOpenGL.A_gettingStarted.Textures()) {

        run()
        end()
    }
}

private class Textures {

    val window: learnOpenGL.common.GlfwWindow

    val ourShader: learnOpenGL.common.Shader

    object Buffer {
        val Vertex = 0
        val Element = 1
        val Max = 2
    }

    val buffers = uno.buffer.intBufferBig(Buffer.Max)
    val vao = uno.buffer.intBufferBig(1)

    val vertices = uno.buffer.floatBufferOf(
            // positions        // texture coords
            +0.5f, +0.5f, 0.0f, 1.0f, 1.0f, // top right
            +0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
            -0.5f, +0.5f, 0.0f, 0.0f, 1.0f  // top left
    )
    val indices = uno.buffer.intBufferOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    )

    val texture = uno.buffer.intBufferBig(1)

    init {

        with(learnOpenGL.common.glfw) {

            /*  Initialize GLFW. Most GLFW functions will not work before doing this.
                It also setups an error callback. The default implementation will print the error message in System.err.    */
            learnOpenGL.common.glfw.init()

            //  Configure GLFW
            learnOpenGL.common.glfw.windowHint {
                version = "3.3"
                profile = "core"
            }
        }

        //  glfw window creation
        window = learnOpenGL.common.GlfwWindow(800, 600, "Textures")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@Textures::framebuffer_size_callback
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        org.lwjgl.opengl.GL.createCapabilities()


        // build and compile our shader program
        ourShader = learnOpenGL.common.Shader("shaders/A_08", "texture") // you can name your shader files however you like


        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(buffers)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, buffers[learnOpenGL.A_gettingStarted.Textures.Buffer.Vertex])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[learnOpenGL.A_gettingStarted.Textures.Buffer.Element])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(uno.glf.semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEXCOORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(uno.glf.semantic.attr.TEXCOORD)


        // load and create a texture
        glGenTextures(texture)
        //  all upcoming GL_TEXTURE_2D operations now have effect on this texture object
        glBindTexture(GL_TEXTURE_2D, texture)
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        val image = learnOpenGL.common.readImage("textures/container.jpg")
        val data = image.toByteBuffer()

        learnOpenGL.common.glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    fun run() {

        //  render loop
        while (window.shouldNotClose) {

            //  input
            processInput(window)

            //  render
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT)

            // bind Texture
            glBindTexture(GL_TEXTURE_2D, texture)

            // render the triangle
            ourShader.use()
            glBindVertexArray(vao)
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            learnOpenGL.common.glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)

        uno.buffer.destroyBuffers(vao, buffers, vertices, indices)

        window.dispose()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        learnOpenGL.common.glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: learnOpenGL.common.GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.shouldClose = true
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }
}