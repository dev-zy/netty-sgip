package org.marker.protocol.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Sequence {
	private static long count = 1;
	private static final ConcurrentHashMap<Integer, AtomicInteger> ATOMICS = new ConcurrentHashMap<Integer, AtomicInteger>();
	
	public static synchronized long next() {
		if (count >= 0x7fffffffL)
			count = 1;
		return count++;
	}
	
	public static int getId(int cid) {
		if(!ATOMICS.containsKey(cid)||ATOMICS.get(cid).get()==0xffff) {
			if(!ATOMICS.containsKey(cid)) {
				ATOMICS.put(cid, new AtomicInteger(0));
			}else {
				ATOMICS.get(cid).set(0);
			}
		}
		int sid = (ATOMICS.get(cid).getAndIncrement() % 0xffff)+1;
		return sid;
	}
	private static SimpleDateFormat sequenceDate = new SimpleDateFormat("MMddHHmmss");
	public static long getCurrentSequenceDate()
	{
		return Long.valueOf(sequenceDate.format(new Date()));
	}
	public static void main(String[] args) {
		while(true){
			System.out.println(Sequence.next());
		}
	}
}
