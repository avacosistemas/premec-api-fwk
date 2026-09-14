package ar.com.avaco.premec.service;

import org.quartz.SchedulerException;

public interface PremecJobService {

	String startStopEnvioFormularioSapJob() throws SchedulerException;

	String startStopReporteDiarioJob() throws SchedulerException;

	boolean isEnvioFormularioSapJobRunning() throws SchedulerException;

	boolean isReporteDiarioJobRunning() throws SchedulerException;

}
