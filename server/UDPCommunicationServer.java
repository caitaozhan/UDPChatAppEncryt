package server;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.net.*;
import java.io.*;

class CommunicationServer extends JFrame implements Runnable
{
	private static final long serialVersionUID = -2346534561072742542L;
	public JLabel     myLabel;
	public TextArea   textArea;
	public JTextField jTextFieldInput;
	Thread s;
	private DatagramSocket sendSocket, receiveSocket;
	private DatagramPacket receivePacket;
	private SocketAddress  sendAddress;
	private String name;

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
		jTextFieldInput = new JTextField();
		textArea = new TextArea();
		
		setSize(400, 200);
		setTitle("UDPServer");
		add(myLabel, BorderLayout.NORTH);
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

	public void run()
	{
		while (true)
		{
			try
			{
				byte buf[] = new byte[1024];
				receivePacket = new DatagramPacket(buf, buf.length);
				
				receiveSocket.receive(receivePacket);  // 通过套接字，等待接受数据
				
				name = receivePacket.getAddress().toString().trim();
				textArea.append("\n来自主机:" + name + "\n端口:" + receivePacket.getPort());
				textArea.append("\n客户端: ");
				byte[] data = receivePacket.getData();
				String receivedString = new String(data);
				textArea.append(receivedString);
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
			textArea.append("\n服务器:");
			String string = jTextFieldInput.getText().trim();
			textArea.append(string);
			byte[] databyte = string.getBytes();
			sendAddress = receivePacket.getSocketAddress();
			DatagramPacket sendPacket = new DatagramPacket(databyte, databyte.length, sendAddress);
			sendSocket.send(sendPacket);
		}
		catch (IOException ioe)
		{
			textArea.append("网络通信出现错误，问题在于" + e.toString());
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}
}

public class UDPCommunicationServer
{
	public static void main(String[] args)
	{
		CommunicationServer frame1 = new CommunicationServer();
		frame1.setVisible(true);
	}
}
