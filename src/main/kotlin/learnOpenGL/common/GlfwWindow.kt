package learnOpenGL.common

import glm.bool
import glm.f
import glm.i
import glm.vec2.Vec2i
import glm.vec4.Vec4i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallbackI
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI
import org.lwjgl.glfw.GLFWScrollCallbackI
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
    private val z = intBufferBig(1)
    private val w = intBufferBig(1)

    val handle = glfwCreateWindow(width, height, title, 0L, 0L)

    init {
        if (handle == MemoryUtil.NULL) {
            glfw.terminate()
            throw RuntimeException("Failed to create the GLFW window")
        }
    }

    var close = false
    val open get() = !close

    var title = title
        set(value) = glfwSetWindowTitle(handle, value)

    // TODO icon

    var pos = Vec2i()
        get() {
            glfwGetWindowPos(handle, x, y)
            return field.put(x[0], y[0])
        }
        set(value) {
            glfwSetWindowPos(handle, value.x, value.y)
            field.put(value)
        }

    var size: Vec2i
        get() {
            glfwGetWindowSize(handle, x, y)
            return Vec2i(x[0], y[0])
        }
        set(value) {
            glfwSetWindowSize(handle, value.x, value.y)
        }

    fun sizeLimit(width: IntRange, height: IntRange) = glfwSetWindowSizeLimits(handle, width.start, height.start, width.endInclusive, height.endInclusive)

    fun aspect(numer: Int, denom: Int) = glfwSetWindowAspectRatio(handle, numer, denom)

    var aspect
        get() = size.x / size.y.f
        set(value) = glfwSetWindowAspectRatio(handle, (value * 1_000).i, 1_000)

    val framebufferSize: Vec2i
        get() {
            glfwGetFramebufferSize(handle, x, y)
            return Vec2i(x, y)
        }

    val frameSize: Vec4i
        get() {
            glfwGetWindowFrameSize(handle, x, y, z, w)
            return Vec4i(x[0], y[0], z[0], w[0])
        }

    fun iconify() = glfwIconifyWindow(handle)
    fun restore() = glfwRestoreWindow(handle)
    fun maximize() = glfwMaximizeWindow(handle)
    fun show() = glfwShowWindow(handle)
    fun hide() = glfwHideWindow(handle)
    fun focus() = glfwFocusWindow(handle)

    val monitor get() = glfwGetWindowMonitor(handle)
    fun monitor(monitor: Long, xPos: Int, yPos: Int, width: Int, height: Int) =
            monitor(monitor, xPos, yPos, width, height, GLFW_DONT_CARE)
    fun monitor(monitor: Long, xPos: Int, yPos: Int, width: Int, height: Int, refreshRate: Int) =
            glfwSetWindowMonitor(handle, monitor, xPos, yPos, width, height, refreshRate)

    val focused get() = glfwGetWindowAttrib(handle, GLFW_FOCUSED).bool
    val iconified get() = glfwGetWindowAttrib(handle, GLFW_ICONIFIED).bool
    val maximized get() = glfwGetWindowAttrib(handle, GLFW_MAXIMIZED).bool
    val visible get() = glfwGetWindowAttrib(handle, GLFW_VISIBLE).bool
    val resizable get() = glfwGetWindowAttrib(handle, GLFW_RESIZABLE).bool
    val decorated get() = glfwGetWindowAttrib(handle, GLFW_DECORATED).bool
    val floating get() = glfwGetWindowAttrib(handle, GLFW_FLOATING).bool



    fun makeContextCurrent() = glfwMakeContextCurrent(handle)

    fun destroy() {

        destroyBuffers(x, y)

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
    }


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


    var cursorPosCallback: ((Double, Double) -> Unit)? = null
        set(value) {
            if (value == null)
                glfwSetCursorPosCallback(handle, null)?.free()
            else
                glfwSetCursorPosCallback(handle, cursorPosListener)?.free()
            field = value
        }
    private val cursorPosListener = CursorPosListener()

    inner class CursorPosListener : GLFWCursorPosCallbackI {
        override fun invoke(window: Long, xPos: Double, yPos: Double) = cursorPosCallback!!.invoke(xPos, yPos)
    }


    var scrollCallback: ((Double, Double) -> Unit)? = null
        set(value) {
            if (value == null)
                glfwSetScrollCallback(handle, null)?.free()
            else
                glfwSetScrollCallback(handle, scrollListener)?.free()
            field = value
        }
    private val scrollListener = ScrollListener()

    inner class ScrollListener : GLFWScrollCallbackI {
        override fun invoke(window: Long, xOffset: Double, yOffset: Double) = scrollCallback!!.invoke(xOffset, yOffset)
    }


    var cursor: Cursor
        get() = when (glfwGetInputMode(handle, GLFW_CURSOR)) {
            GLFW_CURSOR_NORMAL -> Cursor.Normal
            GLFW_CURSOR_HIDDEN -> Cursor.Hidden
            GLFW_CURSOR_DISABLED -> Cursor.Disabled
            else -> throw Error()
        }
        set(value) = glfwSetInputMode(handle, GLFW_CURSOR, when (value) {
            Cursor.Normal -> GLFW_CURSOR_NORMAL
            Cursor.Hidden -> GLFW_CURSOR_HIDDEN
            Cursor.Disabled -> GLFW_CURSOR_DISABLED
            else -> throw Error()
        })

    enum class Cursor {Normal, Hidden, Disabled }


    fun pressed(key: Int) = glfwGetKey(handle, key) == GLFW_PRESS
    fun released(key: Int) = glfwGetKey(handle, key) == GLFW_PRESS
}