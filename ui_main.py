from PyQt6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                             QHBoxLayout, QPushButton, QLabel, QTextEdit, 
                             QLineEdit, QListWidget, QMessageBox, QGroupBox,
                             QTabWidget, QStackedWidget)
from PyQt6.QtCore import Qt
from PyQt6.QtGui import QFont
from server_module import ChatServer
from client_module import ChatClient

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("局域网即时通讯")
        self.setGeometry(100, 100, 1000, 700)
        
        self.chat_server = ChatServer()
        self.chat_client = ChatClient()
        
        self.chat_server.log_signal.connect(self.add_server_log)
        self.chat_server.client_list_signal.connect(self.update_server_client_list)
        
        self.chat_client.message_signal.connect(self.add_client_message)
        self.chat_client.status_signal.connect(self.update_client_status)
        self.chat_client.connected_signal.connect(self.on_client_connected)
        
        self.init_ui()

    def init_ui(self):
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QVBoxLayout(central_widget)
        
        title_label = QLabel("局域网即时通讯系统")
        title_label.setFont(QFont("Arial", 20, QFont.Weight.Bold))
        title_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        main_layout.addWidget(title_label)
        
        self.tab_widget = QTabWidget()
        
        self.server_tab = self.create_server_tab()
        self.client_tab = self.create_client_tab()
        
        self.tab_widget.addTab(self.server_tab, "服务器模式")
        self.tab_widget.addTab(self.client_tab, "客户端模式")
        
        main_layout.addWidget(self.tab_widget)

    def create_server_tab(self):
        tab = QWidget()
        layout = QHBoxLayout(tab)
        
        left_panel = QWidget()
        left_layout = QVBoxLayout(left_panel)
        right_panel = QWidget()
        right_layout = QVBoxLayout(right_panel)
        
        layout.addWidget(left_panel, 2)
        layout.addWidget(right_panel, 1)
        
        config_group = QGroupBox("服务器设置")
        config_layout = QHBoxLayout()
        
        config_layout.addWidget(QLabel("端口:"))
        self.server_port_input = QLineEdit("8888")
        config_layout.addWidget(self.server_port_input)
        
        self.start_server_btn = QPushButton("启动服务器")
        self.start_server_btn.clicked.connect(self.start_server)
        config_layout.addWidget(self.start_server_btn)
        
        self.stop_server_btn = QPushButton("停止服务器")
        self.stop_server_btn.clicked.connect(self.stop_server)
        self.stop_server_btn.setEnabled(False)
        config_layout.addWidget(self.stop_server_btn)
        
        config_group.setLayout(config_layout)
        left_layout.addWidget(config_group)
        
        left_layout.addWidget(QLabel("运行日志:"))
        self.server_log_text = QTextEdit()
        self.server_log_text.setReadOnly(True)
        left_layout.addWidget(self.server_log_text)
        
        right_layout.addWidget(QLabel("在线用户:"))
        self.server_client_list = QListWidget()
        right_layout.addWidget(self.server_client_list)
        
        self.server_status_label = QLabel("状态: 未启动")
        right_layout.addWidget(self.server_status_label)
        
        return tab

    def create_client_tab(self):
        tab = QWidget()
        layout = QHBoxLayout(tab)
        
        left_panel = QWidget()
        left_layout = QVBoxLayout(left_panel)
        right_panel = QWidget()
        right_layout = QVBoxLayout(right_panel)
        
        layout.addWidget(left_panel, 3)
        layout.addWidget(right_panel, 1)
        
        config_group = QGroupBox("连接设置")
        config_layout = QVBoxLayout()
        
        name_layout = QHBoxLayout()
        name_layout.addWidget(QLabel("用户名:"))
        self.client_name_input = QLineEdit()
        self.client_name_input.setPlaceholderText("请输入用户名")
        name_layout.addWidget(self.client_name_input)
        config_layout.addLayout(name_layout)
        
        ip_layout = QHBoxLayout()
        ip_layout.addWidget(QLabel("服务器IP:"))
        self.client_ip_input = QLineEdit("192.168.137.1")
        ip_layout.addWidget(self.client_ip_input)
        config_layout.addLayout(ip_layout)
        
        port_layout = QHBoxLayout()
        port_layout.addWidget(QLabel("端口:"))
        self.client_port_input = QLineEdit("8888")
        port_layout.addWidget(self.client_port_input)
        config_layout.addLayout(port_layout)
        
        btn_layout = QHBoxLayout()
        self.connect_btn = QPushButton("连接")
        self.connect_btn.clicked.connect(self.connect_client)
        self.disconnect_btn = QPushButton("断开")
        self.disconnect_btn.clicked.connect(self.disconnect_client)
        self.disconnect_btn.setEnabled(False)
        btn_layout.addWidget(self.connect_btn)
        btn_layout.addWidget(self.disconnect_btn)
        config_layout.addLayout(btn_layout)
        
        config_group.setLayout(config_layout)
        left_layout.addWidget(config_group)
        
        left_layout.addWidget(QLabel("聊天记录:"))
        self.client_chat_text = QTextEdit()
        self.client_chat_text.setReadOnly(True)
        left_layout.addWidget(self.client_chat_text)
        
        input_layout = QHBoxLayout()
        self.client_message_input = QLineEdit()
        self.client_message_input.setPlaceholderText("输入消息...")
        self.client_message_input.returnPressed.connect(self.send_client_message)
        self.send_btn = QPushButton("发送")
        self.send_btn.clicked.connect(self.send_client_message)
        self.send_btn.setEnabled(False)
        input_layout.addWidget(self.client_message_input, 4)
        input_layout.addWidget(self.send_btn, 1)
        left_layout.addLayout(input_layout)
        
        right_layout.addWidget(QLabel("在线用户:"))
        self.client_online_list = QListWidget()
        right_layout.addWidget(self.client_online_list)
        
        self.client_status_label = QLabel("状态: 未连接")
        right_layout.addWidget(self.client_status_label)
        
        return tab

    def start_server(self):
        try:
            port = int(self.server_port_input.text())
            if self.chat_server.start(port):
                self.start_server_btn.setEnabled(False)
                self.stop_server_btn.setEnabled(True)
                self.server_status_label.setText(f"状态: 运行中 (端口 {port})")
        except ValueError:
            QMessageBox.warning(self, "警告", "请输入有效的端口号!")

    def stop_server(self):
        self.chat_server.stop()
        self.start_server_btn.setEnabled(True)
        self.stop_server_btn.setEnabled(False)
        self.server_status_label.setText("状态: 未启动")

    def connect_client(self):
        username = self.client_name_input.text().strip()
        if not username:
            QMessageBox.warning(self, "警告", "请输入用户名!")
            return
        
        try:
            server_ip = self.client_ip_input.text().strip()
            server_port = int(self.client_port_input.text())
            
            if self.chat_client.connect_to_server(server_ip, server_port, username):
                self.client_chat_text.clear()
        except ValueError:
            QMessageBox.warning(self, "警告", "请输入有效的端口号!")

    def disconnect_client(self):
        self.chat_client.disconnect_from_server()

    def send_client_message(self):
        message = self.client_message_input.text().strip()
        if self.chat_client.send_message(message):
            self.client_chat_text.append(f"我: {message}")
            self.client_message_input.clear()

    def add_server_log(self, message):
        self.server_log_text.append(message)

    def update_server_client_list(self, users):
        self.server_client_list.clear()
        for user in users:
            self.server_client_list.addItem(user)

    def add_client_message(self, message):
        self.client_chat_text.append(message)

    def update_client_status(self, text):
        self.client_status_label.setText(f"状态: {text}")

    def on_client_connected(self, connected):
        self.connect_btn.setEnabled(not connected)
        self.disconnect_btn.setEnabled(connected)
        self.send_btn.setEnabled(connected)
        self.client_name_input.setEnabled(not connected)
        self.client_ip_input.setEnabled(not connected)
        self.client_port_input.setEnabled(not connected)

    def closeEvent(self, event):
        self.chat_server.stop()
        self.chat_client.disconnect_from_server()
        event.accept()
