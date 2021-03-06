package org.menacheri.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.menacheri.app.ISession;
import org.menacheri.event.Events;
import org.menacheri.event.IEvent;
import org.menacheri.event.IEventDispatcher;
import org.menacheri.event.IEventHandler;
import org.menacheri.event.ISessionEventHandler;

public class EventDispatcher implements IEventDispatcher
{
	private static final ExecutorService EXECUTOR = Executors
			.newSingleThreadExecutor();
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				EXECUTOR.shutdown();
			}
		});
	}

	private Map<Integer, List<IEventHandler>> handlersByEventType;
	private List<IEventHandler> genericHandlers;
	private boolean isShuttingDown;

	public EventDispatcher()
	{
		this(new HashMap<Integer, List<IEventHandler>>(2),
				new CopyOnWriteArrayList<IEventHandler>());
	}

	public EventDispatcher(
			Map<Integer, List<IEventHandler>> handlersByEventType,
			List<IEventHandler> genericHandlers)
	{
		this.handlersByEventType = handlersByEventType;
		this.genericHandlers = genericHandlers;
		this.isShuttingDown = false;
	}

	@Override
	public void addHandler(IEventHandler eventHandler)
	{
		int eventType = eventHandler.getEventType();
		synchronized (this)
		{
			if (eventType == Events.ANY)
			{
				genericHandlers.add(eventHandler);
			}
			else
			{
				List<IEventHandler> handlers = this.handlersByEventType
						.get(eventType);
				if (handlers == null)
				{
					handlers = new CopyOnWriteArrayList<IEventHandler>();
					this.handlersByEventType.put(eventType, handlers);
				}

				handlers.add(eventHandler);
			}
		}
	}

	@Override
	public List<IEventHandler> getHandlers(int eventType)
	{
		return handlersByEventType.get(eventType);
	}

	@Override
	public void removeHandler(IEventHandler eventHandler)
	{
		int eventType = eventHandler.getEventType();
		synchronized (this)
		{
			if (eventType == Events.ANY)
			{
				genericHandlers.remove(eventHandler);
			}
			else
			{
				List<IEventHandler> handlers = this.handlersByEventType
						.get(eventType);
				if (null != handlers)
				{
					handlers.remove(eventHandler);
					// Remove the reference if there are no listeners left.
					if (handlers.size() == 0)
					{
						handlersByEventType.put(eventType, null);
					}
				}
			}
		}

	}

	@Override
	public void removeHandlersForEvent(int eventType)
	{
		synchronized (this)
		{
			List<IEventHandler> handlers = this.handlersByEventType
					.get(eventType);
			if (null != handlers)
			{
				handlers.clear();
			}
		}
	}

	@Override
	public boolean removeHandlersForSession(ISession session)
	{
		List<IEventHandler> removeList = new ArrayList<IEventHandler>();
		Collection<List<IEventHandler>> eventHandlersList = handlersByEventType
				.values();
		for (List<IEventHandler> handlerList : eventHandlersList)
		{
			if (null != handlerList)
			{
				for (IEventHandler handler : handlerList)
				{
					if (handler instanceof ISessionEventHandler)
					{
						ISessionEventHandler sessionHandler = (ISessionEventHandler) handler;
						if (sessionHandler.getSession().equals(session))
						{
							removeList.add(handler);
						}
					}
				}
			}
		}
		for (IEventHandler handler : removeList)
		{
			removeHandler(handler);
		}
		return (removeList.size() > 0);
	}

	@Override
	public void fireEvent(final IEvent event)
	{
		boolean isShuttingDown = false;
		synchronized (this)
		{
			isShuttingDown = this.isShuttingDown;
		}
		if (!isShuttingDown)
		{
			EXECUTOR.submit(new Runnable()
			{

				@Override
				public void run()
				{
					for (IEventHandler handler : genericHandlers)
					{
						handler.onEvent(event);
					}

					// retrieval is not thread safe, but since we are not
					// setting it to
					// null anywhere it should be fine.
					List<IEventHandler> handlers = handlersByEventType
							.get(event.getType());
					// Iteration is thread safe since we use copy on write.
					if (null != handlers)
					{
						for (IEventHandler handler : handlers)
						{
							handler.onEvent(event);
						}
					}

				}
			});

		}
		else
		{
			System.err.println("Discarding event: " + event
					+ " as dispatcher is shutting down");
		}

	}

	@Override
	public void close()
	{
		synchronized (this)
		{
			isShuttingDown = true;
			genericHandlers.clear();
			handlersByEventType.clear();
		}

	}

}
