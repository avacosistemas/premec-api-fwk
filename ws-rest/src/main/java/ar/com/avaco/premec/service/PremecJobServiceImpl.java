package ar.com.avaco.premec.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.jobs.JobEnvioFormularioSap;
import ar.com.avaco.premec.jobs.JobNotificacionReclamos;
import ar.com.avaco.premec.jobs.JobNotificacionVencimiento;
import ar.com.avaco.premec.jobs.JobReporteDiario;
import ar.com.avaco.premec.ws.service.ActividadEPService;
import ar.com.avaco.premec.ws.service.FormularioEPService;
import ar.com.avaco.premec.ws.service.ReporteEPService;

@Service("premecJobService")
public class PremecJobServiceImpl implements PremecJobService {

	private static final JobKey ENVIO_FORMULARIO_SAP_JOB_KEY = new JobKey("envioFormularioSapJob",
			"envioFormularioSapGroup");

	private static final JobKey REPORTE_DIARIO_JOB_KEY = new JobKey("reporteDiarioJob", "reporteDiarioGroup");

	@Value("${cron.envioFormularioSap}")
	private String cronEnvioFormularioSap;

	@Value("${cron.reporteDiario}")
	private String cronReporteDiario;
	
	@Value("${cron.notificacionReclamos}")
	private String cronNotificacionReclamos;

	@Value("${cron.notificacionVencimiento}")
	private String cronNotificacionVencimiento;

	@Value("${cron.inicar}")
	private boolean iniciar;

	private static Scheduler scheduler;

	private ActividadEPService actividadEPService;

	private ReporteEPService reporteEPService;

	private FormularioEPService formularioEPService;

	@Autowired
	private NotificacionReclamoService notificacionReclamoService;
	
	@Autowired
	private NotificacionVencimientoService notificacionVencimientoService;
	
	private static final Logger LOGGER = Logger.getLogger(PremecJobService.class);

	@PostConstruct
	public void initScheduler() throws SchedulerException {

		if (scheduler == null) {

			LOGGER.debug("Inicio generacion procesos automaticos");

			scheduler = new StdSchedulerFactory().getScheduler();

			JobDataMap newJobDataMap = new JobDataMap();
			newJobDataMap.put("actividadEPService", actividadEPService);
			newJobDataMap.put("reporteEPService", reporteEPService);
			newJobDataMap.put("formularioEPService", formularioEPService);
			newJobDataMap.put("notificacionReclamoService", notificacionReclamoService);
			newJobDataMap.put("notificacionVencimientoService", notificacionVencimientoService);

			JobDetail jobReporteDiario = JobBuilder.newJob(JobReporteDiario.class)
					.withIdentity("reporteDiarioJob", "reporteDiarioGroup").usingJobData(newJobDataMap).build();
			Trigger triggerJobReporteDiario = TriggerBuilder.newTrigger()
					.withIdentity("reporteDiarioTrigger", "reporteDiarioGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronReporteDiario)).build();
			scheduler.scheduleJob(jobReporteDiario, triggerJobReporteDiario);

			
			
			JobDetail jobNotificacionReclamos = JobBuilder.newJob(JobNotificacionReclamos.class)
					.withIdentity("notificacionReclamosJob", "notificacionReclamosGroup").usingJobData(newJobDataMap).build();
			Trigger triggerNotificacionReclamos = TriggerBuilder.newTrigger()
					.withIdentity("notificacionReclamosTrigger", "notificacionReclamosGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronNotificacionReclamos)).build();
			scheduler.scheduleJob(jobNotificacionReclamos, triggerNotificacionReclamos);

			
			JobDetail jobNotificacionVencimientos = JobBuilder.newJob(JobNotificacionVencimiento.class)
					.withIdentity("notificacionVencimientoJob", "notificacionVencimientoGroup").usingJobData(newJobDataMap).build();
			Trigger triggerNotificacionVencimiento = TriggerBuilder.newTrigger()
					.withIdentity("notificacionVencimientoTrigger", "notificacionVencimientoGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronNotificacionVencimiento)).build();
			scheduler.scheduleJob(jobNotificacionVencimientos, triggerNotificacionVencimiento);
			
			

			JobDetail jobEnvioFormularioSap = JobBuilder.newJob(JobEnvioFormularioSap.class)
					.withIdentity("envioFormularioSapJob", "envioFormularioSapGroup").usingJobData(newJobDataMap)
					.build();
			Trigger triggerJobEnvioFormularioSap = TriggerBuilder.newTrigger()
					.withIdentity("envioFormularioSapTrigger", "envioFormularioSapGroup")
					.withSchedule(CronScheduleBuilder.cronSchedule(cronEnvioFormularioSap)).build();
			scheduler.scheduleJob(jobEnvioFormularioSap, triggerJobEnvioFormularioSap);
			


			scheduler.start();
			if (!iniciar)
				scheduler.pauseAll();
		}
	}

	public String startStopEnvioFormularioSapJob() throws SchedulerException {
		return startStopJob(ENVIO_FORMULARIO_SAP_JOB_KEY);
	}

	@Override
	public String startStopReporteDiarioJob() throws SchedulerException {
		return startStopJob(REPORTE_DIARIO_JOB_KEY);
	}

	private String startStopJob(JobKey jobkey) throws SchedulerException {
		String ret = "";
		if (isJobRunning(jobkey)) {
			scheduler.pauseJob(jobkey);
			ret = "Pausado";
		} else {
			scheduler.resumeJob(jobkey);
			ret = "Iniciado";
		}
		return ret;
	}

	@Override
	public boolean isEnvioFormularioSapJobRunning() throws SchedulerException {
		return isJobRunning(ENVIO_FORMULARIO_SAP_JOB_KEY);
	}

	@Override
	public boolean isReporteDiarioJobRunning() throws SchedulerException {
		return isJobRunning(REPORTE_DIARIO_JOB_KEY);
	}

	private boolean isJobRunning(JobKey jobkey) throws SchedulerException {
		JobDetail jobDetailReporteDiario = scheduler.getJobDetail(jobkey);
		List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetailReporteDiario.getKey());
		for (Trigger trigger : triggers) {
			TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
			if (TriggerState.NORMAL.equals(triggerState)) {
				return true;
			}
		}
		return false;
	}

	@Resource(name = "reporteEPService")
	public void setReporteEPService(ReporteEPService reporteEPService) {
		this.reporteEPService = reporteEPService;
	}

	@Resource(name = "actividadService")
	public void setActividadEPService(ActividadEPService actividadEPService) {
		this.actividadEPService = actividadEPService;
	}

	@Resource(name = "formularioEPService")
	public void setFormularioEPService(FormularioEPService formularioEPService) {
		this.formularioEPService = formularioEPService;
	}

	public void setCronEnvioFormularioSap(String cronEnvioFormularioSap) {
		this.cronEnvioFormularioSap = cronEnvioFormularioSap;
	}

	public void setCronReporteDiario(String cronReporteDiario) {
		this.cronReporteDiario = cronReporteDiario;
	}

	public void setIniciar(boolean iniciar) {
		this.iniciar = iniciar;
	}

}
