#!/usr/local/bin/bash

sleep 10

if pgrep -q chrom; then
	echo "$(date): chrome is already running - exiting update.sh"
	exit 1
fi

export MM_CHARSET=UTF-8
export LC_COLLATE=C
export LANG=en_US.UTF-8
export PYTHONIOENCODING=utf8

cd /home/selenium/schedule-generator/course-download && /usr/local/bin/git pull >> /home/selenium/timetables-updater.log 2>&1
mv db_activities.csv db_activities.csv.old ; mv db_courses.csv db_courses.csv.old ; mv db_sections.csv db_sections.csv.old

/usr/local/bin/python2.7 updateDatabase.py -v >> /home/selenium/timetables-updater.log 2>&1

if [ $? -eq 0 ]; then
	rm *.old
	/bin/rm -f /home/selenium/schedgen/latest.tar.gz /home/selenium/schedgen/archive/courses-`/bin/date +%F`.tar.gz
	/usr/bin/tar -czf /home/selenium/schedgen/archive/courses-`/bin/date +%F`.tar.gz *.csv
	/bin/cp /home/selenium/schedgen/archive/courses-`date +%F`.tar.gz /home/selenium/schedgen/latest.tar.gz
else
	/bin/echo "Update on `/bin/date` failed" >> /home/selenium/timetables-updater.log
	mv db_activities.csv.old db_activities.csv ; mv db_courses.csv.old db_courses.csv ; mv db_sections.csv.old db_sections.csv
fi
