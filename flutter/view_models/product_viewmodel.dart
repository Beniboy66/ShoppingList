import 'package:flutter/foundation.dart';
import '../data/repositories/product_repository.dart';
import '../models/product.dart';

class ProductViewModel with ChangeNotifier {
  final ProductRepository _repository;

  List<Product> _pendingProducts = [];
  List<Product> _completedProducts = [];
  int _pendingCount = 0;
  int _completedCount = 0;

  ProductViewModel(this._repository) {
    _initializeStreams();
  }

  // Getters equivalentes a LiveData
  List<Product> get pendingProducts => _pendingProducts;
  List<Product> get completedProducts => _completedProducts;
  int get pendingCount => _pendingCount;
  int get completedCount => _completedCount;

  // Streams para widgets reactivos
  Stream<List<Product>> get pendingProductsStream => _repository.pendingProducts;
  Stream<List<Product>> get completedProductsStream => _repository.completedProducts;
  Stream<int> get pendingCountStream => _repository.pendingCount;
  Stream<int> get completedCountStream => _repository.completedCount;

  // Inicializar streams
  void _initializeStreams() {
    _repository.pendingProducts.listen((products) {
      _pendingProducts = products;
      notifyListeners();
    });

    _repository.completedProducts.listen((products) {
      _completedProducts = products;
      notifyListeners();
    });

    _repository.pendingCount.listen((count) {
      _pendingCount = count;
      notifyListeners();
    });

    _repository.completedCount.listen((count) {
      _completedCount = count;
      notifyListeners();
    });
  }

  // Métodos exactamente iguales al Kotlin original
  Future<void> insert(Product product) async {
    await _repository.insert(product);
  }

  Future<void> update(Product product) async {
    await _repository.update(product);
  }

  Future<void> delete(Product product) async {
    await _repository.delete(product);
  }

  Future<void> deleteAllCompleted() async {
    await _repository.deleteAllCompleted();
  }

  // Métodos adicionales útiles
  Future<List<Product>> searchProducts(String query) async {
    return await _repository.searchProducts(query);
  }

  Future<Map<String, int>> getStats() async {
    return await _repository.getStats();
  }

  Future<List<Product>> getAllProducts() async {
    return await _repository.getAllProducts();
  }
}