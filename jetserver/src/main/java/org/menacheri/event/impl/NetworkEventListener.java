package org.menacheri.event.impl;

import org.menacheri.app.IGameRoom;
import org.menacheri.app.ISession;
import org.menacheri.event.Events;
import org.menacheri.event.IEvent;
import org.menacheri.event.INetworkEvent;
import org.menacheri.event.ISessionEventHandler;

/**
 * A listener class which will be used by {@link IGameRoom} to send
 * {@link INetworkEvent}s to the connected sessions. When the game room
 * publishes such events to its channel, this listener will pick it up and
 * transmit it to the session which in turn will transmit it to the remote
 * machine/vm.
 * 
 * @author Abraham Menacherry
 * 
 */
public class NetworkEventListener implements ISessionEventHandler
{

	private static final int EVENT_TYPE = Events.NETWORK_MESSAGE;
	private final ISession session;

	public NetworkEventListener(ISession session)
	{
		this.session = session;
	}

	@Override
	public void onEvent(IEvent event)
	{
		session.onEvent(event);
	}

	@Override
	public int getEventType()
	{
		return EVENT_TYPE;
	}

	@Override
	public ISession getSession()
	{
		return session;
	}

	@Override
	public void setSession(ISession session)
	{
		throw new UnsupportedOperationException(
				"Session is a final field in this class. "
						+ "It cannot be reset");
	}

}
