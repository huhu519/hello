import socket
import threading
from PyQt6.QtCore import pyqtSignal, QObject

class ChatClient(QObject):
    message_signal = pyqtSignal(str)
    status_signal = pyqtSignal(str)
    connected_signal = pyqtSignal(bool)

    def __init__(self):
        super().__init__()
        self.client_socket = None
        self.connected = False
        self.username = ""

    def connect_to_server(self, server_ip, server_port, username):
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect((server_ip, server_port))
            self.username = username
            self.connected = True
            
            self.client_socket.sendall(f"USERNAME:{username}".encode('utf-8'))
            
            recv_thread = threading.Thread(target=self._receive_messages, daemon=True)
            recv_thread.start()
            
            self.status_signal.emit("已连接")
            self.connected_signal.emit(True)
            return True
        except Exception as e:
            self.status_signal.emit(f"连接失败: {str(e)}")
            return False

    def disconnect_from_server(self):
        self.connected = False
        if self.client_socket:
            try:
                self.client_socket.close()
            except:
                pass
        self.status_signal.emit("未连接")
        self.connected_signal.emit(False)
        self.client_socket = None

    def _receive_messages(self):
        while self.connected:
            try:
                data = self.client_socket.recv(1024).decode('utf-8')
                if not data:
                    break
                self.message_signal.emit(data)
            except:
                break
        if self.connected:
            self.disconnect_from_server()

    def send_message(self, message):
        if message and self.connected:
            try:
                self.client_socket.sendall(message.encode('utf-8'))
                return True
            except Exception as e:
                self.status_signal.emit(f"发送失败: {str(e)}")
                return False
        return False
