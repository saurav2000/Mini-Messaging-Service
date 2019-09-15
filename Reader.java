import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Reader
{
	private BufferedReader br = null;

	Reader(InputStream i)
	{
		br = new BufferedReader(new InputStreamReader(i));
	}

	public String read(int n)
	{
		String s = "";
		try
		{
			for(int i=0;i<n;++i)
			{
				int t = br.read();
				if(t==-1)
					return null;
				s+= ((char)t);
			}
			return s;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public String readLine()
	{
		try
		{
			return br.readLine();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	// public boolean reset()
	// {
	// 	try
	// 	{
	// 		br.reset();
	// 		return true;
	// 	}
	// 	catch(IOException e)
	// 	{
	// 		e.printStackTrace();
	// 	}
	// 	return false;
	// }
	// public boolean ready()
	// {
	// 	try
	// 	{
	// 		return br.ready();
	// 	}
	// 	catch(IOException e)
	// 	{
	// 		e.printStackTrace();
	// 	}
	// 	return false;
	// }
}