from http.server import BaseHTTPRequestHandler
import io
import logging
import testMain

ABSOLUTE_URL = "/root/HelloSlide/"
OUTPUT_DEBUG = False


class PostHandler(BaseHTTPRequestHandler):

    def do_POST(self):

        # 分析提交的数据(文件长度、文件名称)
        add = self.address_string()
        logging.debug("IP:" + add)

        cin = self.rfile
        message = self.headers
        fileName = ""
        if 'File-Name' in message:
            fileName = message['File-Name']
        flen = 0
        if 'Content-Length' in message:
            flen = int(message['Content-Length'])

        logging.debug("fileName:" + fileName)
        logging.debug("flen:" + str(flen))

        originFileURL = ABSOLUTE_URL + "origin/" + add + "_*_" + fileName
        resultFileURL = ABSOLUTE_URL + "result/" + add + "_*_" + fileName + "_*_" + ".json"
        if OUTPUT_DEBUG:
            resultFileURL = ABSOLUTE_URL + "result/default.json"

        # 获取传输文件
        fi = open(originFileURL, "wb+")
        while flen != 0:
            tmp = cin.read(min(256, flen))
            fi.write(tmp)
            flen -= len(tmp)
        cin.close()
        fi.close()

        # 中间处理，调用nlp模块
        NLP(originFileURL, resultFileURL)

        # 获取结果文件并传输结果
        self.send_response(200)
        self.end_headers()

        fo = open(resultFileURL, "rb")
        cout = self.wfile
        cout.write(fo.read())
        fo.close()


def NLP(originFileURL, resultFileURL):
    theMain = testMain.Main(originFileURL, resultFileURL)
    theMain.writeAndTransform()
    logging.debug("NLP done...")


if __name__ == '__main__':
    logging.basicConfig(level=logging.DEBUG)
    from http.server import HTTPServer

    server = HTTPServer(('', 2202), PostHandler)
    logging.debug("------Starting server------")
    server.serve_forever()
