package learnOpenGL.a_gettingStarted

/**
 * Created by GBarbieri on 25.04.2017.
 */

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import gln.draw.glDrawElements
import gln.get
import gln.glClearColor
import gln.glf.semantic
import gln.program.usingProgram
import gln.texture.glTexImage2D
import gln.texture.plus
import gln.uniform.glUniform1i
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.common.flipY
import learnOpenGL.common.readImage
import learnOpenGL.common.toBuffer
import org.lwjgl.opengl.EXTABGR
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_BGR
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.buffer.use
import uno.glsl.Program
import uno.glsl.glDeleteProgram
import uno.glsl.glUseProgram

fun main(args: Array<String>) {

    with(TexturesCombined()) {
        run()
        end()
    }
}

private class TexturesCombined {

    val window = initWindow("Textures Combined")

    val program = ProgramA()

    enum class Buffer { Vertex, Element }

    val buffers = intBufferBig<Buffer>()
    val vao = intBufferBig(1)

    val vertices = floatArrayOf(
            // positions        // texture coords
            +0.5f, +0.5f, 0f, 1f, 1f, // top right
            +0.5f, -0.5f, 0f, 1f, 0f, // bottom right
            -0.5f, -0.5f, 0f, 0f, 0f, // bottom left
            -0.5f, +0.5f, 0f, 0f, 1f  // top left
    )
    val indices = intArrayOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
    )

    enum class Texture { A, B }

    val textures = intBufferBig<Texture>()

    inner class ProgramA : Program("shaders/a/_4_2", "texture.vert", "texture.frag") {
        init {
            usingProgram(name) {
                // either set it manually like so:
                val textureA_location = glGetUniformLocation(name, "textureA")
                glUniform1i(textureA_location, Texture.A)
                // or set it via glNext
                "textureB".unitE = Texture.B
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
        image.toBuffer().use {
            glTexImage2D(GL_RGB, image.width, image.height, GL_BGR, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }

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
        image.toBuffer().use {
            // ByteBuffered images used BRGA instead RGBA
            glTexImage2D(GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }

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

            // bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + Texture.A)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + Texture.B)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            // render container
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
        glDeleteTextures(textures)

        destroyBuf(vao, buffers, textures)

        window.end()
    }
}