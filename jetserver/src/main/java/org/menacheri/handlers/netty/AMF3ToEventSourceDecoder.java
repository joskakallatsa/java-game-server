package org.menacheri.handlers.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.menacheri.event.IEvent;

/**
 * If the incoming event is of type {@link IEvent} then it will only
 * de-serialize the source of the event rather than the whole event object. The
 * de-serialized source is now set as source of the event.
 * 
 * @author Abraham Menacherry
 * 
 */
public class AMF3ToEventSourceDecoder extends AMF3ToJavaObjectDecoder
{
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception
	{
		IEvent event = (IEvent) msg;
		Object source = super.decode(ctx, channel, event.getSource());
		event.setSource(source);
		return event;
	}
}
