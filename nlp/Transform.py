import json
class Transform:
    def __init__(self, src_path, dst_path):
        # 源文件/临时文件路径
        self.src_path = src_path
        # 目的文件路径
        self.dst_path = dst_path
        # 每行的内容
        self.line_list = self.getLineList()
        # 每页的行数
        self.page_line_list = self.getPageLines()

    def getLineList(self):
        file = open(self.src_path, "r", encoding="utf-8")
        lineList = file.readlines()
        return lineList

    def getPageLines(self):
        lines = []
        label = 0
        thisLines = 0
        for line in self.line_list:
            if line[0] == '*':
                if label == 1:
                    label = 0
                else:
                    label = 1
            else:
                thisLines += 1
            if label == 0:
                lines.append(thisLines)
                thisLines = 0
        return lines

    def getLineContent(self,n):
        theSum = 0
        for line in self.line_list:
            if line[0] == '*':
                continue
            else:
                theSum += 1
            if theSum == n:
                return line.strip('\n')

    def transformToJson(self):
        plus = 0
        jsontext = {'words': []}
        for i in range(len(self.page_line_list)):
            NList = []
            # for k in range(pageLines[i]):
            k = 0
            while k < self.page_line_list[i]:
                plus += 1
                k += 1
                print(plus)
                print(type(self.getLineContent(plus)))
                theList = eval(self.getLineContent(plus))
                nextList = eval(self.getLineContent(plus + 1))
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
        f = open(self.dst_path, 'w',encoding='utf-8')
        f.write(jsondata)
        f.close()
