import os
import requests
import json

directory = r'D:\\Movies\\'
#directory = '/Volumes/Data/Movies/'

def getFiles(dir):
    if(os.path.isdir(dir)):
        for file in os.listdir(dir):
            if(os.path.isdir(dir + file)):
                newDir = dir + file + r'\\'
                #newDir = dir + file + '/'
                getFiles(newDir)
            elif(os.path.isfile(dir + file) and not file.startswith('.')):
                if(file.find("(") == -1):
                    pass
                else:
                    temp = {}
                    temp["title"] = file[:file.index("(")-1]
                    temp["year"] = file[file.index("(")+1:file.index(")")]
                    result.append(temp)
                    if temp["title"] == "Grandma's boy":
                        print(temp['year'])
    return

result = []
getFiles(directory)
print(len(result))

input = {}
input["movies"] = result
#print({k: str(v).encode('utf-8') for k, v in input.items()})

URL = "http://localhost:5000/1/add"
r = requests.post(URL, json=input)
print(json.dumps(r.json(), indent=2))
