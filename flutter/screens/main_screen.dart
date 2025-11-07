import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/firebase_repository.dart';
import '../view_models/product_viewmodel.dart';
import '../widgets/product_adapter.dart';
import 'login_screen.dart';
import 'profile_screen.dart';

class MainScreen extends StatefulWidget {
  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  @override
  void initState() {
    super.initState();
    _checkAuthentication();
  }

  void _checkAuthentication() {
    final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
    if (firebaseRepo.getCurrentUser() == null) {
      _goToLogin();
    }
  }

  @override
  Widget build(BuildContext context) {
    final firebaseRepo = Provider.of<FirebaseRepository>(context);
    
    // Verificar autenticación en build también
    if (firebaseRepo.getCurrentUser() == null) {
      return Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: Text('Lista de Compras'),
          backgroundColor: Colors.blue,
          actions: [
            IconButton(
              icon: Icon(Icons.person),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => ProfileScreen()),
                );
              },
            ),
          ],
          bottom: TabBar(
            tabs: [
              Tab(text: 'Pendientes'),
              Tab(text: 'Completados'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            // Pestaña de productos pendientes
            _buildProductList(false),
            // Pestaña de productos completados
            _buildProductList(true),
          ],
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: _showAddProductDialog,
          child: Icon(Icons.add),
          backgroundColor: Colors.blue,
        ),
      ),
    );
  }

  Widget _buildProductList(bool showCompleted) {
    final viewModel = Provider.of<ProductViewModel>(context);

    return StreamBuilder<List<Product>>(
      stream: showCompleted 
          ? viewModel.completedProducts 
          : viewModel.pendingProducts,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(child: CircularProgressIndicator());
        }

        if (snapshot.hasError) {
          return Center(child: Text('Error: ${snapshot.error}'));
        }

        final products = snapshot.data ?? [];

        if (products.isEmpty) {
          return _buildEmptyState(showCompleted);
        }

        return ProductAdapter(
          products: products,
          onProductTap: (product) => _onProductTap(product),
          onDeleteTap: (product) => _onDeleteTap(product),
          onToggleCompletion: (isCompleted, product) => 
              _updateProduct(product.copyWith(isCompleted: isCompleted)),
        );
      },
    );
  }

  Widget _buildEmptyState(bool showCompleted) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            showCompleted ? Icons.check_circle_outline : Icons.shopping_cart,
            size: 80,
            color: Colors.grey[300],
          ),
          SizedBox(height: 16),
          Text(
            showCompleted 
                ? 'No hay productos completados'
                : 'No hay productos pendientes',
            style: TextStyle(fontSize: 18, color: Colors.grey[600]),
          ),
          SizedBox(height: 8),
          if (!showCompleted)
            Text(
              'Presiona el botón + para agregar uno',
              style: TextStyle(color: Colors.grey[500]),
            ),
        ],
      ),
    );
  }

  void _showAddProductDialog() {
    final nameController = TextEditingController();
    final quantityController = TextEditingController();
    final categoryController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Agregar Producto'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameController,
                decoration: InputDecoration(
                  labelText: 'Nombre del producto',
                  border: OutlineInputBorder(),
                ),
              ),
              SizedBox(height: 12),
              TextField(
                controller: quantityController,
                decoration: InputDecoration(
                  labelText: 'Cantidad',
                  border: OutlineInputBorder(),
                ),
              ),
              SizedBox(height: 12),
              TextField(
                controller: categoryController,
                decoration: InputDecoration(
                  labelText: 'Categoría',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () {
              final name = nameController.text.trim();
              if (name.isNotEmpty) {
                final product = Product(
                  name: name,
                  quantity: quantityController.text.trim().isNotEmpty 
                      ? quantityController.text.trim() 
                      : '1 unidad',
                  category: categoryController.text.trim().isNotEmpty
                      ? categoryController.text.trim()
                      : 'General',
                  isCompleted: false,
                  timestamp: DateTime.now().millisecondsSinceEpoch,
                );
                _insertProduct(product);
                Navigator.pop(context);
              }
            },
            child: Text('Agregar'),
          ),
        ],
      ),
    );
  }

  void _insertProduct(Product product) async {
    try {
      final viewModel = Provider.of<ProductViewModel>(context, listen: false);
      await viewModel.insert(product);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al agregar producto: $e')),
      );
    }
  }

  void _updateProduct(Product product) async {
    try {
      final viewModel = Provider.of<ProductViewModel>(context, listen: false);
      await viewModel.update(product);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al actualizar producto: $e')),
      );
    }
  }

  void _onProductTap(Product product) {
    _showEditProductDialog(product);
  }

  void _onDeleteTap(Product product) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Eliminar Producto'),
        content: Text('¿Estás seguro de que quieres eliminar "${product.name}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Cancelar'),
          ),
          TextButton(
            onPressed: () {
              _deleteProduct(product);
              Navigator.pop(context);
            },
            child: Text('Eliminar', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
  }

  void _deleteProduct(Product product) async {
    try {
      final viewModel = Provider.of<ProductViewModel>(context, listen: false);
      await viewModel.delete(product);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al eliminar producto: $e')),
      );
    }
  }

  void _showEditProductDialog(Product product) {
    final nameController = TextEditingController(text: product.name);
    final quantityController = TextEditingController(text: product.quantity);
    final categoryController = TextEditingController(text: product.category);

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Editar Producto'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: nameController,
                decoration: InputDecoration(
                  labelText: 'Nombre del producto',
                  border: OutlineInputBorder(),
                ),
              ),
              SizedBox(height: 12),
              TextField(
                controller: quantityController,
                decoration: InputDecoration(
                  labelText: 'Cantidad',
                  border: OutlineInputBorder(),
                ),
              ),
              SizedBox(height: 12),
              TextField(
                controller: categoryController,
                decoration: InputDecoration(
                  labelText: 'Categoría',
                  border: OutlineInputBorder(),
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () {
              final updatedProduct = product.copyWith(
                name: nameController.text.trim(),
                quantity: quantityController.text.trim(),
                category: categoryController.text.trim(),
              );
              _updateProduct(updatedProduct);
              Navigator.pop(context);
            },
            child: Text('Actualizar'),
          ),
        ],
      ),
    );
  }

  void _goToLogin() {
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (context) => LoginScreen()),
      (route) => false,
    );
  }
}