package org.marker.protocol.netty;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.marker.protocol.msg.Bind;
import org.marker.protocol.msg.Deliver;
import org.marker.protocol.msg.Message;
import org.marker.protocol.msg.Report;
import org.marker.protocol.msg.Submit;
import org.marker.protocol.msg.UnBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class SgipChannelHandler extends ChannelInboundHandlerAdapter{
	private static Logger logger = LoggerFactory.getLogger(SgipChannelHandler.class);
	private static ConcurrentHashMap<Boolean, List<ChannelHandlerContext>> ctxs = new ConcurrentHashMap<>();
	private boolean isServer;
	public SgipChannelHandler(boolean isServer){
		this.isServer = isServer;
	}
	public void setChannelContext(boolean isServer,ChannelHandlerContext ctx) {
		if(!ctxs.containsKey(isServer)) {
			ctxs.put(isServer, new ArrayList<ChannelHandlerContext>());
		}
		ctxs.get(isServer).add(ctx);
	}
	public ChannelHandlerContext getChannelContext(boolean isServer,String account) {
		List<ChannelHandlerContext> vctxs = ctxs.get(isServer);
		if(vctxs!=null&&!vctxs.isEmpty()) {
			int random = new Random().nextInt(vctxs.size());
			return vctxs.get(random);
		}
		return null;
	}
	@Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        ByteBuf buf = (ByteBuf)msg;
		ByteBuf req = (new UnpooledByteBufAllocator(false, true)).heapBuffer(buf.readableBytes());
		buf.readBytes(req);
		try {
			int commandID = req.getInt(4);
			logger.info("commandID:{} ",commandID);
			if (commandID == Message.SGIP_BIND) {//作为服务端
				Bind bind = Bind.parse(req);
				logger.info("bind:{} ",bind.toString());
				ByteBuf bresp = bind.toResp(Message.SGIP_BIND_SUCCESS);
				if (bind.gatewayId == 0) {
					setChannelContext(isServer, ctx);
					logger.info("登录成功，user ={},{}",bind.Login_Name,bresp);
					ctx.writeAndFlush(bresp);
				} else {
					logger.info("登录失败，result ={}",1);
					ctx.writeAndFlush(bresp);
					ctx.close();
					return;
				}
			}
			if (commandID<Message.SGIP_COMMON_RESP) {//作为客户端请求回执响应
				Message pkg = Message.head(req);
				int result = pkg.getRespResult(req);
				logger.info("--SGIP{cmd:{},status:{}}",Message.Command.valueOf(commandID),result==Message.SGIP_BIND_SUCCESS?"SUCCESS":"ERROR");
				if(commandID==Message.SGIP_BIND_RESP&&!isServer) {
					if (result != 0) {
						ctx.close();
						return;
					}else {
						setChannelContext(false, ctx);
					}
				}
				if(commandID==Message.SGIP_UNBIND_RESP) {
					ctx.close();
					return;
				}
			}
			if (commandID == Message.SGIP_UNBIND) {//作为服务端
				UnBind unbind = UnBind.parse(req);
				ByteBuf bresp = unbind.toResp(Message.SGIP_BIND_SUCCESS);
				ctx.writeAndFlush(bresp);
				logger.info("client unbind ");
				ctx.close();
//				ctx.writeAndFlush(null).addListener(ChannelFutureListener.CLOSE);
			}
			if (commandID == Message.SGIP_SUBMIT) {//作为服务端
				Submit submit = Submit.parse(req);
				ByteBuf bresp = submit.toResp(Message.SGIP_BIND_SUCCESS);
				if(bresp!=null) {
					ctx.writeAndFlush(bresp);
				}
			}
			if (commandID == Message.SGIP_REPORT) {//作为服务端
				Report report = Report.parse(req);
				ByteBuf bresp = report.toResp(Message.SGIP_BIND_SUCCESS);
				ctx.writeAndFlush(bresp);
			}
			if (commandID == Message.SGIP_DELIVER) {//作为服务端
				Deliver deliver = Deliver.parse(req);
				ByteBuf bresp = deliver.toResp(Message.SGIP_BIND_SUCCESS);
				ctx.writeAndFlush(bresp);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (req.readableBytes() > 0) {
				byte[] dirtyData = new byte[req.readableBytes()];
				req.readBytes(dirtyData);
			}
			try {
				ReferenceCountUtil.release(buf);
				ReferenceCountUtil.release(req);
			} catch (Exception ex) {
			}
		}
    }

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		SocketAddress address = ctx!=null&&ctx.channel()!=null?ctx.channel().remoteAddress():null;
		logger.info("--[{}]SGIP接入服务信息:{}", (isServer?"Server":"Client"),(address!=null?address.toString():""));
//		sessionManager.channelActive(ctx);
//		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		SocketAddress address = ctx!=null&&ctx.channel()!=null?ctx.channel().remoteAddress():null;
		logger.info("--[{}]SGIP断开服务信息:{}", (isServer?"Server":"Client"),(address!=null?address.toString():""));
//		sessionManager.channelInactive(ctx);
//		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, Throwable cause) {
		SocketAddress address = ctx!=null&&ctx.channel()!=null?ctx.channel().remoteAddress():null;
		logger.error("--[{"+(isServer?"Server":"Client")+"}]SGIP异常信息:"+(address!=null?address.toString():""),cause);
//		sessionManager.channelInactive(ctx);
//		super.exceptionCaught(ctx, cause);
	}
	
	public void sendMsg(boolean isServer,String appName,Message pkg) {//作为客户端请求
		ChannelHandlerContext ctx = getChannelContext(isServer, appName);
		if(ctx!=null&&pkg!=null) {
			ctx.writeAndFlush(pkg.toBytes());
		}
	}
    public void sendBind(Channel channel,String username,String password) {//作为客户端请求
		if(!isServer) {//客户端
			Bind bind = new Bind();
			bind.gatewayId = 1;
			bind.gatewayts = 1;
			bind.gatewaySeqId = 1;
			bind.Login_Type = 1;
			bind.Login_Name = username;
			bind.Login_Passowrd = password;
			bind.Reserve = "12345678";
			channel.writeAndFlush(bind.toBytes());
			logger.info("--[{}]client{username:{},pwd:{}} login success!",Thread.currentThread().getName(),bind.Login_Name,bind.Login_Passowrd);
		}
	}
}
