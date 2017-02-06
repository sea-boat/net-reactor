# net-reactor
it's a simple and easy net framework with nio mode written by java


# how-to
***just simply like:***
```
Handler handler = new MyHandler();
ReactorPool reactorPool = new ReactorPool(Runtime.getRuntime().availableProcessors(), handler);
new Acceptor(reactorPool, acceptorName, host, port).start();
```

***adding a connection event:***
```
public class RegisterHandler implements ConnectionEventHandler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RegisterHandler.class);

	public int getEventType() {
		return ConnectionEvents.REGISTE;
	}

	public void event(FrontendConnection connection) {
	    //do something here	
	}

}

```
```
Handler handler = new NetHandler();
ConnectionEventHandler connectionEventHandler = new RegisterHandler();
ReactorPool reactorPool = new ReactorPool(Runtime.getRuntime().availableProcessors(), handler);
Acceptor acceptor = new Acceptor(reactorPool, acceptorName, host, port);
acceptor.addConnectionEventHandler(connectionEventHandler);
acceptor.start();
```
