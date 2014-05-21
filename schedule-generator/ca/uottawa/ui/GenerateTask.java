package ca.uottawa.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.uottawa.schedule.Course;
import ca.uottawa.schedule.Schedule;
import ca.uottawa.schedule.ScheduleMessage;

import com.lloseng.ocsf.server.ConnectionToClient;

public class GenerateTask implements Runnable {
	ScheduleMessage message;
	ConnectionToClient client;
	ScheduleGeneratorServer server;

	public GenerateTask(ScheduleMessage message, ConnectionToClient client, ScheduleGeneratorServer server) {
			this.message = message;
			this.client = client;
			this.server = server;
	 }

	public void run() {
		generateSchedules(message, client, server);
	}
	
	private void generateSchedules(ScheduleMessage message, ConnectionToClient client, ScheduleGeneratorServer server) {
		//For timing
		long startTime = System.currentTimeMillis();
		
		//User is generating schedules
    	List<Course> mandatoryCourses = message.getCourses();
        int k = message.getK();
        List<Course> optional = message.getOptionalCourses();
       
        server.display(client.getInfo("ip") + " generates> " + mandatoryCourses.size() +
        		" mandatory. " + k + "/" + optional.size() + " optional.");
        
        String sortOrder = message.getSortOrder();
    	boolean ignoreExtras = message.isIgnoreExtras();
    	List<Schedule> result;
    	if (k>0) {
    		result = Schedule.generateSchedules(mandatoryCourses, optional, k);
    	} else {
    		result = Schedule.generateSchedules(mandatoryCourses);
    	}
    	server.display("[1/3] (" + client.getInfo("ip") + ") Generated " + result.size() + ". Sorting by " + sortOrder + "...");
    	
    	if (result.size() > 0) {
	    	//We should update the schedule stats in preparation of sorting.
	    	for (Schedule s: result) {
	    		s.updateStats();
	    	}
	    	//Now sort
	    	result = Schedule.sort(sortOrder, result, ignoreExtras);
    	}
    	
    	server.updateStats(client.getInetAddress().getHostAddress().toString(), mandatoryCourses.size(), optional.size(), k, result.size());
    	
    	ScheduleMessage schedulesMsg = new ScheduleMessage();
    	schedulesMsg.setCommand("SCHEDULES");
    	
    	if (result.size() > 1000) {
    		List<Schedule> shortResult = result.subList(0, 1000); //Shrink list because it takes a while to transfer.
        	result = new ArrayList<Schedule>();
        	result.addAll(shortResult);
    	}
    	
    	schedulesMsg.setSchedules(result);

		try {
			server.display("[2/3] (" + client.getInfo("ip") + ") Sorted. Sending back " + result.size() + "...");
			client.sendToClient(schedulesMsg);
			long endTime = System.currentTimeMillis();
			server.display("[3/3] (" + client.getInfo("ip") + ") Sent " + result.size() + ", time totaled " + (endTime - startTime) + "ms.");
			server.printCurrentStats();
		} catch (IOException e) {
			e.printStackTrace();
			server.display("Unable to report back generated schedules. Possible connection lost to " + client.getInfo("ip"));
		}
	}

}
