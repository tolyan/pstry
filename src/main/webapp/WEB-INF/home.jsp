<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Task Manager</title>

<style type="text/css">
.wrap {
   width:800px;
   margin:0 auto;
}
.left_col {
   align:right;
   float:left;
   height:30px;
   width:400px;
}
.right_col {
   aligh:left;
   float:right;
   height:30px;
   width:400px;
}
</style>
</head>
<body>
  <h1>Task Manager</h1>


  <table align="center" id='jqGrid'></table>

  <p class="new" align="center">
    Time: <input id="time-selector" class="time-selector">
    Task: <input type="text" class="content" id="taskInput"/>
     <button class="add">Add</button>
  </p>

<div align="center" id="currentInfo">
    <div class="wrap">
        <div class="left_col">
            Current Task:
        </div>
        <div class="right_col" id="cTask" >

        </div>
    </div>
    <div class="wrap">
        <div class="left_col">
            Created at:
        </div>
        <div class="right_col" id="createdAt" >

        </div>
    </div>
    <div class="wrap">
        <div class="left_col">
            Scheduled at:
        </div>
        <div class="right_col" id="scheduledAt" >

        </div>
    </div>
    <div class="wrap">
        <div class="left_col">
            Result:
        </div>
        <div class="right_col" id="result" >

        </div>
    </div>
</div>




  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/themes/redmond/jquery-ui.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/css/ui.jqgrid.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.js"></script>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-timepicker-addon.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/js/jquery.jqgrid.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.3/jquery-ui-sliderAccess.js"></script>
  <script src="${pageContext.request.contextPath}/resources/URI.js"></script>
  <script src="${pageContext.request.contextPath}/resources/moment-with-locales.js"></script>

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
        timeselector.datetimepicker('setDate', (new Date()));
        timeselector.datetimepicker({
            timeFormat: "HH:mm:ss",
            timeInput: true,
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
                    json[i].time = moment(roundSec(new Date(json[i].time))).format();
                    json[i].createdAt = moment(roundSec(new Date(json[i].createdAt))).format();
                }
                $("#jqGrid").jqGrid({
                            data: json,
                            datatype: "local",
                            colNames: [ "ID", "Task", "Created at", "Scheduled at", "Result"],
                            colModel: [
                                { name: "id", width:40 ,height:"auto"},
                                { name: "value", align: "right",height:"auto" },
                                { name: "createdAt",  align: "right" ,height:"auto"},
                                { name: "time",  align: "right" ,height:"auto"},
                                { name: "result",  align: "right" ,height:"auto"}
                            ],
                            rowNum:7,
                            rownumbers:true,
                            viewrecords: true,
                            gridview: true,
                            autoencode: true,
                            autowidth: true,
                            caption: "Recent Tasks"
                        });
        });

        $('.add').click(function(e){
            var time = timeselector.datetimepicker('getDate');
            var checkTime = new Date(time);

            var curr = new Date();
            var allowed = new Date(curr.getTime() + 5*60000);
            if (time < curr){
                alert("Can not set task in the past");
                return;
            }
            var value = $('.new .content').val();
            if (value.length > 20) {
                alert("Task length exceeded maximum.\n Current maxim length is 20.");
                return;
            } else if (checkTime > allowed) {
                alert("Invalid schedule time. \n5 minute range in future is allowed.");
                return;
            }
            $('#taskInput').val("");
            var package = JSON.stringify({"time": time,"value": value, "createdAt": roundSec(curr)});
            console.log("POSTING");
            console.log(package);
            $.ajax({
                url: "/taskmanager/task",
                type: "POST",
                contentType: "application/json",
                data: package,
                success: function(data, textStatus, request){
                    loc = request.getResponseHeader('location');
                    file = URI(loc).filename();
                    raw = JSON.parse(package);
                    renderTask(raw);
                    stompClient.subscribe("/topic/result/".concat(file), notification);
                    console.log("Scheduled task with id: ".concat(file));
                },
                statusCode: {
                    400: function (response) {
                        alert(response.body);
                    },
                    503: function (response) {
                        alert("Service unavailable");
                    }
                },
                error: function(jqXHR, textStatus, errorThrown ){
                    console.log("Error for string ".concat(textStatus)
                                .concat(", ").concat(errorThrown));
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
               data[i].time = moment(new Date(data[i].time)).format();
               data[i].createdAt = moment(new Date(data[i].createdAt)).format();
           }
           p.data = data;
           $grid.trigger("reloadGrid", [{current: true}]);
        });
    }




    // Render task data from server into HTML, registered as callback
    // when subscribing to task topic
    function renderTask(raw) {
      $('.right_col').empty();
      $('#cTask').append(raw.value);
      creDat = roundSec(new Date(raw.createdAt));
      $('#createdAt').append(moment(creDat).format());
      schDat = roundSec(new Date(raw.time));
      $('#scheduledAt').append(moment(schDat).format());
    }

    function notification(raw) {
        json = JSON.parse(raw.body);
        console.log('<<< GOT message');
        console.log(json.result);
        $('.right_col').empty();
        $('#cTask').append(json.value);
        creDate = roundSec(new Date(json.createdAt));
        $('#createdAt').append(moment(creDate).format());
        schDate = roundSec(new Date(json.time));
        $('#scheduledAt').append(moment(schDate).format());
        $('#result').append(json.result);
    }

    function roundSec(date) {
        return new Date(Math.round(date.getTime()/1000)*1000);
    }


  </script>
</body>
</html>
