package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 24.04.2017.
 */

import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.glClearColor
import gln.glf.semantic
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.glDeleteVertexArrays
import org.lwjgl.opengl.GL30.glGenVertexArrays
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig

fun main(args: Array<String>) {

    with(ShadersInterpolation()) {
        run()
        end()
    }
}

private class ShadersInterpolation {

    val window = initWindow("Shaders Interpolation")

    val vertexShaderSource = """
        #version 330 core

        #define POSITION    0
        #define COLOR       3

        layout (location = POSITION) in vec3 aPos;
        layout (location = COLOR) in vec3 aColor;

        out vec3 ourColor;

        void main()
        {
            gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
            ourColor = aColor;
            }
    """
    val fragmentShaderSource = """
        #version 330 core

        #define FRAG_COLOR    0

        layout (location = FRAG_COLOR) out vec4 fragColor;

        in vec3 ourColor;

        void main()
        {
            fragColor = vec4(ourColor, 1.0f);
        }
    """

    val shaderProgram: Int

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    val vertices = floatArrayOf(
            // positions | colors
            +0.5f, -0.5f, 0f, 1f, 0f, 0f, // bottom right
            -0.5f, -0.5f, 0f, 0f, 1f, 0f, // bottom left
            +0.0f, +0.5f, 0f, 0f, 0f, 1f  // top
    )


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

        //  position attribute
        glVertexAttribPointer(semantic.attr.POSITION, Vec3.length, GL_FLOAT, false, 2 * Vec3.size, 0)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        //  color attribute
        glVertexAttribPointer(semantic.attr.COLOR, Vec3.length, GL_FLOAT, false, 2 * Vec3.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.COLOR)

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()

        // as we only have a single shader, we could also just activate our shader once beforehand if we want to
        glUseProgram(shaderProgram)
    }

    fun run() {

        while (window.open) {

            window.processInput()

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT)

            // render the triangle
            glDrawArrays(GL_TRIANGLES, 3)

            window.swapAndPoll()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(shaderProgram)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)

        destroyBuf(vao, vbo)

        window.end()
    }
}