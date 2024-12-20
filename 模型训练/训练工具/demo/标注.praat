path$ = defaultDirectory$ + "\cn\"
;path$ = chooseDirectory$("选择demo里面的cn文件夹")
;path$ = path$ + "\"
Create Strings as file list: "Score", path$+"*.txt"
Create Strings as file list: "Spoken", path$+"*_16000.wav"
fileTotal = Get number of strings
writeInfoLine: ""

;解决0.06的数字误差
for iFile to fileTotal
	iFile$ = string$(iFile)
	selectObject: "Strings Score"
	seqSco$ = Get string: iFile
	Read from file: "'path$''seqSco$'"
	;Rename: "'seqSco$'"
	selectObject: "Strings Spoken"
	seqSpo$ = Get string: iFile
	Read from file: "'path$''seqSpo$'"
	;Rename: "'seqSpo$'"
	dur = Get total duration
	Override sampling frequency: 16000*(dur+0.06)/dur
endfor

;读取表格中的数据
for iFile to fileTotal
	selectObject: "Strings Score"
	seq$ = Get string: iFile
	seq$ = seq$ - ".txt"
	seq$ = replace$(seq$, "(", "_", 0)
	seq$ = replace$(seq$, ")", "_", 0)
	selectObject: "Table 'seq$'"
	rowTotal = Get number of rows
	beg_pos# = zero#(rowTotal)
	end_pos# = zero#(rowTotal)
	time_len# = zero#(rowTotal)
	symbol# = zero#(rowTotal)
	is_yun# = zero#(rowTotal)
	rec_node_type# = zero#(rowTotal)
	dp_message# = zero#(rowTotal)
	perr_msg# = zero#(rowTotal)
	for i to rowTotal
		content$ = Get value: i, "content"
		beg_pos = Get value: i, "beg_pos"
		end_pos = Get value: i, "end_pos"
		symbol$ = Get value: i, "symbol"
		time_len = Get value: i, "time_len"
		rec_node_type$ = Get value: i, "rec_node_type"
		is_yun = Get value: i, "is_yun"
		dp_message = Get value: i, "dp_message"
		perr_msg = Get value: i, "perr_msg"
		beg_pos#[i] = beg_pos * 0.01
		end_pos#[i] = end_pos * 0.01
		time_len#[i] = time_len * 0.01
		recLen = length(rec_node_type$)
		rec_node_type#[i] = recLen
		is_yun#[i] = is_yun
		dp_message#[i] = dp_message
		perr_msg#[i] = perr_msg
		symLen = length(symbol$)
		if symLen > 1
			symbol#[i] = 1
		else
			symbol#[i] = 0
		endif
	endfor

;获得有效数据的开始阶段，这是为了排除最开始的几个时间数据为0的信息
	break = 0
	for m to rowTotal
		if symbol#[m] = 1
			break = break + 1
			if break = 1
				first = m
			endif
		endif
	endfor

;插入边界
	selectObject: "Sound 'seq$'"
	To TextGrid: "syl pinyin shengyun sen_error syl_error", ""
	for j from first to rowTotal
		count = 0
		selectObject: "TextGrid 'seq$'"
		beg_exi = Get interval boundary from time: 1, beg_pos#[j]
		end_exi = Get interval boundary from time: 1, end_pos#[j]

		if rec_node_type#[j] = 5
			if symbol#[j] = 1
				count = 1
			endif
		elsif rec_node_type#[j] = 3
			if dp_message#[j] >= 0
				count = 1
			endif
		endif

		if count = 1
			if beg_exi = 0
				if beg_pos#[j] > 0
					Insert boundary: 1, beg_pos#[j]
					Insert boundary: 2, beg_pos#[j]
					Insert boundary: 3, beg_pos#[j]
					Insert boundary: 4, beg_pos#[j]
					Insert boundary: 5, beg_pos#[j]
				endif
			endif
			if end_exi = 0
				if end_pos#[j] > 0
					if end_pos#[j] <> beg_pos#[j]
						Insert boundary: 1, end_pos#[j]
						Insert boundary: 2, end_pos#[j]
						Insert boundary: 3, end_pos#[j]
						Insert boundary: 4, end_pos#[j]
						Insert boundary: 5, end_pos#[j]
					endif
				endif
			endif
		endif

		if is_yun#[j] = 1
			if beg_pos#[j] > 0
				Insert boundary: 3, beg_pos#[j]
				Insert boundary: 5, beg_pos#[j]
			endif
		endif
	endfor

;填入信息
	selectObject: "TextGrid 'seq$'"
	intTotal = Get number of intervals: 1
	int = 2
	for k from first to rowTotal
		count = 0
		if symbol#[k] = 1
			if time_len#[k] <> 0
				count = 1
			endif
		endif
		if rec_node_type#[k] = 3
			if dp_message#[k] >= 0
				count = 1
			endif
		endif
		if dp_message#[k] = 16
			Set interval text: 4, int, "前有漏读"
		elsif dp_message#[k] = 32
			Set interval text: 4, int, "增读"
		elsif dp_message#[k] = 64
			int = int - 1
			Set interval text: 4, int, "回读"
			int = int + 1
		elsif dp_message#[k] = 128
			Set interval text: 4, int, "替换"
		endif
		if count = 1
			selectObject: "Table 'seq$'"
			content$ = Get value: k, "content"
			symbol$ = Get value: k, "symbol"
			selectObject: "TextGrid 'seq$'"
			Set interval text: 1, int, content$
			Set interval text: 2, int, symbol$
		int = int + 1
		endif
	endfor
	selectObject: "TextGrid 'seq$'"
	intSYTotal = Get number of intervals: 3
	intSY = 2
	for q from first to rowTotal
		count = 0
		selectObject: "Table 'seq$'"
		content$ = Get value: q, "content"
		if is_yun#[q] < 2
			if end_pos#[q] > 0
				if beg_pos#[q] > 0
					count = 1
				endif
			endif
		endif
		if rec_node_type#[q] = 3
			if dp_message#[q] >= 0
				count = 1
			endif
		endif
		if count = 1
			selectObject: "TextGrid 'seq$'"
			Set interval text: 3, intSY, content$
			if perr_msg#[q] = 1
				Set interval text: 5, intSY, "声韵错"
			elsif perr_msg#[q] = 2
				Set interval text: 5, intSY, "调型错"
			elsif perr_msg#[q] = 3
				Set interval text: 5, intSY, "声韵调型错"
			endif
		intSY = intSY + 1
		endif
		selectObject: "TextGrid 'seq$'"
		Save as text file: defaultDirectory$ + "\textgrid\'seq$'.TextGrid"
		selectObject: "Sound 'seq$'"
		Save as WAV file: defaultDirectory$ + "\textgrid\'seq$'.wav"
	endfor
endfor
;select all
;Remove
