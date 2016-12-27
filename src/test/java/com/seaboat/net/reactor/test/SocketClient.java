package com.seaboat.net.reactor.test;

import java.net.Socket;

import org.junit.Test;

public class SocketClient {

	@Test
	public void visit() {
		for(int i = 0 ; i <= 100; i++){
			new Thread(){
				public void run(){
					try {
						Socket socket = new Socket("132.121.95.184", 6789);
						for (;;) {
							socket.getOutputStream().write(new byte[] { 1, 2, 3, 4, 5 });
							Thread.currentThread().sleep(100);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();;
		}
		while(true){
			try {
				Thread.currentThread().sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
