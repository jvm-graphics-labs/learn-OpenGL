package learnOpenGL.common

import glm.i
import glm.vec._2.Vec2i
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.system.MemoryUtil.NULL
import uno.buffer.destroyBuffers
import uno.buffer.intBufferBig

/**
 * Created by elect on 22/04/17.
 */

object glfw {

    fun init() {

        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")
    }

    fun windowHint(block: WindowHint.() -> Unit) = WindowHint.block()

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

object WindowHint {

    fun default() = glfwDefaultWindowHints()

    var resizable = true
        set(value) {
            glfwWindowHint(GLFW_RESIZABLE, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var visible = true
        set(value) {
            glfwWindowHint(GLFW_VISIBLE, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var srgb = true
        set(value) {
            glfwWindowHint(GLFW_SRGB_CAPABLE, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var decorated = true
        set(value) {
            glfwWindowHint(GLFW_DECORATED, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var api = ""
        set(value) {
            glfwWindowHint(GLFW_CLIENT_API, when (value) {
                "gl" -> GLFW_OPENGL_API
                "es" -> GLFW_OPENGL_ES_API
                else -> GLFW_NO_API
            })
            field = value
        }
    var version = ""
        set(value) {
            val major = value.substringBefore('.').i
            val minor = value.substringBefore('.').i
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor)
            field = value
        }
    var major = 0
        set(value) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, value)
            field = value
        }
    var minor = 0
        set(value) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, value)
            field = value
        }
    var profile = ""
        set(value) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, when (value) {
                "core" -> GLFW_OPENGL_CORE_PROFILE
                "compat" -> GLFW_OPENGL_COMPAT_PROFILE
                else -> GLFW_OPENGL_ANY_PROFILE
            })
            field = value
        }
    var forwardComp = true
        set(value) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var debug = true
        set(value) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
    var doubleBuffer = true
        set(value) {
            glfwWindowHint(GLFW_DOUBLEBUFFER, if (value) GLFW_TRUE else GLFW_FALSE)
            field = value
        }
}