class User {
  final String uid;
  final String email;
  final String displayName;
  final int createdAt;
  final int productsAdded;
  final int productsCompleted;

  User({
    this.uid = "",
    this.email = "",
    this.displayName = "",
    int? createdAt,
    this.productsAdded = 0,
    this.productsCompleted = 0,
  }) : createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toMap() {
    return {
      'uid': uid,
      'email': email,
      'displayName': displayName,
      'createdAt': createdAt,
      'productsAdded': productsAdded,
      'productsCompleted': productsCompleted,
    };
  }

  factory User.fromMap(Map<String, dynamic> map) {
    return User(
      uid: map['uid'] ?? '',
      email: map['email'] ?? '',
      displayName: map['displayName'] ?? '',
      createdAt: map['createdAt'] ?? DateTime.now().millisecondsSinceEpoch,
      productsAdded: map['productsAdded'] ?? 0,
      productsCompleted: map['productsCompleted'] ?? 0,
    );
  }

  User copyWith({
    String? uid,
    String? email,
    String? displayName,
    int? createdAt,
    int? productsAdded,
    int? productsCompleted,
  }) {
    return User(
      uid: uid ?? this.uid,
      email: email ?? this.email,
      displayName: displayName ?? this.displayName,
      createdAt: createdAt ?? this.createdAt,
      productsAdded: productsAdded ?? this.productsAdded,
      productsCompleted: productsCompleted ?? this.productsCompleted,
    );
  }
}