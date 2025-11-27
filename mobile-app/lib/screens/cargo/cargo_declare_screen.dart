import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:luoyi_mobile/config/theme.dart';

class CargoDeclareScreen extends StatefulWidget {
  const CargoDeclareScreen({super.key});

  @override
  State<CargoDeclareScreen> createState() => _CargoDeclareScreenState();
}

class _CargoDeclareScreenState extends State<CargoDeclareScreen> {
  final _formKey = GlobalKey<FormState>();
  String _cargoType = 'NORMAL';
  DateTime _departureTime = DateTime.now();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('货物申报')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('货物信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(labelText: '货物名称'),
                validator: (v) => v?.isEmpty == true ? '请输入货物名称' : null,
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                value: _cargoType,
                decoration: const InputDecoration(labelText: '货物类型'),
                items: const [
                  DropdownMenuItem(value: 'NORMAL', child: Text('普通货物')),
                  DropdownMenuItem(value: 'COLD_CHAIN', child: Text('冷链货物')),
                  DropdownMenuItem(value: 'HAZARDOUS', child: Text('危险品')),
                  DropdownMenuItem(value: 'OVERSIZE', child: Text('超限货物')),
                ],
                onChanged: (v) => setState(() => _cargoType = v!),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: TextFormField(
                      decoration: const InputDecoration(labelText: '重量(吨)'),
                      keyboardType: TextInputType.number,
                      validator: (v) => v?.isEmpty == true ? '请输入重量' : null,
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: TextFormField(
                      decoration: const InputDecoration(labelText: '数量'),
                      keyboardType: TextInputType.number,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              const Text('运输信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              DropdownButtonFormField<int>(
                decoration: const InputDecoration(labelText: '选择车辆'),
                items: const [
                  DropdownMenuItem(value: 1, child: Text('京A12345 - 重型货车')),
                  DropdownMenuItem(value: 2, child: Text('京B88888 - 中型货车')),
                ],
                onChanged: (v) {},
                validator: (v) => v == null ? '请选择车辆' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(
                  labelText: '起点',
                  prefixIcon: Icon(Icons.my_location, color: AppTheme.primaryColor),
                ),
                validator: (v) => v?.isEmpty == true ? '请输入起点' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(
                  labelText: '终点',
                  prefixIcon: Icon(Icons.location_on, color: AppTheme.errorColor),
                ),
                validator: (v) => v?.isEmpty == true ? '请输入终点' : null,
              ),
              const SizedBox(height: 16),
              InkWell(
                onTap: () async {
                  final date = await showDatePicker(
                    context: context,
                    initialDate: _departureTime,
                    firstDate: DateTime.now(),
                    lastDate: DateTime.now().add(const Duration(days: 30)),
                  );
                  if (date != null) {
                    setState(() => _departureTime = date);
                  }
                },
                child: InputDecorator(
                  decoration: const InputDecoration(labelText: '预计发车时间'),
                  child: Text('${_departureTime.year}-${_departureTime.month.toString().padLeft(2, '0')}-${_departureTime.day.toString().padLeft(2, '0')}'),
                ),
              ),
              const SizedBox(height: 24),
              const Text('联系信息', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '收货人'))),
                  const SizedBox(width: 16),
                  Expanded(child: TextFormField(decoration: const InputDecoration(labelText: '联系电话'), keyboardType: TextInputType.phone)),
                ],
              ),
              const SizedBox(height: 16),
              TextFormField(
                decoration: const InputDecoration(labelText: '备注'),
                maxLines: 3,
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _submit,
                  child: const Text('提交申报'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _submit() {
    if (_formKey.currentState!.validate()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('申报提交成功'), backgroundColor: AppTheme.successColor),
      );
      context.pop();
    }
  }
}
