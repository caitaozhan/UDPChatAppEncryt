package security;

/**
 * 
 * @author Caitao Zhan (caitaozhan@163.com)
 */
public class ByteArrayUtil
{

	/**
	 * 把array2 复制给array1 复制成功返回true 复制失败返回false
	 * 
	 * @param array1 一个字节数组
	 * @param array2 待被复制的字节数组
	 * @param array2中待复制数据的长度
	 * @return 是否复制成功
 	 */
	public static boolean copyByteArray(byte[] array1, byte[] array2, int array2Length)
	{
		if (array1.length != array2Length || array2.length < array2Length)
			return false;

		for (int i = 0; i < array1.length; i++)
		{
			array1[i] = array2[i];
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param md5byte    MD5报文鉴别码
	 * @param encyptByte 经过加密的密文
	 * @return MD5+encyptByte
	 */
	public static byte[] combine(byte[] md5Byte, byte[] encyptByte)
	{
		byte[] combinebyte = new byte[md5Byte.length + encyptByte.length];
		int index = 0;
		for(int i = 0; i < md5Byte.length; i++, index++)
		{
			combinebyte[index] = md5Byte[i];
		}
		for(int i = 0; i < encyptByte.length; i++, index++)
		{
			combinebyte[index] = encyptByte[i];
		}
		return combinebyte;
	}
	
	/**
	 * 把接受的报文进行拆分，直接对参数进行改变
	 * 拆成md5摘要码和密文
	 * 
	 * @param combined    结合而成的md5Byte+encryptByte
	 * @param md5Byte     128位长的md5Byte， 16进制（4位）表示，需要32个byte
	 * @param encryptByte 密文
	 */
	public static void seperate(byte[] combined, byte[] md5Byte, byte[] encryptByte)
	{
		int encryptLength = combined.length - 32;  // md5 的长度是定长，32个字节
		int index = 0;
		md5Byte = new byte[32];
		encryptByte = new byte[encryptLength];
		for(int i = 0; i < md5Byte.length; i++, index++)
		{
			md5Byte[i] = combined[index];
		}
		for(int i = 0; i < encryptByte.length; i++, index++)
		{
			encryptByte[i] = combined[index];
		}
	}
	
	public static void main(String[] args)
	{

	}

}
