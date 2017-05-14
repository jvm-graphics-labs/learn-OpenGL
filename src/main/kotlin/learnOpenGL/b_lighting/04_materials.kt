package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 28.04.2017.
 */

import glm.f
import glm.glm
import glm.glm.sin
import glm.mat4x4.Mat4
import glm.rad
import glm.vec3.Vec3
import learnOpenGL.common.Camera
import learnOpenGL.common.Camera.Movement.*
import learnOpenGL.common.GlfwWindow
import learnOpenGL.common.GlfwWindow.Cursor.Disabled
import learnOpenGL.common.glfw
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.gln.*
import uno.glsl.Program


fun main(args: Array<String>) {

    with(Materials()) {

        run()
        end()
    }
}

private class Materials {

    val window: GlfwWindow

    val lighting: Lighting
    val lamp: Lamp

    val vbo = intBufferBig(1)

    object VA {
        val Cube = 0
        val Light = 1
        val Max = 2
    }

    val vao = intBufferBig(VA.Max)

    val vertices = floatBufferOf(
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            +0.5f, +0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            +0.5f, +0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 0.0f, -1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f,

            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, 0.0f, 1.0f,

            -0.5f, +0.5f, +0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, +0.5f, -1.0f, 0.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, -1.0f, 0.0f, 0.0f,

            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 1.0f, 0.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 1.0f, 0.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 0.0f, -1.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, 0.0f, -1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f, 0.0f, -1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f,

            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, 0.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 0.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, 0.0f, 1.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, 0.0f, 1.0f, 0.0f)

    // camera
    val camera = Camera(position = Vec3(0.0f, 0.0f, 3.0f))
    var lastX = 800.0f / 2.0
    var lastY = 600.0 / 2.0

    var firstMouse = true

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    // lighting
    val lightPos = Vec3(1.2f, 1.0f, 2.0f)

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
        window = GlfwWindow(800, 600, "Materials")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@Materials::framebuffer_size_callback
            cursorPosCallback = this@Materials::mouse_callback
            scrollCallback = this@Materials::scroll_callback

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
        lighting = Lighting("shaders/b/_04", "materials")
        lamp = Lamp("shaders/b/_01", "lamp")


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3)
        glEnableVertexAttribArray(glf.pos3_nor3)

        glVertexAttribPointer(glf.pos3_nor3[1])
        glEnableVertexAttribArray(glf.pos3_nor3[1])

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3)
        glEnableVertexAttribArray(glf.pos3_nor3)
    }

    inner class Lighting(root: String, shader: String) : Lamp(root, shader) {

        val viewPos = glGetUniformLocation(name, "viewPos")
        val lgt = Light()
        val mtl = Material()

        inner class Light {
            val pos = glGetUniformLocation(name, "light.position")
            val ambient = glGetUniformLocation(name, "light.ambient")
            val diffuse = glGetUniformLocation(name, "light.diffuse")
            val specular = glGetUniformLocation(name, "light.specular")
        }

        inner class Material {
            val ambient = glGetUniformLocation(name, "material.ambient")
            val diffuse = glGetUniformLocation(name, "material.diffuse")
            val specular = glGetUniformLocation(name, "material.specular")
            val shininess = glGetUniformLocation(name, "material.shininess")
        }
    }

    inner open class Lamp(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
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
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting)

            glUniform(lighting.lgt.pos, lightPos)
            glUniform(lighting.viewPos, camera.position)

            // light properties
            val lightColor = Vec3(
                    x = sin(glfw.time * 2.0f),
                    y = sin(glfw.time * 0.7f),
                    z = sin(glfw.time * 1.3f))
            val diffuseColor = lightColor * 0.5f    // decrease the influence
            val ambientColor = diffuseColor * 0.2f  // low influence
            glUniform(lighting.lgt.ambient, ambientColor)
            glUniform(lighting.lgt.diffuse, diffuseColor)
            glUniform3(lighting.lgt.specular, 1.0f)

            // material properties
            glUniform(lighting.mtl.ambient, 1.0f, 0.5f, 0.31f)
            glUniform(lighting.mtl.diffuse, 1.0f, 0.5f, 0.31f)
            glUniform3(lighting.mtl.specular, 0.5f)
            glUniform(lighting.mtl.shininess, 32.0f)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            val view = camera.viewMatrix
            glUniform(lighting.proj, projection)
            glUniform(lighting.view, view)

            // world transformation
            var model = Mat4()
            glUniform(lighting.model, model)

            // render the cube
            glBindVertexArray(vao[VA.Cube])
            glDrawArrays(GL_TRIANGLES, 36)


            // also draw the lamp object
            glUseProgram(lamp)

            glUniform(lamp.proj, projection)
            glUniform(lamp.view, view)
            model = model
                    .translate(lightPos)
                    .scale(0.2f) // a smaller cube
            glUniform(lamp.model, model)

            glBindVertexArray(vao[VA.Light])
            glDrawArrays(GL_TRIANGLES, 36)

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeletePrograms(lighting, lamp)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)

        destroyBuffers(vao, vbo, vertices)

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
            lastX = xpos
            lastY = ypos
            firstMouse = false
        }

        var xoffset = xpos - lastX
        var yoffset = lastY - ypos // reversed since y-coordinates go from bottom to top
        lastX = xpos
        lastY = ypos

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        camera.processMouseMovement(xoffset.f, yoffset.f)
    }

    /** glfw: whenever the mouse scroll wheel scrolls, this callback is called  */
    fun scroll_callback(xOffset: Double, yOffset: Double) = camera.processMouseScroll(yOffset.f)
}