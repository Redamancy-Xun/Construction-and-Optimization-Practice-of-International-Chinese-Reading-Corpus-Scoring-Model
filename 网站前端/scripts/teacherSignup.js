document.addEventListener('DOMContentLoaded', () => {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('checkPassword');
    
    function validatePassword() {
        // 移除之前的错误类（如果有）
        passwordInput.classList.remove('error');                
        confirmPasswordInput.classList.remove('error');
        // 如果密码和确认密码不匹配
        if (passwordInput.value !== confirmPasswordInput.value) {
            passwordInput.classList.add('error'); // 密码输入框变红
            confirmPasswordInput.classList.add('error'); // 确认密码输入框变红
        }
    }

    // 监听输入事件以实时验证
    passwordInput.addEventListener('input', validatePassword);
    confirmPasswordInput.addEventListener('input', validatePassword);

    if (localStorage.getItem('avatar')) {
        document.getElementsByClassName('defaultAvatar').href = localStorage.getItem('avatar');
    }
});

document.getElementById('teacherSignup').addEventListener('submit', async event => {
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 获取用户输入的手机号、密码、确认密码和用户名
    const telephone = document.getElementById('telephone').value;
    const password = document.getElementById('password').value;
    const checkPassword = document.getElementById('checkPassword').value;
    const username = document.getElementById('username').value;
    const role = 1;

    // 密码匹配验证
    const passwordField = document.getElementById('password');
    const checkPasswordField = document.getElementById('checkPassword');
    if (password !== checkPassword) {
        alert('两次密码输入不一致！');
        passwordField.classList.add('error'); // 密码输入框变红
        checkPasswordField.classList.add('error'); // 确认密码输入框变红
        return; // 阻止进一步执行
    } else {
        // 密码匹配正确时，移除错误标记
        passwordField.classList.remove('error');
        checkPasswordField.classList.remove('error');
    }

    try {
        // 发送 AJAX 请求到后端注册接口
        const response = await fetch('https://chinese.redamancyxun.fun:8080/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                telephone: telephone,
                password: password,
                username : username,
                role: role
            })
        });

        // 检查响应状态
        if (!response.ok) {
            throw new Error(`服务器响应错误: ${response.status}`);
        }

        // 解析响应 JSON 数据
        const data = await response.json();

        if (data.code === 0) {
            // 注册成功，处理成功逻辑
            alert('注册成功！');

            // 从响应头中提取令牌（如果后端返回了令牌）
            const token = response.headers.get('session');
            if (token) {
                // 将令牌存储到 localStorage 或其他适当位置
                localStorage.setItem('session', token);
            }

            // 把姓名和头像存储到 localStorage
            localStorage.setItem('username', data.result.username);
            localStorage.setItem('avatar', 'https://chinese.redamancyxun.fun/images/dafaultAvatar.svg');

            // 例如，重定向到首页
            window.location.href = 'teacherSignupInformation.html';
        }else if (data.code === 2003 || data.code == 2004 || data.code === 9041) {
            // 会话过期或未授权，重定向到登录页面
            alert(`${data.message}`);
            window.location.href = 'index.html';
        } else {
            // 注册失败，显示错误信息
            alert(`注册失败：${data.message}`);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});