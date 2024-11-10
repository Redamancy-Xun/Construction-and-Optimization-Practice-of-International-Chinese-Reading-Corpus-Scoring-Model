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

const bookName = ['高级汉语听说教程上册', '发展汉语初级综合课文', '中级汉语听说教材上册', '中级汉语听说教材下册']

// 辅助函数：将阿拉伯数字转换为中文数字
function numberToChinese(num) {
    const chineseNumbers = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九'];
    const chineseTens = ['', '十', '百', '千', '万'];
    const numStr = num.toString();
    let result = '';

    for (let i = 0; i < numStr.length; i++) {
        const digit = parseInt(numStr[i]);
        const place = numStr.length - i - 1;
        if (digit !== 0) {
            result += chineseNumbers[digit] + chineseTens[place];
        } else if (place === 0 && result.length > 0 && result[result.length - 1] === '零') {
            result = result.slice(0, -1);
        }
    }

    return result;
}
 
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

            console.log(data.result.history)

            // 获取模块容器
            const container = document.getElementById('history');

            // 清空容器（避免重复添加）
            container.innerHTML = '';

            if (data.result.history.length == 0) {
                const noHistoryElement = document.createElement('div');
                noHistoryElement.className = 'history-detail';
                noHistoryElement.textContent = '暂无练习记录，加油练习哦~';
                container.appendChild(noHistoryElement);

                const nonHistoryElement = document.createElement('div');
                nonHistoryElement.className = 'advice-detail';
                nonHistoryElement.style.fontFamily = "myFont";
                nonHistoryElement.style.fontSize = "1.3em";
                nonHistoryElement.style.padding = "4%";
                nonHistoryElement.textContent = '暂无练习记录，加油练习哦~';
                document.getElementById("advice").innerHTML = '';
                document.getElementById("advice").appendChild(nonHistoryElement);
                return;
            }
            
            // 根据后端返回的数字循环创建模块
            for (let i = 0; i < data.result.history.length; i++) {
                const moduleElement = document.createElement('div');
                moduleElement.className = 'history-detail';
                moduleElement.id = `${i + 1}`;

                const historyNumber = document.createElement('div');
                historyNumber.className = 'history-detail-number';
                historyNumber.textContent = `第 ${data.result.history[i].id} 次练习`

                // 课本
                const historyBook = document.createElement('div');
                if (parseInt(data.result.history[i].bookId) !== -1) {
                    const historyBookTitle = document.createElement('span');
                    historyBookTitle.className = 'history-detail-title';
                    historyBookTitle.textContent = `课本：`;
                    historyBook.appendChild(historyBookTitle);
    
                    const historyBookContent = document.createElement('span');
                    historyBookContent.className = 'history-detail-content';
                    historyBookContent.textContent = bookName[parseInt(data.result.history[i].bookId)];
                    historyBook.appendChild(historyBookContent);
                } else {
                    const historyBookContent = document.createElement('div');
                    historyBookContent.className = 'history-detail-person-flag';
                    historyBookContent.textContent = '自定义文本';
                    historyBook.appendChild(historyBookContent);
                }


                // 课程
                const historyCourse = document.createElement('div');
                if (parseInt(data.result.history[i].bookId) !== -1) {
                    const historyCourseTitle = document.createElement('span');
                    historyCourseTitle.className = 'history-detail-title';
                    historyCourseTitle.textContent = `课程：`;
                    historyCourse.appendChild(historyCourseTitle);
    
                    const historyCourseContent = document.createElement('span');
                    historyCourseContent.className = 'history-detail-content';
                    const courseParts = data.result.history[i].textId.split('-');
                    const unitNumber = parseInt(courseParts[0]);
                    const lessonNumber = parseInt(courseParts[1]);
                    const formattedCourse = `第${numberToChinese(unitNumber)}单元 第${numberToChinese(lessonNumber)}课`;
                    historyCourseContent.textContent = formattedCourse;
                    historyCourse.appendChild(historyCourseContent);
                } else {
                    const historyCourseBlock = document.createElement('div');
                    historyCourseBlock.className = 'history-detail-person-block';
                    // const historyCourseTitle = document.createElement('div');
                    // historyCourseTitle.className = 'history-detail-person-title';
                    // historyCourseTitle.textContent = `文本`;
                    // historyCourseBlock.appendChild(historyCourseTitle);
    
                    const historyCourseContent = document.createElement('div');
                    historyCourseContent.className = 'history-detail-person-content';
                    historyCourseContent.textContent = data.result.history[i].textId;
                    historyCourseBlock.appendChild(historyCourseContent);

                    historyCourse.appendChild(historyCourseBlock);
                }


                // 语速
                const historySpeed = document.createElement('div');
                const historySpeedTitle = document.createElement('span');
                historySpeedTitle.className = 'history-detail-title';
                historySpeedTitle.textContent = `语速：`;
                historySpeed.appendChild(historySpeedTitle);

                const historySpeedContent = document.createElement('span');
                historySpeedContent.className = 'history-detail-content';
                historySpeedContent.textContent = data.result.history[i].speed + ' ★';
                historySpeed.appendChild(historySpeedContent);


                // 停顿
                const historyPause = document.createElement('div');
                const historyPauseTitle = document.createElement('span');
                historyPauseTitle.className = 'history-detail-title';
                historyPauseTitle.textContent = `停顿：`;
                historyPause.appendChild(historyPauseTitle);

                const historyPauseContent = document.createElement('span');
                historyPauseContent.className = 'history-detail-content';
                historyPauseContent.textContent = data.result.history[i].pause + ' ★';
                historyPause.appendChild(historyPauseContent);


                // 声母
                const historyInitialConsonants = document.createElement('div');
                const historyInitialConsonantsTitle = document.createElement('span');
                historyInitialConsonantsTitle.className = 'history-detail-title';
                historyInitialConsonantsTitle.textContent = `声母：`;
                historyInitialConsonants.appendChild(historyInitialConsonantsTitle);

                const historyInitialConsonantsContent = document.createElement('span');
                historyInitialConsonantsContent.className = 'history-detail-content';
                historyInitialConsonantsContent.textContent = data.result.history[i].initialConsonants + ' ★';
                historyInitialConsonants.appendChild(historyInitialConsonantsContent);


                // 韵母
                const historyFinalVowels = document.createElement('div');
                const historyFinalVowelsTitle = document.createElement('span');
                historyFinalVowelsTitle.className = 'history-detail-title';
                historyFinalVowelsTitle.textContent = `韵母：`;
                historyFinalVowels.appendChild(historyFinalVowelsTitle);

                const historyFinalVowelsContent = document.createElement('span');
                historyFinalVowelsContent.className = 'history-detail-content';
                historyFinalVowelsContent.textContent = data.result.history[i].finalVowels + ' ★';
                historyFinalVowels.appendChild(historyFinalVowelsContent);


                // 声调
                const historyTones = document.createElement('div');
                const historyTonesTitle = document.createElement('span');
                historyTonesTitle.className = 'history-detail-title';
                historyTonesTitle.textContent = `声调：`;
                historyTones.appendChild(historyTonesTitle);

                const historyTonesContent = document.createElement('span');
                historyTonesContent.className = 'history-detail-content';
                historyTonesContent.textContent = data.result.history[i].tones + ' ★';
                historyTones.appendChild(historyTonesContent);


                // 完整度
                const historyCompleteness = document.createElement('div');
                const historyCompletenessTitle = document.createElement('span');
                historyCompletenessTitle.className = 'history-detail-title';
                historyCompletenessTitle.textContent = `完整度：`;
                historyCompleteness.appendChild(historyCompletenessTitle);

                const historyCompletenessContent = document.createElement('span');
                historyCompletenessContent.className = 'history-detail-content';
                historyCompletenessContent.textContent = data.result.history[i].completeness + ' ★';
                historyCompleteness.appendChild(historyCompletenessContent);


                // 评语&更改建议
                const historyAdvice = document.createElement('div');
                const historyAdviceTitle = document.createElement('span');
                historyAdviceTitle.className = 'history-detail-title';
                historyAdviceTitle.textContent = `评语&更改建议：`;
                historyAdvice.appendChild(historyAdviceTitle);

                const historyAdviceContent = document.createElement('div');
                historyAdviceContent.className = 'history-detail-content-advice';
                historyAdviceContent.textContent = data.result.history[i].advice;
                historyAdvice.appendChild(historyAdviceContent);


                // 将历史记录添加到模块中
                moduleElement.appendChild(historyNumber);
                moduleElement.appendChild(historyBook);
                moduleElement.appendChild(historyCourse);
                moduleElement.appendChild(historySpeed);
                moduleElement.appendChild(historyPause);
                moduleElement.appendChild(historyInitialConsonants);
                moduleElement.appendChild(historyFinalVowels);
                moduleElement.appendChild(historyTones);
                moduleElement.appendChild(historyCompleteness);
                moduleElement.appendChild(historyAdvice);

                // 将模块添加到容器中
                container.appendChild(moduleElement);
            }

        } else {
            alert(`用户信息获取失败：${data.message}`);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }

    // 获取 Markdown 输入框的内容
    const markdownText = document.getElementById('advice').innerText;

    // 使用 marked.js 将 Markdown 转换为 HTML
    const htmlContent = marked.parse(markdownText);

    // 将生成的 HTML 显示在页面上
    const moduleElement = document.createElement('div');
    moduleElement.className = 'advice-detail';
    moduleElement.innerHTML = htmlContent;
    document.getElementById('advice').innerHTML = '';
    document.getElementById('advice').appendChild(moduleElement);
});