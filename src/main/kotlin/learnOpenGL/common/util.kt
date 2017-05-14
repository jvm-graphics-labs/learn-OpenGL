package learnOpenGL.common

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uno.buffer.toByteBuffer
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.nio.ByteBuffer
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

fun BufferedImage.toByteBuffer() = (raster.dataBuffer as DataBufferByte).data.toByteBuffer()

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

    val texture = gli.load(path)
    val format = gli.gl.translate(texture.format, texture.swizzles)

    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)
    uno.gln.glTexImage2D(format, texture)
    GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

    texture.dispose()

    return textureID
}