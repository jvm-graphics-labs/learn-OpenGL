package learnOpenGL.common

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

fun readImageFile(filePath: String): ByteArray {

    val url = GlfwWindow::javaClass.javaClass.classLoader.getResource(filePath)
    val file = File(url.toURI())

    val bufferedImage = ImageIO.read(file)

    // get DataBufferBytes from Raster
    val raster = bufferedImage.raster

    return (raster.dataBuffer as DataBufferByte).data
}