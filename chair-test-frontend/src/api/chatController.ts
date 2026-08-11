// /src/api/chatController.ts
export type MessageHandler = (data: string) => void;

class ChatController {
  private socket: WebSocket | null = null;
  private url: string;
  private reconnectTimer: number | null = null;
  private heartbeatTimer: number | null = null;
  private manuallyClosed = false;

  private onMessageHandlers: Set<MessageHandler> = new Set();
  private onOpenHandlers: Set<() => void> = new Set();
  private onCloseHandlers: Set<() => void> = new Set();

  constructor() {
    this.url = `/api/web/chat`;
  }

  // ✅新增：对外暴露实时读取连接状态
  public get isOpen(): boolean {
    return !!this.socket && this.socket.readyState === WebSocket.OPEN;
  }

  connect() {
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN ||
        this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return;
    }
    this.manuallyClosed = false;

    try {
      this.socket = new WebSocket(this.url);

      this.socket.onopen = () => {
        console.log("WebSocket 连接成功");
        this.startHeartbeat();
        this.onOpenHandlers.forEach((handler) => handler());
      };

      this.socket.onmessage = (event: MessageEvent) => {
        this.onMessageHandlers.forEach((handler) => handler(event.data));
      };

      this.socket.onerror = (error: Event) => {
        console.error("WebSocket 发生错误:", error);
      };

      this.socket.onclose = (ev) => {
        console.log("WebSocket 连接断开", ev);
        this.stopHeartbeat();
        this.onCloseHandlers.forEach((handler) => handler());

        if (!this.manuallyClosed) {
          this.scheduleReconnect();
        }
        this.socket = null;
      };
    } catch (error) {
      console.error("WebSocket 创建失败:", error);
      this.scheduleReconnect();
    }
  }

  send(message: string): boolean {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(message);
      return true;
    }
    console.warn("WebSocket 未连接，无法发送消息");
    return false;
  }

  close() {
    this.manuallyClosed = true;
    this.clearAllTimer();
    if (this.socket) {
      const s = this.socket;
      this.socket = null;
      s.onclose = null;
      s.close();
    }
  }

  onMessage(handler: MessageHandler) {
    this.onMessageHandlers.add(handler);
    return () => this.onMessageHandlers.delete(handler);
  }

  onOpen(handler: () => void) {
    this.onOpenHandlers.add(handler);
    return () => this.onOpenHandlers.delete(handler);
  }

  onClose(handler: () => void) {
    this.onCloseHandlers.add(handler);
    return () => this.onCloseHandlers.delete(handler);
  }

  private scheduleReconnect() {
    if (this.reconnectTimer || this.manuallyClosed) return;
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      this.connect();
    }, 5000);
  }

  private clearAllTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.stopHeartbeat();
  }

  private startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = window.setInterval(() => {
      if (this.socket?.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify({ type: "heartbeat" }));
      }
    }, 25000);
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }
}

export const chatController = new ChatController();