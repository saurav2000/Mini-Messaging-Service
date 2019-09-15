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
				if(br.ready())
					s+= ((char)br.read());
				else
					return null;
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
			if(br.ready())
				return br.readLine();
			else
				return null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public boolean reset()
	{
		try
		{
			br.reset();
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}