package com.seaboat.net.reactor;

public interface ConnectionEventHandler {

	public int getEventType();

	public void event(FrontendConnection connection);

}
