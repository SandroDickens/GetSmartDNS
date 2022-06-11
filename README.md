# GetSmartDNS
 This tool is used to download the latest version of the  [OpenWrt](https://openwrt.org/) [SmartDNS](https://github.com/pymumu/smartdns) plugin from github.
# Tips
 Supported architectures are: [x86, x86_64, arm, aarch64, mips, mipsel]
# Usage
```bash
 Usage: -arch <architecture> -proxy <proxy>
 e.g  : -arch x86_64       -arch x86_64 -proxy http:127.0.0.1:10081
     -arch : Supported architectures are: [x86, x86_64, arm, aarch64, mips, mipsel]
     -proxy: Supported proxy type: SOCKS(v4 and v5)(deserted), http
             For example: SOCKS:127.0.0.1:10080, http:127.0.0.1:10081
             Proxy are optional. It is recommended if you have an proxy available, because download from github is very slow in Chinese mainland
```
Proxy are optional. It is recommended if you have an proxy available, because download from github is very slow in Chinese mainland
## Windows
```cmd
java -jar .\GetSmartDNS.jar -arch x86_64 # Without proxy
java -jar .\GetSmartDNS.jar -arch x86_64 -proxy http:127.0.0.1:10080 # Use proxy http://127.0.0.1:10080
```
### Linux
```bash
java -jar ./GetSmartDNS.jar -arch x86_64 # Without proxy
java -jar ./GetSmartDNS.jar -arch x86_64 -proxy http:127.0.0.1:10080 # Use proxy http://127.0.0.1:10080
```
