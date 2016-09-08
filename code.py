from bs4 import BeautifulSoup
import requests

r = requests.get("http://www.amazon.in/Lenovo-Vibe-Note-Black-16GB/product-reviews/B01A11D2U2/")

data = r.text
soup = BeautifulSoup(data,"html.parser")
divs = soup.findAll("div",{"class":"a-row review-data"})

for div in divs:
	print(div.text)
	print()
