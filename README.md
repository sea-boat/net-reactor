# net-reactor
it's a simple and easy net framework with nio mode written by java


# how-to
***just simply like:***
```
Handler handler = new MyHandler();
ReactorPool reactorPool = new ReactorPool(Runtime.getRuntime().availableProcessors(), handler);
new Acceptor(reactorPool, acceptorName, host, port).start();
```