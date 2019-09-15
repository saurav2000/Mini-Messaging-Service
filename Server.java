import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server
{
	private HashMap<String, Socket> map;
	private HashMap<String, Integer> count;
	private ServerSocket ss = null;
	private int mode = -1;

	Server(int port, int mode) throws InvalidObjectException
	{
		map = new HashMap<>();
		count = new HashMap<>();
		this.mode = mode;
		try
		{
			ss = new ServerSocket(port);
		}
		catch (IOException e)
		{
			// e.printStackTrace();
			throw new InvalidObjectException("PORT_ERROR");
		}
	}
	
	private void start()
	{
		while(true)
		{
			try
			{
				Socket s = ss.accept();
				new Thread(new ServerThread(s, this, mode)).start();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void insert(String s, Socket sock)
	{
		map.put(s, sock);
	}
	
	public synchronized Socket get(String s)
	{
		return map.get(s);
	}

	//If receiver thread is run first then map fails
	public synchronized boolean contains(String s)
	{
		if(!map.containsKey(s))
			return false;
		if(!count.containsKey(s))
			return false;
		return count.get(s)>=2;
	}

	//If receiver thread is run first
	public synchronized void updateCount(String s)
	{
		if(count.containsKey(s))
			count.put(s, new Integer(count.get(s)+1));
		else
			count.put(s,1);
	}	

	public synchronized void delete(String s)
	{
		if(map.containsKey(s))
			map.remove(s);
		if(count.containsKey(s))
			count.remove(s);
	}
	
	public static void main(String[] args)
	{
		try
		{
			new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1])).start();
		}
		catch (InvalidObjectException e)
		{
			System.out.println("Port occupied or permission denied");		
		}
		catch(Exception e)
		{
			System.out.println("Please enter valid parameters");
		}
	}
	
}
