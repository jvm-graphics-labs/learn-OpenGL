package learnOpenGL.common

import glm_.cos
import glm_.glm
import glm_.rad
import glm_.sin
import glm_.vec3.Vec3
import learnOpenGL.common.Camera.Movement.*

/**
 * Created by GBarbieri on 27.04.2017.
 */

/** An abstract camera class that processes input and calculates the corresponding Eular Angles, Vectors and Matrices
 *  for use in OpenGL   */
class Camera(
        //  Camera Attributes
        var position: Vec3 = Vec3(),
        var worldUp: Vec3 = Vec3(0.0f, 1.0, 0.0f),
        //  Eular Angles
        var yaw: Float = -90.0f,
        var pitch: Float = 0.0f) {

    //  Camera Attributes
    var front = Vec3(0.0f, 0.0f, -1.0f)
    var up = Vec3()
    var right = Vec3()
    //  Camera options
    var movementSpeed = 2.5f
    var mouseSensitivity = 0.5f
    var zoom = 45.0f

    /** Constructor with scalar values  */
    constructor(posX: Float, posY: Float, posZ: Float, upX: Float, upY: Float, upZ: Float, yaw: Float, pitch: Float) :
            this(Vec3(posX, posY, posZ), Vec3(upX, upY, upZ), yaw, pitch)

    init {
        updateCameraVectors()
    }

    /** Returns the view matrix calculated using Eular Angles and the LookAt Matrix */
    val viewMatrix get() = glm.lookAt(position, position + front, up)

    /**  Processes input received from any keyboard-like input system. Accepts input parameter in the form of camera
     *   defined ENUM (to abstract it from windowing systems)    */
    fun processKeyboard(direction: Movement, deltaTime: Float) {

        val velocity = movementSpeed * deltaTime

        position plus_ when (direction) {
            Forward -> front * velocity
            Backward -> -front * velocity
            Left -> -right * velocity
            Right -> right * velocity
        }
    }

    /** Processes input received from a mouse input system. Expects the offset value in both the x and y direction. */
    fun processMouseMovement(xOffset: Float, yOffset: Float, constrainPitch: Boolean = true) {

        val x = xOffset * mouseSensitivity
        val y = yOffset * mouseSensitivity

        yaw += x
        pitch += y

        // Make sure that when pitch is out of bounds, screen doesn't get flipped
        if (constrainPitch)
            pitch = glm.clamp(pitch, -89.0f, 89.0f)

        // Update Front, Right and Up Vectors using the updated Eular angles
        updateCameraVectors()
    }

    /** Processes input received from a mouse scroll-wheel event. Only requires input on the vertical wheel-axis    */
    fun processMouseScroll(yOffset: Float) {

        if (zoom in 1.0f..45.0f)
            zoom -= yOffset

        zoom = glm.clamp(zoom, 1.0f, 45.0f)
    }

    /** Calculates the front vector from the Camera's (updated) Eular Angles    */
    fun updateCameraVectors() {
        // Calculate the new Front vector
        front.put(
                x = yaw.rad.cos * pitch.rad.cos,
                y = pitch.rad.sin,
                z = yaw.rad.sin * pitch.rad.cos).normalize_()
        /*  Also re-calculate the Right and Up vector, by taking care to normalize the vectors, because their length
            gets closer to 0 the more you look up or down which results in slower movement.         */
        right put (front cross worldUp).normalize_()
        up put (right cross front).normalize_()
    }

    enum class Movement { Forward, Backward, Left, Right }
}