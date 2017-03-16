Information
===========

Here are some observations that may be useful when debugging in the future.


Clients can never connect if:
	- line order of db_sections.csv is changed
	- courses are duplicated in db_courses.csv
	
Courses cannot be generated for a course if:
	- there are duplicate sections
	- the course is the last one in the courses list
	
A crash occurs if:
	- a course/section has no activities (causes a null pointer exception)
	
Things that don't matter:
	- the numbering of activities (e.g. all lectures are numbered '00')