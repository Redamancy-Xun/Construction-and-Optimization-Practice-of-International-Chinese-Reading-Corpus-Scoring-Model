// 从 localStorage 中获取 session 令牌
const token = localStorage.getItem('session');

// 检查令牌是否存在
if (token) {
    console.log('获取到的令牌:', token);
    // 在这里可以使用令牌执行其他操作，例如设置请求头
} else {
    console.log('没有找到令牌');
}

if (localStorage.getItem('avatar')) {
    document.getElementsByClassName('defaultAvatar').href = localStorage.getItem('avatar');
}

// 获取 defaultAvatar 元素
const defaultAvatar = document.getElementsByClassName('defaultAvatar');
defaultAvatar.href = localStorage.getItem('avatar');

// 获取按钮元素
const maleButton = document.getElementById('maleButton');
const femaleButton = document.getElementById('femaleButton');
let gender;

// 选中状态的处理函数
function selectGender(event) {
    // 防止表单的默认提交行为
    event.preventDefault();

    // 移除其他按钮的选中状态
    maleButton.classList.remove('selected');        
    femaleButton.classList.remove('selected');

    // 设置当前按钮为选中状态
    event.target.classList.add('selected');

    // 获取选中的值
    const selectedValue = event.target.getAttribute('data-value');
    gender = selectedValue;
}
    
// 添加点击事件监听器
maleButton.addEventListener('click', selectGender);
femaleButton.addEventListener('click', selectGender);

document.getElementById('studentSignup').addEventListener('click', async event => {
    // 防止表单的默认提交行为
    event.preventDefault(); 

    const form = document.getElementById('signupForm');
    
    if (!form.checkValidity()) {
        form.reportValidity(); // 显示表单验证错误
        return; // 阻止进一步执行
    }

    // 获取用户输入的用户名、性别、年龄、国籍
    const username = document.getElementById('username').value;
    const age = document.getElementById('age').value;
    const nation = document.getElementById('nation').value;

    // 如果sex为null，即用户未选择性别，则提示用户选择
    if (!gender) {
        alert('请选择性别！');
        console.log('未选择性别！');
        return;
    }
    if (!username) {
        alert('请输入用户名！');
        console.log('未输入用户名！');
        return;
    }
    if (!age) {
        alert('请输入年龄！');
        console.log('未输入年龄！');
        return;
    }
    if (!nation) {
        alert('请输入国籍！');
        console.log('未输入国籍！');
        return;
    }

    try {
        // 发送 AJAX 请求到后端注册接口
        const response = await fetch('http://localhost:8080/user/updateUserInfo', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 从 localStorage 中获取令牌并添加到请求头中
                'session': token
            },
            body: JSON.stringify({
                username: username,
                gender: gender,
                age: age,
                nation: nation,
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

            // 把姓名存入 localStorage
            localStorage.setItem('username', username);

            // 例如，重定向到首页
            window.location.href = 'studentIndex.html';
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