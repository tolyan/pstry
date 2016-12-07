<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Task Manager</title>
</head>
<body>
  <h1>Task Manager</h1>

  <table>
    <thead><tr><th>Id</th><th>Content</th><th>Time</th></tr></thead>
    <tbody id="task"></tbody>
  </table>

  <p class="new">
    Content: <input type="text" class="content"/>
    Execution time: <input type="text" class="time"/>
    <button class="add">Add</button>
  </p>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script>
    //Create stomp client over sockJS protocol
    var socket = new SockJS("/taskmanager/ws");
    var stompClient = Stomp.over(socket);

    // Render task data from server into HTML, registered as callback
    // when subscribing to task topic
    function renderTask(frame) {
      var tasks = JSON.parse(frame.body);
      $('#task').empty();
      for(var i in tasks) {
        var task = tasks[i];
        $('#task').append(
          $('<tr>').append(
            $('<td>').html(task.id),
            $('<td>').html(task.value),
            $('<td>').html(task.time)
          )
        );
      }
    }
    
    // Callback function to be called when stomp client is connected to server
    var connectCallback = function() {
      stompClient.subscribe('/topic/tasks', renderTask);
    }; 

    // Callback function to be called when stomp client could not connect to server
    var errorCallback = function(error) {
      alert(error.headers.message);
    };

    // Connect to server via websocket
    stompClient.connect("guest", "guest", connectCallback, errorCallback);
    
    // Register handler for add button
    $(document).ready(function() {
      $('.add').click(function(e){
        e.preventDefault();
        var value = $('.new .value').val();
        var id = Number($('.new .id').val());
        var jsonstr = JSON.stringify({ 'id': id,'value': value });
        stompClient.send("/app/addTask", {}, jsonstr);
        return false;
      });
    });

  </script>
</body>
</html>
