package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 02.05.2017.
 */

import glm_.f
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.rad
import glm_.set
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
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.glf.glf
import uno.glf.semantic
import uno.gln.*
import uno.glsl.Program


fun main(args: Array<String>) {

    with(LightCastersDirectional()) {

        run()
        end()
    }
}

private class LightCastersDirectional {

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
            // positions         // normals    // texture coords
            -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 0.0f, 0.0f,
            +0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 1.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 1.0f, 1.0f,
            -0.5f, +0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, +0.0f, +0.0f, -1.0f, 0.0f, 0.0f,

            -0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 0.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 1.0f, 1.0f,
            -0.5f, +0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, +0.0f, +0.0f, +1.0f, 0.0f, 0.0f,

            -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f, 1.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, -1.0f, +0.0f, +0.0f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, -1.0f, +0.0f, +0.0f, 0.0f, 1.0f,
            -0.5f, -0.5f, +0.5f, -1.0f, +0.0f, +0.0f, 0.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, -1.0f, +0.0f, +0.0f, 1.0f, 0.0f,

            +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, -0.5f, +1.0f, +0.0f, +0.0f, 1.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, +1.0f, +0.0f, +0.0f, 0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, +1.0f, +0.0f, +0.0f, 0.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, +1.0f, +0.0f, +0.0f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f, 0.0f, 1.0f,
            +0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f, 1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f, 1.0f, 0.0f,
            +0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f, 1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f, +0.0f, -1.0f, +0.0f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, +0.0f, -1.0f, +0.0f, 0.0f, 1.0f,

            -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f, 0.0f, 1.0f,
            +0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f, 1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f,
            +0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f, 1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f, +0.0f, +1.0f, +0.0f, 0.0f, 0.0f,
            -0.5f, +0.5f, -0.5f, +0.0f, +1.0f, +0.0f, 0.0f, 1.0f)

    // positions all containers
    val cubePositions = arrayOf(
            Vec3(0.0f, 0.0f, 0.0f),
            Vec3(2.0f, 5.0f, -15.0f),
            Vec3(-1.5f, -2.2f, -2.5f),
            Vec3(-3.8f, -2.0f, -12.3f),
            Vec3(2.4f, -0.4f, -3.5f),
            Vec3(-1.7f, 3.0f, -7.5f),
            Vec3(1.3f, -2.0f, -2.5f),
            Vec3(1.5f, 2.0f, -2.5f),
            Vec3(1.5f, 0.2f, -1.5f),
            Vec3(-1.3f, 1.0f, -1.5f))

    // camera
    val camera = Camera(position = Vec3(0.0f, 0.0f, 3.0f))
    var lastX = 800.0f / 2.0
    var lastY = 600.0 / 2.0

    var firstMouse = true

    var deltaTime = 0.0f    // time between current frame and last frame
    var lastFrame = 0.0f

    object Texture {
        val Diffuse = 0
        val Specular = 1
        val MAX = 2
    }

    val textures = intBufferBig(Texture.MAX)

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
        window = GlfwWindow(800, 600, "Light Caster Directional")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@LightCastersDirectional::framebuffer_size_callback
            cursorPosCallback = this@LightCastersDirectional::mouse_callback
            scrollCallback = this@LightCastersDirectional::scroll_callback

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
        lighting = Lighting("shaders/b/_07", "light-casters")
        lamp = Lamp("shaders/b/_01", "lamp")


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3_tc2)
        glEnableVertexAttribArray(glf.pos3_nor3_tc2)

        glVertexAttribPointer(glf.pos3_nor3_tc2[1])
        glEnableVertexAttribArray(glf.pos3_nor3_tc2[1])

        glVertexAttribPointer(glf.pos3_nor3_tc2[2])
        glEnableVertexAttribArray(glf.pos3_nor3_tc2[2])

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3_tc2)
        glEnableVertexAttribArray(glf.pos3_nor3_tc2)

        // load textures (we now use a utility function to keep the code more organized)
        textures[Texture.Diffuse] = loadTexture("textures/container2.png")
        textures[Texture.Specular] = loadTexture("textures/container2_specular.png")

        // shader configuration
        usingProgram(lighting) {
            "material.diffuse".unit = semantic.sampler.DIFFUSE
            "material.specular".unit = semantic.sampler.SPECULAR
        }
    }

    inner class Lighting(root: String, shader: String) : Lamp(root, shader) {

        val viewPos = glGetUniformLocation(name, "viewPos")
        val lgt = Light()
        val mtl = Material()

        inner class Light {
            val dir = glGetUniformLocation(name, "light.direction")
            val ambient = glGetUniformLocation(name, "light.ambient")
            val diffuse = glGetUniformLocation(name, "light.diffuse")
            val specular = glGetUniformLocation(name, "light.specular")
        }

        inner class Material {
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
            glUniform(lighting.lgt.dir, -0.2f, -1.0f, -0.3f)
            glUniform(lighting.viewPos, camera.position)

            // light properties
            glUniform3(lighting.lgt.ambient, 0.2f)
            glUniform3(lighting.lgt.diffuse, 0.5f)
            glUniform3(lighting.lgt.specular, 1.0f)

            // material properties
            glUniform(lighting.mtl.shininess, 32.0f)

            // view/projection transformations
            val projection = glm.perspective(camera.zoom.rad, window.aspect, 0.1f, 100.0f)
            val view = camera.viewMatrix
            glUniform(lighting.proj, projection)
            glUniform(lighting.view, view)

            // bind diffuse map
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.Diffuse])
            // bind specular map
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.SPECULAR)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.Specular])

            // render containers
            glBindVertexArray(vao[VA.Cube])
            cubePositions.forEachIndexed { i, pos ->

                // calculate the model matrix for each object and pass it to shader before drawing
                val model = Mat4().translate(pos)
                val angle = 20.0f * i
                model.rotate_(angle.rad, 1.0f, 0.3f, 0.5f)
                glUniform(lighting.model, model)

                glDrawArrays(GL_TRIANGLES, 36)
            }

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
        glDeleteTextures(textures)

        destroyBuffers(vao, vbo, textures, vertices)

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