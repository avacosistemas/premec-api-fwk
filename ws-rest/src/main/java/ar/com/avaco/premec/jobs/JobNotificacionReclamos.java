package ar.com.avaco.premec.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.service.NotificacionReclamoService;

@Service
public class JobNotificacionReclamos implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		NotificacionReclamoService notificacionReclamoService = (NotificacionReclamoService) context.getJobDetail().getJobDataMap().get("notificacionReclamoService");
		notificacionReclamoService.enviarNotificaciones();
		
	}


}
