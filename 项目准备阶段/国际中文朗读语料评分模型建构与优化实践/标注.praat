path$ = "C:\Users\Celeste\Desktop\demo\cn\"
;path$ = chooseDirectory$("选择demo里面的cn文件夹")
;path$ = path$ + "\"
Create Strings as file list: "Score", path$+"*.txt"
Create Strings as file list: "Spoken", path$+"*_16000.wav"
fileTotal = Get number of strings
writeInfoLine: ""
for iFile to fileTotal
	iFile$ = string$(iFile)
	selectObject: "Strings Score"
	sequence$ = Get string: iFile
	Read from file: "'path$''sequence$'"
	Rename: "'iFile$'"
	selectObject: "Strings Spoken"
	sequence$ = Get string: iFile
	Read from file: "'path$''sequence$'"
	Rename: "'iFile$'"
	dur = Get total duration
	Resample: 16000, 50
	Override sampling frequency: 16000*(dur+0.06)/dur
endfor
for iFile to fileTotal
	iFile$ = string$(iFile)
	selectObject: "Table 'iFile$'"
	rowTotal = Get number of rows
	beg_pos# = zero#(rowTotal)
	end_pos# = zero#(rowTotal)
	time_len# = zero#(rowTotal)
	symbol# = zero#(rowTotal)
	is_yun# = zero#(rowTotal)
	for i to rowTotal
		content$ = Get value: i, "content"
		beg_pos = Get value: i, "beg_pos"
		end_pos = Get value: i, "end_pos"
		symbol$ = Get value: i, "symbol"
		time_len = Get value: i, "time_len"
		rec_node_type = Get value: i, "rec_node_type"
		is_yun = Get value: i, "is_yun"
		beg_pos#[i] = beg_pos * 0.01
		end_pos#[i] = end_pos * 0.01
		time_len#[i] = time_len * 0.01
		is_yun#[i] = is_yun
		symLen = length(symbol$)
		if symLen > 1
			symbol#[i] = 1
		else
			symbol#[i] = 0
		endif
	endfor
	selectObject: "Sound 'iFile$'_16000"
	To TextGrid: "syl pinyin shengyun", ""
	for j from 3 to rowTotal
		selectObject: "TextGrid 'iFile$'_16000"
		beg_exi = Get interval boundary from time: 1, beg_pos#[j]
			if beg_exi = 0
				if symbol#[j] = 1
					Insert boundary: 1, beg_pos#[j]
					Insert boundary: 2, beg_pos#[j]
					Insert boundary: 3, beg_pos#[j]
				elsif is_yun#[j] = 1
					Insert boundary: 3, beg_pos#[j]
				endif
			endif
		circul = 0
		end_exi = Get interval boundary from time: 1, end_pos#[j]
			if end_exi = 0
				if symbol#[j] = 1
					Insert boundary: 1, end_pos#[j]
					Insert boundary: 2, end_pos#[j]
					Insert boundary: 3, end_pos#[j]
				endif
			endif
	endfor
	selectObject: "TextGrid 'iFile$'_16000"
	intTotal = Get number of intervals: 1
	int = 2
	for k from 3 to rowTotal
		if symbol#[k] = 1
			selectObject: "Table 'iFile$'"
			content$ = Get value: k, "content"
			symbol$ = Get value: k, "symbol"
			selectObject: "TextGrid 'iFile$'_16000"
			Set interval text: 1, int, content$
			Set interval text: 2, int, symbol$
			int = int + 1
		endif
	endfor
	selectObject: "TextGrid 'iFile$'_16000"
	intSYTotal = Get number of intervals: 3
	intSY = 2
	for q to rowTotal
		selectObject: "Table 'iFile$'"
		content$ = Get value: q, "content"
		if is_yun#[q] < 2
			selectObject: "TextGrid 'iFile$'_16000"
			Set interval text: 3, intSY, content$
			intSY = intSY + 1
		endif
	selectObject: "TextGrid 'iFile$'_16000"
	Save as text file: "C:\Users\Celeste\Desktop\demo\textgrid\'iFile$'_16000.TextGrid"
	endfor
endfor
;select all
;Remove
