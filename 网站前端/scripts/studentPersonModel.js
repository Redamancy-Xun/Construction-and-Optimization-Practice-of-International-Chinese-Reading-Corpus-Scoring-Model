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

function containsChinese(text) {
    // 使用正则表达式匹配中文字符
    const regex = /[\u4e00-\u9fff]/;
    return regex.test(text);
}

// 上传输入的文本至服务器
document.getElementById('submit').addEventListener('click', async event => {
    // 防止表单的默认提交行为
    event.preventDefault(); 

    const text = document.getElementById('courseContent').value;

    if (text === null || text === '' || text === undefined || text.length === 0 || !containsChinese(text)) {
        alert('请输入有效的中文文本！');
        return;
    }

    // 把text存储到localStorage
    localStorage.setItem('courseContent', text);

    // 例如，重定向到首页
    window.location.href = 'studentPersonModelCourse.html';

    // try {
    //     // 发送 AJAX 请求到后端注册接口
    //     const response = await fetch('/studentPersonModel', {
    //         method: 'POST',
    //         headers: {
    //             'Content-Type': 'application/json',
    //             'session': token
    //         },
    //         body: JSON.stringify({
    //             text: text
    //         })
    //     });

    //     // 检查响应状态
    //     if (!response.ok) {
    //         throw new Error(`服务器响应错误: ${response.status}`);
    //     }

    //     // 解析响应 JSON 数据
    //     const data = await response.json();

    //     if (data.code === 0) {
    //         // 注册成功，处理成功逻辑
    //         alert('文本上传成功！');

    //         // 把data.result存储到localStorage
    //         localStorage.setItem('courseName', data.result);

    //         // 例如，重定向到首页
    //         window.location.href = 'studentPersonModelCourse.html';
    //     } else {
    //         // 注册失败，显示错误信息
    //         alert(`上传失败：${data.message}`);
    //     }
    // } catch (error) {
    //     // 处理网络错误或其他异常
    //     console.error('网络错误:', error);
    //     alert('网络错误，请稍后再试。');
    // }
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