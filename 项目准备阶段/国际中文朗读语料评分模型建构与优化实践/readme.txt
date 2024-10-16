1. 打开pychram，右上角选择open，打开demo这个文件夹
2. 有几个包是必须要下载的，在terminal窗口输入指令：
pip install librosa websocket==0.2.1 websocket-client==0.56.0 pandas openpyxl
3. 运行audio，获得pcm文件，保存在demo目录下的cn文件夹内。
4. 运行ise_ws_python3_demo，获得xml文件，保存在demo目录下的cn文件夹内。
5. 运行transfer_score，获得xlex和txt文件，保存在demo目录下的cn文件夹内。以上三个都需要等待“Process finished with exit code 0”出现才算是运行结束。
6. 打开praat，运行“标注.praat”这个文件。
7. 产生的textgrid文件在demo目录下的textgrid文件夹内。
8. 对于这些运行过后cn和textgrid文件夹内的文件，再次运行会覆盖。