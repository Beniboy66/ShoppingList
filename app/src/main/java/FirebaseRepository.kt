package com.example.shoppinglist

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Authentication
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun registerUser(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error al crear usuario")

            // Crear documento de usuario en Firestore
            val userDoc = User(
                uid = user.uid,
                email = email,
                displayName = displayName
            )
            firestore.collection("users")
                .document(user.uid)
                .set(userDoc)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error al iniciar sesión")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // Firestore Products
    fun getProducts(completed: Boolean): Flow<List<Product>> = callbackFlow {
        val userId = getCurrentUser()?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .collection("products")
            .whereEqualTo("completed", completed)
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)
                } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    suspend fun insertProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            firestore.collection("users")
                .document(userId)
                .collection("products")
                .add(product)
                .await()

            // Actualizar contador
            updateProductCount(true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("products")
                .whereEqualTo("id", product.id)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentId = querySnapshot.documents[0].id
                firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(documentId)
                    .set(product)
                    .await()

                // Si se completó, actualizar contador
                if (product.isCompleted) {
                    updateProductCount(false, true)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("products")
                .whereEqualTo("id", product.id)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val documentId = querySnapshot.documents[0].id
                firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(documentId)
                    .delete()
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllCompleted(): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            val querySnapshot = firestore.collection("users")
                .document(userId)
                .collection("products")
                .whereEqualTo("completed", true)
                .get()
                .await()

            val batch = firestore.batch()
            querySnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateProductCount(isAdded: Boolean, isCompleted: Boolean = false) {
        try {
            val userId = getCurrentUser()?.uid ?: return
            val userDoc = firestore.collection("users").document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val currentAdded = snapshot.getLong("productsAdded") ?: 0
                val currentCompleted = snapshot.getLong("productsCompleted") ?: 0

                if (isAdded) {
                    transaction.update(userDoc, "productsAdded", currentAdded + 1)
                }
                if (isCompleted) {
                    transaction.update(userDoc, "productsCompleted", currentCompleted + 1)
                }
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserStats(): Flow<Pair<Int, Int>> = callbackFlow {
        val userId = getCurrentUser()?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val added = snapshot?.getLong("productsAdded")?.toInt() ?: 0
                val completed = snapshot?.getLong("productsCompleted")?.toInt() ?: 0

                trySend(Pair(added, completed))
            }

        awaitClose { listener.remove() }
    }
}