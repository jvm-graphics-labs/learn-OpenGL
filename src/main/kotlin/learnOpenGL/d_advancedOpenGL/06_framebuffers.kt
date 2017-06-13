package learnOpenGL.d_advancedOpenGL

/**
 * Created by elect on 13/05/17.
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
import learnOpenGL.common.loadTexture
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.NULL
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*
import uno.glsl.Program


fun main(args: Array<String>) {

    with(Framebuffers()) {

        run()
        end()
    }
}

private class Framebuffers {

    val window: GlfwWindow

    val programRender: ProgramRender
    val programSplash: ProgramSplash

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
        val screen = 2
        val MAX = 3
    }

    val vao = intBufferBig(Object.MAX)
    val vbo = intBufferBig(Object.MAX)


    object Tex {
        val cube = 0
        val plane = 1
        val colorBuffer = 2
        val MAX = 3
    }

    val tex = intBufferBig(Tex.MAX)
    var rbo = intBufferBig(1)
    val framebuffer = intBufferBig(1)

    // set up vertex data (and buffer(s)) and configure vertex attributes
    // ------------------------------------------------------------------
    val vertices = arrayOf(

            floatBufferOf(
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
                    -0.5f, +0.5f, -0.5f, 0.0f, 1.0f),

            floatBufferOf(
                    // positions         // texture Coords
                    +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
                    -5.0f, -0.5f, +5.0f, 0.0f, 0.0f,
                    -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,

                    +5.0f, -0.5f, +5.0f, 2.0f, 0.0f,
                    -5.0f, -0.5f, -5.0f, 0.0f, 2.0f,
                    +5.0f, -0.5f, -5.0f, 2.0f, 2.0f),

            // vertex attributes for a quad that fills the entire screen in Normalized Device Coordinates.
            floatBufferOf(
                    // positions  // texCoords
                    -1.0f, +1.0f, 0.0f, 1.0f,
                    -1.0f, -1.0f, 0.0f, 0.0f,
                    +1.0f, -1.0f, 1.0f, 0.0f,

                    -1.0f, +1.0f, 0.0f, 1.0f,
                    +1.0f, -1.0f, 1.0f, 0.0f,
                    +1.0f, +1.0f, 1.0f, 1.0f))

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
        window = GlfwWindow(size, "Blending Sort")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@Framebuffers::framebuffer_size_callback
            cursorPosCallback = this@Framebuffers::mouse_callback
            scrollCallback = this@Framebuffers::scroll_callback

            // tell GLFW to capture our mouse
            cursor = Disabled
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // configure global opengl state
        glEnable(GL_DEPTH_TEST)

        // build and compile our shader program
        programRender = ProgramRender("shaders/d/_06", "framebuffers")
        programSplash = ProgramSplash("shaders/d/_06", "framebuffers-screen")

        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        for (i in 0..Object.screen) {

            glBindVertexArray(vao[i])
            glBindBuffer(GL_ARRAY_BUFFER, vbo[i])
            glBufferData(GL_ARRAY_BUFFER, vertices[i], GL_STATIC_DRAW)
            if (i == Object.screen) {
                glEnableVertexAttribArray(glf.pos2_tc2)
                glVertexAttribPointer(glf.pos2_tc2)
                glEnableVertexAttribArray(glf.pos2_tc2[1])
                glVertexAttribPointer(glf.pos2_tc2[1])
            } else {
                glEnableVertexAttribArray(glf.pos3_tc2)
                glVertexAttribPointer(glf.pos3_tc2)
                glEnableVertexAttribArray(glf.pos3_tc2[1])
                glVertexAttribPointer(glf.pos3_tc2[1])
            }
            glBindVertexArray()
        }
        // load textures
        tex[Tex.cube] = loadTexture("textures/marble.jpg")
        tex[Tex.plane] = loadTexture("textures/metal.png")

        // framebuffer configuration
        // -------------------------
        glGenFramebuffers(framebuffer)
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)
        // create a color attachment texture
        tex[Tex.colorBuffer] = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, tex[Tex.colorBuffer])
        glTexImage2D(GL_TEXTURE_2D, GL_RGB8, size, GL_RGB, GL_UNSIGNED_BYTE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex[Tex.colorBuffer], 0)
        // create a renderbuffer object for depth and stencil attachment (we won't be sampling these)
        glGenRenderbuffers(rbo)
        glBindRenderbuffer(GL_RENDERBUFFER, rbo)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, size) // use a single renderbuffer object for both a depth AND stencil buffer.
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo) // now actually attach it
        // now that we actually created the framebuffer and added all attachments we want to check if it is actually complete now
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!")
        glBindFramebuffer(GL_FRAMEBUFFER)

        // draw as wireframe
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    }

    inner open class ProgramRender(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            usingProgram(this) { "texture1".unit = semantic.sampler.DIFFUSE }
        }
    }

    inner open class ProgramSplash(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {
        init {
            usingProgram(this) { "screenTexture".unit = semantic.sampler.DIFFUSE }
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
            // bind to framebuffer and draw scene as we normally would to color texture
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)
            glEnable(GL_DEPTH_TEST) // enable depth testing (is disabled for rendering screen-space quad)

            // make sure we clear the framebuffer's content
            glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glUseProgram(programRender)
            var model = Mat4()
            val view = camera.viewMatrix
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            glUniform(programRender.view, view)
            glUniform(programRender.proj, projection)
            // cubes
            glBindVertexArray(vao[Object.cube])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, tex[Tex.cube])
            model.translate_(-1.0f, 0.0f, -1.0f)
            glUniform(programRender.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            model = Mat4()
                    .translate_(2.0f, 0.0f, 0.0f)
            glUniform(programRender.model, model)
            glDrawArrays(GL_TRIANGLES, 36)
            // floor
            glBindVertexArray(vao[Object.plane])
            glBindTexture(GL_TEXTURE_2D, tex[Tex.plane])
            glUniform(programRender.model, Mat4())
            glDrawArrays(GL_TRIANGLES, 6)
            glBindVertexArray(0)

            // now bind back to default framebuffer and draw a quad plane with the attached framebuffer color texture
            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glDisable(GL_DEPTH_TEST) // disable depth test so screen-space quad isn't discarded due to depth test.
            // clear all relevant buffers
            glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // set clear color to white (not really necessary actually, since we won't be able to see behind the quad anyways)
            glClear(GL_COLOR_BUFFER_BIT)

            glUseProgram(programSplash)
            glBindVertexArray(vao[Object.screen])
            glBindTexture(GL_TEXTURE_2D, tex[Tex.colorBuffer])    // use the color attachment texture as the texture of the quad plane
            glDrawArrays(GL_TRIANGLES, 6)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        glDeletePrograms(programRender, programSplash)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(tex)

        destroyBuffers(vao, vbo, tex, *vertices)

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