package learnOpenGL.common

import uno.buffer.toByteBuffer
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

    val url = GlfwWindow::javaClass.javaClass.classLoader.getResource(filePath)
    val file = File(url.toURI())

    return ImageIO.read(file)
}

fun BufferedImage.toByteBuffer() = (raster.dataBuffer as DataBufferByte).data.toByteBuffer()