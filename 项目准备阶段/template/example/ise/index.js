(function () {
  let btnStatus = "UNDEFINED"; // "UNDEFINED" "CONNECTING" "OPEN" "CLOSING" "CLOSED"

  const btnControl = document.getElementById("btn_control");

  const recorder = new RecorderManager("../../dist");
  recorder.onStart = () => {
    changeBtnStatus("OPEN");
  }
  let iseWS;

  /**
   * 获取websocket url
   * 该接口需要后端提供，这里为了方便前端处理
   */
  function getWebSocketUrl() {
    // 请求地址根据语种不同变化
    var url = "wss://ise-api.xfyun.cn/v2/open-ise";
    var host = "ise-api.xfyun.cn";
    var apiKey = "0bd876a7866a314f1dbe9a1024eccd2f";
    var apiSecret = "MzFiZDM0YWY3OWRmNjFmZWY2ZDhmYjY3";
    var date = new Date().toGMTString();
    var algorithm = "hmac-sha256";
    var headers = "host date request-line";
    var signatureOrigin = `host: ${host}\ndate: ${date}\nGET /v2/open-ise HTTP/1.1`;
    var signatureSha = CryptoJS.HmacSHA256(signatureOrigin, apiSecret);
    var signature = CryptoJS.enc.Base64.stringify(signatureSha);
    var authorizationOrigin = `api_key="${apiKey}", algorithm="${algorithm}", headers="${headers}", signature="${signature}"`;
    var authorization = btoa(authorizationOrigin);
    url = `${url}?authorization=${authorization}&date=${date}&host=${host}`;
    return url;
  }

  function toBase64(buffer) {
    var binary = "";
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  function changeBtnStatus(status) {
    btnStatus = status;
    if (status === "CONNECTING") {
      btnControl.innerText = "建立连接中";
    } else if (status === "OPEN") {
      btnControl.innerText = "录音中...";
    } else if (status === "CLOSING") {
      btnControl.innerText = "关闭连接中";
    } else if (status === "CLOSED") {
      btnControl.innerText = "开始录音";
    }
  }

  function renderResult(resultData) {
    // 识别结束
    let jsonData = JSON.parse(resultData);
    console.log(jsonData.data);

    if (jsonData.data && jsonData.data.data) {
      let data = window.atob(jsonData.data.data);
      let grade = parser.parse(data, {
        attributeNamePrefix: "",
        ignoreAttributes: false,
      });
      console.log(grade);
      const readSentence =
        grade?.xml_result?.read_sentence?.rec_paper?.read_chapter;
      document.getElementById("accuracy_score").innerText =
        readSentence?.accuracy_score;
      document.getElementById("fluency_score").innerText =
        readSentence?.fluency_score;
      document.getElementById("integrity_score").innerText =
        readSentence?.integrity_score;
      document.getElementById("phone_score").innerText =
        readSentence?.phone_score || 0;
      document.getElementById("tone_score").innerText =
        readSentence?.tone_score || 0;
      document.getElementById("emotion_score").innerText =
        readSentence?.emotion_score || 0;
      document.getElementById("total_score").innerText =
        readSentence?.total_score;
      let sentence = readSentence?.word || [];
      let resultStr = "";
      sentence.forEach((item) => {
        if (item?.word) {
          item.word.forEach((wt) => {
            let flag = false;
            if (wt.syll?.phone) {
              flag = wt.syll.phone.some((pt) => pt?.perr_msg != 0);
            } else {
              wt.syll.forEach((st) => {
                if (Array.isArray(st?.phone)) {
                  flag = st.phone.some((pt) => pt?.perr_msg != 0);
                }
              });
            }
            if (flag) {
              resultStr += `<span class="err">${wt.content}</span>`;
            } else {
              resultStr += wt.content;
            }
          });
        } else if (item?.syll) {
          let flag = false;
          if (item.syll?.phone) {
            flag = item.syll.phone.some((pt) => pt?.perr_msg != 0);
          } else {
            item.syll.forEach((st) => {
              if (Array.isArray(st?.phone)) {
                flag = st.phone.some((pt) => pt?.perr_msg != 0);
              }
            });
          }
          if (flag) {
            resultStr += `<span class="err">${item.content}</span>`;
          } else {
            resultStr += item.content;
          }
        }
      });
      if (resultStr) {
        document.getElementById("right").style.display = "block";
        document.getElementById("result").innerHTML = resultStr;
      } else {
        document.getElementById("right").style.display = "none";
      }
    }
    if (jsonData.code === 0 && jsonData.data.status === 2) {
      iseWS.close();
    }
    if (jsonData.code !== 0) {
      iseWS.close();
      console.error(jsonData);
    }
  }

  function connectWebSocket() {
    const websocketUrl = getWebSocketUrl();
    if ("WebSocket" in window) {
      iseWS = new WebSocket(websocketUrl);
    } else if ("MozWebSocket" in window) {
      iseWS = new MozWebSocket(websocketUrl);
    } else {
      alert("浏览器不支持WebSocket");
      return;
    }
    changeBtnStatus("CONNECTING");
    iseWS.onopen = (e) => {
      // 开始录音
      recorder.start({
        sampleRate: 16000,
        frameSize: 1280,
      });
      var params = {
        common: {
          app_id: APPID,
        },
        business: {
          category: "read_sentence", // read_syllable/单字朗读，汉语专有 read_word/词语朗读  read_sentence/句子朗读 https://www.xfyun.cn/doc/Ise/IseAPI.html#%E6%8E%A5%E5%8F%A3%E8%B0%83%E7%94%A8%E6%B5%81%E7%A8%8B
          rstcd: "utf8",
          group: "pupil",
          sub: "ise",
          tte: "utf-8",
          cmd: "ssb",
          auf: "audio/L16;rate=16000",
          ent: "en_vip",
          aus: 1,
          aue: "raw",
          text:
            "\uFEFF" +
            (document.getElementById("text")?.value || "where are you"),
        },
        data: {
          status: 0,
          // data_type: 1,
          // encoding: "raw",
        },
      };
      iseWS.send(JSON.stringify(params));
    };
    iseWS.onmessage = (e) => {
      renderResult(e.data);
    };
    iseWS.onerror = (e) => {
      console.error(e);
      recorder.stop();
      changeBtnStatus("CLOSED");
    };
    iseWS.onclose = (e) => {
      recorder.stop();
      changeBtnStatus("CLOSED");
    };
  }

  recorder.onFrameRecorded = ({ isLastFrame, frameBuffer }) => {
    if (iseWS.readyState === iseWS.OPEN) {
      iseWS.send(
        JSON.stringify({
          business: {
            aue: "raw",
            cmd: "auw",
            aus: isLastFrame ? 4 : 2,
          },
          data: {
            status: isLastFrame ? 2 : 1,
            data: toBase64(frameBuffer),
            data_type: 1
          },
        })
      );
      if (isLastFrame) {
        changeBtnStatus("CLOSING");
      }
    }
  };
  recorder.onStop = () => {
  };

  btnControl.onclick = function () {
    if (btnStatus === "UNDEFINED" || btnStatus === "CLOSED") {
      connectWebSocket();
    } else if (btnStatus === "CONNECTING" || btnStatus === "OPEN") {
      // 结束录音
      recorder.stop();
    }
  };
})();
