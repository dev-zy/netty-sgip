package org.marker.protocol.sgip.msg;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.marker.protocol.tools.Sequence;
import org.marker.protocol.tools.Utils;
/**
  * 描述: 被跟踪MT短消息
  * 时间: 2019年1月30日 下午10:44:53
 */
public class Trace extends Message {
	//被跟踪MT短消息的命令序列号
	private String submitSequence;//12
	//被跟踪MT短消息的目的手机号，手机号码前加“86”国别标志
	private String userNumber;//21
	private String reserve;//8
	private long nodeId;
	public long getNodeId() {
		return nodeId;
	}
	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
		super.getHead().setSequence1(this.nodeId);//设置通信节点命令源
	}
	public Trace() {
		super(Message.SGIP_TRACE);
		getHead().setLength(getLength() + 41);
	}
	public Trace(long nodeId,String submitSequence,String userNumber, String reserve){
		super(Message.SGIP_TRACE);
		this.nodeId = nodeId;
		super.getHead().setSequence1(this.nodeId);//设置通信节点命令源
		super.getHead().setSequence2(Sequence.getCurrentSequenceDate());
		super.getHead().setSequence3(Sequence.getId(Message.SGIP_TRACE));
		this.submitSequence = submitSequence;
		this.userNumber = userNumber;
		this.reserve = reserve;
	}

	/**
	  * 描述: 被跟踪MT短消息的命令序列号
	  * 时间: 2019年1月30日 下午9:16:04
	  * @return
	 */
	public String getSubmitSequence() {
		return submitSequence;
	}
	public void setSubmitSequence(String submitSequence) {
		this.submitSequence = submitSequence;
	}
	/**
	  * 描述: 被跟踪MT短消息的目的手机号，手机号码前加“86”国别标志
	  * 时间: 2019年1月30日 下午9:16:04
	  * @return
	 */
	public String getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public String getReserve() {
		return reserve;
	}
	public void setReserve(String reserve) {
		this.reserve = reserve;
	}
	public void write(OutputStream out) throws IOException {
		getHead().setLength(getLength() + 41);
		getHead().write(out);
		byte[] bs = new byte[12];
		if (submitSequence != null)System.arraycopy(submitSequence.getBytes(), 0, bs, 0,Math.min(submitSequence.getBytes().length, 12));
		out.write(bs);
		bs = new byte[21];
		if (userNumber != null)System.arraycopy(userNumber.getBytes(), 0, bs, 0,Math.min(userNumber.getBytes().length, 21));
		out.write(bs);
		bs = new byte[8];
		if (reserve != null)System.arraycopy(reserve.getBytes(), 0, bs, 0,Math.min(reserve.getBytes().length, 8));
		out.write(bs);
		out.flush();
	}

	public void read(InputStream in) throws IOException {
		getHead().read(in);
		byte bs[] = new byte[41];
		int rc = in.read(bs);
		if (rc < 48) {
			throw new EOFException(String.valueOf(rc));
		} else {
			submitSequence = Utils.getString(bs, 0, 12);
			userNumber = Utils.getString(bs, 13, 21);
			reserve = Utils.getString(bs, 33, 8);
			return;
		}
	}
}
