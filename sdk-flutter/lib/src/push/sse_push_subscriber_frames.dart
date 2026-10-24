part of 'sse_push_subscriber.dart';

/// 帧分发与心跳实现（SsePushSubscriber 私有扩展，同库可访问私有成员）
extension _SsePushFrames on SsePushSubscriber {
  /// 分发下行事件
  ///
  /// @param event SSE 下行事件
  void _dispatchEvent(SseServerEvent event) {
    final type = FrameType.tryParse(event.event);
    if (type == null) {
      SsePushSubscriber._log.fine('未知 SSE 事件：${event.event}');
      return;
    }
    switch (type) {
      case FrameType.hello:
        _handleHello(event);
      case FrameType.signal:
        _pongSinceLastPing = true;
        _listener?.onSignal();
      case FrameType.pong:
        _pongSinceLastPing = true;
        _listener?.onFrame(FrameType.pong, _emptyFrame(FrameType.pong, event));
      case FrameType.ping:
        _pongSinceLastPing = true;
      case FrameType.data:
        _handleData(event);
      case FrameType.error:
        final payload = event.tryDataAsJson();
        final message = payload?['message']?.toString() ?? event.data;
        _listener?.onError(TbsServerException('服务端错误帧：$message'));
      case FrameType.shutdown:
        SsePushSubscriber._log.info('服务端要求关闭 SSE 会话，换票重连');
        _transport.close();
      case FrameType.subscribe || FrameType.unsubscribe:
        _listener?.onFrame(type, _emptyFrame(type, event));
    }
  }

  /// 处理握手帧
  ///
  /// 服务端经协议编解码器下发帧封装（data 为 PushFrame JSON，sessionId 位于
  /// payload 字段的二次 JSON 内，见 SseSessionWrapper）；同时兼容外层直接
  /// 携带 sessionId 的直连载荷形态。
  ///
  /// @param event SSE 下行事件
  void _handleHello(SseServerEvent event) {
    final payload = event.tryDataAsJson();
    final hello = payload == null ? null : _resolveHello(payload);
    if (hello == null) {
      SsePushSubscriber._log.warning('hello 帧无法解析 sessionId：${event.data}');
      _listener?.onError(const TbsClientException('hello 帧缺少 sessionId'));
      return;
    }
    _sessionId = hello.sessionId;
    final sessionId = hello.sessionId;
    _handshaked = true;
    _reconnectAttempts = 0;
    _heartbeatFailures = 0;
    _pongSinceLastPing = true;
    _setStatus(ConnectionStatus.connected);
    for (final topic in _topics) {
      _sendFrame(FrameUploadRequest.subscribe(
        sessionId,
        _config.pushOptions.protocolVersion,
        topic,
      ));
    }
    _startHeartbeat();
    _listener?.onHello(hello);
    _listener?.onSignal();
  }

  /// 从握手帧载荷解析 HelloInfo（外层直连 / payload 二次 JSON 双兼容）
  ///
  /// @param payload 帧载荷 Map
  /// @return 握手信息，无法解析时为 null
  HelloInfo? _resolveHello(Map<String, dynamic> payload) {
    final direct = HelloInfo.tryFromJson(payload);
    if (direct != null) {
      return direct;
    }
    final inner = payload['payload'];
    if (inner is String && inner.isNotEmpty) {
      try {
        final decoded = jsonDecode(inner);
        if (decoded is Map) {
          return HelloInfo.tryFromJson(Map<String, dynamic>.from(decoded));
        }
      } on FormatException {
        return null;
      }
    }
    if (inner is Map) {
      return HelloInfo.tryFromJson(Map<String, dynamic>.from(inner));
    }
    return null;
  }

  /// 处理数据帧
  ///
  /// @param event SSE 下行事件
  void _handleData(SseServerEvent event) {
    final payload = event.tryDataAsJson();
    final frame = payload == null ? null : PushFrame.tryFromJson(payload);
    if (frame == null) {
      SsePushSubscriber._log.fine('忽略无法解析的 data 帧：${event.data}');
      return;
    }
    _listener?.onFrame(frame.type, frame);
  }

  /// 启动心跳（连续失败达阈值判定会话已死，关流换票重连）
  void _startHeartbeat() {
    _stopHeartbeat();
    final interval = _config.pushOptions.heartbeatInterval;
    if (interval.inMilliseconds <= 0) {
      return;
    }
    _pongSinceLastPing = true;
    _heartbeatTimer = Timer.periodic(interval, (_) => _onHeartbeatTick());
  }

  /// 心跳节拍处理
  void _onHeartbeatTick() {
    if (!_pongSinceLastPing) {
      _heartbeatFailures++;
      if (_heartbeatFailures >= _config.pushOptions.heartbeatMaxFailures) {
        SsePushSubscriber._log.warning('心跳连续 $_heartbeatFailures 次无应答，判定会话已死，换票重连');
        _transport.close();
        return;
      }
    } else {
      _heartbeatFailures = 0;
    }
    _pongSinceLastPing = false;
    final sessionId = _sessionId;
    if (sessionId == null) {
      return;
    }
    _sendFrame(FrameUploadRequest.ping(
      sessionId,
      _config.pushOptions.protocolVersion,
    ));
  }

  /// 停止心跳
  void _stopHeartbeat() {
    _heartbeatTimer?.cancel();
    _heartbeatTimer = null;
  }

  /// 上行协议帧（fire-and-forget，失败仅记录日志）
  ///
  /// @param request 上行请求
  void _sendFrame(FrameUploadRequest request) {
    unawaited(
      _httpClient.post(c.pathPushSseUpload, body: request.toJson()).then((_) {
        SsePushSubscriber._log.fine('上行帧成功：${request.frame.type.wireName}');
      }).catchError((Object error) {
        SsePushSubscriber._log.warning('上行帧失败：${error.toString()}');
      }),
    );
  }

  /// 构造无载荷回执帧（subscribe/unsubscribe/pong 事件转发用）
  ///
  /// @param type  帧类型
  /// @param event SSE 下行事件
  /// @return 协议帧
  PushFrame _emptyFrame(FrameType type, SseServerEvent event) => PushFrame(
        version: _config.pushOptions.protocolVersion,
        type: type,
        timestamp: DateTime.now().millisecondsSinceEpoch,
        payload: event.data.isEmpty ? null : event.data,
      );

  /// 更新连接状态并回调监听器
  ///
  /// @param status 最新状态
  void _setStatus(ConnectionStatus status) {
    if (_status == status) {
      return;
    }
    _status = status;
    _listener?.onStatusChanged(status);
  }
}
