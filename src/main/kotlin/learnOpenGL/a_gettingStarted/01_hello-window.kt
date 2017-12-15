package learnOpenGL.a_gettingStarted

import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glViewport
import uno.glfw.GlfwWindow
import uno.glfw.glfw


/**
 * Created by GBarbieri on 24.04.2017.
 */

fun main(args: Array<String>) {

    with(HelloWindow()) {
        run()
        end()
    }
}

private class HelloWindow {

    val window: GlfwWindow

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
        window = GlfwWindow(800, 600, "Hello Window").apply {
            // Make the OpenGL context current
            makeContextCurrent()
            // Make the window visible
            show()
            // glfw: whenever the window size changed (by OS or user resize) this callback function executes
            framebufferSizeCallback = { width, height ->
                /*  make sure the viewport matches the new window dimensions; note that width and height will be
                    significantly larger than specified on retina displays.     */
                glViewport(0, 0, width, height)
            }
        }
        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities
           instance and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()
    }

    fun run() {
        //  render loop
        while (window.open) {
            //  input
            window.processInput()
            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {
        window.destroy()
        glfw.terminate()
    }
}

/** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
fun GlfwWindow.processInput() {
    if (pressed(GLFW_KEY_ESCAPE)) close = true
}