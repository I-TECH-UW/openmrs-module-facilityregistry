package org.openmrs.module.facilityregistry.task;

import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class FacilityRegistryTaskTest extends BaseModuleContextSensitiveTest {
	
	private SchedulerService schedulerService;
	
	@Before
	public void init() throws Exception {
		schedulerService = Context.getSchedulerService();
	}
	
	@Test(expected = None.class)
	public void shedulerShouldSheduleFacilityRegistryTask() throws Exception {
		String taskClassName = "org.openmrs.module.facilityregistry.task.FacilityRegistryTask";
		TaskDefinition facilitRegistryTask = getFacilityRegistryTask(taskClassName);
		facilitRegistryTask = getFacilityRegistryTask(taskClassName);
		schedulerService.scheduleTask(facilitRegistryTask);
		schedulerService.shutdownTask(facilitRegistryTask);
	}
	
	@Test(expected = ValidationException.class)
	public void shedulerShouldNotSheduleWrongFacilityRegistryTaskName() throws Exception {
		String worngTaskClassName = "org.openmrs.module.facilityregistry.wrong.FacilityRegistryTask";
		TaskDefinition facilitRegistryTask = getFacilityRegistryTask(worngTaskClassName);
		schedulerService.scheduleTask(facilitRegistryTask);
		schedulerService.shutdownTask(facilitRegistryTask);
	}
	
	private TaskDefinition getFacilityRegistryTask(String taskClassName) {
		TaskDefinition taskDef = new TaskDefinition();
		taskDef.setTaskClass(taskClassName);
		taskDef.setStartOnStartup(false);
		taskDef.setStartTime(null);
		taskDef.setName("Facility Registry Task Definition");
		long repeatInterval = 30000;
		taskDef.setRepeatInterval(repeatInterval);
		schedulerService.saveTaskDefinition(taskDef);
		return taskDef;
	}
	
}
