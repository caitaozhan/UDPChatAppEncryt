package security;

public class MD5Exception extends Exception
{

	private static final long serialVersionUID = -2623931755409414200L;

	public MD5Exception(){};
	
	public MD5Exception(String msg)
	{
		super(msg);
	}
	
	public static void main(String[] args)
	{
		try
		{
			throw new MD5Exception("hehe");
		}
		catch (MD5Exception md5e)
		{
			System.out.println(md5e.toString());
		}
	}

}
