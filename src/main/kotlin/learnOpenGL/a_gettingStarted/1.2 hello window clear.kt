package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 24.04.2017.
 */

import glm_.vec4.Vec4
import gln.glClearColor
import gln.glViewport
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import uno.glfw.GlfwWindow
import uno.glfw.glfw

fun main(args: Array<String>) {

    with(HelloWindowClear()) {
        run()
        end()
    }
}

private class HelloWindowClear {

    val window = initWindow("Hello Window Clear")

    fun run() {
        //  render loop
        while (window.open) {
            //  input
            window.processInput()
            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT)
            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapAndPoll()
        }
    }

    fun end() = window.end()

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    private fun GlfwWindow.processInput() {
        if (pressed(GLFW_KEY_ESCAPE)) close = true
    }
}

fun initWindow(title: String): GlfwWindow {
    with(glfw) {
        init()
        windowHint {
            context.version = "3.3"
            profile = "core"
        }
    }
    return GlfwWindow(windowSize, title).apply {
        makeContextCurrent()
        show()
        framebufferSizeCallback = { size -> glViewport(size) }
    }.also {
        GL.createCapabilities()
    }
}

val clearColor = Vec4(0.2f, 0.3f, 0.3f, 1f)


fun GlfwWindow.end() {
    destroy()
    glfw.terminate()
}

fun GlfwWindow.swapAndPoll() {
    swapBuffers()
    glfw.pollEvents()
}