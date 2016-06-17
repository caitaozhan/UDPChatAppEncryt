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
	public TextArea   TextArea1;
	public JTextField jTextFieldInput;
	Thread s;
	private DatagramSocket sendSocket, receiveSocket;
	private DatagramPacket sendPacket, receivePacket;
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
		TextArea1 = new TextArea();
		setSize(new Dimension(400, 200));
		setTitle("UDPServer");
		jTextFieldInput.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jTextFieldInput_actionPerformed(e);
			}
		});
		add(TextArea1, BorderLayout.CENTER);
		add(jTextFieldInput, BorderLayout.SOUTH);
		try
		{
			sendSocket = new DatagramSocket();   //创建接收用数据报
			receiveSocket = new DatagramSocket(8002);
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
		System.out.println("1");
		while (true)
		{
			System.out.println("2");
			try
			{
				byte buf[] = new byte[100];
				receivePacket = new DatagramPacket(buf, buf.length);
				System.out.println("3");
				receiveSocket.receive(receivePacket);
				System.out.println("4");
				name = receivePacket.getAddress().toString().trim();
				TextArea1.append("\n来自主机:" + name + "\n端口:" + receivePacket.getPort());
				TextArea1.append("\n客户端:\t");
				byte[] data = receivePacket.getData();
				String receivedString = new String(data);
				TextArea1.append(receivedString);
			}
			catch (IOException e)
			{
				TextArea1.append("网络通信出现错误,问题在于" + e.toString());
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
	@SuppressWarnings("deprecation")
	void jTextFieldInput_actionPerformed(ActionEvent e)
	{
		try
		{
			TextArea1.append("\n服务器:");
			String string = jTextFieldInput.getText().trim();
			TextArea1.append(string);
			byte[] databyte = new byte[100];
			System.out.println("caitao-1");
			string.getBytes(0, string.length(), databyte, 0);
			DatagramPacket sendPacket = new DatagramPacket(databyte, string.length(),
					java.net.InetAddress.getByName("192.168.191.2"), 8001);
			System.out.println("caitao-2");
			sendSocket.send(sendPacket);
			System.out.println("caitao-3");
		}
		catch (IOException ioe)
		{
			TextArea1.append("网络通信出现错误，问题在于" + e.toString());
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
