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
if (localStorage.getItem('courseContent')) {
    document.querySelector('.courseName').textContent = localStorage.getItem('courseContent');
}

let speed;
let pause;
let initialConsonants;
let finalVowels;
let tones;
let completeness;
let advice;

// 从LocalStorage中获取courseResult
if (localStorage.getItem('speed')) {
    speed = parseInt(localStorage.getItem('speed'));
}
if (localStorage.getItem('pause')) {
    pause = parseInt(localStorage.getItem('pause'));
}
if (localStorage.getItem('initialConsonants')) {
    initialConsonants = parseInt(localStorage.getItem('initialConsonants'));
}
if (localStorage.getItem('finalVowels')) {
    finalVowels = parseInt(localStorage.getItem('finalVowels'));
}
if (localStorage.getItem('tones')) {
    tones = parseInt(localStorage.getItem('tones'));
}
if (localStorage.getItem('completeness')) {
    completeness = parseInt(localStorage.getItem('completeness'));
}
if (localStorage.getItem('advice')) {
    advice = localStorage.getItem('advice');
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

function setStarRating(ratingLow, retingHigh) {
    const stars = document.querySelectorAll('.star');
    stars.forEach((star, index) => {
        if (index >= ratingLow && index <= retingHigh) {
            star.classList.add('active');
        }
    });
}

setStarRating(0, speed);
setStarRating(5, 5 + pause);
setStarRating(10, 10 + initialConsonants);
setStarRating(15, 15 + finalVowels);
setStarRating(20, 20 + tones);
setStarRating(25, 25 + completeness);

// 显示advice
document.querySelector('.adviceContent').innerText = advice;

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