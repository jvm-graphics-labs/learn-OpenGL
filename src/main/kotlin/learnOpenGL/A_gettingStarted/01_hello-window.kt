package learnOpenGL.A_gettingStarted

import learnOpenGL.common.GlfwWindow
import learnOpenGL.common.glfw
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glViewport

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
                version = "3.3"
                profile = "core"
            }
        }

        //  glfw window creation
        window = GlfwWindow(800, 600, "Hello Window")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback =  this@HelloWindow::framebuffer_size_callback
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities
           instance and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()
    }

    fun run() {

        //  render loop
        while (window.shouldNotClose) {

            //  input
            processInput(window)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        window.dispose()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.shouldClose = true
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be
            significantly larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }
}