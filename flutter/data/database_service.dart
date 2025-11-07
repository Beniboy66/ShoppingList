import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../dao/product_dao.dart';
import '../models/product.dart';
import '../models/group.dart';

class DatabaseService {
  static Database? _database;
  ProductDao? _productDao;

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    _productDao = ProductDao(_database!);
    return _database!;
  }

  ProductDao get productDao {
    if (_productDao == null) {
      throw Exception('Database not initialized. Call database getter first.');
    }
    return _productDao!;
  }

  Future<Database> _initDatabase() async {
    String path = join(await getDatabasesPath(), 'shopping_database.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: _createTables,
    );
  }

  Future<void> _createTables(Database db, int version) async {
    await db.execute('''
      CREATE TABLE products(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        documentId TEXT,
        name TEXT,
        quantity TEXT,
        category TEXT,
        isCompleted INTEGER,
        timestamp INTEGER,
        createdBy TEXT,
        createdByEmail TEXT,
        completedBy TEXT,
        completedAt INTEGER
      )
    ''');

    await db.execute('''
      CREATE TABLE groups(
        groupId TEXT PRIMARY KEY,
        name TEXT,
        members TEXT,
        createdAt INTEGER
      )
    ''');
  }

  // Métodos legacy para compatibilidad
  Future<List<Product>> getProducts() async {
    final dao = await _getDao();
    return await dao.getAllProducts();
  }

  Future<int> insertProduct(Product product) async {
    final dao = await _getDao();
    return await dao.insert(product);
  }

  Future<int> updateProduct(Product product) async {
    final dao = await _getDao();
    return await dao.update(product);
  }

  Future<int> deleteProduct(int id) async {
    final dao = await _getDao();
    final products = await getProducts();
    final product = products.firstWhere((p) => p.id == id, orElse: () => Product());
    return await dao.delete(product);
  }

  Future<ProductDao> _getDao() async {
    await database;
    return _productDao!;
  }
}
