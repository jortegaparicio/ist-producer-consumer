
		Servicio de Publisher-Subscriber concurrente con ActiveMQ
	    
	Autores: Juan Antonio Ortega Aparicio
	         César Borao Moratinos
	                    
	Fecha: 22/05/21
	            
	En esta tercera parte de la práctica final de IST hemos implementado un servicio de ActiveMQ publisher-subscriber de forma concurrente y asíncrono.
	Para ello hemos implementado 4 clases: AsyncSubscriber, Publisher, RunPublishers y RunSubscribers.
	
	- AsyncSubscriber -
	
	Esta clase modela una tarea de subcriber asíncrono. Para ello, implementa tres interfaces: la interfaz Runnable para poder ejecutar la tarea sobre un pool de threads,
	la interfaz ExceptionListener para poder imprimir error en el caso de que suceda una excepción de JMS, y la interfaz que posibilita la recepción asíncrona de los mensajes del topic: la interfaz MessageListener.
	Al igual que el la práctica del PubSub asíncrono con Payara, hemos escogido implementar la interfaz Runnable en vez de Callable, puesto que en el caso de un escenario publishers-subscribers todos los consumidores se cierran al recibir el mismo mensaje "CLOSE".
	
	La clase configura todo lo necesario para ejecutar un subscriber (conexión, sesión y subscriber). Luego, lee todos los mensajes que mande el publisher al topic, y cuando recibe un mensaje "CLOSE", cierra la conexión.
	Otra cosa a destacar, es que en esta clase cerramos las conexiones en orden: primero el subscriber, luego la sesión y lo último la conexión.
	Esto es diferente al funcionamiento con Payara server de las otras partes de la práctica, donde únicamente cerrábamos la conexión (puesto que con hacer eso, se cerraba todo lo que iba por debajo), gracias al soporte que nos daba el servidor de comunicaciones

	
	- Publisher -
	
	Esta clase modela una tarea de publisher. Implementa la interfaz Runnable para poder ejecutar la tarea sobre un pool de threads.
	La clase configura todo lo necesario para ejecutar un publisher (conexión, sesión y subscriber) de forma concurrente. A continuación manda tres mensajes al Topic y un último mensaje "CLOSE".
	Una vez realizada la tarea, cierra la sesión y la conexión y termina.
	
	
	- RunPublishers -
	Ejecuta un pool de hilos que realizan de forma concurrente tareas Publisher. Lo primero que hace es conectarse a la ConnectionFactory del broker (lanzado como proceso independiente) y a partir de ahí lanza N publishers (parametrizado) de forma concurrente.
	Una vez ha acabado, espera que termine el threadpool y termina su ejecución.
	
	- RunSubscribers -
	Ejecuta un pool de hilos que realizan de forma concurrente tareas Subscriber asíncronos. Lo primero que hace es conectarse a la ConnectionFactory del broker (lanzado como proceso independiente) y a partir de ahí lanza N subscribers (parametrizado) de forma concurrente.
	Una vez ha acabado, espera que termine el threadpool y termina su ejecución.
	
	
	Nota: Todo el programa se encuentra parametrizado para poder cambiar el valor del número de publishers y subscribers. Se asume que el usuario introducirá valores permitidos en dichos parámetros, como que el número de publishers y de subscribers es mayor o igual a cero.
	
	
	
	