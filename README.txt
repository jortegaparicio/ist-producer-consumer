
	Servicio de Producer-Consumer concurrente con JMS
	
		    	Autores: Juan Antonio Ortega Aparicio
					 César Borao Moratinos
					 
				Fecha: 12/05/21
			
					 
En esta primera parte de la práctica final de IST se ha implementado un servicio de JMS producer-consumer de forma concurrente y con recepción asíncrona. Lanzaremos varios producers y consumers que interactuarán sobre la misma Cola (Queue) de forma concurrente.

Para ello, hemos creado dos programas que lanzarán hilos con tareas de productor e hilos con tareas de consumidores, respectivamente.

Al ejecutar la clase "RunProducersTest" crearemos un pool de threads donde se ejecutará sobre cada hilo una tarea de envío, implementada en la clase "P2PSender".

De forma análoga, al ejecutar la clase "RunConsumersTest" crearemos el pool de theads donde ejecutaremos en cada hilo una tarea de recepción asíncrona, cuya implementación se especifica en la clase "PSPAsyncReceiver".

El número de hilos creados en el threadpool para los productores se encuentra parametrizado en la constante "NPRODUCERS" en la clase "RunProducersTest".

Lo mismo sucede con el número de hilos para los consumidores, que se puede modificar en la constante "NCONSUMERS" de la clase "RunConsumersTest".

Toda la estructura del programa se ejecutará sobre Payara Server.
