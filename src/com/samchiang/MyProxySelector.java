package com.samchiang;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MyProxySelector extends ProxySelector {
	List<Proxy> proxyList;

	MyProxySelector(Proxy.Type proxyType, InetSocketAddress address) {
		proxyList = new ArrayList<>();
		/* SOCKS proxy */
		//proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 10080));
		/* http proxy */
		//proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10081));
		Proxy proxy = new Proxy(proxyType, address);
		proxyList.add(proxy);
	}

	@Override
	public List<Proxy> select(URI uri) {
		return proxyList;
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		System.out.println("proxy connect failed!");
	}
}
