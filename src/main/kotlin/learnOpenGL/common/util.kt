package learnOpenGL.common

import gli_.gl
import gli_.gli
import gln.texture.glTexImage2D
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X
import org.lwjgl.opengl.GL30
import uno.buffer.toBuf
import uno.glfw.GlfwWindow
import uno.kotlin.uri
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO


/**
 * Created by elect on 23/04/2017.
 */


fun readFile(filePath: String): String {
    val url = GlfwWindow::javaClass.javaClass.classLoader.getResource(filePath)
    val file = File(url.toURI())
    return file.readText()
}

fun readImage(filePath: String): BufferedImage {

    val url = ClassLoader.getSystemResource(filePath)
    val file = File(url.toURI())

    return ImageIO.read(file)
}

fun BufferedImage.toBuffer() = (raster.dataBuffer as DataBufferByte).data.toBuf()

fun BufferedImage.flipY(): BufferedImage {

    var scanline1: Any? = null
    var scanline2: Any? = null

    for (i in 0 until height / 2) {

        scanline1 = raster.getDataElements(0, i, width, 1, scanline1)
        scanline2 = raster.getDataElements(0, height - i - 1, width, 1, scanline2)
        raster.setDataElements(0, i, width, 1, scanline2)
        raster.setDataElements(0, height - i - 1, width, 1, scanline1)
    }

    return this
}

fun loadTexture(path: String): Int {

    val textureID = GL11.glGenTextures()

    val texture = gli.load(path.uri)
    gli.gl.profile = gl.Profile.GL33
    val format = gli.gl.translate(texture.format, texture.swizzles)

    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)
    for (i in 0 until texture.levels()) {
        val extend = texture.extent(i)
        glTexImage2D(i, format.internal, extend.x, extend.y, format.external, format.type, texture.data(0, 0, i))
    }
    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

    texture.dispose()

    return textureID
}


fun loadCubemap(path: String, extension: String): Int {

    val textureID = GL11.glGenTextures()
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)

    listOf("right", "left", "top", "bottom", "back", "front").forEachIndexed { i, it ->

        val texture = gli.load("$path/$it.$extension".uri)
        gli.gl.profile = gl.Profile.GL33
        val format = gli.gl.translate(texture.format, texture.swizzles)

        val extend = texture.extent()
        GL11.glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, format.internal.i,
                extend.x, extend.y, 0, format.external.i, format.type.i, texture.data())

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

        texture.dispose()
    }

    return textureID
}