
	Servicio de Producer-Consumer concurrente con JMS
	
	Autores: Juan Antonio Ortega Aparicio
			 César Borao Moratinos
					 
	Fecha: 12/05/21
			
	En esta primera parte de la práctica final de IST se ha implementado un servicio de JMS producer-consumer de forma concurrente y con recepción asíncrona.
	Lanzaremos varios producers y consumers que interactuarán sobre la misma Cola (Queue) de forma concurrente.

	Para ello, hemos implementado cuatro clases: RunProducersTest, RunConsumersTest, P2PAsyncReceiver y P2PSender.
	
	
	- RunProducersTest -
	
	En esta clase se ha implementado un procedimiento principal mediante el cual lanzamos en un pool N productores (N viene definido en la constante
	NPRODUCERS en el código). Dichos productores ejecutarán de forma concurrente el código implementado en la clase P2PSender.
	Finalmente, esperaremos a que termine el pool de hilos lanzados y recogemos los resultados de la ejecución de los hilos productores mediante el método RecoverResults().
	
	- RunConsumersTest -
	
	Esta clase implementa un procedimiento principal que crea un pool de hilos con la finalidad de lanzar concurrentemente N consumidores asíncronos (N viene definido en la constante
	NCONSUMERS en el código). Las tareas de consumidor se especifican en la implementación del P2PAsyncReceiver.
	Además, al igual que en la clase RunProducersTest, tenemos un método RecoverResults() que nos permitirá recoger el status de ejecución de cada hilo lanzado.
	
	- P2PAsyncReceiver -
	
	Esta clase modela a los consumidores asíncronos y está pensada para correr sobre un hilo del threadpool. Cada consumidor tendrá una nueva conexión, sesión y "receiver", para asegurar la concurrencia en la recepción de los mensajes de la Cola.
	Cada nuevo consumidor quedará ligado a un MessageListener que posibilita la recepción asíncrona. Cuando se reciba un mensaje CLOSE por parte de un productor, el método OnMessage de MessageListener
	se queda dormido (sleep) para evitar la recepción de otros mensajes CLOSE antes de cerrar la conexión del consumidor.
	
	Además, la clase implementa la interfaz Callable, con la finalidad de devolver feedback al usuario a cerca de ejecuación de los consumidores.
	Cuando existan consumidores activos (que no hayan leído de la Cola un mensaje CLOSE) y ya no haya más hilos productores en un timeout de 60 segundos, el programa fuerza el cierre y se retorna un status de "WARNING" en los hilos
	que han sido forzados a cerrar.
	
	- P2PSender -
	
	Esta clase modela a los productores y está pensada como una tarea para poder montarla sobre un hilo. Por ello, implementa la interfaz Runnable (nuevamente, hemos elegido Runnable antes que Callable por la ventaja que supone conocer el resultado de la ejecución de los hilos).
	Cada productor envía tres mensajes a la Cola, seguidos por un mensaje de CLOSE. Posteriormente cierra la conexión y retorna el status.
		
	
	Toda la estructura del programa se ejecutará sobre Payara Server, un servidor de aplicaciones que proporciona soporte para JMS.
	Todo el programa se encuentra parametrizado para poder cambiar el valor del número de producers y consumers. Se asume que el usuario introducirá valores permitidos en dichos parámetros, como que el número de producers y de consumers es mayor o igual a cero.
	
	

