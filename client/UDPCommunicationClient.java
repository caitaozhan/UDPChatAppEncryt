package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import security.*;
import java.math.BigInteger;

/**
 * 
 * @author LM
 *
 */
class CommunicationClient extends JFrame implements Runnable
{
	private static final long serialVersionUID = -1859582902544670970L;
	private int random;
	private int g = 5;
	private int p = 97;
	private String publicKeyB = "";//B的公钥
	private String K = "";//共享的密钥
	private String sendIPAddress="59.71.138.126";
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
	MD5 md5;

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
		md5 = new MD5("");
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(null);
		this.setSize(new Dimension(470, 500));
		this.setLocation(300, 100);
		this.setTitle("UDPCLient-李铭");
		jLabel1.setText("通信记录:");
		jLabel1.setBounds(new Rectangle(16, 5, 68, 27));
		contentPane.setLayout(null);
		jTextArea1.setBounds(new Rectangle(15, 33, 430, 340));
		jTextArea1.setEditable(false);
		jLabel2.setText("输入通信内容:");
		jLabel2.setBounds(new Rectangle(17, 383, 92, 37));  //创建输入内容区域
		jTextField1.setText("client");
		jTextField1.setBounds(new Rectangle(112, 385, 330, 31));
		jTextField1.setEditable(false);
		jTextField1.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jTextField1_actionPerformed(e);
			}
		});
		jb.setBounds(new Rectangle(160, 425, 120, 30));
		
		jb.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jb_actionPerformed(e);
			}
		});
		jb2.setBounds(new Rectangle(160, 425, 120, 30));
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
				
				byte[] databyte_0 = receivePacket.getData();
				int dataLength = receivePacket.getLength();     // 真正的数据的长度
				String receiveString = new String(databyte_0);
				if (jTextField1.isEditable() == false)
				{
					publicKeyB = receiveString;
					jTextArea1.append("\n服务端的R2:");
					jTextArea1.append(receiveString);
				}
				if (jTextField1.isEditable())
				{
					
					byte[] databyte = new byte[dataLength];     // 把 databyte_0 后面的 0 去掉
					ByteArrayUtil.copyByteArray(databyte, databyte_0, dataLength);
					
					byte[] md5Byte = new byte[32];
					byte[] encryptByte = new byte[databyte.length-32];
					
					ByteArrayUtil.seperate(databyte, md5Byte, encryptByte);  // 拆分
					
					byte[] data1 = DES.decrypt(encryptByte, K);
					receiveString = new String(data1);
					
					md5.updateInstance(new String(data1));
					String md5_2 = md5.getMD5();
					byte[] md5Byte2 = md5_2.getBytes();
					
					if (ByteArrayUtil.equal(md5Byte, md5Byte2) == false)     // 判断拆分得到的md5 和 解密得到的明文的md5 是否一致，然后做相关处理
					{
						throw new MD5Exception("MD5验证码不相等，完整性检测失败！");
					}
					
					jTextArea1.append("\n完整性检测成功，MD5校验码="+md5_2);
					jTextArea1.append("\n服务端密文是:");
					jTextArea1.append(new String(encryptByte));			
					jTextArea1.append("\n服务端明文是:");
					jTextArea1.append(receiveString+"\n");
				}            
			}
			catch (Exception e)
			{
				e.printStackTrace();
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
				
				md5.updateInstance(string1);
				String stringMD5 = md5.getMD5();
				byte[] md5Byte = stringMD5.getBytes();                // MD5报文鉴别码
				
				databyte = DES.encrypt(databyte, K);	
				databyte = ByteArrayUtil.combine(md5Byte, databyte);  // 把MD5和密文一起发过去

				DatagramPacket sendPacket = new DatagramPacket(databyte, databyte.length,
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
		K = NormalizeToEight.normalize(K);
		jTextArea1.append("\n共享密钥是："+K+"\n");
		jTextArea1.append("\n产生共享密钥使用了 Diffie-Hellman-Caitao算法");
		jTextArea1.append("\n使用了MD5算法进行完整性检测");
		jTextArea1.append("\n加密算法使用了 DES算法");
		jTextArea1.append("\n-----------请放心，以下通信是经过加密的！------------\n");
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

}

public class UDPCommunicationClient
{
	public static void main(String[] args)
	{
		CommunicationClient frame1 = new CommunicationClient();  //创建一个实例对象
		frame1.setVisible(true);
	}
}