// 从 localStorage 中获取 session 令牌
const token = localStorage.getItem('session');

// 检查令牌是否存在
if (token) {
    console.log('获取到的令牌:', token);
    // 在这里可以使用令牌执行其他操作，例如设置请求头
} else {
    console.log('没有找到令牌');
}

const studentButton = document.getElementById('student');
const teacherButton = document.getElementById('teacher');

studentButton.addEventListener('click', async () => {
    
    try {
        // 发送 AJAX 请求到后端注册接口
        const response = await fetch('https://chinese.redamancyxun.fun:8080/user/checkLogin', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'session' : token
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

            if (data.result === -1) {
                alert('会话过期，请重新登陆！');
                window.location.href = 'studentLogin.html';
            } else if (data.result === 0) {
                window.location.href = 'studentLogin.html';
            } else if (data.result === 1) {
                // alert('登录成功！');
                window.location.href = 'studentIndex.html';
            } else if (data.result === 2) {
                alert('上次登录为教师账号，请确认是否要为学生身份登录！');
                window.location.href = 'teacherLogin.html';
            }
        } else if (data.code === 2003 || data.code == 2004 || data.code === 9041) {
            // 会话过期或未授权，重定向到登录页面
            alert(`${data.message}`);
            window.location.href = 'index.html';
        } else {
            alert(`登录状态确认失败！`);
            console.error(data.message);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});

teacherButton.addEventListener('click', async () => {

    try {
        // 发送 AJAX 请求到后端注册接口
        const response = await fetch('https://chinese.redamancyxun.fun:8080/user/checkLogin', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'session' : token
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
            if (data.result === -1) {
                alert('会话过期，请重新登陆！');
                window.location.href = 'teacherLogin.html';
            } else if (data.result === 0) {
                window.location.href = 'teacherLogin.html';
            } else if (data.result === 2) {
                // alert('登录成功！');
                window.location.href = 'teacherIndex.html';
            } else if (data.result === 1) {
                alert('上次登录为学生账号，请确认是否要为教师身份登录！');
                window.location.href = 'teacherLogin.html';
            }
        }else if (data.code === 2003 || data.code == 2004 || data.code === 9041) {
            // 会话过期或未授权，重定向到登录页面
            alert(`${data.message}`);
            window.location.href = 'index.html';
        } else {
            alert(`登录状态确认失败！`);
            console.error(data.message);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});