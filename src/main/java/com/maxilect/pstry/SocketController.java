package com.maxilect.pstry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


import com.maxilect.pstry.dao.ResultMapper;
import com.maxilect.pstry.dao.TaskMapper;
import oracle.jdbc.OracleConnection;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.log4j.Logger;

@Controller
public class SocketController {

  @Autowired private SimpMessagingTemplate template;  
  private TaskScheduler scheduler = new ConcurrentTaskScheduler();
  private List<Task> tasks = new ArrayList<Task>();
  private Random rand = new Random(System.currentTimeMillis());
  final static Logger logger = Logger.getLogger(SocketController.class);

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

  public void registerNotification(){
      String url = ds.getUrl();
      String user = ds.getUsername();
      String pwd = ds.getPassword();

  }

}
