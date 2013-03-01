package org.fossd.lnetty.object;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

class PersonEncoder extends SimpleChannelHandler {
	private final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Person person = (Person) e.getMessage();
		buffer.writeInt(person.getName().getBytes("GBK").length);
		buffer.writeBytes(person.getName().getBytes("GBK"));
		buffer.writeInt(person.getAge());
		buffer.writeDouble(person.getSalary());
		Channels.write(ctx, e.getFuture(), buffer);
	}
}