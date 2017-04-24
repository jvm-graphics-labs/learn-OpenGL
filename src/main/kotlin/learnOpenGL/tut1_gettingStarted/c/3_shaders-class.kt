package learnOpenGL.tut1_gettingStarted.c

/**
 * Created by elect on 24/04/17.
 */

import glm.vec._3.Vec3
import learnOpenGL.common.GlfwWindow
import learnOpenGL.common.Shader
import learnOpenGL.common.glfw
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.semantic
import uno.gln.glBindBuffer
import uno.gln.glBindVertexArray
import uno.gln.glDrawArrays
import uno.gln.glVertexAttribPointer

fun main(args: Array<String>) {

    with(glfw) {

        /*  Initialize GLFW. Most GLFW functions will not work before doing this.
            It also setups an error callback. The default implementation will print the error message in System.err.    */
        init()

        //  Configure GLFW
        windowHint {
            version = "3.3"
            profile = "core"
        }
    }

    //  glfw window creation
    val window = GlfwWindow(800, 600, "Shaders Interpolation")

    with(window) {

        makeContextCurrent() // Make the OpenGL context current

        show()   // Make the window visible

        framebufferSizeCallback = ::framebuffer_size_callback
    }

    /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
       externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
       and makes the OpenGL bindings available for use.    */
    GL.createCapabilities()


    // build and compile our shader zprogram
    val ourShader = Shader("3.3.shader.vs", "3.3.shader.fs"); // you can name your shader files however you like


    //  set up vertex data (and buffer(s)) and configure vertex attributes
    val vertices = floatBufferOf(
            // positions        // colors
            +0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom left
            +0.0f, +0.5f, 0.0f, 0.0f, 0.0f, 1.0f  // top
    )

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)
    glGenVertexArrays(vao)
    glGenBuffers(vbo)
    //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

    //  position attribute
    glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, 2 * Vec3.SIZE, 0)
    glEnableVertexAttribArray(semantic.attr.POSITION)
    //  color attribute
    glVertexAttribPointer(semantic.attr.COLOR, Vec3.length, GL_FLOAT, false, 2 * Vec3.SIZE, Vec3.SIZE)
    glEnableVertexAttribArray(semantic.attr.COLOR)

    /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
        Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
        when it's not directly necessary.   */
    //glBindVertexArray()

    // as we only have a single shader, we could also just activate our shader once beforehand if we want to
    glUseProgram(shaderProgram)

    val start = System.nanoTime()

    //  render loop
    while (window.shouldNotClose) {

        //  input
        processInput(window)

        //  render
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        // render the triangle
        glDrawArrays(GL_TRIANGLES, 3)

        //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
        window.swapBuffers()
        glfw.pollEvents()
    }

    //  optional: de-allocate all resources once they've outlived their purpose:
    glDeleteVertexArrays(vao)
    glDeleteBuffers(vbo)

    destroyBuffers(vao, vbo, vertices)

    window.dispose()
    //  glfw: terminate, clearing all previously allocated GLFW resources.
    glfw.terminate()
}

/** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
private fun processInput(window: GlfwWindow) {

    if (window.key(GLFW_KEY_ESCAPE).pressed)
        window.shouldClose = true
}

/** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
private fun framebuffer_size_callback(width: Int, height: Int) {

    /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
        larger than specified on retina displays.     */
    glViewport(0, 0, width, height)
}