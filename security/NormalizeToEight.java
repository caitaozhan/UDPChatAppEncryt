package security;

import java.math.BigInteger;

/**
 * 
 * @author Caitao Zhan (caitaozhan@163.com)
 */
public class NormalizeToEight
{	
	/**
	 * 把一个不是8位的sharedKey，规格化为一个8位的字符串
	 * 
	 * @param sharedKey 不是8位的共享密钥
	 * @return 8位的共享密钥
	 */
	public static String normalize(String sharedKey)
	{
		StringBuffer normalizedKey = new StringBuffer();
		try
		{
			int sharedKeyInt = Integer.parseInt(sharedKey);
			BigInteger billion = new BigInteger("1000000000");
			BigInteger tmp = new BigInteger(sharedKey);
			while (tmp.compareTo(billion) == -1)  // 当 tmp < billion
			{
				tmp = tmp.pow(sharedKeyInt);
			} 
			String tmpStr = tmp.toString();
			normalizedKey.append(tmpStr.substring(0, 4));
			normalizedKey.append(tmpStr.substring(tmpStr.length()-4, tmpStr.length()));
		
			return normalizedKey.toString();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		String string = "2";
		System.out.println(normalize(string));
	}

}
