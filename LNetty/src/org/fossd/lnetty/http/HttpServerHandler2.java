package org.fossd.lnetty.http;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class HttpServerHandler2 extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest)e.getMessage();
		String uri = request.getUri();
		HttpMethod method = request.getMethod();
		if (method.equals(HttpMethod.GET)) {
			System.out.println("method:GET");
		}
		System.out.println("uri:" + uri);
		
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		ChannelBuffer buffer = new DynamicChannelBuffer(2048);
		buffer.writeBytes("OK!".getBytes("UTF-8"));
		response.setContent(buffer);
		response.setHeader("Content-Type", "text/html; charset=UTF-8");
		response.setHeader("Content-Length", response.getContent().writerIndex());
		Channel ch = e.getChannel();
		//Write the initials line and the header
		ch.write(response);
		ch.disconnect();
		ch.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Channel ch = e.getChannel();
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		cause.printStackTrace();
		if (ch.isConnected()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);   
        response.setHeader(Names.CONTENT_TYPE, "text/plain; charset=UTF-8");   
        response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));   
  
        // Close the connection as soon as the error message is sent.   
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);   
	}
	
}
