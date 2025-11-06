package com.example.shoppinglist

import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun registerUser(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Error al crear usuario")

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

    // CORREGIDO: Incluir documentId en los productos
    fun getProducts(completed: Boolean): Flow<List<Product>> = callbackFlow {
        val listener = firestore.collection("shared_products")
            .whereEqualTo("completed", completed)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply {
                        documentId = doc.id  // IMPORTANTE: Guardar el ID del documento
                        id = doc.id.hashCode()
                    }
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    suspend fun insertProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")
            val userName = getCurrentUser()?.email ?: "Usuario desconocido"

            val productData = hashMapOf(
                "name" to product.name,
                "quantity" to product.quantity,
                "category" to product.category,
                "completed" to false,  // Siempre comienza como no completado
                "timestamp" to System.currentTimeMillis(),
                "createdBy" to userId,
                "createdByEmail" to userName
            )

            firestore.collection("shared_products")
                .add(productData)
                .await()

            updateProductCount(userId, true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CORREGIDO: Usar documentId para actualizar
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            if (product.documentId.isEmpty()) {
                return Result.failure(Exception("Product documentId is empty"))
            }

            // Obtener el documento actual para verificar el estado previo
            val docRef = firestore.collection("shared_products").document(product.documentId)
            val currentDoc = docRef.get().await()
            val wasCompleted = currentDoc.getBoolean("completed") ?: false

            // Actualizar usando el documentId directamente
            val updates = mutableMapOf<String, Any>(
                "completed" to product.isCompleted
            )

            if (product.isCompleted) {
                updates["completedBy"] = userId
                updates["completedAt"] = System.currentTimeMillis()
            } else {
                // Para eliminar campos en Firestore, usar FieldValue.delete()
                updates["completedBy"] = com.google.firebase.firestore.FieldValue.delete()
                updates["completedAt"] = com.google.firebase.firestore.FieldValue.delete()
            }

            docRef.update(updates).await()

            // Si cambió a completado, actualizar contador
            if (!wasCompleted && product.isCompleted) {
                updateProductCount(userId, false, true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // CORREGIDO: Usar documentId para eliminar
    suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            if (product.documentId.isEmpty()) {
                return Result.failure(Exception("Product documentId is empty"))
            }

            firestore.collection("shared_products")
                .document(product.documentId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllCompleted(): Result<Unit> {
        return try {
            val userId = getCurrentUser()?.uid ?: throw Exception("Usuario no autenticado")

            val querySnapshot = firestore.collection("shared_products")
                .whereEqualTo("completed", true)
                .get()
                .await()

            val batch = firestore.batch()
            var userCompletedCount = 0

            querySnapshot.documents.forEach { doc ->
                if (doc.getString("completedBy") == userId) {
                    userCompletedCount++
                }
                batch.delete(doc.reference)
            }
            batch.commit().await()

            if (userCompletedCount > 0) {
                val userDoc = firestore.collection("users").document(userId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userDoc)
                    val currentCompleted = snapshot.getLong("productsCompleted") ?: 0
                    transaction.update(
                        userDoc,
                        "productsCompleted",
                        (currentCompleted - userCompletedCount).coerceAtLeast(0)
                    )
                }.await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateProductCount(userId: String, isAdded: Boolean, isCompleted: Boolean = false) {
        try {
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

    fun getUserData(): Flow<User?> = callbackFlow {
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

                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }

        awaitClose { listener.remove() }
    }
}