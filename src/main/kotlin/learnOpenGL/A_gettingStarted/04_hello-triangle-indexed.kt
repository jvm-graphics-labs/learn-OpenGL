package learnOpenGL.A_gettingStarted

/**
 * Created by GBarbieri on 24.04.2017.
 */

import glm.vec._3.Vec3
import learnOpenGL.common.GlfwWindow
import learnOpenGL.common.glfw
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.destroyBuffers
import uno.buffer.floatBufferOf
import uno.buffer.intBufferBig
import uno.buffer.intBufferOf
import uno.glf.semantic
import uno.gln.glBindBuffer
import uno.gln.glBindVertexArray
import uno.gln.glDrawElements


fun main(args: Array<String>) {

    with(HelloTriangleIndexed()) {

        run()

        end()
    }
}

internal class HelloTriangleIndexed {

    val window: GlfwWindow

    val vertexShaderSource = """
        #version 330 core

        #define POSITION    0

        layout (location = POSITION) in vec3 aPos;
        void main()
        {
            gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
        }
    """
    val fragmentShaderSource = """
        #version 330 core

        #define FRAG_COLOR    0

        layout (location = FRAG_COLOR) out vec4 fragColor;
        void main()
        {
            fragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
        }
    """

    val shaderProgram: Int

    object Buffer {
        val Vertex = 0
        val Element = 1
        val Max = 2
    }

    val buffers = intBufferBig(Buffer.Max)
    val vao = intBufferBig(1)

    val vertices = floatBufferOf(
            +0.5f, +0.5f, 0.0f, // top right
            +0.5f, -0.5f, 0.0f, // bottom right
            -0.5f, -0.5f, 0.0f, // bottom left
            -0.5f, +0.5f, 0.0f  // top left
    )
    val indices = intBufferOf(// note that we start from 0!
            0, 1, 3, // first Triangle
            1, 2, 3  // second Triangle
    )


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
        window = GlfwWindow(800, 600, "Hello Triangle Indexed")

        with(window) {

            makeContextCurrent() // Make the OpenGL context current

            show()   // Make the window visible

            framebufferSizeCallback = Companion::framebuffer_size_callback
        }

        /* This line is critical for LWJGL's interoperation with GLFW's OpenGL context, or any context that is managed
           externally. LWJGL detects the context that is current in the current thread, creates the GLCapabilities instance
           and makes the OpenGL bindings available for use.    */
        GL.createCapabilities()


        //  build and compile our shader program
        //  vertex shader
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexShaderSource)
        glCompileShader(vertexShader)
        //  heck for shader compile errors
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            val infoLog = glGetShaderInfoLog(vertexShader)
            System.err.println("ERROR::SHADER::VERTEX::COMPILATION_FAILED\n$infoLog")
        }
        //  fragment shader
        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentShaderSource)
        glCompileShader(fragmentShader)
        //  check for shader compile errors
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            val infoLog = glGetShaderInfoLog(fragmentShader)
            System.err.print("ERROR::SHADER::FRAGMENT::COMPILATION_FAILED\n$infoLog")
        }
        //  link shaders
        shaderProgram = glCreateProgram()
        glAttachShader(shaderProgram, vertexShader)
        glAttachShader(shaderProgram, fragmentShader)
        glLinkProgram(shaderProgram)
        //  check for linking errors
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            val infoLog = glGetProgramInfoLog(shaderProgram)
            System.err.print("ERROR::SHADER::PROGRAM::LINKING_FAILED\n$infoLog")
        }
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(buffers)
        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, buffers[Buffer.Vertex])
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[Buffer.Element])
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)

        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.SIZE, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)

        /*  note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound
            vertex buffer object so afterwards we can safely unbind */
        glBindBuffer(GL_ARRAY_BUFFER)

        /*  remember: do NOT unbind the EBO while a VAO is active as the bound element buffer object IS stored in the VAO;
            keep the EBO bound.     */
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        glBindVertexArray()


        //  uncomment this call to draw in wireframe polygons.
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    fun run() {

        //  render loop
        while (window.shouldNotClose) {

            //  input
            processInput(window)

            //  render
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            glClear(GL_COLOR_BUFFER_BIT)

            //  draw our first triangle
            glUseProgram(shaderProgram)
            /*  seeing as we only have a single VAO there's no need to bind it every time, but we'll do so to keep things
                a bit more organized         */
            glBindVertexArray(vao)
            //glDrawArrays(GL_TRIANGLES, 6)
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT)
            // glBindVertexArray() // no need to unbind it every time

            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapBuffers()
            glfw.pollEvents()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)

        destroyBuffers(vao, buffers, vertices, indices)

        window.dispose()
        //  glfw: terminate, clearing all previously allocated GLFW resources.
        glfw.terminate()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun processInput(window: GlfwWindow) {

        if (window.key(GLFW_KEY_ESCAPE).pressed)
            window.shouldClose = true
    }

    companion object {

        /** glfw: whenever the window size changed (by OS or user resize) this callback function executes   */
        fun framebuffer_size_callback(width: Int, height: Int) {

            /*  make sure the viewport matches the new window dimensions; note that width and height will be significantly
                larger than specified on retina displays.     */
            glViewport(0, 0, width, height)
        }
    }
}