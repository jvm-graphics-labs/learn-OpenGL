package learnOpenGL.common

import assimp.AiNode
import assimp.AiScene
import assimp.Importer
import uno.kotlin.uri

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Model(path: String) {

    /*  Model Data */
    val meshes = ArrayList<Mesh>()

    /** Loads a model with supported ASSIMP extensions from file and stores the resulting meshes in the meshes vector.  */
    init {
        // Read file via ASSIMP
        // TODO i and check class if needed
//        val scene = Importer().readFile(path.uri, Triangulate.i or FlipUVs.i or CalcTangentSpace.i)
        val scene = Importer().readFile(path.uri, 0)
        // Check for errors
        if (scene == null) // if is Not Zero
//        if(!scene || scene->mFlags & AI_SCENE_FLAGS_INCOMPLETE || !scene->mRootNode) // if is Not Zero
        {
            throw Error("")
        }
//        // Retrieve the directory path of the filepath
//        this->directory = path.substr(0, path.find_last_of('/'));

        // Process ASSIMP's root node recursively
        processNode(scene.rootNode, scene)
    }

    /** Processes a node in a recursive fashion. Processes each individual mesh located at the node and repeats this
     * process on its children nodes (if any).  */
    fun processNode(node: AiNode, scene: AiScene) {
        // Process each mesh located at the current node
        scene.meshes.forEach {
            // The node object only contains indices to index the actual objects in the scene.
            // The scene contains all the data, node is just to keep stuff organized (like relations between nodes).
            meshes += Mesh(it, scene)
        }
    }

    fun draw(diffuse: Boolean = false, specular: Boolean = false) = meshes.forEach { it.draw(diffuse, specular) }

    fun dispose() = meshes.forEach(Mesh::dispose)
}