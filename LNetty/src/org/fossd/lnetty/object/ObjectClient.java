package org.fossd.lnetty.object;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class ObjectClient {

	public static void main(String[] args) {
		// 创建客户端channel的辅助类,发起connection请求
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new PersonDecoder());
				pipeline.addLast("encoder", new PersonEncoder());
				pipeline.addLast("handler", new ObjectClientHandler());
				return pipeline;
			}
		});
		// 创建无连接传输channel的辅助类(UDP),包括client和server
		ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 9999));
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

}

class ObjectClientHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Person person = (Person) e.getMessage();
		System.out.println(person);
		e.getChannel().close();
	}
}