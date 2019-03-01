package org.marker.protocol.msg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class UnBind extends Message {
	// 4
	public final int Total_Length = 12;
	// 4
	public final int Command_Id = SGIP_UNBIND;
	// 4
	public int Sequence_Id;

	public static UnBind parse(ByteBuf req) {
		UnBind pkg = new UnBind();
		req.readInt();// 4
		req.readInt();// 8
		pkg.Sequence_Id = req.readInt();// 12
		return pkg;
	}

	public ByteBuf toBytes() {
		ByteBuf bs = Unpooled.buffer(Total_Length);
		bs.writeInt(Total_Length);// 4
		bs.writeInt(Command_Id);// 8
		bs.writeInt(Sequence_Id);// 12
		return bs;
	}

	@Override
	public String toString() {
		return "SgipTerminateRequest [Total_Length=" + Total_Length + ", Command_Id=" + Command_Id + ", Sequence_Id=" + Sequence_Id + "]";
	}

	@Override
	public ByteBuf toResp(int result) {
		ByteBuf buf = toResp(SGIP_UNBIND_RESP, result);
		return buf;
	}

}
