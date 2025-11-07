class Product {
  int id;
  String documentId;
  String name;
  String quantity;
  String category;
  bool isCompleted;
  int timestamp;
  String createdBy;
  String createdByEmail;
  String? completedBy;
  int? completedAt;

  Product({
    this.id = 0,
    this.documentId = "",
    this.name = "",
    this.quantity = "",
    this.category = "",
    this.isCompleted = false,
    int? timestamp,
    this.createdBy = "",
    this.createdByEmail = "",
    this.completedBy,
    this.completedAt,
  }) : timestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toFirestore() {
    return {
      'name': name,
      'quantity': quantity,
      'category': category,
      'completed': isCompleted,
      'timestamp': timestamp,
      'createdBy': createdBy,
      'createdByEmail': createdByEmail,
      'completedBy': completedBy,
      'completedAt': completedAt,
    };
  }

  factory Product.fromFirestore(Map<String, dynamic> data, String documentId) {
    return Product(
      documentId: documentId,
      name: data['name'] ?? '',
      quantity: data['quantity'] ?? '',
      category: data['category'] ?? '',
      isCompleted: data['completed'] ?? false,
      timestamp: data['timestamp'] ?? DateTime.now().millisecondsSinceEpoch,
      createdBy: data['createdBy'] ?? '',
      createdByEmail: data['createdByEmail'] ?? '',
      completedBy: data['completedBy'],
      completedAt: data['completedAt'],
    );
  }

  Map<String, dynamic> toSqlite() {
    return {
      'id': id,
      'documentId': documentId,
      'name': name,
      'quantity': quantity,
      'category': category,
      'isCompleted': isCompleted ? 1 : 0,
      'timestamp': timestamp,
      'createdBy': createdBy,
      'createdByEmail': createdByEmail,
      'completedBy': completedBy,
      'completedAt': completedAt,
    };
  }

  factory Product.fromSqlite(Map<String, dynamic> data) {
    return Product(
      id: data['id'],
      documentId: data['documentId'] ?? '',
      name: data['name'] ?? '',
      quantity: data['quantity'] ?? '',
      category: data['category'] ?? '',
      isCompleted: data['isCompleted'] == 1,
      timestamp: data['timestamp'],
      createdBy: data['createdBy'] ?? '',
      createdByEmail: data['createdByEmail'] ?? '',
      completedBy: data['completedBy'],
      completedAt: data['completedAt'],
    );
  }

  Product copyWith({
    int? id,
    String? documentId,
    String? name,
    String? quantity,
    String? category,
    bool? isCompleted,
    int? timestamp,
    String? createdBy,
    String? createdByEmail,
    String? completedBy,
    int? completedAt,
  }) {
    return Product(
      id: id ?? this.id,
      documentId: documentId ?? this.documentId,
      name: name ?? this.name,
      quantity: quantity ?? this.quantity,
      category: category ?? this.category,
      isCompleted: isCompleted ?? this.isCompleted,
      timestamp: timestamp ?? this.timestamp,
      createdBy: createdBy ?? this.createdBy,
      createdByEmail: createdByEmail ?? this.createdByEmail,
      completedBy: completedBy ?? this.completedBy,
      completedAt: completedAt ?? this.completedAt,
    );
  }
}