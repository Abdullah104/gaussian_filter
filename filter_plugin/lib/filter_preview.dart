import 'package:filter_plugin/filter_controller.dart';
import 'package:flutter/material.dart';

class FilterPreview extends StatelessWidget {
  const FilterPreview(this._controller, {super.key});

  final FilterController _controller;

  @override
  Widget build(BuildContext context) {
    if (!_controller.initialized) return Container();

    print('texture ${_controller.textureId}');

    return AspectRatio(
      aspectRatio: _controller.width / _controller.height,
      child: Texture(
        textureId: _controller.textureId,
      ),
    );
  }
}
