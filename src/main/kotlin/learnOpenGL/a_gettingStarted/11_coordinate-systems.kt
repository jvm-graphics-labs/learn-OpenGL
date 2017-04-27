package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 25.04.2017.
 */

import glm.mat4x4.Mat4
import glm.vec2.Vec2
import glm.vec3.Vec3
import learnOpenGL.common.*
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.EXTABGR
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.*
import uno.glf.semantic
import uno.gln.*
import glm.glm
import glm.rad
import org.lwjgl.opengl.GL20.*

fun main(args: Array<String>) {

    with(CoordinateSystems()) {

        run()
        end()
    }
}

private class CoordinateSystems {

    val window: GlfwWindow

    val ourShader: Int

    object Buffer {
        val Vertex = 0
        val Element = 1
        val Max = 2
    }

    val buffers = intBufferBig(Buffer.Max)
    val vao = intBufferBig(1)

    val vertices = floatBufferOf(
            // positions        // texture coords
            +0.5f, +0.5f, 0.0f, 1.0f, 1.0f, // top right
            +0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
            -0.5f, +0.5f, 0.0f, 0.0f, 1.0f  // top left
    )
    val indices = intBufferOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    )

    object Texture {
        val A = 0
        val B = 1
        val Max = 2
    }

    val textures = intBufferBig(Texture.Max)

    val semantic.sampler.DIFFUSE_A get() = 0
    val semantic.sampler.DIFFUSE_B get() = 1

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
        window = GlfwWindow(800, 600, "Coordinate Systems")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = this@CoordinateSystems::framebuffer_size_callback
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        // build and compile our shader program, you can name your shader files however you like
        ourShader = shaderOf(this::class, "shaders/a/_11", "coordinate-systems")


        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(buffers)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, buffers[Buffer.Vertex])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.Element])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size + Vec2.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        // texture coord attribute
        glVertexAttribPointer(semantic.attr.TEXCOORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.TEXCOORD)


        // load and create a texture
        glGenTextures(textures)

        //  texture A
        glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        var image = readImage("textures/container.jpg").flipY()
        var data = image.toByteBuffer()

        glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()


        //  texture B
        glBindTexture(GL_TEXTURE_2D, textures[Texture.B])
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        image = readImage("textures/awesomeface.png").flipY()
        data = image.toByteBuffer()

        glTexImage2D(GL_TEXTURE_2D, GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(GL_TEXTURE_2D)

        data.destroy()


        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()


        /*  Tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
            Code passed to usingProgram() {..] is executed using the given program, which at the end gets unbound   */
        usingProgram(ourShader) {

            "textureA".location.int = semantic.sampler.DIFFUSE_A
            "textureB".location.int = semantic.sampler.DIFFUSE_B
        }
    }

    fun run() {

        //  render loop
        while (window.shouldNotClose) {

            //  input
            processInput(window)

            //  render
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT)

            //  bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE_A)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE_B)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            usingProgram(ourShader) {

                //  create transformations
                val model = glm.rotate(Mat4(), -55.0f.rad, 1.0f, 0.0f, 0.0f)
                val view = glm.translate(Mat4(), 0.0f, 0.0f, -3.0f)
                val projection = glm.perspective(45.0f.rad, 800.0f / 600.0f, 0.1f, 100.0f)
                //  retrieve the matrix uniform locations
                val modelLoc = glGetUniformLocation(ourShader, "model")
                val viewLoc = glGetUniformLocation(ourShader, "view")
                //  pass them to the shaders (3 different ways)
                glUniformMatrix4f(modelLoc, model)
                glUniformMatrix4f(viewLoc, view)
                /*  note: currently we set the projection matrix each frame, but since the projection matrix rarely
                    changes it's often best practice to set it outside the main loop only once. Best place is the
                    framebuffer size callback   */
                "projection".location.mat4 = projection

                //  render container
                glBindVertexArray(vao)
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT)
            }

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)
        glDeleteTextures(textures)

        destroyBuffers(vao, buffers, textures, vertices, indices)

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

        /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
            larger than specified on retina displays.     */
        glViewport(0, 0, width, height)
    }
}