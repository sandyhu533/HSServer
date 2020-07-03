import jieba
import collections


class DisplayMode:
    # 关键词并列：本段中出现的top10，取其中一部分并列
    @staticmethod
    def onlyWords(this_part_str, stopwords_list, keywords_list, output_list):
        words = jieba.lcut(this_part_str)
        words_list_done = []
        for word in words:
            if word not in stopwords_list and word not in words_list_done and word in keywords_list and len(word) > 1:
                words_list_done.append(word)
        for i in range(len(words_list_done)):
            output_list.append([words_list_done[i], len(output_list) + 1, 0])

    @staticmethod
    def senAndWords(this_part_str, stopwords_list, keywords_list, output_list):
        words = jieba.lcut(this_part_str)
        words_list_done = []
        now_num = len(output_list)
        for word in words:
            if word not in stopwords_list and word not in words_list_done and word in keywords_list and len(word) > 1:
                words_list_done.append(word)
        this_part_str_list = this_part_str.split("。")
        print(123)
        for word in words_list_done:
            for sen in this_part_str_list:
                if word in sen:
                    output_list.append([word, now_num + 1, 0])
                    output_list.append([sen, now_num + 2, now_num + 1])
                    this_part_str_list.remove(sen)
                    now_num+=2
                    break

    @staticmethod
    def allSen(this_part_str, stopwords_list, keywords_list, output_list):
        output_list.append([this_part_str, len(output_list) + 1, 0])
