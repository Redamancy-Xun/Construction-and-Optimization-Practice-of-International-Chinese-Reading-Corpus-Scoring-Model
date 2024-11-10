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
if (localStorage.getItem('courseName')) {
    document.querySelector('.courseTitle').textContent = localStorage.getItem('courseName');
}
if (localStorage.getItem('courseContent')) {
    document.querySelector('.courseContent').textContent = localStorage.getItem('courseContent');
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

        // 按钮样式与功能（写死）
        startButton.style.background = "rgba(255, 255, 255, 0.824)";
        startButton.disabled = false;
        stopButton.style.background = "rgba(249, 137, 136, 1)";
        stopButton.disabled = true;
        uploadButton.style.background = "rgba(255, 255, 255, 0.824)";
        uploadButton.disabled = false;
        cancelButton.style.background = "rgba(255, 255, 255, 0.824)";
        cancelButton.disabled = false;

        // 将音频片段合并成一个 Blob 对象
        const audioBlob = new Blob(audioChunks, { type: "audio/wav" });
        // 创建一个 URL 以便在音频播放元素中播放音频
        const audioUrl = URL.createObjectURL(audioBlob);
        audioPlayback.src = audioUrl;
        audioPlayback.style.display = 'block';
        // 更新录音状态显示
        recordStatus.textContent = "状态: 录音已结束";
        
        // 停止更新进度条
        clearInterval(recordingInterval);
    };

    // 开始录音
    mediaRecorder.start();
    // 更新录音状态显示
    recordStatus.textContent = "状态: 录音中...";

    // 按钮样式与功能（写死）
    startButton.style.background = "rgba(249, 137, 136, 1)";
    startButton.disabled = true
    stopButton.style.background = "rgba(255, 255, 255, 0.824)";
    stopButton.disabled = false;
    uploadButton.style.background = "rgba(255, 255, 255, 0.4)";
    uploadButton.disabled = true;
    cancelButton.style.background = "rgba(255, 255, 255, 0.824)";
    cancelButton.disabled = false;

    audioPlayback.style.display = 'none';

    // 初始化录音时间和进度条
    recordingTime = 0;
    recordingInterval = setInterval(() => {
        recordingTime += 1;
        const percentage = (recordingTime / 60) * 100; // 假设最大录音时间为60秒
        progressBar.style.width = percentage + '%';
        progressTime.textContent = `${recordingTime}s/60s`;

        // 当录音时间达到60秒时，自动停止录音
        if (recordingTime >= 60) {
            // 停止录音
            mediaRecorder.stop();
            // 按钮样式与功能（写死）
            startButton.style.background = "rgba(255, 255, 255, 0.824)";
            startButton.disabled = false;
            stopButton.style.background = "rgba(249, 137, 136, 1)";
            stopButton.disabled = true;
            uploadButton.style.background = "rgba(255, 255, 255, 0.824)";
            uploadButton.disabled = false;
            cancelButton.style.background = "rgba(255, 255, 255, 0.824)";
            cancelButton.disabled = false;
            // 更新录音状态显示
            recordStatus.textContent = "状态: 录音已结束";
        }
    }, 1000);
});

// 处理停止录音按钮点击事件
stopButton.addEventListener("click", () => {
    // 停止录音
    mediaRecorder.stop();
    // 按钮样式与功能（写死）
    startButton.style.background = "rgba(255, 255, 255, 0.824)";
    startButton.disabled = false;
    stopButton.style.background = "rgba(249, 137, 136, 1)";
    stopButton.disabled = true;
    uploadButton.style.background = "rgba(255, 255, 255, 0.824)";
    uploadButton.disabled = false;
    cancelButton.style.background = "rgba(255, 255, 255, 0.824)";
    cancelButton.disabled = false;
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
    // 按钮样式与功能（写死）
    startButton.style.background = "rgba(255, 255, 255, 0.824)";
    startButton.disabled = false;
    stopButton.style.background = "rgba(255, 255, 255, 0.4)";
    stopButton.disabled = true;
    uploadButton.style.background = "rgba(255, 255, 255, 0.4)";
    uploadButton.disabled = true;
    cancelButton.style.background = "rgba(255, 255, 255, 0.824)";
    cancelButton.disabled = false;
    // 更新录音状态显示
    recordStatus.textContent = "状态: 尚未录音";
    // 停止更新进度条
    clearInterval(recordingInterval);
    progressBar.style.width = '0%';
    progressTime.textContent = '0s/60s';

    // 隐藏播放音频元素
    audioPlayback.style.display = 'none';
});

// 处理上传按钮点击事件
uploadButton.addEventListener("click", async () => {
    if (audioChunks.length === 0) {
        alert('没有可上传的音频。');
        return;
    }

    // 隐藏原本的界面
    document.getElementById('current-screen').style.display = 'none';
    // 显示加载界面
    document.getElementById('new-screen').style.display = 'block';

    try {
        // 创建一个 AudioContext
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
    
        // 创建一个 FormData 对象用于上传音频
        const formData = new FormData();
    
        // 将录音的 Blob 对象添加到 FormData 中
        const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });

        
        console.log('audioBlob:', audioBlob);
    
        // 使用 FileReader 读取 Blob 数据
        const reader = new FileReader();
        reader.onload = async function(event) {
            try {
                // 解码音频数据
                const arrayBuffer = event.target.result;
                const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
    
                // 提取 PCM 数据
                const pcmData = audioBuffer.getChannelData(0); // 只提取第一个声道的数据
    
                // 将 PCM 数据转换为 Int16Array
                const buffer = new ArrayBuffer(pcmData.length * 2);
                const view = new DataView(buffer);
                for (let i = 0; i < pcmData.length; i++) {
                    const s = Math.max(-1, Math.min(1, pcmData[i]));
                    view.setInt16(i * 2, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
                }
    
                // 创建一个新的 Blob 对象，类型为 "audio/pcm"
                const pcmBlob = new Blob([view], { type: 'audio/pcm' });
    
                // 将 PCM 音频添加到 FormData 中
                formData.append('audio', pcmBlob, 'recording.pcm');

                // 将书籍ID添加到 FormData 中
                const bookId = localStorage.getItem('bookNumber');
                if (bookId) {
                    formData.append('bookId', bookId);
                } else {
                throw new Error('bookId 未找到，请确认缓存中是否存在 bookId 数据。');
                }

                // 将文章ID添加到 FormData 中
                const articleId = localStorage.getItem('courseId');
                if (articleId) {
                    formData.append('courseId', articleId);
                } else {
                    throw new Error('courseId 未找到，请确认缓存中是否存在 courseId 数据。');
                }
    
                // 从缓存中取出文章数据
                const article = localStorage.getItem('courseContent');
    
                // 将文章数据添加到 FormData 中
                if (article) {
                    formData.append('courseContent', article);
                } else {
                    throw new Error('文章数据未找到，请确认缓存中是否存在 courseContent 数据。');
                }

                console.log('FormData:', formData);
                console.log('音频Blob:', pcmBlob);
                console.log('PCM数据:', view);

                // 计算 PCM 音频的大小
                const pcmSize = pcmBlob.size;

                // 估算上传时间（假设上传速度为 1MB/s）
                const uploadSpeed = 1024 * 23; // 1MB/s
                const estimatedTime = Math.ceil(pcmSize / uploadSpeed);

                console.log('pcmSize:', pcmSize);
                console.log('estimatedTime:', estimatedTime);
                
                // 显示倒计时
                let time = estimatedTime;
                const countdownElement = document.getElementById('countdown');
                countdownElement.innerText = `预计还需 ${time} 秒`;
                
                const countdownInterval = setInterval(() => {
                    time--;
                    countdownElement.innerText = `预计还需 ${time} 秒`;
                
                    if (time <= 0) {
                        clearInterval(countdownInterval);
                        countdownElement.innerText = '加载中......';
                    }
                }, 1000);
    
                // 发送 AJAX 请求到后端音频上传接口
                const uploadResponse = await fetch('http://localhost:8080/score/score', {
                    method: 'POST',
                    headers: {
                        'session': token, // 发送会话令牌
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
                    localStorage.setItem('speed', data.result.speed);
                    localStorage.setItem('pause', data.result.pause);
                    localStorage.setItem('initialConsonants', data.result.initialConsonants);
                    localStorage.setItem('finalVowels', data.result.finalVowels);
                    localStorage.setItem('tones', data.result.tones);
                    localStorage.setItem('completeness', data.result.completeness);
                    localStorage.setItem('advice', data.result.advice);
    
                    window.location.href = 'courseResult.html'; // 可选的重定向
                } else {
                    // 上传失败，显示错误信息
                    alert(`上传失败：${data.message}`);
                    console.error(`上传失败：${data.message}`);
                }
            } catch (error) {
                // 处理网络错误或其他异常
                console.error('网络错误:', error);
                alert('网络错误，请稍后再试。');
            }
        };
    
        // 读取 Blob 数据
        reader.readAsArrayBuffer(audioBlob);
    } catch (error) {
        // 处理网络错误或其他异常
        console.error('网络错误:', error);
        alert('网络错误，请稍后再试。');
    }
});

const audioTip = document.getElementById("uploadAudio");
const audioFileInput = document.getElementById("audio-file");

// 处理上传按钮点击事件
audioTip.addEventListener("click", () => {
    audioFileInput.click(); // 触发文件选择对话框
});

// 处理文件选择事件
audioFileInput.addEventListener("change", async (event) => {
    const file = event.target.files[0];

    if (!file) {
        alert('请选择一个音频文件。');
        return;
    }

    // 检查文件类型
    if (!file.type.includes('audio')) {
        alert('请选择一个有效的音频文件。');
        return;
    }

    // TODO 
    // 检查文件大小（假设1分钟音频大约为1.25MB）
    if (file.size > 1024 * 256 * 6) {
        alert('音频文件时间过长，请选择时间不超过1分钟的音频文件。');
        return;
    }

    // 隐藏原本的界面
    document.getElementById('current-screen').style.display = 'none';
    // 显示加载界面
    document.getElementById('new-screen').style.display = 'block';

    try {
        // 创建一个 AudioContext
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();

        // 创建一个 FormData 对象用于上传音频
        const formData = new FormData();

        // 使用 FileReader 读取文件数据
        const reader = new FileReader();
        reader.onload = async function(event) {
            try {
                // 解码音频数据
                const arrayBuffer = event.target.result;
                const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);

                // 提取 PCM 数据
                const pcmData = audioBuffer.getChannelData(0); // 只提取第一个声道的数据

                // 将 PCM 数据转换为 Int16Array
                const buffer = new ArrayBuffer(pcmData.length * 2);
                const view = new DataView(buffer);
                for (let i = 0; i < pcmData.length; i++) {
                    const s = Math.max(-1, Math.min(1, pcmData[i]));
                    view.setInt16(i * 2, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
                }

                // 创建一个新的 Blob 对象，类型为 "audio/pcm"
                const pcmBlob = new Blob([view], { type: 'audio/pcm' });

                // 将 PCM 音频添加到 FormData 中
                formData.append('audio', pcmBlob, 'recording.pcm');

                // 将书籍ID添加到 FormData 中
                const bookId = localStorage.getItem('bookNumber');
                if (bookId) {
                    formData.append('bookId', bookId);
                } else {
                    throw new Error('bookId 未找到，请确认缓存中是否存在 bookId 数据。');
                }

                // 将文章ID添加到 FormData 中
                const articleId = localStorage.getItem('courseId');
                if (articleId) {
                    formData.append('courseId', articleId);
                } else {
                    throw new Error('courseId 未找到，请确认缓存中是否存在 courseId 数据。');
                }

                // 从缓存中取出文章数据
                const article = localStorage.getItem('courseContent');

                // 将文章数据添加到 FormData 中
                if (article) {
                    formData.append('courseContent', article);
                } else {
                    throw new Error('文章数据未找到，请确认缓存中是否存在 courseContent 数据。');
                }

                console.log('FormData:', formData);
                console.log('音频Blob:', pcmBlob);
                console.log('PCM数据:', view);

                // 计算 PCM 音频的大小
                const pcmSize = pcmBlob.size;

                // 估算上传时间（假设上传速度为 1MB/s）
                const uploadSpeed = 1024 * 23; // 1MB/s
                const estimatedTime = Math.ceil(pcmSize / uploadSpeed);

                console.log('pcmSize:', pcmSize);
                console.log('estimatedTime:', estimatedTime);

                // 显示倒计时
                let time = estimatedTime;
                const countdownElement = document.getElementById('countdown');
                countdownElement.innerText = `预计还需 ${time} 秒`;

                const countdownInterval = setInterval(() => {
                    time--;
                    countdownElement.innerText = `预计还需 ${time} 秒`;

                    if (time <= 0) {
                        clearInterval(countdownInterval);
                        countdownElement.innerText = '加载中......';
                    }
                }, 1000);

                // 发送 AJAX 请求到后端音频上传接口
                const uploadResponse = await fetch('http://localhost:8080/score/score', {
                    method: 'POST',
                    headers: {
                        'session': token, // 发送会话令牌
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
                    localStorage.setItem('speed', data.result.speed);
                    localStorage.setItem('pause', data.result.pause);
                    localStorage.setItem('initialConsonants', data.result.initialConsonants);
                    localStorage.setItem('finalVowels', data.result.finalVowels);
                    localStorage.setItem('tones', data.result.tones);
                    localStorage.setItem('completeness', data.result.completeness);
                    localStorage.setItem('advice', data.result.advice);

                    console.log('上传成功，结果:', data.result);

                    window.location.href = 'courseResult.html'; // 可选的重定向
                } else {
                    // 上传失败，显示错误信息
                    alert(`上传失败：${data.message}`);
                    console.error(`上传失败：${data.message}`);
                }
            } catch (error) {
                // 处理网络错误或其他异常
                console.error('网络错误:', error);
                alert('网络错误，请稍后再试。');
            }
        };

        // 读取文件数据
        reader.readAsArrayBuffer(file);
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