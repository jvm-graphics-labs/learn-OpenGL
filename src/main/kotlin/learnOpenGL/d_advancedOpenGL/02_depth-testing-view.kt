package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 07/05/2017.
 */

import glm_.f
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.rad
import glm_.set
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import uno.glfw.GlfwWindow
import uno.glfw.GlfwWindow.Cursor.Disabled
import uno.glfw.glfw
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*
import uno.glsl.Program


fun main(args: Array<String>) {

    with(DepthTestingView()) {

        run()
        end()
    }
}

private class DepthTestingView {

    val window: GlfwWindow

    val program: ProgramA

    // settings
    val size = Vec2i(1280, 720)

    // camera
    val camera = Camera(position = Vec3(0.0f, 0.0f, 3.0f))
    var last = Vec2(size / 2)

    var firstMouse = true

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    object Object {
        val cube = 0
        val plane = 1
        val MAX = 2
    }

    val vao = intBufferBig(Object.MAX)
    val vbo = intBufferBig(Object.MAX)
    val tex = intBufferBig(Object.MAX)

    // set up vertex data (and buffer(s)) and configure vertex attributes
    // ------------------------------------------------------------------
    val cubeVertices = floatBufferOf(
            // positions         // texture Coords
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 1.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,

            -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f)

    val planeVertices = floatBufferOf(
            // positions         // texture Coords (note we set these higher than 1 (together with GL_REPEAT as texture
            //                      wrapping mode). this will cause the floor texture to repeat)
            +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, +5.0f, 0.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

            +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
            -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
            +5.0f, -0.5f, -5.0f, 2.0f, 2.0f)

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
        window = GlfwWindow(size, "Depth Testing View")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@DepthTestingView::framebuffer_size_callback
            cursorPosCallback = this@DepthTestingView::mouse_callback
            scrollCallback = this@DepthTestingView::scroll_callback

            // tell GLFW to capture our mouse
            cursor = Disabled
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // configure global opengl state
        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        // build and compile our shader program
        program = ProgramA("shaders/d/_02", "depth-testing")

        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in 0..Object.plane) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            glBufferData(GL_ARRAY_BUFFER, if (i == Object.cube) cubeVertices else planeVertices, GL_STATIC_DRAW)
            glEnableVertexAttribArray(glf.pos3_tc2)
            glVertexAttribPointer(glf.pos3_tc2)
            glEnableVertexAttribArray(glf.pos3_tc2[1])
            glVertexAttribPointer(glf.pos3_tc2[1])
            glBindVertexArray()
        }
    }

    inner class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(this) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    fun run() {

        //  render loop
        while (window.open) {

            // per-frame time logic
            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            //  input
            processInput(window)

            // render
            glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glUseProgram(program)
            var model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            glUniform(program.proj, projection)
            glUniform(program.view, view)

            // cubes
            glBindVertexArray(vao[Object.cube])
            model = model.translate(-1.0f, 0.0f, -1.0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4().translate(2.0f, 0.0f, 0.0f)
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            // floor
            glBindVertexArray(vao[Object.plane])
            glUniform(program.model, model)
            glDrawArrays(GL_TRIANGLES, 6)
            glBindVertexArray()

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        glDeletePrograms(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuffers(vao, vbo, tex, cubeVertices, planeVertices)

        window.destroy()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.pressed(GLFW_KEY_ESCAPE))
            window.close = true

        if (window.pressed(GLFW_KEY_W)) camera.processKeyboard(Forward, deltaTime)
        if (window.pressed(GLFW_KEY_S)) camera.processKeyboard(Backward, deltaTime)
        if (window.pressed(GLFW_KEY_A)) camera.processKeyboard(Left, deltaTime)
        if (window.pressed(GLFW_KEY_D)) camera.processKeyboard(Right, deltaTime)

        // TODO up/down?
    }

    /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
    fun framebuffer_size_callback(width: Int, height: Int) {

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }

    /** glfw: whenever the mouse moves, this callback is called */
    fun mouse_callback(xpos: Double, ypos: Double) {

        if (firstMouse) {
            last.x = xpos.f
            last.y = ypos.f
            firstMouse = false
        }

        var xoffset = xpos - last.x
        var yoffset = last.y - ypos // reversed since y-coordinates go from bottom to top
        last.x = xpos.f
        last.y = ypos.f

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        camera.processMouseMovement(xoffset.f, yoffset.f)
    }

    /** glfw: whenever the mouse scroll wheel scrolls, this callback is called  */
    fun scroll_callback(xOffset: Double, yOffset: Double) = camera.processMouseScroll(yOffset.f)
}