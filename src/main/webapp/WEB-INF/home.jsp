<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Task Manager</title>
</head>
<body>
  <h1>Task Manager</h1>

  <table id='jqGrid'></table>

  <p class="new">
    Content: <input type="text" class="content"/>
    Execution time: <input type="text" data-timeEntry="show24Hours: true, showSeconds: true, showminTime: 'new Date(0, 0, 0, 8, 30, 0)'"/>
    <button class="add">Add</button>
  </p>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/themes/redmond/jquery-ui.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/css/ui.jqgrid.min.css">
  <link rel="stylesheet" type="text/css" href="css/jquery.timeentry.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/js/jquery.jqgrid.min.js"></script>
  <script type="text/javascript" src="js/jquery.plugin.js"></script>
  <script type="text/javascript" src="js/jquery.timeentry.js"></script>
  <script>

    $( document ).ready(function() {
        $.ajax({
            url: "/taskmanager/task/latest"
        }).then(function(data) {
                var json = data;
                $("#jqGrid").jqGrid({
                            data: json,
                            datatype: "local",
                            colNames: [ "id", "value", "timeStr"],
                            colModel: [
                                { name: "id", width:300 ,height:"auto"},
                                { name: "value", width: 150, align: "right",height:"auto" },
                                { name: "timeStr", width: 100, align: "right" ,height:"auto"}
                            ],
                            rowNum:7,
                            rownumbers:true,
                            viewrecords: true,
                            gridview: true,
                            autoencode: true,
                            caption: "Recent Tasks"
                        });
        });
    });


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
      stompClient.subscribe('/topic/tasks', updateGrid);
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
      $('#time').timeEntry();

      $('.add').click(function(e){
            var value = $('.new .content').val();
            var time = $('.new .time').val();
            $.ajax({
                    url: "/taskmanager/task",
                    type: "POST",
                    contentType: "application/json",
                    dataType: "json",
                    data: JSON.stringify({ 'time': time,'value': value}),
                    success: function(){
                        alert("Task scheduled.");
                    },
                    error: function(){
                        alert("Service unavailable.");
                    }
                });
      });
    });

    function updateGrid() {
        $.ajax({
            url: "/taskmanager/task/latest"
        }).then(function(data) {
           var $grid = $("#jqGrid"),
           p = $grid.jqGrid("getGridParam");
           p.data = data;
           $grid.trigger("reloadGrid", [{current: true}]);
        });


    }

  </script>
</body>
</html>
