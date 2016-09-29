#imports
from bs4 import BeautifulSoup
import requests

#open file for output printing
f=open("urlfile.txt","w+")
url=[]
allurl=[]

url.append("http://localhost/demo.php?hidden=1&url=aHR0cHM6Ly9hZmZpbGlhdGUtYXBpLmZsaXBrYXJ0Lm5ldC9hZmZpbGlhdGUvZmVlZHMvc2h1Ymh6MTIzL2NhdGVnb3J5L3R5eS00aW8uanNvbj9leHBpcmVzQXQ9MTQ3NDgzOTI3NDMxOCZzaWc9Y2M0OTBlNWJiYmY5ZjkyMjI2ZGUyNWQ1M2FiNzFiNDA=")

#url is taken from flipkart api

itt=0;
flag=0;
while(itt<len(url)):
	if(flag==1):
		break
	else:
		r  = requests.get(url[itt])
		data = r.text
#soup object as per url created
		soup = BeautifulSoup(data,"html.parser")
		i=0
		for link in soup.find_all('a'):
			allurl.append(link.get('href'))
#finding links from url we requested
			if i==1:
				temp=link.get('href')
				#print(temp[1:]+"\n")
				temp2=temp[1:];
				if(""+temp2=="url="):
					flag=1;
					break
				url.append("http://localhost/demo.php?hidden=1&"+temp[1:])
			i=i+1
		itt=itt+1
#writing to files
for link in allurl:
	f.write(link+"\n")
