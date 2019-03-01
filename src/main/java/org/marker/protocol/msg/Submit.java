package org.marker.protocol.msg;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

import org.marker.protocol.tools.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Submit extends Message implements Serializable {

	/**
	* 
	*/
	private static Logger logger = LoggerFactory.getLogger(Submit.class);
	
	private static final long serialVersionUID = -2984923166106556374L;

	public  int Message_Length;

	public final int Command_Id = SGIP_SUBMIT;

	public String Sequence_Number;

	public long gatewayId;
	public long gatewayts;
	public long gatewaySeqId;
	//21
	public String SPNumber;// ֮��Ϊ������
	//21
	public String ChargeNumber;
	//1
	public int UserCount;
	//21
	public String UserNumber;// ����ֻ�����
	//5
	public String CorpId;
	//10
	public String ServiceType;
	//1
	public int FeeType;
	//6
	public String FeeValue;
	//6
	public String GivenValue;
	//1
	public int AgentFlag;
	//1
	public int MorelatetoMTFlag;
	//1
	public int Priority;
	//16
	public String ExpireTime;// ����Ϣ��������ֹʱ��
	//16
	public String ScheduleTime;// ����Ϣ��ʱ���͵�ʱ��
	//1
	public int ReportFlag;// 1ģ�����У� 2ģ������
	//1
	public int TP_pid;
	//1
	public int TP_udhi;
	//1
	public int MessageCoding;
	//1
	public int MessageType;
	//4
	public int MessageLength;
	//
	public byte [] MessageContent;
	//8
	public String Reserve;

	public static Submit parse(ByteBuf req) {
		int length;
		Submit pkg = new Submit();
		req.readInt();// 4
		req.readInt();// 8
		pkg.gatewayId=new BigInteger(Integer.toHexString(req.readInt()), 16).longValue();
		//logger.info("ggggggggggggggggggggggggggg={}",pkg.gatewayId);
		pkg.gatewayts=req.readInt();
		pkg.gatewaySeqId=req.readInt();
		pkg.SPNumber = new String(req.readBytes(21).array()).trim();// 41
		pkg.ChargeNumber = new String(req.readBytes(21).array()).trim();// 62
		pkg.UserCount = (int) req.readByte(); // 63
		
		pkg.UserNumber = new String(req.readBytes(21).array()).trim();// 84
		pkg.CorpId = new String(req.readBytes(5).array()).trim();// 89
		pkg.ServiceType = new String(req.readBytes(10).array()).trim();// 99
		pkg.FeeType = (int) req.readByte(); // 100
		
		
		pkg.FeeValue = new String(req.readBytes(6).array()).trim();// 106
		pkg.GivenValue = new String(req.readBytes(6).array()).trim();// 112
		pkg.AgentFlag = (int) req.readByte(); // 113
		pkg.MorelatetoMTFlag = (int) req.readByte(); // 114
		pkg.Priority = (int) req.readByte(); // 115
		
		
		pkg.ExpireTime = new String(req.readBytes(16).array()).trim();// 131
		pkg.ScheduleTime = new String(req.readBytes(16).array()).trim();// 147
		pkg.ReportFlag = (int) req.readByte(); // 148
		pkg.TP_pid = (int) req.readByte(); // 149
		pkg.TP_udhi = (int) req.readByte(); // 150
		pkg.MessageCoding = (int) req.readByte(); // 151
		pkg.MessageType = (int) req.readByte(); // 152
		pkg.MessageLength = req.readInt();//156
		length =pkg.MessageLength;
		pkg.MessageContent =  req.readBytes(length).array();//156+ ?
		req.readBytes(8);// ?
		logger.debug("s1={} s2={} s3={}",pkg.gatewayId,pkg.gatewayts,pkg.gatewaySeqId);
		String ts = String.valueOf(pkg.gatewayts).length()<10 ?"0"+String.valueOf(pkg.gatewayts):String.valueOf(pkg.gatewayts);
		pkg.Sequence_Number = ""+pkg.gatewayId+ts+pkg.gatewaySeqId;
		return pkg;
	}

	public ByteBuf toBytes() {
		Message_Length = 164+ MessageLength;
		ByteBuf bs = Unpooled.buffer(Message_Length);
		
		bs.writeInt(Message_Length);// 4
		bs.writeInt(Command_Id);// 8
		// all sec
		bs.writeBytes(Utils.LongToBytes4(gatewayId));
		bs.writeBytes(Utils.LongToBytes4(gatewayts));
		bs.writeBytes(Utils.LongToBytes4(gatewaySeqId));

		bs.writeBytes(SPNumber.getBytes());// 41
		bs.writeZero(21-SPNumber.getBytes().length);
		
		bs.writeBytes(ChargeNumber.getBytes());// 62
		bs.writeByte(UserCount);//63
		
		bs.writeBytes(UserNumber.getBytes());// 84
		bs.writeZero(21-UserNumber.getBytes().length);
		
		bs.writeBytes(CorpId.getBytes());// 89
		bs.writeBytes(ServiceType.getBytes());// 99
		bs.writeByte(FeeType);//100
		bs.writeBytes(FeeValue.getBytes());// 106
		bs.writeBytes(GivenValue.getBytes());// 112
		bs.writeByte(AgentFlag);//113
		bs.writeByte(MorelatetoMTFlag);//114
		bs.writeByte(Priority);//115
		bs.writeBytes(ExpireTime.getBytes());// 131
		bs.writeBytes(ScheduleTime.getBytes());//147 
		
		bs.writeByte(ReportFlag);//148
		bs.writeByte(TP_pid);//149
		bs.writeByte(TP_udhi);//150
		bs.writeByte(MessageCoding);//151
		bs.writeByte(MessageType);//152
		bs.writeInt(MessageLength);//156
		
		bs.writeBytes(MessageContent);// 156+?
		bs.writeBytes(Reserve.getBytes());// 164 +?

		return bs;
	}

	@Override
	public String toString() {
		return "SgipSubmit [Message_Length=" + Message_Length + ", Command_Id=" + Command_Id + ", Sequence_Number="
				+ Sequence_Number + ", gatewayId=" + gatewayId + ", gatewayts=" + gatewayts + ", gatewaySeqId="
				+ gatewaySeqId + ", SPNumber=" + SPNumber + ", ChargeNumber=" + ChargeNumber + ", UserCount="
				+ UserCount + ", UserNumber=" + UserNumber + ", CorpId=" + CorpId + ", ServiceType=" + ServiceType
				+ ", FeeType=" + FeeType + ", FeeValue=" + FeeValue + ", GivenValue=" + GivenValue + ", AgentFlag="
				+ AgentFlag + ", MorelatetoMTFlag=" + MorelatetoMTFlag + ", Priority=" + Priority + ", ExpireTime="
				+ ExpireTime + ", ScheduleTime=" + ScheduleTime + ", ReportFlag=" + ReportFlag + ", TP_pid=" + TP_pid
				+ ", TP_udhi=" + TP_udhi + ", MessageCoding=" + MessageCoding + ", MessageType=" + MessageType
				+ ", MessageLength=" + MessageLength + ", MessageContent=" + Arrays.toString(MessageContent)
				+ ", Reserve=" + Reserve + "]";
	}
	
	
	public static void main(String[] args) {
		//System.out.println();
		int t = -1294847286;
		long str = new BigInteger(Integer.toHexString(t),16).longValue();
		System.out.println(str);
	}

	@Override
	public ByteBuf toResp(int result) {
		ByteBuf buf = toResp(SGIP_SUBMIT_RESP, result);
		return buf;
	}
}
