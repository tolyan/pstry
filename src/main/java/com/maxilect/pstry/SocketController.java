package com.maxilect.pstry;

import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@Controller
public class SocketController {

    final static Logger logger = Logger.getLogger(SocketController.class);
    @Autowired
    private SimpMessagingTemplate template;
    private TaskScheduler scheduler = new ConcurrentTaskScheduler();
    private List<Task> tasks = new ArrayList<Task>();
    private Random rand = new Random(System.currentTimeMillis());
    @Autowired
    private BasicDataSource ds;

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ResultMapper resultMapper;

    private void broadcastChange() {
        tasks = taskMapper.getLatestTasks(7);
        template.convertAndSend("/topic/tasks", tasks);
    }

    /**
     * Handler to add one task
     */
    @MessageMapping("/addTask")
    public void addTask(Task task) throws Exception {
        taskMapper.addTask(task, task.getValue(), task.getTime(), task.getCreatedAt());
        taskMapper.submitTaskForBackgroundExecution(task.getId());
        broadcastChange();
    }


    /**
     * Serve the main page
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "home";
    }

    @PostConstruct
    public void registerNotification() {
        OracleConnection connection = null;
        DatabaseChangeRegistration changeRegistration = null;
        Properties conProp = new Properties();
        conProp.setProperty("user", ds.getUsername());
        conProp.setProperty("password", ds.getPassword());
        OracleDriver driver = new OracleDriver();
        try {
            logger.debug("TRY: " + ds.getUrl() + ":" + ds.getUsername() + ":" + ds.getPassword());
            connection = (OracleConnection) driver.connect(ds.getUrl(), conProp);
            changeRegistration = connection.registerDatabaseChangeNotification(buildProperties());
            changeRegistration.addListener(new DatabaseChangeListener() {
                @Override
                public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
                    broadcastChange();
                }
            });

            Statement stm = connection.createStatement();
            ((OracleStatement) stm).setDatabaseChangeRegistration(changeRegistration);
            logger.debug("RUNNING STATEMENT");
            stm.setQueryTimeout(1);
            ResultSet rs = stm.executeQuery("select * from result");
            while (rs.next()) {
            }
            String[] tables = changeRegistration.getTables();
            for (int i = 0; i < tables.length; i++) {
                logger.debug("Registred table: " + tables[i]);
            }
            logger.debug("CLOSING SET");
            rs.close();
            logger.debug("CLOSING STATE");
            stm.close();

        } catch (SQLException e) {
            logger.warn("Couldn't register notificator " + e.getMessage());
            if (connection != null)
                try {
                    connection.unregisterDatabaseChangeNotification(changeRegistration);
                } catch (SQLException e1) {
                    logger.warn(e1.getMessage());
                } finally {
                    throw new RuntimeException(e);
                }
        }
    }

    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
        properties.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");
        return properties;
    }

}
