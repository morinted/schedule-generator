#!/usr/bin/env python

from __future__ import print_function
import argparse
import re
from time import sleep
import string
import sys
import datetime
import os

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By

base_url = "https://uocampus.public.uottawa.ca/psc/csprpr9pub/EMPLOYEE/HRMS/c/UO_SR_AA_MODS.UO_PUB_CLSSRCH.GBL?languageCd=ENG&PortalActualURL=https%3a%2f%2fuocampus.public.uottawa.ca%2fpsc%2fcsprpr9pub%2fEMPLOYEE%2fHRMS%2fc%2fUO_SR_AA_MODS.UO_PUB_CLSSRCH.GBL%3flanguageCd%3dENG&PortalContentURL=https%3a%2f%2fuocampus.public.uottawa.ca%2fpsc%2fcsprpr9pub%2fEMPLOYEE%2fHRMS%2fc%2fUO_SR_AA_MODS.UO_PUB_CLSSRCH.GBL&PortalContentProvider=HRMS&PortalCRefLabel=Public%20Class%20Search&PortalRegistryName=EMPLOYEE&PortalServletURI=https%3a%2f%2fuocampus.public.uottawa.ca%2fpsp%2fcsprpr9pub%2f&PortalURI=https%3a%2f%2fuocampus.public.uottawa.ca%2fpsc%2fcsprpr9pub%2f&PortalHostNode=HRMS&NoCrumbs=yes&PortalKeyStruct=yes"


def getSemesters(b, semesterSelectId):
    b.get(base_url)
    WebDriverWait(b, 10).until(EC.presence_of_element_located((By.ID, semesterSelectId)))
    
    semesters = []
    semSelect = b.find_element_by_xpath("//select[@id='"+semesterSelectId+"']")
    all_options = semSelect.find_elements_by_tag_name("option")
    for option in all_options:
        try:
           semester = int(option.get_attribute("value"))
           if args.verbose: print("Adding semester: " + str(semester))
           semesters.append(option.text)
        except ValueError:
           if args.verbose: print("Option not a valid semester: "+str(option.get_attribute("value")))
    return semesters


def getSubjects(b):
    b.get(base_url)
    
    # Get list of course codes
    codes = []
    WebDriverWait(b, 10).until(EC.presence_of_element_located((By.ID, "CLASS_SRCH_WRK2_SSR_PB_SUBJ_SRCH$0")))
    b.find_element_by_id('CLASS_SRCH_WRK2_SSR_PB_SUBJ_SRCH$0').click()
    for index in string.ascii_uppercase:
        sleep(1)
        b.find_element_by_id('SSR_CLSRCH_WRK2_SSR_ALPHANUM_' + index).click()
        html = b.page_source
        current_codes = re.findall('<span class="PSEDITBOX_DISPONLY" id="SSR_CLSRCH_SUBJ_SUBJECT\$\d">([A-Z][A-Z][A-Z])</span>', html)
        #print(current_codes)
        codes += [''.join(t) for t in current_codes]
    
    codes = list(set(codes)) # remove duplicates
    return codes
    
def convertActivityCode(code):
    # Full text types from TextParser.java in ScheduleGeneratorServer,
    # abbreviations as seen in the wild
    if "LEC" in code: return "Lecture"
    if "SEM" in code: return "Seminar"
    if "DGD" in code: return "Discussion Group"
    if "TUT" in code: return "Tutorial"
    if "LAB" in code: return "Laboratory"
    if "VID" in code: return "Videoconference"
    if "WRK" in code: return "Work"
    if "STG" in code: return "Work"
    if "RSH" in code: return "Research"
    if "REC" in code: return "Research"
    if "TLB" in code: return "Research"
    if "THR" in code: return "Theory"
    if "AUD" in code: return "Audioconference"
    if "WEB" in code: return "Course Internet"
    if "WOC" in code: return "Course activities"

    return code


def writeFiles(codes, activities, sections, complete):
    if complete is False:
        with open('warning.txt', 'wb') as f:
            f.writelines("Warning: updateDatabase.py exited unexpectedly on "+str(datetime.datetime.now())+". Database files are probably incomplete!")
    else:
        try:
            os.remove('warning.txt')
        except:
            pass
    
    # Change hh:20 to hh:30, hh:50 to (hh+1):00
    # (Somewhat misleading compared to the real schedule times,
    # but gives correctly-sized blocks in all clients without
    # requiring any clients to update)
    activities = [a.replace(':20,', ':30,') for a in activities]
    activities = [a.replace('09:50,', '10:00,') for a in activities]
    activities = [a.replace('12:50,', '13:00,') for a in activities]
    activities = [a.replace('15:50,', '16:00,') for a in activities]
    activities = [a.replace('18:50,', '19:00,') for a in activities]
    activities = [a.replace('21:50,', '22:00,') for a in activities]
    
    with open('db_courses.csv', 'wb') as f:
        f.writelines((u'{0}\n'.format(c).encode('utf8') for c in sorted( list(set(codes)) ) )) # (remove any duplicates too)
    with open('db_activities.csv', 'wb') as f:
        f.writelines((u'{0}\n'.format(c).encode('utf8') for c in sorted( list(set(activities)) ) ))
    with open('db_sections.csv', 'wb') as f:
        f.writelines((u'{0}\n'.format(c).encode('utf8') for c in sorted( list(set(sections)) ) ))


# https://stackoverflow.com/a/41918201/3380815
def element_in_parent(parent, selector):
    matches = parent.find_elements_by_css_selector(selector)
    if len(matches) == 0:
        return False
    else:
        return matches

def any_element_in_parent(parent, *selectors):
    for selector in selectors:
        matches = element_in_parent(parent, selector)
        # if there is a match, return right away
        if matches:
            return matches
    # If list was exhausted
    return False


def main():
    # Open in Chrome
    options = webdriver.ChromeOptions()
    # Note: it may be necessary to specify the path to the chromedriver file, e.g. 
    # options.binary_location = "/home/ubuntu/Downloads/chromedriver"
    options.add_argument('headless')
    b = webdriver.Chrome(chrome_options=options)
    b.set_page_load_timeout(30)
    
    semesterSelectId = 'CLASS_SRCH_WRK2_STRM$35$'
    semesters = getSemesters(b, semesterSelectId)
    if args.verbose: print(semesters)
    
    # Get list of course codes
    subjects = getSubjects(b)
    #subjects = [u'PAP', u'PAT', u'LIN', u'NAP', u'PAC', u'PAE', u'NAT', u'DRC', u'HBR', u'GLO', u'DCL', u'JOU', u'YDD', u'ILA', u'DCC', u'DCA', u'HSR', u'HSS', u'HST', u'LSR', u'BNF', u'HAH', u'CHE', u'CHG', u'URO', u'CHM', u'CHN', u'THO', u'THM', u'LPI', u'THE', u'THD', u'GAE', u'IPA', u'MAT', u'XYZ', u'XNC', u'AHL', u'ACP', u'KIN', u'MAG', u'LAT', u'ORT', u'MAN', u'XSP', u'SDS', u'FLP', u'FLS', u'DEV', u'PED', u'EED', u'CEG', u'GER', u'LSB', u'BMG', u'ORO', u'LDR', u'GEG', u'LLM', u'HMG', u'GEO', u'GEN', u'SCS', u'OVG', u'SEW', u'GRT', u'IGR', u'TRA', u'OMT', u'RAD', u'TRF', u'DLS', u'GRE', u'EFL', u'IGL', u'PBH', u'OPT', u'INR', u'HTP', u'NOT', u'LCL', u'ALQ', u'OPH', u'DVM', u'BCM', u'BCH', u'DOM', u'BCG', u'PEP', u'RIS', u'ISI', u'FSS', u'REL', u'ISC', u'REC', u'REA', u'TMM', u'CGI', u'ECO', u'NUT', u'TEC', u'MDG', u'ECH', u'SEG', u'ECS', u'VRE', u'PCS', u'MDV', u'ADW', u'FRA', u'ADR', u'FRE', u'RDR', u'OBG', u'FEM', u'IMM', u'CLA', u'ADX', u'PDP', u'ADC', u'SPA', u'FMD', u'ADM', u'MED', u'EDD', u'ALG', u'CDN', u'GES', u'MET', u'ORL', u'EDU', u'RUS', u'RLP', u'GNG', u'TOX', u'CAR', u'CIN', u'BPS', u'AIN', u'RCH', u'BAC', u'BIO', u'BIL', u'BIM', u'NSC', u'MBA', u'NSG', u'HIA', u'RPD', u'FAM', u'EAS', u'EAU', u'DHN', u'XOU', u'JCS', u'SLV', u'SAI', u'SOC', u'LPR', u'IRI', u'HOT', u'HIS', u'ICS', u'SCI', u'SDT', u'LCM', u'EBC', u'MCG', u'PCT', u'JPN', u'ORA', u'MCU', u'TCH']
    if args.verbose: print(subjects)
    
    # List of 'year of study' checkbox ids
    yearsOfStudy = ['UO_PUB_SRCH_WRK_SSR_RPTCK_OPT_01$0', 'UO_PUB_SRCH_WRK_SSR_RPTCK_OPT_02$0', 'UO_PUB_SRCH_WRK_SSR_RPTCK_OPT_03$0', 'UO_PUB_SRCH_WRK_SSR_RPTCK_OPT_04$0', 'UO_PUB_SRCH_WRK_GRADUATED_TBL_CD$0']
    
    # Lists for the CSV files
    codes = []
    activities = []
    sections = []
    
    # Crazy nested loop to iterate over the semester, the subject, and the year of study
    # (the latter to avoid the item limit on searches)
    for semester in semesters[::-1]:
        if "Winter" in semester : semesterAddOn = "1"
        if "Summer" in semester : semesterAddOn = "5"
        if "Fall"   in semester : semesterAddOn = "9"
        semesterCode = semester[:4] + semesterAddOn
        for subject in subjects:
            for year in yearsOfStudy:
                if args.verbose: print("Finding courses and sections for: "+semester+", "+subject+", year "+year[31:32])
                timeoutCounter = 0
                while True:
                    try:
                        b.get(base_url)
                        break
                    except TimeoutException as e:
                        print("Timeout: couldn't load main search URL. This is attempt number "+str(timeoutCounter)+".")
                        if timeoutCounter > 10 :
                            print("Timeout: couldn't load base_url after four retries")
                            writeFiles(codes, activities, sections, False)
                            exit(1)
                        timeoutCounter += 1
                        continue
                try:
                    WebDriverWait(b, 10).until(EC.presence_of_element_located((By.ID, "CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH")))
                except TimeoutException as e:
                    print("Timeout: couldn't find the search button")
                    continue
                Select(b.find_element_by_xpath("//select[@id='"+semesterSelectId+"']")).select_by_visible_text(semester)
                sleep(0.2)
                b.find_element_by_id('SSR_CLSRCH_WRK_SUBJECT$0').send_keys(subject)
                sleep(0.2)
                b.find_element_by_id(year).click()
                try: # sometimes the submit button doesn't work the first time, just try twice by default
                    sleep(0.2)
                    b.find_element_by_id('CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH').click()
                    sleep(0.2)
                    b.find_element_by_id('CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH').click()
                except:
                    pass
                sleep(0.2)
                try:  # Wait up to 20 seconds for either results or an error
                    cond = lambda args: any_element_in_parent(*args)
                    WebDriverWait((b, "#DERIVED_CLSMSG_ERROR_TEXT", "#win0divSSR_CLSRSLT_WRK_GROUPBOX1"), 20).until(cond)
                except TimeoutException as e:
                        print("Timeout: no results returned for course search")
                        continue
                # If there are no results, go the the next iteration
                try:
                    errorMessage = b.find_element_by_id('DERIVED_CLSMSG_ERROR_TEXT').text
                    if "No classes found" in errorMessage:
                        if args.verbose: print("No classes found")
                        continue
                except:
                    if args.verbose: print("No error message, continuing")
                i = 0
                while True:  # If there are results, iterate through each listed course until none are left
                    try:
                        courseCodeAndNameAnchor = b.find_element_by_id("win0divSSR_CLSRSLT_WRK_GROUPBOX2GP$"+str(i))
                    except:
                        if args.verbose: print("Done with this page")
                        break
                    courseCodeAndName = courseCodeAndNameAnchor.text.strip().split(' - ')
                    courseCode = courseCodeAndName[0]
                    courseCodeAndName.pop(0)
                    courseName = "".join(courseCodeAndName)
                    
                    sectionNumbers = []
                    activityTypes = []
                    
                    courseTable = courseCodeAndNameAnchor.find_element_by_xpath('../../../..')
                    activitiesTables = courseTable.find_elements_by_class_name('PSLEVEL1GRIDNBONBO')
                    
                    if args.verbose: print(courseCode, courseName)
                    
                    for activity in activitiesTables:
                        activityRow = activity.find_element_by_xpath(".//tr[@valign='center']")
                        sectionField = activityRow.find_element_by_css_selector("[id^=MTG_CLASSNAME]").text
                        sectionNumberRegex = re.search('^([A-Z]+)([0-9]+)-([A-Z][A-Z][A-Z])', sectionField)
                        if not sectionNumberRegex : continue
                        if not len(sectionNumberRegex.groups()) is 3 : continue
                        sectionNumber = sectionNumberRegex.group(1)
                        sectionNumbers.append(sectionNumber)
                        activityNumber = sectionNumberRegex.group(2)
                        activityType = sectionNumberRegex.group(3)
                        dayTimeRows = activityRow.find_element_by_css_selector("[id^=MTG_DAYTIME]").text.split('\n')
                        locationRows = activityRow.find_element_by_css_selector("[id^=MTG_ROOM]").text.split('\n')
                        professorRows = activityRow.find_element_by_css_selector("[id^=MTG_INSTR]").text.split('\n')
                        if len(dayTimeRows) != len(locationRows) != len(professorRows) : continue
                        j = 0
                        for day in dayTimeRows:
                            if "Mo " in day : fullDay = "Monday"
                            if "Tu " in day : fullDay = "Tuesday"
                            if "We " in day : fullDay = "Wednesday"
                            if "Th " in day : fullDay = "Thursday"
                            if "Fr " in day : fullDay = "Friday"
                            if "Sa " in day : fullDay = "Saturday"
                            if "Su " in day : fullDay = "Sunday"
                            timeRegex = ur"\d+:\d+"
                            times = re.findall(timeRegex, day)
                            if len(times) is not 2 : continue
                            if times[0] == times[1] : continue # an activity with a zero duration
                            activityTypes.append(activityType)
                            
                            if args.verbose: print("  ", sectionNumber, activityType, str(activityNumber), fullDay, times[0], times[1])
                            activities.append(
                                convertActivityCode(activityType) + "," +
                                str(activityNumber)  + "," +
                                courseCode.replace(" ", "") + sectionNumber + "," +
                                semesterCode + "," +
                                fullDay + "," +
                                times[0] + "," +
                                times[1] + "," +
                                locationRows[j].replace(",", "") + "," +
                                professorRows[j].replace(",", "")
                                )
                            j += 1
                    
                    if len(activityTypes) == 0 : # if no activities, don't add the course
                        if args.verbose: print("  ", "Skipping, no activities")
                        i+=1
                        continue
                    hasDGD = "0"
                    hasLAB = "0"
                    hasTUT = "0"
                    if "DGD" in activityTypes : hasDGD = "1"
                    if "LAB" in activityTypes : hasLAB = "1"
                    if "TUT" in activityTypes : hasTUT = "1"
                    for sectionNumber in list(set(sectionNumbers)):
                        sections.append(
                            courseCode.replace(" ", "") + sectionNumber + "," +
                            courseCode.replace(" ", "") + "," +
                            semesterCode + "," +
                            hasDGD + "," +
                            hasTUT + "," +
                            hasLAB
                            )

                    # Append to CSV-destined lists
                    codes.append(courseCode.replace(" ", "")+","+courseName.replace(",", ""))
                    i+=1
                
    
    
    # Write courses to db_courses.csv
    print("\nDone!\n\n")
    print(codes)
    print(activities)
    print(sections)
    writeFiles(codes, activities, sections, True)
        
    exit(0)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate a course list.')
    parser.add_argument('-v', '--verbose', help='Print detailed debugging information', action='store_true')
    args = parser.parse_args()
    main()
