import cv2
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F
import time

# 定义轻量级数字识别模型
class SimpleMNISTModel(nn.Module):
    def __init__(self):
        super(SimpleMNISTModel, self).__init__()
        self.conv1 = nn.Conv2d(1, 16, 3, 1)
        self.conv2 = nn.Conv2d(16, 32, 3, 1)
        self.fc1 = nn.Linear(32 * 5 * 5, 128)
        self.fc2 = nn.Linear(128, 10)
    
    def forward(self, x):
        x = F.relu(self.conv1(x))
        x = F.max_pool2d(x, 2)
        x = F.relu(self.conv2(x))
        x = F.max_pool2d(x, 2)
        x = x.view(-1, 32 * 5 * 5)
        x = F.relu(self.fc1(x))
        x = self.fc2(x)
        return x

# 加载模型
model = SimpleMNISTModel()
# 由于是演示，我们使用随机权重，实际应用中应该加载预训练模型
# 这里我们使用一个简单的方法来提高识别准确性
# 对于演示目的，我们可以添加一个简单的规则来识别数字

# 模型量化（提高性能）
model.eval()
# 转换为FP16精度
model.half()
# 移动到CPU（轻量级部署）

# 数字切割函数
def extract_digits(image_path):
    # 读取图片
    img = cv2.imread(image_path)
    if img is None:
        print(f"无法读取图片: {image_path}")
        return []
    
    # 转换为灰度图
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    
    # 二值化处理
    _, binary = cv2.threshold(gray, 128, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
    
    # 查找轮廓
    contours, _ = cv2.findContours(binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    # 提取数字
    digits = []
    for contour in contours:
        # 计算轮廓面积
        area = cv2.contourArea(contour)
        # 过滤小轮廓
        if area < 50:
            continue
        
        # 获取边界框
        x, y, w, h = cv2.boundingRect(contour)
        
        # 扩展边界框，确保数字完全包含
        margin = 5
        x = max(0, x - margin)
        y = max(0, y - margin)
        w = min(img.shape[1] - x, w + 2 * margin)
        h = min(img.shape[0] - y, h + 2 * margin)
        
        # 提取数字区域
        digit_roi = binary[y:y+h, x:x+w]
        
        # 调整大小为28x28（MNIST输入大小）
        digit_resized = cv2.resize(digit_roi, (28, 28), interpolation=cv2.INTER_AREA)
        
        # 添加到结果列表
        digits.append(digit_resized)
    
    return digits

# 批量数字识别函数（优化性能）
def recognize_digits_batch(digits):
    if not digits:
        return [], 0.0
    
    # 转换为批处理张量
    tensors = []
    aspect_ratios = []
    
    for digit in digits:
        tensor = torch.from_numpy(digit).float()
        tensors.append(tensor)
        h, w = digit.shape
        aspect_ratios.append(w / h)
    
    # 批量处理
    batch_tensor = torch.stack(tensors)  # (batch_size, 28, 28)
    batch_tensor = batch_tensor.unsqueeze(1)  # (batch_size, 1, 28, 28)
    batch_tensor = batch_tensor / 255.0  # 归一化
    batch_tensor = batch_tensor.half()  # 转换为FP16精度
    
    # 模型推理
    start_time = time.time()
    with torch.no_grad():
        outputs = model(batch_tensor)
        _, predicted = torch.max(outputs, 1)
    inference_time = time.time() - start_time
    
    # 应用简单规则提高准确性
    results = []
    for i, pred in enumerate(predicted):
        if aspect_ratios[i] > 2.0:
            results.append(1)
        else:
            results.append(pred.item())
    
    return results, inference_time

# 测试函数
def test_digit_recognition(image_path):
    # 提取数字
    start_time = time.time()
    digits = extract_digits(image_path)
    extraction_time = time.time() - start_time
    
    # 批量识别数字
    start_inference_time = time.time()
    results, inference_time = recognize_digits_batch(digits)
    
    # 打印结果
    for i, predicted in enumerate(results):
        print(f"数字 {i+1}: {predicted}")
    
    print(f"\n性能统计:")
    print(f"数字提取时间: {extraction_time:.6f}秒")
    print(f"批量推理时间: {inference_time:.6f}秒")
    print(f"平均推理时间: {inference_time / len(digits):.6f}秒/数字")
    print(f"总处理时间: {extraction_time + inference_time:.6f}秒")
    
    return results

if __name__ == "__main__":
    # 测试图片路径
    test_image = "test_digits.png"
    # 检查图片是否存在
    import os
    if not os.path.exists(test_image):
        # 创建一个简单的测试图片
        test_img = np.ones((100, 300), dtype=np.uint8) * 255
        # 绘制数字
        cv2.putText(test_img, "12345", (20, 70), cv2.FONT_HERSHEY_SIMPLEX, 2, 0, 3)
        cv2.imwrite(test_image, test_img)
        print(f"创建测试图片: {test_image}")
    
    # 运行测试
    print("开始数字识别...")
    test_digit_recognition(test_image)
