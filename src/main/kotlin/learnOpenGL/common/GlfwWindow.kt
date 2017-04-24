package learnOpenGL.common

import glm.vec._2.Vec2i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI
import org.lwjgl.system.MemoryUtil
import uno.buffer.destroyBuffers
import uno.buffer.intBufferBig

/**
 * Created by GBarbieri on 24.04.2017.
 */

class GlfwWindow(width: Int, height: Int, title: String) {

    constructor(windowSize: Vec2i, title: String) : this(windowSize.x, windowSize.y, title)
    constructor(x: Int, title: String) : this(x, x, title)

    private val x = intBufferBig(1)
    private val y = intBufferBig(1)

    val handle = glfwCreateWindow(width, height, title, 0L, 0L)
    var shouldClose = false
    val shouldNotClose
        get() = !shouldClose

    init {
        if (handle == MemoryUtil.NULL) {
            glfw.terminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
    }

    var pos = Vec2i()
        get() {
            glfwGetWindowPos(handle, x, y)
            return field.put(x[0], y[0])
        }
        set(value) = glfwSetWindowPos(handle, value.x, value.y)

    val size: Vec2i
        get() {
            glfwGetWindowSize(handle, x, y)
            return Vec2i(x[0], y[0])
        }

    fun makeContextCurrent() = glfwMakeContextCurrent(handle)

    fun dispose() {

        destroyBuffers(x, y)

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
    }

    fun show() = glfwShowWindow(handle)
    fun swapBuffers() = glfwSwapBuffers(handle)


    var framebufferSizeCallback: ((Int, Int) -> Unit)? = null
        set(value) {
            if (value == null)
                glfwSetFramebufferSizeCallback(handle, null)?.free()
            else
                glfwSetFramebufferSizeCallback(handle, framebufferSizeListener)?.free()
            field = value
        }
    private val framebufferSizeListener = FramebufferSizeListener()

    inner class FramebufferSizeListener : GLFWFramebufferSizeCallbackI {
        override fun invoke(window: Long, width: Int, height: Int) = framebufferSizeCallback!!.invoke(width, height)
    }

    fun key(key: Int): GlfwKey {
        GlfwKey.handle = handle
        GlfwKey.key = key
        return GlfwKey
    }
}

object GlfwKey {

    var handle = 0L
    var key = GLFW_KEY_0

    val pressed
        get() = glfwGetKey(handle, key) == GLFW_PRESS
    val released
        get() = glfwGetKey(handle, key) == GLFW_RELEASE
}