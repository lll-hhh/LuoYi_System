import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:luoyi_mobile/config/theme.dart';

class VehicleAddScreen extends StatefulWidget {
  const VehicleAddScreen({super.key});

  @override
  State<VehicleAddScreen> createState() => _VehicleAddScreenState();
}

class _VehicleAddScreenState extends State<VehicleAddScreen> {
  final _formKey = GlobalKey<FormState>();
  String _plateColor = 'YELLOW';
  String _vehicleType = 'HEAVY_TRUCK';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('添加车辆')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('基本信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(labelText: '车牌号'),
                validator: (v) => v?.isEmpty == true ? '请输入车牌号' : null,
              ),
              const SizedBox(height: 16),
              const Text('车牌颜色'),
              const SizedBox(height: 8),
              Wrap(
                spacing: 12,
                children: [
                  _buildColorChip('蓝牌', 'BLUE', AppTheme.primaryColor),
                  _buildColorChip('黄牌', 'YELLOW', AppTheme.warningColor),
                  _buildColorChip('绿牌', 'GREEN', AppTheme.successColor),
                ],
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _vehicleType,
                decoration: const InputDecoration(labelText: '车辆类型'),
                items: const [
                  DropdownMenuItem(value: 'LIGHT_TRUCK', child: Text('轻型货车')),
                  DropdownMenuItem(value: 'MEDIUM_TRUCK', child: Text('中型货车')),
                  DropdownMenuItem(value: 'HEAVY_TRUCK', child: Text('重型货车')),
                  DropdownMenuItem(value: 'CONTAINER', child: Text('集装箱车')),
                  DropdownMenuItem(value: 'TANKER', child: Text('罐车')),
                ],
                onChanged: (v) => setState(() => _vehicleType = v!),
              ),
              const SizedBox(height: 24),
              const Text('车辆参数', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '品牌'))),
                  const SizedBox(width: 16),
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '型号'))),
                ],
              ),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(labelText: '载重量(吨)'),
                keyboardType: TextInputType.number,
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '长(m)'), keyboardType: TextInputType.number)),
                  const SizedBox(width: 8),
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '宽(m)'), keyboardType: TextInputType.number)),
                  const SizedBox(width: 8),
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '高(m)'), keyboardType: TextInputType.number)),
                ],
              ),
              const SizedBox(height: 24),
              const Text('证件信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              _buildUploadItem('行驶证照片'),
              const SizedBox(height: 12),
              _buildUploadItem('车辆照片'),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _submit,
                  child: const Text('提交'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildColorChip(String label, String value, Color color) {
    final isSelected = _plateColor == value;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (s) => setState(() => _plateColor = value),
      selectedColor: color.withOpacity(0.2),
      labelStyle: TextStyle(color: isSelected ? color : AppTheme.textSecondary),
    );
  }

  Widget _buildUploadItem(String title) {
    return Container(
      height: 100,
      decoration: BoxDecoration(
        border: Border.all(color: AppTheme.borderColor),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.add_photo_alternate_outlined, size: 32, color: AppTheme.textSecondary),
            const SizedBox(height: 8),
            Text(title, style: const TextStyle(color: AppTheme.textSecondary)),
          ],
        ),
      ),
    );
  }

  void _submit() {
    if (_formKey.currentState!.validate()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('提交成功'), backgroundColor: AppTheme.successColor),
      );
      context.pop();
    }
  }
}
