package com.sf.biocapture;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import nw.orm.core.service.Nworm;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.email.EmailThread;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class StartupListener extends BsClazz{
	
    private ScheduledExecutorService emailThreadPool;

    @PostConstruct
    public void start(){
        logger.debug("Waking up Biocapture Services");
        Properties props = new Properties();
        props.setProperty("config.name", "hibernate.cfg.xml");
        Nworm dbService = Nworm.getInstance();
        dbService.enableJTA();
        dbService.enableSessionByContext();
        logger.debug("Biocapture Services is awake");

        System.out.println("Starting email thread listeners");
        this.emailThreadPool = Executors.newScheduledThreadPool(2);
        startEmailThreads();
    }

    @PreDestroy
    public void stop() throws InterruptedException{
        Nworm.getInstance().closeFactory();
        logger.debug("Goodbye Bio Services");
            
        this.emailThreadPool.shutdownNow();
        this.emailThreadPool.awaitTermination(10L, TimeUnit.SECONDS);
    }

    private void startEmailThreads() {
        this.emailThreadPool.scheduleWithFixedDelay(new EmailThread(), 1L, 1L, TimeUnit.MINUTES);
    }
    
    public static void main(String[] args) {
            System.out.println(Boolean.valueOf("true"));
    }

}
