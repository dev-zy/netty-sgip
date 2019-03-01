package org.marker.protocol.msg;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

import org.marker.protocol.tools.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Deliver extends Message {
	
	public int totalLength;
	public final int commandId = SGIP_DELIVER;
	public long gatewayId;
	public long gatewayts;
	public long gatewaySeqId;
	
	//21
	public String userNumber;
	//21
	public String spNumber;
	//1
	public int tpPid;
	//1
	public int tpUdhi;
	//1
	public int msgCoding;
	//4
	public int msgLength;
	//
	public String msgContent;
	//8
	public byte[] reserve;
	
	@Override
	public ByteBuf toBytes() {
		byte [] contentBytes = null;
		try {
			if(msgCoding==8) {
				contentBytes = msgContent.getBytes("UnicodeBigUnmarked");
			}else if (msgCoding==15) {
				contentBytes =msgContent.getBytes("gbk");
			}else {
				contentBytes =msgContent.getBytes();
			}
		}catch (Exception e) {
			System.out.println("编码异常！");
		}
		msgLength = contentBytes.length;
		totalLength = msgLength+77;
		ByteBuf byteBuf  = Unpooled.buffer(totalLength);
		byteBuf.writeInt(totalLength);
		byteBuf.writeInt(commandId);
		byteBuf.writeBytes(Utils.LongToBytes4(gatewayId));
		byteBuf.writeBytes(Utils.LongToBytes4(gatewayts));
		byteBuf.writeBytes(Utils.LongToBytes4(gatewaySeqId));
		//head ---20--- head-------------------------
		
		byteBuf.writeBytes(userNumber.getBytes());
		byteBuf.writeZero(21-userNumber.length());
		byteBuf.writeBytes(spNumber.getBytes());
		byteBuf.writeZero(21-spNumber.length());
		
		byteBuf.writeByte(tpPid);
		byteBuf.writeByte(tpUdhi);
		byteBuf.writeByte(msgCoding);
		byteBuf.writeInt(msgLength);
		byteBuf.writeBytes(contentBytes);
		
		byteBuf.writeZero(8);
		return byteBuf;
	}

	public static Deliver parse(ByteBuf byteBuf){
		Deliver deliver = new Deliver();
		// 4 length
		deliver.totalLength = byteBuf.readInt();
		
		if(deliver.totalLength != byteBuf.readableBytes()+4){
			return null;
		}
		// 4 commandId
		if(byteBuf.readInt() != deliver.commandId){
			return null;
		}
		//8-12
		deliver.gatewayId = new BigInteger(Integer.toHexString(byteBuf.readInt()), 16).longValue();
		// 12 - 16
		deliver.gatewayts = byteBuf.readInt();
		// 16 - 20
		deliver.gatewaySeqId = byteBuf.readInt();
		
		byte[] bs = new byte[21];
		byteBuf.readBytes(bs, 0, 21);
		// 20 - 41
		deliver.userNumber = new String(bs).trim();
		byteBuf.readBytes(bs, 0, 21);
		// 41 - 62
		deliver.spNumber = new String(bs).trim();
		// 62
		deliver.tpPid = byteBuf.readByte();
		// 63
		deliver.tpUdhi = byteBuf.readByte();
		// 64
		deliver.msgCoding = byteBuf.readByte();
		// 65-69
		deliver.msgLength = byteBuf.readInt();
		// 69 -69+X
		if(deliver.msgLength > 0){
			bs = new byte[deliver.msgLength];
			byteBuf.readBytes(bs, 0, deliver.msgLength);
			if(deliver.msgCoding == 0){
				deliver.msgContent = new String(bs);
			}else if(deliver.msgCoding == 15){
				try {
					deliver.msgContent = new String(bs, "GBK");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else if(deliver.msgCoding == 8){
				try {
					deliver.msgContent = new String(bs, "UnicodeBigUnmarked");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else{
				deliver.msgContent = new String(bs);
			}
		}
		// 69+X - 77+X
		deliver.reserve = new byte[8];
		byteBuf.readBytes(deliver.reserve);		
		return deliver;
	}
	
	


	@Override
	public String toString() {
		return "SGIPDeliver [totalLength=" + totalLength + ", commandId=" + commandId + ", gatewayId=" + gatewayId
				+ ", gatewayts=" + gatewayts + ", gatewaySeqId=" + gatewaySeqId + ", userNumber=" + userNumber
				+ ", spNumber=" + spNumber + ", tpPid=" + tpPid + ", tpUdhi=" + tpUdhi + ", msgCoding=" + msgCoding
				+ ", msgLength=" + msgLength + ", msgContent=" + msgContent + ", reserve=" + Arrays.toString(reserve)
				+ "]";
	}


	@Override
	public ByteBuf toResp(int result) {
		ByteBuf buf = toResp(SGIP_DELIVER_RESP, result);
		return buf;
	}
}
