import jieba
import collections
import matplotlib.pyplot as plt
import random
import json

filename = "test_ch.txt"
max_dif = 3
max_cut = 0.9
min_cut = 0.1
# 方法选取临界值
valueChooseOnlyWords = 5
valueChooseKeySenAndWords = 2
valueChooseAllSen = 1
# 方法排他初始值
methodTag = 1

# 导入停止词
with open("中文停用词表1.txt", "r", encoding="utf-8") as myfile:
    text = myfile.read()
    text = text.replace('\n', ',')
    stopwords = text.split(',')
with open("中文停用词表2.txt", "r", encoding="utf-8") as myfile2:
    text = myfile2.read()
    text = text.replace('\n', ',')
    stopwords = stopwords + text.split(',')

with open(filename, "r", encoding="utf-8") as myfile:
    text = myfile.read()
    text = text.replace('\n', '')
    text = text.replace('\t', '')
    text = text.replace(' ', '')
words = jieba.cut(text, cut_all=False)
text_list = []
object_list = []
for word in words:
    if word not in stopwords and len(word) > 1:
        object_list.append(word)
    text_list.append(word)

word_counts = collections.Counter(object_list)
top10 = word_counts.most_common(10)
print(top10)

text_counts = collections.Counter(text_list)
top10_text = text_counts.most_common(10)

str_list = text.split("。")
thisStr = ""
showStr = ""
partOutPutList = []
for i in range(len(str_list)):
    cutAble = True
    thisStr += str_list[i] + "。"
    showStr += str_list[i] + "。"
    thisStrList = jieba.cut(thisStr, cut_all=False)
    this_counts = collections.Counter(thisStrList)
    if i != len(str_list) - 1:
        nextStr = thisStr + str_list[i + 1] + "。"
        nextStrList = jieba.cut(nextStr, cut_all=False)
        next_counts = collections.Counter(thisStrList)
    else:
        break
    for k in range(10):
        this_word_counts = this_counts[top10[k][0]]
        next_word_counts = next_counts[top10[k][0]]
        dif = abs(next_word_counts - this_word_counts)
        if (dif > max_dif and cutAble == True):
            cutAble = False
            # print(showStr+'\n'+"*"*28+'\n')
            partOutPutList.append(showStr)
            showStr = ""
outFile = open("output630.txt", "w+")


def onlyWords(thisStr_list_jieba_counts):
    showList = []
    for i in range(10):
        if thisStr_list_jieba_counts[top10[i][0]] != 0:
            showList.append("-" + top10[i][0] + "\n")
            # print(top10[i][0])
    outFile.writelines(showList)


def keySenAndWords(thisStr_list_Sen, thisPartKeyWords):
    showList = []
    aimList = []
    for i in range(len(thisStr_list_Sen)):
        plus = 0
        thisSen_jieba = jieba.cut(thisStr_list_Sen[i])
        for s in thisSen_jieba:
            if s not in stopwords and len(s) > 1:
                aimList.append(s)
        thisSen_jieba_counts_top10 = collections.Counter(aimList).most_common(10)
        # print(thisSen_jieba_counts_top10)
        for k in range(len(thisSen_jieba_counts_top10)):
            plus += 1
            match = thisSen_jieba_counts_top10[k][0]
            if match in thisPartKeyWords:
                '''
                theRand=int(random.randint(0,len(thisPartKeyWords)))
                showList.append([thisPartKeyWords[theRand-1],plus,0])
                plus+=1
                showList.append([thisStr_list_Sen[i],plus,plus-1])
                '''
                showList.append([match, plus, 0])
                plus += 1
                break
        showList.append([thisStr_list_Sen[i], plus, plus - 1])
    showList.append('*' * 48)

    for i in showList:
        outFile.writelines(str(i) + '\n')


def allSen(thisStr_list_Sen, thisPartKeyWords):
    showList = []
    for i in range(len(thisStr_list_Sen)):
        matchOrNot = False
        thisSen_jieba = jieba.cut(thisStr_list_Sen[i])
        thisSen_jieba_counts = collections.Counter(thisSen_jieba)
        for k in range(len(thisPartKeyWords)):
            if thisSen_jieba_counts[thisPartKeyWords[k]] != 0:
                theRand = int(random.randint(0, len(thisPartKeyWords)))
                showList.append("-" + thisPartKeyWords[theRand - 1] + " : " + thisStr_list_Sen[i] + "\n")
                matchOrNot = True
                break
        # if(matchOrNot==False):
        # showList.append("-"+thisStr_list_Sen[i]+"\n")
    outFile.writelines(showList)


def allText(thisStr_list_Sen):
    showList = []
    for i in range(len(thisStr_list_Sen)):
        showList.append(thisStr_list_Sen[i] + "\n")
    outFile.writelines(showList)


for i in range(len(partOutPutList)):
    thisStr = partOutPutList[i]
    thisStr_list_Sen = thisStr.split("。")
    thisStr_list_jieba = jieba.cut(thisStr, cut_all=False)
    thisStr_list_jieba_counts = collections.Counter(thisStr_list_jieba)
    # thisStr_list_jieba_counts_firstlist=[]
    # for m in range(len(thisStr_list_jieba_counts)):
    # thisStr_list_jieba_counts_firstlist.append(thisStr_list_jieba_counts[m][0])
    thisPartKeyWords = []
    showtimes = 0
    keyWordsTimes = 0
    for k in range(10):
        showtimes += thisStr_list_jieba_counts[top10[k][0]]
        if thisStr_list_jieba_counts[top10[k][0]] != 0:
            keyWordsTimes += 1
            thisPartKeyWords.append(top10[k][0])
    print(showtimes)
    if keyWordsTimes >= valueChooseOnlyWords and methodTag == 1:
        methodTag = 2
        onlyWords(thisStr_list_jieba_counts)
    elif keyWordsTimes >= valueChooseKeySenAndWords and methodTag == 2:
        methodTag = 3
        keySenAndWords(thisStr_list_Sen, thisPartKeyWords)
    elif keyWordsTimes >= valueChooseAllSen:
        method = 2
        # allSen(thisStr_list_Sen,thisPartKeyWords)
        keySenAndWords(thisStr_list_Sen, thisPartKeyWords)
    else:
        allText(thisStr_list_Sen)

outFile.close()





