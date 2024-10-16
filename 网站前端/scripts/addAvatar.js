// 从 localStorage 中获取 session 令牌
const token = localStorage.getItem('session');

// 检查令牌是否存在
if (token) {
    console.log('获取到的令牌:', token);
    // 在这里可以使用令牌执行其他操作，例如设置请求头
} else {
    console.log('没有找到令牌');
}

document.addEventListener('DOMContentLoaded', function () {
    const uploadInput = document.getElementById('uploadAvatar');
    const uploadShow = document.getElementById('uploadShow');
    const previewContainer = document.getElementById('preview-container');
    const previewImage = document.getElementById('avatarShow');
    const cropButton = document.getElementById('crop-btn');
    const reloadButton = document.querySelector('.reload');
    const uploadButton = document.querySelector('.upload');

    let cropper;

    // 指定一个文件上传元素并添加一个事件监听器，监听 "change" 事件
    uploadInput.addEventListener('change', function (event) {
        // 从事件对象中获取文件列表，并获取第一个文件
        const file = event.target.files[0];
        
        // 如果文件存在
        if (file) {
            // 创建一个新的 FileReader 对象，用于读取文件
            const reader = new FileReader();
            
            // 为 FileReader 的 "load" 事件添加一个监听器
            reader.onload = function (e) {
                // 将 FileReader 读取到的数据设置为预览图像的 src 属性
                // e.target.result 是 FileReader 读取到的数据，这里是一个 data URL
                previewImage.src = e.target.result;
                
                // 显示预览容器
                previewContainer.style.display = 'block';
                cropButton.style.display = 'block';
                uploadButton.style.display = 'none';
                reloadButton.style.display = 'none';
                uploadShow.style.display = 'none';

                // 设置预览图像的属性
                previewImage.style.width = '13em';
                previewImage.style.height = '13em';

                // 初始化或销毁 Cropper.js 实例，Cropper.js 是一个图像裁剪库
                if (cropper) {
                    // 如果已经存在 cropper 实例，则销毁它
                    cropper.destroy();
                }
                
                // 初始化一个新的 Cropper.js 实例，并设置一些选项
                cropper = new Cropper(previewImage, {
                    // 设置裁剪框和原图的宽高比为 1:1
                    aspectRatio: 1,
                    // 设置为 1 表示视图模式会自动适应原图的大小
                    viewMode: 1,
                    // 自动裁剪区域设置为 1，表示裁剪框的宽高比例在拖动时保持不变
                    autoCropArea: 1,
                    // 设置为 true 表示裁剪区域的宽高会自动适应输入元素的宽高
                    responsive: true
                });
            };
            
            // 使用 FileReader.readAsDataURL 方法读取文件，并返回一个 data URL
            // 这个方法用于异步读取指定的 File 或 Blob 对象，并将内容转换为 data URL
            reader.readAsDataURL(file);
        }
    });

    // 为 cropButton 按钮添加点击事件监听器
    cropButton.addEventListener('click', function () {
        // 检查 cropper 实例是否存在
        if (cropper) {
            // 获取裁剪后的画布
            const croppedCanvas = cropper.getCroppedCanvas();
            
            // 将裁剪后的画布转为 PNG 格式的 Data URL，并更新预览图像的 src 属性
            previewImage.src = croppedCanvas.toDataURL('image/png');
            
            // 隐藏裁剪按钮
            cropButton.style.display = 'none';
            uploadButton.style.display = 'block';
            reloadButton.style.display = 'block';

            // 设置预览图像的属性
            previewImage.style.width = '7em';
            previewImage.style.height = '7em';
            
            // 销毁 cropper 实例
            cropper.destroy();
            
            // 设定 cropper 变量为 null
            cropper = null;
        }
    });

    // 为 reloadButton 按钮添加点击事件监听器
    reloadButton.addEventListener('click', function () {
        // 清空文件上传输入的值，以重置文件选择
        uploadInput.value = '';
        
        // 隐藏预览容器
        previewContainer.style.display = 'none';
        uploadShow.style.display = 'block';
        
        // 检查 cropper 实例是否存在
        if (cropper) {
            // 销毁 cropper 实例
            cropper.destroy();
            
            // 设定 cropper 变量为 null
            cropper = null;
        }
    });

    // 为 uploadButton 按钮添加点击事件监听器
    uploadButton.addEventListener('click', async function () {
        // 检查预览图像的 src 属性是否存在
        if (previewImage.src) {
            try {
                // 创建一个 FormData 对象用于上传图片
                const formData = new FormData();
                
                // 将预览图像的 Data URL 转为 Blob 对象
                const response = await fetch(previewImage.src);
                const blob = await response.blob();
                formData.append('file', blob, 'image.png');

                console.log(response);
                const formDataObject = {};
                formData.forEach((value, key) => {
                formDataObject[key] = value;
                });
                console.log(formDataObject);
                console.log(blob);
                console.log(previewImage.src);
                
                // 发送 AJAX 请求到后端图片上传接口
                const uploadResponse = await fetch('http://139.196.124.95:8080/user/uploadPortrait', {
                    method: 'POST',
                    headers: {
                        'session': token // 发送会话令牌
                    },
                    body: formData
                });
            
                // 检查响应状态
                if (!uploadResponse.ok) {
                    throw new Error(`服务器响应错误: ${uploadResponse.status}`);
                }
            
                // 解析响应 JSON 数据
                const data = await uploadResponse.json();
            
                if (data.code === 0) {
                    alert('图片上传成功！');

                    // 处理上传成功的逻辑，例如重定向或显示成功消息
                    // 存储图片 URL 到 localStorage
                    localStorage.setItem('avatar', data.result);

                    window.location.href = 'addAvatar.html'; // 可选的重定向
                } else {
                    // 上传失败，显示错误信息
                    alert(`上传失败：${data.message}`);
                    console.log(data);
                }
            } catch (error) {
                // 处理网络错误或其他异常
                console.error('网络错误:', error);
                alert('网络错误，请稍后再试。');
            }
        } else {
            alert('没有可上传的图片。');
        }
    });
});