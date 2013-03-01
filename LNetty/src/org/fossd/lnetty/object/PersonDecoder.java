package org.fossd.lnetty.object;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 * FrameDecoder and ReplayingDecoder allow you to return an object of any type.
 * 
 */
class PersonDecoder extends FrameDecoder {
	private final ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer channelBuffer) throws Exception {
		if (channelBuffer.readableBytes() < 4) {
			return null;
		}
		if (channelBuffer.readable()) {
			// 读到,并写入buf
			channelBuffer.readBytes(buffer, channelBuffer.readableBytes());
		}
		int namelength = buffer.readInt();
		String name = new String(buffer.readBytes(namelength).array(), "GBK");
		int age = buffer.readInt();
		double salary = buffer.readDouble();
		Person person = new Person(name, age, salary);
		return person;
	}

}