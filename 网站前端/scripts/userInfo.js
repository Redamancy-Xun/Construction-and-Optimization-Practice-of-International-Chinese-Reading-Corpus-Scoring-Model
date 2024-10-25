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
 
const editButton = document.getElementById('edit');
editButton.onclick = function () {
    window.location.href = 'updateUserInfo.html';
}

// 跳转到userInfo页面
const avatar = document.getElementById('avatar');
const welcome = document.getElementById('welcome');
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

// 加载用户信息接口
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('http://localhost:8080/user/getUserInfo', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'session' : token
            }
        });

        // 检查响应状态
        if (!response.ok) {
            throw new Error(`服务器响应错误: ${response.status}`);
        }

        // 解析响应 JSON 数据
        const data = await response.json();

        console.log(data);

        if (data.code === 0) {
            document.getElementsByClassName('username-dispaly')[0].innerText = data.result.username;
            document.getElementsByClassName('sex')[0].innerText = data.result.gender;
            document.getElementsByClassName('age')[0].innerText = data.result.age;
            document.getElementsByClassName('nation')[0].innerText = data.result.nation;
            document.getElementsByClassName('telephone')[0].innerText = data.result.telephone;
            document.getElementById('avatarUpdate').src = data.result.portrait;
            document.getElementsByClassName('advice')[0].innerText = data.result.advice;
            document.getElementsByClassName('history')[0].innerText = data.result.history;
        } else {
            alert(`用户信息获取失败：${data.message}`);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});