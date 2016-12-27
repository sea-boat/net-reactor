package com.seaboat.net.reactor.handler;

import com.seaboat.net.reactor.FrontendConnection;

/**
 * 
 * @author seaboat
 * @date 2016-08-25
 * @version 1.0
 * <pre><b>email: </b>849586227@qq.com</pre>
 * <pre><b>blog: </b>http://blog.csdn.net/wangyangzhizhou</pre>
 * <p>This Handler will be call when there is a data having be ready.</p>
 */
public interface Handler {

	public void handle(FrontendConnection connection);

}
