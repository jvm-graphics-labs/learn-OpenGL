package learnOpenGL.common

import glm.i
import org.lwjgl.glfw.GLFW.*

object windowHint {

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