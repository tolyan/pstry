package com.maxilect.pstry;

import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import com.maxilect.pstry.validator.TaskValidator;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Controller
public class SocketController {

    final static Logger logger = Logger.getLogger(SocketController.class);
    @Autowired
    private SimpMessagingTemplate template;
    private List<Task> tasks = new ArrayList<Task>();
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
        Date createdAt = new Date();
        if (task != null) {
            task.setCreatedAt(createdAt);
        }
        if (!TaskValidator.isTaskValid(task)) {
            logger.debug("BAD SOCKET REQUEST: " + task);
            throw new IllegalArgumentException("Bad value, must have length between 1 and 20");
        }
        taskMapper.addTask(task, task.getValue(), task.getTime(), createdAt);
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

            logger.debug(new StringBuilder("Connecting to DB: ")
                    .append(ds.getUrl()).append(":")
                    .append(ds.getUsername()).append(":")
                    .append(ds.getPassword()));
            connection = (OracleConnection) driver.connect(ds.getUrl(), conProp);
            changeRegistration = connection.registerDatabaseChangeNotification(buildProperties());
            changeRegistration.addListener(new DatabaseChangeListener() {
                @Override
                public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
                    logger.debug("NOTIFICATION EVENT: " + databaseChangeEvent.toString());
                    for (QueryChangeDescription query : databaseChangeEvent.getQueryChangeDescription()) {
                        logger.debug("Row descriptions: " + query.toString());
                        for (TableChangeDescription tab : query.getTableChangeDescription()) {
                            for (RowChangeDescription row : tab.getRowChangeDescription()) {
                                Long rowId = parseRowIdAsLong(row);
                                logger.debug("ROW_ID: " + rowId);

                            }
                        }
                    }
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
            logger.debug("CLOSING STATEMENT");
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

    private Long parseRowIdAsLong(RowChangeDescription row) {
        //NOTE RowId has a form like AAAE5OAABAAAK/pAAH
        // last 3 symbols represent row identifier, encoded in base64
        String rowIdAsString = row.getRowid().stringValue();
        String rowIdPart = rowIdAsString.substring(rowIdAsString.length() - 3, rowIdAsString
                .length());
        byte[] rowIdAsBytes = Base64Utils.decodeFromString("A" + rowIdPart);//NOTE append A to make a valid base64 str
        String rowIdAsHex = DatatypeConverter.printHexBinary(rowIdAsBytes);
        return Long.valueOf(rowIdAsHex);
    }

    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
        properties.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");
        return properties;
    }

}
