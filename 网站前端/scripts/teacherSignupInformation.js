// 从 localStorage 中获取 session 令牌
const token = localStorage.getItem('session');

// 检查令牌是否存在
if (token) {
    console.log('获取到的令牌:', token);
    // 在这里可以使用令牌执行其他操作，例如设置请求头
} else {
    console.log('没有找到令牌');
}

document.getElementById('uploadStudentInformation').addEventListener('submit', async event => {
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 获取用户输入的手机号、姓名、班级
    const name = document.getElementById('name').value;
    const telephone = document.getElementById('telephont').value;
    const classname = document.getElementById('class').value;

    try {
        // 发送 AJAX 请求到后端注册接口
        const response = await fetch('/teacherSignupInformation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 从 localStorage 中获取令牌并添加到请求头中
                'session': token
            },
            body: JSON.stringify({
                name,
                telephone,
                classname,
            })
        });

        // 检查响应状态
        if (!response.ok) {
            throw new Error(`服务器响应错误: ${response.status}`);
        }

        // 解析响应 JSON 数据
        const data = await response.json();

        if (data.code === 0) {
            alert('信息录入成功！');

            // 例如，重定向到首页
            window.location.href = 'teacherIndex.html';
        }else if (data.code === 2003 || data.code == 2004 || data.code === 9041) {
            // 会话过期或未授权，重定向到登录页面
            alert(`${data.message}`);
            window.location.href = 'index.html';
        } else {
            // 注册失败，显示错误信息
            alert(`信息录入失败：${data.message}`);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});