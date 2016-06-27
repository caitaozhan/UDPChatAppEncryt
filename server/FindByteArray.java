package server;

public class FindByteArray
{

	/**
	 * 在字节数组data中寻找字节数组target<br>
	 * 如果找到，返回第一次出现的index<br>如果没有找到，返回-1
	 * 
	 * @param data 数据
	 * @param target 待查找的目标
	 * @return target[0]在data中出现的下标
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
	
	public static void main(String[] args)
	{
		byte[] target = new byte[4];
		target[0] = 2;
		target[1] = 7;
		target[2] = 0;
		target[3] = 3;
		
		byte[] databyte = new byte[10];
		databyte[0] = 1;
		databyte[1] = 2;
		databyte[2] = 3;
		databyte[3] = 4;
		databyte[4] = 2;
		databyte[5] = 7;
		databyte[6] = 0;
		databyte[7] = 3;
		databyte[8] = 3;
		databyte[9] = 3;
		
		System.out.println(findByteArray(databyte, target));
		
	}

}
