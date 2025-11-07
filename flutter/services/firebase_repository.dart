import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/user.dart' as app_user;

class FirebaseRepository {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // Autenticación
  Future<UserCredential> createUserWithEmailAndPassword(
      String email, String password) async {
    return await _auth.createUserWithEmailAndPassword(
      email: email,
      password: password,
    );
  }

  Future<UserCredential> signInWithEmailAndPassword(
      String email, String password) async {
    return await _auth.signInWithEmailAndPassword(
      email: email,
      password: password,
    );
  }

  Future<void> signOut() async {
    await _auth.signOut();
  }

  User? getCurrentUser() {
    return _auth.currentUser;
  }

  // Usuarios en Firestore
  Future<void> saveUser(app_user.User user) async {
    await _firestore
        .collection('users')
        .doc(user.uid)
        .set(user.toMap());
  }

  Stream<app_user.User?> getUserStream(String uid) {
    return _firestore
        .collection('users')
        .doc(uid)
        .snapshots()
        .map((snapshot) {
      if (snapshot.exists) {
        return app_user.User.fromMap(snapshot.data()!);
      }
      return null;
    });
  }

  Future<app_user.User?> getUser(String uid) async {
    final snapshot = await _firestore
        .collection('users')
        .doc(uid)
        .get();
    
    if (snapshot.exists) {
      return app_user.User.fromMap(snapshot.data()!);
    }
    return null;
  }

  // Productos en Firestore
  Stream<List<Product>> getProductsStream() {
    return _firestore
        .collection('products')
        .orderBy('timestamp', descending: true)
        .snapshots()
        .map((snapshot) => snapshot.docs
            .map((doc) => Product.fromFirestore(doc.data(), doc.id))
            .toList());
  }

  Future<void> addProduct(Product product) async {
    await _firestore.collection('products').add(product.toFirestore());
  }

  Future<void> updateProduct(Product product) async {
    await _firestore
        .collection('products')
        .doc(product.documentId)
        .update(product.toFirestore());
  }

  Future<void> deleteProduct(String documentId) async {
    await _firestore
        .collection('products')
        .doc(documentId)
        .delete();
  }

  // Grupos en Firestore
  Future<void> createGroup(Group group) async {
    await _firestore
        .collection('groups')
        .doc(group.groupId)
        .set(group.toFirestore());
  }

  Stream<List<Group>> getGroupsStream(String userId) {
    return _firestore
        .collection('groups')
        .where('members', arrayContains: userId)
        .snapshots()
        .map((snapshot) => snapshot.docs
            .map((doc) => Group.fromFirestore(doc.data(), doc.id))
            .toList());
  }

  Future<void> addMemberToGroup(String groupId, String userId) async {
    await _firestore
        .collection('groups')
        .doc(groupId)
        .update({
      'members': FieldValue.arrayUnion([userId])
    });
  }
}