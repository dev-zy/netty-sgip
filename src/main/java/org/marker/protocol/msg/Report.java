package org.marker.protocol.msg;

import java.math.BigInteger;
import java.util.Arrays;

import org.marker.protocol.tools.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Report extends Message {
	public final int totalLength = 64;
	public final int commandId = SGIP_REPORT;
	public long gatewayId;
	public long gatewayts;
	public long gatewaySeqId;
	//4
	public long nodeId;
	//4
	public long timestamp;
	//4
	public long sequeceId;
	//1
	public int reportType;
	//21
	public String userNumber;
	//1
	public int state;
	//1
	public int errcode;
	//8
	public byte[] reserve;
	
	@Override
	public ByteBuf toBytes() {
		ByteBuf byteBuf = Unpooled.buffer(totalLength);
		byteBuf.writeInt(totalLength);
		byteBuf.writeInt(commandId);
		
		byteBuf.writeBytes(Utils.LongToBytes4(gatewayId));
		byteBuf.writeBytes(Utils.LongToBytes4(gatewayts));
		byteBuf.writeBytes(Utils.LongToBytes4(gatewaySeqId));
		byteBuf.writeBytes(Utils.LongToBytes4(nodeId));
		byteBuf.writeBytes(Utils.LongToBytes4(timestamp));
		byteBuf.writeBytes(Utils.LongToBytes4(sequeceId));
		
		byteBuf.writeByte(reportType);
		byteBuf.writeBytes(userNumber.getBytes());
		byteBuf.writeZero(21-userNumber.length());
		byteBuf.writeByte(state);
		byteBuf.writeByte(errcode);
		byteBuf.writeZero(8);
		return byteBuf;
	}
	
	public static Report parse(ByteBuf byteBuf){
		Report report = new Report();
		if(byteBuf.readInt() != report.totalLength){
			return null;
		}
		if(byteBuf.readInt() != SGIP_REPORT){
			return null;
		}
		//Sequence Number
		
		report.gatewayId = new BigInteger(Integer.toHexString(byteBuf.readInt()), 16).longValue();
		report.gatewayts = byteBuf.readInt();
		
		report.gatewaySeqId = byteBuf.readInt();
		
		//SubmitSequenceNumber
		
		report.nodeId = byteBuf.readInt();
		report.timestamp = byteBuf.readInt();
		report.sequeceId = byteBuf.readInt();
		
		report.reportType = byteBuf.readByte();
		byte[] bs = new byte[21];
		byteBuf.readBytes(bs, 0, 21);
		report.userNumber = new String(bs).trim();
		report.state = byteBuf.readByte();
		report.errcode = byteBuf.readInt();
		
		return report;
	}

	@Override
	public String toString() {
		return "SGIPReport [totalLength=" + totalLength + ", commandId=" + commandId + ", gatewayId=" + gatewayId
				+ ", gatewayts=" + gatewayts + ", gatewaySeqId=" + gatewaySeqId + ", nodeId=" + nodeId + ", timestamp="
				+ timestamp + ", sequeceId=" + sequeceId + ", reportType=" + reportType + ", userNumber=" + userNumber
				+ ", state=" + state + ", errcode=" + errcode + ", reserve=" + Arrays.toString(reserve) + "]";
	}

	@Override
	public ByteBuf toResp(int result) {
		ByteBuf buf = toResp(SGIP_REPORT_RESP, result);
		return buf;
	}
}
