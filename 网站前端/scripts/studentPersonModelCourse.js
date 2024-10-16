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

// 从 localStorage 中获取 courseName
const courseName = localStorage.getItem('courseName');

const recordingTip = document.querySelector('.recordingTip');
recordingTip.addEventListener('click', () => {
    document.querySelector('.recordingTip').style.display = 'none';
    document.querySelector('.uploadAudio').style.display = 'none';
    document.querySelector('.audioTip').style.display = 'block';
});

let mediaRecorder;
let audioChunks = [];
let recordingTime = 0;
let recordingInterval;

// 获取页面中的按钮和进度条元素
const startButton = document.getElementById("start-recording");
const stopButton = document.getElementById("stop-recording");
const uploadButton = document.getElementById("upload");
const cancelButton = document.getElementById("cancel");
const audioPlayback = document.getElementById("audio-playback");
const recordStatus = document.getElementById("record-status");
const progressBar = document.getElementById("progress-bar");
const progressTime = document.getElementById("progress-time");

// 处理开始录音按钮点击事件
startButton.addEventListener("click", async () => {
    // 清空 audioChunks 数组以便进行新的录音
    audioChunks = [];

    // 请求用户权限并获取音频流
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    
    // 创建一个新的 MediaRecorder 实例来录制音频
    mediaRecorder = new MediaRecorder(stream);

    // 当有数据可用时，将数据片段推送到 audioChunks 数组
    mediaRecorder.ondataavailable = event => {
        audioChunks.push(event.data);
    };

    progressBar.style.width = '0%';
    recordingTime = 0; // 初始化录音时间
    progressTime.textContent = `0s/60s`; // 初始化进度条时间

    // 当录音停止时，处理录音结果
    mediaRecorder.onstop = () => {
        // 将音频片段合并成一个 Blob 对象
        const audioBlob = new Blob(audioChunks, { type: "audio/wav" });
        // 创建一个 URL 以便在音频播放元素中播放音频
        const audioUrl = URL.createObjectURL(audioBlob);
        audioPlayback.src = audioUrl;
        audioPlayback.style.display = 'block';
        // 启用上传按钮
        uploadButton.disabled = false;
        // 更新录音状态显示
        recordStatus.textContent = "状态: 录音已结束";
        
        // 停止更新进度条
        clearInterval(recordingInterval);
    };

    // 开始录音
    mediaRecorder.start();
    // 更新录音状态显示
    recordStatus.textContent = "状态: 录音中...";
    // 禁用开始按钮，启用停止和取消按钮
    startButton.disabled = true;
    stopButton.disabled = false;
    audioPlayback.style.display = 'none';

    // 初始化录音时间和进度条
    recordingTime = 0;
    recordingInterval = setInterval(() => {
        recordingTime += 1;
        const percentage = (recordingTime / 60) * 100; // 假设最大录音时间为60秒
        progressBar.style.width = percentage + '%';
        progressTime.textContent = `${recordingTime}s/60s`;
    }, 1000);
});

// 处理停止录音按钮点击事件
stopButton.addEventListener("click", () => {
    // 停止录音
    mediaRecorder.stop();
    // 启用开始按钮，禁用停止和上传按钮
    startButton.disabled = false;
    stopButton.disabled = true;
    uploadButton.disabled = false;
    // 更新录音状态显示
    recordStatus.textContent = "状态: 录音已结束";
});

// 处理取消录音按钮点击事件
cancelButton.addEventListener("click", () => {

    document.querySelector('.recordingTip').style.display = 'inline';
    document.querySelector('.uploadAudio').style.display = 'inline';
    document.querySelector('.audioTip').style.display = 'none';

    // 清空音频片段数组
    audioChunks = [];
    // 停止录音
    mediaRecorder.stop();
    // 启用开始按钮，禁用停止、上传和取消按钮
    startButton.disabled = false;
    stopButton.disabled = true;
    uploadButton.disabled = true;
    // 更新录音状态显示
    recordStatus.textContent = "状态: 尚未录音";
    // 停止更新进度条
    clearInterval(recordingInterval);
    progressBar.style.width = '0%';
    progressTime.textContent = '0s/60s';
});

// 处理上传按钮点击事件
uploadButton.addEventListener("click", async () => {
    if (audioChunks.length === 0) {
        alert('没有可上传的音频。');
        return;
    }

    try {
        // 创建一个 FormData 对象用于上传音频
        const formData = new FormData();
        
        // 将录音的 Blob 对象添加到 FormData 中
        const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
        formData.append('audio', audioBlob, 'recording.wav');
        
        // 发送 AJAX 请求到后端音频上传接口
        const uploadResponse = await fetch('/uploadAudio', {
            method: 'POST',
            headers: {
                'session': token, // 发送会话令牌
                'courseName': courseName
            },
            body: formData
        });
    
        // 检查响应状态
        if (!uploadResponse.ok) {
            throw new Error(`服务器响应错误: ${uploadResponse.status}`);
        }
    
        // 解析响应 JSON 数据
        const data = await uploadResponse.json();
    
        if (data.code === 0) {
            alert('音频上传成功！');

            // 处理上传成功的逻辑，例如重定向或显示成功消息
            // 上传成功，将结果保存到 localStorage 中
            localStorage.setItem('result', data.result);

            window.location.href = 'courseReslutPerson.html'; // 可选的重定向
        } else {
            // 上传失败，显示错误信息
            alert(`上传失败：${data.message}`);
        }
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
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