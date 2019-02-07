/*************************************************************************
 *  @author Jack Anderson
 *  @author jja54
 *
 *  Compilation:  javac LZWmod.java
 *  Execution:    java LZWmod - < input.txt > output.lzw  (compress)
	Execution:    java LZWmod - r < input.txt > output.lzw (compress with dictionary reset)
 *  Execution:    java LZWmod + < input.lzw > output.txt  (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/
import java.io.*;
 
public class LZWmod {
    private static final int R = 256;        // Number of input chars
    private static int W = 9;         // Intial codeword width
	private static int L = (int)Math.pow(2, W); // Number of codewords = 2^W
	private static final int MAX_LENGTH = 16;
	private static final int MIN_LENGTH = 9;
	private static boolean reset = false;

    public static void compress() throws IOException 
	{ 
		StringBuilder w = new StringBuilder();
		
        TSTsb<Integer> st = new TSTsb<Integer>();
		// Filling the codebook with all single chars initially
		StringBuilder sb;
        for (int i = 0; i < R; i++)
		{
			sb = new StringBuilder("" + (char) i);
            st.put(sb, i);
		}
        int code = R+1;  // R is codeword for EOF
		
		if (reset == true)
			BinaryStdOut.write((byte) 'r');
		else
			BinaryStdOut.write((byte) 'n'); // n is arbitrary char - signals not reset mode

		while (!BinaryStdIn.isEmpty()) // While the stream is ready to be read, not EOF(?)
		{
			char c = 'c'; // Initialzing with abitrary char, will be overwritten no matter what
			c = BinaryStdIn.readChar();
			w.append(c); // Add the char to the test string
			// wc = w + c
			if (st.contains(w))
				continue; // w stays the same (becomes w+c), move on to next char
			else
			{
				// Output code for w WITHOUT the c
				w.deleteCharAt(w.length()-1);
				
				if (st.get(w) != null)
					BinaryStdOut.write(st.get(w), W);
				// Add wc to the dictionary
				w.append(c);
				if (code < L)
					st.put(w, code++);
				else if (code >= L) // Codebook is full
				{
					if (W < MAX_LENGTH) // We can increase codeword width still
					{
						W++;
						L = (int)Math.pow(2, W);
						st.put(w, code++);
					}
					else if (W == MAX_LENGTH) // Maxed out dict, check if reset
					{
						if (reset)
						{
							st = new TSTsb<Integer>();
							for (int i = 0; i < R; i++)
							{
								sb = new StringBuilder("" + (char) i);
								st.put(sb, i);
							}
							code = R+1;
							W = MIN_LENGTH;
							L = (int)Math.pow(2, W);
							st.put(w, code++); // Add the new codeword
						}
						// Else just keep reusing the same codebook, move on
					}
				}
				// w just becomes c, moving to next char
				String s = String.valueOf(c);
				w.replace(0, w.length(), s);
			}
		}
		BinaryStdOut.write(st.get(w), W); // Print out the last char of the file
		
		BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() 
	{
        String[] st = new String[(int)Math.pow(2, MAX_LENGTH)]; // Initialize with max space for codewords
        int i; // Next available codeword value

        // Initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

		char modeType = BinaryStdIn.readChar();
		if (modeType == 'r')
			reset = true;
		// Else let codebook fill and keep using
		
        int codeword = BinaryStdIn.readInt(W);
        String val = st[codeword];

        while (true) 
		{
            BinaryStdOut.write(val);
			
			if (i >= L) // Current codeword val is outside codebook slot range
			{
				if (W < MAX_LENGTH) // We can increase codeword width still
				{
					W++;
					L = (int)Math.pow(2, W);
				}
				else if (W == MAX_LENGTH) // Maxed out dict, check if reset
				{
					if (reset)
					{
						st = new String[L];
						for (i = 0; i < R; i++)
							st[i] = "" + (char) i;
						st[i++] = "";
						
						i = R+1;
						W = MIN_LENGTH;
						L = (int)Math.pow(2, W);
					}
				}
				// Else just keep reusing the same codebook, move on
			}
			
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) 
				s = val + val.charAt(0);   // Special case hack
            if (i < L)
				st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }
	
    public static void main(String[] args) 
	{
		if (args.length > 1)
		{
			if (args[1].equals("r"))
				reset = true;
		}
		try
		{
			if      (args[0].equals("-")) compress();
			else if (args[0].equals("+")) expand();
			else throw new RuntimeException("Illegal command line argument");
		}
		catch (IOException e)
		{
			System.out.println(e);
			System.exit(1);
		}
    }
}
