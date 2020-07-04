import jieba
import collections


class FileProcessing:
    def __init__(self, origin_file_path_p, output_file_path_p, ):
        # 源文件路径
        self.origin_file_path = origin_file_path_p
        # 输出文件路径
        self.output_file_path = output_file_path_p
        # 中文停用词表
        self.stopwords_list = self.getStopwordsList()
        # 文本分段斜率
        self.max_dif = 1
        # 初步处理的源文本
        self.origin_text = self.getOriginText()
        # 删去停用词后的经过jieba分词的列表
        self.origin_text_jieba_list = self.getJiebaList()
        # 文本关键词，取前10
        self.key_words_2 = collections.Counter(self.origin_text_jieba_list).most_common(10)
        # 文本关键词，仅词
        self.key_words = self.getKeywords()
        # 文本分段结果
        self.text_cut_list = self.getCutList()
        # 输出树节点列表
        self.output_list = []

    def getStopwordsList(self):
        with open("中文停用词表1.txt", "r", encoding="utf-8") as myfile:
            text = myfile.read()
            text = text.replace('\n', ',')
            stopwords = text.split(',')
        with open("中文停用词表2.txt", "r", encoding="utf-8") as myfile2:
            text = myfile2.read()
            text = text.replace('\n', ',')
            stopwords = stopwords + text.split(',')
        return stopwords

    def getOriginText(self):
        with open(self.origin_file_path, "r", encoding="utf-8") as myfile:
            text = myfile.read()
            text = text.replace('\n', '')
            text = text.replace('\t', '')
            text = text.replace(' ', '')
        return text

    def getJiebaList(self):
        words = jieba.lcut(self.origin_text, cut_all=False)
        object_list = []
        for word in words:
            if word not in self.stopwords_list and len(word) > 1:
                object_list.append(word)
        return object_list

    def getKeywords(self):
        keywords_list=[]
        for k in self.key_words_2:
            keywords_list.append(k[0])
        return keywords_list

    def getCutList(self):
        str_list = self.origin_text.split("。")
        thisStr = ""
        showStr = ""
        partOutPutList = []
        for i in range(len(str_list)):
            cutAble = True
            thisStr += str_list[i] + "。"
            showStr += str_list[i] + "。"
            thisStrList = jieba.lcut(thisStr, cut_all=False)
            this_counts = collections.Counter(thisStrList)
            if i != len(str_list) - 1:
                nextStr = thisStr + str_list[i + 1] + "。"
                nextStrList = jieba.lcut(nextStr, cut_all=False)
                next_counts = collections.Counter(nextStrList)
            else:
                break
            for k in range(10):
                this_word_counts = this_counts[self.key_words_2[k][0]]
                next_word_counts = next_counts[self.key_words_2[k][0]]
                dif = abs(next_word_counts - this_word_counts)
                if (dif > self.max_dif and cutAble == True):
                    cutAble = False
                    partOutPutList.append(showStr)
                    showStr = ""
                #print(this_word_counts,next_word_counts)
        return partOutPutList
