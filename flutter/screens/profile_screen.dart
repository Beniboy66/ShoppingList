import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../services/firebase_repository.dart';
import '../models/user.dart' as app_user;
import 'login_screen.dart';

class ProfileScreen extends StatefulWidget {
  @override
  _ProfileScreenState createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  StreamSubscription<app_user.User?>? _userSubscription;
  
  app_user.User? _currentUser;
  int _addedCount = 0;
  int _completedCount = 0;

  @override
  void initState() {
    super.initState();
    _initializeData();
  }

  void _initializeData() {
    final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
    final currentUser = firebaseRepo.getCurrentUser();
    
    if (currentUser != null) {
      // Escuchar datos del usuario
      _userSubscription = firebaseRepo.getUserStream(currentUser.uid).listen((user) {
        if (mounted) {
          setState(() {
            _currentUser = user;
            if (user != null) {
              _addedCount = user.productsAdded;
              _completedCount = user.productsCompleted;
            }
          });
        }
      });
    }
  }

  @override
  void dispose() {
    _userSubscription?.cancel();
    super.dispose();
  }

  void _logout() {
    try {
      _userSubscription?.cancel();

      final firebaseRepo = Provider.of<FirebaseRepository>(context, listen: false);
      firebaseRepo.signOut();

      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => LoginScreen(fromLogout: true)),
        (route) => false,
      );
      
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al cerrar sesión: $e')),
      );
    }
  }

  void _clearCompleted() async {
    // Aquí implementarías la lógica para eliminar productos completados
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Productos completados eliminados')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final firebaseRepo = Provider.of<FirebaseRepository>(context);
    final currentUser = firebaseRepo.getCurrentUser();

    return Scaffold(
      appBar: AppBar(
        title: Text('Perfil'),
        backgroundColor: Colors.blue,
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            // Información del usuario
            _buildUserInfo(currentUser),
            SizedBox(height: 20),
            
            // Estadísticas
            _buildStatsSection(),
            SizedBox(height: 20),
            
            // Gráfico de Pie
            _buildPieChart(),
            SizedBox(height: 30),
            
            // Botones de acción
            _buildActionButtons(),
          ],
        ),
      ),
    );
  }

  Widget _buildUserInfo(User? currentUser) {
    return Card(
      elevation: 4,
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              _currentUser?.displayName ?? 'Usuario',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 8),
            Text(
              currentUser?.email ?? 'usuario@ejemplo.com',
              style: TextStyle(fontSize: 16, color: Colors.grey[600]),
            ),
            SizedBox(height: 8),
            Text(
              _buildMemberSinceText(),
              style: TextStyle(fontSize: 14, color: Colors.grey[500]),
            ),
          ],
        ),
      ),
    );
  }

  String _buildMemberSinceText() {
    if (_currentUser == null) return 'Miembro desde ...';
    
    final date = DateTime.fromMillisecondsSinceEpoch(_currentUser!.createdAt);
    final months = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    
    return 'Miembro desde ${months[date.month - 1]} ${date.year}';
  }

  Widget _buildStatsSection() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _buildStatCard('Agregados', _addedCount.toString(), Colors.green),
        _buildStatCard('Comprados', _completedCount.toString(), Colors.orange),
      ],
    );
  }

  Widget _buildStatCard(String title, String value, Color color) {
    return Card(
      color: color.withOpacity(0.1),
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            Text(
              value,
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: color),
            ),
            SizedBox(height: 4),
            Text(
              title,
              style: TextStyle(fontSize: 14, color: Colors.grey[600]),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPieChart() {
    final total = _addedCount + _completedCount;
    
    return Card(
      elevation: 4,
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            Text(
              'Estadísticas de Compras',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            SizedBox(height: 16),
            Container(
              height: 300,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  PieChart(
                    PieChartData(
                      sectionsSpace: 2,
                      centerSpaceRadius: 60,
                      startDegreeOffset: -90,
                      sections: _buildPieSections(),
                    ),
                  ),
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        'Total',
                        style: TextStyle(fontSize: 14, color: Colors.grey),
                      ),
                      Text(
                        total.toString(),
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.black,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            SizedBox(height: 16),
            _buildLegend(),
          ],
        ),
      ),
    );
  }

  List<PieChartSectionData> _buildPieSections() {
    final total = _addedCount + _completedCount;
    if (total == 0) {
      return [
        PieChartSectionData(
          color: Colors.grey[300]!,
          value: 1,
          title: 'Sin datos',
          radius: 80,
          titleStyle: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.grey),
        ),
      ];
    }

    return [
      PieChartSectionData(
        color: Color(0xFF4CAF50),
        value: _addedCount.toDouble(),
        title: '${((_addedCount / total) * 100).toStringAsFixed(1)}%',
        radius: 80,
        titleStyle: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.white),
      ),
      PieChartSectionData(
        color: Color(0xFFFF9800),
        value: _completedCount.toDouble(),
        title: '${((_completedCount / total) * 100).toStringAsFixed(1)}%',
        radius: 80,
        titleStyle: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.white),
      ),
    ];
  }

  Widget _buildLegend() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        _buildLegendItem('Agregados: $_addedCount', Color(0xFF4CAF50)),
        SizedBox(width: 20),
        _buildLegendItem('Comprados: $_completedCount', Color(0xFFFF9800)),
      ],
    );
  }

  Widget _buildLegendItem(String text, Color color) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        SizedBox(width: 8),
        Text(
          text,
          style: TextStyle(fontSize: 12, color: Colors.black87),
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    return Column(
      children: [
        ElevatedButton(
          onPressed: _clearCompleted,
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.orange,
            foregroundColor: Colors.white,
            minimumSize: Size(double.infinity, 50),
          ),
          child: Text('Limpiar Productos Comprados'),
        ),
        SizedBox(height: 12),
        ElevatedButton(
          onPressed: _logout,
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.red,
            foregroundColor: Colors.white,
            minimumSize: Size(double.infinity, 50),
          ),
          child: Text('Cerrar Sesión'),
        ),
      ],
    );
  }
}