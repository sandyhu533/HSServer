import jieba
import DisplayMode
import FileProcessing
import Transform


class Main:
    def __init__(self, origin_file_path, result_file_path):
        self.plus_to_choose_onlyWords = 5
        self.plus_to_choose_senAndWords = 1
        self.file_processing = FileProcessing.FileProcessing(origin_file_path, result_file_path)
        self.num_of_part = len(self.file_processing.text_cut_list)
        self.output_list = self.writeToList()
        self.temp_path = "temp.txt"
        self.output_file = open(self.temp_path, "w", encoding="utf-8")

    def writeToList(self):
        output_list = []
        # 模式判定与写入列表
        for i in range(self.num_of_part):
            # 分页
            output_list.append([-1])
            this_part_str = self.file_processing.text_cut_list[i]
            words = jieba.lcut(this_part_str)
            words_list_done = []
            for word in words:
                if word not in self.file_processing.stopwords_list and word not in words_list_done \
                        and \
                        word in self.file_processing.key_words and len(word) > 1:
                    words_list_done.append(word)

            num_of_keywords = len(words_list_done)

            if num_of_keywords > self.plus_to_choose_onlyWords:
                DisplayMode.DisplayMode.onlyWords(this_part_str, self.file_processing.stopwords_list,
                                                  self.file_processing.key_words,
                                                  output_list)
            elif num_of_keywords > self.plus_to_choose_senAndWords:
                DisplayMode.DisplayMode.senAndWords(this_part_str, self.file_processing.stopwords_list,
                                                    self.file_processing.key_words,
                                                    output_list)
            else:
                DisplayMode.DisplayMode.allSen(this_part_str, self.file_processing.stopwords_list,
                                               self.file_processing.key_words,
                                               output_list)
            # 分页
            output_list.append([-1])
        return output_list

    def writeAndTransform(self):
        # 写入数据
        for output in self.output_list:
            # print(output)
            if len(output) == 1:
                self.output_file.write("*" * 32 + '\n')
            else:
                self.output_file.write(str(output) + '\n')
        self.output_file.close()

        # 格式转换
        transformer = Transform.Transform(self.temp_path, self.file_processing.output_file_path)
        transformer.transformToJson()
