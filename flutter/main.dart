import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:sqflite/sqflite.dart';

import 'data/database_service.dart';
import 'data/repositories/product_repository.dart';
import 'services/firebase_repository.dart';
import 'view_models/product_viewmodel.dart';
import 'screens/main_screen.dart';
import 'screens/login_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Inicializar base de datos
  final databaseService = DatabaseService();
  await databaseService.database;
  
  final productDao = databaseService.productDao;
  final productRepository = ProductRepository(productDao);
  final productViewModel = ProductViewModel(productRepository);

  runApp(
    MultiProvider(
      providers: [
        Provider<FirebaseRepository>(create: (_) => FirebaseRepository()),
        ChangeNotifierProvider<ProductViewModel>(
          create: (_) => productViewModel,
        ),
      ],
      child: MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Shopping List',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: Consumer<FirebaseRepository>(
        builder: (context, firebaseRepo, child) {
          final currentUser = firebaseRepo.getCurrentUser();
          
          if (currentUser != null) {
            return MainScreen();
          } else {
            return LoginScreen();
          }
        },
      ),
      routes: {
        '/main': (context) => MainScreen(),
        '/login': (context) => LoginScreen(),
      },
      debugShowCheckedModeBanner: false,
    );
  }
}