package learnOpenGL.a_gettingStarted

/**
 * Created by elect on 24/04/17.
 */

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import gln.draw.glDrawElements
import gln.get
import gln.glClearColor
import gln.glf.semantic
import gln.program.usingProgram
import gln.texture.glBindTexture
import gln.texture.glTexImage2D
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.common.readImage
import learnOpenGL.common.toBuffer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.buffer.use
import uno.glsl.Program
import uno.glsl.glDeleteProgram
import uno.glsl.glUseProgram

fun main(args: Array<String>) {

    with(Textures()) {
        run()
        end()
    }
}

private class Textures {

    val window = initWindow("Textures")

    val program = ProgramA()

    enum class Buffer { Vertex, Element }

    val buffers = intBufferBig<Buffer>()
    val vao = intBufferBig(1)

    val vertices = floatArrayOf(
            // positions | texture coords
            +0.5f, +0.5f, 0f, 1f, 1f, // top right
            +0.5f, -0.5f, 0f, 1f, 0f, // bottom right
            -0.5f, -0.5f, 0f, 0f, 0f, // bottom left
            -0.5f, +0.5f, 0f, 0f, 1f  // top left
    )
    val indices = intArrayOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    )

    val texture = intBufferBig(1)

    class ProgramA : Program("shaders/a/_4_1", "texture.vert", "texture.frag") {
        init {
            usingProgram(name) {
                glUniform1i(
                        glGetUniformLocation(name, "textureA"),
                        semantic.sampler.DIFFUSE)
            }
        }
    }

    init {

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
        glVertexAttribPointer(semantic.attr.TEX_COORD, Vec2.length, GL_FLOAT, false, Vec3.size + Vec2.size, Vec3.size)
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)


        // load and create a texture
        glGenTextures(texture)
        //  all upcoming GL_TEXTURE_2D operations now have effect on this texture object
        glBindTexture(GL_TEXTURE_2D, texture)
        //  set the texture wrapping parameters to GL_REPEAT (default wrapping method)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // set texture filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

        // load image, create texture and generate mipmaps
        val image = readImage("textures/container.jpg")
        image.toBuffer().use {
            // ByteBuffered images used BRG instead RGB
            glTexImage2D(GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        } // byteBuffer automatically dispose with `use{ .. }`

        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    fun run() {

        while (window.open) {

            window.processInput()

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT)

            // bind Texture
            glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
            glBindTexture(GL_TEXTURE_2D, texture)

            // render the triangle
            glUseProgram(program)
            glBindVertexArray(vao)
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT)


            window.swapAndPoll()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(buffers)
        glDeleteTextures(texture)

        destroyBuf(vao, buffers, texture)

        window.end()
    }
}