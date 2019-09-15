import java.io.OutputStream;
import java.io.PrintWriter;

class Writer
{
	private PrintWriter pw = null;
	
	Writer(OutputStream o)
	{
		pw = new PrintWriter(o);
	}

	public void println(String s)
	{
		pw.println(s);
		pw.flush();
	}

	public void print(String s)
	{
		pw.print(s);
		pw.flush();
	}

	public boolean checkError()
	{
		return pw.checkError();
	}
}