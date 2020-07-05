import pandas as pd
import os, requests, json, datetime


location = 'C:\\Users\\Amish Cyborg\\Downloads\\ml-25m\\ml-25m\\links.csv'



def getFiles(dir):
    if(os.path.isdir(dir)):
        return pd.read_csv(location)
def zeros(id):
    return 'tt' + str(id).zfill(8)

ids = list(map(zeros, pd.read_csv(location)['imdbId'].tolist()))
print(ids)
for id in ids:
    if len(id) != 10:
        print("fail")

input = {}
input["ids"] = ids

URL = "http://localhost:5000/0/add/id"
start = datetime.datetime.now()
r = requests.post(URL, json=input)
end = datetime.datetime.now()
print("Time to add:", str(end-start))
print(json.dumps(r.json(), indent=2))