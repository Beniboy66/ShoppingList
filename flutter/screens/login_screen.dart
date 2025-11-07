import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/firebase_repository.dart';
import '../models/user.dart' as app_user;
import 'main_screen.dart';

class LoginScreen extends StatefulWidget {
  final bool fromLogout;

  const LoginScreen({this.fromLogout = false, Key? key}) : super(key: key);

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  bool _isLoading = false;
  bool _isRegisterLoading = false;

  @override
  void initState() {
    super.initState();
    _checkCurrentUser();
  }

  void _checkCurrentUser() async {
    if (!widget.fromLogout) {
      final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
      final currentUser = firebaseRepo.getCurrentUser();
      
      if (currentUser != null) {
        _goToMainScreen();
      }
    }
  }

  void _loginUser() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
    });

    try {
      final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
      final email = _emailController.text.trim();
      final password = _passwordController.text.trim();

      await firebaseRepo.signInWithEmailAndPassword(email, password);
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Inicio de sesión exitoso')),
      );
      
      _goToMainScreen();
      
    } catch (e) {
      String errorMessage = 'Error al iniciar sesión';
      
      if (e.toString().contains('wrong-password')) {
        errorMessage = 'Contraseña incorrecta';
      } else if (e.toString().contains('user-not-found')) {
        errorMessage = 'Usuario no encontrado';
      } else if (e.toString().contains('network-request-failed')) {
        errorMessage = 'Error de conexión. Verifica tu internet';
      } else if (e.toString().contains('invalid-email')) {
        errorMessage = 'Correo electrónico inválido';
      }
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(errorMessage)),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  void _showRegisterDialog() {
    final nameController = TextEditingController();
    final emailController = TextEditingController();
    final passwordController = TextEditingController();
    final confirmPasswordController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Registro'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: nameController,
                decoration: InputDecoration(
                  labelText: 'Nombre completo',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Por favor ingresa tu nombre';
                  }
                  return null;
                },
              ),
              SizedBox(height: 12),
              TextFormField(
                controller: emailController,
                decoration: InputDecoration(
                  labelText: 'Correo electrónico',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.emailAddress,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Por favor ingresa tu correo';
                  }
                  if (!value.contains('@')) {
                    return 'Correo electrónico inválido';
                  }
                  return null;
                },
              ),
              SizedBox(height: 12),
              TextFormField(
                controller: passwordController,
                decoration: InputDecoration(
                  labelText: 'Contraseña',
                  border: OutlineInputBorder(),
                ),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Por favor ingresa tu contraseña';
                  }
                  if (value.length < 6) {
                    return 'La contraseña debe tener al menos 6 caracteres';
                  }
                  return null;
                },
              ),
              SizedBox(height: 12),
              TextFormField(
                controller: confirmPasswordController,
                decoration: InputDecoration(
                  labelText: 'Confirmar contraseña',
                  border: OutlineInputBorder(),
                ),
                obscureText: true,
                validator: (value) {
                  if (value != passwordController.text) {
                    return 'Las contraseñas no coinciden';
                  }
                  return null;
                },
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
            onPressed: _isRegisterLoading ? null : () async {
              final name = nameController.text.trim();
              final email = emailController.text.trim();
              final password = passwordController.text.trim();
              final confirmPassword = confirmPasswordController.text.trim();

              if (name.isEmpty || email.isEmpty || password.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Completa todos los campos')),
                );
                return;
              }

              if (password != confirmPassword) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Las contraseñas no coinciden')),
                );
                return;
              }

              await _registerUser(email, password, name);
            },
            child: _isRegisterLoading 
                ? CircularProgressIndicator()
                : Text('Registrar'),
          ),
        ],
      ),
    );
  }

  Future<void> _registerUser(String email, String password, String displayName) async {
    setState(() {
      _isRegisterLoading = true;
    });

    try {
      final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
      
      // Registrar usuario en Firebase Auth
      final userCredential = await firebaseRepo.createUserWithEmailAndPassword(email, password);
      
      // Crear objeto User y guardar en Firestore
      final user = app_user.User(
        uid: userCredential.user!.uid,
        email: email,
        displayName: displayName,
        createdAt: DateTime.now().millisecondsSinceEpoch,
        productsAdded: 0,
        productsCompleted: 0,
      );
      
      await firebaseRepo.saveUser(user);
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Registro exitoso. ¡Bienvenido!')),
      );
      
      _goToMainScreen();
      
    } catch (e) {
      String errorMessage = 'Error al registrar usuario';
      
      if (e.toString().contains('email-already-in-use')) {
        errorMessage = 'Este correo ya está registrado';
      } else if (e.toString().contains('weak-password')) {
        errorMessage = 'Contraseña muy débil';
      }
      
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(errorMessage)),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isRegisterLoading = false;
        });
      }
    }
  }

  void _goToMainScreen() {
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (context) => MainScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding: EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                'Shopping List',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: Colors.blue,
                ),
              ),
              SizedBox(height: 40),
              TextFormField(
                controller: _emailController,
                decoration: InputDecoration(
                  labelText: 'Correo electrónico',
                  border: OutlineInputBorder(),
                  prefixIcon: Icon(Icons.email),
                ),
                keyboardType: TextInputType.emailAddress,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Por favor ingresa tu correo';
                  }
                  if (!value.contains('@')) {
                    return 'Correo electrónico inválido';
                  }
                  return null;
                },
              ),
              SizedBox(height: 16),
              TextFormField(
                controller: _passwordController,
                decoration: InputDecoration(
                  labelText: 'Contraseña',
                  border: OutlineInputBorder(),
                  prefixIcon: Icon(Icons.lock),
                ),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Por favor ingresa tu contraseña';
                  }
                  return null;
                },
              ),
              SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _loginUser,
                  child: _isLoading
                      ? CircularProgressIndicator()
                      : Text('Iniciar Sesión'),
                ),
              ),
              SizedBox(height: 16),
              TextButton(
                onPressed: _isLoading ? null : _showRegisterDialog,
                child: Text('¿No tienes cuenta? Regístrate'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
}