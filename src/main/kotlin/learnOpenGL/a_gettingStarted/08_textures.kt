package learnOpenGL.a_gettingStarted

/**
 * Created by elect on 24/04/17.
 */

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import learnOpenGL.common.*
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.*
import uno.glf.semantic
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.gln.*
import uno.glsl.Program

fun main(args: Array<String>) {

    with(learnOpenGL.a_gettingStarted.Textures()) {

        run()
        end()
    }
}

private class Textures {

    val window: GlfwWindow

    val program: Int

    object Buffer {
        val Vertex = 0
        val Element = 1
        val Max = 2
    }

    val buffers = intBufferBig(Buffer.Max)
    val vao = intBufferBig(1)

    val vertices = floatBufferOf(
            // positions        // texture coords
            +0.5f, +0.5f, 0.0f, 1.0f, 1.0f, // top right
            +0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
            -0.5f, +0.5f, 0.0f, 0.0f, 1.0f  // top left
    )
    val indices = intBufferOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    )

    val texture = intBufferBig(1)

    init {

        with(glfw) {

            /*  Initialize GLFW. Most GLFW functions will not work before doing this.
                It also setups an error callback. The default implementation will print the error message in System.err.    */
            init()

            //  Configure GLFW
            windowHint {
                context.version = "3.3"
                profile = "core"
            }
        }

        //  glfw window creation
        window = GlfwWindow(800, 600, "Textures")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@Textures::framebuffer_size_callback
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // build and compile our shader program
        program = ProgramA("shaders/a/_08", "texture").name


        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(buffers)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, buffers[Buffer.Vertex])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.Element])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(uno.glf.semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEX_COORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(uno.glf.semantic.attr.TEX_COORD)


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
        val image = readImage("textures/container.jpg")
        val data = image.toByteBuffer()

        // ByteBuffered images used BRG instead RGB
        glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {
        init {
            glUniform1i(
                    glGetUniformLocation(name, "textureA"),
                    semantic.sampler.DIFFUSE)
        }
    }

    fun run() {

        //  render loop
        while (window.open) {

            //  input
            processInput(window)

            //  render
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT)

            // bind Texture
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, texture)

            // render the triangle
            glUseProgram(program)
            glBindVertexArray(vao)
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)
        glDeleteTextures(texture)

        destroyBuffers(vao, buffers, texture, vertices, indices)

        window.destroy()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.close = true
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }
}