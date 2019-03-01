package org.marker.protocol.msg;

import java.math.BigInteger;

import org.marker.protocol.tools.Sequence;
import org.marker.protocol.tools.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 抽象消息类
 * @author marker
 * */
public abstract class Message {
	public static final int SGIP_BIND_SUCCESS = 0;
	public static final int SGIP_COMMON_RESP = 0x80000000;
	
	public static final int SGIP_BIND = 0x1;
	public static final int SGIP_BIND_RESP = 0x80000001;
	public static final int SGIP_UNBIND = 0x2;
	public static final int SGIP_UNBIND_RESP = 0x80000002;
	public static final int SGIP_SUBMIT = 0x3;
	public static final int SGIP_SUBMIT_RESP = 0x80000003;
	public static final int SGIP_DELIVER = 0x4;
	public static final int SGIP_DELIVER_RESP = 0x80000004;
	public static final int SGIP_REPORT = 0x5;
	public static final int SGIP_REPORT_RESP = 0x80000005;
	public static final int SGIP_USERRPT = 0x11;
	public static final int SGIP_USERRPT_RESP = 0x80000011;
	public static final int SGIP_TRACE = 0x1000;
	public static final int SGIP_TRACE_RESP = 0x80001000;
	
	public static final int SGIP_COMMON_RESP_LENGTH = 29;
	public static final int SGIP_UNBIND_AND_RESP_LENGTH = 20;
	
	public abstract ByteBuf toBytes();
	public abstract ByteBuf toResp(int result);
	
	protected int  len;//消息长度
	protected int  cmd;//消息命令
	protected long nid;//网关id
	protected long time;//时间
	protected long sn;//序号
	private String seqId;
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public int getCmd() {
		return cmd;
	}
	public void setCmd(int cmd) {
		this.cmd = cmd;
	}
	public long getNid() {
		return nid;
	}
	public void setNid(long nid) {
		this.nid = nid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getSn() {
		return sn;
	}
	public void setSn(long sn) {
		this.sn = sn;
	}
	public String getSeqId() {
		this.seqId = nid+""+((""+time).length()==10?time:"0"+time)+""+sn;//12
		return seqId;
	}
	public Message parse(ByteBuf req,boolean isReadSeqId) {
		this.len = req.readInt();// 4
		this.cmd = req.readInt();// 8
		this.nid = new BigInteger(Integer.toHexString(isReadSeqId?req.readInt():req.getInt(4)), 16).longValue();
		this.time = new BigInteger(Integer.toHexString(isReadSeqId?req.readInt():req.getInt(4)), 16).longValue();
		this.sn = new BigInteger(Integer.toHexString(isReadSeqId?req.readInt():req.getInt(4)), 16).longValue();
		return this;
	}
	public static Message head(ByteBuf req) {
		Message head = new Message() {
			@Override
			public ByteBuf toResp(int result) {
				return null;
			}
			@Override
			public ByteBuf toBytes() {
				return null;
			}
		};
		return head.parse(req,true);
	}
	public int getRespResult(ByteBuf req) {//针对response
		switch(this.cmd) {
		case SGIP_BIND_RESP:
		case SGIP_SUBMIT_RESP:
		case SGIP_DELIVER_RESP:
		case SGIP_REPORT_RESP:
		case SGIP_TRACE_RESP:
			return req.readInt();
		case SGIP_UNBIND:
		case SGIP_UNBIND_RESP:
			return 0;
		default:
			return -1;
		}
	}
	
	public ByteBuf toResp(int cmd,int result) {
		int len  = SGIP_COMMON_RESP_LENGTH;
		if(cmd==SGIP_UNBIND_RESP||cmd==SGIP_UNBIND) {
			len  = SGIP_UNBIND_AND_RESP_LENGTH;
		}
		ByteBuf buf = Unpooled.buffer(len);
		buf.writeInt(len);// 4
		buf.writeInt(cmd);// 8
		buf.writeBytes(Utils.LongToBytes4(nid>0?nid:1));//12
		buf.writeBytes(Utils.LongToBytes4(time>0?time:Sequence.getCurrentSequenceDate()));//16
		buf.writeBytes(Utils.LongToBytes4(sn>0?sn:Sequence.getId(cmd)));// 20
		if(len>20) {
			buf.writeByte(result);// 21
			buf.writeZero(8); //29
		}
		return buf;
	}
	public enum Command{
		SGIP_BIND(0x1),
		SGIP_BIND_RESP(0x80000001),
		SGIP_UNBIND(0x2),
		SGIP_UNBIND_RESP(0x80000002),
		SGIP_SUBMIT(0x3),
		SGIP_SUBMIT_RESP(0x80000003),
		SGIP_DELIVER(0x4),
		SGIP_DELIVER_RESP(0x80000004),
		SGIP_REPORT(0x5),
		SGIP_REPORT_RESP(0x80000005),
		SGIP_USERRPT(0x11),
		SGIP_USERRPT_RESP(0x80000011),
		SGIP_TRACE(0x1000),
		SGIP_TRACE_RESP(0x80001000),
		SGIP_CMD_NO_FOUND(0x87654321);
		
		private int cmd;
		private Command(int cmd){
			this.cmd = cmd;
		}
		public static Command valueOf(int cmd) {
			for(Command obj:values()) {
				if(cmd==obj.getCmd()) {
					return obj;
				}
			}
			return SGIP_CMD_NO_FOUND;
		}
		public int getCmd() {
			return cmd;
		}
	}
}
