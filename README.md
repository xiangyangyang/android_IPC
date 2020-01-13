# android_IPC 跨进程通信
## AIDL 
https://www.cnblogs.com/zhujiabin/p/6080806.html  
https://github.com/singwhatiwanna/android-art-res  
这篇文章里说的很详细了。只做一点补充。  
当客户端是另外一个应用时，为什么需要将整个aidl包复制到客户端工程？  
我想这类似java RMI(remote method invocation)远程方法调用。客户端需要辅助对象stub，将真正要调用对象的方法通过底层传给服务端(service),在服务端执行真正的方法。  
另外注意复制时，客户端与服务端要保持一致，因为客户端要反序列化服务端中和aidl接口相关的所有类，如果完整路径不一致的话，就无法反序列化成功。


