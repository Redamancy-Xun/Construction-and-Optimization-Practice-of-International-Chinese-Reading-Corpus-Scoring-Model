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
});

document.getElementById('studentSignup').addEventListener('click', async event => {
    // 防止表单的默认提交行为
    event.preventDefault(); 

    const form = document.getElementById('signupForm');
    
    if (!form.checkValidity()) {
        form.reportValidity(); // 显示表单验证错误
        return; // 阻止进一步执行
    }

    // 获取用户输入的手机号、密码和确认密码
    const telephone = document.getElementById('telephone').value;
    const password = document.getElementById('password').value;
    const checkPassword = document.getElementById('checkPassword').value;
    const role = 0;

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
        const response = await fetch('http://139.196.124.95:8080/user/signup?telephone=' + telephone + '&password=' + password + '&role=' + role, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
        });

        console.log("请求发出");

        // 检查响应状态
        if (!response.ok) {
            throw new Error(`服务器响应错误: ${response.status}`);
        }

        // 解析响应 JSON 数据
        const data = await response.json();

        if (data.code === 0) {
            // 注册成功，处理成功逻辑
            alert('注册成功！');

            console.log(data);
            console.log(response);
            console.log(response.headers);
            console.log(response.headers.get('session'));
            // 从响应头中提取令牌（如果后端返回了令牌）
            const token = data.result.sessionId;
            if (token) {
                // 将令牌存储到 localStorage 或其他适当位置
                localStorage.setItem('session', token);
                localStorage.setItem('avatar', data.result.portrait);
            }

            // 例如，重定向到首页
            window.location.href = 'studentSignupInformation.html';
        } else {
            // 注册失败，显示错误信息
            alert(`注册失败：${data.message}`);
            console.error(data.message);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});