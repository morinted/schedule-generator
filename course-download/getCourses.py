import urllib2, re, time
from selenium import webdriver

#Search page - Timetable uOttawa
baseurl="https://web30.uottawa.ca/v3/SITS/timetable/Search.aspx"

#Open page in Firefox
browser = webdriver.Firefox()
browser.get(baseurl)

#Click search button
searchButton = browser.find_element_by_id('ctl00_MainContentPlaceHolder_Basic_Button')
searchButton.click()
codes = []

#Continue flag
cont=True

#For each page in "Next"
while cont:
	try:
		html = browser.page_source
		currentCodes = re.findall('(?<=">)[a-zA-Z]{3}\d{4}[a-zA-Z]?(?=</a>)', html)
		while len(currentCodes) < 2:
			time.sleep(2)
			html = browser.page_source
			currentCodes = re.findall('(?<=">)[a-zA-Z]{3}\d{4}[a-zA-Z]?(?=</a>)', html)
		
		codes += currentCodes
		print currentCodes
		next = browser.find_element_by_xpath('/html/body/form/div[3]/div[2]/div/div[3]/div[2]/span[2]/a')
		next.click()
	except:
		print 'Exception: Possible no next button. Check browser.'
		cont = False

#Print raw count of courses
print 'Found ' + str(len(codes)) + ' courses.'

#Remove duplicates with set, then sort.
print 'Removing duplicates and sorting alphabetically...'
codes = list(set(codes))
codes.sort()

#Print net/unique course count
print 'Done! Now there are ' + str(len(codes)) + ' courses.'

#Convert courses into one string for a text file
courses_string = ''
for code in codes:
	courses_string+=code + '\n'

#Write courses to courses.txt
course_list = open("courses.txt", "w")
course_list.write(courses_string)
course_list.close()
print 'Courses printed to courses.txt'