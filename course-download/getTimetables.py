#!/usr/bin/env python

from __future__ import print_function
import sys
import time
import traceback
import urllib2
import re
import datetime
import multiprocessing
from threading import Thread, Lock, Event
from Queue import Queue

from bs4 import BeautifulSoup


class WorkThread(Thread):
    def __init__(self, name, work_lock, work_queue, count, skipped_queue, q_activities, q_sections, q_courses, l_db):
        """
        :param str name:
        :param work_lock:
        :type work_lock: threading.Lock
        :param work_queue:
        :type work_queue: Queue.Queue
        :param count:
        :type count: int
        :param skipped_queue:
        :type skipped_queue: Queue.Queue
        :param q_activities:
        :type q_activities: Queue.Queue
        :param q_sections:
        :type q_sections: Queue.Queue
        :param q_courses:
        :type q_courses: Queue.Queue
        :param l_db:
        :type l_db: threading.Lock
        """

        Thread.__init__(self, name=name)
        self.lock = work_lock
        self.queue = work_queue
        self.count = count
        self.skipped = skipped_queue
        self.aq = q_activities
        self.sq = q_sections
        self.cq = q_courses
        self.lq = l_db

    def run(self):
        process_data(self.name, self.lock, self.queue, self.count, self.skipped, self.aq, self.sq, self.cq, self.lq)


def process_data(thread_name, lock, queue, count, skipped_queue, activity_queue, section_queue, course_queue,
                 db_lock):
    """Downloads and processes the course info
    :param thread_name: The name of the thread that started this instance
    :type thread_name: str
    :param lock: The global queue lock
    :type lock: threading.Lock
    :param queue: The queue holding the work pieces (course codes)
    :type queue: Queue.Queue
    :param count: The number of work pieces
    :type count: int
    :param skipped_queue:
    :type skipped_queue: Queue.Queue
    :param activity_queue:
    :type activity_queue: Queue.Queue
    :param section_queue:
    :type section_queue: Queue.Queue
    :param course_queue:
    :type course_queue: Queue.Queue
    :param db_lock:
    :type db_lock: threading.Lock
    """

    local_count = 0
    while True:
        lock.acquire()
        if queue.empty():
            lock.release()
            break
        else:
            course = queue.get()
            lock.release()

        try:
            retry = 5  # This loop will restart at most 5 times
            while retry:
                html = ''
                try:
                    site = urllib2.urlopen(
                        'https://web30.uottawa.ca/v3/SITS/timetable/Course.aspx?code={0}'.format(course))
                    html = site.read()
                except urllib2.HTTPError, e:
                    print('Server error: {0}.'.format(e.reason))
                    if retry:
                        print('Waiting 2 seconds...')
                        time.sleep(2)
                        retry -= 1
                        continue
                    else:
                        print('Skipping course')
                        lock.acquire()
                        skipped_queue.put(course)
                        lock.release()
                        break
                except urllib2.URLError, e:
                    print('Internet problem: {0}.'.format(e.reason))
                    if retry:
                        print('Waiting 5 seconds...')
                        time.sleep(5)
                        retry -= 1
                        continue
                    else:
                        print('Skipping course')
                        lock.acquire()
                        skipped_queue.put(course)
                        lock.release()
                        break
                finally:
                    try:
                        site.close()
                    except NameError:
                        pass

                soup = BeautifulSoup(html)
                content = soup.find('div', id='main-content')

                # Get course title
                title = re.search(r'{0} - (.*)'.format(course), content.find('h1').get_text())
                if title is None:
                    lock.acquire()
                    skipped_queue.put(course)
                    lock.release()
                    break
                else:
                    title = title.group(1).strip()

                if ',' in title:
                    title = u'"{0}"'.format(title)

                # Get all semesters
                sections = 0
                for semester in content.find('div', id='schedule').find_all('div', class_='schedule'):
                    # Get semester integer
                    semester_id = semester.get('id')

                    for section in semester.find_all('table'):
                        _section_title = section.find('td', class_='Section').contents[0]
                        section_id = re.search(r'{0} (.*)'.format(course), _section_title)
                        if section_id is not None:
                            section_id = section_id.group(1).strip()

                        one_dgd = 0
                        one_lab = 0
                        one_tut = 0

                        _footer = section.find('tr', class_='footer')
                        if _footer is not None:
                            _footer_content = _footer.find('td').get_text()

                            if u'Only one discussion group' in _footer_content:
                                one_dgd = 1

                            if u'Only one laboratory' in _footer_content:
                                one_lab = 1

                            if u'Only one tutorial' in _footer_content:
                                one_tut = 1

                        activities = 0
                        for activity in section.find_all('td', class_='Activity'):
                            # Lecture, Lab, etc.
                            activity_type = re.search(r'([a-zA-Z ]+)', activity.get_text()).group(1).strip()

                            # 1, 2, etc.
                            activity_num = re.search(r'(\d+)', activity.get_text()).group(1).strip()

                            # Day
                            _day = activity.next_sibling

                            # Sunday - Saturday
                            activity_day = re.search(r'([a-zA-Z ]+)', _day.get_text())
                            if activity_day is None:
                                activity_day = u'N/A'
                            else:
                                activity_day = activity_day.group(1).strip()

                            # 08:30 - 22:00
                            activity_time_start = re.search(r'(\d{2}:\d{2}) -', _day.get_text()).group(1).strip()
                            activity_time_end = re.search(r'- (\d{2}:\d{2})', _day.get_text()).group(1).strip()

                            # Place
                            _place = _day.next_sibling

                            # eg: SMD 224
                            activity_place = _place.get_text().strip()
                            if u'available' in activity_place:
                                activity_place = u'N/A'

                            # Professor
                            _professor = _place.next_sibling

                            # eg: John Smith
                            activity_prof = _professor.get_text().strip()

                            if activity_prof == u'&nbsp;' or u'available' in activity_prof:
                                activity_prof = u'N/A'

                            # Add activity to list
                            db_lock.acquire()
                            if section_id is None:
                                string = u'{0},{1},{2},{4},{5},{6},{7},{8},{9}'
                            else:
                                string = u'{0},{1},{2} {3},{4},{5},{6},{7},{8},{9}'

                            activity_queue.put(
                                string.format(
                                    activity_type, activity_num, course, section_id, semester_id, activity_day,
                                    activity_time_start, activity_time_end, activity_place, activity_prof
                                )
                            )
                            activities = 1
                            db_lock.release()

                        if activities:
                            # If there was at least one activity, add the section to the sections list.
                            db_lock.acquire()
                            if section_id is None:
                                string = u'{0},{0},{2},{3},{4},{5}'
                            else:
                                string = u'{0} {1},{0},{2},{3},{4},{5}'

                            section_queue.put(
                                string.format(
                                    course, section_id, semester_id, str(one_dgd), str(one_tut), str(one_lab)
                                )
                            )
                            sections = 1
                            db_lock.release()

                if sections:
                    # If there was at least one section, add the course to the courses list.
                    db_lock.acquire()
                    course_queue.put(
                        u'{0},{1}'.format(
                            course, title
                        )
                    )
                    db_lock.release()
                else:
                    # Otherwise add it to the skipped list so that the user can double-check it
                    lock.acquire()
                    skipped_queue.put(course)
                    lock.release()

                break  # break out of the retry loop

            local_count += 1
            lock.acquire()
            todo = queue.qsize()
            print('[{0}][Thread: {1}][Total: {2}/{3}]'.format(thread_name, local_count, count - todo, count))
            lock.release()
        except AttributeError, e:
            print('Error in thread {0} with course {1}: {2}'.format(thread_name, course, e.message), file=sys.stderr)
            traceback.print_exc()


def main():
    """Main method/entrypoint
    """

    # Courses
    work_lock = Lock()
    work_queue = Queue(0)
    course_count = 0
    skipped_queue = Queue(0)

    work_lock.acquire()

    with open("courses.txt", "r") as f:
        for line in f:
            work_queue.put(line.strip())
            course_count += 1

    work_lock.release()

    # For holding the database info
    db_courses = Queue(0)
    db_sections = Queue(0)
    db_activities = Queue(0)
    db_lock = Lock()

    # Create the threads
    thread_list = []
    thread_count = multiprocessing.cpu_count() * 2
    for x in range(1, thread_count + 1):
        t = WorkThread(name=''.join(['Worker-', str(x)]), work_lock=work_lock, work_queue=work_queue,
                       count=course_count, skipped_queue=skipped_queue, q_activities=db_activities,
                       q_sections=db_sections, q_courses=db_courses, l_db=db_lock)
        thread_list.append(t)

    # Start the threads
    for t in thread_list:
        t.start()

    # Wait for all threads to complete
    for t in thread_list:
        t.join()

    print()
    # Announce skipped courses
    if not skipped_queue.empty():
        print('These courses were skipped: ')
        with open('skippedCourses.txt', 'w') as f:
            while not skipped_queue.empty():
                skipped_course = skipped_queue.get()
                print('  {0}'.format(skipped_course))
                f.write(u'{0}\n'.format(skipped_course).encode('utf8'))
        print()

    # Print total count of all items
    print('Courses: {0}'.format(db_courses.qsize()))
    print('Sections: {0}'.format(db_sections.qsize()))
    print('Activities: {0}'.format(db_activities.qsize()))

    # Write courses to files
    with open('db_courses.csv', 'w') as f:
        while not db_courses.empty():
            f.write(u'{0}\n'.format(db_courses.get()).encode('utf8'))

    # Write sections to files
    with open('db_sections.csv', 'w') as f:
        while not db_sections.empty():
            f.write(u'{0}\n'.format(db_sections.get()).encode('utf8'))

    # Write activities to files
    with open('db_activities.csv', 'w') as f:
        while not db_activities.empty():
            f.write(u'{0}\n'.format(db_activities.get()).encode('utf8'))


if __name__ == '__main__':
    sys.exit(main())
