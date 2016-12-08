<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Task Manager</title>
</head>
<body>
  <h1>Task Manager</h1>

  <table id='jqGrid'></table>

  <div class="time-selector"></div>
  <p><span class="time-span"></span></p>
  <p class="new">
    Task: <input type="text" class="content"/>
     <button class="add">Add</button>
  </p>


  <p>Current Task: </p>
  <p>Created at: </p>
  <p>Scheduled at: </p>
  <p>Result: </p>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/themes/redmond/jquery-ui.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/css/ui.jqgrid.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/jqCron.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.js"></script>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/js/jquery.jqgrid.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-sliderAccess.js"></script>
  <script src="${pageContext.request.contextPath}/resources/jqCron.js"></script>
  <script src="${pageContext.request.contextPath}/resources/jqCron.en.js"></script>

  <script>



    $( document ).ready(function() {
        var curr = new Date();
        var allowed = new Date(curr.getTime() + 5*60000);
        var timeselector = $('.time-selector');
        timeselector.datetimepicker({
            timeFormat: "hh:mm:ss",
            hour: curr.getHours(),
            minute: curr.getMinutes(),
            second: curr.getSeconds(),
            minTime: curr,
            maxTime: allowed
        });


        $.ajax({
            url: "/taskmanager/task/latest"
        }).then(function(data) {
                var json = data;
                for(var i in json)
                {
                    json[i].time = new Date(json[i].time);
                    json[i].createdAt = new Date(json[i].createdAt);
                }
                $("#jqGrid").jqGrid({
                            data: json,
                            datatype: "local",
                            colNames: [ "ID", "Task", "Created at", "Scheduled at", "Result"],
                            colModel: [
                                { name: "id", width:40 ,height:"auto"},
                                { name: "value", width: 350, align: "right",height:"auto" },
                                { name: "createdAt", width: 200, align: "right" ,height:"auto"},
                                { name: "time", width: 200, align: "right" ,height:"auto"},
                                { name: "result", width: 200, align: "right" ,height:"auto"}
                            ],
                            rowNum:7,
                            rownumbers:true,
                            viewrecords: true,
                            gridview: true,
                            autoencode: true,
                            caption: "Recent Tasks"
                        });
        });

        $('.add').click(function(e){
            var time = timeselector.datetimepicker('getDate');
            var curr = new Date();
            var allowed = new Date(time + 5*60000);
            var value = $('.new .content').val();
            if (value.length > 20) {
                alert("Task length exceeded maximum.\n Current maxim length is 20.");
                return false;
            } else if (time < allowed.getTime() && time < curr) {
                alert("Ivalid schedule time. \n5 minute range in future is allowed only.");
                return false;
            }
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

  </script>
</body>
</html>
