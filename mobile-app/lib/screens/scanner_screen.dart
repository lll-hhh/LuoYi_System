import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

/// 扫码屏幕
class ScannerScreen extends StatefulWidget {
  const ScannerScreen({super.key});

  @override
  State<ScannerScreen> createState() => _ScannerScreenState();
}

class _ScannerScreenState extends State<ScannerScreen> {
  MobileScannerController cameraController = MobileScannerController();
  bool _isScanned = false;
  bool _isTorchOn = false;
  bool _isFrontCamera = false;

  @override
  void dispose() {
    cameraController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('扫一扫'),
        backgroundColor: Colors.transparent,
        elevation: 0,
        actions: [
          IconButton(
            icon: Icon(_isTorchOn ? Icons.flash_on : Icons.flash_off),
            onPressed: () {
              cameraController.toggleTorch();
              setState(() => _isTorchOn = !_isTorchOn);
            },
          ),
          IconButton(
            icon: Icon(_isFrontCamera ? Icons.camera_front : Icons.camera_rear),
            onPressed: () {
              cameraController.switchCamera();
              setState(() => _isFrontCamera = !_isFrontCamera);
            },
          ),
        ],
      ),
      extendBodyBehindAppBar: true,
      body: Stack(
        children: [
          MobileScanner(
            controller: cameraController,
            onDetect: _onDetect,
          ),
          _buildOverlay(),
          _buildHint(),
          _buildBottomActions(),
        ],
      ),
    );
  }

  Widget _buildOverlay() {
    return ColorFiltered(
      colorFilter: ColorFilter.mode(
        Colors.black.withOpacity(0.5),
        BlendMode.srcOut,
      ),
      child: Stack(
        children: [
          Container(
            decoration: const BoxDecoration(
              color: Colors.black,
              backgroundBlendMode: BlendMode.dstOut,
            ),
          ),
          Center(
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHint() {
    return Center(
      child: SizedBox(
        width: 250,
        height: 250,
        child: Stack(
          children: [
            // 扫描框角落装饰
            Positioned(
              top: 0,
              left: 0,
              child: _buildCorner(0),
            ),
            Positioned(
              top: 0,
              right: 0,
              child: _buildCorner(1),
            ),
            Positioned(
              bottom: 0,
              left: 0,
              child: _buildCorner(2),
            ),
            Positioned(
              bottom: 0,
              right: 0,
              child: _buildCorner(3),
            ),
            // 扫描线动画
            const Positioned.fill(
              child: _ScanLineAnimation(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCorner(int position) {
    final borderSide = BorderSide(
      color: Colors.blue,
      width: 3,
    );

    return Container(
      width: 20,
      height: 20,
      decoration: BoxDecoration(
        border: Border(
          top: position < 2 ? borderSide : BorderSide.none,
          bottom: position >= 2 ? borderSide : BorderSide.none,
          left: position % 2 == 0 ? borderSide : BorderSide.none,
          right: position % 2 == 1 ? borderSide : BorderSide.none,
        ),
      ),
    );
  }

  Widget _buildBottomActions() {
    return Positioned(
      bottom: 0,
      left: 0,
      right: 0,
      child: Container(
        padding: const EdgeInsets.all(24),
        child: SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text(
                '将二维码/条形码放入框内，即可自动扫描',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  _buildActionButton(
                    icon: Icons.photo_library,
                    label: '相册',
                    onTap: _pickFromGallery,
                  ),
                  _buildActionButton(
                    icon: Icons.history,
                    label: '历史',
                    onTap: _showHistory,
                  ),
                  _buildActionButton(
                    icon: Icons.qr_code,
                    label: '我的码',
                    onTap: _showMyQRCode,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: Colors.white, size: 24),
          ),
          const SizedBox(height: 8),
          Text(
            label,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }

  void _onDetect(BarcodeCapture capture) {
    if (_isScanned) return;
    
    final List<Barcode> barcodes = capture.barcodes;
    if (barcodes.isEmpty) return;

    final barcode = barcodes.first;
    if (barcode.rawValue == null) return;

    setState(() => _isScanned = true);
    
    // 处理扫码结果
    _handleScanResult(barcode.rawValue!);
  }

  void _handleScanResult(String code) {
    // 解析扫码结果
    if (code.startsWith('TASK:')) {
      // 任务二维码
      final taskId = code.substring(5);
      Navigator.pop(context);
      Navigator.pushNamed(context, '/task-detail', arguments: taskId);
    } else if (code.startsWith('VEHICLE:')) {
      // 车辆二维码
      final vehicleId = code.substring(8);
      _showVehicleInfo(vehicleId);
    } else if (code.startsWith('CARGO:')) {
      // 货物二维码
      final cargoId = code.substring(6);
      _showCargoInfo(cargoId);
    } else {
      // 未知格式
      _showGenericResult(code);
    }
  }

  void _showVehicleInfo(String vehicleId) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              Icons.local_shipping,
              size: 60,
              color: Colors.blue,
            ),
            const SizedBox(height: 16),
            const Text(
              '车辆信息',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '车辆ID: $vehicleId',
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      _resetScanner();
                    },
                    child: const Text('继续扫描'),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      Navigator.pop(context);
                      // 导航到车辆详情
                    },
                    child: const Text('查看详情'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    ).then((_) => _resetScanner());
  }

  void _showCargoInfo(String cargoId) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(
              Icons.inventory_2,
              size: 60,
              color: Colors.orange,
            ),
            const SizedBox(height: 16),
            const Text(
              '货物信息',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '货物ID: $cargoId',
              style: TextStyle(color: Colors.grey[600]),
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      _resetScanner();
                    },
                    child: const Text('继续扫描'),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () {
                      Navigator.pop(context);
                      Navigator.pop(context);
                      // 导航到货物详情
                    },
                    child: const Text('查看详情'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    ).then((_) => _resetScanner());
  }

  void _showGenericResult(String code) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('扫描结果'),
        content: SelectableText(code),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              _resetScanner();
            },
            child: const Text('继续扫描'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              Navigator.pop(context);
            },
            child: const Text('确定'),
          ),
        ],
      ),
    ).then((_) => _resetScanner());
  }

  void _resetScanner() {
    setState(() => _isScanned = false);
  }

  Future<void> _pickFromGallery() async {
    // 从相册选择图片识别二维码
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('从相册选择功能开发中')),
    );
  }

  void _showHistory() {
    Navigator.pushNamed(context, '/scan-history');
  }

  void _showMyQRCode() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('我的二维码'),
        content: SizedBox(
          width: 200,
          height: 200,
          child: Center(
            child: Container(
              width: 180,
              height: 180,
              color: Colors.grey[200],
              child: const Center(
                child: Text(
                  '用户二维码',
                  style: TextStyle(color: Colors.grey),
                ),
              ),
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('关闭'),
          ),
        ],
      ),
    );
  }
}

/// 扫描线动画
class _ScanLineAnimation extends StatefulWidget {
  const _ScanLineAnimation();

  @override
  State<_ScanLineAnimation> createState() => _ScanLineAnimationState();
}

class _ScanLineAnimationState extends State<_ScanLineAnimation>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 2),
      vsync: this,
    )..repeat(reverse: true);
    _animation = Tween<double>(begin: 0, end: 1).animate(_controller);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Positioned(
          top: _animation.value * 230,
          left: 10,
          right: 10,
          child: Container(
            height: 2,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  Colors.transparent,
                  Colors.blue.withOpacity(0.8),
                  Colors.transparent,
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
