import 'package:flutter/material.dart';
import '../models/product.dart';

class ProductAdapter extends StatefulWidget {
  final List<Product> products;
  final Function(Product) onProductTap;
  final Function(Product) onDeleteTap;
  final Function(bool, Product) onToggleCompletion;

  const ProductAdapter({
    Key? key,
    required this.products,
    required this.onProductTap,
    required this.onDeleteTap,
    required this.onToggleCompletion,
  }) : super(key: key);

  @override
  _ProductAdapterState createState() => _ProductAdapterState();
}

class _ProductAdapterState extends State<ProductAdapter> {
  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: widget.products.length,
      itemBuilder: (context, index) {
        final product = widget.products[index];
        return _ProductItem(
          product: product,
          onTap: () => widget.onProductTap(product),
          onDelete: () => widget.onDeleteTap(product),
          onToggleCompletion: (isCompleted) => 
              widget.onToggleCompletion(isCompleted, product),
        );
      },
    );
  }
}

class _ProductItem extends StatelessWidget {
  final Product product;
  final VoidCallback onTap;
  final VoidCallback onDelete;
  final Function(bool) onToggleCompletion;

  const _ProductItem({
    Key? key,
    required this.product,
    required this.onTap,
    required this.onDelete,
    required this.onToggleCompletion,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      elevation: 2,
      child: ListTile(
        leading: Checkbox(
          value: product.isCompleted,
          onChanged: (bool? value) {
            if (value != null) {
              onToggleCompletion(value);
            }
          },
          activeColor: Colors.green,
        ),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              product.name,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                decoration: product.isCompleted 
                    ? TextDecoration.lineThrough 
                    : TextDecoration.none,
                color: product.isCompleted ? Colors.grey : Colors.black,
              ),
            ),
            if (product.createdByEmail.isNotEmpty) ...[
              SizedBox(height: 2),
              Text(
                'Agregado por: ${product.createdByEmail}',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                  fontStyle: FontStyle.italic,
                ),
              ),
            ],
          ],
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(height: 4),
            Row(
              children: [
                _buildInfoChip(
                  'Cantidad: ${product.quantity}',
                  Colors.blue,
                ),
                SizedBox(width: 8),
                _buildInfoChip(
                  product.category,
                  Colors.orange,
                ),
              ],
            ),
            if (product.completedBy != null && product.isCompleted) ...[
              SizedBox(height: 4),
              Text(
                'Completado por: ${product.completedBy}',
                style: TextStyle(
                  fontSize: 11,
                  color: Colors.green[700],
                  fontStyle: FontStyle.italic,
                ),
              ),
            ],
          ],
        ),
        trailing: IconButton(
          icon: Icon(Icons.delete, color: Colors.red),
          onPressed: onDelete,
        ),
        onTap: onTap,
      ),
    );
  }

  Widget _buildInfoChip(String text, Color color) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.3)),
      ),
      child: Text(
        text,
        style: TextStyle(
          fontSize: 12,
          color: color,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}