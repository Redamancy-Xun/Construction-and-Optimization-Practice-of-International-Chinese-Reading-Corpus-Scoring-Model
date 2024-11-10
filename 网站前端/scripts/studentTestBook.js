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

const book0 = document.querySelector('.book-list1');
book0.addEventListener('click', async event => {
    console.log('click')
            
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 储存书籍编号到 localStorage
    localStorage.setItem('bookNumber', event.target.id);

    // 例如，重定向到首页
    window.location.href = 'studentTestModel.html';
})

const book1 = document.querySelector('.book-list2');
book1.addEventListener('click', async event => {
    console.log('click')
            
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 储存书籍编号到 localStorage
    localStorage.setItem('bookNumber', event.target.id);

    // 例如，重定向到首页
    window.location.href = 'studentTestModel.html';
})

const book2 = document.querySelector('.book-list3');
book2.addEventListener('click', async event => {
    console.log('click')
            
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 储存书籍编号到 localStorage
    localStorage.setItem('bookNumber', event.target.id);

    // 例如，重定向到首页
    window.location.href = 'studentTestModel.html';
})

const book3 = document.querySelector('.book-list4');
book3.addEventListener('click', async event => {
    console.log('click')
            
    // 防止表单的默认提交行为
    event.preventDefault(); 

    // 储存书籍编号到 localStorage
    localStorage.setItem('bookNumber', event.target.id);

    // 例如，重定向到首页
    window.location.href = 'studentTestModel.html';
})

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