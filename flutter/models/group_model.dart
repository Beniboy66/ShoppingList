class Group {
  final String groupId;
  final String name;
  final List<String> members;
  final int createdAt;

  Group({
    this.groupId = "",
    this.name = "",
    List<String>? members,
    int? createdAt,
  })  : members = members ?? [],
        createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  Map<String, dynamic> toFirestore() {
    return {
      'groupId': groupId,
      'name': name,
      'members': members,
      'createdAt': createdAt,
    };
  }

  factory Group.fromFirestore(Map<String, dynamic> data, String documentId) {
    return Group(
      groupId: documentId,
      name: data['name'] ?? '',
      members: List<String>.from(data['members'] ?? []),
      createdAt: data['createdAt'] ?? DateTime.now().millisecondsSinceEpoch,
    );
  }

  Map<String, dynamic> toSqlite() {
    return {
      'groupId': groupId,
      'name': name,
      'members': members.join(','),
      'createdAt': createdAt,
    };
  }

  factory Group.fromSqlite(Map<String, dynamic> data) {
    return Group(
      groupId: data['groupId'] ?? '',
      name: data['name'] ?? '',
      members: (data['members'] as String).split(',').where((m) => m.isNotEmpty).toList(),
      createdAt: data['createdAt'],
    );
  }

  Group copyWith({
    String? groupId,
    String? name,
    List<String>? members,
    int? createdAt,
  }) {
    return Group(
      groupId: groupId ?? this.groupId,
      name: name ?? this.name,
      members: members ?? this.members,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  bool isMember(String userId) {
    return members.contains(userId);
  }
}