package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 24.04.2017.
 */

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.buffer.glBufferData
import gln.draw.glDrawArrays
import gln.glClearColor
import gln.glf.semantic
import gln.vertexArray.glBindVertexArray
import imgui.ImGui
import imgui.WindowFlags
import imgui.functionalProgramming.withWindow
import imgui.impl.LwjglGL3
import imgui.or
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig

fun main(args: Array<String>) {

    with(HelloTriangle()) {
        run()
        end()
    }
}

private class HelloTriangle {

    val window = initWindow("Hello Triangle")

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

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    val vertices = floatArrayOf(
            -0.5f, -0.5f, 0f, // left
            +0.5f, -0.5f, 0f, // right
            +0.0f, +0.5f, 0f) // top

    init {
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
        glGenBuffers(vbo)
        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)

        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, Vec3.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)

        /*  note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound
            vertex buffer object so afterwards we can safely unbind */
        glBindBuffer(GL_ARRAY_BUFFER)

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        glBindVertexArray()

        // Setup ImGui binding
        LwjglGL3.init(window, true)
    }

    var showOverlay = true
    var polygonMode = 0

    fun run() {

        while (window.open) {

            window.processInput()
            LwjglGL3.newFrame()

            with(ImGui) {
                setNextWindowPos(Vec2(10))
                withWindow("Overlay", ::showOverlay, WindowFlags.NoTitleBar or WindowFlags.NoResize or WindowFlags.AlwaysAutoResize or WindowFlags.NoMove or WindowFlags.NoSavedSettings) {
                    text("Polygon Mode:")
                    radioButton("GL_LINE", ::polygonMode, 0); sameLine(); radioButton("GL_FILL", ::polygonMode, 1)
                }
            }

            glPolygonMode(GL_FRONT_AND_BACK, if(polygonMode == 0) GL_LINE else GL_FILL)

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT)

            //  draw our first triangle
            glUseProgram(shaderProgram)
            /*  seeing as we only have a single VAO there's no need to bind it every time, but we'll do so to keep
                things a bit more organized         */
            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLES, 3)
            // glBindVertexArray() // no need to unbind it every time


            ImGui.render()
            //  glfw: swap buffers and poll IO events (keys pressed/released, mouse moved etc.)
            window.swapAndPoll()
        }
    }

    fun end() {

        LwjglGL3.shutdown()

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(shaderProgram)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)

        destroyBuf(vao, vbo)

        window.end()
    }
}


