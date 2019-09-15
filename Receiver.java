import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

class Receiver implements Runnable
{
	private Reader in = null;
	private Writer out = null;
	private Client client = null;
	private KeyPair kp = null;
	private int mode = -1;

	Receiver(InputStream i, OutputStream o, Client c, KeyPair kp, int mode)
	{
		in = new Reader(i);
		out = new Writer(o);
		client = c;
		this.kp = kp;
		this.mode = mode;
	}

	public void run()
	{
		boolean hashCheck = false;
		String message = "",sender="",signature="";
		int l1 = 0, l2 = 0;
		if(!register())
			return;
		System.out.println("Receiver socket registered");
		while(true)
		{
			//deregister
			if(client.getUsername()==null)
				return;
			String line = in.readLine();
			if(line==null)
				continue;
			//FETCHKEY for encryption/signature check
			if(line.startsWith("FETCHKEY"))
			{
				in.readLine();
				String temp = Cryptography.toString(kp.getPublic().getEncoded());
				out.print("FETCHEDKEY\n"+temp.length()+"\n"+temp+"\n\n");
			}

			else if(hashCheck)
			{
				//Key fetched for signature check
				if(line.startsWith("FETCHEDKEY"))
				{
					int l = Integer.parseInt(in.readLine());
					String key = in.read(l);
					in.readLine();
					in.readLine();
					boolean safe = Cryptography.verify(message,signature,key).booleanValue();
					String safes = safe?" (SAFE)":" (UNSAFE)";
					message = Cryptography.decrypt(kp.getPrivate().getEncoded(),message);
					System.out.println("@"+sender+": "+message+" "+safes+"\n");
					if(safe)
						out.print("RECEIVED "+sender+"\n\n");
					else
						out.print("ERROR 106 MESSAGE TAMPERED\n\n");

					hashCheck = false;
				}
			}
			//forwarded messsage from server
			else if(line.startsWith("FORWARD "))
			{
				message = "";
				signature = "";
				l1 = 0; l2 = 0;
				sender = line.substring(line.indexOf(32)+1);
				line = in.readLine();
				l1 = Integer.parseInt(line.substring(16));
				if(mode==2)
				{
					line = in.readLine();
					l2 = Integer.parseInt(line.substring(18));
				}
				if(line==null)
				{
					out.print("ERROR 103 Header incomplete\n\n");
					continue;
				}
				line = in.readLine();
				if(!line.equals(""))
				{
					out.print("ERROR 103 Header incomplete\n\n");
					continue;
				}
				line = in.read(l1);
				if(line==null)
				{
					out.print("ERROR 103 Header incomplete\n\n");
					continue;
				}
				else
				{
					message = line;
					//reading signature, sending fetchkey for signature check
					if(mode==2)
					{
						line = in.read(l2);
						signature=line;
						out.print("FETCHKEY\n\n");
						hashCheck = true;
						continue;
					}
					//message decryption
					if(mode==1)
						message = Cryptography.decrypt(kp.getPrivate().getEncoded(),message);
					System.out.println("@"+sender+": "+message+"\n");
					out.print("RECEIVED "+sender+"\n\n");
				}
			}
		}
	}

	private boolean register()
	{
		boolean sent = false, done = false;
		do
		{
			if(!sent)
			{
				out.print("REGISTER TORECV "+client.getUsername()+" "+mode+"\n\n");
				sent = true;
			}
			
			String s = in.readLine();
			if(s==null)
				continue;
			
			else
			{
				if(s.startsWith("REGISTERED TORECV"))
					done = true;
				else if(s.startsWith("ERROR 100"))
					sent = false;
				else if(s.startsWith("ERROR 107"))
					return false;
				else
					System.out.println(s);
				
				in.readLine();
			}
			
		}while(!done);
		return true;
	}
}