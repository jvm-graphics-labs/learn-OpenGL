package learnOpenGL.a_gettingStarted

/**
 * Created by elect on 26/04/17.
 */

import glm_.f
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.glm
import glm_.glm.cos
import glm_.glm.sin
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2d
import glm_.vec3.Vec3
import glm_.vec3.operators.times
import gln.buffer.glBindBuffer
import gln.draw.glDrawArrays
import gln.get
import gln.glClearColor
import gln.glf.semantic
import gln.program.usingProgram
import gln.texture.glTexImage2D
import gln.texture.plus
import gln.vertexArray.glBindVertexArray
import gln.vertexArray.glVertexAttribPointer
import learnOpenGL.common.flipY
import learnOpenGL.common.readImage
import learnOpenGL.common.toBuffer
import org.lwjgl.glfw.GLFW.*
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
import uno.glfw.GlfwWindow
import uno.glfw.GlfwWindow.Cursor.Disabled
import uno.glfw.glfw
import uno.glsl.Program
import uno.glsl.glDeleteProgram
import uno.glsl.usingProgram


fun main(args: Array<String>) {

    with(CameraMouseZoom()) {
        run()
        end()
    }
}

private class CameraMouseZoom {

    val window = initWindow("Camera Mouse Zoom")

    val program: ProgramA

    val vbo = intBufferBig(1)
    val vao = intBufferBig(1)

    enum class Texture { A, B }

    val textures = intBufferBig<Texture>()

    // camera
    val cameraPos = Vec3(0f, 0f, 3f)
    var cameraFront = Vec3(0f, 0f, -1f)
    val cameraUp = Vec3(0f, 1f, 0f)

    var firstMouse = true
    /*  yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right so we
        initially rotate a bit to the left.     */
    var yaw = -90f
    var pitch = 0f
    var last = Vec2d(800, 600) / 2
    var fov = 45f

    var deltaTime = 0f    // time between current frame and last frame
    var lastFrame = 0f

    init {

        with(window) {
            cursorPosCallback = ::mouseCallback
            // glfw: whenever the mouse scroll wheel scrolls, this callback is called
            scrollCallback = { _, yOffset ->
                if (fov in 1f..45f) fov -= yOffset.f
                fov = glm.clamp(fov, 1f, 45f)
            }

            // tell GLFW to capture our mouse
            cursor = Disabled
        }

        glEnable(GL_DEPTH_TEST)


        // build and compile our shader program, you can name your shader files however you like
        program = ProgramA("shaders/a/_14", "camera")


        //  set up vertex data (and buffer(s)) and configure vertex attributes
        glGenVertexArrays(vao)
        glGenBuffers(vbo)

        //  bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, verticesCube, GL_STATIC_DRAW)

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
            glTexImage2D(GL_RGB, image.width, image.height, EXTABGR.GL_ABGR_EXT, GL_UNSIGNED_BYTE, it)
            glGenerateMipmap(GL_TEXTURE_2D)
        }


        /*  You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens.
            Modifying other VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs)
            when it's not directly necessary.   */
        //glBindVertexArray()
    }

    inner class ProgramA(root: String, shader: String) : Program(root, "$shader.vert", "$shader.frag") {

        val model = glGetUniformLocation(name, "model")
        val view = glGetUniformLocation(name, "view")
        val proj = glGetUniformLocation(name, "projection")

        init {
            /*  Tell opengl for each sampler to which texture unit it belongs to (only has to be done once)
            Code passed to usingProgram() {..] is executed using the given program, which at the end gets unbound   */
            usingProgram(name) {
                "textureA".unitE = Texture.A
                "textureB".unitE = Texture.B
            }
        }
    }

    fun run() {

        while (window.open) {

            // per-frame time logic
            val currentFrame = glfw.time
            deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            window.processInput0()

            //  render
            glClearColor(clearColor)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // also clear the depth buffer now!

            //  bind textures on corresponding texture units
            glActiveTexture(GL_TEXTURE0 + Texture.A)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.A])
            glActiveTexture(GL_TEXTURE0 + Texture.B)
            glBindTexture(GL_TEXTURE_2D, textures[Texture.B])

            usingProgram(program) {

                // pass projection matrix to shader (note that in this case it could change every frame)
                glm.perspective(fov.rad, window.aspect, 0.1f, 100f) to program.proj

                // camera/view transformation
                glm.lookAt(cameraPos, cameraPos + cameraFront, cameraUp) to program.view

                // render boxes
                glBindVertexArray(vao)
                cubePositions.forEachIndexed { i, vec3 ->

                    // calculate the model matrix for each object and pass it to shader before drawing
                    val model = Mat4() translate_ vec3
                    val angle = 20.0f * i
                    model.rotate_(angle.rad, 1.0f, 0.3f, 0.5f)
                    model to program.model

                    glDrawArrays(GL_TRIANGLES, 36)
                }
            }

            window.swapAndPoll()
        }
    }

    fun end() {

        glDeleteProgram(program)
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textures)

        destroyBuf(vao, vbo, textures)

        window.end()
    }

    /** process all input: query GLFW whether relevant keys are pressed/released this frame and react accordingly   */
    fun GlfwWindow.processInput0() {

        processInput()

        val cameraSpeed = 2.5 * deltaTime
        if (window.pressed(GLFW_KEY_W)) cameraPos += cameraSpeed * cameraFront
        if (window.pressed(GLFW_KEY_S)) cameraPos -= cameraSpeed * cameraFront
        if (window.pressed(GLFW_KEY_A)) cameraPos -= (cameraFront cross cameraUp).normalizeAssign() * cameraSpeed
        if (window.pressed(GLFW_KEY_D)) cameraPos += (cameraFront cross cameraUp).normalizeAssign() * cameraSpeed

        // TODO up/down?
    }

    /** glfw: whenever the mouse moves, this callback is called */
    fun mouseCallback(xpos: Double, ypos: Double) {

        if (firstMouse) {
            last.put(xpos, ypos)
            firstMouse = false
        }

        var xoffset = xpos - last.x
        var yoffset = last.y - ypos // reversed since y-coordinates go from bottom to top
        last.put(xpos, ypos)

        val sensitivity = 0.1f // change this value to your liking
        xoffset *= sensitivity
        yoffset *= sensitivity

        yaw += xoffset.f
        pitch += yoffset.f

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        pitch = glm.clamp(pitch, -89f, 89f)

        val front = Vec3(
                x = cos(glm.radians(yaw)) * cos(glm.radians(pitch)), // classic glm
                y = sin(pitch.rad),                 // one glm alternative
                z = yaw.rad.sin * pitch.rad.cos)    // another glm alternative
        cameraFront = front.normalizeAssign()
    }
}