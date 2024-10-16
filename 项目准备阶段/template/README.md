# 文档

## 前置步骤

1. 将dist目录下`processor.worker.js`、`processor.worklet.js`复制到项目目录中，使其可以通过浏览器地址栏访问，
2. 浏览器录音权限需要在localhost、127.0.0.1或https下

## RecorderManager

浏览器录音管理器

## 方法

### RecorderManager.constructor(processorPath)

构造函数

> `processorPath`是 `processor.worker.js`、`processor.worklet.js`的路径地址，如果访问地址`/a/b/c/processor.worker.js`，则processorPath为`/a/b/c`

### RecorderManager.start(Object object)

开始录音

Object object

| 属性 | 类型 | 默认值 | 必填 | 取值范围 | 说明 |
| ------ | ------ | ------ | ------ | ------ | ------ |
| sampleRate | number | 浏览器默认采样率 | 否 | 8000 ~ 96000 | 采样率 |
| frameSize | number |  -  | 否 | >0 | 指定帧大小，传入 frameSize 后，每录制指定帧大小的内容后，会回调录制的文件内容，不指定则不会回调。ps:在不支持设置采样率的浏览器下，录音分片数据会比此处设置的值大 |
| arrayBufferType | string | - | 否 | "short16" "float32" | 音频数据ArrayBuffer类型 |

### RecorderManager.stop()

停止录音

> 调用stop后，还需要等待浏览器处理完已经录制的音频后才会真正结束，需要在停止录音成功后做的处理请在`.onStop`回调中处理

### RecorderManager.onStart = function listener

监听录音开始事件

### RecorderManager.onStop = function listener

监听录音结束事件

```js
const listener = (audioBuffers: ArrayBuffer[]) => {}
```

| 属性 | 类型 | 说明 |
| ------ | ------ | ------ |
| audioBuffers | ArrayBuffer[] | 录音分片数据数组 |

### RecorderManager.onFrameRecorded = function listener

监听已录制完指定帧大小的文件事件。如果设置了 frameSize，则会回调此事件。

```js
const listener = ({isLastFrame, frameBuffer} : {
  isLastFrame: boolean;
  frameBuffer: ArrayBuffer;
}) => {}
```

| 属性 | 类型 | 说明 |
| ------ | ------ | ------ |
| isLastFrame | boolean | 当前帧是否正常录音结束前的最后一帧 |
| frameSize | ArrayBuffer | 录音分片数据  |

## 示例代码

```js
const recorder = new RecorderManager ("../../dist");

recorder.onFrameRecorded = ({ isLastFrame, frameBuffer }) => {
  console.log(isLastFrame, frameBuffer)
};
recorder.onStop = (audioBuffers) => {
  console.log(audioBuffers)
}


recorder.start({
  sampleRate: 16000,
  frameSize: 1280,
});

setTimeout(() => {
  recorder.stop()
}, 60000)

```

## 听写示例

查看example/iat
将example/iat/index.html下面的值替换成真实的值

```js
  var APPID = "xxx";
  var API_SECRET = "xxx";
  var API_KEY = "xxx";
```

## 实时语音转写

查看example/rtasr
将example/rtasr/index.html下面的值替换成真实的值

```js
  var APPID = "xxx";
  var API_KEY = "xxx";

```

### 运行

1. 安装 `npm install -g http-server`
2. 运行 `http-server .`
3. 浏览器打开听写示例：http://127.0.0.1:xxx/example/iat/index.html
4. 浏览器打开实时语音转写示例：http://127.0.0.1:xxx/example/rtasr/index.html