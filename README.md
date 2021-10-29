# uOttawa Schedule Generator - Unmaintained

> **Hi friends. It's been a pleasure helping you generate schedules over the years. This project is now unmaintained and we'll point you to another modern option: https://uschedule.me/**
>
> **Thank you for using it over the years 🫂**

A schedule generator for the University of Ottawa written in Java, using OCSF. It is a tool to help register for courses, as the University-supplied program, uOCampus, does a terribly poor job at helping students find optimized, conflict-free schedules. The schedule generator finds all possible combinations of sections for the chosen courses, and sorts them based on criteria like "Earliest Start", "Shortest Days", etc.

## [Download](https://github.com/morinted/schedule-generator/releases/download/v1.2/uOttawa-Schedule-Generator-v1.2.jar)

If you just want to get started generating schedules right away, then [download the program for Windows, Mac, and Linux](https://github.com/morinted/schedule-generator/releases/download/v1.2/uOttawa-Schedule-Generator-v1.2.jar). Note that you [need Java](https://www.java.com/download/) to run the .jar file.

## Screenshot

### The main UI, with a sample schedule already generated:

![The main UI, with a sample schedule already generated.](https://github.com/morinted/schedule-generator/raw/master/Documentation/Screenshots/mainui.png)

## Introduction

This project was created by Ted Morin & Daniel Murdoch as assignment 5 & 7 for the course SEG2105A at the University of Ottawa in 2013. The program is now maintained by David Schlachter.
We created this program based on one simple problem line:
University of Ottawa students have no easy way to generate a conflict-free, optimized schedule.

## Features

### Current Features

Some features currently present:

- A console and graphical user interface.
- Select mandatory courses, but can edit sections.
- Select x of y optional courses, ideal for electives.
- Hosting a server at schlachter.ca to let individuals connect with the GUI.
- Export .ics files for easy calendar integration.
- Sorting by various attributes, including start time, days off, and length of day.
- Can ignore discussion groups and tutorials when sorting (eg. A day with only a discussion group would be considered a day off)


## User's Guide

### Getting Started

1. To start, it is recommended to download the program's front-end. This can be found in the releases section under the name [uOttawa-Schedule-Generator-v1.2.jar](https://github.com/morinted/schedule-generator/releases/download/v1.2/uOttawa-Schedule-Generator-v1.2.jar).

2. Run the .jar.

	Simply double-click the .jar file to run. If you don't have Java installed, you can get it from [Sun's website](http://java.com/en/download/index.jsp). Note that on macOS you may need to open the program by right-clicking it and selecting Open, depending on your Gatekeeper settings.

3. Wait for the server to connect, then select a semester from the drop down.

4. Start typing a course code in the search text box.

5. Select a course from the search results. You can click "Add Selected Course", double-click the course, or, if there is only one search result, hit enter.

6. If you'd like to select a number of courses from a list of electives, check the "Optional?" option and add your electives. For example, you can add 10 electives, then say that you want to choose 2 of them, and your schedule will reflect this. The number of electives you'd like to choose is at the bottom of the left pane, changed with the "-" and "+" buttons.

	Mandatory courses or electives you definitely want should *not* be using the optional option. The function is for when you are looking at which electives are the most convenient for your schedule.

7. After selecting your courses, you may choose to omit certain sections or activities (like DGDs) based on their location, professor, or if they're full. To do this, double-click your course or click the course and select "Edit". You can use the check-boxes in the pop-up window to change your selection.

8. Select a sort order: Earliest start, shortest days, least days per week, whatever. You choose what matters most to you. By default, all other sort orders have a secondary sort of least days per week. Meaning that if two schedules have the same average starting time, the one with the least days will be favoured.

9. Click generate! You will receive the schedules on the calendar, which you can browse through.

10. You can view the text-formatted schedule (for easy entry into uOCampus) or export an .ICS file (for import into Google Calendar)

### Google Calendar Import

After exporting an iCalendar file, you will want to import it into a program. Microsoft Outlook makes it simple to just open the file, but Google Calendar has a slightly more involved process.

Note: You'll want to create a new calendar, just in case you want to delete the events created in this process. If you add it to your regular calendar and there is an error, there is no quick "Undo" function.

1. Create a new Calendar on the left side-bar. Next to "My calendars", click the drop-down and select "Create New Calendar". Assign it a snazzy color and name it something suave like "uOttawa F2014".

2. Click "Create Calendar"

3. In the drop-down next to "Other calendars", select "Import calendar"

4. On this pop-up, hit "Choose File" and select the .ICS file generated by the schedule-generator.

5. Select the calendar you just created in the drop-box, and hit "Import"

6. Enjoy your newly-generated calendar! Notice that the section, course code, course name, professor, and location are included in the events.

	We don't account for holidays, so make sure you don't come to school during Study Week!

### Hosting a Server

To host a server, download the entire project, and use ServerConsole.jar (in the releases section). Note that the ServerConsole depends on the ../course-download/db_*.csv files. So for this reason, you cannot run the ServerConsole.jar as a standalone.

To launch, you use the command: `java -jar ServerConsole.jar [port]`

Where [port] is the port to host on. If omitted, the default of 5555 will apply.

The server will create a server.stat file when schedules are generated that keeps track of how many users connect, how many schedules are generated, as well as the types of generations.

### Connecting to Different Server or Port

ClientGUI and ClientConsole can take several launch commands. In this example, the UI will be named `schedule-generator.jar`. It applies for both the GUI and the console.

In the working directory of the jar file, run `java -jar schedule-generator.jar [host] [port]`

Both optional, host lets you specify a server to connect to. The default is schlachter.ca.

Port lets you specify the port to connect on, the default, if omitted, is 5555.

### Updating Course Database

The timetables are stored in a CSV file, and follow a specific format. The `updateDatabase.py` script should download the timetables off the [uOttawa website](http://www.timetable.uottawa.ca). The output CSV is appropriately formatted to be read by the schedule generator server.

Originally, the script was a macro-enabled Excel spreadsheet using VBA to download the pages, but it was slow and the error messages were vague and difficult to pinpoint. Sometimes the script would freeze, too, with no indication of what was happening. The next iteration used a set of Python scripts, one that got all the course names using Selenium webdriver, and the other that updated all the timetables based on those courses. Currently a single Python script uses webdriver to get all the data.

In order to run these scripts, make sure to have Selenium and Chrome/Chromium installed.

To update timetables, use: `python updateDatabase.py`

Depending on your connection, the script takes about 1 - 2 hours to run for the full ~4000 courses.

**Remember to use Python 2.7+ and not 3.**


## Dependencies

The schedule generator was made using the [OCSF](http://www.site.uottawa.ca/school/research/lloseng/supportMaterial/ocsf/ocsf.html) client-server interface. The iCalendar support is provided by [Biweekly](http://sourceforge.net/projects/biweekly/)
