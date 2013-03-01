package org.fossd.lnetty.http;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class HttpClient {

	public static void main(String[] args) {
		String host = "localhost";
		int port = 8080;
		final int firstMessageSize = 32;
		
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new HttpClientHandler(firstMessageSize));
			}
		});

		// Start the connection attempt.
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

		// Wait until the connection is closed or the connection attempt fails.
		future.getChannel().getCloseFuture().awaitUninterruptibly();

		// Shut down thread pools to exit.
		bootstrap.releaseExternalResources();
	}

}

class HttpClientHandler extends SimpleChannelUpstreamHandler {
	private final ChannelBuffer firstMessage;
	private final AtomicLong transferredBytes = new AtomicLong();

	/**
	 * Creates a client-side handler.
	 */
	public HttpClientHandler(int firstMessageSize) {
		if (firstMessageSize <= 0) {
			throw new IllegalArgumentException("firstMessageSize: " + firstMessageSize);
		}
		firstMessage = ChannelBuffers.buffer(firstMessageSize);
		for (int i = 0; i < firstMessage.capacity(); i++) {
			firstMessage.writeByte((byte) i);
		}
	}

	public long getTransferredBytes() {
		return transferredBytes.get();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		// Send the first message. Server will not send anything here
		// because the firstMessage's capacity is 0.
		e.getChannel().write(firstMessage);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		// Send back the received message to the remote peer.
		transferredBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
		e.getChannel().write(e.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		// Close the connection when an exception is raised.
		System.out.println("Unexpected exception from downstream." + e.getCause());
		e.getChannel().close();
	}
}
