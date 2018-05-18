#!/usr/bin/env python

from __future__ import print_function
import argparse
import re
import time

from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException

base_url = "https://web30.uottawa.ca/v3/SITS/timetable/Search.aspx"


def main(debug=False):
    # Open in Chrome
    # Note: it may be necessary to specify the path to the chromedriver file, e.g. 
    # browser = webdriver.Chrome("/home/david/Downloads/chromedriver")
    browser = webdriver.Chrome()
    browser.get(base_url)

    # Click search button
    search_button = browser.find_element_by_id('ctl00_MainContentPlaceHolder_Basic_Button')
    search_button.click()
    codes = []

    # Continue flag
    cont = True

    # For each page in "Next"
    while cont:
        # noinspection PyBroadException
        try:
            html = browser.page_source
            current_codes = re.findall('<a href="Course.aspx\?id=(\d+&)amp;(term=\d+&)amp;(session=[A-Z]+)', html)
            waited = 0
            while len(current_codes) < 2:
                time.sleep(2)
                waited += 1
                html = browser.page_source
                current_codes = re.findall('<a href="Course.aspx\?id=(\d+&)amp;(term=\d+&)amp;(session=[A-Z]+)', html)
                if waited > 15:
                    print('Time out. Could not find course codes.')
                    break

            codes += [''.join(t) for t in current_codes]
            if debug:
                print(current_codes)
            next_link = browser.find_element_by_xpath('/html/body/form/div[3]/div[2]/div/div[3]/div[4]/span[2]/a')
            next_link.click()
        except NoSuchElementException:
            print('No next button. We\'ve reached the end of the list.')
            cont = False

    browser.quit()

    # Print raw count of courses
    print('Found', str(len(codes)), 'courses.')

    # Remove duplicates with set, then sort.
    print('Removing duplicates and sorting alphabetically...')
    codes = list(set(codes))
    codes.sort()

    # Print net/unique course count
    print('Done! Now there are', str(len(codes)), 'courses.')

    # Write courses to courses.txt
    with open('courses.txt', 'wb') as f:
        f.writelines((u'{0}\n'.format(c).encode('utf8') for c in codes))

    print('Courses printed to courses.txt')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Generate a course list.')
    parser.add_argument('-v', '--verbose', help='Print all the course IDs', action='store_true')
    parser.add_argument('--version', action='version', version='%(prog)s 2.0')
    args = parser.parse_args()
    main(debug=args.verbose)
