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
    Execution time: <input type="text" class="time"/>
    <button class="add">Add</button>
  </p>

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/themes/redmond/jquery-ui.min.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/css/ui.jqgrid.min.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.1.1/sockjs.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/free-jqgrid/4.13.5/js/jquery.jqgrid.min.js"></script>
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
      $('.add').click(function(e){
        e.preventDefault();
        var value = $('.new .value').val();
        var id = Number($('.new .id').val());
        var jsonstr = JSON.stringify({ 'id': id,'value': value });
        stompClient.send("/app/addTask", {}, jsonstr);
        return false;
      });
    });

    function updateGrid(data) {
        alert(data.body);
        var $grid = $("#jqGrid"), // the grid
        p = $grid.jqGrid("getGridPagam");

        p.data = data.body;
        $grid.trigger("reloadGrid", [{current: true}]);
    }

  </script>
</body>
</html>
