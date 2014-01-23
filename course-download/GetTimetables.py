#! python3
import urllib.request, os, time
from time import strftime

#Use this variable to handle empty courses:
coursenameWritten = False

def combine(semes, sec, limit, totalSection, cnwritten):
	#Sometimes there is a session with no activities. It's a uOttawa glitch.
	result = ""
	if len(totalSection) > 0 and len(sec) > 0:
		if not cnwritten:
			#We only want to add the course name if there is a valid activity
			result += courseName+",,,,,,,,\n"
		result += semes + "," + sec + limit + totalSection
	else:
		result = ""	
	return result

'''
Note the desired output:
Classname/Semester; Section+required DGD/LAB; type; num; day; start; end; place; professor
-------------------------------- ------------------ --- - - ---- ----- ------- -------------
ABC1234 - Introduction to Class.
							1234 ABC1234A 1DGD 1LAB LEC 1 M 8:30 22:00 ABC 111 Fakous Notous
								 					DGD 1 T 8:30 22:00 ABC 111 Fakous Notous
								 					LAB 1 W 8:30 22:00 ABC 111 Fakous Notous
							5678 ABC1234B 1DGD 1LAB LEC 1 R 8:30 22:00 ABC 111 Fakous Notous
								 					DGD 1 F 8:30 22:00 ABC 111 Fakous Notous
								 					LAB 1 S 8:30 22:00 ABC 111 Fakous Notous
'''

#Read in the course codes
courses = [line.strip() for line in open('course-list.txt')]

#Check if the file we're writing to exists... if it does, delete it.
if os.access('courseCodes.csv', os.F_OK):
	os.remove('courseCodes.csv')
#Re-create a blank file for writing to.
result = open('courseCodes.csv', 'w+')

total = len(courses)
current = 0

start_time = time.time()

invalidCourses = ["Invalid courses removed:"]


for course in courses:
	current+=1
	print("Course " + str(current) + "/" + str(total) + ": " + course)
	percent = 100*current//total
	bar = "["
	for i in range(0,percent):
		bar = bar + "-"
	for i in range(percent, 100):
			bar = bar + " "
	bar = bar + "]"
	print("\n    %" + str(percent) + ":  " +bar + "\n")
	elapsed_time = time.time() - start_time
	print("Time elapsed: " + str(int(elapsed_time)//60) + ":" + str(int(elapsed_time)%60).zfill(2))
	if percent >= 1:
		time_left = elapsed_time*(1.0/(current/float(total)))-elapsed_time
		print("Time left: " + str(int(time_left)//60) + ":" + str(int(time_left)%60).zfill(2))
	print(" ")
	retry = True
	while retry:
		try:
			#Download webpage
			webpage = str(urllib.request.urlopen("https://web30.uottawa.ca/v3/SITS/timetable/Course.aspx?code=" + course).read(), encoding='utf8')
			#Split at start of Schedule information.
			partitions = webpage.split('<!-- Schedule inforamtion -->')
			#In the first partition, find the second instance of the course
			#In front of it is the course name.
			courseName = partitions[0].split(course)[2]
			courseName = course + courseName.split("<", 1)[0].title()
			if "," in courseName:
				courseName = "\"" + courseName + "\""
			retry = False
		except IndexError:
			print("Error dealing with course " + course + ". Will retry download.")
	'''
	Now we have the course name in the format:
	ABC1234 - Introduction To Class. or "ABC1234 - Class, Introduction"
	'''
	coursenameWritten = False
	
	#Time to get each semester's information seperately
	partitions = partitions[1:]
	for semester in partitions:
		rows = semester.split("<td class=\"Section\">")
		sem = rows[0].split("id='", 2)[1]
		sem = sem.split("'", 1)[0]
		sem = sem.replace("\n", "")
		sem = sem.strip()

		'''
		Now we have the semester in the format:
		YYYYM
		'''
		first = True
		currentSection = ""
		firstSection = ""
		totalSection = ""
		limits = ""
		rows = rows[1:]
		for row in rows:
			#Now, each 'row' contains an activity, with some rows starting a new section.
			#Initially, we will always start with a section.
			section = row.split("<", 1)[0]
			section = section.replace(" ", "")
			'''
			Now we have the section in the format:
			ABC1234A or &nbsp;
			'''
			if section=="&nbsp;":
				section = ""
			elif not first:
				result.write(combine("", currentSection, limits, totalSection, coursenameWritten))
				currentSection = section
				if not coursenameWritten and len(totalSection) > 0:
					coursenameWritten = True
			elif currentSection == "":
				currentSection = section
			else:
				first = False
				result.write(combine(sem, currentSection, limits, totalSection, coursenameWritten))
				if not coursenameWritten and len(totalSection) > 0:
					coursenameWritten = True
				currentSection = section
				


			activity = (row.split("td class=\"Activity\">", 2)[1]).split("<", 2)[0].strip()
			activity = activity.split()
			type = activity[0]

			if type=="Lecture":
				type="LEC"
			elif type=="Seminar":
				type="SEM"
			elif type=="Discussion":
				type="DGD"
			elif type=="Tutorial":
				type="TUT"
			elif type=="Laboratory":
				type="LAB"
			elif type=="Videoconference":
				type="VID"
			elif type=="Work":
				type="WRK"
			elif type=="Research":
				type="RSH"
			elif type=="Theory":
				type="THR"
			elif type=="Audioconference":
				type="AUD"
			elif type=="Course" and len(activity) >= 4:
				if activity[3] == "Internet":
					type="WEB"
				elif activity[3]== "activities":
					type="WOC"
				else:
					print("Unknown activity found in: " + course)
			else:
				print("Unknown activity found in: " + course)
			#Now we have the activity, time to get the number
			num = activity[len(activity)-1]

			day = ""
			startTime = ""
			endTime = ""

			if type == "WEB":
				day = "N/A"
				startTime = "0"
				endTime = "0"
			else:
				schedule = row.split("td class=\"Day\">", 2)[1]
				schedule = schedule.split("<", 1)[0]
				schedule = schedule.split()
				day = schedule[0]
				if day=="Monday":
					day="M"
				elif day=="Tuesday":
					day="T"
				elif day=="Wednesday":
					day="W"
				elif day=="Thursday":
					day="R"
				elif day=="Friday":
					day="F"
				elif day=="Saturday":
					day="S"
				elif day=="Sunday":
					day="U"
				else:
					day="N/A"
				if day!="N/A":
					startTime = schedule[1]
					#schedule[2] is a dash
					endTime = schedule[3]
				else:
					startTime="0"
					endTime="0"
			'''
			Now we have type, number, day, start time, end time.
			'''
			place = row.split("span class='trigger'>")
			if len(place) < 2:
				place = row.split("class=\"Place\">", 2)
			place = place[1].split("<", 2)[0]
			professor = row.split("td class=\"Professor\">", 2)[1]
			professor = professor.split("<",1)[0].strip()
			if professor=="":
				professor="N/A"
			
			#Check for requirements
			limits = ""
			if "required" in row:
				limitations = row.split("required")
				for limit in limitations[:-1]:
					limit = limit.upper()
					limitNum=0
					if "ONE" in limit:
						limitNum=1
					elif "TWO" in limit:
						limitNum=2
					elif "THREE" in limit:
						limitNum=3
					if "DISCUSSION" in limit:
						limit="DGD"
					elif "LAB" in limit:
						limit="LAB"
					elif "TUT" in limit:
						limit="TUT"
					else:
						print("Unknown requirement: " + course + ": " + limit)
						limit = ""
						limitNum = ""
					limits = limits + " " + str(limitNum) + limit
				#now we have limits in a string.
			if len(section) > 0:
				totalSection = "," + type + "," + num + "," + day + "," + startTime + "," + endTime + "," + place + "," + professor + "\n"
			else:
				totalSection = totalSection + "," + section + "," + type + "," + num + "," + day + "," + startTime + "," + endTime + "," + place + "," + professor + "\n"
		if not first:
			sem=""
		result.write(combine(sem, currentSection, limits, totalSection, coursenameWritten))
		if not coursenameWritten and len(totalSection) > 0:
					coursenameWritten = True
		elif not coursenameWritten:
			try:
				courses.remove(course)
			except ValueError:
				print("Error removing " + courseName)
			invalidCourses.append(courseName)

for course in invalidCourses:
	print(course)
#We are now going to rewrite the course-list without the invalid courses.
#Check if the file we're writing to exists... if it does, delete it.
if os.access('course-list.txt', os.F_OK):
	os.remove('course-list.txt')
#Re-create a blank file for writing to.
course_list = open('course-list.txt', 'w+')
course_list.write(courses[0])
for course in courses[1:]:
	course_list.write("\n" + course)

result.close()