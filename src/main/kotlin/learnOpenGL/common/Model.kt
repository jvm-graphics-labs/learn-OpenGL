package learnOpenGL.common

import assimp.*
import assimp.AiPostProcessSteps.*
import glm.vec2.Vec2
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import uno.gln.glTexImage2D

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Model(
        path: String,
        val program: Int,
        val diffuseUnit: Int? = null,
        val specularUnit: Int? = null,
        val gammaCorrection: Boolean = false) {

    /*  Model Data */
    val meshes = ArrayList<Mesh>()

    /** Loads a model with supported ASSIMP extensions from file and stores the resulting meshes in the meshes vector.  */
    init {
        // Read file via ASSIMP
        val importer = Importer()
        val scene = importer.readFile(this::class.java, path, Triangulate.i or FlipUVs.i or CalcTangentSpace.i)
        // Check for errors
        if (scene == null) // if is Not Zero
//        if(!scene || scene->mFlags & AI_SCENE_FLAGS_INCOMPLETE || !scene->mRootNode) // if is Not Zero
        {
//            cout << "ERROR::ASSIMP:: " << importer.GetErrorString() << endl;
            throw Error("")
        }
//        // Retrieve the directory path of the filepath
//        this->directory = path.substr(0, path.find_last_of('/'));
//
//        // Process ASSIMP's root node recursively
        processNode(scene.mRootNode, scene)
    }

    /** Processes a node in a recursive fashion. Processes each individual mesh located at the node and repeats this
     * process on its children nodes (if any).  */
    fun processNode(node: AiNode, scene: AiScene) {
        // Process each mesh located at the current node
        scene.mMeshes.forEach {
            // The node object only contains indices to index the actual objects in the scene.
            // The scene contains all the data, node is just to keep stuff organized (like relations between nodes).
            meshes += Mesh(it, scene)
        }
    }
}