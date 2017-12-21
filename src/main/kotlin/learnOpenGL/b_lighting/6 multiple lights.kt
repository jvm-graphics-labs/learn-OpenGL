package learnOpenGL.b_lighting

/**
 * Created by GBarbieri on 02.05.2017.
 */

import glm_.func.cos
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

    with(MultipleLights()) {
        run()
        end()
    }
}

private class MultipleLights {

    val window = initWindow0("Multiple Lights")

    val lighting = Lighting()
    val lamp = Lamp()

    val vbo = intBufferBig(1)

    enum class VA { Cube, Light }

    val vao = intBufferBig<VA>()

    // positions of the point lights
    val pointLightPositions = arrayOf(
            Vec3(0.7f, 0.2f, 2f),
            Vec3(2.3f, -3.3f, -4f),
            Vec3(-4f, 2f, -12f),
            Vec3(0f, 0f, -3f))

    enum class Texture { Diffuse, Specular }

    val textures = intBufferBig<Texture>()

    inner class Lighting : Lamp("shaders/b/_6", "multiple-lights") {

        val viewPos = glGetUniformLocation(name, "viewPos")
        val dirLight = DirLight()
        val pointLight = Array(4, { PointLight(it) })
        val spotLight = SpotLight()
        val mtl = Material()

        inner class DirLight {
            val dir = glGetUniformLocation(name, "light.direction")
            val ambient = glGetUniformLocation(name, "light.ambient")
            val diffuse = glGetUniformLocation(name, "light.diffuse")
            val specular = glGetUniformLocation(name, "light.specular")
        }

        inner class PointLight(i: Int) {
            val pos = glGetUniformLocation(name, "pointLights[$i].position")
            val ambient = glGetUniformLocation(name, "pointLights[$i].ambient")
            val diffuse = glGetUniformLocation(name, "pointLights[$i].diffuse")
            val specular = glGetUniformLocation(name, "pointLights[$i].specular")
            val constant = glGetUniformLocation(name, "pointLights[$i].constant")
            val linear = glGetUniformLocation(name, "pointLights[$i].linear")
            val quadratic = glGetUniformLocation(name, "pointLights[$i].quadratic")
        }

        inner class SpotLight() {
            val pos = glGetUniformLocation(name, "spotLight.position")
            val dir = glGetUniformLocation(name, "spotLight.direction")
            val ambient = glGetUniformLocation(name, "spotLight.ambient")
            val diffuse = glGetUniformLocation(name, "spotLight.diffuse")
            val specular = glGetUniformLocation(name, "spotLight.specular")
            val constant = glGetUniformLocation(name, "spotLight.constant")
            val linear = glGetUniformLocation(name, "spotLight.linear")
            val quadratic = glGetUniformLocation(name, "spotLight.quadratic")
            val cutOff = glGetUniformLocation(name, "spotLight.cutOff")
            val outerCutOff = glGetUniformLocation(name, "spotLight.outerCutOff")
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
            glUniform(lighting.viewPos, camera.position)
            glUniform(lighting.mtl.shininess, 32f)

            /*  Here we set all the uniforms for the 5/6 types of lights we have. We have to set them manually and index
                the proper PointLight struct in the array to set each uniform variable. This can be done more
                code-friendly by defining light types as classes and set their values in there, or by using a more
                efficient uniform approach by using 'Uniform buffer objects', but that is something we'll discuss in
                the 'Advanced GLSL' tutorial.        */
            // directional light
            with(lighting.dirLight) {
                glUniform(dir, -0.2f, -1f, -0.3f)
                glUniform3(ambient, 0.05f)
                glUniform3(diffuse, 0.4f)
                glUniform3(specular, 0.5f)
            }
            // point lights
            for (i in 0..3)
                with(lighting.pointLight[i]) {
                    glUniform(pos, pointLightPositions[i])
                    glUniform3(ambient, 0.05f)
                    glUniform3(diffuse, 0.8f)
                    glUniform3(specular, 1f)
                    glUniform(constant, 1f)
                    glUniform(linear, 0.09f)
                    glUniform(quadratic, 0.032f)
                }
            // spotLight
            with(lighting.spotLight) {
                glUniform(pos, camera.position)
                glUniform(dir, camera.front)
                glUniform3(ambient, 0f)
                glUniform3(diffuse, 1f)
                glUniform3(specular, 1f)
                glUniform(constant, 1f)
                glUniform(linear, 0.9f)
                glUniform(quadratic, 0.032f)
                glUniform(cutOff, 12.5f.rad.cos)
                glUniform(outerCutOff, 15f.rad.cos)
            }


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

            // we now draw as many light bulbs as we have point lights.
            glBindVertexArray(vao[VA.Light])
            pointLightPositions.forEach {
                val model = Mat4()
                        .translate(it)
                        .scale(0.2f) // Make it a smaller cube

                glUniform(lamp.model, model)
                glDrawArrays(GL_TRIANGLES, 36)
            }


            window.swapAndPoll()
        }
    }

    fun end() {

        //  optional: de-allocate all resources once they've outlived their purpose:
        glDeletePrograms(lighting, lamp)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)

        destroyBuf(vao, vbo, textures)

        window.end()
    }
}