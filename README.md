##Задача
Спроектировать и разработать приложение, которое посредством Web-интерфейса получает
данные от пользователя. Данные передаются в backend по HTTP restful API интерфейсу. Backend
осуществляет непосредственное взаимодействие с БД. Данные сохраняются в БД в виде заказа на
обработку и в заданный момент времени обрабатываются процессом, запущенным на сервере БД.
Обработка заключается в конкатенации строки, переданной пользователем со строками из 3х
предыдущих заказов. О завершении операции обработки данных пользователь должен быть
оповещен в Web-интерфейсе не позднее, чем через 5 секунд после готовности. Помимо отправки
данных на обработку пользователь должен иметь возможность просматривать в Web-интерфейсе
историю из семи последних заказов на обработку. Необходимо обеспечить работоспособность
приложения при возможных отказах на всех трех узлах.
Приложение должно выполнять валидацию входных данных на их соответствие указанным ниже
ограничениям. При несоответствии ограничениям, создание заказа невозможно.

###Передаваемые в Web-интерфейс данные
* Строка (ограничение – от 1 до 20 символов) - входная строка для обработки.
* Дата и время с точностью до секунд (ограничение – от текущего момента до +5 минут) -время, когда необходимо выполнить обработку заказа.

###Просматриваемая информация по каждому из заказов
* Дата создания заказа.
* Дата, указанная при создании заказа.
* Входная строка.
* Результат обработки – строка, которая является конкатенацией входной строки с
входными строками из трех предыдущих заказов. 

##Описание Решения.
###Общее архитектурное решение.
Приложение состоит из 3 компонентов: клиентская часть, REST API/Websocket
сервлет, сервер БД. Клиентская часть выполнена с использованием библиотеки Jquery
и генерируется в клиентском браузере. Сервлет написан с использованием Spring
MVC и обеспечивает передачу данных между клиентской частью и БД. База данных
хранит данные и обрабатывает их согласно бизнес-логике, отображённой в хранимых
процедурах PL/SQL.
###Стандартный жизненный цикл​ .
Клиентская часть отправляет HTTP POST запрос для регистрации задачи.
Сервлет принимает и валидирует REST запрос, после успешной валидации сервер
сохраняет данные в БД, регистрирует работу в Oracle Job Scheduler и отправляет
локацию задачи обратно клиентской части(заголовок Location в HTTP Response).
Клиентская часть получает идентификатор задачи и регистрируется с ним в очереди
сообщений через Websocket.
При инициализации сервлета с помощью Oracle JDBC запускается демон,
который слушает изменения в таблице с результатами выполнения работ (Oracle
Database Change Notification), уведомляет сервлет о выполненной работе и
возвращает требуемое значение. Сервлет отсылает в очередь сообщений данные по
адресу соответствующему идентификатору задачи. Клиент получает данные и
отображает значения полей для задачи и результата.
Так же при запуске клиентская часть подписывается на широковещательную очередь
и получает данные при каждом добавлении задачи на сервлете, после этого она
делает REST запрос к сервлету, получает список из 7 последних задач и отображает
его.
###Обоснование решения.
В данной задаче наибольшая нагрузка приходится на генерацию пользовательского
интерфейса и выполнение бизнес-логики (конкатенация строк в нашем случае). Наше
решение позволяет всю нагрузку по генерации пользовательского интерфейса
перенести на машины пользователей, при этом использование websocket даёт
возможность отказаться от периодического опрашивания сервера, что дополнительно
снижает нагрузку на сервер. Нагрузка по выполнению бизнес-логики ложится на
сервер БД, и здесь также использование механизма Database Change Notification
позволяет отказаться от периодического опрашивания базы данных и сократить
нагрузку на БД. В итоге сервлет выполняет только диспетчерскую функцию, при этом
он не хранит состояния системы и может свободно реплицироваться, например, с
помощью распределения нагрузки через DNS.


##English version
###Task
Design and develop application that recieves data from user via Web-interface. Data should be transfered via HTTP RESTfull API. Backend communicates with with RDBMS. Data are stored in RDBMS in the form of processing order and should be consumed by background process on RDBMS server.

Data processing should result in concatenation of string passed by user with 3 string from previous orders. User should be notified about result of processing via Web-interface in 5 seconds after processing end. Besides order sending user should have posibility to view history of 7 previous orders. 

Application should validate input data according rules below, order creation is impossible in case of invalid data.

####Data validation rules
* String - 1 to 20 symbols.
* Date and time of order proccessing - 0-5 minutes from date and time of order creation.

####Data view
* Date of order creation
* Date of order proccessing
* Input string
* Result of proccessing

###Solution
Application consist of 3 tiers: client, REST API/Websocket servlet, RDBMS. Client part is developed with Jquery library and is generated by client browser. Servlet is developed with Spring MVC and transfers data between client part and RDBMS. RDBMS stores data and processes it according to business logic in PL/SQL stored procedures.

####Application life cycle
Client sends HTTP POST request for order registration. Servlet recieves and validates REST request, after succesfull validation servlet saves data in DB, registers job in Oracle Job Scheduler and sends order location back to client part inside Location header of HTTP Response. Client recieves order identificator and registers with it in message queue via Websocket.
During servlet initialization demon process is started on side of Oracle DB, that demon process listens to changes inside table containing results of order processes (Oracle Database Change Notification) and informs servlet about order processing results. In this case servlet sends data with corresponding order identificator to message queue. Client recieves these data and displays it to user.
Also, client part subsribes to broadcast queue and recieves notification on every order update on servlet, after that client send REST request to servlet about 7 latest orders and displays it to user. 

####Solution rationale
This task puts most of the load on user interface generation and implementation of business logic (strings concatenation in our case). Our solution allows to transfer load of user interface generation to client machines and usage of websocket gives us opportunity to refrain from server polling and reduce server load even more. Load on implementation of business logic is transfered to RDBMS server and usage of Database Change Notification allows to reduce DB server polling as well. In result our servlet is fully stateless and used only to dispatch requests from client to RDBMS, thus can be easily replicated with load share via DNS, for instance.
