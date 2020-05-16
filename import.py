import os
import requests
import json

directory = r'D:\\Movies\\'

def getFiles(dir):
    if(os.path.isdir(dir)):
        for file in os.listdir(dir):
            if(os.path.isdir(dir + file)):
                newDir = dir + file + r'\\'
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
input["userId"] = 0
input["movies"] = result
print({k: str(v).encode('utf-8') for k, v in input.items()})

URL = "http://localhost:5000/movie/add"
r = requests.post(URL, json=input)
#print(json.dumps(r.json(), indent=2))
