package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.math.BigInteger;


class CommunicationClient extends JFrame implements Runnable
{
	private static final long serialVersionUID = -1859582902544670970L;
	private int random;
	private int g = 5;
	private int p = 97;
	private String publicKeyB = "";//B的公钥
	private String K = "";//共享的密钥
	private String sendIPAddress="127.0.0.1";
	private int sendPort=8002;

	JPanel contentPane;
	JLabel jLabel1 = new JLabel();
	TextArea jTextArea1 = new TextArea(100, 100);
	JLabel jLabel2 = new JLabel();
	JTextField jTextField1 = new JTextField();
	JButton jb = new JButton("产生发送R1");
	JButton jb2 = new JButton("产生共享密钥");
	Thread c;
	private DatagramSocket sendSocket;
	private DatagramPacket receivePacket;
	private byte[] ESCEOT;  // 帧定界符

	public CommunicationClient()
	{
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
		do
		{
			random = (int) (Math.random() * p);
		} while (random <= 1);
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(null);
		this.setSize(new Dimension(400, 500));
		this.setLocation(300, 100);
		this.setTitle("UDPCLient-李铭");
		jLabel1.setText("通信记录:");
		jLabel1.setBounds(new Rectangle(16, 5, 68, 27));
		contentPane.setLayout(null);
		jTextArea1.setBounds(new Rectangle(15, 33, 349, 340));
		jTextArea1.setEditable(false);
		jLabel2.setText("输入通信内容:");
		jLabel2.setBounds(new Rectangle(17, 383, 92, 37));  //创建输入内容区域
		jTextField1.setText("client");
		jTextField1.setBounds(new Rectangle(112, 385, 250, 31));
		jTextField1.setEditable(false);
		jTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jTextField1_actionPerformed(e);
			}
		});
		jb.setBounds(new Rectangle(120, 425, 120, 30));
		
		jb.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jb_actionPerformed(e);
			}
		});
		jb2.setBounds(new Rectangle(120, 425, 120, 30));
		jb2.setVisible(false);
		jb2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jb2_actionPerformed(e);
			}
		});
		contentPane.add(jLabel1, null);
		contentPane.add(jTextArea1, null);
		contentPane.add(jTextField1, null);
		contentPane.add(jLabel2, null);
		contentPane.add(jb);
		contentPane.add(jb2);

		try
		{
			sendSocket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			jTextArea1.append("不能打开数据报Socket,或者数据报Socket无法与指定端口连接！");
		}
		ESCEOT = new byte[4];
		ESCEOT[0] = 2;
		ESCEOT[1] = 7;
		ESCEOT[2] = 0;
		ESCEOT[3] = 3;
		
		c = new Thread(this);  //创建一个线程
		c.start();
	}
	public void run()
	{
		while (true)
		{
			try
			{
				byte buf[] = new byte[1024];
				receivePacket = new DatagramPacket(buf, buf.length);
				sendSocket.receive(receivePacket);
				
				byte[] databyte = receivePacket.getData();
				String receiveString = new String(databyte);
				if (jTextField1.isEditable() == false)
				{
					publicKeyB = receiveString;
					jTextArea1.append("\n服务端的R2:");
					jTextArea1.append(receiveString);
				}
				if (jTextField1.isEditable())
				{
					
					byte[] databyteEOT = new byte[databyte.length];
					copyByteArray(databyteEOT, databyte);
					
					int indexEOT = findByteArray(databyteEOT, ESCEOT);  // 找到在哪里填空了字节
					
					if(indexEOT != -1)                     // 把帧定界符去掉
					{// 减去字节填充
						databyte = new byte[indexEOT];
						for(int i = 0; i < indexEOT; i++)
						{
							databyte[i] = databyteEOT[i];
						}
					}
					else
					{
						throw new Exception("没有找到 EOT");
					}
					
					byte[] data1 = DES.decrypt(databyte, K);
					receiveString = new String(data1);
					
					jTextArea1.append("\n服务端密文是:");
					jTextArea1.append(new String(databyte));
					jTextArea1.append("\n服务端明文是:");
					jTextArea1.append(receiveString+"\n");
				}
				
				
				//       System.out.println("B的公钥："+publicKeyB);	             
			}
			catch (Exception e)
			{
				e.printStackTrace();
				//   jTextArea1.append("网络通信出现错误,问题在于" + e.toString());
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
	public void jTextField1_actionPerformed(ActionEvent e)
	{
		if (jTextField1.isEditable())
		{
			try
			{
				jTextArea1.append("\n客户端:");
				String string1 = jTextField1.getText().trim();
				jTextArea1.append(string1+"\n");
				jTextField1.setText("");
				byte[] databyte = string1.getBytes();
				
				databyte = DES.encrypt(databyte, K);
				
				for(int i = 0; i < databyte.length; i++) System.out.print(databyte[i]);
				System.out.println();
				
				//增加字节填充: 两个字符ESC,EOT（4个字节长度）实际中发现，只能填充[0000]
				int length = databyte.length + 4;
				byte[] databyteEND = new byte[length];
				for(int i = 0; i < databyte.length; i++)
				{
					databyteEND[i] = databyte[i];
				}
				databyteEND[length - 4] = 2;
				databyteEND[length - 3] = 7;  // ESC: 27
				databyteEND[length - 2] = 0;
				databyteEND[length - 1] = 3;  // EOT: 03
				
//				jTextArea1.append("\n发送的密文是:");
//				jTextArea1.append(new String(databyteEND));
				System.out.println("发送的流：");
				for(int i = 0; i < databyteEND.length; i++) System.out.print(databyteEND[i]);
				System.out.println();
				
				DatagramPacket sendPacket = new DatagramPacket(databyteEND, databyteEND.length,
						java.net.InetAddress.getByName(sendIPAddress), sendPort);
				sendSocket.send(sendPacket);
			}
			catch (IOException ioe)
			{
				jTextArea1.append("网络通信出现错误,问题在于" + e.toString());
			}
		}
	}
	public void jb_actionPerformed(ActionEvent e)
	{
		String publicKeyA = modularExponentiation(String.valueOf(g));
		try
		{
			jTextArea1.append("发送给:"+sendIPAddress+" 端口号:"+sendPort+"\n");		
			jTextArea1.append("\n客户端的R1:");
			jTextArea1.append(publicKeyA);
			String string1 = publicKeyA;
			byte[] databyte = string1.getBytes();
			
			DatagramPacket sendPacket = new DatagramPacket(databyte, databyte.length,
					java.net.InetAddress.getByName(sendIPAddress), sendPort);
			sendSocket.send(sendPacket);
			jb2.setVisible(true);
			jb.setVisible(false);
		}
		catch (IOException ioe)
		{
			jTextArea1.append("网络通信出现错误,问题在于" + e.toString());
		}
	}

	public void jb2_actionPerformed(ActionEvent e)
	{
		K = modularExponentiation(publicKeyB.trim());
		K = normalize(K);
		jTextArea1.append("\n共享密钥是："+K+"\n");
		jTextArea1.append("\n产生共享密钥使用了 Diffie-Hellman-Caitao算法");
		jTextArea1.append("\n加密算法使用了 DES算法");
		jTextArea1.append("\n-----------请放心，以下通信是经过加密的！------------\n");
	//	System.out.println("共享密钥：" + K);
		jb2.setVisible(false);
		jTextField1.setEditable(true);
	}

	public String modularExponentiation(String base)  // 模幂运算
	{
		BigInteger tmp = new BigInteger(base);   // tmp = base

		tmp = tmp.pow(random);                   // tmp = base^random
		tmp = tmp.mod(BigInteger.valueOf(p));    // tmp = tmp%p
		return tmp.toString();
	}

	/*
	 * 把一个不是8位的sharedKey，规格化为一个8位的字符串
	 */
	public String normalize(String sharedKey)
	{
		StringBuffer normalizedKey = new StringBuffer();
		try
		{
			int sharedKeyInt = Integer.parseInt(sharedKey);
			BigInteger billion = new BigInteger("1000000000");
			BigInteger tmp = new BigInteger(sharedKey);
			while (tmp.compareTo(billion) == -1) // 当 tmp < billion
			{
				tmp = tmp.pow(sharedKeyInt);
			}
			String tmpStr = tmp.toString();
			normalizedKey.append(tmpStr.substring(0, 4));
			normalizedKey.append(tmpStr.substring(tmpStr.length() - 4, tmpStr.length()));

			return normalizedKey.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/*
	 * 把array2 复制给array1
	 * 复制成功返回true
	 * 复制失败返回false
	 */
	public boolean copyByteArray(byte[] array1, byte[] array2)
	{
		if(array1.length != array2.length)
			return false;
		
		for(int i = 0; i < array1.length; i++)
		{
			array1[i] = array2[i];
		}
		return true;
	}
	
	/*
	 * 在 字节数组data中寻找字节数组target
	 * 如果找到，返回第一次出现的index
	 * 如果没有找到，返回-1
	 */
	public static int findByteArray(byte[] data, byte[] target)
	{
		byte[] tmp = new byte[4];
		for(int i = 0; i < data.length; i++)
		{
			if(i + 3 < data.length)
			{	
				tmp[0] = data[i];
				tmp[1] = data[i + 1];
				tmp[2] = data[i + 2];
				tmp[3] = data[i + 3];
			}
			else 
			{
				break;     // 此时肯定找不到了
			}
			
			if(compare(tmp, target))
			{
				return i;  // 找到了，返回第一次出现的数组下标
			}
			
		}
		return -1;
	}
	
	/*
	 * 直接数组array1和array2相比较
	 * 如果一模一样，返回true
	 * 如果有不同，返回false
	 */
	public static boolean compare(byte[] array1, byte[] array2)
	{
		if(array1.length != array2.length)  // 长度都不一样，返回false
			return false;
		
		for(int i = 0; i < array1.length; i++)
		{
			if(array1[i] != array2[i])
				return false;
		}
		return true;
	}


}

public class UDPCommunicationClient
{
	public static void main(String[] args)
	{
		CommunicationClient frame1 = new CommunicationClient();  //创建一个实例对象
		frame1.setVisible(true);
	}
}