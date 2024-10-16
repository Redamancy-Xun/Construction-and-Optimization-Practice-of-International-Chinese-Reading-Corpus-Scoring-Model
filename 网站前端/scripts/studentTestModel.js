// 从 localStorage 中获取 session 令牌
const token = localStorage.getItem('session');

// 检查令牌是否存在
if (token) {
    console.log('获取到的令牌:', token);
    // 在这里可以使用令牌执行其他操作，例如设置请求头
} else {
    console.log('没有找到令牌');
}

// 从LocalStorage中获取username和avatar
if (localStorage.getItem('username')) {
    document.getElementById('welcome').innerText = '你好，' + localStorage.getItem('username') + "！";
} 
if (localStorage.getItem('avatar')) {
    document.getElementById('avatar').src = localStorage.getItem('avatar');
}

// 实时改变time
function showTime() {
    var today = new Date();
    var year = today.getFullYear();
    var month = today.getMonth() + 1;
    var day = today.getDate();
    // 星期
    var week = today.getDay();
    document.getElementById('time').innerText = year + '年' + month + '月' + day + '日 ' + '星期' + '日一二三四五六'.charAt(week);
}

showTime();

function toggleCourses(unit) {
    const courses = unit.querySelector('.courses');
    const isExpanded = courses.style.display === 'block';
    
    document.querySelectorAll('.courses').forEach(c => c.style.display = 'none');

    courses.style.display = isExpanded ? 'none' : 'block';
}

// 点击button后根据button的value发送指令给服务器并将传来的数据储存到localStorage
document.querySelectorAll('.course').forEach(c => 
    {c.addEventListener('click', async event => {
        // 防止表单的默认提交行为
        event.preventDefault(); 
    
        // 获取课程编号value
        const courseID = event.target.value;
    
        try {
            // 发送 AJAX 请求（GET）到后端获取课程信息接口
            const response = await fetch(`/studentTestModel?courseId=${courseID}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    // 从 localStorage 中获取令牌并添加到请求头中
                    'session': token
                }
            });
    
            // 检查响应状态
            if (!response.ok) {
                throw new Error(`服务器响应错误: ${response.status}`);
            }
    
            // 解析响应 JSON 数据
            const data = await response.json();
    
            if (data.code === 0) {
                alert('开始学习！');
    
                // 把课程内容存入 localStorage
                localStorage.setItem('courseContent', data.result);
    
                // 例如，重定向到首页
                window.location.href = 'studentTestModelCourse.html';
            } else {
                // 注册失败，显示错误信息
                alert(`进入课程失败：${data.message}`);
            }
        } catch (error) {
            // 处理网络错误或其他异常
            console.error('网络错误:', error);
            alert('网络错误，请稍后再试。');
        }
    })
});

// 跳转到userInfo页面
const avatar = document.getElementById('avatar');
const welcome = document.getElementById('welcome');;
const time = document.getElementById('time');

avatar.addEventListener('click', () => {
    window.location.href = 'userInfo.html';
});
welcome.addEventListener('click', () => {
    window.location.href = 'userInfo.html';
});
time.addEventListener('click', () => {
    window.location.href = 'userInfo.html';
});