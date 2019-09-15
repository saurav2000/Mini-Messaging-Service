import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

class Sender implements Runnable
{
	private Reader in = null;
	private Reader inUser = null;
	private Writer out = null;
	private Client client = null;
	private KeyPair kp = null;
	private int mode = -1;

	Sender(InputStream i, OutputStream o, Client c, KeyPair kp, int mode)
	{
		inUser = new Reader(System.in);
		in = new Reader(i);
		out = new Writer(o);
		client = c;
		this.kp = kp;
		this.mode = mode;
	}

	public void run()
	{
		if(!register())
			return;
		System.out.println("Sender socket registered");
		boolean reading = true, encrypt = false;
		String recipient = "", message = "", signature="";
		while(true)
		{
			if(reading)
			{
				String line = inUser.readLine();
				if(line==null)
					continue;

				if(!isValidLine(line))
				{
					System.out.println("ERROR: Wrong Format");
					continue;
				}

				if(line.equals("DEREGISTER"))
				{
					//For the server to destroy its thread
					out.print("DEREGISTER\n\n");
					//Setting username to null which 
					//is checked in receiver
					client.setUsername(null);
					return;
				}

				message = line.substring(line.indexOf(32)+1);
				//RSA512 uses 11 bytes for padding,
				//thus can't encrypt more than 53 bytes 
				if(message.length()>=53&&mode!=0)
				{
					System.out.println("ERROR: Max length exceeded");
					continue;
				}
				recipient = line.substring(1,line.indexOf(32));
				//FETCHKEY for encryption
				if(mode>0)
				{
					out.print("FETCHKEY "+recipient+"\n\n");
					reading = false;
					encrypt = true;
				}
				//message sent to server
				else
				{
					out.print("SEND "+recipient+"\nContent-length: "+message.length()+"\n\n"+message);
					reading = false;
				}
			}

			else if(encrypt)
			{
				String s = in.readLine();
				if(s==null)
					continue;
				//Key fetched for encryption
				if(s.startsWith("FETCHEDKEY"))
				{
					String key = in.read(Integer.parseInt(in.readLine()));
					in.readLine();
					in.readLine();
					message = Cryptography.encrypt(key,message);
					//Send without signature for mode 1
					if(mode==1)
						out.print("SEND "+recipient+"\nContent-length: "+message.length()+"\n\n"+message);
					else if(mode==2)
					{
						signature = Cryptography.sign(message,kp.getPrivate());
						out.print("SEND " + recipient+"\nContent-length: "+message.length()+"\nSignature-length: "
							+signature.length()+"\n\n"+message+signature);
					}

					reading = false;
					encrypt = false;
				}
				//recipient not registered
				else if(s.startsWith("ERROR 101"))
				{
					System.out.println(s.substring(10));
					encrypt = false;
					reading = true;
				}
			}

			else
			{
				String s = in.readLine();
				if(s==null)
					continue;
				//Acknowledgment message
				if(s.startsWith("SENT"))
					System.out.println("Succesfully Sent"+"\n");
				//recipient not registered(mode 0)
				else if(s.startsWith("ERROR 101"))
					System.out.println(s.substring(10));
				else if(s.startsWith("ERROR 102"))
					System.out.println("Unsuccesfully Sent: "+s.substring(s.indexOf('2')+2)+"\n");
				else if(s.startsWith("ERROR 103"))
					System.out.println("Header Incomplete"+"\n");
				else if(s.startsWith("ERROR 106"))
					System.out.println("Message Authentication Failed"+"\n");
				else
					System.out.println("HI BIACH");
				
				in.readLine();

				reading = true;
			}
		}
	}
	
	private boolean register()
	{
		boolean sent = true, done = false;
		out.println("REGISTER TOSEND "+client.getUsername()+" "+mode+"\n");

		while(!done)
		{
			if(!sent)
			{
				String u = inUser.readLine();
				if(u==null)
					continue;
				//Synchronizing with receiver
				client.setUsername(u);
				out.println("REGISTER TOSEND "+client.getUsername()+" "+mode+"\n");
				sent = true;
			}
			
			String s = in.readLine();
			
			if(s==null)
				continue;
			else
			{
				if(s.startsWith("REGISTERED TOSEND"))
					done = true;
				else if(s.startsWith("ERROR 100"))
				{
					sent = false;
					System.out.print("The username is invalid or already taken: ");
				}
				else if(s.startsWith("ERROR 107"))
				{
					System.out.println("The mode is incompatible with the server");
					return false;	
				}
				else
					System.out.println(s);
				
				in.readLine();
			}
		}
		return true;
	}

	//Checking the format of the input line
	private boolean isValidLine(String s)
	{
		if(s==null)
			return false;

		if(s.equals("DEREGISTER"))
			return true;
		
		s.trim();
		if(s.equals(""))
			return false;
		
		if(s.indexOf(32)==-1)
			return false;
		
		String sub = s.substring(0,s.indexOf(32));
		if(sub.length()<=1)
			return false;
		
		if(sub.charAt(0)!='@')
			return false;
		
		sub = sub.substring(1).trim();
		if(sub.equals(""))
			return false;
		sub = s.substring(s.indexOf(32)).trim();
		if(sub.length()==0)
			return false;
		
		return true;
	}
}