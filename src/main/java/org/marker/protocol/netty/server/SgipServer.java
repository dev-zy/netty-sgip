package org.marker.protocol.netty.server;

import org.marker.protocol.netty.SgipChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class SgipServer {
	private static Logger logger = LoggerFactory.getLogger(SgipServer.class);
	private SgipChannelHandler handler;
	public void setHandler(SgipChannelHandler handler) {
		this.handler = handler;
	}
	public void bind(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ChannelInitializer<SocketChannel> initHandler = new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(320, 0, 4, -4, 0, true));
					ch.pipeline().addLast(handler);
				}
			};
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workGroup);// 绑定俩个线程组
			bootstrap.channel(NioServerSocketChannel.class);// 指定NIO的模式.NioServerSocketChannel对应TCP, NioDatagramChannel对应UDP
			bootstrap.option(ChannelOption.SO_BACKLOG, 128); // 设置TCP缓冲区
			bootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024); // 设置发送缓冲大小
			bootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024); // 这是接收缓冲大小
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);// 保持连接
			bootstrap.childHandler(initHandler);
			ChannelFuture future = bootstrap.bind(port).sync();
			if (future.isSuccess()) {
				logger.info("--SGIP[port={}]Server Start OK !", port);
		    }else {
		    	logger.info("--SGIP[port={}]Server Start NO !", port);
		    }
			future.channel().closeFuture().sync();
		}catch(Exception e) {
			logger.error("--SGIP[port="+port+"]Server Start NO !",e);
			System.exit(1);
		} finally {
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
    }
}
