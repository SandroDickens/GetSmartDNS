package work.nereus.github;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		Solution solution = new Solution();

		Arch targetArch = solution.parseTargetArchArgs(args);
		JSONObject proxyJSONObject = solution.parseProxyArgs(args);

		if ((targetArch != null) && (proxyJSONObject != null)) {
			Proxy.Type proxyType = (Proxy.Type) proxyJSONObject.get("proxyType");
			if (proxyType != Proxy.Type.DIRECT) {
				String proxyAddrStr = proxyJSONObject.getString("address");
				int proxyPort = Integer.parseInt((String) proxyJSONObject.get("port"));

				InetSocketAddress proxyAddr = new InetSocketAddress(proxyAddrStr, proxyPort);
				solution.setProxySelector(proxyType, proxyAddr);
				System.out.println("Use proxy " + proxyType + ":" + proxyAddrStr + ":" + proxyPort);
			}

			JSONArray urlArray = solution.getLatestURL(targetArch);
			if (urlArray != null) {
				for (int i = 0; i < urlArray.length(); i++) {
					JSONObject jsonObject = urlArray.getJSONObject(i);
					System.out.println("ipk:  " + jsonObject.get("name"));
					System.out.println("link: " + jsonObject.get("url"));
				}
				solution.downloadIpk(urlArray);
			}

		}
		else {
			System.out.println("What is this?\n" +
					"This tool is used to download the latest version of the OpenWrt SmartDNS plugin from github.\n" +
					"\n" +
					"Usage: -arch <architecture> -proxy <proxy>\n" +
					"e.g  : -arch x86_64" +
					"       -arch x86_64 -proxy http:127.0.0.1:10081" +
					"\n" +
					"    -arch : Supported architectures are: " + Arrays.toString(Arch.values()) + "\n" +
					"    -proxy: Supported proxy type: SOCKS(v4 and v5)(deserted), http\n" +
					"            For example: SOCKS:127.0.0.1:10080, http:127.0.0.1:10081\n" +
					"            Proxy are optional. It is recommended if you have an proxy available, because download from github is very slow in Chinese mainland\n");
		}
	}
}

class Solution {
	private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36";
	private ProxySelector proxySelector;

	Solution() {
		proxySelector = ProxySelector.getDefault();
	}

	public Arch parseTargetArchArgs(String[] args) {
		for (String x : args) {
			for (int i = 0; i < Arch.values().length; i++) {
				if (x.equals(Arch.values()[i].getDescribe())) {
					return Arch.values()[i];
				}
			}
		}
		return null;
	}

	public JSONObject parseProxyArgs(String[] args) {
		boolean isUseProxy = false;
		Proxy.Type proxyType = Proxy.Type.DIRECT;
		String proxyString = null;
		JSONObject jsonObject;
		for (String x : args) {
			if (x.contains("-proxy")) {
				isUseProxy = true;
			}
			else if (x.contains("SOCKS")) {
				/* SOCKS proxy */
				proxyType = Proxy.Type.SOCKS;
				proxyString = x;
				break;
			}
			else if (x.contains("http")) {
				/* http proxy */
				proxyType = Proxy.Type.HTTP;
				proxyString = x;
				break;
			}
		}
		jsonObject = new JSONObject();
		jsonObject.put("proxyType", proxyType);

		if (isUseProxy) {
			/* /(http:)(((25[0-5])|(2[0-4]\d)|([01]?\d{1,2}))\.){1,3}((25[0-5])|(2[0-4]\d)|([01]?\d{1,2})){1}(:)([0-9]+)/gm */
			final String httpProxyRegex = "(http:)(((25[0-5])|(2[0-4]\\d)|([01]?\\d{1,2}))\\.){1,3}((25[0-5])|(2[0-4]\\d)|([01]?\\d{1,2}))(:)([0-9]+)";
			/* /(SOCKS:)(((25[0-5])|(2[0-4]\d)|([01]?\d{1,2}))\.){1,3}((25[0-5])|(2[0-4]\d)|([01]?\d{1,2})){1}(:)([0-9]+)/gm */
			final String socksProxyRegex = "(SOCKS:)(((25[0-5])|(2[0-4]\\d)|([01]?\\d{1,2}))\\.){1,3}((25[0-5])|(2[0-4]\\d)|([01]?\\d{1,2}))(:)([0-9]+)";

			if (proxyString != null) {
				String[] argArray;

				boolean isArgMatch;
				if (proxyType == Proxy.Type.HTTP) {
					isArgMatch = proxyString.matches(httpProxyRegex);
				}
				else {
					isArgMatch = proxyString.matches(socksProxyRegex);
				}
				if (isArgMatch) {
					argArray = proxyString.split(":");
					jsonObject.put("address", argArray[1]);
					jsonObject.put("port", argArray[2]);
				}
				else {
					System.out.println("Invalid proxy parameter");
				}
			}
		}

		return jsonObject;
	}

	public JSONArray getLatestURL(Arch arch) {
		JSONArray jsonURLArray = null;
		/* github latest release API of SmartDNS */
		String githubReleaseURL = "https://api.github.com/repos/pymumu/smartdns/releases/latest";

		HttpClient httpClient = HttpClient.newBuilder().build();
		HttpRequest httpRequest = HttpRequest.newBuilder().GET()
				.uri(URI.create(githubReleaseURL))
				.setHeader("User-Agent", userAgent)
				.build();
		try {
			HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			JSONObject responseJSON = new JSONObject(httpResponse.body());
			JSONArray assets = responseJSON.getJSONArray("assets");
			/* smartdns.1.2020.09.08-2235.x86_64-openwrt-all.ipk */
			/* luci-app-smartdns.1.2020.09.08-2235.all-luci-all.ipk */
			jsonURLArray = new JSONArray();
			for (int i = 0; i < assets.length(); i++) {
				JSONObject jsonObject = assets.getJSONObject(i);
				String name = jsonObject.getString("name");
				if (name.contains("luci-all") ||
						name.contains(arch.getDescribe() + "-openwrt-all") ||
						name.contains(arch.getDescribe() + "luci-compat-all")) {
					String downLoadURL = jsonObject.getString("browser_download_url");
					JSONObject object = new JSONObject();
					object.put("name", name);
					object.put("url", downLoadURL);
					jsonURLArray.put(object);
				}
			}
		}
		catch (IOException | InterruptedException expt) {
			expt.printStackTrace();
		}
		return jsonURLArray;
	}

	public void downloadIpk(JSONArray resource) {
		for (int i = 0; i < resource.length(); i++) {
			JSONObject jsonObject = resource.getJSONObject(i);
			String name = jsonObject.getString("name");
			String URL = jsonObject.getString("url");
			HttpClient httpClient = HttpClient.newBuilder()
					.followRedirects(HttpClient.Redirect.ALWAYS)
					.proxy(proxySelector)
					.build();
			HttpRequest httpRequest = HttpRequest.newBuilder().GET()
					.uri(URI.create(URL))
					.setHeader("User-Agent", userAgent)
					.build();
			try {
				HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
				InputStream downStream = httpResponse.body();
				File outFile = new File(name);
				FileOutputStream outputStream = new FileOutputStream(outFile);
				byte[] context = new byte[8 * 1024 * 1024];
				int readSize;
				int offset = 0;
				do {
					readSize = downStream.read(context);
					if (readSize != -1) {
						outputStream.write(context, offset, readSize);
						offset += readSize;
					}
					else {
						break;
					}
				} while (true);

				outputStream.close();
				downStream.close();
				System.out.println("Download completed:" + name + ", size(bytes): " + offset);
			}
			catch (IOException | InterruptedException expt) {
				expt.printStackTrace();
			}

		}
	}

	public void setProxySelector(Proxy.Type proxyType, InetSocketAddress address) {
		this.proxySelector = new MyProxySelector(proxyType, address);
	}
}

enum Arch {
	x86("x86"),
	x86_64("x86_64"),
	arm("arm"),
	aarch64("aarch64"),
	mips("mips"),
	mipsel("mipsel");

	private String describe;

	Arch(String desc) {
		this.describe = desc;
	}

	public String getDescribe() {
		return describe;
	}
}