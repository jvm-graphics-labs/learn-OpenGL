package learnOpenGL.common

import assimp.*
import assimp.AiPostProcessSteps.*
import glm.vec2.Vec2

/**
 * Created by GBarbieri on 02.05.2017.
 */

class Model(path: String, val gammaCorrection: Boolean = false) {

    /*  Model Data */
    val meshes = ArrayList<mesh>()

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

        // Load all Diffuse, Specular, Normal and Height maps

    }

    /** Processes a node in a recursive fashion. Processes each individual mesh located at the node and repeats this
     * process on its children nodes (if any).  */
    fun processNode(node: AiNode, scene: AiScene) {

        // Process each mesh located at the current node
        scene.mMeshes.forEach {
            // The node object only contains indices to index the actual objects in the scene.
            // The scene contains all the data, node is just to keep stuff organized (like relations between nodes).
            meshes +=
        }
    }

    fun processMesh(mesh: AiMesh, scene: AiScene): mesh {

        // Data to fill
        val vertices = ArrayList<Vertex>()
        val indices = ArrayList<Int>()
        val textures = ArrayList<Texture>()

        // Walk through each of the mesh's vertices
        for (i in 0 until mesh.mNumVertices)

            vertices += Vertex(
                    position = mesh.mVertices[i],
                    normal = mesh.mVertices[i],
                    /*  A vertex can contain up to 8 different texture coordinates. We thus make the assumption that we
                        won't use models where a vertex can have multiple texture coordinates so we always take the
                        first set (0).  */
                    texCoords =
                    if (mesh.mTextureCoords.isNotEmpty())
                        Vec2(mesh.mTextureCoords[0][i])
                    else
                        Vec2(),
                    tangent = mesh.mTangents[i],
                    bitangent = mesh.mBitangents[i])

        /* Now wak through each of the mesh's faces (a face is a mesh its triangle) and retrieve the corresponding
         * vertex indices.  */
        mesh.mFaces.forEach { indices += it }   // Retrieve all indices of the face and store them in the indices vector

        // Process materials
        if(mesh.mMaterialIndex >= 0) {

            val material = scene.mMaterials[mesh.mMaterialIndex]

            /*  We assume a convention for sampler names in the shaders. Each diffuse texture should be named as
                'texture_diffuseN' where N is a sequential number ranging from 1 to MAX_SAMPLER_NUMBER.
                Same applies to other texture as the following list summarizes:
                Diffuse: texture_diffuseN
                Specular: texture_specularN
                Normal: texture_normalN */

            // Load all Diffuse, Specular, Normal and Height maps
            textures.addAll(loadMaterialTextures(material, AiTexture.Type.diffuse, "texture_diffuse"))
        }
    }

    /**
     * Checks all material textures of a given type and loads the textures if they're not loaded yet.
     * The required info is returned as a Texture struct.
     */
    fun loadMaterialTextures(mat: AiMaterial, type: AiTexture.Type, typeName: String): List<Texture> {

        val textures = ArrayList<Texture>()


    }
}