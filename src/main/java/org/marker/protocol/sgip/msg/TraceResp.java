package org.marker.protocol.sgip.msg;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.marker.protocol.tools.Utils;

public class TraceResp extends Message {

	private int count;//1
	private int result;//1
	private String nodeId;//6
	private String receiveTime;//16
	private String sendTime;//16
	private String reserve;//8

	public TraceResp() {
		super(Message.SGIP_TRACE_RESP);
		getHead().setLength(getLength() + 48);
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	/**
	  * 描述: Trace命令在该节点是否成功接收(0：接收成功,1：等待处理,其它：错误码)
	  * 时间: 2019年1月30日 下午9:16:04
	  * @return
	 */
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	/**
	  * 描述: 节点编号
	  * 时间: 2019年1月30日 下午9:17:35
	 */
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	/**
	  * 描述: yymmddhhmmss
	  * 时间: 2019年1月30日 下午9:14:59
	 */
	public String getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(String receiveTime) {
		this.receiveTime = receiveTime;
	}
	/**
	  * 描述: yymmddhhmmss
	  * 时间: 2019年1月30日 下午9:14:59
	 */
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	public String getReserve() {
		return reserve;
	}
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	public void write(OutputStream out) throws IOException {
		getHead().write(out);
		out.write(count);
		out.write(result);
		byte[] bs = new byte[6];
		if (nodeId != null)System.arraycopy(nodeId.getBytes(), 0, bs, 0,Math.min(nodeId.getBytes().length, 6));
		out.write(bs);
		bs = new byte[16];
		if (receiveTime != null)System.arraycopy(receiveTime.getBytes(), 0, bs, 0,Math.min(receiveTime.getBytes().length, 16));
		out.write(bs);
		bs = new byte[16];
		if (sendTime != null)System.arraycopy(sendTime.getBytes(), 0, bs, 0,Math.min(sendTime.getBytes().length, 16));
		out.write(bs);
		bs = new byte[8];
		if (reserve != null)System.arraycopy(reserve.getBytes(), 0, bs, 0,Math.min(reserve.getBytes().length, 8));
		out.write(bs);
		out.flush();
	}

	public void read(InputStream in) throws IOException {
		byte bs[] = new byte[48];
		int rc = in.read(bs);
		if (rc < 48) {
			throw new EOFException(String.valueOf(rc));
		} else {
			count = bs[0];
			result = bs[1];
			nodeId = Utils.getString(bs, 2, 6);
			receiveTime = Utils.getString(bs, 8, 16);
			sendTime = Utils.getString(bs, 24, 16);
			reserve = Utils.getString(bs, 40, 8);
			return;
		}
	}
}
