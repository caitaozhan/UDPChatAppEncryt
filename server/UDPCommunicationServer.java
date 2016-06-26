package server;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.math.BigInteger;
import java.net.*;
import java.io.*;

class CommunicationServer extends JFrame implements Runnable
{
	private static final long serialVersionUID = -2346534561072742542L;
	private JLabel myLabel;
	private TextArea textArea;
	private JTextField jTextFieldInput;
	private JPanel panelNorth;
	private JButton rButton;   // 产生 R
	private JButton keyButton; // 产生共享 key
	private int p;             // 素数
	private int g;             // p的原根
	private int random;        // 保密的随机数
	private String r1;         // 对方的R1（假设我是Bob）
	private String sharedKey;  // 双方共享的密钥
	Thread s;
	private DatagramSocket datagramSocket;            // 用于收发UDP数据报
	private DatagramPacket sendPacket, receivePacket; // 包含具体的要传输的信息
	// 为了发送数据，要将数据封装到DatagramPacket中，使用DatagramSocket发送该包
	private SocketAddress sendAddress;
	private String name;
	private boolean canSend;
	private byte[] ESCEOT;      // 帧定界符

	public CommunicationServer()
	{
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception
	{
		myLabel = new JLabel("通信记录");
		panelNorth = new JPanel();
		panelNorth.add(myLabel);
		panelNorth.add(getRButton());
		panelNorth.add(getKeyButton());
		jTextFieldInput = new JTextField();
		jTextFieldInput.setEditable(false);
		textArea = new TextArea();
		textArea.setEditable(false);
		canSend = false;
		p = 97;
		g = 5;
		do
		{
			random = (int) (Math.random() * p);
		} while (random <= 1);

		setBounds(400, 100, 400, 400);
		setTitle("UDPServer-詹才韬");
		add(panelNorth, BorderLayout.NORTH);
		add(textArea, BorderLayout.CENTER);
		add(jTextFieldInput, BorderLayout.SOUTH);
		jTextFieldInput.addActionListener((ActionEvent e) ->
		{
			jTextFieldInput_actionPerformed(e);
		}); // 使用lambda替换匿名类

		try
		{
			datagramSocket = new DatagramSocket(8002);  // 创建接收方的套接字，IP(chosen by the kernal), 端口号8002
			// System.out.println(datagramSocket.getPort()); // 为什么是 -1 ?
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		ESCEOT = new byte[4];
		ESCEOT[0] = 2;
		ESCEOT[1] = 7;
		ESCEOT[2] = 0;
		ESCEOT[3] = 3;

		s = new Thread(this); // 创建线程
		s.start();
	}

	public void run()         // java 里面，swing那一些控件，是专门一个线程么？
	{
		while (true)
		{
			try
			{
				byte buf[] = new byte[1024];
				receivePacket = new DatagramPacket(buf, buf.length); // 可以不是每一次都new么？用同一个
				datagramSocket.receive(receivePacket);  // 通过套接字，等待接受数据
				canSend = true;                         // 必须先受到客户端的消息，我方（服务器）才能够发送消息（给客户端）
				sendAddress = receivePacket.getSocketAddress();

				byte[] databyte = receivePacket.getData();

				if (jTextFieldInput.isEditable() == true) // 当jTextFieldInput可以编辑的时候，可以发送信息，此时才进行加密
				{
					textArea.append("\n客户端密文是：" + new String(databyte) + '\n');

					byte[] databyteEOT = new byte[databyte.length];
					copyByteArray(databyteEOT, databyte);

					int indexEOT = FindByteArray.findByteArray(databyteEOT, ESCEOT); // 找到在哪里填空了字节

					if (indexEOT != -1) // 把帧定界符去掉
					{// 减去字节填充
						databyte = new byte[indexEOT];
						for (int i = 0; i < indexEOT; i++)
						{
							databyte[i] = databyteEOT[i];
						}
					}
					else
					{
						throw new Exception("没有找到 EOT");
					}

					databyte = DES.decrypt(databyte, sharedKey);
					String receivedString = new String(databyte);
					textArea.append("\n客户端明文是：" + receivedString + '\n');
				}
				if (jTextFieldInput.isEditable() == false) // 当jTextFieldInput无法编辑的时候(初始阶段), 接受的是共享密钥
				{
					textArea.append("https://github.com/caitaozhan/UDPChatAppEncryt");
					name = receivePacket.getAddress().toString().trim();
					textArea.append("\n来自主机:" + name + " 端口:" + receivePacket.getPort());

					r1 = new String(databyte);
					r1 = r1.trim();
					textArea.append("\n客户端的R1 = " + r1);
				}
			}
			catch (IOException ioe)
			{
				textArea.append("网络通信出现错误,问题在于" + ioe.toString());
			}
			catch (Exception e)
			{
				textArea.append("解密出现异常");
			}
		}
	}

	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			System.exit(0);
		}
	}

	void jTextFieldInput_actionPerformed(ActionEvent e)
	{
		try
		{
			if (canSend == true) // 必须先等待客户端先发送消息
			{
				textArea.append("\n服务端:");
				String string = jTextFieldInput.getText().trim();
				textArea.append(string + "\n");
				byte[] databyte = string.getBytes();

				databyte = DES.encrypt(databyte, sharedKey);

				// 增加字节填充: 两个字符ESC,EOT（4个字节长度）
				int length = databyte.length + 4;
				byte[] databyteEND = new byte[length];
				for (int i = 0; i < databyte.length; i++)
				{
					databyteEND[i] = databyte[i];
				}
				databyteEND[length - 4] = 2;
				databyteEND[length - 3] = 7; // ESC: 27
				databyteEND[length - 2] = 0;
				databyteEND[length - 1] = 3; // EOT: 03

				sendPacket = new DatagramPacket(databyteEND, databyteEND.length, sendAddress);
				datagramSocket.send(sendPacket);

				jTextFieldInput.setText("");
				canSend = false;             // 恢复为“不能发送”的状态，等待客户端发送下一个消息
			}
			else
			{
				System.out.println("不知道发送给谁");
			}
		}
		catch (IOException ioe)
		{
			textArea.append("网络通信出现错误，问题在于" + e.toString());
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public JButton getRButton()
	{
		if (rButton == null) // 当第一次调用这个方法的时候，rButton == null，进行初始化操作
		{
			rButton = new JButton("产生发送R2");
			rButton.addActionListener((ActionEvent e) ->
			{
				try
				{
					if (canSend == true)
					{
						String G = String.valueOf(g);
						String R2 = modularExponentiation(G); // 假设我是Bob，产生 R2
						textArea.append("\n服务端的R2 = " + R2);
						byte[] databyte = R2.getBytes();

						sendPacket = new DatagramPacket(databyte, databyte.length, sendAddress);
						datagramSocket.send(sendPacket); // 发送 R2

						canSend = false;                 // 恢复为“不能发送”的状态，等待客户端发送下一个消息
						rButton.setVisible(false);       // “产生R”的按钮消失
						keyButton.setVisible(true);      // “产生共享key”的按钮出现
					}
					else
					{
						System.out.println("不知道发送给谁");
					}
				}
				catch (IOException ioe)
				{
					textArea.append("网络通信出现错误，问题在于" + e.toString());
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			});
		}
		return rButton;
	}

	public JButton getKeyButton()
	{
		if (keyButton == null) // 当第一次调用这个方法的时候，keyButton == null，进行初始化操作
		{
			keyButton = new JButton("产生共享密钥");
			keyButton.setVisible(false);
			keyButton.addActionListener((ActionEvent e) ->
			{
				try
				{
					String key = modularExponentiation(r1);      // 产生sharedKey（不是8位）
					sharedKey = NormalizeToEight.normalize(key); // 规格化成为8位的sharedKey
					textArea.append("\n共享密钥是: " + sharedKey + "\n产生共享密钥使用了 Diffie-Hellman-Caitao算法\n");
					textArea.append("加密算法使用了 DES算法\n-----------请放心，以下通信是经过加密的！------------\n");
					canSend = false;                             // 恢复为“不能发送”的状态，等待客户端发送下一个消息
					keyButton.setVisible(false);                 // “产生共享key”按钮消失
					jTextFieldInput.setEditable(true);           // 输入栏可以编辑
				}
				catch (NumberFormatException nfe)
				{
					textArea.append("String 转换 int 异常");
				}
				catch (Exception exception)
				{
					exception.printStackTrace();
				}
			});
		}
		return keyButton;
	}

	public String modularExponentiation(String base) // 模幂运算
	{
		BigInteger tmp = new BigInteger(base);       // tmp = base
		tmp = tmp.pow(random);                       // tmp = base^random
		tmp = tmp.mod(BigInteger.valueOf(p));        // tmp = tmp%p
		return tmp.toString();
	}

	/*
	 * 把array2 复制给array1 复制成功返回true 复制失败返回false
	 */
	public boolean copyByteArray(byte[] array1, byte[] array2)
	{
		if (array1.length != array2.length)
			return false;

		for (int i = 0; i < array1.length; i++)
		{
			array1[i] = array2[i];
		}
		return true;
	}
}

public class UDPCommunicationServer
{
	public static void main(String[] args)
	{
		CommunicationServer UDPserver = new CommunicationServer();
		UDPserver.setVisible(true);
	}
}
