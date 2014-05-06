#!/usr/bin/python

import Queue, threading, time, urllib2, re, datetime, multiprocessing, traceback

#Used to stop threads
exitFlag = 0

class myThread (threading.Thread):
    def __init__(self, threadID, name, q):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.q = q
    def run(self):
        print "Starting " + self.name
        process_data(self.name, self.q)
        print "Exiting " + self.name

def process_data(threadName, q):
    t_count = 0
    while not exitFlag:
        queueLock.acquire()
        if not workQueue.empty():
            #Get course from queue
            course = q.get()
            queueLock.release()

            #Process course
            #Add to global count
            thread_totals[0]+=1
            #Add to local count
            t_count+=1

            #Print progress
            print '[' + threadName + '][Thread count: ' + str(t_count) + '][Total: ' + str(thread_totals[0]) + '/' + str(thread_totals[1]) + '] ' + course
            
            #This loop will continue until the page is downloaded
            retry = True
            while retry:
                try:
                    #Download webpage
                    site = urllib2.urlopen("https://web30.uottawa.ca/v3/SITS/timetable/Course.aspx?code=" + course)
                    webpage = site.read()

                    #Get course description
                    description = re.search('(?<=<h2>' + course + ' - )[^<]*(?=</h2>)', webpage).group(0)
                    if "," in description:
                        description = '"' + description + '"'
                    
                    #Get all semesters
                    semesters = re.split('Course schedule for the session:', webpage, flags=re.MULTILINE)
                    semesters=semesters[1:]
                    totalSections = []
                    for semester in semesters:
                        #Get semester integer
                        sem = re.search("(?<=<div id=')\d+(?=' class='schedule'>)", semester, flags=re.MULTILINE).group(0)
                        
                        #Get all sections for semester
                        sections = re.split('<td class="Section">' + course, semester)
                        sections = sections[1:]
                        totalSections += sections
                        for section in sections:
                            sec = re.search('[^<]*(?=<br/>)', section).group(0)
                            sec = sec.strip()
                            
                            #Any required DGDs?
                            requiredDGD = 'Only one discussion group' in section
                            if requiredDGD:
                                requiredDGD = 1
                            else:
                                requiredDGD = 0
                            requiredLAB = 'Only one laboratory' in section
                            if requiredLAB:
                                requiredLAB = 1
                            else:
                                requiredLAB = 0
                            requiredTUT = 'Only one tutorial' in section
                            if requiredTUT:
                                requiredTUT = 1
                            else:
                                requiredTUT = 0

                            activities = re.split('<td class="Activity">', section)
                            activities = activities[1:]
                            #For each activity...
                            for activity in activities:
                                #Get the activity type (Lecture, Lab, etc.)
                                activity_name = re.search('^[^<]+(?=</td>)', activity).group(0)
                                activity_name = re.search('[a-zA-Z ]+', activity_name).group(0)
                                activity_name = activity_name.strip()
                                
                                #Lecture '1', '2', etc.
                                activity_num = re.search('\d+', activity).group(0)
                                activity_time = re.search('(?<=<td class="Day">)[^<]*(?=</td>)', activity).group(0)
                                
                                #Sunday-Saturday
                                activity_day_time_dash_time = re.split(' ', activity_time)
                                activity_day = activity_day_time_dash_time[0]
                                
                                #08:30:00-22:00:00
                                activity_strt_time = activity_day_time_dash_time[1]
                                activity_end_time = activity_day_time_dash_time[3]

                                #eg: SMD 224
                                activity_place = re.search('((?<=<td class="Place">)[^<]*(?=</td>)|(?<=<td class="Place"><div class=\'bubble\'><span class=\'trigger\'>)[^<]*(?=</span>))', activity).group(0)
                                
                                #John Smith
                                activity_prof = re.search('((?<=<td class="Professor">)[^<]*(?=</td>)|(?<=<td class="Professor"><div class=\'bubble\'><span class=\'trigger\'>)[^<]*(?=</span>))', activity).group(0)
                                
                                #Add activity to list
                                db_activities.append(activity_name + ',' + activity_num + ',' + course + ' ' + sec + ',' + sem + ',' + activity_day + ',' + activity_strt_time + ',' + activity_end_time + ',' + activity_place + ',' + activity_prof)
                            if len(activities) > 0:
                                #If there was at least one activity,
                                #add the section to thhe sections list.
                                db_sections.append(course + ' ' + sec + ',' + course + ',' + sem + ',' + str(requiredDGD) + ',' + str(requiredTUT) + ',' + str(requiredLAB))
                        
                    #If there was at least one section,
                    #add the course to the courses list.
                    if 0 < len(totalSections):
                        db_courses.append(course + ',' + description)
                    else:
                        #otherwise add it to the skipped list so
                        #that the user can double-check it.
                        skipped_courses.append(course)
                    #break out of the loop
                    retry = False
                except IndexError:
                    #usually caused by invalidly downloaded HTML
                    print("Error dealing with course " + course + ". Will retry download.")
                except AttributeError:
		    traceback.print_exc()
                    print("Error reading course " + course + ". Retrying...")
                except urllib2.HTTPError:
                    #Server error
                    print("Internal server error. Waiting 2 seconds and trying again.")
                    time.sleep(2)
                except urllib2.URLError:
                    #No internet connection
                    print("Internet problem. Waiting 5 seconds before retry...")
                    time.sleep(5)
        else:
            queueLock.release()
    #Give thread totals to global variable
    thread_totals.append(threadName + ' completed ' + str(t_count) + ' courses.')


'''
Main Thread
'''

#Format for the time
time_format = "%H:%M:%S"
#Get the start time
start_time = time.strftime(time_format)


#List of threads.
threadList = []
for x in range(0,multiprocessing.cpu_count()):
    threadList.append('T' + str(x+1))
#A lock for the queue
queueLock = threading.Lock()
#No limit on the workQueue length
workQueue = Queue.Queue(0)

threads = []
threadID = 1

#For holding the database info
db_courses = []
db_sections = []
db_activities = []

#Read in the courses from courses.txt
course_list = open("courses.txt", "r")
courses = course_list.read()
course_list.close()
courses = courses.split() 

#Add in current and total couses to list
thread_totals = [0,len(courses)]

#For holding skipped courses
skipped_courses = []

# Create new threads
for tName in threadList:
    thread = myThread(threadID, tName, workQueue)
    thread.start()
    threads.append(thread)
    threadID += 1

# Fill the queue with courses
queueLock.acquire()
for course in courses:
    workQueue.put(course)
queueLock.release()

# Wait for queue to empty
while not workQueue.empty():
    time.sleep(.2) 
    pass

# Notify threads it's time to exit
exitFlag = 1

# Wait for all threads to complete
for t in threads:
    t.join()

#Give statistics
print('Done dealing with courses.')

thread_totals.sort()

print

#Totals per thread
for total in thread_totals[2:]:
    print total

print

#Announce skipped courses
if len(skipped_courses) > 0:
    print 'These courses were skipped:'
    print skipped_courses
else:
    print 'No courses skipped.'
print

#Print total count of all items
print'Courses:', str(len(db_courses))
print'Sections:', str(len(db_sections))
print'Activities:', str(len(db_activities))

#Write courses to files
course_csv = open("db_courses.csv", "w")
courses_string = ''
for line in db_courses:
    courses_string+=line + '\n'
course_csv.write(courses_string)
course_csv.close()

#Write sections to files
section_csv = open("db_sections.csv", "w")
sections_string = ''
for line in db_sections:
    sections_string+=line + '\n'
section_csv.write(sections_string)
section_csv.close()

#Write activities to files
activity_csv = open("db_activities.csv", "w")
activities_string = ''
for line in db_activities:
    activities_string+=line + '\n'
activity_csv.write(activities_string)
activity_csv.close()

#Calculate script time and print it
end_time = time.strftime(time_format)
tdelta = datetime.datetime.strptime(end_time, time_format) - datetime.datetime.strptime(start_time, time_format)
print '\nTime elapsed: ' + str(tdelta)