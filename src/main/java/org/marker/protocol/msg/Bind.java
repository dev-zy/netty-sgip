package org.marker.protocol.msg;

import java.io.Serializable;
import java.math.BigInteger;

import org.marker.protocol.tools.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Bind extends Message implements Serializable {

	/**
	 * bind
	 */
	private static final long serialVersionUID = -5119798112262477203L;
		
		public final int Message_Length = 61;
		
		public final int Command_Id = SGIP_BIND;
		
		public String Sequence_Number ;
		
		public long gatewayId;
		public long gatewayts;
		public long gatewaySeqId;
		// 1
		public int Login_Type;
		// 16
		public String Login_Name;
		// 16
		public String Login_Passowrd;
		// 8
		public String Reserve;

		
		public static Bind parse(ByteBuf req) {
			Bind pkg = new Bind();
			pkg.parse(req, false);
			pkg.gatewayId=new BigInteger(Integer.toHexString(req.readInt()), 16).longValue();
			req.readBytes(8);//20
			pkg.Login_Type = (int) req.readByte();   // 21
			pkg.Login_Name=new String(req.readBytes(16).array());// 37
			pkg.Login_Passowrd=new String(req.readBytes(16).array());// 53
			pkg.Reserve = new String(req.readBytes(8).array());// 61
			return pkg;
		}

		public ByteBuf toBytes() {
			ByteBuf bs = Unpooled.buffer(Message_Length);
			bs.writeInt(Message_Length);//4
			bs.writeInt(Command_Id);//8
			//all sec
			bs.writeBytes(Utils.LongToBytes4(gatewayId));
			bs.writeBytes(Utils.LongToBytes4(gatewayts));
			bs.writeBytes(Utils.LongToBytes4(gatewaySeqId));
			
			bs.writeByte(Login_Type);//21
			bs.writeBytes(Login_Name.getBytes());//37
			bs.writeZero(16-Login_Name.length());
			bs.writeBytes(Login_Passowrd.getBytes());// 53
			bs.writeZero(16-Login_Passowrd.length());
			bs.writeZero(8);// 61
			
			return bs;
		}

		@Override
		public String toString() {
			return "Bind [Message_Length=" + Message_Length + ", Command_Id=" + Command_Id + ", Sequence_Number="
					+ Sequence_Number + ", gatewayId=" + gatewayId + ", gatewayts=" + gatewayts + ", gatewaySeqId="
					+ gatewaySeqId + ", Login_Type=" + Login_Type + ", Login_Name=" + Login_Name + ", Login_Passowrd="
					+ Login_Passowrd + ", Reserve=" + Reserve + "]";
		}

		@Override
		public ByteBuf toResp(int result) {//BindResp
			ByteBuf buf = toResp(SGIP_BIND_RESP,result);
			return buf;
		}
}
