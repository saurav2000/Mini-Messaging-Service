import java.net.Socket;
import java.io.InvalidObjectException;
import java.security.KeyPair;

public class Client
{
	private String username = null;
	private Socket sendSocket = null;
	private Socket receiveSocket = null;
	private Thread sender = null;
	private Thread receiver = null;
	private KeyPair kp = null;
	
	Client(String name, String ip, int port, int mode) throws InvalidObjectException
	{
		username = name;
		try
		{
			sendSocket = new Socket(ip, port);
			receiveSocket = new Socket(ip, port);
			kp = Cryptography.generateKeyPair();
			sender = new Thread(new Sender(sendSocket.getInputStream(), 
				sendSocket.getOutputStream(), this, kp, mode));
			receiver = new Thread(new Receiver(receiveSocket.getInputStream(), 
				receiveSocket.getOutputStream(), this, kp, mode));
		}
		catch(Exception e)
		{
			// e.printStackTrace();
			throw new InvalidObjectException("CLIENT_CREATE_ERROR");
		}
	}

	private void start()
	{
		sender.start();
		receiver.start();
	}

	//To synchronize username between Sender and Receiver objects
	public synchronized String getUsername()
	{
		return username;
	}

	//To synchronize username between Sender and Receiver objects
	public synchronized void setUsername(String s)
	{
		this.username=s;
	}
	
	public static void main(String[] args)
	{
		try                                                                                                                                                                                     
		{
			int mode = Integer.parseInt(args[3]);
			if(mode>=3||mode<0)
			{
				System.out.println("Invalid mode");
				return;
			}
			Client c = new Client(args[0], args[1], 
				Integer.parseInt(args[2]), mode);
			c.start();
		}
		catch (InvalidObjectException e)
		{
			// e.printStackTrace();
			System.out.println("Unable to connect socket");
		}
		catch(Exception e)
		{
			System.out.println("Please enter valid parameters");
		}
		
	}

}