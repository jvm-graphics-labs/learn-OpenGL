package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 02.05.2017.
 */

import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.glf
import gln.glf.semantic
import gln.set
import gln.uniform.glUniform
import gln.uniform.glUniform3
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.a_gettingStarted.cubePositions
import learnOpenGL.a_gettingStarted.end
import learnOpenGL.a_gettingStarted.swapAndPoll
import learnOpenGL.common.loadTexture
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL30.*
import uno.buffer.destroyBuf
import uno.buffer.intBufferBig
import uno.glsl.Program
import uno.glsl.glDeletePrograms
import uno.glsl.glUseProgram
import uno.glsl.usingProgram


fun main(args: Array<String>) {

    with(LightCastersPoint()) {
        run()
        end()
    }
}

private class LightCastersPoint {

    val window = initWindow0("Light Caster Point")

    val lighting = Lighting()
    val lamp = Lamp()

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    enum class Texture { Diffuse, Specular }

    val textures = intBufferBig<Texture>()

    val lightPos = Vec3(1.2f, 1f, 2f)

    inner class Lighting : Lamp("shaders/b/_5_2", "light-casters") {

        val viewPos = glGetUniformLocation(name, "viewPos")
        val lgt = Light()
        val mtl = Material()

        inner class Light {
            val pos = glGetUniformLocation(name, "light.position")
            val ambient = glGetUniformLocation(name, "light.ambient")
            val diffuse = glGetUniformLocation(name, "light.diffuse")
            val specular = glGetUniformLocation(name, "light.specular")
            val constant = glGetUniformLocation(name, "light.constant")
            val linear = glGetUniformLocation(name, "light.linear")
            val quadratic = glGetUniformLocation(name, "light.quadratic")
        }

        inner class Material {
            val shininess = glGetUniformLocation(name, "material.shininess")
        }
    }

    inner open class Lamp(root: String = "shaders/b/_1", shader: String = "lamp") : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")
    }

    init {

        glEnable(GL_DEPTH_TEST)


        glGenVertexArrays(vao)

        // first, configure the cube's VAO (and VBO)
        glGenBuffers(vbo)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube1, GL_STATIC_DRAW)

        glBindVertexArray(vao[VA.Cube])

        glVertexAttribPointer(glf.pos3_nor3_tc2)
        glEnableVertexAttribArray(glf.pos3_nor3_tc2)

        // second, configure the light's VAO (VBO stays the same; the vertices are the same for the light object which is also a 3D cube)
        glBindVertexArray(vao[VA.Light])

        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // note that we update the lamp's position attribute's stride to reflect the updated buffer data
        glVertexAttribPointer(glf.pos3_nor3_tc2[0])
        glEnableVertexAttribArray(glf.pos3_nor3_tc2[0])

        // load textures (we now use a utility function to keep the code more organized)
        textures[Texture.Diffuse] = loadTexture("textures/container2.png")
        textures[Texture.Specular] = loadTexture("textures/container2_specular.png")

        // shader configuration
        usingProgram(lighting) {
            "material.diffuse".unit = semantic.sampler.DIFFUSE
            "material.specular".unit = semantic.sampler.SPECULAR
        }
    }

    fun run() {

        while (window.open) {

            window.processFrame()


            // render
            glClearColor(clearColor0)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // be sure to activate shader when setting uniforms/drawing objects
            glUseProgram(lighting)
            glUniform(lighting.lgt.pos, lightPos)
            glUniform(lighting.viewPos, camera.position)

            // light properties
            glUniform3(lighting.lgt.ambient, 0.2f)
            glUniform3(lighting.lgt.diffuse, 0.5f)
            glUniform3(lighting.lgt.specular, 1f)
            glUniform(lighting.lgt.constant, 1f)
            glUniform(lighting.lgt.linear, 0.09f)
            glUniform(lighting.lgt.quadratic, 0.032f)

            // material properties
            glUniform(lighting.mtl.shininess, 32f)

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
                val angle = 20f * i
                model.rotate_(angle.rad, 1f, 0.3f, 0.5f)
                glUniform(lighting.model, model)

                glDrawArrays(GL_TRIANGLES, 36)
            }

            // also draw the lamp object
            glUseProgram(lamp)

            glUniform(lamp.proj, projection)
            glUniform(lamp.view, view)
            val model = Mat4()
                    .translate(lightPos)
                    .scale(0.2f) // a smaller cube
            glUniform(lamp.model, model)

            glBindVertexArray(vao[VA.Light])
            glDrawArrays(GL_TRIANGLES, 36)


            window.swapAndPoll()
        }
    }

    fun end() {

        glDeletePrograms(lighting, lamp)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)

        destroyBuf(vao, vbo, textures)

        window.end()
    }
}