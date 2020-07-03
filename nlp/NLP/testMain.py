import jieba
import collections
import DisplayMode
import FileProcessing
import json

plus_to_choose_onlyWords=4
plus_to_choose_senAndWords=1

output_list=[]


file_processing=FileProcessing.FileProcessing("test_ch.txt", "tmp.txt")
num_of_part=len(file_processing.text_cut_list)
output_file=open(file_processing.output_file_path,"w",encoding="utf-8")

for i in range(num_of_part):
    output_list.append([-1])
    this_part_str=file_processing.text_cut_list[i]
    words = jieba.lcut(this_part_str)
    words_list_done = []
    for word in words:
        if word not in file_processing.stopwords_list and word not in words_list_done\
                and\
                word in file_processing.key_words and len(word) > 1:
            words_list_done.append(word)

    num_of_keywords=len(words_list_done)

    if num_of_keywords>plus_to_choose_onlyWords:
        DisplayMode.DisplayMode.onlyWords(this_part_str,file_processing.stopwords_list,file_processing.key_words,output_list)
    elif num_of_keywords>plus_to_choose_senAndWords:
        DisplayMode.DisplayMode.senAndWords(this_part_str,file_processing.stopwords_list,file_processing.key_words,output_list)
    else:
        DisplayMode.DisplayMode.allSen(this_part_str,file_processing.stopwords_list,file_processing.key_words,output_list)
    #分页
    output_list.append([-1])
#print(len(output_list))
for output in output_list:
    #print(output)
    if len(output)==1:
        output_file.write("*"*32+'\n')
    else:
        output_file.write(str(output)+'\n')
output_file.close()

src=open("tmp.txt","r",encoding='utf-8')
lines_list=src.readlines()
def getPageLines():
    lines=[]
    label=0
    thisLines=0
    for line in lines_list:
        if line[0]=='*':
            if label==1:
                label=0
            else:
                label=1
        else:
            thisLines+=1
        if label==0:
            lines.append(thisLines)
            thisLines=0
    return lines
def getLineContent(n):
    theSum=0
    for line in lines_list:
        if line[0]=='*':
            continue
        else:
            theSum+=1
        if theSum==n:
            return line.strip('\n')


pageLines = getPageLines()
plus = 0
jsontext = {'words': []}
for i in range(len(pageLines)):
    NList = []
    # for k in range(pageLines[i]):
    k = 0
    while k < pageLines[i]:
        plus += 1
        k += 1
        print(plus)
        print(type(getLineContent(plus)))
        theList = eval(getLineContent(plus))
        nextList = eval(getLineContent(plus + 1))
        if nextList[2] == theList[1]:
            # 共同添加
            NList.append({'content': theList[0], 'type': 1})
            NList.append({'content': nextList[0], 'type': 2})
            plus += 1
            k += 1
        else:
            NList.append({'content': theList[0], 'type': 1})

    jsontext['words'].append({'page': NList})

jsondata = json.dumps(jsontext, indent=4, separators=(',', ':'), ensure_ascii=False)
f = open("test_ch.json", 'w')
f.write(jsondata)
f.close()
