from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
import time

from bs4 import BeautifulSoup
f = open("file.txt", "wb")
driver = webdriver.Chrome()
timeout = 10
driver.get("https://www.flipkart.com/apple-iphone-6s-plus-gold-64-gb/product-reviews/itmebysghzuqtrge?pid=MOBEBY3WHRGP4BU7")
assert "Flipkart" in driver.title
T=True

while T:
	temp=0
	for rm in driver.find_elements_by_class_name('_1EPkIx'):
		rm.click()
		temp=temp+1
		print (temp)
	
	html_source = driver.page_source

	soup = BeautifulSoup(html_source,"html.parser")
	for div in soup.find_all('div',{'class':'qwjRop'}):
		f.write(div.text.encode("utf-8"))
		f.write("\n".encode("utf-8"))	
	try:
		n= driver.find_element_by_link_text("NEXT").click()
		print (driver.current_url)
		element_present = EC.presence_of_element_located((By.ID, 'element_id'))
		WebDriverWait(driver, timeout).until(element_present)
			
	except NoSuchElementException as e:
		T=False
	except TimeoutException:
	    print ("Timed out waiting for page to load")

print("done...")
