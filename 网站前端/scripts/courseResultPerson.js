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

// 获取localStorage中的courseResult
const courseResult = JSON.parse(localStorage.getItem('courseResult'));

document.querySelector('.course').innerText = courseResult.courseName;

function setStarRating(ratingLow, retingHigh) {
    const stars = document.querySelectorAll('.star');
    stars.forEach((star, index) => {
        if (index >= ratingLow && index <= retingHigh) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

for (let i = 0; i < courseResult.rating.size(); i++) {
    setStarRating(courseResult.rating[i][0], courseResult.rating[i][1]);
}

document.querySelector('.adviceContent').innerText = courseResult.adviceContent;

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