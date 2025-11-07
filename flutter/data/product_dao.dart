import 'package:sqflite/sqflite.dart';
import '../models/product.dart';

class ProductDao {
  final Database database;

  ProductDao(this.database);

  Future<List<Product>> getPendingProducts() async {
    final List<Map<String, dynamic>> maps = await database.query(
      'products',
      where: 'isCompleted = ?',
      whereArgs: [0],
      orderBy: 'id DESC',
    );
    return maps.map((map) => Product.fromSqlite(map)).toList();
  }

  Future<List<Product>> getCompletedProducts() async {
    final List<Map<String, dynamic>> maps = await database.query(
      'products',
      where: 'isCompleted = ?',
      whereArgs: [1],
      orderBy: 'id DESC',
    );
    return maps.map((map) => Product.fromSqlite(map)).toList();
  }

  Stream<List<Product>> watchPendingProducts() async* {
    while (true) {
      final products = await getPendingProducts();
      yield products;
      await Future.delayed(Duration(seconds: 2));
    }
  }

  Stream<List<Product>> watchCompletedProducts() async* {
    while (true) {
      final products = await getCompletedProducts();
      yield products;
      await Future.delayed(Duration(seconds: 2));
    }
  }

  Future<int> getPendingCount() async {
    final List<Map<String, dynamic>> result = await database.rawQuery(
      'SELECT COUNT(*) FROM products WHERE isCompleted = 0'
    );
    return result.first['COUNT(*)'] as int;
  }

  Future<int> getCompletedCount() async {
    final List<Map<String, dynamic>> result = await database.rawQuery(
      'SELECT COUNT(*) FROM products WHERE isCompleted = 1'
    );
    return result.first['COUNT(*)'] as int;
  }

  Stream<int> watchPendingCount() async* {
    while (true) {
      final count = await getPendingCount();
      yield count;
      await Future.delayed(Duration(seconds: 2));
    }
  }

  Stream<int> watchCompletedCount() async* {
    while (true) {
      final count = await getCompletedCount();
      yield count;
      await Future.delayed(Duration(seconds: 2));
    }
  }

  Future<int> insert(Product product) async {
    return await database.insert('products', product.toSqlite());
  }

  Future<int> update(Product product) async {
    return await database.update(
      'products',
      product.toSqlite(),
      where: 'id = ?',
      whereArgs: [product.id],
    );
  }

  Future<int> delete(Product product) async {
    return await database.delete(
      'products',
      where: 'id = ?',
      whereArgs: [product.id],
    );
  }

  Future<int> deleteAllCompleted() async {
    return await database.delete(
      'products',
      where: 'isCompleted = ?',
      whereArgs: [1],
    );
  }

  Future<List<Product>> searchProducts(String query) async {
    final List<Map<String, dynamic>> maps = await database.query(
      'products',
      where: 'name LIKE ?',
      whereArgs: ['%$query%'],
      orderBy: 'id DESC',
    );
    return maps.map((map) => Product.fromSqlite(map)).toList();
  }
}