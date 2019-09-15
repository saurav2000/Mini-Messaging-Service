import java.net.Socket;
import java.io.IOException;

public class ServerThread implements Runnable
{
	private Socket socket = null;
	private Server server = null;
	//In and out are of sender socket of server
	private Reader in = null;
	private Writer out = null;
	private int mode = -1;

	ServerThread(Socket s, Server server, int mode)
	{
		socket = s;
		this.server = server;
		try
		{
			in = new Reader(socket.getInputStream());
			out = new Writer(socket.getOutputStream());
			this.mode = mode;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		String username = null;
		boolean rcv = false;

		//First loop is for registration
		while(true)
		{
			String s = in.readLine();
			if(s==null)
				continue;
			
			in.readLine();
			
			if(s.startsWith("REGISTER TORECV"))
			{
				rcv = true;
				username = s.substring(16, s.lastIndexOf(32));
				int m = Integer.parseInt(s.substring(s.lastIndexOf(32)).trim());
				if(m!=mode)
				{
					out.print("ERROR 107 INVALID MODE\n\n");
					return;
				}
				if(isValidUsername(username))
					out.print("REGISTERED TORECV "+username+"\n\n");
				else
				{
					out.print("ERROR 100 Malformed Username\n\n");
					continue;
				}
			}
			else if(s.startsWith("REGISTER TOSEND"))
			{
				username = s.substring(16, s.lastIndexOf(32));
				int m = Integer.parseInt(s.substring(s.lastIndexOf(32)).trim());
				if(m!=mode)
				{
					out.print("ERROR 107 INVALID MODE\n\n");
					return;
				}
				if(isValidUsername(username))
					out.print("REGISTERED TOSEND "+username+"\n\n");
				else
				{
					out.print("ERROR 100 Malformed Username\n\n");
					continue;
				}	
			}
			else
				System.out.println(s);
			break;
		}

		//If receiver thread insert socket and delete
		if(rcv)
		{
			server.insert(username, socket);
			server.updateCount(username);
			System.out.println(username+" recv reg done");
			return;
		}                                                                                                                  

		System.out.println(username+" send reg done");
		//To ensure count is 2 for fully 
		//registering username
		server.updateCount(username);
		

		boolean recv = true, hash = false;
		//br and pw are of the recipient thread of server
		Reader br = null;
		Writer pw = null;
		String recip = null;
		while(true)
		{
			//When receiving comm from sender thread of sender
			if(recv)
			{
				recip = null;
				String s = null, header1=null, header2 = null, message = null, signature=null;
				int l1 = 0, l2 = 0;
				br = null;
				pw = null;

				s = in.readLine();
				if(s==null)
					continue;

				//Sending message
				if(s.startsWith("SEND"))
				{
					recip = s.substring(5);
					header1 = in.readLine();
					if(header1==null)
					{
						out.print("ERROR 103 Header incomplete\n\n");
						continue;	
					}
					l1 = Integer.parseInt(header1.substring(16));
					if(mode==2)
					{
						header2 = in.readLine();
						if(header2==null)
						{
							out.print("ERROR 103 Header incomplete\n\n");
							continue;
						}
						l2 = Integer.parseInt(header2.substring(18));
					}
					if(!in.readLine().equals(""))
					{
						out.print("ERROR 103 Header incomplete\n\n");
						continue;
					}
					message = in.read(l1);
					in.readLine();
					if(mode==2)
					{	signature = in.read(l2);
						in.readLine();
					}
				}
				//Sending fetchkey to recipient for encryption
				else if(s.startsWith("FETCHKEY"))
				{
					recip = s.substring(9);
					in.readLine();
				}
				else if(s.startsWith("DEREGISTER"))
				{
					server.delete(username);
					in.readLine();
					return;
				}

				//Checking if recipient exists				
				if(!server.contains(recip))
				{
					out.print("ERROR 101 No User Registered as "+recip+"\n\n");
					continue;
				}

				Socket recipSocket = server.get(recip);
				try
				{
					br = new Reader(recipSocket.getInputStream());
					pw = new Writer(recipSocket.getOutputStream());
				}
				catch(IOException e){e.printStackTrace();}

				if(br==null||pw ==null)
					continue;
				//Sending message
				if(s.startsWith("SEND"))
				{

					if(mode==2)
						pw.print("FORWARD "+username+"\n"+header1+"\n"+header2+"\n\n"+message+"\n"+signature+"\n");
					else
						pw.print("FORWARD "+username+"\n"+header1+"\n\n"+message+"\n");
					try
					{
						System.out.println(username + " TO "+recip+"\n"+(new String(message.getBytes(), "UTF-8"))+"\n");
						System.out.println();	
					}catch(Exception e){}
					
				}
				//Sending fetchkey to recipient for encryption
				else if(s.startsWith("FETCHKEY"))
				{
					pw.print("FETCHKEY"+"\n\n");
				}

				recv = false;
			}

			//When receiving comm from receiver thread of recipient
			else
			{
				//Sending public key of sender to recipient
				if(hash)
				{
					try
					{
						//Reading from receiver thread of this username for public key
						Reader r = new Reader(server.get(username).getInputStream());
						String s = r.readLine();
						if(s==null)
							continue;
						if(s.startsWith("FETCHEDKEY"))
						{
							int l = Integer.parseInt(r.readLine());
							pw.print("FETCHEDKEY\n"+l+"\n"+r.read(l)+"\n");
							r.readLine();r.readLine();
							hash = false;
						}
					}
					catch(IOException e){e.printStackTrace();}
				}

				String s = br.readLine();
				if(s==null)
					continue;

				//Acknowledgement message
				if(s.startsWith("RECEIVED"))
				{
					br.readLine();
					out.print("SENT "+recip+"\n\n");
				}
				//For encryption of message
				else if(s.startsWith("FETCHEDKEY"))
				{
					s = br.readLine();
					String key = br.read(Integer.parseInt(s));
					br.readLine();
					br.readLine();
					out.print("FETCHEDKEY\n"+s+"\n"+key+"\n\n");
				}
				else if(s.startsWith("ERROR 102"))
				{
					br.readLine();
					out.print(s+"\n\n");
				}
				else if(s.startsWith("ERROR 103"))
				{
					br.readLine();
					out.print("ERROR 102 Recipient Server communication failed"+"\n\n");
				}
				else if(s.startsWith("ERROR 106"))
				{
					br.readLine();
					out.print(s+"\n\n");
				}

				recv = true;

				//If recipient wants public key of sender
				//for signature check
				if(s.startsWith("FETCHKEY"))
				{
					br.readLine();
					try
					{
						Writer w = new Writer(server.get(username).getOutputStream());
						w.print("FETCHKEY\n\n");
					}
					catch(IOException e){e.printStackTrace();}
					recv = false;
					hash = true;
				}
			}
		}
	}

	public boolean isValidUsername(String s)
	{
		if(server.contains(s))
			return false;
		for(int i=0;i<s.length();++i)
		{
			if(!Character.isLetterOrDigit(s.charAt(i)))
				return false;
		}
		return true;
	}
}

