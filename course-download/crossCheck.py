#!/usr/local/bin/python3.7

# Program to validate scraped timetables against the uschedule.me API
# If we see too many differences, it is likely that one of the
# implementations is failing and this should be investigated.

import csv
import requests
import json
from time import sleep
import sys

# the 25 most commonly added courses from April 2018 – April 2020
topCourses = ["CEG2136", "CSI2110", "SEG2105", "ENG1112", "MAT1320", "MAT1322", "CHM1311", "MAT1341", "ITI1120", "ECO1104", "MAT2377", "PHI1101", "ECO1102", "ENG1100", "ADM1340", "MAT1348", "ITI1121", "ITI1100", "ECO1504", "PSY1102", "ECO1502", "CSI2132", "PSY1101", "MAT1300", "CSI2101"]

# the activities for the topCourses
activities = []
with open('db_activities.csv') as a_csv:
	a_reader = csv.reader(a_csv, delimiter=',')
	for row in a_reader:
		for course in topCourses:
			if row[2].startswith(course):
				activities.append(row)

# get the active semesters
semesters = []
for a in activities:
	semesters.append(a[3])

semesters=list(set(semesters))
formatted_semesters = []
a = ""
for s in semesters:
	if s[4:5] is "1":
		a = "winter"
	elif s[4:5] is "9":
		a = "fall"
	elif s[4:5] is "5":
		a = "summer"
	formatted_semesters.append( [ s[0:4], a ])

# independent schedules against which to validate
independent = []
for course in topCourses:
	for s in formatted_semesters:
		try:
			r = requests.get("https://uschedule.me/api/v1/schedule/course/?school=uottawa&year=" + s[0] + "&term=" + s[1] + "&subject_code=" + course[0:3] + "&course_code=" + course[3:7])
			independent.append(json.loads(r.content))
		except:
			continue
		sleep(5)

good = 0
bad = 0

# go through checking that each section is the same
for i_course in independent:
	if i_course["data"] is None: # if the course is not offered in the given semester
		continue # but we should first check this in our database too
	i_coursecode = i_course["data"]["subject_code"] + i_course["data"]["course_code"]
	for i_s in i_course["data"]["sections"].items():
		i_section = i_s[0]
		for i_c in i_s[1]["components"].items():
			# check if this activity is in our database
			was_good = False
			for a in activities:
				if a[2].startswith(i_coursecode + i_section) and a[5].startswith(i_c[1]["start_time"]): # need to check activity type as well, and day of week. don't check end time since usm doesn't use the actual end time
					good +=1
					was_good = True
			if was_good is False:
				print("Couldn't find " + i_coursecode + i_c[0] + ", " + i_c[1]["day"] + " " + i_c[1]["start_time"])
				bad  +=1

print("good: " + str(good))
print("bad: " + str(bad))
if bad > 0:
	sys.exit(1)

