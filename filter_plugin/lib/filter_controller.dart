// Channel to send messages to the native platform land
import 'package:flutter/services.dart';

const _channel = MethodChannel('filter_plugin');

class FilterController {
  FilterController();

  int _textureId = -1;
  int _width = 0;
  int _height = 0;
  bool _isDisposed = false;
  bool _initialized = false;

  bool get initialized => _initialized;

  int get textureId => _textureId;

  int get width => _width;

  int get height => _height;

  Future<void> initialize(ByteData bytes, int width, int height) async {
    if (_isDisposed) throw Exception('Disposed FilterController');

    _width = width;
    _height = height;

    // Initialize the filter on the native platform
    final params = {
      'img': bytes.buffer.asUint8List(0),
      'width': width,
      'height': height,
    };

    final reply =
        await _channel.invokeMapMethod<String, dynamic>('create', params);

    _initialized = true;
    _textureId = reply!['textureId'];
  }

  Future<void> dispose() async {
    if (_isDisposed) return;

    // Dispose the filter on the native platform
    _channel.invokeMethod('dispose');
    _isDisposed = true;
  }

  Future<void> draw(double radius) async {
    if (!_initialized) throw Exception('FilterController not initialized');

    // Call the filter draw method on the native platform
    final params = {'radius': radius};
    await _channel.invokeMethod('draw', params);
  }
}
