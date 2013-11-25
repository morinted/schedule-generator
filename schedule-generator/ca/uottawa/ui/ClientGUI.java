package ca.uottawa.ui;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.uottawa.schedule.Activity;
import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.CourseSelection;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.Section;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ClientGUI implements ClientIF, ActionListener, DocumentListener, ItemListener, WindowListener, ListSelectionListener {
	
	final public static int DEFAULT_PORT = 5555;
	ScheduleGeneratorClient client;
	
	//Instance variables.
	int currSchedule;
	int currSemester;
	int k;
	int n;
	List<Schedule> currSchedules;
	Course courseEditing;
	List<JCheckBox> chkSections = new ArrayList<JCheckBox>();
	List<ArrayList<JCheckBox>> chkActivities = new ArrayList<ArrayList<JCheckBox>>();
	
	//Constants
	private static final int SIDEBAR_WIDTH = 390;
	private static final int HEIGHT = 900;
	private static final int CANVAS_HEIGHT = 840;
	private static final int CANVAS_WIDTH = 1040;
	private static final int WIDTH = 1430;
	private static final int HALF_HOUR = 30;
	private static final int HALF_HOUR_MARGIN = 10;
	private static final int DAY = 130;
	private static final int DAY_MARGIN = 30;
	
	//GUI variables
	//The top-level frame
	JFrame frame;
	//I personally like making sub-components to that we can easily move them around.
	Container paneMain;
	JPanel paneLeftSideBar, paneRightSideBar, paneSemester, paneSearch, paneList, paneOptions, paneDisplay, paneSchedule, paneIncDec;
	//Some labels that will go into those components above.
	JLabel lblSearch, lblSemester, lblCourses, lblOptionalCourses, lblOptions, lblNChooseK, lblSortOrder, lblCurrSchedule;
	//Options please?
	JCheckBox chkOptional, chkIgnoreExtras;
	//Selecting the semester and the sort order with a combobox
	JComboBox<String> cboSemester, cboSortOrder;
	//Buttons. These are pretty telling of what actions will occur.
	JButton btnAdd, btnRemove, btnEdit, btnClearAll, btnIncK, btnDecK, btnGenerate, btnNext, btnPrev, btnFirst, btnLast, btnPrint;
	//Areas to write text
	JTextField txtSearch;
	//To hold lists (like search results)
	JList<String> lstSearchResults, lstCourses, lstOptionalCourses;
	JScrollPane scrSearchResults, scrCourses, scrOptionalCourses;
	//To draw schedules
	BufferedImage biSchedule;
	JLabel lblDisplay; //to hold the BI
	Color bg = Color.WHITE; //default background color.
	RenderingHints renderingHints; 
	Font fntMain;
	Font fntActivity;
	Color labColor;
	Color lecColor;
	Color othColor;
	
	
	public ClientGUI(String title, String studentNumber, String host, int port) {
		//Create the main frame.
		JFrame frame = new JFrame(title);
		paneMain = frame.getContentPane();
		frame.setSize(new Dimension(WIDTH, HEIGHT));
		k = 0; //We start by chosing 0 of 0 optional courses
		n = 0;
		createComponents();
		addListeners();
		
		//Some settings
		currSchedule = 0;
		currSchedules = new ArrayList<Schedule>();
		labColor = new Color(255, 210, 210);
		lecColor = new Color(255, 250, 180);
		othColor = new Color(216, 212, 255);
		fntMain = new Font("SansSerif", Font.BOLD, 15);
		fntActivity = new Font("SansSerif", Font.BOLD, 13);
		
		//Display the frame:
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		clear();
		
		try 
	    {
	      client = new ScheduleGeneratorClient(studentNumber, host, port, this);
	    } 
	    catch(IOException exception) 
	    {
	      display("Error: Can't setup connection!"
	                + " Terminating program.");
	      System.exit(1);
	    }
	}

	private void addListeners() {
		//We will listen for actions on the following items:
		
		//Buttons
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		btnClearAll.addActionListener(this);
		btnEdit.addActionListener(this);
		btnIncK.addActionListener(this);
		btnDecK.addActionListener(this);
		btnGenerate.addActionListener(this);
		btnNext.addActionListener(this);
		btnPrev.addActionListener(this);
		btnLast.addActionListener(this);
		btnFirst.addActionListener(this);
		
		//Text boxes
		txtSearch.getDocument().addDocumentListener(this);
		
		//ComboBoxes
		cboSemester.addItemListener(this);
		cboSortOrder.addItemListener(this);
		
		//Checkboxes
		chkIgnoreExtras.addActionListener(this);
		
		//Lists
		lstCourses.addListSelectionListener(this);
		lstOptionalCourses.addListSelectionListener(this);
	}

	private void createComponents() {
		//Now we are going to create all the components for the layout. This is where we are flexing our muscles, so to speak.
		//paneMain is a box layout in the x axis. We will create other panes as vertical layouts.
		paneMain.setLayout(new BoxLayout(paneMain, BoxLayout.X_AXIS));
		paneLeftSideBar = new JPanel();
		paneRightSideBar = new JPanel();
		paneLeftSideBar.setLayout(new BoxLayout(paneLeftSideBar, BoxLayout.Y_AXIS));
		paneRightSideBar.setLayout(new BoxLayout(paneRightSideBar, BoxLayout.Y_AXIS));
		GridBagConstraints c; //For using the gridbag layout.
		
		/*
		 * Creating the semester selector. This is the first thing the user should do.
		 */
		paneSemester = new JPanel();
		paneSemester.setLayout(new GridBagLayout());
		paneSemester.setBorder(BorderFactory.createTitledBorder("Select Semester"));
		lblSemester = new JLabel("Semester:");
		cboSemester = new JComboBox<String>();
		
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		paneSemester.add(lblSemester, c);
		c.gridx = 1;
		c.gridy = 0;
		paneSemester.add(cboSemester, c);
		
		/*
		 * Creating the search pane.
		 * Search for course code: [       ]
		 * |-------------------------------|
		 * |
		 * |
		 * |
		 * |-------------------------------|
		 * [ ] Optional?  [  ADD SELECTED  ]
		 */
		paneSearch = new JPanel();
		paneSearch.setLayout(new GridBagLayout());
		paneSearch.setBorder(BorderFactory.createTitledBorder("Add Courses"));
		c = new GridBagConstraints();
		//Create search label.
		lblSearch = new JLabel("Search:");
		//Create search text box.
		txtSearch = new JTextField();
		//And a list box to display the search results
		lstSearchResults = new JList<String>();
		lstSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstSearchResults.setPrototypeCellValue("Prototype Cell Value");
		scrSearchResults = new JScrollPane(lstSearchResults);
		//We'll now have the option to add optional.
		chkOptional = new JCheckBox("Optional?");
		//Button to add, finally.
		btnAdd = new JButton("Add Selected Course");
		//Add components to pane
		c.fill = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		paneSearch.add(lblSearch, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; //Move one over and place text box.
		c.gridy = 0;
		paneSearch.add(txtSearch, c);
		c.gridx = 0;
		c.gridy = 1; //Place search results below.
		c.gridwidth = 2;
		paneSearch.add(scrSearchResults, c);
		c.gridx = 0;
		c.gridy = 2; //chkOptional is even lower.
		c.gridwidth = 1;
		c.fill = GridBagConstraints.CENTER;
		paneSearch.add(chkOptional, c);
		c.gridx = 1;
		c.gridy = 2; //And add button is the the right of optional.
		c.fill = GridBagConstraints.HORIZONTAL;
		paneSearch.add(btnAdd, c);
		
		
		/*
		 * Creating the selected courses list panes
		 */
		paneList = new JPanel();
		paneList.setLayout(new GridBagLayout());
		paneList.setBorder(BorderFactory.createTitledBorder("Selected Courses"));
		c = new GridBagConstraints();
		
		//Create labels
		lblCourses = new JLabel("Mandatory Courses:");
		lblOptionalCourses = new JLabel("Optional Courses:");
		
		//Create lists and scroll panes
		lstCourses = new JList<String>();
		lstOptionalCourses = new JList<String>();
		lstCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstOptionalCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstCourses.setPrototypeCellValue("Prototype Cell Value");
		lstOptionalCourses.setPrototypeCellValue("Prototype Cell Value");

		scrCourses = new JScrollPane(lstCourses);
		scrOptionalCourses = new JScrollPane(lstOptionalCourses);
		
		//Create remove and clear button
		btnRemove = new JButton("Remove");
		btnClearAll = new JButton("Clear");
		btnEdit = new JButton("Edit");
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		paneList.add(lblCourses, c);
		c.gridx = 0;
		c.gridy = 1;
		paneList.add(scrCourses, c);
		c.gridx = 0;
		c.gridy = 2;
		paneList.add(lblOptionalCourses, c);
		c.gridx = 0;
		c.gridy = 3;
		paneList.add(scrOptionalCourses, c);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		paneList.add(btnClearAll, c);
		c.gridx = 1;
		c.gridy = 4;
		paneList.add(btnRemove, c);
		c.gridx = 2;
		c.gridy = 4;
		paneList.add(btnEdit, c);
		
		/*
		 * Create options pane
		 */
		paneOptions = new JPanel();
		paneOptions.setLayout(new GridBagLayout());
		paneOptions.setBorder(BorderFactory.createTitledBorder("Generate"));
		//Create labels
		lblNChooseK = new JLabel("Selecting " + k + " out of " + n + " optional courses");
		lblSortOrder = new JLabel("Sort order:");
		//Check box for ignore extras
		chkIgnoreExtras = new JCheckBox("Ignore discussion groups and tutorials while sorting.");
		//Buttons
		btnIncK = new JButton("+");
		btnDecK = new JButton("-");
		btnGenerate = new JButton("Generate Schedules");
		//Combobox for sortorder
		cboSortOrder = new JComboBox<String>();
		cboSortOrder.addItem("Earliest Start");
		cboSortOrder.addItem("Latest Start");
		cboSortOrder.addItem("Earliest End");
		cboSortOrder.addItem("Latest End");
		cboSortOrder.addItem("Shortest Days");
		cboSortOrder.addItem("Longest Days");
		cboSortOrder.addItem("Most Days Per Week");
		cboSortOrder.addItem("Least Days Per Week");
		
		paneIncDec = new JPanel();
		paneIncDec.setLayout(new BoxLayout(paneIncDec, BoxLayout.X_AXIS));
		paneIncDec.add(btnIncK);
		paneIncDec.add(btnDecK);
		
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		paneOptions.add(lblNChooseK, c);
		c.gridy = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.EAST;
		paneOptions.add(paneIncDec, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		paneOptions.add(chkIgnoreExtras, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		paneOptions.add(lblSortOrder, c);
		c.gridx = 1;
		c.gridy = 2;
		paneOptions.add(cboSortOrder, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		paneOptions.add(btnGenerate, c);
		
		/*
		 * Creating the display pane.
		 */
		paneDisplay = new JPanel();
		paneDisplay.setLayout(new BoxLayout(paneDisplay, BoxLayout.X_AXIS));
		biSchedule = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		lblDisplay = new JLabel(new ImageIcon(biSchedule));
		lblDisplay.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		paneDisplay.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		paneDisplay.add(lblDisplay);
		
		/*
		 * Creating the navigation pane for schedules
		 */
		paneSchedule = new JPanel();
		paneSchedule.setLayout(new BoxLayout(paneSchedule, BoxLayout.X_AXIS));
		btnNext = new JButton("Next >");
		btnNext.setEnabled(false);
		btnPrev = new JButton("< Prev");
		btnPrev.setEnabled(false);
		btnFirst = new JButton("|<< First");
		btnFirst.setEnabled(false);
		btnLast = new JButton("Last >>|");
		btnLast.setEnabled(false);
		lblCurrSchedule = new JLabel("  Displaying Schedule 0 / 0  ");
		paneSchedule.add(btnFirst);
		paneSchedule.add(btnPrev);
		paneSchedule.add(lblCurrSchedule);
		paneSchedule.add(btnNext);
		paneSchedule.add(btnLast);

		
		/*
		 * Adding all panes to main layout.
		 */
		paneLeftSideBar.add(paneSemester);
		paneLeftSideBar.add(paneSearch);
		paneLeftSideBar.add(paneList);
		paneLeftSideBar.add(paneOptions);
		paneRightSideBar.add(paneDisplay);
		paneRightSideBar.add(paneSchedule);
		paneMain.add(paneLeftSideBar);
		paneMain.add(paneRightSideBar);

	}
	
	public void clear() {
		//Clears the canvas
		Graphics2D g = biSchedule.createGraphics();
		g.setFont(fntMain);
		g.setBackground(new Color(255,255,255,0));
		g.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		
		
		//Drawing time-lines (haha)
		for (int i=0; i<27; i++) {
			if (i>0) {
			//Let's make a label saying the time.
			g.setColor(Color.BLACK);
			//Draw time:
			String time = new String(((i/2)+8)+ ":00");
			g.drawString(time, (int)(0.5*DAY-DAY_MARGIN), HALF_HOUR*i-HALF_HOUR_MARGIN+(int)(fntMain.getSize()/2));
			
			g.drawLine(DAY-DAY_MARGIN, HALF_HOUR*i-HALF_HOUR_MARGIN, DAY*8-DAY_MARGIN, HALF_HOUR*i-HALF_HOUR_MARGIN);
			}
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(DAY-DAY_MARGIN, HALF_HOUR*(i+1)-HALF_HOUR_MARGIN, DAY*8-DAY_MARGIN, HALF_HOUR*(i+1)-HALF_HOUR_MARGIN);
			i++;
		}
		g.setColor(Color.BLACK);
		String time = new String("22:00");
		g.drawString(time, (int)(0.5*DAY-DAY_MARGIN), HALF_HOUR*28-HALF_HOUR_MARGIN+(int)(fntMain.getSize()/2));
		g.drawLine(DAY-DAY_MARGIN, HALF_HOUR*(28)-HALF_HOUR_MARGIN, DAY*8-DAY_MARGIN, HALF_HOUR*(28)-HALF_HOUR_MARGIN);
		
		//Drawing day-dividers
		String[] day = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		for (int i=1; i<=8; i++) {
			//First day is Sunday
			g.setColor(Color.BLACK);
			//We'd like to center the string.
			//We don't print a day for the last line, which is after Saturday
			if (i<8) {
			int stringLen = (int)g.getFontMetrics().getStringBounds(day[i-1], g).getWidth();  
			stringLen = stringLen/2;
			g.drawString(day[i-1], DAY*i-DAY_MARGIN+(DAY/2)-stringLen, HALF_HOUR-(int)(HALF_HOUR_MARGIN*1.5));
			}
			g.drawLine(DAY*i-DAY_MARGIN, HALF_HOUR-HALF_HOUR_MARGIN, DAY*i-DAY_MARGIN, HALF_HOUR*28-HALF_HOUR_MARGIN);
		}
		
		//Update the label to announce which schedule we're drawing, can be 0 / 0!
		lblCurrSchedule.setText("  Displaying Schedule " + currSchedule + " / " + currSchedules.size() + "  ");
		
		g.dispose();
		lblDisplay.repaint();
	}


	public static void main(String[] args) {
		//Start the GUI.
		//For now, use default host/port
		String studentNumber = ""; //For storing the student number
	    String host = "";
	    int port;
		//S# is a required parameter.
	    try
	    {
	      studentNumber = args[0]; //Gets the login ID.
	    }
	    catch(ArrayIndexOutOfBoundsException e) //If the user didn't provide one, then the client disconnects.
	    {
	    	studentNumber = JOptionPane.showInputDialog("Please enter your student number.");
	    }
	    try //Gets host param is necessary.
	    {
	      host = args[1];
	    }
	    catch(ArrayIndexOutOfBoundsException e)
	    {
	      host = "localhost";
	    }
	    
	    try //Get port if needed
	    {
	    	port = Integer.parseInt(args[2]); //Try to get it after the host
	    }
	    catch(ArrayIndexOutOfBoundsException e)
	    {
	    	port = DEFAULT_PORT; //Else default to the default port.
	    } 
		new ClientGUI("uOttawa Schedule Generator", studentNumber, host, port);
		
	}

	public void actionPerformed(ActionEvent e) {
		Object sender = e.getSource();
		if (sender.equals(btnAdd)) {
			addCourse();
		} else if (sender.equals(btnIncK)) {
			if (k < n) {
				k++;
				setK();
				updateLblNChooseK();
			}
		} else if (sender.equals(btnDecK)) {
			if (k > 1) {
				k--;
				setK();
				updateLblNChooseK();
			}
		} else if (sender.equals(btnRemove)) {
			removeCourse();
		} else if (sender.equals(btnClearAll)) {
			int reply = JOptionPane.showConfirmDialog(null, "Clear course selection? There's no going back!", "Confirm Clear", JOptionPane.YES_NO_OPTION);
	        if (reply == JOptionPane.YES_OPTION) {
			removeAllCourses(); 
			}
		} else if (sender.equals(chkIgnoreExtras)) {
			int ie = chkIgnoreExtras.isSelected() ? 1 : 0;
				send("IGNOREEXTRAS " + ie);
		} else if (sender.equals(btnGenerate)) {
			send("GENERATE");
		} else if (sender.equals(btnEdit)) {
				String toEdit;
				toEdit = lstCourses.getSelectedValue();
				if (toEdit == null) {
					toEdit = lstOptionalCourses.getSelectedValue();
				}
				if (toEdit == null) {
					display("Can't edit: No course selected.");
				} else {
					toEdit = toEdit.split(" ")[0];
					send("EDIT " + toEdit);
				}
				
		} else if (sender.equals(btnNext)) {
			currSchedule++;
			if (currSchedule > 1) {
				btnPrev.setEnabled(true);
				btnFirst.setEnabled(true);
			}
			if (currSchedule==currSchedules.size()) {
				btnNext.setEnabled(false);
				btnLast.setEnabled(false);
			}
			drawSchedule();
		} else if (sender.equals(btnLast)) {
			currSchedule = currSchedules.size();
			if (currSchedule > 1) {
				btnPrev.setEnabled(true);
				btnFirst.setEnabled(true);
			}
			btnNext.setEnabled(false);
			btnLast.setEnabled(false);
			drawSchedule();
		} else if (sender.equals(btnPrev)) {
			currSchedule--;
			if (currSchedule < currSchedules.size()) {
				btnNext.setEnabled(true);
				btnLast.setEnabled(true);
			}
			if (currSchedule == 1) {
				btnPrev.setEnabled(false);
				btnFirst.setEnabled(false);
			}
			drawSchedule();
		} else if (sender.equals(btnFirst)) {
			currSchedule = 1;
			if (currSchedule < currSchedules.size()) {
				btnNext.setEnabled(true);
				btnLast.setEnabled(true);
			}
			btnFirst.setEnabled(false);
			btnPrev.setEnabled(false);
			drawSchedule();
		}
	}
	
	private void send(String msg) {
		try {
			client.handleMessageFromClientUI(msg);
		} catch (IOException e) {
			display("Error communicating with client.");
		}
	}

	private void removeAllCourses() {
		ListModel<String> courses = lstCourses.getModel();
		ListModel<String> nCourses = lstOptionalCourses.getModel();
		String toRemove;
		for (int i = 0; i < courses.getSize(); i++) {
			toRemove = courses.getElementAt(i).split(" ")[0];
			send("REMOVE " + toRemove);
		}
		for (int i = 0; i < nCourses.getSize(); i++) {
			toRemove = nCourses.getElementAt(i).split(" ")[0];
			send("REMOVE " + toRemove);
		}
	}

	private void removeCourse() {
		if ((lstCourses.getSelectedValue() == null) && (lstOptionalCourses.getSelectedValue() == null)) {
			display("No course selected!");
		} else {
			String courseCode;
			if (lstCourses.getSelectedValue() != null) {
				courseCode = lstCourses.getSelectedValue().split(" ")[0];
			} else {
				courseCode = lstOptionalCourses.getSelectedValue().split(" ")[0];
			}
				send("REMOVE " + courseCode);
		}
	}

	private void addCourse() {
		if (lstSearchResults.getSelectedValue() == null) {
			display("No course selected!");
		} else {
				String courseCode = lstSearchResults.getSelectedValue().split(" ")[0];
				//Determine if we're sending an optional or mandatory course.
				String optional = chkOptional.isSelected() ? "OPTIONAL " : "";
				send("ADD " + optional + courseCode);
		}
	}

	public void sendSearchResults(List<String> results) {
		//We must display the search results.
		String[] searchResults = results.toArray(new String[results.size()]);
		lstSearchResults.setListData(searchResults);
	}

	public void sendInfo(String msg) {
		System.out.println(msg);
	}

	public String getSemester(List<String> semesters) {
		String currentSelection = (String) cboSemester.getSelectedItem();
		if (currentSelection == null) {
			String month, year;
			for (String s : semesters) {
				year = s.substring(0, 4);
                month = new DateFormatSymbols().getMonths()[Integer.parseInt(s.substring(4))-1];
				cboSemester.addItem(month + " " + year);
			}
			cboSemester.setSelectedIndex(-1);
			btnAdd.setEnabled(false);
			txtSearch.setEditable(false);
			chkOptional.setEnabled(false);
			btnClearAll.setEnabled(false);
			btnRemove.setEnabled(false);
			btnIncK.setEnabled(false);
			btnDecK.setEnabled(false);
			chkIgnoreExtras.setEnabled(false);
			cboSortOrder.setEnabled(false);
			btnGenerate.setEnabled(false);
			btnEdit.setEnabled(false);
			while (cboSemester.getSelectedIndex() == -1) {
				//wait.
			}
			btnAdd.setEnabled(true);
			txtSearch.setEditable(true);
			chkOptional.setEnabled(true);
			btnClearAll.setEnabled(true);
			btnRemove.setEnabled(true);
			btnIncK.setEnabled(true);
			btnDecK.setEnabled(true);
			chkIgnoreExtras.setEnabled(true);
			cboSortOrder.setEnabled(true);
			btnGenerate.setEnabled(true);
			btnEdit.setEnabled(true);

		} 
		//refresh the list in case we have just changed semesters.
		send("LIST");
		currSemester = cboSemester.getSelectedIndex();
		return semesters.get(cboSemester.getSelectedIndex());
	}

	public void done() {}

	@Override
	public String getSortOrder() {
		String sortOrder = "earliestStart";//just in case there's an error.
		switch(cboSortOrder.getSelectedIndex()) {
		 			 case 0: sortOrder = "earliestStart";
		 			 break;
		             case 1: sortOrder = "latestStart";
		             break;
		             case 2:sortOrder = "earliestEnd";
		             break;
		             case 3:sortOrder = "latestEnd";
		             break;
		             case 4:sortOrder = "shortestDay";
		             break;
		             case 5:sortOrder = "longestDay";
		             break;
		             case 6:sortOrder = "days";
		             break;
		             case 7:sortOrder = "daysOff";
		}
		return sortOrder;
	}

	public void displaySchedules(List<Schedule> schedules) {
		currSchedules = schedules;
		if (schedules.size() < 1) {
			display("There are no possible schedules for your current selection.");
			clear();
			System.out.println("Clearing");
		} else {
			currSchedule = 1;
			if (schedules.size()>1) {
			btnNext.setEnabled(true);
			btnLast.setEnabled(true);
			}
			drawSchedule();
		}
	}

	private void drawSchedule() {
		clear(); //clean the last schedule.
		
		//Draws the current schedule!
		Schedule toDraw = currSchedules.get(currSchedule-1);
		Graphics2D g = biSchedule.createGraphics();
		g.setFont(fntActivity);
		
		for (CourseSelection cs : toDraw.getCourseSelections()) {
			//So, we're moving through something like 5 classes per schedule.
			//CS is ONE class.
			//The CS is made up of activities. We've got to draw each activity.
			for (Activity a: cs.getActivities()) {
				//Now we've got one activity. Yay! The nitty-gritty!
				int day = a.getDay();
				if (day==0) {
					//This must be a web course. We don't draw non-scheduled items.
					continue;
				}
				Date startTime = a.getStartTime();
				Date endTime = a.getEndTime();
				//We're going to convert start time into the number
				//of half-hour blocks after 8:30 that it is. (0-27).
				double hStart = startTime.getTime()/1000/60/30 - 26;
				double hLength = endTime.getTime()/1000/60/30 - 26 - hStart;
				//Determine class information
				String strSec = a.getSection().getName();
				String actType = a.getType();
				String strAct = actType + " " + a.getNumber();
				String strLoc = a.getPlace();
				
				//We should find out what color this activity will be drawn in.
				switch (actType) {
				case "LEC":
				case "SEM":
				case "VID":
				case "AUD":
					//This is a lecture-type course.
					g.setColor(lecColor);
					break;
				case "LAB":
				case "RSH":
				case "WRK":
					//This is an applied course of sorts.
					g.setColor(labColor);
					break;
				default:
					//This is a DGD/TUT/etc. type course.
					g.setColor(othColor);
				}
				
				//Now we have the information for the activity. We must
				//draw it as a rectangle on the graphics.
				int x = day*DAY-DAY_MARGIN+1;
				int y = (int) (hStart*HALF_HOUR - HALF_HOUR_MARGIN) + 1;
				int width = DAY-1;
				int height = (int) (hLength*HALF_HOUR) -1;
				g.fillRect(x, y, width, height);
				
				//Rectangle drawn. Now we can add some text.
				//Text color: Black of course!
				g.setColor(Color.BLACK);
				int stringLen = (int)g.getFontMetrics().getStringBounds(strSec, g).getWidth();  
				stringLen = stringLen/2;
				x += DAY/2;
				y += 20;
				g.drawString(strSec, x-stringLen, y);
				y += 20;
				stringLen = (int)g.getFontMetrics().getStringBounds(strAct, g).getWidth();  
				stringLen = stringLen/2;
				g.drawString(strAct, x-stringLen, y);
				y += 20;
				stringLen = (int)g.getFontMetrics().getStringBounds(strLoc, g).getWidth();  
				stringLen = stringLen/2;
				g.drawString(strLoc, x-stringLen, y);
			}
		}
		lblDisplay.repaint();
	}

	public void setCourses(List<Course> courses, List<Course> nCourses) {
		//This is the display courses section.
		
		String[] manCourses = new String[courses.size()];
		for (int i = 0; i < courses.size(); i++) {
			manCourses[i] = courses.get(i).getDescription();
		}
		lstCourses.setListData(manCourses);
		String[] opCourses = new String[nCourses.size()];
		for (int i = 0; i < nCourses.size(); i++) {
			opCourses[i] = nCourses.get(i).getDescription();
		}
		lstOptionalCourses.setListData(opCourses);
		n = opCourses.length;
		if (k>n) {
			k=n; //if this changes the validity of k over n, we should fix it.
		}
		updateLblNChooseK();
	}

	private void updateLblNChooseK() {
		if (n==0) {
			k=0;
			setK();
		} else if (n>0 && k==0) {
			k = 1;
			setK();
		}
		
		lblNChooseK.setText("Selecting " + k + " ouf of " + n + " optional courses");
	}
	
	private void setK() {
		send("SETK " + k);
	}

	public void editCourse(Course edit, String semester) {
		//Editing a course. Let's make a list, similar to how we do on the client console.
		List<Section> editSections = new ArrayList<Section>();
		for (Section s : edit.getSections()) {
			if (s.getSemester().equals(semester)) {
				editSections.add(s);
			}
		}
		courseEditing = edit;
		
		//Now edit sections contains the current semester's lists.
		JFrame editFrame = new JFrame("Edit Course");
		editFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		editFrame.setResizable(false);
		Container pane = (editFrame.getContentPane());
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		cboSemester.setEnabled(false);
		btnAdd.setEnabled(false);
		txtSearch.setEditable(false);
		chkOptional.setEnabled(false);
		btnClearAll.setEnabled(false);
		btnRemove.setEnabled(false);
		btnIncK.setEnabled(false);
		btnDecK.setEnabled(false);
		chkIgnoreExtras.setEnabled(false);
		cboSortOrder.setEnabled(false);
		btnGenerate.setEnabled(false);
		btnEdit.setEnabled(false);
		lstCourses.setEnabled(false);
		lstOptionalCourses.setEnabled(false);
		lstSearchResults.setEnabled(false);
		
		//Let's think of the cases.
		//1. Course has only one section and no optional courses. All checkboxes will be disabled.
		//2. Course has multiple sections with no optional courses. You may choose down to only 1 sections.
		//3. Course has DONTCARE sections, and optional courses. You may choose down to 1 activity of that type.
		
		//So we need to make a checkbox for each section.
		chkSections = new ArrayList<JCheckBox>();
		chkActivities = new ArrayList<ArrayList<JCheckBox>>();
		int i=0;
		for (Section s : editSections) {
			JCheckBox currentChk = new JCheckBox(s.getName());
			boolean sectionSelected;
			if (s.isSelected()) {
				sectionSelected = true;
				currentChk.setSelected(true);
			} else {
				sectionSelected = false;
			}
			currentChk.setName(new String(Integer.toString(i)));
			chkSections.add(currentChk);
			int requiredDGD = s.getRequiredDGD();
			int requiredLAB = s.getRequiredLAB();
			int requiredTUT = s.getRequiredTUT();
			
			ArrayList<JCheckBox> activities = new ArrayList<JCheckBox>();
			int j = 0;
			for (Activity a : s.getActivities()) {
				//Set the name of tempChk to the full activity name, minus its selected status.
				JCheckBox tempChk = new JCheckBox(a.toString().split(" Selected")[0]);
				tempChk.setEnabled(false);
				tempChk.setSelected(a.getSelected());
				if (sectionSelected) {
				switch (a.getType()) {
				case "DGD":
					if (requiredDGD>0) {
						tempChk.setEnabled(true);
					}
					break;
				case "LAB":
					if (requiredLAB>0) {
						tempChk.setEnabled(true);
					}
					break;
				case "TUT":
					if (requiredTUT>0) {
						tempChk.setEnabled(true);
					}
					break;
				}
				}
				tempChk.setName(i+","+j);
				activities.add(tempChk);
				j++;
			}
			chkActivities.add(activities);
			i++;
		}
		
		//So now we have two lists of checkboxes. One of each section, and one of each of their activities.
		for (int i1=0; i1<chkSections.size(); i1++){
			pane.add(Box.createRigidArea(new Dimension(15, 15))); //Gives us some margins.
			pane.add(chkSections.get(i1));
			chkSections.get(i1).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editCheckBox(e, true);
				}
			});
			for (int j=0; j<chkActivities.get(i1).size(); j++) {
				pane.add(chkActivities.get(i1).get(j));
				chkActivities.get(i1).get(j).addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						editCheckBox(e, false);
					}
				});
			}
		}
		pane.add(Box.createRigidArea(new Dimension(15, 15))); //Gives us some margins.
		editFrame.pack();
		editFrame.setVisible(true);
		editFrame.addWindowListener(this);
	}

	protected void editCheckBox(ActionEvent e, boolean section) {
		JCheckBox sender = (JCheckBox) e.getSource();
		if (section) { //If it's a section, we just need to make sure that there are enough enabled sections.
			//Find the index of the course
			//We set the name of the sender to an integer
			//while iterating through the loop.
			int i = Integer.parseInt(sender.getName());
			Section currSection = courseEditing.getSection(i);
			int enabledSections = 0;
			for (JCheckBox chk : chkSections) {
				if (chk.isSelected()) {
					enabledSections++;
				}
			}
			if (enabledSections == 0) {
				display("You must have at least one section enabled.");
				sender.setSelected(true);
			} else {
				boolean sectionSelected = !currSection.isSelected(); //The new state of the selection.
				currSection.setSelected(sectionSelected);
				//We are now to check for optionals to enable:
				int j = 0;
				int requiredDGD = currSection.getRequiredDGD();
				int requiredLAB = currSection.getRequiredLAB();
				int requiredTUT = currSection.getRequiredTUT();

				for (Activity a : currSection.getActivities()) {
					switch (a.getType()) {
					case "DGD":
						if (requiredDGD>0) {
							chkActivities.get(i).get(j).setEnabled(sectionSelected);
						}
						break;
					case "LAB":
						if (requiredLAB>0) {
							chkActivities.get(i).get(j).setEnabled(sectionSelected);
						}
						break;
					case "TUT":
						if (requiredTUT>0) {
							chkActivities.get(i).get(j).setEnabled(sectionSelected);
						}
						break;
					}
					j++;
				}
				
			}
		} else {
			//So we've got an activity selected.
			int i, j;
			String[] args = sender.getName().split(",");
			//It should be the case where [0] is the
			//index of the section, and [1] is the index
			//of the activity.
			i = Integer.parseInt(args[0]);
			j = Integer.parseInt(args[1]);
			
			//Lets check to see what kind of activity this is.
			//We are assuming that if this checkbox is being toggled,
			//it is because it is enabled. For it to be enabled,
			//it must be an OPTION (ie. DGD, LAB, TUT!)
			Section currSection = courseEditing.getSection(i);
			Activity currActivity = currSection.getActivity(j);
			String type = currActivity.getType();
			int count = 0;
			for (Activity a : currSection.getActivities()) {
				if (a.isSelected() && a.getType().equals(type)) {
					count++;
				}
			}
			//Now we know how many activities are enabled.
			boolean disabling; //We have to check if the user is disabling the box.
			disabling = !sender.isSelected();
			if ((count == 1) && disabling) {
				display("You must have at least one " + type + " enabled.");
				sender.setSelected(true);
			} else {
				currActivity.setSelected(!currActivity.isSelected());
			}
		}
	}

	@Override
	public boolean confirmSemester() {
		//We are changing semesters. Let's set things back to how they should be.
	    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to change semesters? You will lose all settings so far.", "Confirm Change Semester", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
        	txtSearch.setText("");
    		chkOptional.setSelected(false);
    		chkIgnoreExtras.setSelected(false);
    		currSchedules = new ArrayList<Schedule>();
    		currSchedule = 0;
    		clear();
    		btnNext.setEnabled(false);
    		btnLast.setEnabled(false);
    		btnFirst.setEnabled(false);
    		btnPrev.setEnabled(false);
    		
    		return true;
        } else {
        	cboSemester.setSelectedIndex(currSemester);
        	return false;
        }
		
	}
	
	private void display(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Info:", JOptionPane.INFORMATION_MESSAGE);
	}

	//For all document events, we are likely watching the search bar being typed in:
	public void changedUpdate(DocumentEvent e) {
		Object doc = e.getDocument();
		if (doc.equals(txtSearch.getDocument())) {
			updateSearch();
		}
	}
	public void insertUpdate(DocumentEvent e) {
		Object doc = e.getDocument();
		if (doc.equals(txtSearch.getDocument())) {
			updateSearch();
		}
	}
	public void removeUpdate(DocumentEvent e) {
		Object doc = e.getDocument();
		if (doc.equals(txtSearch.getDocument())) {
			updateSearch();
		}
	}
	//Updates the search list box below
	private void updateSearch() {
			String query = txtSearch.getText().toUpperCase();
			if (query.length() > 0) {
				send("SEARCH " + query.toUpperCase());
			} else {
				lstSearchResults.setListData(new String[0]);
			}
	}

	public void courseAdded(String description) {
		//A course was added, so we want to list.
		send("LIST");
	}

	public void courseExists(String description) {
		//A course was not added.
		display("Course " + description + " is already in the list of courses.");
	}

	public void courseNotExists(String description) {
		display("Course " + description + " is not in the list of courses and can't be removed.");
		send("LIST");
	}

	public void courseRemoved(String description) {
		//course was removed, relist them.
		send("LIST");
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object sender = e.getSource();
			if (sender.equals(cboSortOrder)) {
				send("SORTORDER");
			} else if (sender.equals(cboSemester)) {
				if (cboSemester.getSelectedIndex() != currSemester) {
					send("SEMESTER");
				}
			}
		}
	}

	public void schedulesGenerated(int count) {
		if (count==0) {
			currSchedules = new ArrayList<Schedule>();
    		currSchedule = 0;
    		clear();
    		btnNext.setEnabled(false);
    		btnLast.setEnabled(false);
    		btnFirst.setEnabled(false);
    		btnPrev.setEnabled(false);
			display("No conflict-free timetable possible with your current selection!");
		} else {
			System.out.println("Sending display");
			send("DISPLAY");
		}
	}

	public void windowClosing(WindowEvent e) {
		cboSemester.setEnabled(true);
		btnAdd.setEnabled(true);
		txtSearch.setEditable(true);
		chkOptional.setEnabled(true);
		btnClearAll.setEnabled(true);
		btnRemove.setEnabled(true);
		btnIncK.setEnabled(true);
		btnDecK.setEnabled(true);
		chkIgnoreExtras.setEnabled(true);
		cboSortOrder.setEnabled(true);
		btnGenerate.setEnabled(true);
		btnEdit.setEnabled(true);
		lstCourses.setEnabled(true);
		lstOptionalCourses.setEnabled(true);
		lstSearchResults.setEnabled(true);
	}
	public void windowActivated(WindowEvent arg0) {
	}
	public void windowClosed(WindowEvent e) {
	}
	public void windowDeactivated(WindowEvent arg0) {
	}
	public void windowDeiconified(WindowEvent arg0) {
	}
	public void windowIconified(WindowEvent arg0) {
	}
	public void windowOpened(WindowEvent arg0) {
	}

	public void valueChanged(ListSelectionEvent e) {
		JList<String> sender = (JList<String>)e.getSource();
		if (sender.equals(lstCourses) && (lstCourses.getSelectedIndex() > -1)) {
			lstOptionalCourses.clearSelection();
		} else if (sender.equals(lstOptionalCourses) && (lstOptionalCourses.getSelectedIndex() > -1)) {
			lstCourses.clearSelection();
		}
	}

	public void courseNone() {
		display("Cannot generate: no courses selected!");
	}
	

}
