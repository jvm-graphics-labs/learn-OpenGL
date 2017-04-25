package learnOpenGL.common

import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import java.io.File
import kotlin.reflect.KClass

/**
 * Created by elect on 24/04/17.
 */

class Shader(root: String, vertexName: String, fragmentName: String) {

    constructor(root: String, shader: String) : this(root, shader, shader)

    val id: Int

    init {

        //  1. retrieve the vertex/fragment source code from filePath
        val vShaderCode = readFile("$root/$vertexName.vert")
        val fShaderCode = readFile("$root/$fragmentName.frag")

        //  2. compile shaders
        val vertex = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertex, vShaderCode)
        glCompileShader(vertex)
        checkCompileErrors(vertex, "vertex")
        // fragment Shader
        val fragment = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragment, fShaderCode)
        glCompileShader(fragment)
        checkCompileErrors(fragment, "fragment")
        // shader Program
        id = glCreateProgram()
        glAttachShader(id, vertex)
        glAttachShader(id, fragment)
        glLinkProgram(id)
        checkCompileErrors(id, "program")
        // delete the shaders as they're linked into our program now and no longer necessary
        glDeleteShader(vertex)
        glDeleteShader(fragment)
    }

    fun readFile(filePath: String) = File(javaClass.classLoader.getResource(filePath).toURI()).readText()

    /** activate the shader */
    fun use() = glUseProgram(id)
}

fun shaderOf(context: KClass<*>, root: String, shader: String) = shaderOf(context, root, shader, shader)

fun shaderOf(context: KClass<*>, root: String, vertexName: String, fragmentName: String): Int {

    //  1. retrieve the vertex/fragment source code from filePath
    val vShaderCode = readFile("$root/$vertexName.vert")
    val fShaderCode = readFile("$root/$fragmentName.frag")

    //  2. compile shaders
    val vertex = glCreateShader(GL_VERTEX_SHADER)
    glShaderSource(vertex, vShaderCode)
    glCompileShader(vertex)
    checkCompileErrors(vertex, "vertex")
    // fragment Shader
    val fragment = glCreateShader(GL_FRAGMENT_SHADER)
    glShaderSource(fragment, fShaderCode)
    glCompileShader(fragment)
    checkCompileErrors(fragment, "fragment")
    // shader Program
    val id = glCreateProgram()
    glAttachShader(id, vertex)
    glAttachShader(id, fragment)
    glLinkProgram(id)
    checkCompileErrors(id, "program")
    // delete the shaders as they're linked into our program now and no longer necessary
    glDeleteShader(vertex)
    glDeleteShader(fragment)

    return id
}

/** utility function for checking shader compilation/linking errors.    */
fun checkCompileErrors(shader: Int, type: String) {

    if (type != "program") {

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {

            val infoLog = glGetShaderInfoLog(shader)
            System.err.println("ERROR::SHADER_COMPILATION_ERROR of type: $type\n$infoLog")
        }
    } else {

        if (glGetProgrami(shader, GL_LINK_STATUS) == GL_FALSE) {

            val infoLog = glGetProgramInfoLog(shader)
            System.err.println("ERROR::PROGRAM_LINKING_ERROR of type: $type\n$infoLog")
        }
    }
}