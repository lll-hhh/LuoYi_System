import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import '../providers/task_provider.dart';
import '../providers/location_provider.dart';
import '../models/anomaly_event.dart';
import '../widgets/widgets.dart';

/// 上报异常屏幕
class ReportAnomalyScreen extends StatefulWidget {
  final String taskId;

  const ReportAnomalyScreen({super.key, required this.taskId});

  @override
  State<ReportAnomalyScreen> createState() => _ReportAnomalyScreenState();
}

class _ReportAnomalyScreenState extends State<ReportAnomalyScreen> {
  final _formKey = GlobalKey<FormState>();
  final _descriptionController = TextEditingController();
  
  AnomalyType? _selectedType;
  Severity _selectedSeverity = Severity.medium;
  final List<XFile> _photos = [];
  bool _isSubmitting = false;
  bool _useCurrentLocation = true;
  double? _customLatitude;
  double? _customLongitude;

  final ImagePicker _imagePicker = ImagePicker();

  @override
  void dispose() {
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('上报异常'),
      ),
      body: LoadingOverlay(
        isLoading: _isSubmitting,
        message: '正在提交...',
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _buildTypeSelector(),
              const SizedBox(height: 24),
              _buildSeveritySelector(),
              const SizedBox(height: 24),
              _buildDescriptionField(),
              const SizedBox(height: 24),
              _buildPhotoSection(),
              const SizedBox(height: 24),
              _buildLocationSection(),
              const SizedBox(height: 32),
              _buildSubmitButton(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTypeSelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '异常类型 *',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: AnomalyType.values.map((type) {
            final isSelected = _selectedType == type;
            return ChoiceChip(
              label: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    _getTypeIcon(type),
                    size: 16,
                    color: isSelected ? Colors.white : Colors.grey[700],
                  ),
                  const SizedBox(width: 4),
                  Text(_getTypeLabel(type)),
                ],
              ),
              selected: isSelected,
              onSelected: (_) => setState(() => _selectedType = type),
              selectedColor: Colors.blue,
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : Colors.grey[700],
              ),
            );
          }).toList(),
        ),
        if (_selectedType == null)
          Padding(
            padding: const EdgeInsets.only(top: 8),
            child: Text(
              '请选择异常类型',
              style: TextStyle(
                color: Colors.red[400],
                fontSize: 12,
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildSeveritySelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '严重程度',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: Severity.values.map((severity) {
            final isSelected = _selectedSeverity == severity;
            return Expanded(
              child: GestureDetector(
                onTap: () => setState(() => _selectedSeverity = severity),
                child: Container(
                  margin: const EdgeInsets.symmetric(horizontal: 4),
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  decoration: BoxDecoration(
                    color: isSelected
                        ? _getSeverityColor(severity)
                        : Colors.grey[100],
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(
                      color: isSelected
                          ? _getSeverityColor(severity)
                          : Colors.grey[300]!,
                      width: isSelected ? 2 : 1,
                    ),
                  ),
                  child: Column(
                    children: [
                      Icon(
                        _getSeverityIcon(severity),
                        color: isSelected ? Colors.white : Colors.grey[600],
                        size: 24,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _getSeverityLabel(severity),
                        style: TextStyle(
                          color: isSelected ? Colors.white : Colors.grey[700],
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildDescriptionField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          '详细描述 *',
          style: TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: _descriptionController,
          maxLines: 5,
          maxLength: 500,
          decoration: InputDecoration(
            hintText: '请详细描述异常情况...',
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8),
            ),
            filled: true,
            fillColor: Colors.grey[50],
          ),
          validator: (value) {
            if (value == null || value.trim().isEmpty) {
              return '请输入详细描述';
            }
            if (value.trim().length < 10) {
              return '描述至少需要10个字符';
            }
            return null;
          },
        ),
      ],
    );
  }

  Widget _buildPhotoSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              '现场照片',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
            Text(
              '${_photos.length}/5',
              style: TextStyle(
                color: Colors.grey[500],
                fontSize: 14,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 100,
          child: ListView(
            scrollDirection: Axis.horizontal,
            children: [
              if (_photos.length < 5)
                _buildAddPhotoButton(),
              ..._photos.asMap().entries.map((entry) =>
                  _buildPhotoItem(entry.key, entry.value)),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildAddPhotoButton() {
    return GestureDetector(
      onTap: _showPhotoOptions,
      child: Container(
        width: 100,
        height: 100,
        margin: const EdgeInsets.only(right: 8),
        decoration: BoxDecoration(
          color: Colors.grey[100],
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: Colors.grey[300]!,
            style: BorderStyle.solid,
          ),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.add_a_photo, color: Colors.grey[500], size: 32),
            const SizedBox(height: 4),
            Text(
              '添加照片',
              style: TextStyle(
                color: Colors.grey[500],
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPhotoItem(int index, XFile photo) {
    return Container(
      width: 100,
      height: 100,
      margin: const EdgeInsets.only(right: 8),
      child: Stack(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: Image.file(
              File(photo.path),
              width: 100,
              height: 100,
              fit: BoxFit.cover,
            ),
          ),
          Positioned(
            top: 4,
            right: 4,
            child: GestureDetector(
              onTap: () => setState(() => _photos.removeAt(index)),
              child: Container(
                padding: const EdgeInsets.all(4),
                decoration: const BoxDecoration(
                  color: Colors.red,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.close,
                  color: Colors.white,
                  size: 16,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLocationSection() {
    return Consumer<LocationProvider>(
      builder: (context, locationProvider, child) {
        final currentLocation = locationProvider.currentLocation;

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '位置信息',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            SwitchListTile(
              title: const Text('使用当前位置'),
              subtitle: currentLocation != null
                  ? Text(
                      '${currentLocation.latitude.toStringAsFixed(6)}, ${currentLocation.longitude.toStringAsFixed(6)}',
                      style: TextStyle(
                        color: Colors.grey[500],
                        fontSize: 12,
                      ),
                    )
                  : Text(
                      '正在获取位置...',
                      style: TextStyle(
                        color: Colors.grey[500],
                        fontSize: 12,
                      ),
                    ),
              value: _useCurrentLocation,
              onChanged: (value) => setState(() => _useCurrentLocation = value),
            ),
            if (!_useCurrentLocation)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Column(
                  children: [
                    TextFormField(
                      decoration: const InputDecoration(
                        labelText: '纬度',
                        hintText: '例如: 39.904200',
                      ),
                      keyboardType: TextInputType.number,
                      onChanged: (value) {
                        _customLatitude = double.tryParse(value);
                      },
                    ),
                    const SizedBox(height: 8),
                    TextFormField(
                      decoration: const InputDecoration(
                        labelText: '经度',
                        hintText: '例如: 116.407526',
                      ),
                      keyboardType: TextInputType.number,
                      onChanged: (value) {
                        _customLongitude = double.tryParse(value);
                      },
                    ),
                  ],
                ),
              ),
          ],
        );
      },
    );
  }

  Widget _buildSubmitButton() {
    return LoadingButton(
      isLoading: _isSubmitting,
      onPressed: _submitReport,
      text: '提交异常报告',
      icon: Icons.send,
      height: 52,
      backgroundColor: Colors.red,
    );
  }

  void _showPhotoOptions() {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.camera_alt),
              title: const Text('拍照'),
              onTap: () {
                Navigator.pop(context);
                _takePhoto();
              },
            ),
            ListTile(
              leading: const Icon(Icons.photo_library),
              title: const Text('从相册选择'),
              onTap: () {
                Navigator.pop(context);
                _pickFromGallery();
              },
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  Future<void> _takePhoto() async {
    final photo = await _imagePicker.pickImage(
      source: ImageSource.camera,
      maxWidth: 1920,
      maxHeight: 1080,
      imageQuality: 80,
    );

    if (photo != null) {
      setState(() => _photos.add(photo));
    }
  }

  Future<void> _pickFromGallery() async {
    final photos = await _imagePicker.pickMultiImage(
      maxWidth: 1920,
      maxHeight: 1080,
      imageQuality: 80,
    );

    if (photos.isNotEmpty) {
      final remaining = 5 - _photos.length;
      setState(() {
        _photos.addAll(photos.take(remaining));
      });
    }
  }

  Future<void> _submitReport() async {
    if (_selectedType == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('请选择异常类型'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final locationProvider = context.read<LocationProvider>();
      final currentLocation = locationProvider.currentLocation;

      double latitude;
      double longitude;

      if (_useCurrentLocation && currentLocation != null) {
        latitude = currentLocation.latitude;
        longitude = currentLocation.longitude;
      } else if (!_useCurrentLocation &&
          _customLatitude != null &&
          _customLongitude != null) {
        latitude = _customLatitude!;
        longitude = _customLongitude!;
      } else {
        throw Exception('无法获取位置信息');
      }

      final event = AnomalyEvent(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        taskId: widget.taskId,
        type: _selectedType!,
        severity: _selectedSeverity,
        description: _descriptionController.text.trim(),
        latitude: latitude,
        longitude: longitude,
        photos: _photos.map((p) => p.path).toList(),
        reportedAt: DateTime.now(),
        reportedBy: 'current_user', // 实际项目中从用户状态获取
      );

      await context.read<TaskProvider>().reportAnomaly(widget.taskId, event);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('异常报告已提交'),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('提交失败: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  IconData _getTypeIcon(AnomalyType type) {
    switch (type) {
      case AnomalyType.vehicleBreakdown:
        return Icons.build;
      case AnomalyType.trafficAccident:
        return Icons.car_crash;
      case AnomalyType.roadBlock:
        return Icons.block;
      case AnomalyType.weatherDelay:
        return Icons.cloud;
      case AnomalyType.cargoDamage:
        return Icons.broken_image;
      case AnomalyType.cargoLoss:
        return Icons.search_off;
      case AnomalyType.routeDeviation:
        return Icons.alt_route;
      case AnomalyType.illegalParking:
        return Icons.local_parking;
      case AnomalyType.speeding:
        return Icons.speed;
      case AnomalyType.fatigueDriving:
        return Icons.airline_seat_flat;
      case AnomalyType.equipmentFailure:
        return Icons.settings;
      case AnomalyType.communication:
        return Icons.signal_cellular_off;
      case AnomalyType.customerComplaint:
        return Icons.feedback;
      case AnomalyType.other:
        return Icons.more_horiz;
    }
  }

  String _getTypeLabel(AnomalyType type) {
    switch (type) {
      case AnomalyType.vehicleBreakdown:
        return '车辆故障';
      case AnomalyType.trafficAccident:
        return '交通事故';
      case AnomalyType.roadBlock:
        return '道路阻塞';
      case AnomalyType.weatherDelay:
        return '天气延误';
      case AnomalyType.cargoDamage:
        return '货物损坏';
      case AnomalyType.cargoLoss:
        return '货物丢失';
      case AnomalyType.routeDeviation:
        return '路线偏离';
      case AnomalyType.illegalParking:
        return '违规停车';
      case AnomalyType.speeding:
        return '超速';
      case AnomalyType.fatigueDriving:
        return '疲劳驾驶';
      case AnomalyType.equipmentFailure:
        return '设备故障';
      case AnomalyType.communication:
        return '通讯异常';
      case AnomalyType.customerComplaint:
        return '客户投诉';
      case AnomalyType.other:
        return '其他';
    }
  }

  Color _getSeverityColor(Severity severity) {
    switch (severity) {
      case Severity.low:
        return Colors.blue;
      case Severity.medium:
        return Colors.orange;
      case Severity.high:
        return Colors.deepOrange;
      case Severity.critical:
        return Colors.red;
    }
  }

  IconData _getSeverityIcon(Severity severity) {
    switch (severity) {
      case Severity.low:
        return Icons.info_outline;
      case Severity.medium:
        return Icons.warning_amber_outlined;
      case Severity.high:
        return Icons.error_outline;
      case Severity.critical:
        return Icons.dangerous_outlined;
    }
  }

  String _getSeverityLabel(Severity severity) {
    switch (severity) {
      case Severity.low:
        return '轻微';
      case Severity.medium:
        return '一般';
      case Severity.high:
        return '严重';
      case Severity.critical:
        return '紧急';
    }
  }
}
