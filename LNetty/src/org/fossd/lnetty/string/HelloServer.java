package org.fossd.lnetty.string;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

public class HelloServer {

	public static void main(String[] args) {
		// 初始化channel的辅助类，为具体子类提供公共数据结构
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("encoder", new StringEncoder());
				pipeline.addLast("handler", new HelloWorldServerHandler());
				return pipeline;
			}
		});
		// 创建服务器端channel的辅助类,接收connection请求
		bootstrap.bind(new InetSocketAddress(8080));
	}

}

class HelloWorldServerHandler extends SimpleChannelHandler {
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		e.getChannel().write("Hello, World");
	}

	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		System.out.println("Unexpected exception from downstream." + e.getCause());
		e.getChannel().close();
	}
}