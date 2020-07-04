# NLP for HelloSlide (Server Part)
## Server Module @sandyhu533
### use python to serve as a HTTP server. find it all in 'Server.py'.
### server ip:121.41.224.199; port: 2202
### receive a file from client :  
1. Format: No limit
2. FileName: client ip + "_*_" + origin file name
3. FileLocation: absolute location / origin /
### send a file to client : 
1. Format: Json
2. FileName: client ip + "_*_" + origin file name + "_*_" + ".json"
3. FileLocation: absolute location / result /
### how to use?
1. set ABSOLUTE_URL to config the absolute location of the resouce repo
2. implement 
> def NLP(originFileURL, resultFileURL):
    logging.debug("here is the nlp process")
3. set OUTPUT_DEBUG to False to get result
## NLP Module

