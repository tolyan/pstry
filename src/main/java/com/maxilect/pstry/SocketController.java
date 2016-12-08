package com.maxilect.pstry;

import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import oracle.jdbc.OracleConnection;
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
import java.sql.SQLException;
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

    private void updateTaskAndBroadcast() {
        template.convertAndSend("/topic/tasks", tasks);
    }

    /**
     * Handler to add one task
     */
    @MessageMapping("/addTask")
    public void addStock(Task task) throws Exception {
        //TODO add proper logic
//    tasks.add(new Task(1l, "taskvalue", new Date(), new Date()));
        tasks = taskMapper.getLatestTasks(7);
        updateTaskAndBroadcast();
    }


    /**
     * Serve the main page
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "home";
    }

    @PostConstruct
    public void registerNotification() throws SQLException {
        OracleConnection connection = null;
        DatabaseChangeRegistration changeRegistration = null;
        try {
            connection = (OracleConnection) ds.getConnection();
            changeRegistration = connection.registerDatabaseChangeNotification(buildProperties());
            changeRegistration.addListener(new DatabaseChangeListener() {
                @Override
                public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
                    //do main staff here
                }
            });
        } catch (SQLException e) {
            if (connection != null)
                connection.unregisterDatabaseChangeNotification(changeRegistration);
            throw new RuntimeException(e);
        }
    }

    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
        properties.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");
        return properties;
    }

}
