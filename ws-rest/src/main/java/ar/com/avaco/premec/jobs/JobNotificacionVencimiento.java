package ar.com.avaco.premec.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

import ar.com.avaco.premec.service.NotificacionVencimientoService;

@Service
public class JobNotificacionVencimiento implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		NotificacionVencimientoService notificacionVencimientoService = (NotificacionVencimientoService) context.getJobDetail().getJobDataMap().get("notificacionVencimientoService");
		notificacionVencimientoService.enviarNotificaciones();
		
	}


}
