一、环境
如果client和server运行在两台电脑上，请client和server在同一个局域网内
如果client和server运行在同一台电脑，ip = 127.0.0.1


二、共享密钥产生的过程
注意序号，需要严格执行先后关系。我们实现了Diffie-Hellman算法，用以进行共享密钥的产生。
共享密钥是对称相等的，只有client和server双方知道，第三方无法监听。
在传统的Diffie-Hellman算法基础上，Caitao进行了稍许改进，用以配合DES加密算法的使用。
新的共享密钥的名称是Diffie-Hellman-Caitao算法

client（Alice）                                            server（Bob）
1：客户端点击"产生发送R1"按钮产生R1并发送给服务器

                                                          2：服务端点击"产生发送R2"按钮产生R2并发送给客户端

3：客户端点击"产生共享密钥"利用R2产生共享密钥K              3：服务端点击"产生共享密钥"利用R1产生共享密钥K

4：客户端安全愉悦的发送消息

                                                          5：服务端安全愉悦的发送消息


三、加密算法
加密算法我们采用了经典的DES算法，其中的对称密钥使用Diffie-Hellman-Caitao算法产生。
