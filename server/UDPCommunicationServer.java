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
	private JLabel     myLabel;
	private TextArea   textArea;
	private JTextField jTextFieldInput;
	private JPanel     panelNorth;
	private JButton    rButton;      // 产生 R
	private JButton    keyButton;    // 产生共享 key
	private int p;      // 素数
	private int g;      // p的原根
	private int random; // 保密的随机数
	private String r1;  // 对方的R1（假设我是Bob）
	private int K;      // 双方共享的秘密密钥
	Thread s;
	private DatagramSocket sendSocket, receiveSocket;  // 用于收发UDP数据报
	private DatagramPacket sendPacket, receivePacket;  // 包含具体的要传输的信息
	// 为了发送数据，要将数据封装到DatagramPacket中，使用DatagramSocket发送该包
	private SocketAddress sendAddress;
	private String  name;
	private boolean canSend;

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
			random = (int) (Math.random()*p);
		}while(random <= 1);

		setSize(400, 400);
		setTitle("UDPServer");
		add(panelNorth, BorderLayout.NORTH);
		add(textArea, BorderLayout.CENTER);
		add(jTextFieldInput, BorderLayout.SOUTH);
		jTextFieldInput.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jTextFieldInput_actionPerformed(e);
			}
		});
		try
		{
			sendSocket = new DatagramSocket();         // 创建发送方的套接字，IP默认为本地，端口号随机
			//System.out.println(sendSocket.getPort());// 为什么是 -1 ?
			receiveSocket = new DatagramSocket(8002);  // 创建接收方的套接字，IP(chosen by the kernal),端口号8002
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		s = new Thread(this);  //创建线程
		s.start();
	}

	public void run()  // java 里面，swing那一些控件，是专门一个线程么？
	{
		while (true)
		{
			try
			{
				byte buf[] = new byte[1024];
				receivePacket = new DatagramPacket(buf, buf.length);  // 可以不是每一次都new么？用同一个

				receiveSocket.receive(receivePacket);  // 通过套接字，等待接受数据
				canSend = true;                        // 必须先受到客户端的消息，我方（服务器）才能够发送消息（给客户端）
				
				sendAddress = receivePacket.getSocketAddress();
				
				name = receivePacket.getAddress().toString().trim();
				textArea.append("\n来自主机:" + name + "\n端口:" + receivePacket.getPort());
				textArea.append("\n客户端: ");
				byte[] data = receivePacket.getData();
				String receivedString = new String(data);
				textArea.append(receivedString);
				
				if(jTextFieldInput.isEditable() == false)  // 最开始计算共享密钥的时候，jTextFieldInput 无法编辑
				{
					r1 = receivedString.trim();
				}
			}
			catch (IOException e)
			{
				textArea.append("网络通信出现错误,问题在于" + e.toString());
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
			if (canSend == true)  // 必须先等待客户端先发送消息
			{
				textArea.append("\n服务器:");
				String string = jTextFieldInput.getText().trim();
				textArea.append(string);
				byte[] databyte = string.getBytes();
				
				sendPacket = new DatagramPacket(databyte, databyte.length, sendAddress);
				sendSocket.send(sendPacket);
				
				canSend = false;  // 恢复为“不能发送”的状态，等待客户端发送下一个消息
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
		if(rButton == null)  // 当第一次调用这个方法的时候，rButton == null，进行初始化操作
		{
			rButton = new JButton("产生 R");
			rButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						if(canSend == true)
						{
							String G = String.valueOf(g);
							String R2 = modularExponentiation(G);  // 假设我是Bob，产生 R2
							textArea.append("\n产生R=" + R2);
							byte[] databyte = R2.getBytes();
							
							sendPacket = new DatagramPacket(databyte, databyte.length, sendAddress);
							sendSocket.send(sendPacket);           // 发送 R2
							
							canSend = false;  // 恢复为“不能发送”的状态，等待客户端发送下一个消息
							rButton.setVisible(false);   // “产生R”的按钮消失
							keyButton.setVisible(true);  // “产生共享key”的按钮出现
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
			});
		}
		return rButton;
	}
	
	public JButton getKeyButton()
	{
		if(keyButton == null)  // 当第一次调用这个方法的时候，keyButton == null，进行初始化操作
		{
			keyButton = new JButton("产生共享Key");
			keyButton.setVisible(false);;
			keyButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						System.out.println(r1);
						String key = modularExponentiation(r1);  // 产生共享的key
						K = Integer.parseInt(key);
						textArea.append("\n共享密钥是: " + key);
						
						canSend = false;  // 恢复为“不能发送”的状态，等待客户端发送下一个消息
						keyButton.setVisible(false);             // “产生共享key”按钮消失 
						jTextFieldInput.setEnabled(true);        // 输入栏可以编辑
					}
					catch (NumberFormatException nfe)
					{
						textArea.append("String 转换 int 异常");
					}
					catch (Exception exception)
					{
						exception.printStackTrace();
					}
				}
			});
		}
		return keyButton;
	}
	
	
	public String modularExponentiation(String base)  // 模幂运算
	{
		BigInteger tmp = new BigInteger(base);   // tmp = base
		tmp = tmp.pow(random);                   // tmp = base^random
		tmp = tmp.mod(BigInteger.valueOf(p));    // tmp = tmp%p
		return tmp.toString();
	}
}

public class UDPCommunicationServer
{
	public static void main(String[] args)
	{
		CommunicationServer UDPserver = new CommunicationServer();
		UDPserver.setVisible(true);
		
		/*String base = "10";
		String key = UDPserver.modularExponentiation(base);
		System.out.println(key);*/
		
		
	}
}
