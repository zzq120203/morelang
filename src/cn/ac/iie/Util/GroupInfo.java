package cn.ac.iie.Util;

public class GroupInfo {
	public long gid;
	public int nr;
	public long simhash;
	public long ts;
	public long uids[];
	
	public static String toEncryption(Long a) {
		long b = (a << 17) | (a >>> (64 - 17));
		long c = b ^ 0xffffffffffffffffL;
		
		return String.format("%X", c);
	}
	
	public static long toDecrypt(String b) {
		long c = Long.parseLong(b, 16);
		long d = c ^ 0xffffffffffffffffL;
		long e = (d >>> 17) | (d << (64 - 17));
		
		return e;
	}
	
	public static void main(String[] argv) {
		System.out.println(toEncryption(7745789917L));
	}
}
