package org.marker.protocol.netty.client;

import org.marker.protocol.netty.SgipChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SgipClient {
	private static Logger logger = LoggerFactory.getLogger(SgipClient.class);
	private SgipChannelHandler handler;
	public void setHandler(SgipChannelHandler handler) {
		this.handler = handler;
	}
	public void bind(String host,int port,String username,String password) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			ChannelInitializer<SocketChannel> initHandler = new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(320, 0, 4, -4, 0, true));
					ch.pipeline().addLast(handler);
				}
			};
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group);// 绑定俩个线程组
			bootstrap.channel(NioSocketChannel.class);// 指定NIO的模式.NioServerSocketChannel对应TCP, NioDatagramChannel对应UDP
			bootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024); // 这是接收缓冲大小
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);// 保持连接
			bootstrap.handler(initHandler);
			ChannelFuture future = bootstrap.connect(host,port).sync();
		    if (future.isSuccess()) {
		    	logger.info("--[{}]SGIP[host={},port={}]Client Start OK !",Thread.currentThread().getName(), host,port);
		    }else {
		    	logger.error("--[{}]SGIP[host={},port={}]Client Start No !",Thread.currentThread().getName(), host,port);
		    }
		    Channel channel = future.channel();
		    handler.sendBind(channel, username, password);//发送bind请求
			channel.closeFuture().sync();
		}catch(Exception e) {
			logger.error("--SGIP[host="+host+",port="+port+"]Client Start NO !",e);
			try {
				Thread.sleep(10*1000l);
			} catch (InterruptedException ex) {
			}
		} finally {
			group.shutdownGracefully();
		}
    }
}
