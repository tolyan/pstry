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


  <p id="cTask">Current Task: </p>
  <p id="createdAt">Created at: </p>
  <p id="scheduledAt">Scheduled at: </p>
  <p id="Result">Result: </p>

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
  <script src="${pageContext.request.contextPath}/resources/URI.js"></script>

  <script>
  //Create stomp client over sockJS protocol
  var socket = new SockJS("/taskmanager/ws");
  var stompClient = Stomp.over(socket);

  // Callback function to be called when stomp client is connected to server
  var connectCallback = function() {
    stompClient.subscribe('/topic/tasks', updateGrid);
  };

  // Callback function to be called when stomp client could not connect to server
  var errorCallback = function(error) {
    console.log(error);
    alert("Service unavailable.");
  };

  // Connect to server via websocket
  stompClient.connect("guest", "guest", connectCallback, errorCallback);

    $( document ).ready(function() {
        var curr = new Date();
        var allowed = new Date(curr.getTime() + 5*60000);
        var timeselector = $('.time-selector');
        timeselector.datetimepicker({
            timeFormat: "HH:mm:ss",
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
                                { name: "value", width: 200, align: "right",height:"auto" },
                                { name: "createdAt", width: 350, align: "right" ,height:"auto"},
                                { name: "time", width: 350, align: "right" ,height:"auto"},
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
            var package = JSON.stringify({ "time": time,"value": value});
            $.ajax({
                url: "/taskmanager/task",
                type: "POST",
                contentType: "application/json",
                data: package,
                success: function(data, textStatus, request){
                    loc = request.getResponseHeader('location');
                    file = URI(loc).filename();
                    stompClient.subscribe("topic/result/".concat(file), renderTask);
                    console.log("Scheduled task with id: ".concat(file));
                },
                error: function(jqXHR, textStatus, errorThrown ){
                    console.log("Error for string ".concat(textStatus)
                                .concat(", ").concat(errorThrown));
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
           for(var i in data)
           {
               data[i].time = new Date(data[i].time);
               data[i].createdAt = new Date(data[i].createdAt);
           }
           p.data = data;
           $grid.trigger("reloadGrid", [{current: true}]);
        });
    }




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



  </script>
</body>
</html>
