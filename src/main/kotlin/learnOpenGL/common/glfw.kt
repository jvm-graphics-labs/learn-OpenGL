package learnOpenGL.common

import glm.vec._2.Vec2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode

/**
 * Created by elect on 22/04/17.
 */

object glfw {

    fun init() {

        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")
    }

    fun windowHint(block: windowHint.() -> Unit) = windowHint.block()

    val primaryMonitor
        get() = glfwGetPrimaryMonitor()

    val videoMode: GLFWVidMode
        get() = glfwGetVideoMode(primaryMonitor)

    fun videoMode(monitor: Long): GLFWVidMode = glfwGetVideoMode(monitor)

    val resolution
        get() = Vec2i(videoMode.width(), videoMode.height())

    var swapInterval = 0
        set(value) = glfwSwapInterval(value)

    fun terminate() {
        glfwTerminate()
        glfwSetErrorCallback(null).free()
    }

    fun pollEvents() = glfwPollEvents()
}

