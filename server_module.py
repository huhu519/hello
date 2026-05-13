import socket
import threading
from PyQt6.QtCore import pyqtSignal, QObject

class ChatServer(QObject):
    log_signal = pyqtSignal(str)
    client_list_signal = pyqtSignal(list)

    def __init__(self):
        super().__init__()
        self.server_socket = None
        self.client_list = []
        self.client_info = {}
        self.running = False

    def start(self, port):
        try:
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('0.0.0.0', port))
            self.server_socket.listen(5)
            self.running = True
            self.log_signal.emit(f"服务器已启动，监听端口 {port}")
            
            thread = threading.Thread(target=self._accept_clients, daemon=True)
            thread.start()
            return True
        except Exception as e:
            self.log_signal.emit(f"启动失败: {str(e)}")
            return False

    def stop(self):
        self.running = False
        for conn in self.client_list:
            try:
                conn.close()
            except:
                pass
        if self.server_socket:
            try:
                self.server_socket.close()
            except:
                pass
        self.client_list.clear()
        self.client_info.clear()
        self.log_signal.emit("服务器已停止")
        self.client_list_signal.emit([])

    def _accept_clients(self):
        while self.running:
            try:
                self.server_socket.settimeout(1.0)
                try:
                    conn, addr = self.server_socket.accept()
                    client_thread = threading.Thread(target=self._handle_client, args=(conn, addr), daemon=True)
                    client_thread.start()
                except socket.timeout:
                    continue
            except:
                break

    def _handle_client(self, conn, addr):
        username = None
        try:
            data = conn.recv(1024).decode('utf-8')
            if data.startswith("USERNAME:"):
                username = data[9:]
                self.client_list.append(conn)
                self.client_info[conn] = (username, addr)
                self.log_signal.emit(f"{username} ({addr}) 加入了聊天室")
                self._update_online_list()
                self._broadcast(f"【系统】{username} 加入了聊天室", exclude_conn=conn)
                
                while self.running:
                    try:
                        data = conn.recv(1024).decode('utf-8')
                        if not data:
                            break
                        self.log_signal.emit(f"{username}: {data}")
                        self._broadcast(f"{username}: {data}")
                    except:
                        break
        except:
            pass
        finally:
            if conn in self.client_list:
                self.client_list.remove(conn)
                del self.client_info[conn]
            try:
                conn.close()
            except:
                pass
            if username:
                self.log_signal.emit(f"{username} 离开了聊天室")
                self._broadcast(f"【系统】{username} 离开了聊天室")
                self._update_online_list()

    def _broadcast(self, message, exclude_conn=None):
        for conn in self.client_list:
            if conn != exclude_conn:
                try:
                    conn.sendall(message.encode('utf-8'))
                except:
                    pass

    def _update_online_list(self):
        users = [info[0] for info in self.client_info.values()]
        self.client_list_signal.emit(users)
